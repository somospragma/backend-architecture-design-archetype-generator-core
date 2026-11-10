package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for Entity Lombok annotations (Property 5).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 5: Domain Entities with Lombok
 *
 * For any valid entity name, the Entity.java.ftl template output must:
 * - Have @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
 * - Not contain manual getters/setters
 * - Not contain framework annotations (@Entity, @Table, @Document)
 *
 * **Validates: Requirements 5.1, 5.2, 5.3, 5.4**
 */
class EntityLombokPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processEntityTemplate(entityName: String, packageName: String): String {
        val template = freemarkerConfig.getTemplate(
            "frameworks/spring/imperative/domain/Entity.java.ftl"
        )
        val dataModel = mapOf(
            "entityName" to entityName,
            "packageName" to packageName,
            "hasId" to true,
            "idType" to "Long",
            "needsUUID" to false,
            "needsLocalDateTime" to false,
            "needsLocalDate" to false,
            "needsBigDecimal" to false,
            "fields" to listOf(
                mapOf("type" to "String", "name" to "name"),
                mapOf("type" to "String", "name" to "description")
            )
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 5.1: Entity template output should contain @Data annotation" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { entityName, packageName ->
                val output = processEntityTemplate(entityName, packageName)
                output shouldContain "@Data"
            }
        }

        "Property 5.2: Entity template output should contain @Builder annotation" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { entityName, packageName ->
                val output = processEntityTemplate(entityName, packageName)
                output shouldContain "@Builder"
            }
        }

        "Property 5.3: Entity template output should contain @NoArgsConstructor annotation" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { entityName, packageName ->
                val output = processEntityTemplate(entityName, packageName)
                output shouldContain "@NoArgsConstructor"
            }
        }

        "Property 5.4: Entity template output should contain @AllArgsConstructor annotation" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { entityName, packageName ->
                val output = processEntityTemplate(entityName, packageName)
                output shouldContain "@AllArgsConstructor"
            }
        }

        "Property 5.5: Entity template output should not contain manual getter methods" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { entityName, packageName ->
                val output = processEntityTemplate(entityName, packageName)
                output shouldNotContain "public String getName()"
                output shouldNotContain "public Long getId()"
                output shouldNotContain Regex("public\\s+\\w+\\s+get[A-Z]\\w*\\(\\)")
            }
        }

        "Property 5.6: Entity template output should not contain manual setter methods" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { entityName, packageName ->
                val output = processEntityTemplate(entityName, packageName)
                output shouldNotContain Regex("public\\s+void\\s+set[A-Z]\\w*\\(")
            }
        }

        "Property 5.7: Entity template output should not contain framework annotations" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { entityName, packageName ->
                val output = processEntityTemplate(entityName, packageName)
                output shouldNotContain "@Entity"
                output shouldNotContain "@Table"
                output shouldNotContain "@Document"
            }
        }

        "Property 5.8: Entity class name should match the provided entity name" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { entityName, packageName ->
                val output = processEntityTemplate(entityName, packageName)
                output shouldContain "public class $entityName"
            }
        }
    }
}
