package com.pragma.archetype.domain.model.structure;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.pragma.archetype.domain.model.validation.ValidationResult;

/**
 * Metadata describing an architecture's structure and conventions.
 * Loaded from structure.yml files in template repository.
 */
public record StructureMetadata(
    String architectureType,
    String resourcesPath,
    Map<String, String> adapterPaths,
    TemplatePaths templatePaths,
    PackageMappings packageMappings,
    NamingConventions namingConventions,
    LayerDependencies layerDependencies,
    java.util.List<String> packages,
    java.util.List<ModuleMetadata> moduleMetadata,
    String entityModule,
    String useCaseModule,
    java.util.List<StaticFile> staticFiles,
    String sourceDirectory,
    String testDirectory,
    java.util.List<String> folders,
    boolean adaptersAsModules,
    Boolean multiModule) {

  private static final String DEFAULT_SOURCE_DIR = "src/main/java";
  private static final String DEFAULT_TEST_DIR = "src/test/java";
  private static final String DEFAULT_RESOURCES_PATH = "src/main/resources";

  public StructureMetadata {
    Objects.requireNonNull(architectureType, "Architecture type cannot be null");
    Objects.requireNonNull(adapterPaths, "Adapter paths cannot be null");
    adapterPaths = Collections.unmodifiableMap(adapterPaths);
    if (packages != null) {
      packages = Collections.unmodifiableList(packages);
    }
    if (moduleMetadata != null) {
      moduleMetadata = Collections.unmodifiableList(moduleMetadata);
    }
    if (staticFiles != null) {
      staticFiles = Collections.unmodifiableList(staticFiles);
    }
    folders = folders == null ? java.util.List.of() : java.util.List.copyOf(folders);
  }

  /**
   * Creates metadata without organizing folders and with adapters generated as
   * packages (the defaults when structure.yml omits them).
   */
  public StructureMetadata(
      String architectureType,
      String resourcesPath,
      Map<String, String> adapterPaths,
      TemplatePaths templatePaths,
      PackageMappings packageMappings,
      NamingConventions namingConventions,
      LayerDependencies layerDependencies,
      java.util.List<String> packages,
      java.util.List<ModuleMetadata> moduleMetadata,
      String entityModule,
      String useCaseModule,
      java.util.List<StaticFile> staticFiles,
      String sourceDirectory,
      String testDirectory) {
    this(architectureType, resourcesPath, adapterPaths, templatePaths, packageMappings, namingConventions,
        layerDependencies, packages, moduleMetadata, entityModule, useCaseModule, staticFiles, sourceDirectory,
        testDirectory, java.util.List.of(), false, null);
  }

  /**
   * Whether the generated project is a multi-module Gradle build. Uses the
   * explicit {@code multiModule} flag of structure.yml; single-module
   * architectures also declare moduleMetadata (to locate packages), so the
   * presence of modules alone is not enough. Without the flag, falls back to
   * {@link #isMultiModule()}.
   */
  public boolean generatesMultiModuleProject() {
    return multiModule != null ? multiModule : isMultiModule();
  }

  /**
   * Gradle module, relative to the project root, that owns an adapter. Its
   * dependencies, build plugins, configuration classes and resources all go
   * there: the adapter's own module when adapters are modules, the module
   * containing the adapter path in multi-module projects, or {@code "."} for
   * single-module projects.
   *
   * @param adapterType "driven" or "driving"
   * @param adapterName the adapter name as given by the user
   */
  public String adapterModulePath(String adapterType, String adapterName) {
    if (adaptersAsModules) {
      String baseDirectory = getAdapterBaseDirectory(adapterType);
      if (baseDirectory == null) {
        baseDirectory = "driving".equals(adapterType)
            ? "infrastructure/entry-points"
            : "infrastructure/driven-adapters";
      }
      return baseDirectory + "/" + adapterName.toLowerCase();
    }
    if (!generatesMultiModuleProject()) {
      return ".";
    }
    String baseDirectory = getAdapterBaseDirectory(adapterType);
    ModuleMetadata owningModule = findModuleForPath(baseDirectory);
    return owningModule != null ? owningModule.getPath() : ".";
  }

  /**
   * Resources directory of an adapter's module, relative to the project root.
   */
  public String adapterResourcesPath(String adapterType, String adapterName) {
    String modulePath = adapterModulePath(adapterType, adapterName);
    return ".".equals(modulePath) ? getResourcesPath() : modulePath + "/" + DEFAULT_RESOURCES_PATH;
  }

  /**
   * Module that holds a kind of domain code ("ports", "usecases", "model"): the
   * multi-module module declaring that package type, else the use case module,
   * else {@code "."}. Lets an architecture keep ports in their own module.
   */
  public String modulePathForPackageType(String packageType) {
    if (!generatesMultiModuleProject()) {
      return ".";
    }
    for (ModuleMetadata module : getModuleMetadata()) {
      if (module.getPackageTypes() != null && module.getPackageTypes().containsKey(packageType)
          && module.getPath() != null) {
        return module.getPath();
      }
    }
    if (useCaseModule != null && !useCaseModule.isBlank()) {
      ModuleMetadata module = getModule(useCaseModule);
      if (module != null && module.getPath() != null) {
        return module.getPath();
      }
    }
    return ".";
  }

  /**
   * Module that runs the application (holds application.yml and the main
   * class): the module containing {@code resourcesPath}, or {@code "."}.
   */
  public String applicationModulePath() {
    if (!generatesMultiModuleProject()) {
      return ".";
    }
    ModuleMetadata appModule = findModuleForPath(getResourcesPath());
    return appModule != null ? appModule.getPath() : ".";
  }

  /**
   * Resolves the path for an adapter based on its type and name.
   *
   * @param adapterType The type of adapter (driven or driving)
   * @param name        The adapter name
   * @return The resolved path with placeholders substituted
   */
  public String resolveAdapterPath(String adapterType, String name) {
    String pathTemplate = adapterPaths.get(adapterType);
    if (pathTemplate == null) {
      throw new IllegalArgumentException(
          "No adapter path defined for type: " + adapterType);
    }
    return pathTemplate.replace("{name}", name);
  }

  /**
   * Validates that all required fields are present.
   *
   * @return ValidationResult indicating if metadata is valid
   */
  public ValidationResult validate() {
    if (adapterPaths.isEmpty()) {
      return ValidationResult.failure("Adapter paths cannot be empty");
    }
    if (!adapterPaths.containsKey("driven") && !adapterPaths.containsKey("driving")) {
      return ValidationResult.failure(
          "Adapter paths must contain at least 'driven' or 'driving' entries");
    }
    return ValidationResult.success();
  }

  /**
   * Checks if this structure has naming conventions defined.
   */
  public boolean hasNamingConventions() {
    return namingConventions != null;
  }

  /**
   * Checks if this structure has layer dependencies defined.
   */
  public boolean hasLayerDependencies() {
    return layerDependencies != null;
  }

  /**
   * Checks if this structure supports multiple modules.
   */
  public boolean isMultiModule() {
    return moduleMetadata != null && !moduleMetadata.isEmpty();
  }

  /**
   * Gets the list of module metadata for multi-module architectures.
   */
  public java.util.List<ModuleMetadata> getModuleMetadata() {
    return moduleMetadata != null ? moduleMetadata : Collections.emptyList();
  }

  /**
   * Gets a specific module by name.
   *
   * @param moduleName The module name to find
   * @return The ModuleMetadata or null if not found
   */
  public ModuleMetadata getModule(String moduleName) {
    if (moduleMetadata == null) {
      return null;
    }
    return moduleMetadata.stream()
        .filter(m -> m.getName().equals(moduleName))
        .findFirst()
        .orElse(null);
  }

  /**
   * Gets adapter base directory without {name} placeholder.
   * Example: "infrastructure/entry-points/{name}" → "infrastructure/entry-points"
   *
   * @param adapterType The adapter type (driven or driving)
   * @return The base directory path
   */
  public String getAdapterBaseDirectory(String adapterType) {
    String pathTemplate = adapterPaths.get(adapterType);
    if (pathTemplate == null) {
      return null;
    }
    return pathTemplate.replace("/{name}", "").replace("{name}", "");
  }

  /**
   * Gets the entity module metadata.
   *
   * @return The entity module metadata
   * @throws IllegalStateException if entityModule is not configured
   */
  public ModuleMetadata getEntityModule() {
    if (entityModule == null || entityModule.isBlank()) {
      throw new IllegalStateException(
          com.pragma.archetype.domain.model.error.StructureErrorMessages.ENTITY_MODULE_NOT_CONFIGURED);
    }
    return getModule(entityModule);
  }

  /**
   * Gets the use case module metadata.
   *
   * @return The use case module metadata
   * @throws IllegalStateException if useCaseModule is not configured
   */
  public ModuleMetadata getUseCaseModule() {
    if (useCaseModule == null || useCaseModule.isBlank()) {
      throw new IllegalStateException(
          com.pragma.archetype.domain.model.error.StructureErrorMessages.USE_CASE_MODULE_NOT_CONFIGURED);
    }
    return getModule(useCaseModule);
  }

  /**
   * Resolves a template path with variable substitution.
   *
   * @param templateType The type of template (e.g., "inputAdapter", "entity")
   * @param variables    The variables to substitute
   * @return The resolved template path
   * @throws IllegalStateException if templatePaths is not configured or variables
   *                               are unresolved
   */
  public String resolveTemplatePath(String templateType, Map<String, String> variables) {
    if (templatePaths == null) {
      throw new IllegalStateException("Template paths not configured in structure.yml");
    }
    return templatePaths.resolve(templateType, variables);
  }

  /**
   * Converts filesystem path to Java package name.
   * Example: "infrastructure/entry-points" → "infrastructure.entrypoints"
   * Applies packageMappings transformations if configured, otherwise uses
   * fallback logic.
   *
   * @param path The filesystem path
   * @return The Java package name
   */
  public String pathToPackageName(String path) {
    if (path == null) {
      return null;
    }

    // Use packageMappings if configured
    if (packageMappings != null) {
      return packageMappings.transform(path);
    }

    // Fallback logic
    return path
        .replace("-", "") // Remove hyphens (entry-points → entrypoints)
        .replace("/", "."); // Convert to package notation
  }

  /**
   * Gets adapter directory name for template path resolution.
   * Example: "infrastructure/entry-points" → "entry-points"
   *
   * @param adapterType The adapter type (driven or driving)
   * @return The directory name (last segment of path)
   */
  public String getAdapterDirName(String adapterType) {
    String basePath = getAdapterBaseDirectory(adapterType);
    if (basePath == null) {
      return null;
    }
    // Get last segment
    int lastSlash = basePath.lastIndexOf('/');
    return lastSlash >= 0 ? basePath.substring(lastSlash + 1) : basePath;
  }

  /**
   * Checks if template paths are configured.
   */
  public boolean hasTemplatePaths() {
    return templatePaths != null;
  }

  /**
   * Gets template paths or default if not configured.
   */
  public TemplatePaths getTemplatePathsOrDefault() {
    return templatePaths != null ? templatePaths : TemplatePaths.builder().build();
  }

  /**
   * Gets the list of static files to be copied.
   * 
   * @return List of static files or empty list if none configured
   */
  public java.util.List<StaticFile> getStaticFiles() {
    return staticFiles != null ? staticFiles : Collections.emptyList();
  }

  /**
   * Checks if this structure has static files configured.
   */
  public boolean hasStaticFiles() {
    return staticFiles != null && !staticFiles.isEmpty();
  }

  /**
   * Finds the module whose path is a prefix of the given filesystem path.
   * For example, if the path is "infrastructure/driven-adapters/account"
   * and there's a module with path "infrastructure", it returns that module.
   * For single-module architectures (module path "."), returns null.
   *
   * @param filesystemPath The filesystem path to match against module paths
   * @return The matching ModuleMetadata, or null if no multi-module match found
   */
  public ModuleMetadata findModuleForPath(String filesystemPath) {
    if (moduleMetadata == null || filesystemPath == null) {
      return null;
    }
    for (ModuleMetadata module : moduleMetadata) {
      String modulePath = module.getPath();
      // Skip root modules (single-module architecture)
      if (".".equals(modulePath)) {
        continue;
      }
      // Check if the filesystem path starts with this module's path
      if (filesystemPath.startsWith(modulePath + "/") || filesystemPath.equals(modulePath)) {
        return module;
      }
    }
    return null;
  }

  /**
   * Strips the module path prefix from a filesystem path.
   * For example, "infrastructure/driven-adapters/account" with module path "infrastructure"
   * returns "driven-adapters/account".
   *
   * @param filesystemPath The full filesystem path
   * @param modulePath     The module path prefix to strip
   * @return The path without the module prefix
   */
  public String stripModulePrefix(String filesystemPath, String modulePath) {
    if (modulePath == null || ".".equals(modulePath) || filesystemPath == null) {
      return filesystemPath;
    }
    if (filesystemPath.startsWith(modulePath + "/")) {
      return filesystemPath.substring(modulePath.length() + 1);
    }
    if (filesystemPath.equals(modulePath)) {
      return "";
    }
    return filesystemPath;
  }

  /**
   * Gets the resources path for this architecture.
   * Returns the configured path from structure.yml or defaults to
   * "src/main/resources".
   * 
   * @return The resources path relative to project root
   */
  public String getResourcesPath() {
    return resourcesPath != null && !resourcesPath.isBlank()
        ? resourcesPath
        : DEFAULT_RESOURCES_PATH;
  }

  /**
   * Gets the source directory for this architecture.
   * Returns the configured directory from structure.yml or defaults to
   * "src/main/java".
   * 
   * @return The source directory relative to project root
   */
  public String getSourceDirectory() {
    return sourceDirectory != null && !sourceDirectory.isBlank()
        ? sourceDirectory
        : DEFAULT_SOURCE_DIR;
  }

  /**
   * Gets the test directory for this architecture.
   * Returns the configured directory from structure.yml or defaults to
   * "src/test/java".
   * 
   * @return The test directory relative to project root
   */
  public String getTestDirectory() {
    return testDirectory != null && !testDirectory.isBlank()
        ? testDirectory
        : DEFAULT_TEST_DIR;
  }
}
