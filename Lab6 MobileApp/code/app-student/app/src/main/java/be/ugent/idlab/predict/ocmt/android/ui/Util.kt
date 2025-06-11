package be.ugent.idlab.predict.ocmt.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import be.ugent.idlab.predict.ocmt.android.util.elapsedString
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun Number.format(decimals: Int) = "%.${decimals}f".format(toFloat())

@Composable
fun Instant.elapsedTime() = produceState(
    initialValue = elapsedString(),
    key1 = this
) {
    while (true) {
        val elapsed = this@elapsedTime - Clock.System.now()
        val delay = when {
            elapsed.inWholeHours != 0L -> 1.hours
            elapsed.inWholeMinutes != 0L -> 1.minutes
            // going faster than once a second looks weird
            else -> 1.seconds
        }
        delay(delay)
        value = elapsedString()
    }
}
