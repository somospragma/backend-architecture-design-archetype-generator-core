plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
    jacoco
}

group = "com.pragma"
version = "0.1.15-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Freemarker for template processing
    implementation("org.freemarker:freemarker:2.3.32")
    
    // YAML processing for configuration
    implementation("org.yaml:snakeyaml:2.2")
    
    // HTTP client for downloading templates
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

gradlePlugin {
    plugins {
        create("cleanArchGenerator") {
            id = "com.pragma.archetype-generator"
            implementationClass = "com.pragma.archetype.infrastructure.config.CleanArchPlugin"
            displayName = "Clean Architecture Generator"
            description = "Gradle plugin to generate clean architecture projects"
        }
    }
}

tasks.test {
    useJUnitPlatform()
    ignoreFailures = true
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

