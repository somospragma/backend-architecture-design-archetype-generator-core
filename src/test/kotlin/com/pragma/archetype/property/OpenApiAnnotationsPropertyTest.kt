package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for OpenAPI Annotations in Imperative Controllers (Property 18).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 18: OpenAPI Annotations in Imperative Controllers
 *
 * For any valid controller name with imperative paradigm, the Controller.java.ftl template output must:
 * - Include @Tag annotation on the controller class
 * - Include @Operation annotations on each endpoint method
 * - Include @ApiResponse annotations documenting success and error responses
 *
 * **Validates: Requirements 25.1, 25.2, 25.3**
 */
class OpenApiAnnotationsPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processControllerTemplate(controllerName: String, packageName: String, basePackage: String): String {
        val template = freemarkerConfig.getTemplate(
            "frameworks/spring/imperative/adapters/entry-points/rest/Controller.java.ftl"
        )
        val dataModel = mapOf(
            "controllerName" to controllerName,
            "packageName" to packageName,
            "basePackage" to basePackage,
            "portsPackage" to "$basePackage.domain.ports",
            "useCaseName" to ""
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 18.1: Imperative controller should have @Tag annotation on the class" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName, basePackage ->
                val output = processControllerTemplate(controllerName, packageName, basePackage)
                output shouldContain "@Tag("
                output shouldContain "import io.swagger.v3.oas.annotations.tags.Tag;"
            }
        }

        "Property 18.2: Imperative controller should have @Operation annotations on endpoint methods" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName, basePackage ->
                val output = processControllerTemplate(controllerName, packageName, basePackage)
                output shouldContain "@Operation("
                output shouldContain "import io.swagger.v3.oas.annotations.Operation;"
            }
        }

        "Property 18.3: Imperative controller should have @ApiResponse annotations" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName, basePackage ->
                val output = processControllerTemplate(controllerName, packageName, basePackage)
                output shouldContain "@ApiResponse("
                output shouldContain "import io.swagger.v3.oas.annotations.responses.ApiResponse;"
            }
        }

        "Property 18.4: @Tag annotation should reference the controller name" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName, basePackage ->
                val output = processControllerTemplate(controllerName, packageName, basePackage)
                output shouldContain "@Tag(name = \"${controllerName}\""
            }
        }

        "Property 18.5: @ApiResponse should document success response codes" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName, basePackage ->
                val output = processControllerTemplate(controllerName, packageName, basePackage)
                output shouldContain "responseCode = \"20"
            }
        }
    }
}
