package be.ugent.idlab.predict.ocmt.egress.server.modules.graphql

import com.expediagroup.graphql.server.ktor.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import be.ugent.idlab.predict.ocmt.egress.server.modules.security.authenticate
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLGetRoute

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
        authenticate {
            graphQLGetRoute("graphql")
        }
    }
}
