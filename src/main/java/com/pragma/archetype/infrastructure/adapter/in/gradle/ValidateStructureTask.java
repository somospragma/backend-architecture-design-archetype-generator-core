package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

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

  @Inject
  protected ExecOperations getExecOperations() {
    throw new UnsupportedOperationException();
  }

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
      copyArchUnitTests(templateRepository, metadata, projectPath, config);

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
   * Delegates to TemplateRepositoryFactory for centralized resolution logic.
   */
  private FreemarkerTemplateRepository createTemplateRepository(Path projectPath) {
    TemplateRepository repo = TemplateRepositoryFactory.create(projectPath, getLogger());
    if (repo instanceof FreemarkerTemplateRepository freemarkerRepo) {
      return freemarkerRepo;
    }
    throw new org.gradle.api.GradleException("Unexpected template repository type");
  }

  /**
   * Copies ArchUnit tests from templates to project
   */
  private void copyArchUnitTests(
      TemplateRepository templateRepository,
      StructureMetadata metadata,
      Path projectPath,
      ProjectConfig config) throws Exception {

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
    String basePackage = config.basePackage();
    context.put("basePackage", basePackage);
    // Same paradigm flags initCleanArch passes, so rules can differ by paradigm
    String paradigm = config.paradigm().name().toLowerCase();
    context.put("paradigm", paradigm);
    context.put("isReactive", "reactive".equals(paradigm));
    context.put("isImperative", "imperative".equals(paradigm));
    context.put("framework", config.framework().name().toLowerCase());

    for (StaticFile testFile : archTests) {
      String source = testFile.resolveSource(metadata.architectureType(),
          config.framework().name().toLowerCase(), paradigm);
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
    getExecOperations().exec(execSpec -> {
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
