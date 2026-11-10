package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for Reactive Error Handling (Property 17).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 17: Reactive Error Handling with AbstractErrorWebExceptionHandler
 *
 * For any valid configPackage, the GlobalErrorWebExceptionHandler.java.ftl template output must:
 * - Extend AbstractErrorWebExceptionHandler
 * - Override getRoutingFunction
 * - Not use @RestControllerAdvice
 *
 * **Validates: Requirements 11.1, 11.2, 11.3, 11.4**
 */
class ReactiveErrorHandlingPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processGlobalErrorWebExceptionHandlerTemplate(configPackage: String): String {
        val template = freemarkerConfig.getTemplate(
            "frameworks/spring/reactive/project/GlobalErrorWebExceptionHandler.java.ftl"
        )
        val dataModel = mapOf(
            "configPackage" to configPackage
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 17.1: GlobalErrorWebExceptionHandler should extend AbstractErrorWebExceptionHandler" {
            checkAll(DomainModelGenerators.packageName()) { configPackage ->
                val output = processGlobalErrorWebExceptionHandlerTemplate(configPackage)
                output shouldContain "extends AbstractErrorWebExceptionHandler"
            }
        }

        "Property 17.2: GlobalErrorWebExceptionHandler should override getRoutingFunction" {
            checkAll(DomainModelGenerators.packageName()) { configPackage ->
                val output = processGlobalErrorWebExceptionHandlerTemplate(configPackage)
                output shouldContain "getRoutingFunction"
            }
        }

        "Property 17.3: GlobalErrorWebExceptionHandler should not use @RestControllerAdvice" {
            checkAll(DomainModelGenerators.packageName()) { configPackage ->
                val output = processGlobalErrorWebExceptionHandlerTemplate(configPackage)
                // Check that @RestControllerAdvice is not used as an actual annotation (non-comment lines)
                val nonCommentLines = output.lines().filter { line ->
                    val trimmed = line.trim()
                    !trimmed.startsWith("*") && !trimmed.startsWith("//") && !trimmed.startsWith("/*")
                }
                val nonCommentContent = nonCommentLines.joinToString("\n")
                nonCommentContent.contains("@RestControllerAdvice") shouldBe false
            }
        }

        "Property 17.4: GlobalErrorWebExceptionHandler should use the provided configPackage" {
            checkAll(DomainModelGenerators.packageName()) { configPackage ->
                val output = processGlobalErrorWebExceptionHandlerTemplate(configPackage)
                output shouldContain "package $configPackage;"
            }
        }

        "Property 17.5: GlobalErrorWebExceptionHandler should use RouterFunction for error routing" {
            checkAll(DomainModelGenerators.packageName()) { configPackage ->
                val output = processGlobalErrorWebExceptionHandlerTemplate(configPackage)
                output shouldContain "RouterFunction<ServerResponse>"
            }
        }
    }
}
