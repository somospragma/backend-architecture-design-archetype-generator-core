package com.pragma.archetype.property

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * Integration tests for end-to-end flows.
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Integration: End-to-End Flows
 *
 * Tests:
 * - Adapter-architecture-paradigm matrix for all supported combinations (Req 28)
 * - Consistent adapter paths across all structure.yml files
 *
 * **Validates: Requirements 28.1, 28.2, 28.3, 28.4, 28.5, 31.3**
 */
class EndToEndFlowsTest : StringSpec() {

    private val templatesRoot = File("../backend-architecture-design-archetype-generator-templates/templates")

    data class ArchConfig(val name: String, val templateDir: String)

    private val architectures = listOf(
        ArchConfig("hexagonal-single", "hexagonal-single"),
        ArchConfig("hexagonal-multi", "hexagonal-multi"),
        ArchConfig("hexagonal-multi-granular", "hexagonal-multi-granular"),
        ArchConfig("onion-single", "onion-single")
    )

    private val paradigms = listOf("imperative", "reactive")

    /**
     * Driven adapter types available per paradigm.
     * Maps paradigm -> list of adapter directory names.
     */
    private val drivenAdaptersByParadigm = mapOf(
        "imperative" to listOf("mongodb", "postgresql", "redis", "http-client", "dynamodb", "sqs-producer"),
        "reactive" to listOf("mongodb", "postgresql", "redis", "http-client", "dynamodb", "sqs-producer")
    )

    /**
     * Entry-point adapter types available per paradigm.
     */
    private val entryPointsByParadigm = mapOf(
        "imperative" to listOf("rest", "graphql", "grpc", "sqs-consumer", "scheduled"),
        "reactive" to listOf("rest", "graphql", "grpc", "sqs-consumer", "scheduled")
    )

    @Suppress("UNCHECKED_CAST")
    private fun loadStructureYaml(archDir: String): Map<String, Any> {
        val file = File(templatesRoot, "architectures/$archDir/structure.yml")
        return Yaml().load(file.inputStream())
    }

