
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class ApplicationTest {

    @Test
    fun RESTSourcesTest() = RESTTest { client ->
        client.get("rest/sources")
    }

    @Test
    fun RESTCountTest() = RESTTest { client ->
        client.get("rest/counts?start=${start}")
    }

    @Test
    fun RESTBadCountTest() = RESTTest { client ->
        client.get("rest/counts?source=invalid")
    }

    @Test
    fun RESTIDsTest() = RESTTest { client ->
        client.get("rest/ids?start=${start}")
    }

    @Test
    fun RESTBadIDsTest() = RESTTest { client ->
        client.get("rest/ids?start=${start}&source=invalid")
    }

    @Test
    fun RESTAttendanceTest() = RESTTest { client ->
        client.get("rest/attendance?start=${start}")
    }

    @Test
    fun RESTBadAttendanceTest() = RESTTest { client ->
        client.get("rest/attendance?start=${start}&source=invalid")
    }

    @Test
    fun RESTForecastTest() = RESTTest { client ->
        println("Waiting 2 minutes to make the call, ensuring that the forecasting jobs have executed at least once")
        //delay(2.minutes)
        client.get("rest/forecast?source=vip area")
    }

    @Test
    fun GraphQLSourceTest() = GoodGraphQLTest { client ->
        client.get("graphql?query={sources}")
    }

    @Test
    fun GraphQLCountTest() = GoodGraphQLTest { client ->
        client.get("graphql?query={counts(start:\"${start}\"\nsource:\"vip area\") {timestamp\nvalue\nsource}}")
    }

    @Test
    fun GraphQLBadCountTest() = BadGraphQLTest { client ->
        client.get("graphql?query={counts(duration:\"${start}\") {timestamp\nvalue}}")
    }

    @Test
    fun GraphQLIDsTest() = GoodGraphQLTest { client ->
        client.get("graphql?query={ids(start:\"${start}\"\nsource:\"vip area\") {timestamp\nid\nsource}}")
    }

    @Test
    fun GraphQLBadIDsTest() = BadGraphQLTest { client ->
        client.get("graphql?query={ids(start:\"yesterday\") {timestamp\nid}}")
    }

    @Test
    fun GraphQLAttendanceTest() = GoodGraphQLTest { client ->
        client.get("graphql?query={attendance(start:\"${start}\"\nsource:\"vip area\") {timestamp\nid\narrival\nsource}}")
    }

}
