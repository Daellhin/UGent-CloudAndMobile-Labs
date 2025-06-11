package be.ugent.idlab.predict.ocmt.egress.server.modules.rest

import be.ugent.idlab.predict.ocmt.egress.services.Defaults
import io.ktor.server.routing.*
import be.ugent.idlab.predict.ocmt.egress.services.Forecasting
import be.ugent.idlab.predict.ocmt.egress.services.forecasting.ServiceCache
import io.ktor.http.*
import io.ktor.server.response.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import io.github.smiley4.ktoropenapi.resources.get
import io.ktor.resources.*
import kotlinx.datetime.Clock

@Serializable
data class PredictionData(
    val timestamp: Long,
    val value: Int
)

@Serializable
data class ForecastResponse(
    val source: String,
    val method: String,
    val predictions: List<PredictionData>
)

@Resource("forecast")
class Forecast(
    val time: Long? = null,
    val source: String,
    val method: String
)
fun Route.forecast() {
    get<Forecast>({
        summary="Get the forcast for a given source and method"
        description="Returns the best fitting forecast given the timestamp`time` (epochs in ms) or using default value Clock.System.now(), according to the provided source value (tag)"
        response {
            HttpStatusCode.OK to { body<ForecastResponse> {} }
        }
    }) { forecast ->
        val timeInstant = if(forecast.time != null) Instant.fromEpochMilliseconds(forecast.time) else Clock.System.now()
        val source = forecast.source
        val method = forecast.method

        val predictions = ServiceCache.get(method, source, timeInstant)
        if (predictions.isNullOrEmpty()) {
            call.respond(
                HttpStatusCode.NotFound,
                "No predictions available for the given source: $source and method: $method"
            )
            return@get
        }
        val formattedPredictions = predictions.map { pair ->
            PredictionData(pair.first.toEpochMilliseconds(), pair.second)
        }

        call.respond(
            ForecastResponse(
                source = source,
                method = method,
                predictions = formattedPredictions
            )
        )
    }
}