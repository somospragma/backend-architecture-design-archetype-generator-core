package com.pragma.archetype.domain.model.adapter;

import java.util.List;

import org.gradle.internal.impldep.com.fasterxml.jackson.annotation.JsonCreator;
import org.gradle.internal.impldep.com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonCreator
    public AdapterIndexEntry(
        @JsonProperty("name") String name,
        @JsonProperty("displayName") String displayName,
        @JsonProperty("description") String description,
        @JsonProperty("type") String type,
        @JsonProperty("status") String status,
        @JsonProperty("version") String version) {
      this.name = name;
      this.displayName = displayName;
      this.description = description;
      this.type = type;
      this.status = status;
      this.version = version;
    }
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

  @JsonCreator
  public AdapterIndex(
      @JsonProperty("version") String version,
      @JsonProperty("lastUpdated") String lastUpdated,
      @JsonProperty("adapters") List<AdapterIndexEntry> adapters) {
    this.version = version;
    this.lastUpdated = lastUpdated;
    this.adapters = adapters;
  }
}
