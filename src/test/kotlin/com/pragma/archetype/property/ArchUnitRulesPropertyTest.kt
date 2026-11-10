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
 * Generated projects get one ArchitectureTest per architecture plus rule sets shared by
 * every architecture (templates/common), by framework (frameworks/{framework}/tests) and by
 * paradigm (frameworks/{framework}/{paradigm}/tests). Together they must cover:
 * - Framework-free domain and use cases, and no Reactor in imperative business logic
 * - I prefix on port/gateway interfaces, *Gateway suffix on output interfaces
 * - *UseCase suffix on use case classes, role suffix on adapter classes
 * - The layer rules of each architecture
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

    enum class ArchType(val templateDir: String) {
        HEXAGONAL_SINGLE("hexagonal-single"),
        HEXAGONAL_MULTI("hexagonal-multi"),
        HEXAGONAL_MULTI_GRANULAR("hexagonal-multi-granular"),
        ONION_SINGLE("onion-single")
    }

    /** Architectures with input ports and use cases in domain.usecases. */
    enum class HexagonalArchType(val templateDir: String) {
        HEXAGONAL_SINGLE("hexagonal-single"),
        HEXAGONAL_MULTI("hexagonal-multi"),
        HEXAGONAL_MULTI_GRANULAR("hexagonal-multi-granular")
    }

    private fun render(templatePath: String, basePackage: String, reactive: Boolean = false): String {
        val writer = StringWriter()
        freemarkerConfig.getTemplate(templatePath)
            .process(mapOf("basePackage" to basePackage, "isReactive" to reactive), writer)
        return writer.toString()
    }

    private fun architectureTest(basePackage: String, templateDir: String) =
        render("architectures/$templateDir/tests/architecture/ArchitectureTest.java.ftl", basePackage)

    private fun commonTest(name: String, basePackage: String, reactive: Boolean = false) =
        render("common/tests/architecture/$name.java.ftl", basePackage, reactive)

    init {
        "Property 14.1: DomainPurityTest should keep the domain free of frameworks and infrastructure" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = commonTest("DomainPurityTest", basePackage)
                output shouldContain "domainIsFreeOfFrameworks"
                output shouldContain "domainDoesNotDependOnInfrastructure"
                output shouldContain "resideInAPackage(\"..domain..\")"
                output shouldContain "\"org.springframework..\""
            }
        }

        "Property 14.2: DomainPurityTest should keep the application layer free of frameworks" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = commonTest("DomainPurityTest", basePackage)
                output shouldContain "applicationLayerIsFreeOfFrameworks"
                output shouldContain "resideInAPackage(\"..application..\")"
            }
        }

        "Property 14.3: NamingConventionTest should include I prefix rule for ports and gateways" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = commonTest("NamingConventionTest", basePackage)
                output shouldContain "domainInterfacesShouldHaveIPrefix"
                output shouldContain "haveSimpleNameStartingWith(\"I\")"
                output shouldContain "\"..domain.ports..\", \"..domain.gateway..\""
            }
        }

        "Property 14.4: NamingConventionTest should include Gateway suffix rule for output interfaces" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = commonTest("NamingConventionTest", basePackage)
                output shouldContain "outputInterfacesShouldHaveGatewaySuffix"
                output shouldContain "haveSimpleNameEndingWith(\"Gateway\")"
            }
        }

        "Property 14.5: NamingConventionTest should include UseCase suffix rule for both use case packages" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = commonTest("NamingConventionTest", basePackage)
                output shouldContain "useCasesShouldEndWithUseCaseSuffix"
                output shouldContain "haveSimpleNameEndingWith(\"UseCase\")"
                output shouldContain "\"..domain.usecases..\", \"..application.usecase..\""
            }
        }

        "Property 14.6: NamingConventionTest should include Adapter suffix rule" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = commonTest("NamingConventionTest", basePackage)
                output shouldContain "adapterClassesShouldEndWithAdapterSuffix"
                output shouldContain "haveNameMatching(\".*(Adapter|"
            }
        }

        "Property 14.7: ArchitectureTest should NOT reference adapter.in or adapter.out patterns" {
            checkAll(DomainModelGenerators.packageName(), Arb.enum<ArchType>()) { basePackage, archType ->
                val output = architectureTest(basePackage, archType.templateDir)
                output shouldNotContain "adapter.in"
                output shouldNotContain "adapter.out"
            }
        }

        "Property 14.8: ArchitectureTest should use entrypoints/drivenadapters packages" {
            checkAll(DomainModelGenerators.packageName(), Arb.enum<ArchType>()) { basePackage, archType ->
                val output = architectureTest(basePackage, archType.templateDir)
                output shouldContain "..infrastructure.entrypoints.."
                output shouldContain "..infrastructure.drivenadapters.."
            }
        }

        "Property 14.9: ArchitectureTest package declaration should match basePackage" {
            checkAll(DomainModelGenerators.packageName(), Arb.enum<ArchType>()) { basePackage, archType ->
                architectureTest(basePackage, archType.templateDir) shouldContain "package $basePackage.architecture;"
            }
        }

        "Property 14.10: shared rule tests package declaration should match basePackage" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                for (name in listOf("DomainPurityTest", "AdapterRulesTest", "NamingConventionTest",
                        "ImmutabilityTest", "CodingRulesTest")) {
                    commonTest(name, basePackage) shouldContain "package $basePackage.architecture;"
                }
            }
        }

        "Property 14.11: Hexagonal ArchitectureTest should enforce ports and adapters layers" {
            checkAll(DomainModelGenerators.packageName(), Arb.enum<HexagonalArchType>()) { basePackage, archType ->
                val output = architectureTest(basePackage, archType.templateDir)
                output shouldContain "layeredArchitecture()"
                output shouldContain ".layer(\"Ports\").definedBy(\"..domain.ports..\")"
                output shouldContain ".layer(\"UseCases\").definedBy(\"..domain.usecases..\")"
                output shouldContain ".whereLayer(\"UseCases\").mayOnlyBeAccessedByLayers(\"Config\")"
                output shouldContain "useCasesImplementAnInputPort"
                output shouldNotContain "onionArchitecture()"
            }
        }

        "Property 14.12: Onion ArchitectureTest should enforce its rings" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val output = architectureTest(basePackage, ArchType.ONION_SINGLE.templateDir)
                output shouldContain "onionArchitecture()"
                output shouldContain ".domainModels(\"..domain.model..\")"
                output shouldContain ".domainServices(\"..domain.gateway..\")"
                output shouldContain ".applicationServices(\"..application..\")"
                output shouldNotContain "layeredArchitecture()"
            }
        }

        "Property 14.13: DomainPurityTest should forbid Reactor in business logic only for imperative projects" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                commonTest("DomainPurityTest", basePackage, reactive = false) shouldContain "\"reactor..\""
                commonTest("DomainPurityTest", basePackage, reactive = true) shouldNotContain "\"reactor..\""
            }
        }

        "Property 14.14: Spring rule sets should cover injection, stereotypes and each paradigm" {
            checkAll(DomainModelGenerators.packageName()) { basePackage ->
                val framework = render("frameworks/spring/tests/architecture/FrameworkRulesTest.java.ftl", basePackage)
                framework shouldContain "noFieldInjection"
                framework shouldContain "controllersLiveInEntryPoints"
                framework shouldContain "repositoriesLiveInDrivenAdapters"

                val reactive = render("frameworks/spring/reactive/tests/architecture/ParadigmRulesTest.java.ftl", basePackage)
                reactive shouldContain "noBlockingCallsOnPublishers"
                reactive shouldContain "noBlockingStacks"

                val imperative = render("frameworks/spring/imperative/tests/architecture/ParadigmRulesTest.java.ftl", basePackage)
                imperative shouldContain "noReactiveStack"
            }
        }
    }
}
