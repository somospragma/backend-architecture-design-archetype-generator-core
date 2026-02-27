package com.pragma.archetype.infrastructure.adapter.out.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.gradle.internal.impldep.com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterIndex;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.adapter.TemplateFile;
import com.pragma.archetype.domain.port.out.TemplateRepository;

import lombok.RequiredArgsConstructor;

/**
 * Discovers adapters dynamically from template directories by reading
 * index.json files.
 * 
 * <p>
 * This implementation validates adapters against index.json files for specific
 * framework/paradigm combinations, enabling true dynamic adapter discovery.
 * New adapters can be added by creating template directories and updating
 * index.json
 * without modifying any Java code.
 * </p>
 */
@RequiredArgsConstructor
public class TemplateAdapterDiscovery implements AdapterDiscoveryPort {

  private static final Logger logger = LoggerFactory.getLogger(TemplateAdapterDiscovery.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final TemplateRepository templateRepository;
  private final AdapterMetadataLoader metadataLoader;

  @Override
  public List<AdapterMetadata> discoverAdapters(
      AdapterDirection direction,
      String framework,
      String paradigm) {
    logger.debug("Discovering {} adapters for {}/{}", direction, framework, paradigm);

    String indexPath = buildIndexPath(framework, paradigm, direction);

    if (!templateRepository.templateExists(indexPath)) {
      logger.warn("Index file not found: {}", indexPath);
      return Collections.emptyList();
    }

    try {
      AdapterIndex index = readIndexJson(indexPath);
      var adapters = new ArrayList<AdapterMetadata>();

      for (AdapterIndex.AdapterIndexEntry entry : index.getAdapters()) {
        try {
          String metadataPath = buildMetadataPath(framework, paradigm, direction, entry.getName());
          AdapterMetadata metadata = metadataLoader.loadMetadata(metadataPath);

          // Enrich with index information
          metadata = enrichMetadata(metadata, entry, framework, paradigm, direction);
          adapters.add(metadata);

          logger.debug("Discovered {} adapter: {}", direction, entry.getName());
        } catch (Exception e) {
          logger.warn("Failed to load metadata for adapter '{}': {}", entry.getName(), e.getMessage());
        }
      }

      logger.info("Discovered {} {} adapters for {}/{}", adapters.size(), direction, framework, paradigm);
      return adapters;

    } catch (Exception e) {
      logger.error("Failed to read index.json: {}", indexPath, e);
      return Collections.emptyList();
    }
  }

  @Override
  public Optional<AdapterMetadata> findAdapter(
      String adapterName,
      AdapterDirection direction,
      String framework,
      String paradigm) {
    logger.debug("Finding adapter: {} ({}) for {}/{}", adapterName, direction, framework, paradigm);

    // 1. Validate exists in index.json
    if (!adapterExists(adapterName, direction, framework, paradigm)) {
      logger.debug("Adapter '{}' not found in index for {}/{}", adapterName, framework, paradigm);
      return Optional.empty();
    }

    try {
      // 2. Load metadata.yml
      String metadataPath = buildMetadataPath(framework, paradigm, direction, adapterName);
      AdapterMetadata metadata = metadataLoader.loadMetadata(metadataPath);

      // 3. Get index entry for enrichment
      String indexPath = buildIndexPath(framework, paradigm, direction);
      AdapterIndex index = readIndexJson(indexPath);
      Optional<AdapterIndex.AdapterIndexEntry> entryOpt = index.getAdapters().stream()
          .filter(e -> e.getName().equals(adapterName))
          .findFirst();

      if (entryOpt.isEmpty()) {
        return Optional.empty();
      }

      // 4. Discover templates automatically
      List<TemplateFile> templates = discoverTemplates(framework, paradigm, direction, adapterName);

      // 5. Enrich metadata with discovered templates and index info
      metadata = enrichMetadata(metadata, entryOpt.get(), framework, paradigm, direction);
      metadata = metadata.toBuilder()
          .discoveredTemplates(templates)
          .build();

      logger.debug("Found adapter: {} ({}) for {}/{}", adapterName, direction, framework, paradigm);
      return Optional.of(metadata);

    } catch (Exception e) {
      logger.error("Error loading adapter metadata for '{}': {}", adapterName, e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Override
  public boolean adapterExists(
      String adapterName,
      AdapterDirection direction,
      String framework,
      String paradigm) {
    String indexPath = buildIndexPath(framework, paradigm, direction);

    if (!templateRepository.templateExists(indexPath)) {
      logger.warn("Index file not found: {}", indexPath);
      return false;
    }

    try {
      AdapterIndex index = readIndexJson(indexPath);
      return index.getAdapters().stream()
          .anyMatch(a -> a.getName().equals(adapterName));
    } catch (Exception e) {
      logger.error("Failed to check adapter existence: {}", adapterName, e);
      return false;
    }
  }

  @Override
  public List<String> listAdapterNames(
      AdapterDirection direction,
      String framework,
      String paradigm) {
    String indexPath = buildIndexPath(framework, paradigm, direction);

    if (!templateRepository.templateExists(indexPath)) {
      logger.warn("Index file not found: {}", indexPath);
      return Collections.emptyList();
    }

    try {
      AdapterIndex index = readIndexJson(indexPath);
      return index.getAdapters().stream()
          .map(AdapterIndex.AdapterIndexEntry::getName)
          .sorted()
          .toList();
    } catch (Exception e) {
      logger.error("Failed to list adapter names: {}", indexPath, e);
      return Collections.emptyList();
    }
  }

  // Legacy methods (deprecated)

  @Override
  @Deprecated
  public List<AdapterMetadata> discoverAdapters(AdapterDirection direction) {
    // Default to spring/reactive for backward compatibility
    return discoverAdapters(direction, "spring", "reactive");
  }

  @Override
  @Deprecated
  public Optional<AdapterMetadata> findAdapter(String adapterName, AdapterDirection direction) {
    // Default to spring/reactive for backward compatibility
    return findAdapter(adapterName, direction, "spring", "reactive");
  }

  @Override
  @Deprecated
  public boolean adapterExists(String adapterName, AdapterDirection direction) {
    // Default to spring/reactive for backward compatibility
    return adapterExists(adapterName, direction, "spring", "reactive");
  }

  @Override
  @Deprecated
  public List<String> listAdapterNames(AdapterDirection direction) {
    // Default to spring/reactive for backward compatibility
    return listAdapterNames(direction, "spring", "reactive");
  }

  // Private helper methods

  private String buildIndexPath(String framework, String paradigm, AdapterDirection direction) {
    String adapterDir = direction == AdapterDirection.INPUT ? "entry-points" : "driven-adapters";
    return String.format("frameworks/%s/%s/adapters/%s/index.json",
        framework, paradigm, adapterDir);
  }

  private String buildMetadataPath(String framework, String paradigm,
      AdapterDirection direction, String adapterName) {
    String adapterDir = direction == AdapterDirection.INPUT ? "entry-points" : "driven-adapters";
    return String.format("frameworks/%s/%s/adapters/%s/%s/metadata.yml",
        framework, paradigm, adapterDir, adapterName);
  }

  private AdapterIndex readIndexJson(String indexPath) {
    try {
      String content = templateRepository.getTemplateContent(indexPath);
      return objectMapper.readValue(content, AdapterIndex.class);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to read index.json: " + indexPath, e);
    }
  }

  private List<TemplateFile> discoverTemplates(String framework, String paradigm,
      AdapterDirection direction, String adapterName) {
    String adapterDir = direction == AdapterDirection.INPUT ? "entry-points" : "driven-adapters";
    String templateDir = String.format("frameworks/%s/%s/adapters/%s/%s",
        framework, paradigm, adapterDir, adapterName);

    // Discover all .ftl files in adapter directory
    List<TemplateFile> templates = new ArrayList<>();

    // Try common template names
    String[] commonTemplates = {
        "Controller.java.ftl", "Adapter.java.ftl", "Repository.java.ftl",
        "Mapper.java.ftl", "Entity.java.ftl", "Config.java.ftl",
        "Resolver.java.ftl", "Service.java.ftl", "Handler.java.ftl",
        "Consumer.java.ftl", "Producer.java.ftl", "ScheduledTask.java.ftl"
    };

    for (String templateName : commonTemplates) {
      String templatePath = templateDir + "/" + templateName;
      if (templateRepository.templateExists(templatePath)) {
        templates.add(TemplateFile.builder()
            .name(templateName)
            .type("main")
            .destination(templateName.replace(".ftl", ""))
            .build());
      }
    }

    return templates;
  }

  private AdapterMetadata enrichMetadata(AdapterMetadata metadata,
      AdapterIndex.AdapterIndexEntry entry,
      String framework,
      String paradigm,
      AdapterDirection direction) {
    // Enrich metadata with information from index.json
    return metadata.toBuilder()
        .displayName(entry.getDisplayName() != null ? entry.getDisplayName() : metadata.getDisplayName())
        .description(entry.getDescription() != null ? entry.getDescription() : metadata.getDescription())
        .status(entry.getStatus())
        .version(entry.getVersion())
        .build();
  }
}
