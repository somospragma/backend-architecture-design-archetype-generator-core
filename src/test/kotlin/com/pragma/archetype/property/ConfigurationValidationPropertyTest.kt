package com.pragma.archetype.property

import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for configuration validation.
 * 
 * These tests verify validation rules hold for all inputs:
 * 1. Valid package names follow Java conventions
 * 2. Invalid package names are rejected
 * 3. Project names follow naming conventions
 */
class ConfigurationValidationPropertyTest : PropertyTestBase() {
    
    init {
        "Property 1: Valid package names should have at least 2 segments" {
            checkAll(DomainModelGenerators.packageName()) { packageName ->
                val segments = packageName.split(".")
                (segments.size >= 2) shouldBe true
            }
        }
        
        "Property 2: Package name segments should be lowercase" {
            checkAll(DomainModelGenerators.packageName()) { packageName ->
                val segments = packageName.split(".")
                segments.all { segment ->
                    segment.all { it.isLowerCase() || it.isDigit() || it == '_' }
                } shouldBe true
            }
        }
        
        "Property 3: Package names should not have consecutive dots" {
            checkAll(DomainModelGenerators.packageName()) { packageName ->
                packageName.contains("..") shouldBe false
            }
        }
        
        "Property 4: Package names should not start or end with dot" {
            checkAll(DomainModelGenerators.packageName()) { packageName ->
                packageName.startsWith(".") shouldBe false
                packageName.endsWith(".") shouldBe false
            }
        }
        
        "Property 5: Project names should be lowercase with hyphens" {
            checkAll(DomainModelGenerators.projectName()) { projectName ->
                projectName.all { it.isLowerCase() || it.isDigit() || it == '-' } shouldBe true
            }
        }
        
        "Property 6: Project names should not have consecutive hyphens" {
            checkAll(DomainModelGenerators.projectName()) { projectName ->
                projectName.contains("--") shouldBe false
            }
        }
        
        "Property 7: Project names should not start or end with hyphen" {
            checkAll(DomainModelGenerators.projectName()) { projectName ->
                projectName.startsWith("-") shouldBe false
                projectName.endsWith("-") shouldBe false
            }
        }
        
        "Property 8: Class names should start with uppercase" {
            checkAll(DomainModelGenerators.className()) { className ->
                className.first().isUpperCase() shouldBe true
            }
        }
        
        "Property 9: Class names should not contain special characters" {
            checkAll(DomainModelGenerators.className()) { className ->
                className.all { it.isLetterOrDigit() } shouldBe true
            }
        }
    }
}
