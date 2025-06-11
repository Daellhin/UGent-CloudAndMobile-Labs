package be.ugent.idlab.predict.ocmt.egress.server.modules.security

import be.ugent.idlab.predict.ocmt.egress.services.auth.RateLimiter
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respond

val BackoffPlugin = createApplicationPlugin("BackoffPlugin") {
    onCall { call ->
        // Check if the host is rate-limited
        if (RateLimiter.isRateLimited(call)) {
            call.respond(HttpStatusCode.TooManyRequests, "You are temporarily banned due to repeated invalid requests.")
            return@onCall
        }
    }

    onCallRespond { call, statusCode ->
        // Observe responses and handle bad calls
        if (statusCode == HttpStatusCode.Unauthorized || statusCode == HttpStatusCode.BadRequest) {
            RateLimiter.onBadCallMade(call)
        }
    }
}
