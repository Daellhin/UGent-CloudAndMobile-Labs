package be.ugent.idlab.predict.ocmt.egress.server.modules.rest

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.module() {
    routing {
        route("/rest") {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                })
            }
            sources()
            counts()
            ids()
            attendance()
            forecast()
        }
    }
}
