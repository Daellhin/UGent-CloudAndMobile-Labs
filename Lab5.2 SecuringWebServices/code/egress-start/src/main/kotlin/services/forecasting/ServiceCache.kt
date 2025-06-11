package be.ugent.idlab.predict.ocmt.egress.services.forecasting

import be.ugent.idlab.predict.ocmt.egress.services.Forecasting
import io.ktor.util.logging.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*
import kotlin.time.Duration.Companion.seconds

object ServiceCache {

    private val LOGGER = KtorSimpleLogger("services.Forecasting.ServiceCache")

    private val retention = Forecasting
        .properties["forecasting.cache_retention"]
        .toString()
        .toInt()
        .seconds

    // cache layout: method -> source -> end of input data -> response data
    private val cache: Map<String, MutableMap<String, SortedMap<Instant, List<Pair<Instant, Int>>>>> = buildMap {
        Forecasting.methods.forEach { method -> put(method, mutableMapOf()) }
    }

    /**
     * Inserts a new set of predictions associated with `method` and `source`, putting the start time of the predictions
     *  as the start time of the received `data`
     */
    fun put(
        method: String,
        source: String,
        data: List<Pair<Instant, Int>>
    ) {
        if (data.isEmpty()) {
            LOGGER.warn("Empty data received in `put` for source `$source` and method `$method`, ignoring...")
            return
        }
        cache[method]!!
            .getOrPut(source) { sortedMapOf() }
            // dropping all cache entries where the requests are too old according to their start time
            .also { it
                .headMap(Clock.System.now() - retention)
                .also { map ->
                    LOGGER.info("Clearing ${map.size} entry/entries containing ${map.values.sumOf { it.size }} prediction(s) (starting timestamp(s) ${map.keys})")
                }
                .clear()
            }
            .put(data.first().first, data)
    }

    /**
     * Returns the list of cached predictions containing the best fit for the provided `time` (which is
     *  the set of predictions where the start time is closest but older than `time`)
     */
    fun get(
        method: String = Forecasting.defaultMethod,
        source: String,
        time: Instant
    ): List<Pair<Instant, Int>>? = cache[method]!![source]
        ?.headMap(time)
        ?.runCatching { this[lastKey()] }
        ?.getOrNull()

}
