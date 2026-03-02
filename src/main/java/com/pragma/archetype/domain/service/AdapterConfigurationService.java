package com.pragma.archetype.domain.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;

/**
 * Shared service for adapter configuration tasks.
 * Handles application properties merging, configuration class generation,
 * dependency injection, and build configuration for both input and output
 * adapters.
 * 
 * This eliminates code duplication between GenerateInputAdapterUseCaseImpl
 * and GenerateAdapterUseCaseImpl.
 */
public class AdapterConfigurationService {

    /**
     * Configuration for adapter processing.
     */
    public record AdapterConfig(
            String name,
            String type,
            String framework,
            String paradigm,
            String packageName,
            String useCaseName,
            String entityName,
            AdapterDirection direction) {
    }

    private static final Logger logger = LoggerFactory.getLogger(AdapterConfigurationService.class);
    private final ConfigurationPort configurationPort;
    private final FileSystemPort fileSystemPort;
    private final TemplateRepository templateRepository;
    private final YamlConfigurationAdapter yamlConfigurationAdapter;
    private final BuildFileMerger buildFileMerger;

    private final BuildConfigurationProcessor buildConfigurationProcessor;

    public AdapterConfigurationService(
            ConfigurationPort configurationPort,
            FileSystemPort fileSystemPort,
            TemplateRepository templateRepository,
            YamlConfigurationAdapter yamlConfigurationAdapter) {
        this.configurationPort = configurationPort;
        this.fileSystemPort = fileSystemPort;
        this.templateRepository = templateRepository;
        this.yamlConfigurationAdapter = yamlConfigurationAdapter;
        this.buildFileMerger = new BuildFileMerger();
        this.buildConfigurationProcessor = new BuildConfigurationProcessor(templateRepository);
    }

    /**
     * Merges application properties from adapter template into existing
     * application.yml.
     * Only merges if the adapter has an applicationPropertiesTemplate defined.
     *
     * @param projectPath path to the project root
     * @param config      adapter configuration
     * @param metadata    adapter metadata
     */
    public void mergeApplicationPropertiesIfNeeded(
            Path projectPath,
            AdapterConfig config,
            AdapterMetadata metadata) {
        try {
            if (!metadata.hasApplicationProperties()) {
                logger.debug("Adapter {} does not have application properties template, skipping merge",
                        config.name());
                return;
            }

            logger.info("Processing application properties template for adapter: {}", config.name());

            // Read project configuration to get framework and paradigm
            var projectConfig = configurationPort.readConfiguration(projectPath).orElse(null);
            if (projectConfig == null) {
                logger.warn("Could not read project configuration, cannot determine template path");
                return;
            }

            // Load structure metadata to get adapter directory names
            StructureMetadata structureMetadata = templateRepository
                    .loadStructureMetadata(projectConfig.architecture());

            String framework = projectConfig.framework().name().toLowerCase();
            String paradigm = projectConfig.paradigm().name().toLowerCase();
            String adapterTypeCategory = determineAdapterTypeCategory(config.direction(), structureMetadata);
            String adapterTypeLower = config.type().toLowerCase();

            // Prepare template context with adapter information
            Map<String, Object> templateContext = prepareApplicationPropertiesContext(config, projectPath);

            // Build dynamic template path:
            // frameworks/{framework}/{paradigm}/adapters/{category}/{type}/{template}
            String propertiesTemplatePath = String.format("frameworks/%s/%s/adapters/%s/%s/%s",
                    framework, paradigm, adapterTypeCategory, adapterTypeLower,
                    metadata.applicationPropertiesTemplate());

            logger.debug("Processing application properties template from: {}", propertiesTemplatePath);
            String processedProperties = templateRepository.processTemplate(propertiesTemplatePath, templateContext);

            // Parse the processed properties as YAML
            org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
            Map<String, Object> newProperties = yaml.load(processedProperties);

            if (newProperties == null || newProperties.isEmpty()) {
                logger.warn("Processed application properties template is empty for adapter: {}", config.name());
                return;
            }

            // Determine application.yml path
            Path applicationYmlPath = projectPath.resolve("src/main/resources/application.yml");

            // Read existing application.yml if it exists
            Map<String, Object> existingProperties = yamlConfigurationAdapter.readYaml(applicationYmlPath);

            // Merge new properties using YamlConfigurationAdapter
            Map<String, Object> mergedProperties = yamlConfigurationAdapter.mergeYaml(existingProperties,
                    newProperties);

            // Write merged properties back to application.yml
            yamlConfigurationAdapter.writeYaml(applicationYmlPath, mergedProperties);

            logger.info("Successfully merged application properties for adapter: {}", config.name());

        } catch (TemplateRepository.TemplateNotFoundException e) {
            logger.warn("Application properties template not found for adapter {}: {}",
                    config.name(), e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to merge application properties for adapter {}: {}",
                    config.name(), e.getMessage(), e);
        }
    }