    init {
        // --- Requirement 28.1, 28.2: Adapter template directories exist for all paradigms ---

        "Req 28.1: All driven adapter template directories should exist for imperative paradigm" {
            val adapters = drivenAdaptersByParadigm["imperative"]!!
            for (adapter in adapters) {
                val dir = File(
                    templatesRoot,
                    "frameworks/spring/imperative/adapters/driven-adapters/$adapter"
                )
                dir.exists() shouldBe true
            }
        }

        "Req 28.1: All driven adapter template directories should exist for reactive paradigm" {
            val adapters = drivenAdaptersByParadigm["reactive"]!!
            for (adapter in adapters) {
                val dir = File(
                    templatesRoot,
                    "frameworks/spring/reactive/adapters/driven-adapters/$adapter"
                )
                dir.exists() shouldBe true
            }
        }

        "Req 28.2: All entry-point adapter template directories should exist for imperative paradigm" {
            val adapters = entryPointsByParadigm["imperative"]!!
            for (adapter in adapters) {
                val dir = File(
                    templatesRoot,
                    "frameworks/spring/imperative/adapters/entry-points/$adapter"
                )
                dir.exists() shouldBe true
            }
        }

        "Req 28.2: All entry-point adapter template directories should exist for reactive paradigm" {
            val adapters = entryPointsByParadigm["reactive"]!!
            for (adapter in adapters) {
                val dir = File(
                    templatesRoot,
                    "frameworks/spring/reactive/adapters/entry-points/$adapter"
                )
                dir.exists() shouldBe true
            }
        }

        // --- Requirement 28.3, 28.4: Each adapter directory has metadata.yml ---

        "Req 28.3: All driven adapter directories should contain metadata.yml for imperative" {
            val adapters = drivenAdaptersByParadigm["imperative"]!!
            for (adapter in adapters) {
                val metadataFile = File(
                    templatesRoot,
                    "frameworks/spring/imperative/adapters/driven-adapters/$adapter/metadata.yml"
                )
                metadataFile.exists() shouldBe true
            }
        }

        "Req 28.3: All driven adapter directories should contain metadata.yml for reactive" {
            val adapters = drivenAdaptersByParadigm["reactive"]!!
            for (adapter in adapters) {
                val metadataFile = File(
                    templatesRoot,
                    "frameworks/spring/reactive/adapters/driven-adapters/$adapter/metadata.yml"
                )
                metadataFile.exists() shouldBe true
            }
        }

        "Req 28.4: All entry-point adapter directories should contain metadata.yml for imperative" {
            val adapters = entryPointsByParadigm["imperative"]!!
            for (adapter in adapters) {
                val metadataFile = File(
                    templatesRoot,
                    "frameworks/spring/imperative/adapters/entry-points/$adapter/metadata.yml"
                )
                metadataFile.exists() shouldBe true
            }
        }

        "Req 28.4: All entry-point adapter directories should contain metadata.yml for reactive" {
            val adapters = entryPointsByParadigm["reactive"]!!
            for (adapter in adapters) {
                val metadataFile = File(
                    templatesRoot,
                    "frameworks/spring/reactive/adapters/entry-points/$adapter/metadata.yml"
                )
                metadataFile.exists() shouldBe true
            }
        }

        // --- Requirement 28.5: All structure.yml files have consistent adapterPaths ---

        "Req 28.5: All structure.yml files should define adapterPaths with driven and driving keys" {
            for (arch in architectures) {
                val structure = loadStructureYaml(arch.templateDir)
                @Suppress("UNCHECKED_CAST")
                val adapterPaths = structure["adapterPaths"] as? Map<String, String>
                adapterPaths shouldNotBe null
                adapterPaths!!.containsKey("driven") shouldBe true
                adapterPaths.containsKey("driving") shouldBe true
            }
        }

        "Req 28.5: All adapterPaths driven should contain driven-adapters" {
            for (arch in architectures) {
                val structure = loadStructureYaml(arch.templateDir)
                @Suppress("UNCHECKED_CAST")
                val adapterPaths = structure["adapterPaths"] as Map<String, String>
                adapterPaths["driven"]!! shouldContain "driven-adapters"
            }
        }

        "Req 28.5: All adapterPaths driving should contain entry-points" {
            for (arch in architectures) {
                val structure = loadStructureYaml(arch.templateDir)
                @Suppress("UNCHECKED_CAST")
                val adapterPaths = structure["adapterPaths"] as Map<String, String>
                adapterPaths["driving"]!! shouldContain "entry-points"
            }
        }

        // --- Requirement 31.3: Each adapter has at least one .ftl template ---

        "Req 31.3: All driven adapter directories should contain at least one .ftl template for imperative" {
            val adapters = drivenAdaptersByParadigm["imperative"]!!
            for (adapter in adapters) {
                val dir = File(
                    templatesRoot,
                    "frameworks/spring/imperative/adapters/driven-adapters/$adapter"
                )
                val ftlFiles = dir.listFiles()?.filter { it.extension == "ftl" } ?: emptyList()
                (ftlFiles.isNotEmpty()) shouldBe true
            }
        }

        "Req 31.3: All driven adapter directories should contain at least one .ftl template for reactive" {
            val adapters = drivenAdaptersByParadigm["reactive"]!!
            for (adapter in adapters) {
                val dir = File(
                    templatesRoot,
                    "frameworks/spring/reactive/adapters/driven-adapters/$adapter"
                )
                val ftlFiles = dir.listFiles()?.filter { it.extension == "ftl" } ?: emptyList()
                (ftlFiles.isNotEmpty()) shouldBe true
            }
        }

        "Req 31.3: All entry-point adapter directories should contain at least one .ftl template for imperative" {
            val adapters = entryPointsByParadigm["imperative"]!!
            for (adapter in adapters) {
                val dir = File(
                    templatesRoot,
                    "frameworks/spring/imperative/adapters/entry-points/$adapter"
                )
                val ftlFiles = dir.listFiles()?.filter { it.extension == "ftl" } ?: emptyList()
                (ftlFiles.isNotEmpty()) shouldBe true
            }
        }

        "Req 31.3: All entry-point adapter directories should contain at least one .ftl template for reactive" {
            val adapters = entryPointsByParadigm["reactive"]!!
            for (adapter in adapters) {
                val dir = File(
                    templatesRoot,
                    "frameworks/spring/reactive/adapters/entry-points/$adapter"
                )
                val ftlFiles = dir.listFiles()?.filter { it.extension == "ftl" } ?: emptyList()
                (ftlFiles.isNotEmpty()) shouldBe true
            }
        }

        // --- Cross-architecture consistency ---

        "All structure.yml files that define entityModule should set it to domain" {
            for (arch in architectures) {
                val structure = loadStructureYaml(arch.templateDir)
                val entityModule = structure["entityModule"] as? String
                if (entityModule != null) {
                    // hexagonal-multi-granular uses "model" module for entities
                    val expected = if (arch.name == "hexagonal-multi-granular") "model" else "domain"
                    entityModule shouldBe expected
                }
            }
        }

        "All structure.yml files that define useCaseModule should point to the module holding use cases" {
            for (arch in architectures) {
                val structure = loadStructureYaml(arch.templateDir)
                val useCaseModule = structure["useCaseModule"] as? String
                if (useCaseModule != null) {
                    // granular has a "usecases" module; onion keeps use cases in the application ring
                    val expected = when (arch.name) {
                        "hexagonal-multi-granular" -> "usecases"
                        "onion-single" -> "application"
                        else -> "domain"
                    }
                    useCaseModule shouldBe expected
                }
            }
        }

        "All structure.yml files should have adapterPaths with {name} placeholder" {
            for (arch in architectures) {
                val structure = loadStructureYaml(arch.templateDir)
                @Suppress("UNCHECKED_CAST")
                val adapterPaths = structure["adapterPaths"] as Map<String, String>
                adapterPaths["driven"]!! shouldContain "{name}"
                adapterPaths["driving"]!! shouldContain "{name}"
            }
        }
    }
}
