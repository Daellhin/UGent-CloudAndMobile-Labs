package be.ugent.idlab.predict.ocmt.android.data.service

import android.content.Context
import android.content.SharedPreferences
import be.ugent.idlab.predict.ocmt.android.data.Credentials
import be.ugent.idlab.predict.ocmt.android.data.Egress
import be.ugent.idlab.predict.ocmt.android.data.User
import be.ugent.idlab.predict.ocmt.android.data._errors
import be.ugent.idlab.predict.ocmt.android.data.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.content.edit

/**
 * User session instance, responsible for containing the session token obtained when logging in,
 *  logging back out, and providing the ability for users to register new accounts.
 */
class UserSession(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow<User?>(null)
    val state = _state.asStateFlow()

    fun loadToken() {
        scope.launch {
            val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val savedToken = sharedPreferences.getString("session_token", null)
            if (!savedToken.isNullOrEmpty()) {
                print("token found")
                _state.emit(User(token = savedToken))
            } else {
                print("no token found")
            }
        }
    }

    fun login(
        credentials: Credentials,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        scope.launch {
            login(credentials)
                .onFailure { onFailure() }
                .onFailure { _errors.emit(it) }
                .onSuccess { session ->
                    val sharedPreferences = this@UserSession.context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                    sharedPreferences.edit { putString("session_token", session.token) }
                    _state.emit(session)
                    onSuccess()
                }
        }
    }

    fun logout() {
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        sharedPreferences.edit { remove("session_token") }
        _state.update { null }
    }

    fun register(
        credentials: Credentials,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        scope.launch {
            register(credentials)
                .onFailure { onFailure() }
                .onFailure { _errors.emit(it) }
                .onSuccess { onSuccess() }
        }
    }

    private suspend fun login(credentials: Credentials): Result<User> {
        return runCatching {
            val response = httpClient.post(Egress.login) {
                contentType(ContentType.Application.Json)
                setBody(credentials)
            }
            if (response.status != HttpStatusCode.OK) {
                throw Exception("Login failed with status: ${response.status}")
            }
            response.body<User>()
        }
    }

    private suspend fun register(credentials: Credentials): Result<Unit> {
        return runCatching {
            val response = httpClient.post(Egress.register) {
                contentType(ContentType.Application.Json)
                setBody(credentials)
            }
            if (response.status != HttpStatusCode.OK) {
                throw Exception("Register failed with status: ${response.status}")
            }
            Unit
        }
    }

}
