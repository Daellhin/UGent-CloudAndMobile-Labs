package be.ugent.idlab.predict.ocmt.android.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json

val httpClient = HttpClient(CIO) {
    install(DefaultRequest) {
        header("Accept", "application/json")
        header("Content-type", "application/json")
        contentType(ContentType.Application.Json)
    }
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = false
        })
    }

}

internal val _errors = MutableSharedFlow<Throwable>()
val errors = _errors.asSharedFlow()
