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
 * Property-based tests for Reactive REST RouterFunction pattern (Property 8).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 8: Reactive REST Uses RouterFunction Pattern
 *
 * For any valid controller name with paradigm reactive, the Router + Handler templates must:
 * - Router has RouterFunction<ServerResponse>
 * - Handler has ServerRequest and Mono<ServerResponse>
 * - No @RestController, @GetMapping, @PostMapping, @PutMapping, @DeleteMapping, @PatchMapping
 *
 * **Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5**
 */
class ReactiveRestRouterPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processRouterTemplate(controllerName: String, packageName: String): String {
        val template = freemarkerConfig.getTemplate(
            "frameworks/spring/reactive/adapters/entry-points/rest/Router.java.ftl"
        )
        val dataModel = mapOf(
            "controllerName" to controllerName,
            "packageName" to packageName
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    private fun processHandlerTemplate(controllerName: String, packageName: String): String {
        val template = freemarkerConfig.getTemplate(
            "frameworks/spring/reactive/adapters/entry-points/rest/Handler.java.ftl"
        )
        val dataModel = mapOf(
            "controllerName" to controllerName,
            "packageName" to packageName
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 8.1: Router should define RouterFunction<ServerResponse>" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName ->
                val output = processRouterTemplate(controllerName, packageName)
                output shouldContain "RouterFunction<ServerResponse>"
            }
        }

        "Property 8.2: Handler should accept ServerRequest and return Mono<ServerResponse>" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName ->
                val output = processHandlerTemplate(controllerName, packageName)
                output shouldContain "ServerRequest"
                output shouldContain "Mono<ServerResponse>"
            }
        }

        "Property 8.3: Router should not use @RestController annotation" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName ->
                val output = processRouterTemplate(controllerName, packageName)
                // Check that @RestController is not used as an actual annotation (non-comment lines)
                val nonCommentLines = output.lines().filter { line ->
                    val trimmed = line.trim()
                    !trimmed.startsWith("*") && !trimmed.startsWith("//") && !trimmed.startsWith("/*")
                }
                val nonCommentContent = nonCommentLines.joinToString("\n")
                nonCommentContent.contains("@RestController") shouldBe false
            }
        }

        "Property 8.4: Handler should not use @RestController annotation" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName ->
                val output = processHandlerTemplate(controllerName, packageName)
                // Check that @RestController is not used as an actual annotation (non-comment lines)
                val nonCommentLines = output.lines().filter { line ->
                    val trimmed = line.trim()
                    !trimmed.startsWith("*") && !trimmed.startsWith("//") && !trimmed.startsWith("/*")
                }
                val nonCommentContent = nonCommentLines.joinToString("\n")
                nonCommentContent.contains("@RestController") shouldBe false
            }
        }

        "Property 8.5: Router should not use @GetMapping or @PostMapping annotations" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName ->
                val output = processRouterTemplate(controllerName, packageName)
                val nonCommentLines = output.lines().filter { line ->
                    val trimmed = line.trim()
                    !trimmed.startsWith("*") && !trimmed.startsWith("//") && !trimmed.startsWith("/*")
                }
                val nonCommentContent = nonCommentLines.joinToString("\n")
                nonCommentContent.contains("@GetMapping") shouldBe false
                nonCommentContent.contains("@PostMapping") shouldBe false
                nonCommentContent.contains("@PutMapping") shouldBe false
                nonCommentContent.contains("@DeleteMapping") shouldBe false
                nonCommentContent.contains("@PatchMapping") shouldBe false
            }
        }

        "Property 8.6: Handler should not use @GetMapping or @PostMapping annotations" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName ->
                val output = processHandlerTemplate(controllerName, packageName)
                val nonCommentLines = output.lines().filter { line ->
                    val trimmed = line.trim()
                    !trimmed.startsWith("*") && !trimmed.startsWith("//") && !trimmed.startsWith("/*")
                }
                val nonCommentContent = nonCommentLines.joinToString("\n")
                nonCommentContent.contains("@GetMapping") shouldBe false
                nonCommentContent.contains("@PostMapping") shouldBe false
                nonCommentContent.contains("@PutMapping") shouldBe false
                nonCommentContent.contains("@DeleteMapping") shouldBe false
                nonCommentContent.contains("@PatchMapping") shouldBe false
            }
        }

        "Property 8.7: Router should use RouterFunctions.route() for defining routes" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName ->
                val output = processRouterTemplate(controllerName, packageName)
                output shouldContain "RouterFunctions.route()"
            }
        }

        "Property 8.8: Router should be a @Configuration class" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName ->
                val output = processRouterTemplate(controllerName, packageName)
                output shouldContain "@Configuration"
            }
        }
    }
}
