package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for ArchUnit Rules Completeness (Property 14).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 14: ArchUnit Rules Completeness
 *
 * For any valid basePackage and architecture type, the ArchitectureTest.java.ftl and
 * NamingConventionTest.java.ftl templates must include rules for:
 * - Domain layer isolation
 * - Application layer isolation
 * - I prefix on port interfaces
 * - *Gateway suffix on output ports
 * - *UseCase suffix on use case classes
 * - *Adapter suffix on adapter classes
 * - No adapter.in or adapter.out package patterns
 *
 * **Validates: Requirements 22.1, 22.2, 22.3, 22.4, 22.5, 22.6, 22.7**
 */
class ArchUnitRulesPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    /**
     * Architecture types mapped to their template directory names.
     */
    enum class ArchType(val templateDir: String) {
        HEXAGONAL_SINGLE("hexagonal-single"),
        HEXAGONAL_MULTI("hexagonal-multi"),
        HEXAGONAL_MULTI_GRANULAR("hexagonal-multi-granular"),
        ONION_SINGLE("onion-single")
    }

    private fun processArchitectureTestTemplate(basePackage: String, archType: ArchType): String {
        val template = freemarkerConfig.getTemplate(
            "architectures/${archType.templateDir}/tests/architecture/ArchitectureTest.java.ftl"
        )
        val dataModel = mapOf("basePackage" to basePackage)
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    private fun processNamingConventionTestTemplate(basePackage: String, archType: ArchType): String {
        val template = freemarkerConfig.getTemplate(
            "architectures/${archType.templateDir}/tests/architecture/NamingConventionTest.java.ftl"
        )
        val dataModel = mapOf("basePackage" to basePackage)
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 14.1: ArchitectureTest should include domain layer isolation rule" {
            checkAll(
                DomainModelGenerators.packageName(),
                Arb.enum<ArchType>()
            ) { basePackage, archType ->
                val output = processArchitectureTestTemplate(basePackage, archType)
                output shouldContain "domainLayerShouldBeIsolated"
                output shouldContain "noClasses()"
                output shouldContain "resideInAPackage(\"..domain..\")"
            }
        }

        "Property 14.2: ArchitectureTest should include application layer isolation rule" {
            checkAll(
                DomainModelGenerators.packageName(),
                Arb.enum<ArchType>()
            ) { basePackage, archType ->
                val output = processArchitectureTestTemplate(basePackage, archType)
                output shouldContain "applicationLayerShouldOnlyDependOnDomain"
                output shouldContain "resideInAPackage(\"..application..\")"
            }
        }

        "Property 14.3: NamingConventionTest should include I prefix rule for port interfaces" {
            checkAll(
                DomainModelGenerators.packageName(),
                Arb.enum<ArchType>()
            ) { basePackage, archType ->
                val output = processNamingConventionTestTemplate(basePackage, archType)
                output shouldContain "portInterfacesShouldHaveIPrefix"
                output shouldContain "haveSimpleNameStartingWith(\"I\")"
                output shouldContain "resideInAPackage(\"..ports..\")"
            }
        }

        "Property 14.4: NamingConventionTest should include Gateway suffix rule for output ports" {
            checkAll(
                DomainModelGenerators.packageName(),
                Arb.enum<ArchType>()
            ) { basePackage, archType ->
                val output = processNamingConventionTestTemplate(basePackage, archType)
                output shouldContain "outputPortsShouldHaveGatewaySuffix"
                output shouldContain "haveSimpleNameEndingWith(\"Gateway\")"
            }
        }

        "Property 14.5: NamingConventionTest should include UseCase suffix rule" {
            checkAll(
                DomainModelGenerators.packageName(),
                Arb.enum<ArchType>()
            ) { basePackage, archType ->
                val output = processNamingConventionTestTemplate(basePackage, archType)
                output shouldContain "useCasesShouldEndWithUseCaseSuffix"
                output shouldContain "haveSimpleNameEndingWith(\"UseCase\")"
                output shouldContain "resideInAPackage(\"..domain.usecases..\")"
            }
        }

        "Property 14.6: NamingConventionTest should include Adapter suffix rule" {
            checkAll(
                DomainModelGenerators.packageName(),
                Arb.enum<ArchType>()
            ) { basePackage, archType ->
                val output = processNamingConventionTestTemplate(basePackage, archType)
                output shouldContain "adapterClassesShouldEndWithAdapterSuffix"
                output shouldContain "haveSimpleNameEndingWith(\"Adapter\")"
            }
        }

        "Property 14.7: ArchitectureTest should NOT reference adapter.in or adapter.out patterns" {
            checkAll(
                DomainModelGenerators.packageName(),
                Arb.enum<ArchType>()
            ) { basePackage, archType ->
                val output = processArchitectureTestTemplate(basePackage, archType)
                output shouldNotContain "adapter.in"
                output shouldNotContain "adapter.out"
                output shouldNotContain "\"..infrastructure.adapter.in..\""
                output shouldNotContain "\"..infrastructure.adapter.out..\""
            }
        }

        "Property 14.8: ArchitectureTest should use entrypoints/drivenadapters in onion architecture rule" {
            checkAll(
                DomainModelGenerators.packageName(),
                Arb.enum<ArchType>()
            ) { basePackage, archType ->
                val output = processArchitectureTestTemplate(basePackage, archType)
                output shouldContain "entrypoints"
                output shouldContain "drivenadapters"
            }
        }

        "Property 14.9: ArchitectureTest package declaration should match basePackage" {
            checkAll(
                DomainModelGenerators.packageName(),
                Arb.enum<ArchType>()
            ) { basePackage, archType ->
                val output = processArchitectureTestTemplate(basePackage, archType)
                output shouldContain "package $basePackage.architecture;"
            }
        }

        "Property 14.10: NamingConventionTest package declaration should match basePackage" {
            checkAll(
                DomainModelGenerators.packageName(),
                Arb.enum<ArchType>()
            ) { basePackage, archType ->
                val output = processNamingConventionTestTemplate(basePackage, archType)
                output shouldContain "package $basePackage.architecture;"
            }
        }
    }
}
