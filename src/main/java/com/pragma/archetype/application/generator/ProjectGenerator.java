package com.pragma.archetype.application.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Application service responsible for generating project structure.
 * Orchestrates template processing and file generation.
 */
public class ProjectGenerator {

        private final TemplateRepository templateRepository;
        private final FileSystemPort fileSystemPort;

        public ProjectGenerator(TemplateRepository templateRepository, FileSystemPort fileSystemPort) {
                this.templateRepository = templateRepository;
                this.fileSystemPort = fileSystemPort;
        }

        /**
         * Generates the complete project structure based on configuration.
         *
         * @param projectPath the root directory where the project will be generated
         * @param config      the project configuration
         * @return list of generated files
         */
        public List<GeneratedFile> generateProject(Path projectPath, ProjectConfig config) {
                List<GeneratedFile> generatedFiles = new ArrayList<>();

                // 1. Prepare template context (variables for Freemarker)
                Map<String, Object> context = prepareTemplateContext(config);

                // 2. Generate base project structure
                generatedFiles.addAll(generateBaseStructure(projectPath, config, context));

                // 3. Check if multi-module architecture
                boolean isMultiModule = isMultiModuleArchitecture(config.architecture());

                if (isMultiModule) {
                        // 3a. Generate multi-module structure
                        generatedFiles.addAll(generateMultiModuleStructure(projectPath, config, context));
                } else {
                        // 3b. Generate single-module structure
                        generatedFiles.addAll(generateFrameworkFiles(projectPath, config, context));
                        generatedFiles.addAll(generateArchitectureStructure(projectPath, config, context));
                }

                // 4. Write all files to disk
                fileSystemPort.writeFiles(generatedFiles);

                return generatedFiles;
        }

        /**
         * Updates settings.gradle.kts to include a new module.
         * Reads the file, adds the include statement if not present, and writes back.
         *
         * @param projectPath the project root path
         * @param modulePath  the module path (e.g.,
         *                    "infrastructure:entry-points:rest-api")
         */
        public void addModuleToSettings(Path projectPath, String modulePath) {
                Path settingsFile = projectPath.resolve("settings.gradle.kts");

                if (!fileSystemPort.exists(settingsFile)) {
                        throw new RuntimeException("settings.gradle.kts not found");
                }

                String content = fileSystemPort.readFile(settingsFile);
                String includeStatement = "include(\"" + modulePath + "\")";

                // Check if already included
                if (content.contains(includeStatement)) {
                        return; // Already included
                }

                // Add the include statement at the end
                String newContent = content.trim() + "\n" + includeStatement + "\n";
                fileSystemPort.writeFile(GeneratedFile.create(settingsFile, newContent));
        }

        /**
         * Updates a module's build.gradle.kts to add a dependency.
         * Reads the file, adds the dependency if not present, and writes back.
         *
         * @param projectPath    the project root path
         * @param modulePath     the module path (e.g., "application/app-service")
         * @param dependencyPath the dependency module path (e.g.,
         *                       ":infrastructure:entry-points:rest-api")
         */
        public void addDependencyToModule(Path projectPath, String modulePath, String dependencyPath) {
                Path buildFile = projectPath.resolve(modulePath).resolve("build.gradle.kts");

                if (!fileSystemPort.exists(buildFile)) {
                        throw new RuntimeException("build.gradle.kts not found for module: " + modulePath);
                }

                String content = fileSystemPort.readFile(buildFile);
                String dependencyStatement = "    implementation(project(\"" + dependencyPath + "\"))";

                // Check if already added
                if (content.contains(dependencyStatement)) {
                        return; // Already added
                }

                // Find the dependencies block and add the dependency
                if (content.contains("dependencies {")) {
                        // Add after "dependencies {"
                        String newContent = content.replace(
                                        "dependencies {",
                                        "dependencies {\n" + dependencyStatement);
                        fileSystemPort.writeFile(GeneratedFile.create(buildFile, newContent));
                } else {
                        throw new RuntimeException("dependencies block not found in build.gradle.kts");
                }
        }

