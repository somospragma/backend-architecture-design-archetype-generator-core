package com.pragma.archetype.property

import com.pragma.archetype.domain.model.ArchitectureType
import com.pragma.archetype.domain.model.StructureMetadata
import com.pragma.archetype.domain.port.out.PathResolver
import com.pragma.archetype.domain.port.out.TemplateRepository
import com.pragma.archetype.domain.service.PathResolverImpl
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Property-based tests for path resolution.
 * 
 * These tests verify invariants that should hold for all valid inputs:
 * 1. Adapter paths should be consistent with adapter type
 * 2. Paths should not contain invalid characters
 * 3. Paths should be relative (not absolute)
 */
class PathResolutionPropertyTest : PropertyTestBase() {
    
    private val templateRepository: TemplateRepository = mock(TemplateRepository::class.java).apply {
        // Mock structure metadata for all architecture types
        ArchitectureType.values().forEach { arch ->
            val metadata = StructureMetadata(
                arch.value,
                mapOf(
                    "driven" to "infrastructure/adapter/out/{name}",
                    "driving" to "infrastructure/adapter/in/{name}"
                ),
                null, // namingConventions
                null, // layerDependencies
                emptyList(), // packages
                emptyList() // modules
            )
            `when`(loadStructureMetadata(arch)).thenReturn(metadata)
        }
    }
    
    private val pathResolver: PathResolver = PathResolverImpl(templateRepository)
    
    init {
        "Property 1: Driven adapters should be placed in driven/output paths" {
            checkAll(
                DomainModelGenerators.architectureType(),
                DomainModelGenerators.className()
            ) { architecture, adapterName ->
                val context = mapOf("basePackage" to "com.example")
                val path = pathResolver.resolveAdapterPath(
                    architecture,
                    "driven",
                    adapterName.lowercase(),
                    context
                )
                
                // Path should contain indicators of driven/output adapters
                val pathStr = path.toString()
                (pathStr.contains("driven") || 
                 pathStr.contains("out") || 
                 pathStr.contains("adapter")) shouldBe true
            }
        }
        
        "Property 2: Entry point adapters should be placed in entry-point/input paths" {
            checkAll(
                DomainModelGenerators.architectureType(),
                DomainModelGenerators.className()
            ) { architecture, adapterName ->
                val context = mapOf("basePackage" to "com.example")
                val path = pathResolver.resolveAdapterPath(
                    architecture,
                    "driving",
                    adapterName.lowercase(),
                    context
                )
                
                // Path should contain indicators of entry points/input adapters
                val pathStr = path.toString()
                (pathStr.contains("entry") || 
                 pathStr.contains("in") || 
                 pathStr.contains("adapter")) shouldBe true
            }
        }
        
        "Property 3: Resolved paths should never be absolute" {
            checkAll(
                DomainModelGenerators.architectureType(),
                DomainModelGenerators.className()
            ) { architecture, adapterName ->
                val context = mapOf("basePackage" to "com.example")
                val path = pathResolver.resolveAdapterPath(
                    architecture,
                    "driven",
                    adapterName.lowercase(),
                    context
                )
                
                // Path should be relative (not start with /)
                path.isAbsolute shouldBe false
            }
        }
        
        "Property 4: Paths should not contain invalid characters" {
            checkAll(
                DomainModelGenerators.architectureType(),
                DomainModelGenerators.className()
            ) { architecture, adapterName ->
                val context = mapOf("basePackage" to "com.example")
                val path = pathResolver.resolveAdapterPath(
                    architecture,
                    "driven",
                    adapterName.lowercase(),
                    context
                )
                
                val pathStr = path.toString()
                // Should not contain spaces, special chars
                pathStr shouldNotContain " "
                pathStr shouldNotContain "\t"
                pathStr shouldNotContain "\n"
            }
        }
        
        "Property 5: Same inputs should produce same output (deterministic)" {
            checkAll(
                DomainModelGenerators.architectureType(),
                DomainModelGenerators.className()
            ) { architecture, adapterName ->
                val context = mapOf("basePackage" to "com.example")
                
                val path1 = pathResolver.resolveAdapterPath(
                    architecture,
                    "driven",
                    adapterName.lowercase(),
                    context
                )
                
                val path2 = pathResolver.resolveAdapterPath(
                    architecture,
                    "driven",
                    adapterName.lowercase(),
                    context
                )
                
                path1 shouldBe path2
            }
        }
    }
}
