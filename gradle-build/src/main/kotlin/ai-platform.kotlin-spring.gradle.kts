plugins {
    // 1. Apply our own common base
    id("ai-platform.kotlin-common")

    // 2. Apply Spring Framework specific plugins
    id("org.springframework.boot") // Version from libs.versions.toml
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.plugin.spring") // Open classes for Spring AOP
}

// 3. Optional: Spring specific dependencies that ALL Spring services need
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

// 4. Spring often requires this task adjustment
tasks.getByName<Jar>("jar") {
    enabled = false
}