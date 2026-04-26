package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for Imperative Error Handling (Property 16).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 16: Imperative Error Handling with ProblemDetail
 *
 * For any valid configPackage, the GlobalExceptionHandler.java.ftl template output must:
 * - Have @RestControllerAdvice annotation
 * - Return ProblemDetail objects
 * - Handle MethodArgumentNotValidException, ConstraintViolationException, and generic Exception
 *
 * **Validates: Requirements 10.1, 10.2, 10.3, 10.4**
 */
class ImperativeErrorHandlingPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processGlobalExceptionHandlerTemplate(configPackage: String): String {
        val template = freemarkerConfig.getTemplate(
            "frameworks/spring/imperative/project/GlobalExceptionHandler.java.ftl"
        )
        val dataModel = mapOf(
            "configPackage" to configPackage
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 16.1: GlobalExceptionHandler should have @RestControllerAdvice" {
            checkAll(DomainModelGenerators.packageName()) { configPackage ->
                val output = processGlobalExceptionHandlerTemplate(configPackage)
                output shouldContain "@RestControllerAdvice"
            }
        }

        "Property 16.2: GlobalExceptionHandler should return ProblemDetail" {
            checkAll(DomainModelGenerators.packageName()) { configPackage ->
                val output = processGlobalExceptionHandlerTemplate(configPackage)
                output shouldContain "ProblemDetail"
            }
        }

        "Property 16.3: GlobalExceptionHandler should handle MethodArgumentNotValidException" {
            checkAll(DomainModelGenerators.packageName()) { configPackage ->
                val output = processGlobalExceptionHandlerTemplate(configPackage)
                output shouldContain "MethodArgumentNotValidException"
            }
        }

        "Property 16.4: GlobalExceptionHandler should handle ConstraintViolationException" {
            checkAll(DomainModelGenerators.packageName()) { configPackage ->
                val output = processGlobalExceptionHandlerTemplate(configPackage)
                output shouldContain "ConstraintViolationException"
            }
        }

        "Property 16.5: GlobalExceptionHandler should handle generic Exception" {
            checkAll(DomainModelGenerators.packageName()) { configPackage ->
                val output = processGlobalExceptionHandlerTemplate(configPackage)
                output shouldContain "@ExceptionHandler(Exception.class)"
            }
        }

        "Property 16.6: GlobalExceptionHandler should use the provided configPackage" {
            checkAll(DomainModelGenerators.packageName()) { configPackage ->
                val output = processGlobalExceptionHandlerTemplate(configPackage)
                output shouldContain "package $configPackage;"
            }
        }
    }
}
