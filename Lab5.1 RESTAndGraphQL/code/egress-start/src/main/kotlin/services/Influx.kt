package be.ugent.idlab.predict.ocmt.egress.services

import com.influxdb.LogLevel
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.client.write.Point
import com.influxdb.query.FluxRecord
import com.influxdb.query.dsl.Flux
import io.ktor.util.logging.*
import kotlinx.coroutines.channels.toList
import java.util.*

object Influx {

    private val LOGGER = KtorSimpleLogger("services.Influx")

    private val properties = this::class.java
        .classLoader
        .getResourceAsStream("influx2.properties")
        .use { Properties().apply { load(it) } }

    private val client = InfluxDBClientKotlinFactory
        .create(
            url = properties.resolve("influx2.url"),
            org = properties.resolve("influx2.org"),
            token = properties.resolve("influx2.token").toCharArray(),
            bucket = properties.resolve("influx2.bucket"),
        )
        .apply {
            setLogLevel(LogLevel.valueOf(properties.resolve("influx2.logLevel")))
            enableGzip()
        }

    val Bucket = properties["influx2.bucket"] as String
    val Query = Flux.from(Bucket)

    suspend fun write(point: Point) {
        client.getWriteKotlinApi().writePoint(point)
    }

    fun close() = client.close()

    suspend fun query(flux: Flux) = query(flux.toString())

    suspend fun query(flux: String): List<FluxRecord> {
        LOGGER.info("Executing query `${flux.replace(Regex("\\s+"), " ")}`")
        return client.getQueryKotlinApi().query(flux).toList()
    }

}
