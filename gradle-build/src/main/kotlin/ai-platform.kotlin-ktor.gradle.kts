plugins {
    // 1. Apply our own common base
    id("ai-platform.kotlin-common")

    // 2. Apply Ktor specific plugins
    id("io.ktor.plugin") // Version from libs.versions.toml
    id("org.jetbrains.kotlin.plugin.serialization") // Ktor usually needs this
    application
}

// 3. Ktor specific dependencies
dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    // Add Ktor Swagger support automatically
    implementation("io.ktor:ktor-server-swagger:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-openapi:${libs.versions.ktor.get()}")
}

// 4. Default application settings (can be overridden in specific services)
application {
    // This allows you to run `./gradlew run` comfortably
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

val generateOpenApi by tasks.registering {
    group = "documentation"
    description = "Exports Ktor OpenAPI specs"

    doLast {
        // Ktor usually requires manual definition or specific code-generation plugins.
        // If you are using the manual definition approach (resources/openapi/documentation.yaml),
        // this task acts as a validator or publisher.
        val openApiFile = layout.projectDirectory.file("src/main/resources/openapi/documentation.yaml")
        if (openApiFile.asFile.exists()) {
            println("OpenAPI spec found at: ${openApiFile.asFile.path}")
            // Logic to copy/publish this file to a documentation portal could go here
        } else {
            println("No OpenAPI spec found in standard location.")
        }
    }
}

