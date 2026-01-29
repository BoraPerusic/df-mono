plugins {
    base
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

jib {
    container {
        mainClass = "com.example.auth.ApplicationKt"
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.4.0")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.4.0")
    implementation("org.jetbrains.exposed:exposed-core:1.0.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0")
    implementation("com.h2database:h2:2.4.240")
    implementation("io.ktor:ktor-server-csrf-jvm:3.4.0")
    implementation("io.ktor:ktor-server-auth-jvm:3.4.0")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:3.4.0")
    implementation("io.ktor:ktor-client-core-jvm:3.4.0")
    implementation("io.ktor:ktor-client-apache-jvm:3.4.0")
    implementation("io.ktor:ktor-server-cors-jvm:3.4.0")
    implementation("io.ktor:ktor-server-forwarded-header-jvm:3.4.0")
    implementation("io.ktor:ktor-server-openapi-jvm:3.4.0")
    implementation("io.ktor:ktor-server-routing-openapi-jvm:3.4.0")
    implementation("io.ktor:ktor-server-netty-jvm:3.4.0")
    implementation("ch.qos.logback:logback-classic:1.5.26")
    implementation("io.opentelemetry:opentelemetry-api:1.58.0")
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:1.58.0")
    implementation("io.opentelemetry.instrumentation:opentelemetry-ktor-3.0:2.18.1-alpha")
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.4.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.3.0")
}
