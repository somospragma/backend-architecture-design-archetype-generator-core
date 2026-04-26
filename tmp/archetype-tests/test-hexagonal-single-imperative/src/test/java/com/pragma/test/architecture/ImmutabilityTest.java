package com.pragma.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

/**
 * Immutability tests to ensure thread-safety and prevent concurrency issues.
 * Validates that use cases and beans have final fields (constructor injection).
 * 
 * DO NOT MODIFY OR DELETE THIS FILE.
 * This file is automatically synchronized during build to ensure immutability rules are enforced.
 */
class ImmutabilityTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.pragma.test");
    }

    @Test
    void useCasesShouldHaveFinalFields() {
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..application..")
            .and().areDeclaredInClassesThat().haveSimpleNameContaining("UseCase")
            .and().areNotStatic()
            .should().beFinal()
            .allowEmptyShould(true)
            .because("Use case fields should be final to ensure immutability and thread-safety");

        rule.check(classes);
    }

    @Test
    void domainServicesShouldHaveFinalFields() {
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..domain.service..")
            .and().areNotStatic()
            .should().beFinal()
            .allowEmptyShould(true)
            .because("Domain service fields should be final to ensure immutability");

        rule.check(classes);
    }

    @Test
    void configurationClassesShouldHaveFinalFields() {
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..infrastructure.config..")
            .and().areDeclaredInClassesThat().areAnnotatedWith("org.springframework.context.annotation.Configuration")
            .and().areNotStatic()
            .and().areNotAnnotatedWith("org.springframework.beans.factory.annotation.Value")
            .should().beFinal()
            .allowEmptyShould(true)
            .because("Configuration class fields should be final (constructor injection preferred over @Value)");

        rule.check(classes);
    }

    @Test
    void adaptersShouldHaveFinalFields() {
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..infrastructure.adapter..")
            .and().areNotStatic()
            .and().areNotAnnotatedWith("org.springframework.beans.factory.annotation.Value")
            .should().beFinal()
            .allowEmptyShould(true)
            .because("Adapter fields should be final to ensure immutability and thread-safety");

        rule.check(classes);
    }
}
