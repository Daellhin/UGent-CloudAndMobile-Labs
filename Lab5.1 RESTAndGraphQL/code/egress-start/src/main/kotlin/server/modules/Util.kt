package be.ugent.idlab.predict.ocmt.egress.server.modules

import com.influxdb.query.dsl.Flux
import com.influxdb.query.dsl.functions.restriction.ColumnRestriction
import com.influxdb.query.dsl.functions.restriction.Restrictions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

internal suspend fun onError(call: ApplicationCall, cause: Throwable) {
    val developmentMode = call.application.environment.config.propertyOrNull("ktor.deployment.development")?.getString()?.toBoolean() ?: false
    if (developmentMode) {
        call.respond(HttpStatusCode.InternalServerError, cause.stackTraceToString())
    } else {
        call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
    }
}

internal suspend fun onNotFound(call: ApplicationCall, code: HttpStatusCode) {
    call.respondText(status = code, text = "404: Page Not Found")
}

fun Flux.filter(restrictions: Restrictions?): Flux {
    return if (restrictions != null) filter(restrictions) else this
}

fun ColumnRestriction.equal(value: Any?) = value?.let { equal(it) }
