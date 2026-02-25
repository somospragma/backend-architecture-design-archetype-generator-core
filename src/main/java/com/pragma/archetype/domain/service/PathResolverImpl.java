package com.pragma.archetype.domain.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.out.PathResolver;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Implementation of PathResolver that handles path resolution and validation
 * across different architecture types.
 */
public class PathResolverImpl implements PathResolver {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");
  private final TemplateRepository templateRepository;

  public PathResolverImpl(TemplateRepository templateRepository) {
    this.templateRepository = Objects.requireNonNull(templateRepository,
        "Template repository cannot be null");
  }

  @Override
  public Path resolveAdapterPath(
      ArchitectureType architecture,
      String adapterType,
      String name,
      Map<String, String> context) {

    Objects.requireNonNull(architecture, "Architecture cannot be null");
    Objects.requireNonNull(adapterType, "Adapter type cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");
    Objects.requireNonNull(context, "Context cannot be null");

    // Load structure metadata for the architecture
    StructureMetadata metadata = templateRepository.loadStructureMetadata(architecture);

    // Get the path template for the adapter type
    String pathTemplate = metadata.adapterPaths().get(adapterType);
    if (pathTemplate == null) {
      throw new IllegalArgumentException(
          String.format("No adapter path defined for type '%s' in architecture '%s'",
              adapterType, architecture.getValue()));
    }

    // Create context with name included
    Map<String, String> fullContext = new java.util.HashMap<>(context);
    fullContext.put("name", name);
    fullContext.put("type", adapterType);

    // If path template contains {module} placeholder and no module is provided,
    // check if this is a multi-module architecture
    if (pathTemplate.contains("{module}") && !fullContext.containsKey("module")) {
      if (metadata.isMultiModule()) {
        throw new IllegalArgumentException(
            String.format("Architecture '%s' is multi-module but no module was specified. " +
                "Available modules: %s",
                architecture.getValue(), String.join(", ", metadata.getModules())));
      }
      // For single-module architectures, remove the {module} placeholder
      pathTemplate = pathTemplate.replace("{module}/", "").replace("/{module}", "");
    }

    // Substitute placeholders
    String resolvedPath = substitutePlaceholders(pathTemplate, fullContext);

    return Paths.get(resolvedPath);
  }

  @Override
  public ValidationResult validatePath(Path path, ArchitectureType architecture) {
    Objects.requireNonNull(path, "Path cannot be null");
    Objects.requireNonNull(architecture, "Architecture cannot be null");

    // Load structure metadata
    StructureMetadata metadata = templateRepository.loadStructureMetadata(architecture);

    // If no layer dependencies defined, validation passes
    if (!metadata.hasLayerDependencies()) {
      return ValidationResult.success();
    }

    // Extract layer from path
    String pathStr = path.toString();
    String layer = extractLayer(pathStr);

    if (layer == null) {
      return ValidationResult.failure(
          String.format("Could not determine layer from path: %s", pathStr));
    }

    // Validate layer exists in dependency rules
    var layerDeps = metadata.layerDependencies();
    if (!layerDeps.allowedDependencies().containsKey(layer)) {
      return ValidationResult.failure(
          String.format("Layer '%s' is not defined in architecture '%s'",
              layer, architecture.getValue()));
    }

    return ValidationResult.success();
  }

  @Override
  public String substitutePlaceholders(String template, Map<String, String> context) {
    Objects.requireNonNull(template, "Template cannot be null");
    Objects.requireNonNull(context, "Context cannot be null");

    String result = template;
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);

    while (matcher.find()) {
      String placeholder = matcher.group(1);
      String value = context.get(placeholder);

      if (value != null) {
        result = result.replace("{" + placeholder + "}", value);
      }
    }

    return result;
  }

  /**
   * Extracts the layer name from a path string.
   * Looks for common layer names like domain, application, infrastructure, core.
   * Returns the first (leftmost) layer found in the path.
   *
   * @param pathStr The path string
   * @return The layer name, or null if not found
   */
  private String extractLayer(String pathStr) {
    String normalizedPath = pathStr.replace("\\", "/").toLowerCase();

    // Common layer names to look for
    String[] layers = { "core", "domain", "application", "infrastructure" };

    // Find the first layer that appears in the path
    int earliestIndex = Integer.MAX_VALUE;
    String foundLayer = null;

    for (String layer : layers) {
      int index = -1;

      if (normalizedPath.startsWith(layer + "/") || normalizedPath.equals(layer)) {
        index = 0;
      } else if (normalizedPath.contains("/" + layer + "/")) {
        index = normalizedPath.indexOf("/" + layer + "/");
      } else if (normalizedPath.endsWith("/" + layer)) {
        index = normalizedPath.lastIndexOf("/" + layer);
      }

      if (index >= 0 && index < earliestIndex) {
        earliestIndex = index;
        foundLayer = layer;
      }
    }

    return foundLayer;
  }
}
