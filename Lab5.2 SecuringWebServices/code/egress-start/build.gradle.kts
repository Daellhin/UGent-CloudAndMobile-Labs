plugins {
    kotlin("jvm") version "2.1.20"
    id("io.ktor.plugin") version "3.1.1"
    kotlin("plugin.serialization") version "1.9.21"
    id("com.expediagroup.graphql") version "8.4.0"
    id("application")
}

group = "be.ugent.idlab.predict.ocmt.egress"
version = "1.0-SNAPSHOT"

application {
    mainClass = "be.ugent.idlab.predict.ocmt.egress.ApplicationKt"
}

ktor {
    fatJar {
        archiveFileName.set("egress.jar")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val ktor_version: String by project
    val kt_datetime_version: String by project
    val kt_serialization_version: String by project
    val influx_version: String by project
    val logback_version: String by project
    val graphql_kt_version= "8.4.0"
    val bcrypt_version: String by project
    val exposed_version: String by project
    val mariadb_version: String by project
    val ktor_openapi_tools = "5.0.1"

    // basic architecture dependencies
    implementation("com.influxdb:influxdb-client-kotlin:$influx_version")
    implementation("com.influxdb:flux-dsl:$influx_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kt_serialization_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kt_datetime_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    // basic egress dependencies
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-resources:$ktor_version")
    implementation("com.expediagroup:graphql-kotlin-ktor-server:$graphql_kt_version")
    implementation("com.expediagroup:graphql-kotlin-schema-generator:$graphql_kt_version")
    // forecasting dependencies
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    // egress session/auth dependencies
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    // egress user management dependencies
    implementation("at.favre.lib:bcrypt:$bcrypt_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.mariadb.jdbc:mariadb-java-client:$mariadb_version")
    // test-only dependencies
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    // openapi dependencies https://smiley4.github.io/ktor-openapi-tools
    implementation("io.github.smiley4:ktor-openapi:$ktor_openapi_tools")
    implementation("io.github.smiley4:ktor-swagger-ui:$ktor_openapi_tools")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

graphql {
    schema {
        packages = listOf("be.ugent.idlab.predict.ocmt.egress")
    }
}
