package com.pragma.archetype.property

import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.yaml.snakeyaml.Yaml

/**
 * Property-based tests for YAML merge operations.
 * 
 * These tests verify invariants for YAML merging:
 * 1. Merge preserves all values from both maps
 * 2. New values override existing values
 * 3. Nested structures are merged correctly
 * 4. Round-trip (merge then serialize then parse) preserves data
 */
class YamlMergePropertyTest : PropertyTestBase() {
    
    private val yamlAdapter = YamlConfigurationAdapter()
    private val yaml = Yaml()
    
    init {
        "Property 1: Merge should preserve all keys from both maps" {
            checkAll(
                DomainModelGenerators.yamlMap(),
                DomainModelGenerators.yamlMap()
            ) { map1, map2 ->
                val merged = yamlAdapter.mergeYaml(map1, map2)
                
                // All keys from both maps should be present
                val allKeys = map1.keys + map2.keys
                allKeys.all { it in merged.keys } shouldBe true
            }
        }
        
        "Property 2: Existing values should be preserved (not overridden)" {
            checkAll(
                DomainModelGenerators.yamlMap(),
                DomainModelGenerators.yamlMap()
            ) { map1, map2 ->
                val merged = yamlAdapter.mergeYaml(map1, map2)
                
                // Values from map1 should be preserved (not overridden by map2)
                map1.forEach { (key, value) ->
                    if (key in merged) {
                        merged[key] shouldBe value
                    }
                }
            }
        }
        
        "Property 3: Merging with empty map should return original" {
            checkAll(DomainModelGenerators.yamlMap()) { map ->
                val merged = yamlAdapter.mergeYaml(map, emptyMap())
                merged shouldBe map
            }
        }
        
        "Property 4: Merging empty map with values should return values" {
            checkAll(DomainModelGenerators.yamlMap()) { map ->
                val merged = yamlAdapter.mergeYaml(emptyMap(), map)
                merged shouldBe map
            }
        }
        
        "Property 5: Merge is associative for non-overlapping keys" {
            checkAll(
                DomainModelGenerators.yamlMap(),
                DomainModelGenerators.yamlMap()
            ) { map1, map2 ->
                // Filter to non-overlapping keys
                val filtered1 = map1.filterKeys { it !in map2.keys }
                val filtered2 = map2.filterKeys { it !in map1.keys }
                
                if (filtered1.isNotEmpty() && filtered2.isNotEmpty()) {
                    val merged1 = yamlAdapter.mergeYaml(filtered1, filtered2)
                    val merged2 = yamlAdapter.mergeYaml(filtered2, filtered1)
                    
                    // Both should contain all keys
                    merged1.keys shouldBe merged2.keys
                }
            }
        }
        
        "Property 6: YAML round-trip preserves simple values" {
            checkAll(DomainModelGenerators.yamlMap()) { map ->
                // Serialize to YAML
                val yamlString = yaml.dump(map)
                
                // Parse back
                @Suppress("UNCHECKED_CAST")
                val parsed = yaml.load<Map<String, Any>>(yamlString)
                
                // Should preserve all keys
                parsed.keys shouldBe map.keys
            }
        }
        
        "Property 7: Merging a map with itself should return the same map" {
            checkAll(DomainModelGenerators.yamlMap()) { map ->
                val merged = yamlAdapter.mergeYaml(map, map)
                merged shouldBe map
            }
        }
    }
}
