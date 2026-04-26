package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for Imperative HTTP Client Uses RestClient (Property 9).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 9: Imperative HTTP Client Uses RestClient
 *
 * For any valid adapter name with imperative paradigm, the HTTP client adapter template output must:
 * - Use RestClient from Spring 6.1+
 * - Not reference RestTemplate in any import or field
 * - Provide methods for GET, POST, PUT, PATCH, and DELETE operations
 *
 * Note: The HTTP client Adapter.java.ftl uses FreeMarker raw string escaping,
 * so the output contains literal placeholder patterns.
 *
 * **Validates: Requirements 9.1, 9.2, 9.3, 9.4**
 */
class HttpClientRestClientPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processHttpClientAdapterTemplate(entityName: String, adapterName: String, packageName: String, basePackage: String): String {
        val template = freemarkerConfig.getTemplate(
            "frameworks/spring/imperative/adapters/driven-adapters/http-client/Adapter.java.ftl"
        )
        val dataModel = mapOf(
            "entityName" to entityName,
            "adapterName" to adapterName,
            "packageName" to packageName,
            "basePackage" to basePackage
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 9.1: HTTP client adapter should use RestClient" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processHttpClientAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "RestClient"
            }
        }

        "Property 9.2: HTTP client adapter should not reference RestTemplate" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processHttpClientAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldNotContain "RestTemplate"
            }
        }

        "Property 9.3: HTTP client adapter should use RestClient.Builder for construction" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processHttpClientAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "RestClient.Builder"
            }
        }

        "Property 9.4: HTTP client adapter should provide GET, POST, PUT, PATCH, DELETE methods" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processHttpClientAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "restClient.get()"
                output shouldContain "restClient.post()"
                output shouldContain "restClient.put()"
                output shouldContain "restClient.patch()"
                output shouldContain "restClient.delete()"
            }
        }

        "Property 9.5: HTTP client adapter should import RestClient" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processHttpClientAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "import org.springframework.web.client.RestClient;"
            }
        }
    }
}