        /**
         * Checks if the architecture is multi-module.
         */
        private boolean isMultiModuleArchitecture(ArchitectureType architecture) {
                return architecture.isMultiModule();
        }

        /**
         * Prepares the template context with all variables needed for template
         * processing.
         *
         * @param config the project configuration
         * @return map of template variables
         */
        private Map<String, Object> prepareTemplateContext(ProjectConfig config) {
                Map<String, Object> context = new HashMap<>();

                // Basic project info
                context.put("projectName", config.name());
                context.put("projectNamePascalCase", toPascalCase(config.name()));
                context.put("basePackage", config.basePackage());
                context.put("packagePath", config.basePackage().replace('.', '/'));

                // Architecture info
                context.put("architecture", config.architecture().getValue());
                context.put("architectureType", config.architecture());

                // Paradigm info
                context.put("paradigm", config.paradigm().name().toLowerCase());
                context.put("isReactive", config.paradigm().name().equalsIgnoreCase("REACTIVE"));
                context.put("isImperative", config.paradigm().name().equalsIgnoreCase("IMPERATIVE"));

                // Framework info
                context.put("framework", config.framework().name().toLowerCase());
                context.put("isSpring", config.framework().name().equalsIgnoreCase("SPRING"));
                context.put("isQuarkus", config.framework().name().equalsIgnoreCase("QUARKUS"));

                // Plugin info
                context.put("pluginVersion", config.pluginVersion());
                context.put("createdAt", config.createdAt().toString());

                // Java version
                context.put("javaVersion", "21");

                // Dependency versions
                context.put("groupId", config.basePackage());
                context.put("version", "0.0.1-SNAPSHOT");
                context.put("springBootVersion", "3.2.1");
                context.put("mapstructVersion", "1.5.5.Final");

                return context;
        }

        /**
         * Generates base project files (build.gradle.kts, settings.gradle.kts, etc.)
         *
         * @param projectPath the project root path
         * @param config      the project configuration
         * @param context     the template context
         * @return list of generated files
         */
        private List<GeneratedFile> generateBaseStructure(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context) {

                List<GeneratedFile> files = new ArrayList<>();

                // Get base templates for the architecture
                String architecturePath = "architectures/"
                                + config.architecture().getValue();

                // Generate build.gradle.kts
                String buildGradleContent = templateRepository.processTemplate(
                                architecturePath + "/project/build.gradle.kts.ftl",
                                context);
                files.add(GeneratedFile.create(
                                projectPath.resolve("build.gradle.kts"),
                                buildGradleContent));

                // Generate settings.gradle.kts
                String settingsGradleContent = templateRepository.processTemplate(
                                architecturePath + "/project/settings.gradle.kts.ftl",
                                context);
                files.add(GeneratedFile.create(
                                projectPath.resolve("settings.gradle.kts"),
                                settingsGradleContent));

                // Generate .gitignore
                String gitignoreContent = templateRepository.processTemplate(
                                architecturePath + "/project/.gitignore.ftl",
                                context);
                files.add(GeneratedFile.create(
                                projectPath.resolve(".gitignore"),
                                gitignoreContent));

                // Generate README.md
                String readmeContent = templateRepository.processTemplate(
                                architecturePath + "/project/README.md.ftl",
                                context);
                files.add(GeneratedFile.create(
                                projectPath.resolve("README.md"),
                                readmeContent));

                return files;
        }

