package be.ugent.idlab.predict.ocmt.egress.server.modules.security

import be.ugent.idlab.predict.ocmt.egress.services.auth.SessionManager
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configure() {
    configureAuth()
    configureSessionEndpoints()
    install(BackoffPlugin)
}

private fun Application.configureAuth() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(SessionManager.secret))
                    .withIssuer(SessionManager.issuer)
                    .withAudience(SessionManager.audience)
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("username").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = "Invalid or missing token"
                )
            }
        }
    }
}

private fun Application.configureSessionEndpoints() {
    routing {
        install(ContentNegotiation) {
            json()
        }
        post("/register") {
            val credentials = call.receive<SessionManager.Credentials>()
            val result = SessionManager.createAccount(credentials)

            print("hi")
            result.fold(
                onSuccess = {
                    call.respond(HttpStatusCode.Created, "Account created successfully")
                },
                onFailure = { exception ->
                    when (exception) {
                        is SessionManager.AlreadyExists -> call.respond(HttpStatusCode.Conflict, "Username already taken")
                        else -> call.respond(HttpStatusCode.InternalServerError, "Failed to create account")
                    }
                }
            )
        }
        post("/login") {
            val credentials = call.receive<SessionManager.Credentials>()
            val result = SessionManager.processLogin(credentials)

            result.fold(
                onSuccess = { token ->
                    call.respond(HttpStatusCode.OK, token)
                },
                onFailure = { exception ->
                    when (exception) {
                        is SessionManager.InvalidCredentials -> call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                        else -> call.respond(HttpStatusCode.InternalServerError, "Failed to process login")
                    }
                }
            )
        }
        authenticate {
            get("me") {
                val name = call.principal<JWTPrincipal>()!!.payload.getClaim("username")!!.asString()
                call.respond("Hi, $name")
            }
        }
    }
}

fun Routing.authenticate(block: Route.() -> Unit) {
    authenticate("auth-jwt", build = block)
}
