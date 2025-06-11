package be.ugent.idlab.predict.ocmt.egress.services

import kotlinx.datetime.Clock
import java.util.*
import kotlin.time.Duration

object Defaults {

    private val properties = this::class.java
        .classLoader
        .getResourceAsStream("defaults.properties")
        .use { Properties().apply { load(it) } }

    val START_OFFSET = Duration.parse(properties["egress.defaultStart"].toString())

    val STOP_OFFSET = Duration.parse(properties["egress.defaultStop"].toString())

    fun start() = Clock.System.now() + START_OFFSET
    fun stop() = Clock.System.now() + STOP_OFFSET

}
