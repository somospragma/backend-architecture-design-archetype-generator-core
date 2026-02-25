package com.pragma.archetype.property

import com.pragma.archetype.domain.model.*
import com.pragma.archetype.domain.model.adapter.*
import com.pragma.archetype.domain.model.config.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*

/**
 * Generators (Arb) for domain models used in property-based testing.
 * 
 * These generators create random but valid instances of domain objects
 * for testing invariants and properties.
 */
object DomainModelGenerators {
    
    /**
     * Generates valid Java package names.
     * Format: lowercase letters, numbers, dots
     * Example: com.example.service
     */
    fun packageName(): Arb<String> = Arb.string(3..10, Codepoint.az()).flatMap { first ->
        Arb.string(3..10, Codepoint.az()).flatMap { second ->
            Arb.string(3..10, Codepoint.az()).map { third ->
                "$first.$second.$third"
            }
        }
    }
    
    /**
     * Generates valid project names.
     * Format: lowercase letters, numbers, hyphens
     * Example: my-service-api
     */
    fun projectName(): Arb<String> = Arb.string(3..8, Codepoint.az()).flatMap { first ->
        Arb.string(3..8, Codepoint.az()).map { second ->
            "$first-$second"
        }
    }
    
    /**
     * Generates valid Java class names.
     * Format: PascalCase
     * Example: UserService
     */
    fun className(): Arb<String> = Arb.string(3..15, Codepoint.az()).map { name ->
        name.replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Generates architecture types.
     */
    fun architectureType(): Arb<ArchitectureType> = 
        Arb.enum<ArchitectureType>()
    
    /**
     * Generates framework types.
     */
    fun framework(): Arb<Framework> = 
        Arb.enum<Framework>()
    
    /**
     * Generates paradigm types.
     */
    fun paradigm(): Arb<Paradigm> = 
        Arb.enum<Paradigm>()
    
    /**
     * Generates adapter types.
     */
    fun adapterType(): Arb<AdapterConfig.AdapterType> = 
        Arb.enum<AdapterConfig.AdapterType>()
    
    /**
     * Generates complete ProjectConfig instances.
     */
    fun projectConfig(): Arb<ProjectConfig> = 
        Arb.bind(
            projectName(),
            packageName(),
            architectureType(),
            framework(),
            paradigm(),
            Arb.boolean()
        ) { name, basePackage, architecture, framework, paradigm, adaptersAsModules ->
            ProjectConfig.builder()
                .name(name)
                .basePackage(basePackage)
                .architecture(architecture)
                .framework(framework)
                .paradigm(paradigm)
                .pluginVersion("1.0.0")
                .createdAt(java.time.LocalDateTime.now())
                .adaptersAsModules(adaptersAsModules)
                .dependencyOverrides(emptyMap())
                .build()
        }
    
    /**
     * Generates complete AdapterConfig instances.
     */
    fun adapterConfig(): Arb<AdapterConfig> = 
        Arb.bind(
            className(),
            packageName(),
            adapterType(),
            className()
        ) { name, packageName, type, entityName ->
            AdapterConfig.builder()
                .name(name)
                .packageName(packageName)
                .type(type)
                .entityName(entityName)
                .methods(emptyList())
                .build()
        }
    
    /**
     * Generates valid YAML map structures.
     */
    fun yamlMap(): Arb<Map<String, Any>> = 
        Arb.bind(
            Arb.int(1..5),
            Arb.list(Arb.string(3..10, Codepoint.az()), 1..5),
            Arb.list(Arb.choice(
                Arb.string(3..20),
                Arb.int(1..100),
                Arb.boolean()
            ), 1..5)
        ) { size, keys, values ->
            keys.take(size).zip(values.take(size)).toMap()
        }
}
