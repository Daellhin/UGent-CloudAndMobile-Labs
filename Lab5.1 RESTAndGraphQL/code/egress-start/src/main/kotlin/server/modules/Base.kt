package be.ugent.idlab.predict.ocmt.egress.server.modules

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.github.smiley4.ktoropenapi.*
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktorswaggerui.*
import io.ktor.server.routing.*
import io.ktor.server.resources.Resources

fun Application.setup() {
    install(StatusPages) {
        exception<Throwable>(handler = ::onError)
        status(HttpStatusCode.NotFound, handler = ::onNotFound)
    }
    install(OpenApi) {
        schemas { generator = SchemaGenerator.kotlinx {} }
        autoDocumentResourcesRoutes = true
    }
    install(Resources)
    routing {
        trace {
            application.log.info("Incoming call: {}", it.call.request.uri.replace("\n", "\\n"))
        }
        get("/", {
            summary = "Index";
            description = "Hello world route"
            response { HttpStatusCode.OK to { body<String> {} } }
        }) {
            call.respond(HttpStatusCode.Companion.OK, "Hello world")
        }
        route("api.json") {
            openApi()
        }
        route("swagger") {
            swaggerUI("/api.json") {
                displayRequestDuration = true
            }
        }
    }
}
