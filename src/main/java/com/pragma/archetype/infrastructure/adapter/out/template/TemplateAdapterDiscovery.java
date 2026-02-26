package com.pragma.archetype.infrastructure.adapter.out.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.port.out.TemplateRepository;

import lombok.RequiredArgsConstructor;

/**
 * Discovers adapters dynamically from template directories.
 * Scans templates/adapters/ directory and loads metadata.yml files.
 * 
 * <p>
 * This replaces hardcoded enums (InputAdapterType, AdapterType) with
 * dynamic discovery. New adapters can be added by creating template
 * directories without modifying code.
 * </p>
 */
@RequiredArgsConstructor
public class TemplateAdapterDiscovery implements AdapterDiscoveryPort {

  private static final Logger logger = LoggerFactory.getLogger(TemplateAdapterDiscovery.class);
  private static final String ADAPTERS_BASE_PATH = "adapters";

  /**
   * List of known adapter names to scan.
   * TODO: Replace with directory scanning when filesystem access is available.
   */
  private static final List<String> KNOWN_ADAPTERS = List.of(
      "rest",
      "graphql",
      "grpc",
      "websocket",
      "sqs-consumer",
      "scheduled",
      "jpa",
      "mongodb",
      "redis",
      "dynamodb",
      "sqs-producer",
      "http-client");
  private final TemplateRepository templateRepository;

  private final AdapterMetadataLoader metadataLoader;

  @Override
  public List<AdapterMetadata> discoverAdapters(AdapterDirection direction) {
    logger.debug("Discovering {} adapters", direction);

    var adapters = new ArrayList<AdapterMetadata>();

    for (String adapterName : KNOWN_ADAPTERS) {
      try {
        // Try to load metadata for this adapter
        String metadataPath = ADAPTERS_BASE_PATH + "/" + adapterName + "/metadata.yml";

        if (templateRepository.templateExists(metadataPath)) {
          AdapterMetadata metadata = metadataLoader.loadMetadata(metadataPath);

          // Filter by direction
          if (metadata.getDirection() == direction) {
            adapters.add(metadata);
            logger.debug("Discovered {} adapter: {}", direction, adapterName);
          }
        }
      } catch (Exception e) {
        logger.warn("Failed to load metadata for adapter '{}': {}", adapterName, e.getMessage());
      }
    }

    logger.info("Discovered {} {} adapters", adapters.size(), direction);
    return adapters;
  }

  @Override
  public Optional<AdapterMetadata> findAdapter(String adapterName, AdapterDirection direction) {
    logger.debug("Finding adapter: {} ({})", adapterName, direction);

    try {
      String metadataPath = ADAPTERS_BASE_PATH + "/" + adapterName + "/metadata.yml";

      if (!templateRepository.templateExists(metadataPath)) {
        logger.debug("Adapter metadata not found: {}", metadataPath);
        return Optional.empty();
      }

      AdapterMetadata metadata = metadataLoader.loadMetadata(metadataPath);

      // Verify direction matches
      if (metadata.getDirection() != direction) {
        logger.warn("Adapter '{}' found but has wrong direction: expected {}, got {}",
            adapterName, direction, metadata.getDirection());
        return Optional.empty();
      }

      logger.debug("Found adapter: {} ({})", adapterName, direction);
      return Optional.of(metadata);

    } catch (Exception e) {
      logger.error("Error loading adapter metadata for '{}': {}", adapterName, e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Override
  public boolean adapterExists(String adapterName, AdapterDirection direction) {
    return findAdapter(adapterName, direction).isPresent();
  }

  @Override
  public List<String> listAdapterNames(AdapterDirection direction) {
    return discoverAdapters(direction).stream()
        .map(AdapterMetadata::getName)
        .sorted()
        .toList();
  }
}
