package be.ugent.idlab.predict.ocmt.android.data.service

import android.annotation.SuppressLint
import android.content.Context
import be.ugent.idlab.predict.ocmt.android.data.Egress
import be.ugent.idlab.predict.ocmt.android.data._errors
import be.ugent.idlab.predict.ocmt.android.util.userSession
import io.ktor.client.call.body
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * Sources service responsible for interacting with all registered sources in the egress API.
 *  Uses the active session's token retrieved through `context.userSession` to create an authorized
 *  request to the egress instance in the cloud, retrieving the list of sources.
 *
 * These sources are made available through a StateFlow: the initial value is an empty list, which
 *  changes to the available sources whenever `refresh()` is called. Furthermore, this value should
 *  be updated whenever the session changes (e.g. user signs in), so the user experience is smooth.
 */
class SourcesService(
    private val context: Context,
    private val scope: CoroutineScope
) {

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class SourcesResponse(
        val sources: List<String>
    )

    private val _sources = MutableStateFlow<List<String>>(emptyList())
    val sources = _sources.asStateFlow()

    init {
        scope.launch {
            context.userSession.state.collect {
                if (it != null) {
                    refresh()
                } else {
                    // no one is signed in, no sources should be available
                    _sources.update { emptyList() }
                }
            }
        }
    }

    fun refresh() {
        scope.launch {
            try {
                val response = context.userSession.authorizedRequest {
                    url(Egress.sources)
                    method = HttpMethod.Get
                }

                if (response.status != HttpStatusCode.OK) {
                    throw Exception("Failed to get sources: ${response.status}")
                }

                val sources = response.body<SourcesResponse>().sources
                _sources.update { sources }
            } catch (e: Exception) {
                _errors.emit(e)
            }
        }
    }

}
