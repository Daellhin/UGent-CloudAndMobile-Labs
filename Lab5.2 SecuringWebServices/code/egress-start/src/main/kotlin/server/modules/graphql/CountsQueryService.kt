package be.ugent.idlab.predict.ocmt.egress.server.modules.graphql

import be.ugent.idlab.predict.ocmt.egress.server.modules.getCountFluxQuery
import be.ugent.idlab.predict.ocmt.egress.services.Defaults
import be.ugent.idlab.predict.ocmt.egress.services.Influx
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import io.ktor.server.response.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EventData(
    val timestamp: String,
    val value: Int,
    val source: String
)
@Serializable
data class CountsResponse(
    val events: List<EventData>
)

class CountsQueryService: Query {
    @GraphQLDescription("Returns an overview of the changing count values over time. Filterable by start and stop timestamps (epochs in ms) and source tag.")
    suspend fun counts(
        start: String? = null,
        stop: String? = null,
        source: String? = null
    ) : CountsResponse {
        val startInstant = try {
            if (start != null) Instant.fromEpochMilliseconds(start.toLong()) else Defaults.start()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid start timestamp: $start. Expected a numeric value representing epoch time in ms")
        }
        val stopInstant = try {
            if (stop != null) Instant.fromEpochMilliseconds(stop.toLong()) else Defaults.stop()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid stop timestamp: $stop. Expected a numeric value representing epoch time in ms")
        }

        val records = Influx.query( getCountFluxQuery(startInstant, stopInstant, source))
        val events = records.map { record ->
            EventData(
                timestamp = (record.time?.toEpochMilli() ?: 0).toString(),
                value = record.value.toString().toInt(),
                source = record.getValueByKey("source")?.toString() ?: "unknown"
            )
        }

        return CountsResponse(events)
    }
}
