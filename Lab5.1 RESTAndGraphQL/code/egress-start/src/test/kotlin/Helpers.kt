import be.ugent.idlab.predict.ocmt.egress.services.Forecasting
import be.ugent.idlab.predict.ocmt.egress.services.Influx
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*
import kotlin.random.Random
import kotlin.test.assertEquals

var start: Long = -1L // set during test setup creation
    private set

fun RESTTest(
    block: suspend ApplicationTestBuilder.(client: HttpClient) -> HttpResponse
) = testApplication {
    createTestSetup()
    val client = createClient {
        install(ContentNegotiation) { json() }
    }
    val response = block(client)
    val content = response.bodyAsText()

    println(response)
    if (content.isNotEmpty()) {
        println("Received the following data from a REST-test:\n${content}")
    } else {
        println("Caught the following exception when executing the REST-test:")
    }
    assertEquals(HttpStatusCode.OK, response.status, content)
    stopTestSetup()
}

fun GoodGraphQLTest(
    block: suspend ApplicationTestBuilder.(client: HttpClient) -> HttpResponse
) = testApplication {
    createTestSetup()
    val client = createClient {}
    val response = block(client)
    val content = response.bodyAsText()
    println(response)
    assertEquals(HttpStatusCode.OK, response.status, content)
    assert("errors" !in content) { content }
    assert("Validation error" !in content) { content }
    println("Received the following data from a GraphQL-test:")
    println(content)
    stopTestSetup()
}

fun BadGraphQLTest(
    block: suspend ApplicationTestBuilder.(client: HttpClient) -> HttpResponse
) = testApplication {
    createTestSetup()
    val client = createClient {}
    val response = block(client)
    val content = response.bodyAsText()
    println(response)
    assertEquals(HttpStatusCode.OK, response.status, content)
    assert("errors" in content) { content }
    println("Received the following data from a GraphQL-test:")
    println(content)
    stopTestSetup()
}

private fun createTestSetup() {
    // starting the services
    Forecasting.start()
    // adding mock data to influx for testing purposes
    runBlocking {
        val people = Point("people")
            .addTag("source", "vip area")
        val raw = Point("raw_ids")
            .addTag("source", "vip area")
        val rand = Random(0)
        val ids = (0..20).map { UUID.randomUUID().toString() }.toSet()
        val present = mutableSetOf<String>()
        start = Clock.System.now().toEpochMilliseconds() - 10_000L
        println(
            "Adding mocking data to influx between ${Instant.fromEpochMilliseconds(start)} and ${
                Instant.fromEpochMilliseconds(
                    start + 10_000
                )
            }"
        )
        repeat(10) { index ->
            val id = ids.random(rand)
            if (id in present) {
                present.remove(id)
            } else {
                present.add(id)
            }
            Influx.write(
                people
                    .addField("value", present.size)
                    .time(start + index * 1000L, WritePrecision.MS)
            )
            Influx.write(
                raw
                    .addField("value", id)
                    .time(start + index * 1000L, WritePrecision.MS)
            )
        }
    }
}

private fun stopTestSetup() {
    Forecasting.stop()
}
