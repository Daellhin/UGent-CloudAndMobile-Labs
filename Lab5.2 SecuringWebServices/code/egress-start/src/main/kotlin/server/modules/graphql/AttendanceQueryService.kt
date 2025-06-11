package be.ugent.idlab.predict.ocmt.egress.server.modules.graphql

import be.ugent.idlab.predict.ocmt.egress.server.modules.getAttendanceFluxQuery
import be.ugent.idlab.predict.ocmt.egress.services.Defaults
import be.ugent.idlab.predict.ocmt.egress.services.Influx
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceResponse(
    val events: List<AttendanceEvent>
)
@Serializable
data class AttendanceEvent(
    val timestamp: String, // Long is not supported in GraphQL
    val id: String,
    val arrival: Boolean,
    val source: String
)

class AttendanceQueryService: Query {
    @GraphQLDescription("Returns an overview of the attendance over time. Filterable by start and stop timestamps (epochs in ms) and source tag.")
    suspend fun attendance(
        start: String? = null,
        stop: String? = null,
        source: String? = null
    ): AttendanceResponse  {
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

        val records = Influx.query(getAttendanceFluxQuery(startInstant, stopInstant, source))
        val events = records.map { record ->
            AttendanceEvent(
                timestamp = (record.time?.toEpochMilli() ?: 0).toString(),
                id = record.getValueByKey("_value")?.toString() ?: "unknown",
                arrival = record.getValueByKey("arrival")?.toString()?.toBoolean() ?: false,
                source = record.getValueByKey("source")?.toString() ?: "unknown"
            )
        }

        return AttendanceResponse(events)
    }
}
