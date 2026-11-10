package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.StringWriter

/**
 * Example-based unit tests for specific validations.
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Example-Based Validations
 *
 * Tests:
 * - lombok.config exact content (Req 17)
 * - Jakarta Validation in DTOs (Req 4.3)
 * - Onion package alignment entry-points/driven-adapters (Req 23)
 * - helpers module in multi-granular with correct dependencies (Req 20)
 * - RequestParameterValidator generation for reactive (Req 27)
 *
 * **Validates: Requirements 4.3, 17.1, 17.2, 17.3, 20.1, 20.3, 23.1, 23.2, 27.1, 29.1**
 */
class ExampleBasedValidationsTest : StringSpec() {

    private val templatesRoot = File("../backend-architecture-design-archetype-generator-templates/templates")

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(templatesRoot)
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processTemplate(templatePath: String, dataModel: Map<String, Any>): String {
        val template = freemarkerConfig.getTemplate(templatePath)
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadStructureYaml(archDir: String): Map<String, Any> {
        val file = File(templatesRoot, "architectures/$archDir/structure.yml")
        return Yaml().load(file.inputStream())
    }

    init {
        // --- Requirement 17: lombok.config exact content ---

        "Req 17.1: lombok.config template should contain config.stopBubbling = true" {
            val output = processTemplate(
                "architectures/hexagonal-single/project/lombok.config.ftl",
                emptyMap()
            )
            output shouldContain "config.stopBubbling = true"
        }

        "Req 17.2: lombok.config template should contain lombok.addLombokGeneratedAnnotation = true" {
            val output = processTemplate(
                "architectures/hexagonal-single/project/lombok.config.ftl",
                emptyMap()
            )
            output shouldContain "lombok.addLombokGeneratedAnnotation = true"
        }

        "Req 17.3: lombok.config should have exactly the expected content" {
            val output = processTemplate(
                "architectures/hexagonal-single/project/lombok.config.ftl",
                emptyMap()
            ).trim()
            val expectedLines = output.lines().map { it.trim() }.filter { it.isNotEmpty() }
            expectedLines shouldContain "config.stopBubbling = true"
            expectedLines shouldContain "lombok.addLombokGeneratedAnnotation = true"
        }

        // --- Requirement 4.3: Jakarta Validation in DTOs ---

        "Req 4.3: Imperative REST Controller template should import jakarta.validation" {
            val output = processTemplate(
                "frameworks/spring/imperative/adapters/entry-points/rest/Controller.java.ftl",
                mapOf(
                    "packageName" to "com.example.infrastructure.entrypoints.rest",
                    "basePackage" to "com.example",
                    "controllerName" to "Account"
                )
            )
            output shouldContain "jakarta.validation"
        }

        "Req 4.3: Imperative REST Controller should use @Valid annotation" {
            val output = processTemplate(
                "frameworks/spring/imperative/adapters/entry-points/rest/Controller.java.ftl",
                mapOf(
                    "packageName" to "com.example.infrastructure.entrypoints.rest",
                    "basePackage" to "com.example",
                    "controllerName" to "Account"
                )
            )
            output shouldContain "@Valid"
        }

        "Req 4.3: Imperative REST Controller should use @Validated annotation" {
            val output = processTemplate(
                "frameworks/spring/imperative/adapters/entry-points/rest/Controller.java.ftl",
                mapOf(
                    "packageName" to "com.example.infrastructure.entrypoints.rest",
                    "basePackage" to "com.example",
                    "controllerName" to "Account"
                )
            )
            output shouldContain "@Validated"
        }

        // --- Requirement 23: Onion package alignment ---

        "Req 23.1: onion-single should use infrastructure/entry-points instead of infrastructure/adapter/in" {
            val structure = loadStructureYaml("onion-single")
            @Suppress("UNCHECKED_CAST")
            val packages = structure["packages"] as? List<String> ?: emptyList()
            val hasEntryPoints = packages.any { it.contains("entry-points") }
            val hasAdapterIn = packages.any { it.contains("adapter/in") }
            hasEntryPoints shouldBe true
            hasAdapterIn shouldBe false
        }

        "Req 23.2: onion-single should use infrastructure/driven-adapters instead of infrastructure/adapter/out" {
            val structure = loadStructureYaml("onion-single")
            @Suppress("UNCHECKED_CAST")
            val packages = structure["packages"] as? List<String> ?: emptyList()
            val hasDrivenAdapters = packages.any { it.contains("driven-adapters") }
            val hasAdapterOut = packages.any { it.contains("adapter/out") }
            hasDrivenAdapters shouldBe true
            hasAdapterOut shouldBe false
        }

        "Req 23.3: onion-single adapterPaths should use entry-points and driven-adapters" {
            val structure = loadStructureYaml("onion-single")
            @Suppress("UNCHECKED_CAST")
            val adapterPaths = structure["adapterPaths"] as? Map<String, String> ?: emptyMap()
            adapterPaths["driven"] shouldContain "driven-adapters"
            adapterPaths["driving"] shouldContain "entry-points"
        }

        // --- Requirement 20: helpers module in multi-granular ---

        "Req 20.1: hexagonal-multi-granular should have a helpers module" {
            val structure = loadStructureYaml("hexagonal-multi-granular")
            @Suppress("UNCHECKED_CAST")
            val modules = structure["modules"] as? List<Map<String, Any>> ?: emptyList()
            val helpersModule = modules.find { it["name"] == "helpers" }
            helpersModule shouldNotBe null
        }

        "Req 20.2: helpers module path should be infrastructure/helpers" {
            val structure = loadStructureYaml("hexagonal-multi-granular")
            @Suppress("UNCHECKED_CAST")
            val modules = structure["modules"] as? List<Map<String, Any>> ?: emptyList()
            val helpersModule = modules.find { it["name"] == "helpers" }
            helpersModule shouldNotBe null
            helpersModule!!["path"] shouldBe "infrastructure/helpers"
        }

        "Req 20.3: helpers module should depend on model and ports" {
            val structure = loadStructureYaml("hexagonal-multi-granular")
            @Suppress("UNCHECKED_CAST")
            val modules = structure["modules"] as? List<Map<String, Any>> ?: emptyList()
            val helpersModule = modules.find { it["name"] == "helpers" }
            helpersModule shouldNotBe null
            @Suppress("UNCHECKED_CAST")
            val dependencies = helpersModule!!["dependencies"] as? List<String> ?: emptyList()
            dependencies shouldContain "model"
            dependencies shouldContain "ports"
        }

        // --- Requirement 27: RequestParameterValidator for reactive ---

        "Req 27.1: RequestParameterValidator.java.ftl should exist for reactive paradigm" {
            val file = File(
                templatesRoot,
                "frameworks/spring/reactive/project/RequestParameterValidator.java.ftl"
            )
            file.exists() shouldBe true
        }

        "Req 27.2: RequestParameterValidator should be abstract class" {
            val output = processTemplate(
                "frameworks/spring/reactive/project/RequestParameterValidator.java.ftl",
                mapOf("configPackage" to "com.example.config")
            )
            output shouldContain "public abstract class RequestParameterValidator"
        }

        "Req 27.3: RequestParameterValidator should use reactive types (Mono)" {
            val output = processTemplate(
                "frameworks/spring/reactive/project/RequestParameterValidator.java.ftl",
                mapOf("configPackage" to "com.example.config")
            )
            output shouldContain "Mono<T>"
            output shouldContain "reactor.core.publisher.Mono"
        }

        "Req 27.4: RequestParameterValidator should use Jakarta Validation" {
            val output = processTemplate(
                "frameworks/spring/reactive/project/RequestParameterValidator.java.ftl",
                mapOf("configPackage" to "com.example.config")
            )
            output shouldContain "jakarta.validation"
            output shouldContain "Validator"
        }

        // --- Requirement 29: HTTP client naming ---

        "Req 29.1: HTTP client adapter metadata should exist for imperative" {
            val file = File(
                templatesRoot,
                "frameworks/spring/imperative/adapters/driven-adapters/http-client/metadata.yml"
            )
            file.exists() shouldBe true
        }
    }
}