    /**
     * Generates configuration classes if the adapter has configurationClasses
     * defined.
     * 
     * @param projectPath path to the project root
     * @param config      adapter configuration
     * @param metadata    adapter metadata
     * @return list of generated configuration class files
     */
    public List<GeneratedFile> generateConfigurationClassesIfNeeded(
            Path projectPath,
            AdapterConfig config,
            AdapterMetadata metadata) {
        List<GeneratedFile> configFiles = new ArrayList<>();

        try {
            if (!metadata.hasConfigurationClasses()) {
                logger.debug("Adapter {} does not have configuration classes, skipping generation",
                        config.name());
                return configFiles;
            }

            logger.info("Generating configuration classes for adapter: {}", config.name());

            // Read project configuration to get base package, framework, and paradigm
            var projectConfig = configurationPort.readConfiguration(projectPath).orElse(null);
            if (projectConfig == null) {
                logger.warn("Could not read project configuration, skipping configuration class generation");
                return configFiles;
            }

            // Load structure metadata to get source directory
            StructureMetadata structureMetadata = templateRepository
                    .loadStructureMetadata(projectConfig.architecture());

            String basePackage = projectConfig.basePackage();
            String framework = projectConfig.framework().name().toLowerCase();
            String paradigm = projectConfig.paradigm().name().toLowerCase();
            String adapterTypeCategory = determineAdapterTypeCategory(config.direction(), structureMetadata);
            String adapterTypeLower = config.type().toLowerCase();

            // Prepare template context
            Map<String, Object> templateContext = prepareConfigurationClassContext(config, projectConfig);

            // Process each configuration class
            for (AdapterMetadata.ConfigurationClass configClass : metadata.configurationClasses()) {
                try {
                    // Build dynamic template path:
                    // frameworks/{framework}/{paradigm}/adapters/{category}/{type}/{template}
                    String configTemplatePath = String.format("frameworks/%s/%s/adapters/%s/%s/%s",
                            framework, paradigm, adapterTypeCategory, adapterTypeLower, configClass.templatePath());

                    logger.debug("Processing configuration class template from: {}", configTemplatePath);

                    // Process the template
                    String processedContent = templateRepository.processTemplate(configTemplatePath, templateContext);

                    // Resolve package path for the configuration class
                    String fullPackage = basePackage + "." + configClass.packagePath();
                    String packagePath = fullPackage.replace('.', '/');

                    // Build file path using dynamic source directory from metadata
                    String sourceDir = structureMetadata.getSourceDirectory();
                    String fileName = config.name() + configClass.name() + ".java";
                    Path configFilePath = projectPath
                            .resolve(sourceDir)
                            .resolve(packagePath)
                            .resolve(fileName);

                    // Create GeneratedFile
                    GeneratedFile configFile = GeneratedFile.javaSource(configFilePath, processedContent);
                    configFiles.add(configFile);

                    // Write file to disk immediately
                    fileSystemPort.writeFile(configFile);

                    logger.info("Generated configuration class: {} at {}", configClass.name(), configFilePath);

                } catch (TemplateRepository.TemplateNotFoundException e) {
                    logger.warn("Configuration class template not found: {} for adapter {}",
                            configClass.templatePath(), config.name());
                } catch (Exception e) {
                    logger.error("Failed to generate configuration class {} for adapter {}: {}",
                            configClass.name(), config.name(), e.getMessage(), e);
                }
            }

            logger.info("Successfully generated {} configuration class(es) for adapter: {}",
                    configFiles.size(), config.name());

        } catch (Exception e) {
            logger.error("Failed to generate configuration classes for adapter {}: {}",
                    config.name(), e.getMessage(), e);
        }

        return configFiles;
    }

