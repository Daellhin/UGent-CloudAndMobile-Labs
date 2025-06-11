package be.ugent.idlab.predict.ocmt.android.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

fun Instant.localised() = toLocalDateTime(TimeZone.currentSystemDefault())

fun Instant.elapsedString() = if (Clock.System.now() > this) {
    "${(Clock.System.now() - this).toCompactString()} ago"
} else {
    "in ${(this - Clock.System.now()).toCompactString()}"
}

// compact, so only representing the largest non-null unit
fun Duration.toCompactString(): String {
    return when {
        inWholeDays != 0L -> "$inWholeDays day(s)"
        inWholeHours != 0L -> "$inWholeHours hour(s)"
        inWholeMinutes != 0L -> "$inWholeMinutes minute(s)"
        inWholeSeconds != 0L -> "$inWholeSeconds second(s)"
        else -> "< 1 second"
    }
}

fun Instant.toCompactString() =
    toLocalDateTime(TimeZone.currentSystemDefault()).toCompactString()

fun LocalDateTime.toCompactString() = "${time.toCompactString()} ${date.toCompactString()}"

fun LocalDate.toCompactString() = "$dayOfMonth/$monthNumber"

fun LocalTime.toCompactString() = "${hour.padded()}:${minute.padded()}:${second.padded()}"

fun Int.padded(): String = if (this < 10) "0$this" else "$this"
