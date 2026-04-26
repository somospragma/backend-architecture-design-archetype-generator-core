package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for DTOs as Java Records (Property 4).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 4: DTOs as Java Records
 *
 * For any valid controller name, the Controller.java.ftl template output must:
 * - Produce request/response DTOs as Java Records (using `record` keyword)
 * - Not use Lombok @Data/@Getter/@Setter annotations on DTOs
 * - Not use the suffix DTO
 *
 * Note: The Controller.java.ftl uses FreeMarker raw string escaping,
 * so the output contains literal placeholder patterns.
 *
 * **Validates: Requirements 4.1, 4.2, 4.4, 4.5**
 */
class DtosAsRecordsPropertyTest : PropertyTestBase() {

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
            "basePackage" to basePackage
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 4.1: Controller should define request DTO as Java Record with Request suffix" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName, basePackage ->
                val output = processControllerTemplate(controllerName, packageName, basePackage)
                output shouldContain "record Create\${controllerName}Request("
            }
        }

        "Property 4.2: Controller should define response DTO as Java Record with Response suffix" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName, basePackage ->
                val output = processControllerTemplate(controllerName, packageName, basePackage)
                output shouldContain "record \${controllerName}Response("
            }
        }

        "Property 4.3: Controller DTOs should not use Lombok @Data annotation" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName, basePackage ->
                val output = processControllerTemplate(controllerName, packageName, basePackage)
                // Extract the DTO section (after the record definitions)
                val recordSection = output.substringAfter("record ", "")
                if (recordSection.isNotEmpty()) {
                    recordSection shouldNotContain "@Data"
                    recordSection shouldNotContain "@Getter"
                    recordSection shouldNotContain "@Setter"
                }
            }
        }

        "Property 4.4: Controller should not use DTO suffix in any class name" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName, basePackage ->
                val output = processControllerTemplate(controllerName, packageName, basePackage)
                output shouldNotContain "RequestDTO"
                output shouldNotContain "ResponseDTO"
                output shouldNotContain "DTO {"
                output shouldNotContain "DTO("
            }
        }

        "Property 4.5: Request and Response types should use record keyword not class" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName, basePackage ->
                val output = processControllerTemplate(controllerName, packageName, basePackage)
                // Should not define DTOs as regular classes
                output shouldNotContain "class Create\${controllerName}Request"
                output shouldNotContain "class \${controllerName}Response"
            }
        }
    }
}