    /**
     * Adds dependencies to the build file if the adapter has dependencies.
     * Dependencies are added with implementation scope in Gradle.
     *
     * @param projectPath path to the project root
     * @param config      adapter configuration
     * @param metadata    adapter metadata
     */
    public void addDependenciesIfNeeded(
            Path projectPath,
            AdapterConfig config,
            AdapterMetadata metadata) {
        try {
            if (metadata.getDependencies().isEmpty()) {
                logger.debug("Adapter {} does not have dependencies, skipping", config.name());
                return;
            }

            logger.info("Adding dependencies for adapter: {}", config.name());

            // Determine the build file path
            Path buildFilePath = projectPath.resolve("build.gradle.kts");

            if (!fileSystemPort.exists(buildFilePath)) {
                logger.warn("Build file not found at {}, skipping dependency addition", buildFilePath);
                return;
            }

            // Read the current build file content
            String buildFileContent = fileSystemPort.readFile(buildFilePath);

            // Add each dependency
            String updatedContent = buildFileContent;
            for (String dep : metadata.getDependencies()) {
                updatedContent = addDependencyToBuildFile(updatedContent, dep);
            }

            // Write the updated build file if changes were made
            if (!updatedContent.equals(buildFileContent)) {
                fileSystemPort.writeFile(GeneratedFile.create(buildFilePath, updatedContent));
                logger.info("Successfully added {} dependencies for adapter: {}",
                        metadata.getDependencies().size(), config.name());
            } else {
                logger.debug("No new dependencies to add for adapter: {}", config.name());
            }

        } catch (Exception e) {
            logger.error("Failed to add dependencies for adapter {}: {}",
                    config.name(), e.getMessage(), e);
        }
    }

    /**
     * Adds test dependencies to the build file if the adapter has testDependencies.
     * Test dependencies are added with testImplementation scope in Gradle.
     *
     * @param projectPath path to the project root
     * @param config      adapter configuration
     * @param metadata    adapter metadata
     */
    public void addTestDependenciesIfNeeded(
            Path projectPath,
            AdapterConfig config,
            AdapterMetadata metadata) {
        try {
            if (!metadata.hasTestDependencies()) {
                logger.debug("Adapter {} does not have test dependencies, skipping", config.name());
                return;
            }

            logger.info("Adding test dependencies for adapter: {}", config.name());

            // Determine the build file path
            Path buildFilePath = projectPath.resolve("build.gradle.kts");

            if (!fileSystemPort.exists(buildFilePath)) {
                logger.warn("Build file not found at {}, skipping test dependency addition", buildFilePath);
                return;
            }

            // Read the current build file content
            String buildFileContent = fileSystemPort.readFile(buildFilePath);

            // Add each test dependency
            String updatedContent = buildFileContent;
            for (AdapterMetadata.Dependency testDep : metadata.testDependencies()) {
                // Convert Dependency to coordinate string
                String coordinate = testDep.group() + ":" + testDep.artifact() + ":" + testDep.version();
                updatedContent = addTestDependencyToBuildFile(updatedContent, coordinate);
            }

            // Write the updated build file if changes were made
            if (!updatedContent.equals(buildFileContent)) {
                fileSystemPort.writeFile(GeneratedFile.create(buildFilePath, updatedContent));
                logger.info("Successfully added {} test dependencies for adapter: {}",
                        metadata.testDependencies().size(), config.name());
            } else {
                logger.debug("No new test dependencies to add for adapter: {}", config.name());
            }

        } catch (Exception e) {
            logger.error("Failed to add test dependencies for adapter {}: {}",
                    config.name(), e.getMessage(), e);
        }
    }

