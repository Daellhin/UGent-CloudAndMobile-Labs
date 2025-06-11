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
import kotlin.time.Duration.Companion.seconds

/**
 * Forecasting service responsible for interacting with cached forecasts exposed by the egress API.
 *  Uses the active session's token retrieved through `context.userSession` to create an authorized
 *  request to the egress instance in the cloud, retrieving forecasts using parameters
 *  set by the user (or defaults). These parameters are similar to the ones found in the egress API:
 *   * source (string): directly used in egress
 *   * period (duration): frequency at which new data is requested; defaults to 20 seconds
 *
 * The forecasts retrieved are by default always the most recent ones, using current system time as
 *  timestamp.
 * Results are exposed through a `Flow`, with its values being observed and shown in UI.
 */
class ForecastService(
    private val context: Context
) {

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class Response(
        val source: String,
        val method: String,
        val predictions: List<Prediction>
    ) {
        @Serializable
        data class Prediction(
            val timestamp: Long,
            val value: Int,
        )
    }

    fun observe(
        source: String,
        time: Instant? = null,
        period: Duration = 20.seconds
    ): Flow<List<Response.Prediction>> {
        return flow {
            while (true) {
                try {
                    val now = time?: Clock.System.now()
                    val response = context.userSession.authorizedRequest {
                        url(Egress.forecast(now, source))
                        method = HttpMethod.Get
                    }

                    if (response.status != HttpStatusCode.OK) {
                        throw Exception("Failed to get forecasts from source '${source}': ${response.status}")
                    }

                    val predictions = response.body<Response>().predictions

                    emit(predictions)
                    delay(period)
                } catch (e: Exception) {
                    _errors.emit(e)
                }
            }
        }
    }

}
