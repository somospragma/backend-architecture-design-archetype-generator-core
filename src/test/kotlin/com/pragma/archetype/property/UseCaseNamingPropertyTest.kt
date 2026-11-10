package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for Use Case Naming Convention (Property 11).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 11: Use Case Naming Convention
 *
 * For any valid use case name, the UseCase.java.ftl template output must:
 * - Have class named ${useCaseName}UseCase (not ${useCaseName}UseCaseImpl)
 * - Have @RequiredArgsConstructor
 * - Not contain @Service or @Component
 *
 * **Validates: Requirements 13.1, 13.2, 13.3, 13.4**
 */
class UseCaseNamingPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processUseCaseTemplate(useCaseName: String, packageName: String, paradigm: String): String {
        val templatePath = "frameworks/spring/$paradigm/usecase/UseCase.java.ftl"
        val template = freemarkerConfig.getTemplate(templatePath)
        val dataModel = mapOf(
            "useCaseName" to useCaseName,
            "packageName" to "$packageName.ports",
            "implPackage" to "$packageName.usecases"
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 11.1: Imperative UseCase class should be named useCaseName+UseCase" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processUseCaseTemplate(useCaseName, packageName, "imperative")
                output shouldContain "public class ${useCaseName}UseCase"
            }
        }

        "Property 11.2: Reactive UseCase class should be named useCaseName+UseCase" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processUseCaseTemplate(useCaseName, packageName, "reactive")
                output shouldContain "public class ${useCaseName}UseCase"
            }
        }

        "Property 11.3: Imperative UseCase should not use Impl suffix" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processUseCaseTemplate(useCaseName, packageName, "imperative")
                output shouldNotContain "${useCaseName}UseCaseImpl"
            }
        }

        "Property 11.4: Reactive UseCase should not use Impl suffix" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processUseCaseTemplate(useCaseName, packageName, "reactive")
                output shouldNotContain "${useCaseName}UseCaseImpl"
            }
        }

        "Property 11.5: Imperative UseCase should have @RequiredArgsConstructor" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processUseCaseTemplate(useCaseName, packageName, "imperative")
                output shouldContain "@RequiredArgsConstructor"
            }
        }

        "Property 11.6: Reactive UseCase should have @RequiredArgsConstructor" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processUseCaseTemplate(useCaseName, packageName, "reactive")
                output shouldContain "@RequiredArgsConstructor"
            }
        }

        "Property 11.7: Imperative UseCase should not contain @Service annotation" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processUseCaseTemplate(useCaseName, packageName, "imperative")
                output shouldNotContain "@Service"
            }
        }

        "Property 11.8: Imperative UseCase should not contain @Component annotation" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processUseCaseTemplate(useCaseName, packageName, "imperative")
                output shouldNotContain "@Component"
            }
        }

        "Property 11.9: Reactive UseCase should not contain @Service or @Component" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processUseCaseTemplate(useCaseName, packageName, "reactive")
                output shouldNotContain "@Service"
                output shouldNotContain "@Component"
            }
        }

        "Property 11.10: UseCase should implement I-prefixed interface" {
            checkAll(
                DomainModelGenerators.className(),
                DomainModelGenerators.packageName()
            ) { useCaseName, packageName ->
                val output = processUseCaseTemplate(useCaseName, packageName, "imperative")
                output shouldContain "implements I${useCaseName}UseCase"
            }
        }
    }
}
