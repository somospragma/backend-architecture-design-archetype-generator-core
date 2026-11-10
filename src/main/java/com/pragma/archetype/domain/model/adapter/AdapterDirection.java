package com.pragma.archetype.domain.model.adapter;

/**
 * Represents the direction of an adapter in clean architecture.
 * Replaces the need for separate InputAdapterType and AdapterType enums.
 */
public enum AdapterDirection {
  /**
   * Input adapters (driving adapters) - receive requests from external sources.
   * Examples: REST controllers, GraphQL resolvers, gRPC services, message
   * consumers.
   */
  INPUT,

  /**
   * Output adapters (driven adapters) - interact with external systems.
   * Examples: database repositories, HTTP clients, message producers, cache
   * adapters.
   */
  OUTPUT
}