    /**
     * Adds build plugins and configuration to build.gradle.kts if the adapter has
     * them.
     * Plugins are added to the plugins {} block, and configuration is appended to
     * the end.
     *
     * @param projectPath       path to the project root
     * @param config            adapter configuration
     * @param metadata          adapter metadata
     * @param structureMetadata structure metadata for variable replacement
     */
    public void addBuildPluginsAndConfigurationIfNeeded(
            Path projectPath,
            AdapterConfig config,
            AdapterMetadata metadata,
            com.pragma.archetype.domain.model.structure.StructureMetadata structureMetadata) {

        try {
            if (!metadata.hasBuildPlugins() && !metadata.hasBuildConfiguration()) {
                logger.debug("Adapter {} does not have build plugins or configuration, skipping", config.name());
                return;
            }

            logger.info("Processing build plugins and configuration for adapter: {}", config.name());

            // Determine the build file path
            Path buildFilePath = projectPath.resolve("build.gradle.kts");

            if (!fileSystemPort.exists(buildFilePath)) {
                logger.warn("Build file not found at {}, skipping build configuration", buildFilePath);
                return;
            }

            // Read the current build file content
            String buildFileContent = fileSystemPort.readFile(buildFilePath);

            // Add plugins if present
            if (metadata.hasBuildPlugins()) {
                buildFileContent = buildFileMerger.addPlugins(buildFileContent, metadata.getBuildPlugins());
            }

            // Process and add build configuration if present
            if (metadata.hasBuildConfiguration()) {
                String processedConfig = buildConfigurationProcessor.process(
                        metadata.getBuildConfiguration(),
                        structureMetadata,
                        config.name(),
                        config.packageName(),
                        config.type());

                buildFileContent = buildFileMerger.addBuildConfiguration(buildFileContent, processedConfig);
            }

            // Write the updated build file
            fileSystemPort.writeFile(GeneratedFile.create(buildFilePath, buildFileContent));

            logger.info("Successfully added build plugins and configuration for adapter: {}", config.name());

        } catch (Exception e) {
            logger.error("Failed to add build plugins and configuration for adapter {}: {}",
                    config.name(), e.getMessage(), e);
        }
    }

    /**
     * Prepares template context for processing application properties template.
     *
     * @param config      adapter configuration
     * @param projectPath path to the project root
     * @return template context map
     */
    private Map<String, Object> prepareApplicationPropertiesContext(AdapterConfig config, Path projectPath) {
        Map<String, Object> context = new HashMap<>();
        context.put("adapterName", config.name());
        context.put("adapterType", config.type().toLowerCase());
        context.put("packageName", config.packageName());
        context.put("useCaseName", config.useCaseName());
        context.put("entityName", config.entityName());

        // Add project name if available
        try {
            var projectConfig = configurationPort.readConfiguration(projectPath).orElse(null);
            if (projectConfig != null) {
                context.put("projectName", projectConfig.name());
                context.put("basePackage", projectConfig.basePackage());
            }
        } catch (Exception e) {
            logger.debug("Could not read project configuration for template context: {}", e.getMessage());
        }

        return context;
    }

