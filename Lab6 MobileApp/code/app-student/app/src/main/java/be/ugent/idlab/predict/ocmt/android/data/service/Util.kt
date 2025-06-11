package be.ugent.idlab.predict.ocmt.android.data.service

import be.ugent.idlab.predict.ocmt.android.data.httpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

suspend inline fun UserSession.authorizedRequest(
    block: HttpRequestBuilder.() -> Unit
): HttpResponse {
    val token = state.value?.token ?: throw IllegalStateException("Not signed in!")
    println("Preparing request....")
    val response = httpClient.request {
        block()
        header(key = "Authorization", value = "Bearer $token")
        println("Sending out a request: ${url.buildString()}")
    }
    // token is no longer valid, so logging out
    if (response.status == HttpStatusCode.Unauthorized) {
        println("Unauthorized!")
        logout()
        throw IllegalStateException("Session is no longer valid!")
    }
    println("Valid response received")
    return response
}
