package be.ugent.idlab.predict.ocmt.egress.server.modules.rest

import be.ugent.idlab.predict.ocmt.egress.server.modules.getAttendanceFluxQuery
import be.ugent.idlab.predict.ocmt.egress.services.Defaults
import be.ugent.idlab.predict.ocmt.egress.services.Influx
import io.ktor.resources.*
import io.ktor.server.routing.*
import io.github.smiley4.ktoropenapi.resources.get
import io.ktor.http.*
import io.ktor.server.response.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceResponse(
    val events: List<AttendanceEvent>
)
@Serializable
data class AttendanceEvent(
    val timestamp: Long,
    val id: String,
    val arrival: Boolean,
    val source: String
)

@Resource("attendance")
class Attendance(   
    val start: Long? = null,
    val stop: Long? = null,
    val source: String? = null
)
fun Route.attendance() {
    get<Attendance>({
        summary = "Get attendance over time"
        description = "Returns an overview of the attendance over time. Filterable by start and stop timestamps (epochs in ms) and source tag."
        response {
            HttpStatusCode.OK to { body<AttendanceResponse> {} }
        }
    }) { attendance ->
        val start = if(attendance.start != null) Instant.fromEpochMilliseconds(attendance.start) else Defaults.start()
        val stop = if(attendance.stop != null) Instant.fromEpochMilliseconds(attendance.stop) else Defaults.stop()
        val source = attendance.source

        val records = Influx.query(getAttendanceFluxQuery(start, stop, source))
        val events = records.map { record ->
            AttendanceEvent(
                timestamp = record.time?.toEpochMilli() ?: 0,
                id = record.getValueByKey("_value")?.toString() ?: "unknown",
                arrival = record.getValueByKey("arrival")?.toString()?.toBoolean() ?: false,
                source = record.getValueByKey("source")?.toString() ?: "unknown"
            )
        }
        call.respond(AttendanceResponse(events))
    }
}
