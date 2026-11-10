package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for UseCasesConfig regex scan (Property 7).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 7: UseCasesConfig with Regex Scan
 *
 * For any valid configPackage and useCasesPackage, the UseCasesConfig.java.ftl template output must:
 * - Have @Configuration annotation
 * - Have @ComponentScan with regex filter ^.+UseCase$
 * - Have an empty class body with no @Bean methods
 *
 * **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5**
 */
class UseCasesConfigPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    private fun processUseCasesConfigTemplate(configPackage: String, useCasesPackage: String): String {
        val template = freemarkerConfig.getTemplate(
            "architectures/hexagonal-single/project/UseCasesConfig.java.ftl"
        )
        val dataModel = mapOf(
            "configPackage" to configPackage,
            "useCasesPackage" to useCasesPackage
        )
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 7.1: UseCasesConfig should have @Configuration annotation" {
            checkAll(
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { configPackage, useCasesPackage ->
                val output = processUseCasesConfigTemplate(configPackage, useCasesPackage)
                output shouldContain "@Configuration"
            }
        }

        "Property 7.2: UseCasesConfig should have @ComponentScan with regex filter" {
            checkAll(
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { configPackage, useCasesPackage ->
                val output = processUseCasesConfigTemplate(configPackage, useCasesPackage)
                output shouldContain "@ComponentScan"
                output shouldContain "FilterType.REGEX"
                output shouldContain "^.+UseCase\$"
            }
        }

        "Property 7.3: UseCasesConfig should have empty class body with no @Bean methods" {
            checkAll(
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { configPackage, useCasesPackage ->
                val output = processUseCasesConfigTemplate(configPackage, useCasesPackage)
                output shouldNotContain "@Bean"
            }
        }

        "Property 7.4: UseCasesConfig class should be named UseCasesConfig" {
            checkAll(
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { configPackage, useCasesPackage ->
                val output = processUseCasesConfigTemplate(configPackage, useCasesPackage)
                output shouldContain "public class UseCasesConfig"
            }
        }

        "Property 7.5: UseCasesConfig should scan the provided useCasesPackage" {
            checkAll(
                DomainModelGenerators.packageName(),
                DomainModelGenerators.packageName()
            ) { configPackage, useCasesPackage ->
                val output = processUseCasesConfigTemplate(configPackage, useCasesPackage)
                output shouldContain "basePackages = \"$useCasesPackage\""
            }
        }
    }
}
