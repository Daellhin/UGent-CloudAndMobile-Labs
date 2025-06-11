package be.ugent.idlab.predict.ocmt.egress.server.modules.graphql

import be.ugent.idlab.predict.ocmt.egress.server.modules.getIDsFluxQuery
import be.ugent.idlab.predict.ocmt.egress.services.Defaults
import be.ugent.idlab.predict.ocmt.egress.services.Influx
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class IDsResponse(
    val events: List<IDsEvent>
)
@Serializable
data class IDsEvent(
    val timestamp: String,
    val id: String,
    val source: String
)

class IDQueryService: Query {
    @GraphQLDescription("Returns an overview of scanned ids. Filterable by start and stop timestamps (epochs in ms) and source tag.")
    suspend fun ids(
        start: String? = null,
        stop: String? = null,
        source: String? = null
    ) : IDsResponse {
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

        val records = Influx.query(getIDsFluxQuery(startInstant, stopInstant, source))
        val events = records.map { record ->
            IDsEvent(
                timestamp = (record.time?.toEpochMilli() ?: 0).toString(),
                id = record.getValueByKey("_value")?.toString() ?: "unknown",
                source = record.getValueByKey("source")?.toString() ?: "unknown"
            )
        }

        return IDsResponse(events)
    }
}
