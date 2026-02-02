plugins {
    id("java-library")
}

description = "Domain use cases - Business logic implementation"

dependencies {
    // Depends on models and ports
    api(project(":domain:model"))
    api(project(":domain:ports"))
    
    // Reactive support
    implementation("io.projectreactor:reactor-core:3.6.1")
    
    // Testing
    testImplementation("io.projectreactor:reactor-test:3.6.1")
}