        /**
         * Generates framework-specific files (application.yml, Application.java, etc.)
         *
         * @param projectPath the project root path
         * @param config      the project configuration
         * @param context     the template context
         * @return list of generated files
         */
        private List<GeneratedFile> generateFrameworkFiles(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context) {

                List<GeneratedFile> files = new ArrayList<>();

                String frameworkPath = "frameworks/" + config.framework().name().toLowerCase() + "/"
                                + config.paradigm().name().toLowerCase() + "/project";

                // Generate application.yml
                String applicationYmlContent = templateRepository.processTemplate(
                                frameworkPath + "/application.yml.ftl",
                                context);

                Path resourcesPath = projectPath.resolve("src/main/resources");
                fileSystemPort.createDirectory(resourcesPath);

                files.add(GeneratedFile.create(
                                resourcesPath.resolve("application.yml"),
                                applicationYmlContent));

                // Generate main Application class
                String applicationClassContent = templateRepository.processTemplate(
                                frameworkPath + "/Application.java.ftl",
                                context);

                String applicationClassName = toPascalCase(config.name()) + "Application.java";
                Path mainClassPath = projectPath
                                .resolve("src/main/java")
                                .resolve(config.basePackage().replace('.', '/'))
                                .resolve(applicationClassName);

                fileSystemPort.createDirectory(mainClassPath.getParent());

                files.add(GeneratedFile.create(
                                mainClassPath,
                                applicationClassContent));

                // Generate BeanConfiguration class (Spring dependency injection config)
                String architecturePath = "architectures/"
                                + config.architecture().getValue();

                String beanConfigContent = templateRepository.processTemplate(
                                architecturePath + "/project/BeanConfiguration.java.ftl",
                                context);

                Path configPath = projectPath
                                .resolve("src/main/java")
                                .resolve(config.basePackage().replace('.', '/'))
                                .resolve("infrastructure/config")
                                .resolve("BeanConfiguration.java");

                fileSystemPort.createDirectory(configPath.getParent());

                files.add(GeneratedFile.create(
                                configPath,
                                beanConfigContent));

                return files;
        }

        /**
         * Generates architecture-specific folder structure.
         * Loads structure metadata from structure.yml and creates packages dynamically.
         *
         * @param projectPath the project root path
         * @param config      the project configuration
         * @param context     the template context
         * @return list of generated files (empty for now, just creates directories)
         */
        private List<GeneratedFile> generateArchitectureStructure(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context) {

                List<GeneratedFile> files = new ArrayList<>();

                try {
                        // Load structure metadata for the architecture
                        StructureMetadata structureMetadata = templateRepository
                                        .loadStructureMetadata(config.architecture());

                        Path basePath = projectPath
                                        .resolve("src/main/java")
                                        .resolve(config.basePackage().replace('.', '/'));

                        // Create directories from structure metadata packages
                        if (structureMetadata.packages() != null && !structureMetadata.packages().isEmpty()) {
                                for (String packagePath : structureMetadata.packages()) {
                                        Path fullPath = basePath.resolve(packagePath);
                                        fileSystemPort.createDirectory(fullPath);

                                        // Add .gitkeep file to preserve empty directories
                                        files.add(GeneratedFile.create(
                                                        fullPath.resolve(".gitkeep"),
                                                        ""));
                                }
                        } else {
                                // Fallback to hardcoded structure if packages not defined in metadata
                                createFallbackStructure(basePath, config.architecture(), files);
                        }

                } catch (Exception e) {
                        // If structure metadata loading fails, fall back to hardcoded structure
                        System.err.println("Failed to load structure metadata, using fallback structure: "
                                        + e.getMessage());
                        Path basePath = projectPath
                                        .resolve("src/main/java")
                                        .resolve(config.basePackage().replace('.', '/'));
                        createFallbackStructure(basePath, config.architecture(), files);
                }

                return files;
        }

