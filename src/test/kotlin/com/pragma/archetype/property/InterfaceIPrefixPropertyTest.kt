package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for Interface I-Prefix on All Ports (Property 2).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 2: Interface I-Prefix on All Ports
 *
 * For any valid use case name, the InputPort.java.ftl template output must
 * produce an interface starting with the `I` prefix.
 *
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**
 */
class InterfaceIPrefixPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processInputPortTemplate(useCaseName: String, packageName: String, paradigm: String): String {
        val templatePath = "frameworks/spring/$paradigm/usecase/InputPort.java.ftl"
        val template = freemarkerConfig.getTemplate(templatePath)
        val dataModel = mapOf(
            "useCaseName" to useCaseName,
            "packageName" to packageName
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 2.1: Imperative InputPort interface name should start with I prefix" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processInputPortTemplate(useCaseName, packageName, "imperative")
                output shouldContain "public interface I${useCaseName}UseCase"
            }
        }

        "Property 2.2: Reactive InputPort interface name should start with I prefix" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processInputPortTemplate(useCaseName, packageName, "reactive")
                output shouldContain "public interface I${useCaseName}UseCase"
            }
        }

        "Property 2.3: Imperative InputPort should not have interface without I prefix" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processInputPortTemplate(useCaseName, packageName, "imperative")
                // Should not contain the old pattern without I prefix
                output shouldNotContain "public interface ${useCaseName}UseCase {"
            }
        }

        "Property 2.4: Reactive InputPort should not have interface without I prefix" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processInputPortTemplate(useCaseName, packageName, "reactive")
                output shouldNotContain "public interface ${useCaseName}UseCase {"
            }
        }

        "Property 2.5: InputPort interface name should include UseCase suffix after I prefix" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processInputPortTemplate(useCaseName, packageName, "imperative")
                output shouldContain "I${useCaseName}UseCase"
            }
        }

        "Property 2.6: InputPort package declaration should match provided package name" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processInputPortTemplate(useCaseName, packageName, "imperative")
                output shouldContain "package $packageName;"
            }
        }
    }
}
