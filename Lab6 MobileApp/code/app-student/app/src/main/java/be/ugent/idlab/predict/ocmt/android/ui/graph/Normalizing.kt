package be.ugent.idlab.predict.ocmt.android.ui.graph

import kotlinx.datetime.Instant

data class NormalizedResult<X: Comparable<X>, Y: Comparable<Y>>(
    val lines: List<NormalizedLine>,
    val bottomLeft: Pair<X, Y>,
    val topRight: Pair<X, Y>
)

data class ScaledPoint(
    val x: Float,
    val y: Float
)

typealias NormalizedLine = List<ScaledPoint>

/**
 * Normalizes the data, first sorting them by time and returning both fields as `Float`s mapped
 *  in 0..1, with 0 being equal to the earliest `Instant`/lowest value from the original data, and
 *  1 being the latest/highest value.
 */
fun <Y> List<List<Pair<Instant, Y>>>.normalized(): NormalizedResult<Instant, Y>? where Y: Comparable<Y>, Y: Number {
    val filtered = filter { it.isNotEmpty() }
    if (filtered.isEmpty()) {
        return null
    }
    val minTime = filtered.minOf { it.minOf { it.first } }
    val maxTime = filtered.maxOf { it.maxOf { it.first } }
    val minValue = filtered.minOf { it.minOf { it.second } }
    val maxValue = filtered.maxOf { it.maxOf { it.second } }
    val min = minTime to minValue
    val max = maxTime to maxValue
    val lines = filtered.map { it.normalized(min = min, max = max) }
    return NormalizedResult(
        lines = lines,
        bottomLeft = min,
        topRight = max
    )
}

fun <Y> List<Pair<Instant, Y>>.normalized(
    min: Pair<Instant, Y>,
    max: Pair<Instant, Y>
): NormalizedLine where Y: Comparable<Y>, Y: Number {
    val timeDiff = (max.first - min.first).inWholeMicroseconds
    val valueDiff = max.second - min.second
    return when {
        timeDiff == 0L && valueDiff == 0f -> {
            listOf(ScaledPoint(.5f, .5f))
        }
        valueDiff == 0f -> sortedBy { it.first }.map {
            ScaledPoint(
                x = (it.first - min.first).inWholeMicroseconds.toFloat() / timeDiff,
                y = .5f
            )
        }
        timeDiff == 0L -> sortedBy { it.first }.map {
            ScaledPoint(
                x = .5f,
                y = (it.second - min.second) / valueDiff
            )
        }
        else -> sortedBy { it.first }.map {
            ScaledPoint(
                x = (it.first - min.first).inWholeMicroseconds.toFloat() / timeDiff,
                y = (it.second - min.second) / valueDiff
            )
        }
    }
}

private operator fun Number.minus(other: Number) = toFloat() - other.toFloat()