        /**
         * Creates fallback folder structure when structure metadata is not available.
         * This maintains backward compatibility with existing behavior.
         */
        private void createFallbackStructure(Path basePath, ArchitectureType architecture, List<GeneratedFile> files) {
                switch (architecture) {
                        case HEXAGONAL_SINGLE, HEXAGONAL_MULTI, HEXAGONAL_MULTI_GRANULAR -> {
                                // Domain layer
                                fileSystemPort.createDirectory(basePath.resolve("domain/model"));
                                fileSystemPort.createDirectory(basePath.resolve("domain/port/in"));
                                fileSystemPort.createDirectory(basePath.resolve("domain/port/out"));

                                // Application layer
                                fileSystemPort.createDirectory(basePath.resolve("application/usecase"));

                                // Infrastructure layer
                                fileSystemPort.createDirectory(basePath.resolve("infrastructure/entry-points/rest"));
                                fileSystemPort.createDirectory(basePath.resolve("infrastructure/driven-adapters"));
                                fileSystemPort.createDirectory(basePath.resolve("infrastructure/config"));

                                // Add .gitkeep files to preserve empty directories
                                files.add(GeneratedFile.create(basePath.resolve("domain/model/.gitkeep"), ""));
                                files.add(GeneratedFile.create(basePath.resolve("domain/port/in/.gitkeep"), ""));
                                files.add(GeneratedFile.create(basePath.resolve("domain/port/out/.gitkeep"), ""));
                                files.add(GeneratedFile.create(basePath.resolve("application/usecase/.gitkeep"), ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("infrastructure/entry-points/rest/.gitkeep"), ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("infrastructure/driven-adapters/.gitkeep"), ""));
                                files.add(GeneratedFile.create(basePath.resolve("infrastructure/config/.gitkeep"), ""));
                        }
                        case ONION_SINGLE, ONION_MULTI -> {
                                // Core layer
                                fileSystemPort.createDirectory(basePath.resolve("core/domain"));
                                fileSystemPort.createDirectory(basePath.resolve("core/application/service"));
                                fileSystemPort.createDirectory(basePath.resolve("core/application/port"));

                                // Infrastructure layer
                                fileSystemPort.createDirectory(basePath.resolve("infrastructure/adapter/in"));
                                fileSystemPort.createDirectory(basePath.resolve("infrastructure/adapter/out"));
                                fileSystemPort.createDirectory(basePath.resolve("infrastructure/config"));

                                // Add .gitkeep files
                                files.add(GeneratedFile.create(basePath.resolve("core/domain/.gitkeep"), ""));
                                files.add(GeneratedFile.create(basePath.resolve("core/application/service/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(basePath.resolve("core/application/port/.gitkeep"), ""));
                                files.add(GeneratedFile.create(basePath.resolve("infrastructure/adapter/in/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(basePath.resolve("infrastructure/adapter/out/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(basePath.resolve("infrastructure/config/.gitkeep"), ""));
                        }
                }
        }

        /**
         * Converts a kebab-case or snake_case string to PascalCase.
         * Examples: "payment-service" -> "PaymentService", "test_project" ->
         * "TestProject"
         */
        private String toPascalCase(String input) {
                if (input == null || input.isEmpty()) {
                        return input;
                }

                StringBuilder result = new StringBuilder();
                boolean capitalizeNext = true;

                for (char c : input.toCharArray()) {
                        if (c == '-' || c == '_' || c == ' ') {
                                capitalizeNext = true;
                        } else if (capitalizeNext) {
                                result.append(Character.toUpperCase(c));
                                capitalizeNext = false;
                        } else {
                                result.append(Character.toLowerCase(c));
                        }
                }

                return result.toString();
        }

        /**
         * Generates multi-module project structure.
         * Creates separate modules with their own build files and source directories.
         *
         * @param projectPath the project root path
         * @param config      the project configuration
         * @param context     the template context
         * @return list of generated files
         */
        private List<GeneratedFile> generateMultiModuleStructure(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context) {

                List<GeneratedFile> files = new ArrayList<>();

                String architecturePath = "architectures/"
                                + config.architecture().getValue();

                // For hexagonal-multi: domain, application, infrastructure
                if (config.architecture() == ArchitectureType.HEXAGONAL_MULTI) {
                        files.addAll(generateDomainModule(projectPath, config, context, architecturePath));
                        files.addAll(generateApplicationModule(projectPath, config, context, architecturePath));
                        files.addAll(generateInfrastructureModule(projectPath, config, context, architecturePath));
                }

                // For hexagonal-multi-granular: domain modules + app-service
                if (config.architecture() == ArchitectureType.HEXAGONAL_MULTI_GRANULAR) {
                        files.addAll(generateGranularStructure(projectPath, config, context, architecturePath));
                }

                return files;
        }

        /**
         * Generates the domain module for multi-module architecture.
         */
        private List<GeneratedFile> generateDomainModule(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context,
                        String architecturePath) {

                List<GeneratedFile> files = new ArrayList<>();
                Path domainPath = projectPath.resolve("domain");

                // Generate domain/build.gradle.kts
                String buildGradleContent = templateRepository.processTemplate(
                                architecturePath + "/modules/domain/build.gradle.kts.ftl",
                                context);
                files.add(GeneratedFile.create(
                                domainPath.resolve("build.gradle.kts"),
                                buildGradleContent));

                // Create domain package structure
                Path domainSrcPath = domainPath
                                .resolve("src/main/java")
                                .resolve(config.basePackage().replace('.', '/'))
                                .resolve("domain");

                fileSystemPort.createDirectory(domainSrcPath.resolve("model"));
                fileSystemPort.createDirectory(domainSrcPath.resolve("port/in"));
                fileSystemPort.createDirectory(domainSrcPath.resolve("port/out"));

                // Add .gitkeep files
                files.add(GeneratedFile.create(domainSrcPath.resolve("model/.gitkeep"), ""));
                files.add(GeneratedFile.create(domainSrcPath.resolve("port/in/.gitkeep"), ""));
                files.add(GeneratedFile.create(domainSrcPath.resolve("port/out/.gitkeep"), ""));

                return files;
        }

        /**
         * Generates the application module for multi-module architecture.
         */
        private List<GeneratedFile> generateApplicationModule(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context,
                        String architecturePath) {

                List<GeneratedFile> files = new ArrayList<>();
                Path applicationPath = projectPath.resolve("application");

                // Generate application/build.gradle.kts
                String buildGradleContent = templateRepository.processTemplate(
                                architecturePath + "/modules/application/build.gradle.kts.ftl",
                                context);
                files.add(GeneratedFile.create(
                                applicationPath.resolve("build.gradle.kts"),
                                buildGradleContent));

                // Create application package structure
                Path applicationSrcPath = applicationPath
                                .resolve("src/main/java")
                                .resolve(config.basePackage().replace('.', '/'))
                                .resolve("application");

                fileSystemPort.createDirectory(applicationSrcPath.resolve("usecase"));

                // Add .gitkeep file
                files.add(GeneratedFile.create(applicationSrcPath.resolve("usecase/.gitkeep"), ""));

                return files;
        }

        /**
         * Generates the infrastructure module for multi-module architecture.
         */
        private List<GeneratedFile> generateInfrastructureModule(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context,
                        String architecturePath) {

                List<GeneratedFile> files = new ArrayList<>();
                Path infrastructurePath = projectPath.resolve("infrastructure");

                // Generate infrastructure/build.gradle.kts
                String buildGradleContent = templateRepository.processTemplate(
                                architecturePath + "/modules/infrastructure/build.gradle.kts.ftl",
                                context);
                files.add(GeneratedFile.create(
                                infrastructurePath.resolve("build.gradle.kts"),
                                buildGradleContent));

                // Create infrastructure package structure
                Path infrastructureSrcPath = infrastructurePath
                                .resolve("src/main/java")
                                .resolve(config.basePackage().replace('.', '/'))
                                .resolve("infrastructure");

                fileSystemPort.createDirectory(infrastructureSrcPath.resolve("entrypoints/rest"));
                fileSystemPort.createDirectory(infrastructureSrcPath.resolve("drivenadapters"));
                fileSystemPort.createDirectory(infrastructureSrcPath.resolve("config"));

                // Add .gitkeep files
                files.add(GeneratedFile.create(infrastructureSrcPath.resolve("entrypoints/rest/.gitkeep"), ""));
                files.add(GeneratedFile.create(infrastructureSrcPath.resolve("drivenadapters/.gitkeep"), ""));

                // Generate application.yml
                String frameworkPath = "frameworks/" + config.framework().name().toLowerCase() + "/"
                                + config.paradigm().name().toLowerCase() + "/project";

                String applicationYmlContent = templateRepository.processTemplate(
                                frameworkPath + "/application.yml.ftl",
                                context);

                Path resourcesPath = infrastructurePath.resolve("src/main/resources");
                fileSystemPort.createDirectory(resourcesPath);

                files.add(GeneratedFile.create(
                                resourcesPath.resolve("application.yml"),
                                applicationYmlContent));

                // Generate main Application class
                String applicationClassContent = templateRepository.processTemplate(
                                frameworkPath + "/Application.java.ftl",
                                context);

                String applicationClassName = toPascalCase(config.name()) + "Application.java";
                Path mainClassPath = infrastructureSrcPath
                                .resolve("config")
                                .resolve(applicationClassName);

                files.add(GeneratedFile.create(
                                mainClassPath,
                                applicationClassContent));

                // Generate BeanConfiguration class
                String beanConfigContent = templateRepository.processTemplate(
                                architecturePath + "/project/BeanConfiguration.java.ftl",
                                context);

                Path configPath = infrastructureSrcPath
                                .resolve("config")
                                .resolve("BeanConfiguration.java");

                files.add(GeneratedFile.create(
                                configPath,
                                beanConfigContent));

                return files;
        }

        /**
         * Generates granular multi-module structure.
         * Creates domain modules (model, ports, usecase) and app-service module.
         * Infrastructure adapters are created dynamically when generating adapters.
         */
        private List<GeneratedFile> generateGranularStructure(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context,
                        String architecturePath) {

                List<GeneratedFile> files = new ArrayList<>();

                // Create organizing folders (not Gradle modules)
                fileSystemPort.createDirectory(projectPath.resolve("domain"));
                fileSystemPort.createDirectory(projectPath.resolve("application"));
                fileSystemPort.createDirectory(projectPath.resolve("infrastructure/entry-points"));
                fileSystemPort.createDirectory(projectPath.resolve("infrastructure/driven-adapters"));

                // Generate domain modules
                files.addAll(generateGranularDomainModelModule(projectPath, config, context, architecturePath));
                files.addAll(generateGranularDomainPortsModule(projectPath, config, context, architecturePath));
                files.addAll(generateGranularDomainUseCaseModule(projectPath, config, context, architecturePath));

                // Generate application module
                files.addAll(generateGranularAppServiceModule(projectPath, config, context, architecturePath));

                return files;
        }

        private List<GeneratedFile> generateGranularDomainModelModule(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context,
                        String architecturePath) {

                List<GeneratedFile> files = new ArrayList<>();
                Path modulePath = projectPath.resolve("domain/model");

                // Generate build.gradle.kts
                String buildGradleContent = templateRepository.processTemplate(
                                architecturePath + "/modules/domain-model/build.gradle.kts.ftl",
                                context);
                files.add(GeneratedFile.create(
                                modulePath.resolve("build.gradle.kts"),
                                buildGradleContent));

                // Create package structure
                Path srcPath = modulePath
                                .resolve("src/main/java")
                                .resolve(config.basePackage().replace('.', '/'))
                                .resolve("domain/model");

                fileSystemPort.createDirectory(srcPath);
                files.add(GeneratedFile.create(srcPath.resolve(".gitkeep"), ""));

                return files;
        }

        private List<GeneratedFile> generateGranularDomainPortsModule(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context,
                        String architecturePath) {

                List<GeneratedFile> files = new ArrayList<>();
                Path modulePath = projectPath.resolve("domain/ports");

                // Generate build.gradle.kts
                String buildGradleContent = templateRepository.processTemplate(
                                architecturePath + "/modules/domain-ports/build.gradle.kts.ftl",
                                context);
                files.add(GeneratedFile.create(
                                modulePath.resolve("build.gradle.kts"),
                                buildGradleContent));

                // Create package structure
                Path srcPath = modulePath
                                .resolve("src/main/java")
                                .resolve(config.basePackage().replace('.', '/'))
                                .resolve("domain/port");

                fileSystemPort.createDirectory(srcPath.resolve("in"));
                fileSystemPort.createDirectory(srcPath.resolve("out"));
                files.add(GeneratedFile.create(srcPath.resolve("in/.gitkeep"), ""));
                files.add(GeneratedFile.create(srcPath.resolve("out/.gitkeep"), ""));

                return files;
        }

        private List<GeneratedFile> generateGranularDomainUseCaseModule(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context,
                        String architecturePath) {

                List<GeneratedFile> files = new ArrayList<>();
                Path modulePath = projectPath.resolve("domain/usecase");

                // Generate build.gradle.kts
                String buildGradleContent = templateRepository.processTemplate(
                                architecturePath + "/modules/domain-usecase/build.gradle.kts.ftl",
                                context);
                files.add(GeneratedFile.create(
                                modulePath.resolve("build.gradle.kts"),
                                buildGradleContent));

                // Create package structure
                Path srcPath = modulePath
                                .resolve("src/main/java")
                                .resolve(config.basePackage().replace('.', '/'))
                                .resolve("domain/usecase");

                fileSystemPort.createDirectory(srcPath);
                files.add(GeneratedFile.create(srcPath.resolve(".gitkeep"), ""));

                return files;
        }

        private List<GeneratedFile> generateGranularAppServiceModule(
                        Path projectPath,
                        ProjectConfig config,
                        Map<String, Object> context,
                        String architecturePath) {

                List<GeneratedFile> files = new ArrayList<>();
                Path modulePath = projectPath.resolve("application/app-service");

                // Generate build.gradle.kts
                String buildGradleContent = templateRepository.processTemplate(
                                architecturePath + "/modules/app-service/build.gradle.kts.ftl",
                                context);
                files.add(GeneratedFile.create(
                                modulePath.resolve("build.gradle.kts"),
                                buildGradleContent));

                // Create package structure
                Path srcPath = modulePath
                                .resolve("src/main/java")
                                .resolve(config.basePackage().replace('.', '/'))
                                .resolve("config");

                fileSystemPort.createDirectory(srcPath);

                // Generate application.yml
                String frameworkPath = "frameworks/" + config.framework().name().toLowerCase() + "/"
                                + config.paradigm().name().toLowerCase() + "/project";

                String applicationYmlContent = templateRepository.processTemplate(
                                frameworkPath + "/application.yml.ftl",
                                context);

                Path resourcesPath = modulePath.resolve("src/main/resources");
                fileSystemPort.createDirectory(resourcesPath);

                files.add(GeneratedFile.create(
                                resourcesPath.resolve("application.yml"),
                                applicationYmlContent));

                // Generate main Application class
                String applicationClassContent = templateRepository.processTemplate(
                                frameworkPath + "/Application.java.ftl",
                                context);

                String applicationClassName = toPascalCase(config.name()) + "Application.java";
                Path mainClassPath = srcPath.resolve(applicationClassName);

                files.add(GeneratedFile.create(
                                mainClassPath,
                                applicationClassContent));

                // Generate BeanConfiguration class
                String beanConfigContent = templateRepository.processTemplate(
                                architecturePath + "/project/BeanConfiguration.java.ftl",
                                context);

                Path configPath = srcPath.resolve("BeanConfiguration.java");

                files.add(GeneratedFile.create(
                                configPath,
                                beanConfigContent));

                return files;
        }

}
