package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for Gateway Suffix on Output Ports (Property 3).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 3: Gateway Suffix on Output Ports
 *
 * For any valid entity name and driven adapter type, the adapter template output must:
 * - Have the adapter implement I{entityName}Gateway
 * - Use Gateway suffix (not Repository) for the port interface
 *
 * Note: The PostgreSQL Adapter.java.ftl uses FreeMarker raw string escaping,
 * so the output contains literal placeholder patterns. The property tests verify
 * these structural patterns hold for any valid entity/adapter name combination.
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**
 */
class GatewaySuffixPropertyTest : PropertyTestBase() {

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
            "modelPackage" to "$basePackage.domain.model"
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 3.1: PostgreSQL adapter should implement I{entityName}Gateway interface" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "implements I${entityName}Gateway"
            }
        }

        "Property 3.2: PostgreSQL adapter should import I{entityName}Gateway from domain.ports" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "import ${basePackage}.domain.ports.I${entityName}Gateway;"
            }
        }

        "Property 3.3: PostgreSQL adapter should not reference Repository as port interface" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldNotContain "implements ${entityName}Repository"
                output shouldNotContain "implements I${entityName}Repository"
            }
        }

        "Property 3.4: PostgreSQL adapter should use Gateway suffix in interface name" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "I${entityName}Gateway"
            }
        }

        "Property 3.5: PostgreSQL adapter class name should end with Adapter" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "${adapterName}Adapter implements"
            }
        }
    }
}
