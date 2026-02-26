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
 * Adapters are discovered by scanning the templates/adapters/ directory
 * and loading metadata.yml files. This enables adding new adapters without
 * modifying code - just create a new template directory.
 * </p>
 */
public interface AdapterDiscoveryPort {

  /**
   * Discover all available adapters of a specific direction.
   *
   * @param direction INPUT or OUTPUT
   * @return List of adapter metadata, empty if none found
   */
  List<AdapterMetadata> discoverAdapters(AdapterDirection direction);

  /**
   * Find adapter metadata by name.
   *
   * @param adapterName Name like "rest", "kafka-consumer", etc.
   * @param direction   INPUT or OUTPUT
   * @return Adapter metadata if found
   */
  Optional<AdapterMetadata> findAdapter(String adapterName, AdapterDirection direction);

  /**
   * Check if an adapter exists.
   *
   * @param adapterName Name to check
   * @param direction   INPUT or OUTPUT
   * @return true if adapter template exists
   */
  boolean adapterExists(String adapterName, AdapterDirection direction);

  /**
   * List all available adapter names.
   * Useful for error messages: "Available adapters: rest, graphql, grpc,
   * kafka-consumer"
   *
   * @param direction INPUT or OUTPUT
   * @return List of adapter names, empty if none found
   */
  List<String> listAdapterNames(AdapterDirection direction);
}
