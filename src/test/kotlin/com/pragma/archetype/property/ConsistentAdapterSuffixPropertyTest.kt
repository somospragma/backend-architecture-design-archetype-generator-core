package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for Consistent Adapter Suffix (Property 10).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 10: Consistent Adapter Suffix
 *
 * For any valid adapter name, the adapter template output must:
 * - Have class name ending with Adapter (never Repository) for driven adapters
 * - Use Controller suffix for imperative REST entry-points
 * - Use Handler suffix for reactive REST entry-points
 *
 * **Validates: Requirements 12.1, 12.2, 12.3, 12.4**
 */
class ConsistentAdapterSuffixPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processPostgresqlAdapterTemplate(entityName: String, adapterName: String, packageName: String, basePackage: String): String {
        val template = freemarkerConfig.getTemplate(
            "frameworks/spring/imperative/adapters/driven-adapters/postgresql/Adapter.java.ftl"
        )
        val dataModel = mapOf(
            "entityName" to entityName,
            "adapterName" to adapterName,
            "packageName" to packageName,
            "basePackage" to basePackage,
            "portsPackage" to "$basePackage.domain.ports",
            "modelPackage" to "$basePackage.domain.model",
            "entityPackage" to "$packageName.entity"
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    private fun processImperativeControllerTemplate(controllerName: String, packageName: String, basePackage: String): String {
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

    private fun processReactiveHandlerTemplate(controllerName: String, packageName: String): String {
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
        "Property 10.1: PostgreSQL driven adapter class name should end with Adapter" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "${adapterName}Adapter"
            }
        }

        "Property 10.2: PostgreSQL driven adapter should never use Repository suffix in class name" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                // The class declaration should use Adapter suffix, not Repository
                output shouldNotContain "public class ${adapterName}Repository"
                output shouldNotContain "public class ${entityName}Repository"
            }
        }

        "Property 10.3: Imperative REST entry-point should use Controller suffix" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName, basePackage ->
                val output = processImperativeControllerTemplate(controllerName, packageName, basePackage)
                output shouldContain "${controllerName}Controller"
            }
        }

        "Property 10.4: Reactive REST entry-point should use Handler suffix" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { controllerName, packageName ->
                val output = processReactiveHandlerTemplate(controllerName, packageName)
                output shouldContain "${controllerName}Handler"
            }
        }
    }
}
