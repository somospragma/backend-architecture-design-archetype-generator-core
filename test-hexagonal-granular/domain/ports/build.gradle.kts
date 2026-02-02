plugins {
    id("java-library")
}

description = "Domain ports - Interfaces for adapters"

dependencies {
    // Depends only on models
    api(project(":domain:model"))
    
    // Reactive support
    implementation("io.projectreactor:reactor-core:3.6.1")
}
