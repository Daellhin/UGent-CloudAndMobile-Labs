package be.ugent.idlab.predict.ocmt.egress.services.forecasting

import be.ugent.idlab.predict.ocmt.egress.server.modules.getCountFluxQuery
import be.ugent.idlab.predict.ocmt.egress.services.Forecasting
import be.ugent.idlab.predict.ocmt.egress.services.Influx
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.logging.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PredictionRequest(
    val timestamps: List<Long>,
    val counts: List<Int>
)
@Serializable
data class PredictionResponse(
    val timestamps: List<Long>,
    val counts: List<Int>
)

object Model {

    private val LOGGER = KtorSimpleLogger("services.Forecasting.Model")

    private val url = Forecasting.properties["forecasting.url"]
        .toString()

    private val client = HttpClient {
        install(DefaultRequest) {
            header("Accept", "application/json")
            header("Content-type", "application/json")
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json()
        }
    }

    /**
     * Creates predictions using the data available from `source` in influx ranging from `inputStart` and `inputStop`
     *  using the provided `method`. The return value is one of three possible scenarios:
     *  * value is list with valid predictions (result has value and represents success)
     *  * value is empty list (result has no value as there was no data available, and represents irrecoverable failure)
     *  * value is an exception (exception was caused, a reattempt should be done later)
     */
    suspend fun predict(
        inputStart: Instant,
        inputStop: Instant,
        source: String,
        method: String
    ): Result<List<Pair<Instant, Int>>> {
        val records = Influx.query(getCountFluxQuery(inputStart, inputStop, source))
        if (records.isEmpty())
            return Result.success(emptyList())

        val counts = records.map { record -> record.value.toString().toInt() }.takeLast(200)
        val timestamps = records.map { record -> record.time?.toEpochMilli() ?: 0 }.takeLast(200)

        return try {
            val requestBody = PredictionRequest(timestamps, counts)
            val response = client.post("$url/$method") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val responseBody = response.body<PredictionResponse>()
            val predictions = responseBody.timestamps.zip(responseBody.counts) { timestamp, count ->
                Pair(Instant.fromEpochMilliseconds(timestamp), count)
            }

            Result.success(predictions)
        } catch (e: Exception) {
            LOGGER.error("Failed to get predictions: ${e.message}")
            Result.failure(e)
        }
    }

}
