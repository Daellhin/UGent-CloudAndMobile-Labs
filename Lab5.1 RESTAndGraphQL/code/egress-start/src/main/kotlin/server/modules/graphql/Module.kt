package be.ugent.idlab.predict.ocmt.egress.server.modules.graphql

import com.expediagroup.graphql.server.ktor.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {
    install(GraphQL) {
        schema {
            packages = listOf("be.ugent.idlab.predict.ocmt.egress")
            queries = listOf(
                SourcesQueryService(),
                CountsQueryService(),
                IDQueryService(),
                AttendanceQueryService()
            )
        }
    }
    routing {
        graphQLGetRoute("graphql")
        graphQLPostRoute("graphql")
        graphiQLRoute("graphiql")
        graphQLSDLRoute("sdl")
    }
}
