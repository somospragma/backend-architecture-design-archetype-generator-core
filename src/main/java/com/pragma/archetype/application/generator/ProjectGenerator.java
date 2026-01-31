package com.pragma.archetype.application.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.ProjectConfig;
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

                // 3. Generate framework-specific files
                generatedFiles.addAll(generateFrameworkFiles(projectPath, config, context));

                // 4. Generate architecture-specific structure
                generatedFiles.addAll(generateArchitectureStructure(projectPath, config, context));

                // 5. Write all files to disk
                fileSystemPort.writeFiles(generatedFiles);

                return generatedFiles;
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
                context.put("architecture", config.architecture().name().toLowerCase());
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
                                + config.architecture().name().toLowerCase().replace('_', '-');

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

                return files;
        }

        /**
         * Generates architecture-specific folder structure.
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

                Path basePath = projectPath
                                .resolve("src/main/java")
                                .resolve(config.basePackage().replace('.', '/'));

                // Create architecture-specific folders based on type
                switch (config.architecture()) {
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
                                files.add(GeneratedFile.create(
                                                basePath.resolve("domain/model/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("domain/port/in/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("domain/port/out/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("application/usecase/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("infrastructure/entry-points/rest/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("infrastructure/driven-adapters/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("infrastructure/config/.gitkeep"),
                                                ""));
                        }
                        case ONION_SINGLE, ONION_MULTI -> {
                                // Core layer
                                fileSystemPort.createDirectory(basePath.resolve("core/domain"));
                                fileSystemPort.createDirectory(basePath.resolve("core/application/service"));
                                fileSystemPort.createDirectory(basePath.resolve("core/application/port"));

                                // Infrastructure layer
                                fileSystemPort.createDirectory(basePath.resolve("infrastructure/entry-points"));
                                fileSystemPort.createDirectory(basePath.resolve("infrastructure/driven-adapters"));
                                fileSystemPort.createDirectory(basePath.resolve("infrastructure/config"));

                                // Add .gitkeep files
                                files.add(GeneratedFile.create(
                                                basePath.resolve("core/domain/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("core/application/service/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("core/application/port/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("infrastructure/adapter/.gitkeep"),
                                                ""));
                                files.add(GeneratedFile.create(
                                                basePath.resolve("infrastructure/config/.gitkeep"),
                                                ""));
                        }
                }

                return files;
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
}
