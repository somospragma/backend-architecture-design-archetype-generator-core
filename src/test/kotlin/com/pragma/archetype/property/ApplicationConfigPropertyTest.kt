package com.pragma.archetype.property

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll
import java.io.File
import java.io.StringWriter

/**
 * Property-based tests for Application Configuration Completeness (Property 13).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 13: Application Configuration Completeness
 *
 * For any valid ProjectConfig, the generated application.yml must include:
 * - server.shutdown: graceful
 * - spring.lifecycle.timeout-per-shutdown-phase: 30s
 * - Actuator health probes enabled
 * - Secrets as env var placeholders
 * - Structured logging configuration with JSON format support
 *
 * **Validates: Requirements 21.1, 21.2, 21.3, 21.4, 26.1, 26.2, 26.3**
 */
class ApplicationConfigPropertyTest : PropertyTestBase() {

    private val freemarkerConfig: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setDirectoryForTemplateLoading(
            File("../backend-architecture-design-archetype-generator-templates/templates")
        )
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }

    /**
     * Template paths for application.yml across architectures and paradigms.
     */
    enum class AppYmlTemplate(val path: String, val label: String) {
        HEXAGONAL_SINGLE("architectures/hexagonal-single/project/application.yml.ftl", "hexagonal-single"),
        ONION_SINGLE("architectures/onion-single/project/application.yml.ftl", "onion-single"),
        IMPERATIVE("frameworks/spring/imperative/project/application.yml.ftl", "imperative"),
        REACTIVE("frameworks/spring/reactive/project/application.yml.ftl", "reactive")
    }

    private fun processApplicationYml(
        templateEnum: AppYmlTemplate,
        projectName: String,
        basePackage: String
    ): String {
        val template = freemarkerConfig.getTemplate(templateEnum.path)
        val dataModel = mutableMapOf<String, Any>(
            "projectName" to projectName,
            "basePackage" to basePackage
        )
        // Imperative and reactive templates also use projectNamePascalCase
        if (templateEnum == AppYmlTemplate.IMPERATIVE || templateEnum == AppYmlTemplate.REACTIVE) {
            dataModel["projectNamePascalCase"] = projectName.replaceFirstChar { it.uppercase() }
        }
        val writer = StringWriter()
        template.process(dataModel, writer)
        return writer.toString()
    }

    init {
        "Property 13.1: application.yml should contain server.shutdown: graceful" {
            checkAll(
                DomainModelGenerators.projectName(),
                DomainModelGenerators.packageName(),
                Arb.enum<AppYmlTemplate>()
            ) { projectName, basePackage, templateEnum ->
                val output = processApplicationYml(templateEnum, projectName, basePackage)
                output shouldContain "shutdown: graceful"
            }
        }

        "Property 13.2: application.yml should contain spring.lifecycle.timeout-per-shutdown-phase: 30s" {
            checkAll(
                DomainModelGenerators.projectName(),
                DomainModelGenerators.packageName(),
                Arb.enum<AppYmlTemplate>()
            ) { projectName, basePackage, templateEnum ->
                val output = processApplicationYml(templateEnum, projectName, basePackage)
                output shouldContain "timeout-per-shutdown-phase: 30s"
            }
        }

        "Property 13.3: application.yml should enable Actuator health probes" {
            checkAll(
                DomainModelGenerators.projectName(),
                DomainModelGenerators.packageName(),
                Arb.enum<AppYmlTemplate>()
            ) { projectName, basePackage, templateEnum ->
                val output = processApplicationYml(templateEnum, projectName, basePackage)
                output shouldContain "probes:"
                output shouldContain "enabled: true"
            }
        }

        "Property 13.4: application.yml should expose health, info, and prometheus endpoints" {
            checkAll(
                DomainModelGenerators.projectName(),
                DomainModelGenerators.packageName(),
                Arb.enum<AppYmlTemplate>()
            ) { projectName, basePackage, templateEnum ->
                val output = processApplicationYml(templateEnum, projectName, basePackage)
                output shouldContain "include: health,info,prometheus"
            }
        }

        "Property 13.5: application.yml should contain env var placeholders for secrets" {
            checkAll(
                DomainModelGenerators.projectName(),
                DomainModelGenerators.packageName(),
                Arb.enum<AppYmlTemplate>()
            ) { projectName, basePackage, templateEnum ->
                val output = processApplicationYml(templateEnum, projectName, basePackage)
                output shouldContain "\${DB_PASSWORD}"
            }
        }

        "Property 13.6: application.yml should contain structured logging configuration" {
            checkAll(
                DomainModelGenerators.projectName(),
                DomainModelGenerators.packageName(),
                Arb.enum<AppYmlTemplate>()
            ) { projectName, basePackage, templateEnum ->
                val output = processApplicationYml(templateEnum, projectName, basePackage)
                output shouldContain "structured:"
                output shouldContain "format:"
            }
        }

        "Property 13.7: application.yml should contain correlation ID support" {
            checkAll(
                DomainModelGenerators.projectName(),
                DomainModelGenerators.packageName(),
                Arb.enum<AppYmlTemplate>()
            ) { projectName, basePackage, templateEnum ->
                val output = processApplicationYml(templateEnum, projectName, basePackage)
                output shouldContain "correlation:"
                output shouldContain "traceId"
                output shouldContain "spanId"
            }
        }
    }
}
