package be.ugent.idlab.predict.ocmt.egress.server.modules.rest

import be.ugent.idlab.predict.ocmt.egress.server.modules.getSourcesFluxQuery
import be.ugent.idlab.predict.ocmt.egress.services.Influx
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import io.github.smiley4.ktoropenapi.resources.get
import io.ktor.http.*
import io.ktor.resources.*

@Serializable
data class SourcesResponse(
    val sources: List<String>
)

@Resource("sources")
class Sources(
)
fun Route.sources() {
    get<Sources>({
        summary="Get all sources"
        description = "Returns an overview of all sources that ever submitted data"
        response {
            HttpStatusCode.OK to { body<SourcesResponse> {} }
        }
    }) { _ ->
        val records = Influx.query(getSourcesFluxQuery())
        val foundSources = records.mapNotNull { record ->
            record.getValueByKey("source")?.toString()
        }

        call.respond(SourcesResponse(foundSources))
    }
}