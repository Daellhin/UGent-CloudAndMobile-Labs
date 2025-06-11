package be.ugent.idlab.predict.ocmt.egress.services.auth

import be.ugent.idlab.predict.ocmt.egress.services.resolve
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.util.logging.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

object RateLimiter {

    private val LOGGER = KtorSimpleLogger("services.RateLimiter")

    private val properties = this::class.java
        .classLoader
        .getResourceAsStream("auth.properties")
        .use { Properties().apply { load(it) } }

    // behaviour config
    private val timeout = properties.resolve("rl.timeout").toInt().milliseconds
    private val period = properties.resolve("rl.period").toInt().milliseconds
    private val frequency = properties.resolve("rl.frequency").toInt()

    // recent bad calls not yet banned
    private val pending = mutableMapOf<String, SortedSet<Instant>>()
    // hosts currently banned, paired with their ban end
    private val banned = mutableMapOf<String, Instant>()

    fun isRateLimited(call: ApplicationCall): Boolean {
        update()
        // checking if host is in there
        return (call.host in banned)
            .also { banned -> if (banned) LOGGER.info("Requester ${call.host} is currently banned") }
    }

    fun onBadCallMade(call: ApplicationCall) {
        update()
        val host = call.host
        if (host in banned) {
            // increasing their timeout by re-banning them
            ban(host)
            return
        }
        val history = pending.getOrPut(host) { sortedSetOf() }
        if (history.size == frequency - 1) {
            ban(host)
        } else {
            history.add(Clock.System.now())
        }
    }

    private fun ban(host: String) {
        pending.remove(host)
        banned[host] = Clock.System.now() + timeout
        LOGGER.info("Banned (or extended existing ban for) $host")
    }

    private fun update() {
        // updating the ban list
        banned.entries.removeAll { it.value < Clock.System.now() }
        // updating the pending state
        val threshold = Clock.System.now() - period
        pending.mapValues { it.value.prune(threshold = threshold) }
    }

    private fun SortedSet<Instant>.prune(threshold: Instant = Clock.System.now()): SortedSet<Instant> {
        while (isNotEmpty() && first() < threshold) {
            remove(first())
        }
        return this
    }

    private val ApplicationCall.host get() = request.origin.remoteAddress

}
