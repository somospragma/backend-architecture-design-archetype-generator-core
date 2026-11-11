plugins {
    `java-gradle-plugin`
    `maven-publish`
    signing
    id("com.gradle.plugin-publish") version "2.2.1"
    id("org.sonarqube") version "7.5.0.8588"
    jacoco
    pmd
    kotlin("jvm") version "1.9.25"
}

group = "co.com.pragma"

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
    // Lombok for reducing boilerplate
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")
    
    testCompileOnly("org.projectlombok:lombok:1.18.48")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.48")
    
    // Freemarker for template processing
    implementation("org.freemarker:freemarker:2.3.35")
    
    // YAML processing for configuration
    implementation("org.yaml:snakeyaml:2.7")
    
    // HTTP client for downloading templates
    implementation("com.squareup.okhttp3:okhttp:4.12.1")
    
    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.4")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.20")
    implementation("ch.qos.logback:logback-classic:1.6.6")
    
    // Testing - JUnit
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.4")
    testImplementation("org.mockito:mockito-core:5.24.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.24.0")
    testImplementation("org.assertj:assertj-core:3.27.7")
    
    // Testing - MockWebServer for HTTP tests
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.1")
    
    // Testing - Kotest for property-based testing
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-property:5.9.1")
    testImplementation("io.kotest:kotest-framework-datatest:5.9.1")
}

// Generate plugin.properties with version information
val generatePluginProperties by tasks.registering {
    val propertiesFile = layout.buildDirectory.file("resources/main/plugin.properties").get().asFile
    outputs.file(propertiesFile)
    
    doLast {
        propertiesFile.parentFile.mkdirs()
        propertiesFile.writeText("version=${project.version}\n")
    }
}

tasks.named("processResources") {
    dependsOn(generatePluginProperties)
}

gradlePlugin {
    website.set("https://github.com/somospragma/backend-architecture-design-archetype-generator-core")
    vcsUrl.set("https://github.com/somospragma/backend-architecture-design-archetype-generator-core.git")
    
    plugins {
        create("cleanArchGenerator") {
            id = "co.com.pragma.archetype-generator"
            implementationClass = "com.pragma.archetype.infrastructure.config.CleanArchPlugin"
            displayName = "Clean Architecture Generator"
            description = "Gradle plugin to generate clean architecture projects with multiple frameworks and adapters"
            tags.set(listOf("clean-architecture", "hexagonal", "ddd", "code-generator", "spring-boot"))
        }
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/test/html"))
        csv.required.set(false)
    }
    
    // Exclude Lombok-generated code from coverage
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/infrastructure/config/**",
                    "**/*Plugin.class",
                    "**/*Task.class"
                )
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    classDirectories.setFrom(tasks.jacocoTestReport.get().classDirectories)
    violationRules {
        rule {
            limit {
                minimum = "0.60".toBigDecimal()
            }
        }
    }
}

pmd {
    toolVersion = "7.28.0"
    ruleSetFiles = files("config/pmd/dead-code.xml")
    ruleSets = listOf()
    isConsoleOutput = true
}

// Dead code only matters in production sources
tasks.named("pmdTest") { enabled = false }

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
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
        withType<MavenPublication> {
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
            name = "build"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    // Make signing optional - only required for Maven Central, not for Gradle Plugin Portal
    isRequired = false
    
    // Only sign if GPG credentials are available (required for Maven Central, not for Gradle Plugin Portal)
    val signingKey = project.findProperty("signingKey")?.toString() ?: System.getenv("ORG_GRADLE_PROJECT_signingKey")
    val signingPassword = project.findProperty("signingPassword")?.toString() ?: System.getenv("ORG_GRADLE_PROJECT_signingPassword")
    
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        val publications = publishing.publications.matching { 
            it.name == "pluginMaven" || it.name == "cleanArchGeneratorPluginMarkerMaven" 
        }
        sign(publications)
    }
}


// SonarCloud. The Gradle scanner ignores sonar-project.properties, so the whole
// configuration lives here. Organization and project key come from the CI
// secrets SONAR_ORGANIZATION and SONAR_PROJECT_KEY.
sonar {
    properties {
        System.getenv("SONAR_ORGANIZATION")?.takeIf { it.isNotBlank() }
            ?.let { property("sonar.organization", it) }
        System.getenv("SONAR_PROJECT_KEY")?.takeIf { it.isNotBlank() }
            ?.let { property("sonar.projectKey", it) }
        property("sonar.projectName", "Archetype Generator Core")
        property("sonar.projectVersion", version.toString())
        property("sonar.gradle.skipCompile", "true")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile.path
        )
        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/config/**",
                "**/infrastructure/config/**",
                "**/*Plugin.java",
                "**/*Task.java",
                "**/domain/model/**",
                "**/domain/port/**"
            ).joinToString(",")
        )
    }
}

// Task to create a bundle for Maven Central Portal manual upload
tasks.register<Zip>("createMavenCentralBundle") {
    dependsOn("publishToMavenLocal")
    
    archiveFileName.set("maven-central-bundle-${version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    
    from(layout.buildDirectory.dir("repo")) {
        include("**/*")
    }
    
    doLast {
        println("Maven Central bundle created at: ${archiveFile.get().asFile.absolutePath}")
        println("Upload this file manually to: https://central.sonatype.com/publishing/deployments")
    }
}
