package be.ugent.idlab.predict.ocmt.egress.services

import be.ugent.idlab.predict.ocmt.egress.services.forecasting.ServiceCache
import be.ugent.idlab.predict.ocmt.egress.services.forecasting.ServiceSyncJob
import io.ktor.util.logging.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.datetime.Instant
import java.util.*
import kotlin.time.Duration.Companion.seconds

object Forecasting {

    private val LOGGER = KtorSimpleLogger("services.Forecasting")

    val properties = this::class.java
        .classLoader
        .getResourceAsStream("forecasting.properties")
        .use { Properties().apply { load(it) } }

    val methods = properties["forecasting.features"]
        .toString()
        .split(',')
    val defaultMethod = methods.first()

    private val syncDelay = properties["forecasting.interval"]
        .toString()
        .toInt()
        .seconds

    private lateinit var syncJobs: List<ServiceSyncJob>

    fun start() {
        syncJobs = properties["forecasting.sources"]
            .toString()
            .split(',')
            // "cartesian product" with all methods, as all method - source pairs should be synced
            .flatMap { source -> methods.map { method -> method to source } }
            .map { (method, source) ->
                // it's safe to use global scope here: these sync jobs last the entire application's lifetime
                @OptIn(DelicateCoroutinesApi::class)
                ServiceSyncJob(method = method, source = source, syncDelay = syncDelay, scope = GlobalScope)
            }
            // starting all the sync jobs as well
            .onEach { it.start() }
            .also { LOGGER.info("Started all sync processes!") }
    }

    fun stop() {
        LOGGER.info("Stop called, stopping sync processes!")
        syncJobs.forEach { it.stop() }
    }

    /**
     * Returns a series of predictions ranging from `start` till `stop` in a list.
     */
    fun getPredictions(
        time: Instant,
        source: String,
        method: String = defaultMethod
    ): List<Pair<Instant, Int>>? {
        // simply forwarding the call to the cache, which is potentially `null`
        return ServiceCache.get(
            method = method,
            source = source,
            time = time
        )
    }

}
