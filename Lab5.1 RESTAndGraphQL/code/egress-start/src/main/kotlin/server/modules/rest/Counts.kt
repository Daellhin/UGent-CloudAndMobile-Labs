package be.ugent.idlab.predict.ocmt.egress.server.modules.rest

import be.ugent.idlab.predict.ocmt.egress.server.modules.getCountFluxQuery
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
data class EventData(
    val timestamp: Long,
    val value: Int,
    val source: String
)
@Serializable
data class CountsResponse(
    val events: List<EventData>
)

@Resource("counts")
class Counts(
    val start: Long? = null,
    val stop: Long? = null,
    val source: String? = null
)
fun Route.counts() {
    get<Counts>({
        summary = "Get counts over time"
        description =
            "Returns an overview of the changing count values over time. Filterable by start and stop timestamps (epochs in ms) and source tag."
        response {
            HttpStatusCode.OK to { body<CountsResponse> {} }
        }
    }) { counts ->
        val start = if(counts.start != null) Instant.fromEpochMilliseconds(counts.start) else Defaults.start()
        val stop = if(counts.stop != null) Instant.fromEpochMilliseconds(counts.stop) else Defaults.stop()
        val source = counts.source

        val records = Influx.query( getCountFluxQuery(start, stop, source))
        val events = records.map { record ->
            EventData(
                timestamp = record.time?.toEpochMilli() ?: 0,
                value = record.value.toString().toInt(),
                source = record.getValueByKey("source")?.toString() ?: "unknown"
            )
        }

        call.respond(CountsResponse(events))
    }
}