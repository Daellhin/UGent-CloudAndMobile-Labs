package be.ugent.idlab.predict.ocmt.egress.server.modules

import be.ugent.idlab.predict.ocmt.egress.services.Influx
import com.influxdb.query.dsl.functions.restriction.Restrictions
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant

fun getSourcesFluxQuery() = """
    from(bucket: "${Influx.Bucket}")
      |> range(start: 0, stop: 0ms)
      |> filter(fn: (r) => r["_measurement"] == "raw_ids")
      |> distinct(column: "source")
""".trimIndent()

fun getCountFluxQuery(
    start: Instant,
    stop: Instant,
    source: String?
) = Influx.Query
    .range(start.toJavaInstant(), stop.toJavaInstant())
    .filter(Restrictions.measurement().equal("people"))
    .filter(Restrictions.tag("source").equal(source))

fun getIDsFluxQuery(
    start: Instant,
    stop: Instant,
    source: String?
) = Influx.Query
    .range(start.toJavaInstant(), stop.toJavaInstant())
    .filter(Restrictions.measurement().equal("raw_ids"))
    .filter(Restrictions.tag("source").equal(source))

fun getAttendanceFluxQuery(
    start: Instant,
    stop: Instant,
    source: String?
): String {
    val _start = start.toJavaInstant()
    val _stop = stop.toJavaInstant()
    return """
        import "join"
        diff = from(bucket:"${Influx.Bucket}")
            |> range(start: $_start, stop: $_stop)
            |> filter(fn: (r) => r["_measurement"] == "people")
            ${if (source != null) "|> filter(fn: (r) => r[\"source\"] == \"$source\")" else ""}
            |> difference(initialZero: true)
            |> group(columns: ["_time"])
        ids = from(bucket:"default")
            |> range(start: $_start, stop: $_stop)
            |> filter(fn: (r) => r["_measurement"] == "raw_ids")
            ${if (source != null) "|> filter(fn: (r) => r[\"source\"] == \"$source\")" else ""}
            |> group(columns: ["_time"])
        join.inner(
            left: diff,
            right: ids,
            on: (l, r) => l._time == r._time,
            as: (l, r) => {
                return { r with arrival: l._value == 1 }
            }
        )
        """.trimIndent()
}
