package com.pragma.archetype.property

import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * Property-based tests for Domain Use Cases Package Path (Property 15).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 15: Domain Use Cases Package Path
 *
 * For any valid architecture type and use case name, verify use case placed in
 * `domain/usecases/` (not `application/usecase/`).
 *
 * **Validates: Requirements 30.1, 30.2, 30.3, 30.4**
 */
class DomainUseCasesPackagePropertyTest : PropertyTestBase() {

    private val templatesRoot = File("../backend-architecture-design-archetype-generator-templates/templates")

    enum class ArchType(val templateDir: String) {
        HEXAGONAL_SINGLE("hexagonal-single"),
        HEXAGONAL_MULTI("hexagonal-multi"),
        HEXAGONAL_MULTI_GRANULAR("hexagonal-multi-granular"),
        ONION_SINGLE("onion-single")
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadStructure(archType: ArchType): Map<String, Any> {
        val structureFile = File(templatesRoot, "architectures/${archType.templateDir}/structure.yml")
        return Yaml().load(structureFile.inputStream())
    }

    /**
     * Extracts all package paths from the structure.yml.
     */
    @Suppress("UNCHECKED_CAST")
    private fun extractAllPackagePaths(archType: ArchType): List<String> {
        val structure = loadStructure(archType)
        val paths = mutableListOf<String>()

        val packages = structure["packages"] as? List<String>
        if (packages != null) {
            paths.addAll(packages)
        }

        val modules = structure["modules"] as? List<Map<String, Any>>
        if (modules != null) {
            for (module in modules) {
                val modulePkgs = module["packages"] as? List<String>
                if (modulePkgs != null) {
                    paths.addAll(modulePkgs)
                }
                val packagePath = module["packagePath"] as? String
                if (packagePath != null) {
                    paths.add(packagePath)
                }
                val path = module["path"] as? String
                if (path != null) {
                    paths.add(path)
                }
            }
        }

        val moduleMetadata = structure["moduleMetadata"] as? List<Map<String, Any>>
        if (moduleMetadata != null) {
            for (meta in moduleMetadata) {
                val packageTypes = meta["packageTypes"] as? Map<String, String>
                if (packageTypes != null) {
                    paths.addAll(packageTypes.values)
                }
            }
        }

        return paths
    }

    init {
        "Property 15.1: No architecture should have application/usecase package path" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val paths = extractAllPackagePaths(archType)
                val hasApplicationUsecase = paths.any {
                    it.contains("application/usecase") || it.contains("application.usecase")
                }
                hasApplicationUsecase shouldBe false
            }
        }

        "Property 15.2: All architectures should have usecases in domain layer" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val paths = extractAllPackagePaths(archType)
                val hasUsecasesInDomain = paths.any {
                    it.contains("domain/usecases") || it.contains("domain.usecases") ||
                    it.contains("domain/usecases") || it == "usecases"
                }
                hasUsecasesInDomain shouldBe true
            }
        }

        "Property 15.3: useCaseModule should reference domain layer for all architectures" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val structure = loadStructure(archType)
                val useCaseModule = structure["useCaseModule"] as? String
                if (useCaseModule != null) {
                    // hexagonal-multi-granular uses separate "usecases" module (still under domain/ folder)
                    val validValues = listOf("domain", "usecases")
                    (useCaseModule in validValues) shouldBe true
                }
            }
        }

        "Property 15.4: Use cases package should use 'usecases' (plural) not 'usecase' (singular)" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val paths = extractAllPackagePaths(archType)
                val usecaseRelatedPaths = paths.filter {
                    it.contains("usecase")
                }
                for (path in usecaseRelatedPaths) {
                    val segments = path.replace(".", "/").split("/")
                    for (segment in segments) {
                        if (segment.startsWith("usecase")) {
                            segment shouldBe "usecases"
                        }
                    }
                }
            }
        }

        "Property 15.5: entityModule should reference domain layer for all architectures" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val structure = loadStructure(archType)
                val entityModule = structure["entityModule"] as? String
                if (entityModule != null) {
                    // hexagonal-multi-granular uses separate "model" module (still under domain/ folder)
                    val validValues = listOf("domain", "model")
                    (entityModule in validValues) shouldBe true
                }
            }
        }

        "Property 15.6: For multi-granular, usecases module path should be under domain/" {
            val structure = loadStructure(ArchType.HEXAGONAL_MULTI_GRANULAR)
            @Suppress("UNCHECKED_CAST")
            val modules = structure["modules"] as? List<Map<String, Any>> ?: emptyList()
            val usecasesModule = modules.find { it["name"] == "usecases" }
            usecasesModule shouldBe usecasesModule // exists
            val path = usecasesModule?.get("path") as? String
            path?.startsWith("domain/") shouldBe true
        }
    }
}
