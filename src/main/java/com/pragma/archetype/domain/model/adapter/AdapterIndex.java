package com.pragma.archetype.domain.model.adapter;

import java.util.List;

import lombok.Builder;
import lombok.Value;

/**
 * Represents the index.json file that lists available adapters for a specific
 * framework/paradigm combination.
 * 
 * <p>
 * Index files are located at:
 * templates/frameworks/{framework}/{paradigm}/adapters/{direction}/index.json
 * </p>
 */
@Value
@Builder
public class AdapterIndex {
  /**
   * Represents a single adapter entry in the index.
   */
  @Value
  @Builder
  public static class AdapterIndexEntry {
    /**
     * Adapter name (e.g., "rest", "graphql", "postgresql").
     * Must match the adapter directory name.
     */
    String name;

    /**
     * Human-readable display name (e.g., "REST API", "GraphQL API").
     */
    String displayName;

    /**
     * Description of what this adapter does.
     */
    String description;

    /**
     * Adapter type: "entry-point" or "driven-adapter".
     */
    String type;

    /**
     * Adapter status: "stable", "beta", "experimental", "deprecated".
     */
    String status;

    /**
     * Adapter version (e.g., "1.0.0").
     */
    String version;
  }

  /**
   * Index version (e.g., "1.0.0").
   */
  String version;

  /**
   * Last update timestamp (ISO 8601 format).
   */
  String lastUpdated;

  /**
   * List of adapters available in this framework/paradigm/direction.
   */
  List<AdapterIndexEntry> adapters;
}
