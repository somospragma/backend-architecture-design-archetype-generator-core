package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for MapStruct Mapper Interfaces (Property 6).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 6: MapStruct Mapper Interfaces
 *
 * For any valid entity name, the adapter template output must:
 * - Reference a mapper named {entityName}EntityMapper
 * - Inject the mapper via constructor injection (@RequiredArgsConstructor)
 * - Use toDomain and toEntity mapping methods
 * - Not contain static mapping methods
 *
 * Since there is no standalone mapper template, we verify mapper usage patterns
 * in the PostgreSQL adapter template which imports and uses the mapper.
 *
 * **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**
 */
class MapStructMapperPropertyTest : PropertyTestBase() {

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
        "Property 6.1: Adapter should reference EntityMapper following naming convention" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "${entityName}EntityMapper"
            }
        }

        "Property 6.2: Adapter should import mapper from mapper sub-package" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain ".mapper.${entityName}EntityMapper;"
            }
        }

        "Property 6.3: Adapter should inject mapper via constructor injection (RequiredArgsConstructor)" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "@RequiredArgsConstructor"
                output shouldContain "${entityName}EntityMapper mapper;"
            }
        }

        "Property 6.4: Adapter should use toDomain mapping method" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                val hasToDomain = output.contains("mapper.toDomain(") || output.contains("mapper::toDomain")
                assert(hasToDomain) { "Expected mapper.toDomain() call or mapper::toDomain reference" }
            }
        }

        "Property 6.5: Adapter should use toEntity mapping method" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldContain "mapper.toEntity("
            }
        }

        "Property 6.6: Adapter should not contain static mapping methods" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { entityName, adapterName, packageName, basePackage ->
                val output = processPostgresqlAdapterTemplate(entityName, adapterName, packageName, basePackage)
                output shouldNotContain "static.*toDomain"
                output shouldNotContain "static.*toEntity"
            }
        }
    }
}
