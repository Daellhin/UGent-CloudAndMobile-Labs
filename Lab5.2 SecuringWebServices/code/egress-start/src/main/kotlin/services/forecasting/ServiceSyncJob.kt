package be.ugent.idlab.predict.ocmt.egress.services.forecasting

import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlinx.coroutines.*

/**
 * Job instance responsible for asynchronously (coroutines) & periodically (looped with delay) getting new
 *  predictions made through the Model object. Predictions generated by this background process will be
 *  stored in ServiceCache and made accessible through the Forecasting object. Therefore, it is not directly
 *  exposed by any job instance.
 */
class ServiceSyncJob(
    // - prediction parameters
    /** The name of the source for which inputs are gathered **/
    private val source: String,
    /** Method used for creating predictions using the model **/
    private val method: String,
    // - interval parameters
    /** The delay between successful sync calls / size of the individual input intervals **/
    private val syncDelay: Duration,
    /** The beginning of the input interval used when generating new predictions **/
    private var begin: Instant = Clock.System.now() - syncDelay,
    /** The coroutine scope used by this job instance **/
    private val scope: CoroutineScope,

    private var job: Job? = null
) {

    private val LOGGER = KtorSimpleLogger("services.Forecasting.ServiceSyncJob-$source-$method")

    /**
     * This job's start method, responsible for starting its synchronisation logic inside of the provided [scope]
     *  instance, respecting the various parameters provided to this instance:
     *  * [source] defining which inputs are gathered when creating predictions;
     *  * [method] defining what prediction method to use;
     *  * [syncDelay] defining the size of the window that is being predicted for, as well as the delay
     *    between *successful* predictions;
     *  * [begin] defining the start of the next refresh window, which is *updated* after
     *    every *successful* prediction.
     *
     * This method is only called *once*, and is expected to synchronise periodically, until it's [stop] method is
     *  called.
     */
    fun start() {
        if (job != null)
            return
        
        job = scope.launch {
            while (isActive) {
                try {
                    val now = Clock.System.now()
                    val result = Model.predict(begin, now, source, method)
                    result.fold(
                        onSuccess = { predictions ->
                            if (predictions.isNotEmpty())
                                ServiceCache.put(method, source, predictions)
                            LOGGER.info("Stored ${predictions.size} predictions in cache for source: $source, method: $method")
                            begin = now
                        },
                        onFailure = { error ->
                            LOGGER.error("Failed to get predictions: ${error.message}")
                        }
                    )
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    LOGGER.error("Error in sync job: ${e.message}", e)
                } finally {
                    delay(syncDelay)
                }
            }
        }
    }

    /**
     * This job's stop method, stopping the job logic that was started using [start] (or does nothing if [start] was
     *  never called)
     */
    fun stop() {
        job?.let {
            LOGGER.info("Stopping sync job for source: $source, method: $method")
            it.cancel()
            job = null
        } ?: LOGGER.warn("No job running, ignoring stop request")
    }
}