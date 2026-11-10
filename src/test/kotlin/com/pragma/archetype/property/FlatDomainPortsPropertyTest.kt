package com.pragma.archetype.property

import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * Property-based tests for Flat Domain Ports Package (Property 1).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 1: Flat Domain Ports Package
 *
 * For any valid ProjectConfig and architecture type, verify `domain/ports/` is flat
 * (no `in/`/`out/`), uses `ports` (plural).
 *
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5**
 */
class FlatDomainPortsPropertyTest : PropertyTestBase() {

    private val templatesRoot = File("../backend-architecture-design-archetype-generator-templates/templates")

    enum class ArchType(val templateDir: String) {
        HEXAGONAL_SINGLE("hexagonal-single"),
        HEXAGONAL_MULTI("hexagonal-multi"),
        HEXAGONAL_MULTI_GRANULAR("hexagonal-multi-granular")
        // onion-single is excluded on purpose: it keeps use cases in
        // application.usecase and gateways in domain.gateway, without input ports
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadStructure(archType: ArchType): Map<String, Any> {
        val structureFile = File(templatesRoot, "architectures/${archType.templateDir}/structure.yml")
        return Yaml().load(structureFile.inputStream())
    }

    /**
     * Extracts all package paths from the structure.yml depending on architecture type.
     * For single-module: reads `packages` list
     * For multi-module: reads `modules[].packages` lists
     * For multi-granular: reads `modules[].packagePath` values
     */
    @Suppress("UNCHECKED_CAST")
    private fun extractAllPackagePaths(archType: ArchType): List<String> {
        val structure = loadStructure(archType)
        val paths = mutableListOf<String>()

        // Single-module architectures have a top-level `packages` list
        val packages = structure["packages"] as? List<String>
        if (packages != null) {
            paths.addAll(packages)
        }

        // Multi-module architectures have `modules` with `packages` or `packagePath`
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

        // Also check moduleMetadata for packageTypes
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
        "Property 1.1: No architecture should have domain/port/in package path" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val paths = extractAllPackagePaths(archType)
                val hasPortIn = paths.any {
                    it.contains("port/in") || it.contains("port.in")
                }
                hasPortIn shouldBe false
            }
        }

        "Property 1.2: No architecture should have domain/port/out package path" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val paths = extractAllPackagePaths(archType)
                val hasPortOut = paths.any {
                    it.contains("port/out") || it.contains("port.out")
                }
                hasPortOut shouldBe false
            }
        }

        "Property 1.3: All architectures should use 'ports' (plural) not 'port' (singular) for domain ports" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val paths = extractAllPackagePaths(archType)
                // Find paths related to domain ports
                val portRelatedPaths = paths.filter {
                    it.contains("port") && !it.contains("entry-points") && !it.contains("entrypoints")
                }
                // All port-related paths should use "ports" (plural)
                for (path in portRelatedPaths) {
                    // Should contain "ports" not just "port" (without the s)
                    val segments = path.replace(".", "/").split("/")
                    for (segment in segments) {
                        if (segment.startsWith("port")) {
                            segment shouldBe "ports"
                        }
                    }
                }
            }
        }

        "Property 1.4: domain/ports should be flat - no sub-packages under ports" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val paths = extractAllPackagePaths(archType)
                // Check that no path has ports/in, ports/out, ports.in, ports.out
                val hasPortsSubPackage = paths.any {
                    it.contains("ports/in") || it.contains("ports/out") ||
                    it.contains("ports.in") || it.contains("ports.out")
                }
                hasPortsSubPackage shouldBe false
            }
        }

        "Property 1.5: Every architecture should have a ports package or module" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val paths = extractAllPackagePaths(archType)
                val hasPortsPackage = paths.any {
                    it.contains("ports")
                }
                hasPortsPackage shouldBe true
            }
        }

        "Property 1.6: useCaseModule should reference domain layer for all architectures" {
            checkAll(Arb.enum<ArchType>()) { archType ->
                val structure = loadStructure(archType)
                val useCaseModule = structure["useCaseModule"] as? String
                if (useCaseModule != null) {
                    // hexagonal-multi-granular uses separate "usecases" module (under domain/ folder)
                    val validValues = listOf("domain", "usecases")
                    (useCaseModule in validValues) shouldBe true
                }
            }
        }
    }
}
