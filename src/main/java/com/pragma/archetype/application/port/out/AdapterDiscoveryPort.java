package com.pragma.archetype.application.port.out;

import java.util.List;
import java.util.Optional;

import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;

/**
 * Port for discovering available adapters from templates.
 * Replaces hardcoded enums with dynamic discovery.
 * 
 * <p>
 * Adapters are discovered by reading index.json files for specific
 * framework/paradigm combinations and loading metadata.yml files.
 * This enables adding new adapters without modifying code - just create
 * a new template directory and update index.json.
 * </p>
 */
public interface AdapterDiscoveryPort {

  /**
   * Discover all available adapters for a specific framework/paradigm/direction.
   * Reads from:
   * templates/frameworks/{framework}/{paradigm}/adapters/{direction}/index.json
   *
   * @param direction INPUT or OUTPUT
   * @param framework Framework name (e.g., "spring")
   * @param paradigm  Paradigm name (e.g., "imperative", "reactive")
   * @return List of adapter metadata, empty if none found
   */
  List<AdapterMetadata> discoverAdapters(
      AdapterDirection direction,
      String framework,
      String paradigm);

  /**
   * Find adapter metadata by name for a specific framework/paradigm.
   * Validates against index.json and returns metadata enriched with discovered
   * templates.
   *
   * @param adapterName Name like "rest", "kafka-consumer", etc.
   * @param direction   INPUT or OUTPUT
   * @param framework   Framework name (e.g., "spring")
   * @param paradigm    Paradigm name (e.g., "imperative", "reactive")
   * @return Adapter metadata if found and valid
   */
  Optional<AdapterMetadata> findAdapter(
      String adapterName,
      AdapterDirection direction,
      String framework,
      String paradigm);

  /**
   * Check if an adapter exists in index.json for a specific framework/paradigm.
   *
   * @param adapterName Name to check
   * @param direction   INPUT or OUTPUT
   * @param framework   Framework name (e.g., "spring")
   * @param paradigm    Paradigm name (e.g., "imperative", "reactive")
   * @return true if adapter exists in index.json
   */
  boolean adapterExists(
      String adapterName,
      AdapterDirection direction,
      String framework,
      String paradigm);

  /**
   * List all available adapter names from index.json.
   * Useful for error messages: "Available adapters: rest, graphql, grpc"
   *
   * @param direction INPUT or OUTPUT
   * @param framework Framework name (e.g., "spring")
   * @param paradigm  Paradigm name (e.g., "imperative", "reactive")
   * @return List of adapter names from index.json, empty if none found
   */
  List<String> listAdapterNames(
      AdapterDirection direction,
      String framework,
      String paradigm);

}
