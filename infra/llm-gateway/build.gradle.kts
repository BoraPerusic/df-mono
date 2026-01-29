import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.node)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.spring.dep)
    alias(libs.plugins.spring.boot)
}

group = "org.tatrman.llmgateway"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation(libs.boot.starter.web)
    implementation(libs.boot.starter.actuator)
    implementation(libs.boot.starter.data.jdbc)
    implementation(libs.boot.starter.oauth2.resource.server)
    implementation(libs.boot.starter.security)
    implementation(libs.boot.starter.ai)

    // AI
    implementation(libs.ai.starter.model.azure.openai)
    
    // Kotlin
    implementation(libs.jackson.module.kotlin)
//    implementation(libs.kotlin.reflect)
//    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.ser.json)

    // DB
    implementation(libs.flyway.core)
    runtimeOnly(libs.pgsql)
    runtimeOnly(libs.mssql)
    
    // Messaging
    implementation(libs.nats) // Check version
    
    // gRPC
    implementation(libs.boot.starter.grpc)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.kotlin)
    implementation(libs.protobuf.java)
    implementation(libs.grpc.netty.shaded)
    implementation(libs.spring.grpc.server.web.spring.boot.starter)

    // Config
    implementation(libs.hocon.config)
    
    // Dev calls
    developmentOnly(libs.boot.docker.compose)

    // Testing
    testImplementation(libs.boot.starter.test)
    testImplementation(libs.boot.testcontainers)
    testImplementation(libs.testcontainers.jupiter)
    testImplementation(libs.testcontainers.pgsql)
    testImplementation(libs.wiremock)
    testImplementation(libs.boot.starter.security.test)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.extensions.spring)
}