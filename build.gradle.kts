plugins {
    base // Standard lifecycle tasks (clean, build, assemble)
}

tasks.register("cleanAll") {
    group = "build"
    description = "Cleans the root and all subprojects"
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
    dependsOn(subprojects.map { it.tasks.named("clean") })
}