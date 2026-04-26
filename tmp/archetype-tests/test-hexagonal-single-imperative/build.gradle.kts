plugins {
    id("java")
    jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("co.com.pragma.archetype-generator") version "1.3.5-SNAPSHOT"
    alias(libs.plugins.pitest)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.owasp.dependencycheck)
}

group = "com.pragma.test"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

// Apply quality tools configuration
apply(from = "main.gradle")

dependencies {
    // Spring Boot Actuator
    implementation(libs.spring.boot.starter.actuator)

    // MapStruct
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // SpringDoc OpenAPI
    implementation(libs.springdoc.openapi.starter.webmvc)

    // Testing
    testImplementation(libs.spring.boot.starter.test)

    // ArchUnit for architecture testing
    testImplementation(libs.archunit.junit5)
}

tasks.test {
    useJUnitPlatform()
}