    /**
     * Prepares template context for processing configuration class templates.
     *
     * @param config        adapter configuration
     * @param projectConfig project configuration
     * @return template context map
     */
    private Map<String, Object> prepareConfigurationClassContext(
            AdapterConfig config,
            com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {
        Map<String, Object> context = new HashMap<>();
        context.put("adapterName", config.name());
        context.put("adapterType", config.type().toLowerCase());
        context.put("packageName", projectConfig.basePackage());
        context.put("useCaseName", config.useCaseName());
        context.put("entityName", config.entityName());
        context.put("basePackage", projectConfig.basePackage());
        context.put("projectName", projectConfig.name());
        context.put("framework", config.framework());
        context.put("paradigm", config.paradigm());

        return context;
    }

    /**
     * Adds a dependency to the build file content.
     * Uses implementation scope for Gradle.
     *
     * @param buildFileContent current build file content
     * @param dependency       dependency coordinate string (group:artifact:version)
     * @return updated build file content
     */
    private String addDependencyToBuildFile(String buildFileContent, String dependency) {
        // Parse dependency coordinate
        String[] parts = dependency.split(":");
        if (parts.length < 3) {
            logger.warn("Invalid dependency format: {}. Expected group:artifact:version", dependency);
            return buildFileContent;
        }

        String group = parts[0];
        String artifact = parts[1];
        String version = parts[2];

        // Format: implementation("group:artifact:version")
        String dependencyStatement = String.format("    implementation(\"%s:%s:%s\")",
                group, artifact, version);

        // Check if dependency is already present
        if (buildFileContent.contains(dependencyStatement)) {
            logger.debug("Dependency already present: {}", dependency);
            return buildFileContent;
        }

        // Find the dependencies block and add the dependency
        if (buildFileContent.contains("dependencies {")) {
            // Look for existing implementation dependencies section or add at the beginning
            if (buildFileContent.contains("implementation(")) {
                // Add after the last implementation
                int lastImpl = buildFileContent.lastIndexOf("implementation(");
                int endOfLine = buildFileContent.indexOf('\n', lastImpl);
                if (endOfLine != -1) {
                    return buildFileContent.substring(0, endOfLine + 1) +
                            dependencyStatement + "\n" +
                            buildFileContent.substring(endOfLine + 1);
                }
            }

            // If no implementation found, add after "dependencies {"
            int dependenciesStart = buildFileContent.indexOf("dependencies {");
            int lineEnd = buildFileContent.indexOf('\n', dependenciesStart);
            if (lineEnd != -1) {
                return buildFileContent.substring(0, lineEnd + 1) +
                        dependencyStatement + "\n" +
                        buildFileContent.substring(lineEnd + 1);
            }
        }

        logger.warn("Could not find dependencies block in build file, dependency not added: {}",
                dependency);
        return buildFileContent;
    }

    /**
     * Adds a test dependency to the build file content.
     * Uses testImplementation scope for Gradle.
     *
     * @param buildFileContent current build file content
     * @param dependency       dependency coordinate string (group:artifact:version)
     * @return updated build file content
     */
    private String addTestDependencyToBuildFile(String buildFileContent, String dependency) {
        // Parse dependency coordinate
        String[] parts = dependency.split(":");
        if (parts.length < 3) {
            logger.warn("Invalid test dependency format: {}. Expected group:artifact:version", dependency);
            return buildFileContent;
        }

        String group = parts[0];
        String artifact = parts[1];
        String version = parts[2];

        // Format: testImplementation("group:artifact:version")
        String dependencyStatement = String.format("    testImplementation(\"%s:%s:%s\")",
                group, artifact, version);

        // Check if dependency is already present
        if (buildFileContent.contains(dependencyStatement)) {
            logger.debug("Test dependency already present: {}", dependency);
            return buildFileContent;
        }

        // Find the dependencies block and add the test dependency
        if (buildFileContent.contains("dependencies {")) {
            // Look for existing test dependencies section or add at the end of dependencies
            // block
            if (buildFileContent.contains("testImplementation(")) {
                // Add after the last testImplementation
                int lastTestImpl = buildFileContent.lastIndexOf("testImplementation(");
                int endOfLine = buildFileContent.indexOf('\n', lastTestImpl);
                if (endOfLine != -1) {
                    return buildFileContent.substring(0, endOfLine + 1) +
                            dependencyStatement + "\n" +
                            buildFileContent.substring(endOfLine + 1);
                }
            }

            // If no testImplementation found, add before the closing brace of dependencies
            // block
            int dependenciesStart = buildFileContent.indexOf("dependencies {");
            int dependenciesEnd = findMatchingClosingBrace(buildFileContent,
                    dependenciesStart + "dependencies {".length());

            if (dependenciesEnd != -1) {
                // Add before the closing brace
                return buildFileContent.substring(0, dependenciesEnd) +
                        "\n    // Test dependencies\n" +
                        dependencyStatement + "\n" +
                        buildFileContent.substring(dependenciesEnd);
            }
        }

        logger.warn("Could not find dependencies block in build file, test dependency not added: {}",
                dependency);
        return buildFileContent;
    }

    /**
     * Finds the matching closing brace for a given opening position.
     *
     * @param content  the content to search
     * @param startPos position after the opening brace
     * @return position of the matching closing brace, or -1 if not found
     */
    private int findMatchingClosingBrace(String content, int startPos) {
        int braceCount = 1;
        for (int i = startPos; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Determines the adapter type category for framework-aware template loading.
     * Uses structure metadata to get the actual directory name configured for the
     * architecture.
     * 
     * @param direction         adapter direction
     * @param structureMetadata structure metadata
     * @return adapter type category string from structure metadata
     */
    private String determineAdapterTypeCategory(AdapterDirection direction, StructureMetadata structureMetadata) {
        String adapterType = direction == AdapterDirection.INPUT ? "driving" : "driven";
        return structureMetadata.getAdapterDirName(adapterType);
    }
}
