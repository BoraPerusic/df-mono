plugins {
    `kotlin-dsl`
}

dependencies {
    // Allows us to use the Version Catalog in our precompiled script plugins
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}