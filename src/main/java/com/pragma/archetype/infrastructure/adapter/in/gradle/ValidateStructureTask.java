package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.structure.StaticFile;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.template.FreemarkerTemplateRepository;

/**
 * Gradle task for validating project architecture structure.
 * Copies ArchUnit tests from templates and runs architecture tests.
 * 
 * This ensures that architecture tests are always up-to-date from templates
 * and prevents developers from modifying or removing architecture validation
 * rules.
 * 
 * Usage:
 * ./gradlew validateStructure
 */
public class ValidateStructureTask extends DefaultTask {

  @TaskAction
  public void validateStructure() {
    Path projectPath = Paths.get(getProject().getProjectDir().getAbsolutePath());

    try {
      getLogger().lifecycle("Validating architecture structure...");

      // 1. Read project configuration
      ProjectConfig config = readProjectConfig(projectPath);
      if (config == null) {
        throw new org.gradle.api.GradleException(
            "Could not read project configuration from .cleanarch.yml");
      }

      String architecture = config.architecture().getValue();
      String basePackage = config.basePackage();

      getLogger().lifecycle("Architecture: {}", architecture);
      getLogger().lifecycle("Base package: {}", basePackage);

      // 2. Setup template repository
      FreemarkerTemplateRepository templateRepository = createTemplateRepository(projectPath);

      // 3. Load structure metadata
      ArchitectureType archType = ArchitectureType.fromValue(architecture);
      StructureMetadata metadata = templateRepository.loadStructureMetadata(archType);

      // 4. Copy ArchUnit tests from templates
      copyArchUnitTests(templateRepository, metadata, projectPath, basePackage);

      // 5. Run architecture tests
      runArchitectureTests();

      getLogger().lifecycle("✓ Architecture validation completed successfully");

    } catch (org.gradle.api.GradleException e) {
      throw e;
    } catch (Exception e) {
      getLogger().error("Failed to validate structure: {}", e.getMessage(), e);
      throw new org.gradle.api.GradleException(
          "Architecture validation failed: " + e.getMessage(), e);
    }
  }

  /**
   * Reads project configuration from .cleanarch.yml
   */
  private ProjectConfig readProjectConfig(Path projectPath) {
    try {
      var configAdapter = new YamlConfigurationAdapter();
      var configOpt = configAdapter.readConfiguration(projectPath);
      return configOpt.orElse(null);
    } catch (Exception e) {
      getLogger().error("Error reading configuration: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Creates template repository.
   * Reads configuration from .cleanarch.yml or uses auto-detection.
   * Falls back to embedded templates if available.
   */
  private FreemarkerTemplateRepository createTemplateRepository(Path projectPath) {
    YamlConfigurationAdapter configAdapter = new YamlConfigurationAdapter();

    // 1. Check for .cleanarch.yml configuration (highest priority)
    if (configAdapter.configurationExists(projectPath)) {
      var templateConfig = configAdapter.readTemplateConfiguration(projectPath);

      if (templateConfig.isLocalMode()) {
        Path localPath = Path.of(templateConfig.localPath());
        if (Files.exists(localPath)) {
          getLogger().lifecycle("✓ Using local templates from .cleanarch.yml: {}", localPath.toAbsolutePath());
          return new FreemarkerTemplateRepository(localPath);
        } else {
          getLogger().error("✗ Template localPath in .cleanarch.yml does not exist: {}", localPath);
          throw new org.gradle.api.GradleException("Template path not found: " + localPath);
        }
      }
    }

    // 2. Try to auto-detect local templates (for plugin development only)
    Path autoDetectPath = projectPath
        .resolve("../../backend-architecture-design-archetype-generator-templates/templates")
        .normalize();

    if (Files.exists(autoDetectPath)) {
      getLogger().lifecycle("✓ Auto-detected local templates (development mode): {}", autoDetectPath.toAbsolutePath());
      return new FreemarkerTemplateRepository(autoDetectPath);
    }

    // 3. Fall back to embedded templates (in JAR) - production mode
    getLogger().lifecycle("✓ Using embedded templates from plugin JAR (production mode)");
    return new FreemarkerTemplateRepository("embedded");
  }

  /**
   * Copies ArchUnit tests from templates to project
   */
  private void copyArchUnitTests(
      TemplateRepository templateRepository,
      StructureMetadata metadata,
      Path projectPath,
      String basePackage) throws Exception {

    getLogger().lifecycle("Copying ArchUnit tests from templates...");

    // Find architecture test files in staticFiles
    List<StaticFile> archTests = metadata.getStaticFiles().stream()
        .filter(file -> file.getDestination().contains("/architecture/")
            && file.getDestination().endsWith("Test.java"))
        .toList();

    if (archTests.isEmpty()) {
      getLogger().warn("No ArchUnit tests found in structure metadata");
      return;
    }

    int copiedCount = 0;
    Map<String, Object> context = new HashMap<>();
    context.put("basePackage", basePackage);

    for (StaticFile testFile : archTests) {
      String source = "architectures/" + metadata.architectureType() + "/" + testFile.getSource();
      String destination = testFile.resolveDestination(basePackage);

      Path targetPath = projectPath.resolve(destination);

      // Create parent directories
      Files.createDirectories(targetPath.getParent());

      // Process template and write file
      String content = templateRepository.processTemplate(source, context);
      Files.writeString(targetPath, content);

      copiedCount++;
      getLogger().lifecycle("  ✓ Copied: {}", targetPath.getFileName());
    }

    getLogger().lifecycle("✓ Copied {} ArchUnit test(s)", copiedCount);
  }

  /**
   * Runs architecture tests using Gradle test task
   */
  private void runArchitectureTests() throws Exception {
    getLogger().lifecycle("Running architecture tests...");

    // Execute test task with filter for architecture tests
    getProject().exec(execSpec -> {
      execSpec.setWorkingDir(getProject().getProjectDir());

      // Use gradlew if available, otherwise gradle
      File gradlew = new File(getProject().getProjectDir(), "gradlew");
      String executable = gradlew.exists() && gradlew.canExecute() ? "./gradlew" : "gradle";

      execSpec.commandLine(
          executable,
          "test",
          "--tests", "*architecture*",
          "--rerun-tasks");
    });
  }
}
