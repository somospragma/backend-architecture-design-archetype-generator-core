plugins {
    `java-gradle-plugin`
    `maven-publish`
    signing
    id("com.gradle.plugin-publish") version "1.2.1"
    jacoco
    kotlin("jvm") version "1.9.21"
}

group = "com.pragma"
version = "1.0.0-PRERELEASE"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
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
    
    // Testing - JUnit
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    
    // Testing - MockWebServer for HTTP tests
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    
    // Testing - Kotest for property-based testing
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-property:5.8.0")
    testImplementation("io.kotest:kotest-framework-datatest:5.8.0")
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

// Maven Central Publishing Configuration
publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "com.pragma"
            artifactId = "archetype-generator"
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("Clean Architecture Generator")
                description.set("Gradle plugin to generate clean architecture projects with multiple frameworks and adapters")
                url.set("https://github.com/somospragma/backend-architecture-design-archetype-generator-core")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("somospragma")
                        name.set("Pragma S.A.")
                        email.set("info@pragma.com.co")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/somospragma/backend-architecture-design-archetype-generator-core.git")
                    developerConnection.set("scm:git:ssh://github.com/somospragma/backend-architecture-design-archetype-generator-core.git")
                    url.set("https://github.com/somospragma/backend-architecture-design-archetype-generator-core")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "OSSRH"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            
            credentials {
                username = project.findProperty("ossrhUsername")?.toString() ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword")?.toString() ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications["pluginMaven"])
}

