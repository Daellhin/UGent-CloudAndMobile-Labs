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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Counts service responsible for interacting with raw count observations exposed by the egress API.
 *  Uses the active session's token retrieved through `context.userSession` to create an authorized
 *  request to the egress instance in the cloud, retrieving raw count information using parameters
 *  set by the user (or defaults). These parameters are similar to the ones found in the egress API:
 *   * source (string): directly used in egress
 *   * interval (duration): used to calculate the oldest time to retrieve data samples
 *     for (retrieving all data between `now - interval` & `now`); defaults to 5 minutes
 *   * period (duration): frequency at which new data is requested; defaults to 20 seconds
 *
 * Results are exposed through a `Flow`, with its values being observed and shown in UI.
 */
class CountsService(
    private val context: Context
) {

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class Response(
        val events: List<Event>
    ) {
        @Serializable
        data class Event(
            val timestamp: Long,
            val value: Int,
            val source: String?
        )
    }

    fun observe(
        source: String,
        start: Instant?,
        end: Instant?,
        interval: Duration = 30.minutes,
        period: Duration = 20.seconds
    ): Flow<List<Response.Event>> {
        return flow {
            while (true) {
                try {
                    val end = end ?: Clock.System.now()
                    val start = start ?: (end - interval)
                    val response = context.userSession.authorizedRequest {
                        url(Egress.counts(start, end, source))
                        method = HttpMethod.Get
                    }

                    if (response.status != HttpStatusCode.OK) {
                        throw Exception("Failed to retrieve counts from source '${source}': ${response.status}")
                    }

                    val events = response.body<Response>().events

                    emit(events)
                    delay(period)
                } catch (e: Exception) {
                    _errors.emit(e)
                }
            }
        }
    }

}
