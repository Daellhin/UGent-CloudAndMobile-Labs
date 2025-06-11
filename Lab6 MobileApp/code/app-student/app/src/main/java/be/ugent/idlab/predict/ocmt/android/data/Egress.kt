package be.ugent.idlab.predict.ocmt.android.data

import io.ktor.http.encodeURLPath
import kotlinx.datetime.Instant

object Egress {

    // TODO
    private const val URL = "http://egress.daellhin.cloudandmobile.ilabt.imec.be"

    const val register = "$URL/register"

    const val login = "$URL/login"

    const val sources = "$URL/rest/sources"

    const val counts = "$URL/rest/counts"

    const val attendance = "$URL/rest/attendance"

    const val forecast = "$URL/rest/forecast"

    fun counts(start: Instant, end: Instant) =
        "$counts?start=${start.toEpochMilliseconds()}&end=${end.toEpochMilliseconds()}"

    fun counts(start: Instant, end: Instant, source: String) =
        "$counts?source=${source.encodeURLPath()}&start=${start.toEpochMilliseconds()}&end=${end.toEpochMilliseconds()}"

    fun attendance(start: Instant, end: Instant) =
        "$attendance?start=${start.toEpochMilliseconds()}&end=${end.toEpochMilliseconds()}"

    fun attendance(start: Instant, end: Instant, source: String) =
        "$attendance?source=${source.encodeURLPath()}&start=${start.toEpochMilliseconds()}&end=${end.toEpochMilliseconds()}"

    fun forecast(time: Instant, source: String) =
        "$forecast?source=${source.encodeURLPath()}&method=mean_forecast"

    fun forecast(time: Instant, method: String, source: String) =
        "$forecast?source=${source.encodeURLPath()}&time=${time.toEpochMilliseconds()}&method=${method.encodeURLPath()}"

}
