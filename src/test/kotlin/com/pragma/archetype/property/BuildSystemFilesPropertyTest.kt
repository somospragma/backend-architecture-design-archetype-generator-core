package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for Build System Files Generation (Property 12).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 12: Build System Files Generation
 *
 * For any valid ProjectConfig, the generated build system files must include:
 * - libs.versions.toml with [versions], [libraries], [plugins] sections
 * - gradle.properties with performance settings and credential placeholders
 * - main.gradle with JaCoCo (85%), PIT (20%), SonarQube, OWASP, MapStruct
 * - build.gradle.kts with version catalog aliases (libs.mapstruct, etc.)
 *
 * **Validates: Requirements 14.1, 14.2, 14.3, 15.1, 15.2, 15.3, 15.4, 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 18.1, 18.2**
 */
class BuildSystemFilesPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processTemplate(templatePath: String, dataModel: Map<String, Any>): String {
        val template = freemarkerConfig.getTemplate(templatePath)
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    private fun processLibsVersionsToml(): String {
        return processTemplate(
            "architectures/hexagonal-single/project/libs.versions.toml.ftl",
            emptyMap()
        )
    }

    private fun processGradleProperties(): String {
        return processTemplate(
            "architectures/hexagonal-single/project/gradle.properties.ftl",
            emptyMap()
        )
    }

    private fun processMainGradle(basePackage: String): String {
        return processTemplate(
            "architectures/hexagonal-single/project/main.gradle.ftl",
            mapOf("basePackage" to basePackage)
        )
    }

    private fun processBuildGradleKts(
        basePackage: String,
        pluginVersion: String,
        framework: String,
        paradigm: String
    ): String {
        return processTemplate(
            "architectures/hexagonal-single/project/build.gradle.kts.ftl",
            mapOf(
                "basePackage" to basePackage,
                "pluginVersion" to pluginVersion,
                "framework" to framework,
                "paradigm" to paradigm
            )
        )
    }

    init {
        // --- libs.versions.toml ---

        "Property 12.1: libs.versions.toml should contain [versions] section" {
            val output = processLibsVersionsToml()
            output shouldContain "[versions]"
        }

        "Property 12.2: libs.versions.toml should contain [libraries] section" {
            val output = processLibsVersionsToml()
            output shouldContain "[libraries]"
        }

        "Property 12.3: libs.versions.toml should contain [plugins] section" {
            val output = processLibsVersionsToml()
            output shouldContain "[plugins]"
        }

        "Property 12.4: libs.versions.toml should define Spring Boot version" {
            val output = processLibsVersionsToml()
            output shouldContain "spring-boot"
        }

        "Property 12.5: libs.versions.toml should define MapStruct version" {
            val output = processLibsVersionsToml()
            output shouldContain "mapstruct"
        }

        "Property 12.6: libs.versions.toml should define springdoc-openapi version" {
            val output = processLibsVersionsToml()
            output shouldContain "springdoc-openapi"
        }

        // --- gradle.properties ---

        "Property 12.7: gradle.properties should contain org.gradle.parallel=true" {
            val output = processGradleProperties()
            output shouldContain "org.gradle.parallel=true"
        }

        "Property 12.8: gradle.properties should contain org.gradle.caching=true" {
            val output = processGradleProperties()
            output shouldContain "org.gradle.caching=true"
        }

        "Property 12.9: gradle.properties should contain org.gradle.daemon=true" {
            val output = processGradleProperties()
            output shouldContain "org.gradle.daemon=true"
        }

        "Property 12.10: gradle.properties should contain credential placeholders" {
            val output = processGradleProperties()
            output shouldContain "sonarToken"
            output shouldContain "nexusUser"
        }

        "Property 12.11: gradle.properties should contain project version" {
            val output = processGradleProperties()
            output shouldContain "version="
        }

        // --- main.gradle ---

        "Property 12.12: main.gradle should configure JaCoCo with 85% minimum line coverage" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = processMainGradle(basePackage)
                output shouldContain "jacoco"
                output shouldContain "0.85"
            }
        }

        "Property 12.13: main.gradle should configure PIT with 20% minimum mutation kill" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = processMainGradle(basePackage)
                output shouldContain "pitest"
                output shouldContain "mutationThreshold = 20"
            }
        }

        "Property 12.14: main.gradle should configure SonarQube with env-var properties" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = processMainGradle(basePackage)
                output shouldContain "sonar"
                output shouldContain "System.getenv"
                output shouldContain "SONAR_TOKEN"
            }
        }

        "Property 12.15: main.gradle should configure OWASP Dependency Check" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = processMainGradle(basePackage)
                output shouldContain "dependencyCheck"
                output shouldContain "failBuildOnCVSS"
            }
        }

        "Property 12.16: main.gradle should configure MapStruct annotation processor" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = processMainGradle(basePackage)
                output shouldContain "mapstruct.defaultComponentModel=spring"
            }
        }

        // --- build.gradle.kts ---

        "Property 12.17: build.gradle.kts should use version catalog aliases for dependencies" {
            checkAll(
                DomainModelGenerators.packageName()
            ) { basePackage ->
                val output = processBuildGradleKts(basePackage, "1.0.0", "spring", "imperative")
                output shouldContain "libs.mapstruct"
                output shouldContain "libs.lombok"
                output shouldContain "libs.archunit.junit5"
            }
        }

        "Property 12.18: build.gradle.kts should target Java 21" {
            checkAll(
                DomainModelGenerators.packageName()
            ) { basePackage ->
                val output = processBuildGradleKts(basePackage, "1.0.0", "spring", "imperative")
                output shouldContain "JavaLanguageVersion.of(21)"
            }
        }

        "Property 12.19: build.gradle.kts should apply main.gradle" {
            checkAll(
                DomainModelGenerators.packageName()
            ) { basePackage ->
                val output = processBuildGradleKts(basePackage, "1.0.0", "spring", "imperative")
                output shouldContain "apply(from = \"main.gradle\")"
            }
        }

        "Property 12.20: build.gradle.kts should use version catalog plugin aliases for Spring Boot" {
            checkAll(
                DomainModelGenerators.packageName()
            ) { basePackage ->
                val output = processBuildGradleKts(basePackage, "1.0.0", "spring", "imperative")
                output shouldContain "alias(libs.plugins.spring.boot)"
            }
        }

        "Property 12.21: build.gradle.kts should include springdoc-openapi for imperative" {
            checkAll(
                DomainModelGenerators.packageName()
            ) { basePackage ->
                val output = processBuildGradleKts(basePackage, "1.0.0", "spring", "imperative")
                output shouldContain "libs.springdoc.openapi.starter.webmvc"
            }
        }

        "Property 12.22: build.gradle.kts should include springdoc-openapi for reactive" {
            checkAll(
                DomainModelGenerators.packageName()
            ) { basePackage ->
                val output = processBuildGradleKts(basePackage, "1.0.0", "spring", "reactive")
                output shouldContain "libs.springdoc.openapi.starter.webflux"
            }
        }
    }
}
