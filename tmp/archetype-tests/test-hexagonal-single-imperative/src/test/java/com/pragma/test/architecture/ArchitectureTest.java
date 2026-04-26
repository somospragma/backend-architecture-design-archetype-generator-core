package com.pragma.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

/**
 * Architecture tests to validate clean architecture principles.
 * These tests ensure that the project follows the hexagonal/onion architecture pattern
 * with entry-points and driven-adapters (KB-aligned package naming).
 * 
 * DO NOT MODIFY OR DELETE THIS FILE.
 * This file is automatically synchronized during build to ensure architecture rules are enforced.
 */
class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.pragma.test");
    }

    @Test
    void domainLayerShouldNotDependOnApplicationLayer() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..application..")
            .allowEmptyShould(true)
            .because("Domain layer should not depend on Application layer");

        rule.check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnInfrastructureLayer() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .allowEmptyShould(true)
            .because("Domain layer should not depend on Infrastructure layer");

        rule.check(classes);
    }

    @Test
    void domainLayerShouldBeIsolated() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..infrastructure..",
                "..application..",
                "..config.."
            )
            .allowEmptyShould(true)
            .because("Domain layer must be completely isolated - it should not depend on any external layer");

        rule.check(classes);
    }

    @Test
    void applicationLayerShouldNotDependOnInfrastructureLayer() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .allowEmptyShould(true)
            .because("Application layer should not depend on Infrastructure layer");

        rule.check(classes);
    }

    @Test
    void applicationLayerShouldOnlyDependOnDomain() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..infrastructure..",
                "..entrypoints..",
                "..drivenadapters.."
            )
            .allowEmptyShould(true)
            .because("Application layer should only depend on domain layer");

        rule.check(classes);
    }

    @Test
    void onionArchitectureShouldBeRespected() {
        onionArchitecture()
            .domainModels("..domain.model..")
            .domainServices("..domain.service..", "..domain.usecases..", "..domain.ports..")
            .applicationServices("..application..")
            .adapter("entrypoints", "..infrastructure.entrypoints..")
            .adapter("drivenadapters", "..infrastructure.drivenadapters..")
            .withOptionalLayers(true)
            .check(classes);
    }

    @Test
    void infrastructureLayerShouldOnlyBeAccessedByConfiguration() {
        ArchRule rule = classes()
            .that().resideInAPackage("..infrastructure..")
            .should().onlyBeAccessed().byAnyPackage(
                "..infrastructure..",
                "..infrastructure.config.."
            )
            .allowEmptyShould(true)
            .because("Infrastructure should only be accessed through configuration");

        rule.check(classes);
    }
}
