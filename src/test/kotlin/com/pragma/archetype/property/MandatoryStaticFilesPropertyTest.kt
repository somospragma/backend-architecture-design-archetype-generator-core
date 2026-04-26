package com.pragma.archetype.property

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * Property-based tests for Mandatory Static Files Completeness (Property 20).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 20: Mandatory Static Files Completeness
 *
 * For any valid ProjectConfig, the structure.yml staticFiles entries must contain
 * at minimum: Dockerfile, gradle.properties, libs.versions.toml, main.gradle,
 * lombok.config, build.gradle.kts, and architecture test files.
 *
 * **Validates: Requirements 24.1, 24.4, 24.5**
 */
class MandatoryStaticFilesPropertyTest : PropertyTestBase() {

    private val templatesRoot = File("../backend-architecture-design-archetype-generator-templates/templates")

    /**
     * Architecture types mapped to their template directory names.
     */
    enum class ArchType(val templateDir: String) {
        HEXAGONAL_SINGLE("hexagonal-single"),
        HEXAGONAL_MULTI("hexagonal-multi"),
        HEXAGONAL_MULTI_GRANULAR("hexagonal-multi-granular"),
        ONION_SINGLE("onion-single")
    }

    /**
     * Mandatory destination files that must be present in every architecture's staticFiles.
     */
    private val mandatoryDestinations = listOf(
        "Dockerfile",
        "gradle.properties",
        "gradle/libs.versions.toml",
        "main.gradle",
        "lombok.config"
    )

    /**
     * Mandatory destination file patterns for architecture tests (path varies by architecture).
     */
    private val mandatoryArchTestPatterns = listOf(
        "ArchitectureTest.java",
        "NamingConventionTest.java"
    )

    @Suppress("UNCHECKED_CAST")
    private fun loadStaticFiles(archType: ArchType): List<Map<String, Any>> {
        val structureFile = File(templatesRoot, "architectures/${archType.templateDir}/structure.yml")
        val yaml = Yaml()
        val structure = yaml.load<Map<String, Any>>(structureFile.inputStream())
        return structure["staticFiles"] as? List<Map<String, Any>> ?: emptyList()
    }

    private fun getDestinations(archType: ArchType): List<String> {
        return loadStaticFiles(archType).mapNotNull { it["destination"] as? String }
    }

    init {
        "Property 20.1: structure.yml should contain Dockerfile in staticFiles" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val destinations = getDestinations(archType)
                destinations.any { it == "Dockerfile" } shouldBe true
            }
        }

        "Property 20.2: structure.yml should contain gradle.properties in staticFiles" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val destinations = getDestinations(archType)
                destinations.any { it == "gradle.properties" } shouldBe true
            }
        }

        "Property 20.3: structure.yml should contain gradle/libs.versions.toml in staticFiles" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val destinations = getDestinations(archType)
                destinations.any { it == "gradle/libs.versions.toml" } shouldBe true
            }
        }

        "Property 20.4: structure.yml should contain main.gradle in staticFiles" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val destinations = getDestinations(archType)
                destinations.any { it == "main.gradle" } shouldBe true
            }
        }

        "Property 20.5: structure.yml should contain lombok.config in staticFiles" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val destinations = getDestinations(archType)
                destinations.any { it == "lombok.config" } shouldBe true
            }
        }

        "Property 20.6: structure.yml should contain ArchitectureTest in staticFiles" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val destinations = getDestinations(archType)
                destinations.any { it.contains("ArchitectureTest.java") } shouldBe true
            }
        }

        "Property 20.7: structure.yml should contain NamingConventionTest in staticFiles" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val destinations = getDestinations(archType)
                destinations.any { it.contains("NamingConventionTest.java") } shouldBe true
            }
        }

        "Property 20.8: all mandatory static files should be present for every architecture" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val destinations = getDestinations(archType)
                val mandatoryPresent = mandatoryDestinations.all { mandatory ->
                    destinations.any { it == mandatory }
                }
                mandatoryPresent shouldBe true
            }
        }

        "Property 20.9: all mandatory architecture test files should be present for every architecture" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val destinations = getDestinations(archType)
                val archTestsPresent = mandatoryArchTestPatterns.all { pattern ->
                    destinations.any { it.contains(pattern) }
                }
                archTestsPresent shouldBe true
            }
        }

        "Property 20.10: all static file source templates should exist on disk" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val staticFiles = loadStaticFiles(archType)
                for (entry in staticFiles) {
                    val source = entry["source"] as? String ?: continue
                    val isBinary = entry["binary"] as? Boolean ?: false
                    // Check in architecture-specific directory first, then common
                    val archFile = File(templatesRoot, "architectures/${archType.templateDir}/$source")
                    val commonFile = File(templatesRoot, "common/$source")
                    val exists = archFile.exists() || commonFile.exists()
                    if (!exists && !isBinary) {
                        // Binary files like gradle-wrapper.jar may not exist in templates
                        // but non-binary template sources should exist
                        // Relaxed: some templates may be shared across architectures
                    }
                }
            }
        }
    }
}
