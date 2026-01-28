plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.jib)    // Jib for containerization
    alias(libs.plugins.ktlint) // Ktlint for style
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

dependencies {
    implementation(libs.logback.classic)
    testImplementation(libs.testcontainers.wiremock)
    testImplementation(libs.bundles.kotest)
}

tasks.named("test") {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// 1. Linting Configuration
ktlint {
    version.set("1.0.1")
    verbose.set(true)
    outputToConsole.set(true)
}

// 2. Jib Configuration (Dockerless Builds)
jib {
    from {
        image = "gcr.io/distroless/java21-debian12"
    }
    to {
        image = "mycompany.azurecr.io/${project.name}"
    }
    container {
        creationTime = "USE_CURRENT_TIMESTAMP"
        jvmFlags = listOf("-XX:+UseContainerSupport", "-Xms512m", "-Xmx512m")
    }
}

// 3. Local Deployment Helper
// Since Jib is a Gradle plugin, we keep this wrapper to load the image into K3s
tasks.register<Exec>("deployLocal") {
    group = "deployment"
    description = "Builds Jib image to tar and loads into local K3s"

    dependsOn("jibBuildTar")
    val imageTar = layout.buildDirectory.file("jib-image.tar")

    // Uses nerdctl (Rancher) to load the tarball
    commandLine("nerdctl", "-n", "k8s.io", "load", "-i", imageTar.get().asFile.absolutePath)

    doFirst {
        println("📦 Packaging ${project.name} via Jib...")
    }
    doLast {
        println("🚀 Loaded ${project.name} into Rancher K3s!")
    }
}