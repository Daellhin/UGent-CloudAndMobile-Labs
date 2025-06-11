package be.ugent.idlab.predict.ocmt.egress.server.modules.rest

import be.ugent.idlab.predict.ocmt.egress.server.modules.getIDsFluxQuery
import be.ugent.idlab.predict.ocmt.egress.services.Defaults
import be.ugent.idlab.predict.ocmt.egress.services.Influx
import io.github.smiley4.ktoropenapi.resources.get
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class IDsResponse(
    val events: List<IDsEvent>
)
@Serializable
data class IDsEvent(
    val timestamp: Long,
    val id: String,
    val source: String
)

@Resource("ids")
class IDs(
    val start: Long? = null,
    val stop: Long? = null,
    val source: String? = null
)
fun Route.ids() {
    get<IDs>({
        summary="Get scanned ids overview"
        description = "Returns an overview of scanned ids. Filterable by start and stop timestamps (epochs in ms) and source tag."
        response {
            HttpStatusCode.OK to { body<IDsResponse> {} }
        }
    }) { ids ->
        val start = if(ids.start != null) Instant.fromEpochMilliseconds(ids.start) else Defaults.start()
        val stop = if(ids.stop != null) Instant.fromEpochMilliseconds(ids.stop) else Defaults.stop()
        val source = ids.source

        val records = Influx.query(getIDsFluxQuery(start, stop, source))
        val events = records.map { record ->
            IDsEvent(
                timestamp = record.time?.toEpochMilli() ?: 0,
                id = record.getValueByKey("_value")?.toString() ?: "unknown",
                source = record.getValueByKey("source")?.toString() ?: "unknown"
            )
        }

        call.respond(IDsResponse(events))
    }
}
