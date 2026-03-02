package com.pragma.archetype.infrastructure.adapter.out.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterIndex;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.adapter.TemplateFile;
import com.pragma.archetype.domain.model.template.TemplateRepositoryPaths;
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

    logger.info("Checking if adapter exists: name={}, direction={}, framework={}, paradigm={}, indexPath={}",
        adapterName, direction, framework, paradigm, indexPath);

    if (!templateRepository.templateExists(indexPath)) {
      logger.warn("Index file not found: {}", indexPath);
      return false;
    }

    try {
      AdapterIndex index = readIndexJson(indexPath);
      logger.info("Index loaded successfully. Adapters found: {}",
          index.getAdapters().stream().map(a -> a.getName()).toList());

      boolean exists = index.getAdapters().stream()
          .anyMatch(a -> {
            boolean matches = a.getName().equals(adapterName);
            logger.debug("Comparing '{}' with '{}': {}", a.getName(), adapterName, matches);
            return matches;
          });

      logger.info("Adapter '{}' exists: {}", adapterName, exists);
      return exists;
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
    String adapterDir = TemplateRepositoryPaths.AdapterDirectories.getDirectoryName(
        direction == AdapterDirection.INPUT);
    return String.format(TemplateRepositoryPaths.Patterns.ADAPTER_INDEX,
        framework, paradigm, adapterDir);
  }

  private String buildMetadataPath(String framework, String paradigm,
      AdapterDirection direction, String adapterName) {
    String adapterDir = TemplateRepositoryPaths.AdapterDirectories.getDirectoryName(
        direction == AdapterDirection.INPUT);
    return String.format(TemplateRepositoryPaths.Patterns.ADAPTER_METADATA,
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
    String adapterDir = TemplateRepositoryPaths.AdapterDirectories.getDirectoryName(
        direction == AdapterDirection.INPUT);
    String templateDir = String.format(TemplateRepositoryPaths.Patterns.ADAPTER_TEMPLATE_DIR,
        framework, paradigm, adapterDir, adapterName);

    // Load metadata to get the suffix
    AdapterMetadata metadata = null;
    try {
      metadata = templateRepository.loadAdapterMetadata(adapterName, framework, paradigm, adapterDir);
    } catch (Exception e) {
      logger.warn("Failed to load metadata for adapter '{}': {}", adapterName, e.getMessage());
    }

    // Determine the main template name based on suffix from metadata
    String mainTemplateName = null;
    if (metadata != null && metadata.getSuffix() != null && !metadata.getSuffix().isBlank()) {
      mainTemplateName = metadata.getSuffix() + ".java.ftl";
      logger.debug("Main template determined from suffix: {}", mainTemplateName);
    }

    // Discover all .ftl files dynamically in the adapter directory
    List<String> templateFileNames = templateRepository.listTemplatesInDirectory(templateDir);

    if (templateFileNames.isEmpty()) {
      logger.warn("No template files found in directory: {}", templateDir);
      return Collections.emptyList();
    }

    logger.debug("Discovered {} template files in {}: {}", templateFileNames.size(), templateDir, templateFileNames);

    // Build TemplateFile list
    List<TemplateFile> templates = new ArrayList<>();
    for (String templateName : templateFileNames) {
      // Mark as "main" only if it matches the suffix from metadata
      String type = (mainTemplateName != null && templateName.equals(mainTemplateName)) ? "main" : "secondary";

      templates.add(TemplateFile.builder()
          .name(templateName)
          .type(type)
          .destination(templateName.replace(".ftl", ""))
          .build());
    }

    // If no main template was found but we have templates, mark the first one as
    // main (fallback)
    if (templates.stream().noneMatch(t -> "main".equals(t.getType())) && !templates.isEmpty()) {
      logger.warn("No template matched suffix '{}', using first template as main: {}",
          mainTemplateName, templates.get(0).getName());
      TemplateFile firstTemplate = templates.get(0);
      templates.set(0, TemplateFile.builder()
          .name(firstTemplate.getName())
          .type("main")
          .destination(firstTemplate.getDestination())
          .build());
    }

    logger.debug("Template discovery complete. Main: {}, Secondary: {}",
        templates.stream().filter(t -> "main".equals(t.getType())).map(TemplateFile::getName).findFirst()
            .orElse("none"),
        templates.stream().filter(t -> "secondary".equals(t.getType())).count());

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
