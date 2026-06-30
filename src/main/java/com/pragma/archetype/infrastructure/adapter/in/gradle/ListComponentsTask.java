package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;

/**
 * Gradle task to list all generated components in the project.
 * Scans the project structure to identify adapters, entities, and use cases.
 *
 * Usage: ./gradlew listComponents
 */
public class ListComponentsTask extends DefaultTask {

  public ListComponentsTask() {
    setGroup("Clean Architecture");
    setDescription("List all generated components (adapters, entities, use cases)");
  }

  @TaskAction
  public void listComponents() {
    Path projectPath = getProject().getProjectDir().toPath();

    getLogger().lifecycle("");
    getLogger().lifecycle("╔══════════════════════════════════════════════════╗");
    getLogger().lifecycle("║         Clean Architecture Components           ║");
    getLogger().lifecycle("╚══════════════════════════════════════════════════╝");
    getLogger().lifecycle("");

    // Load project configuration
    ConfigurationPort configPort = new YamlConfigurationAdapter();
    var configOpt = configPort.readConfiguration(projectPath);

    if (configOpt.isEmpty()) {
      getLogger().error("✗ No .cleanarch.yml found. Run 'initCleanArch' first.");
      return;
    }

    ProjectConfig config = configOpt.get();

    // Print project info
    getLogger().lifecycle("  Project: {}", config.name());
    getLogger().lifecycle("  Package: {}", config.basePackage());
    getLogger().lifecycle("  Architecture: {}", config.architecture().name().toLowerCase().replace('_', '-'));
    getLogger().lifecycle("  Framework: {}", config.framework().name().toLowerCase());
    getLogger().lifecycle("  Paradigm: {}", config.paradigm().name().toLowerCase());
    getLogger().lifecycle("");

    // Determine paths based on architecture
    String srcBase = resolveSrcBase(config);
    String basePackagePath = config.basePackage().replace('.', '/');

    // Find driven adapters
    String drivenBase = srcBase + "/" + basePackagePath + "/infrastructure/drivenadapters";
    if (!Files.exists(projectPath.resolve(drivenBase))) {
      // Try alternative naming convention
      drivenBase = srcBase + "/" + basePackagePath + "/infrastructure/driven";
    }
    listAdaptersInDirectory(projectPath, drivenBase, "Driven Adapters (Output)");

    // Find entry points
    String drivingBase = srcBase + "/" + basePackagePath + "/infrastructure/entrypoints";
    if (!Files.exists(projectPath.resolve(drivingBase))) {
      drivingBase = srcBase + "/" + basePackagePath + "/infrastructure/entry";
    }
    listAdaptersInDirectory(projectPath, drivingBase, "Entry Points (Input)");

    // Find domain entities
    String modelPath = srcBase + "/" + basePackagePath + "/domain/model";
    listJavaComponents(projectPath, modelPath, "Domain Entities", "");

    // Find use cases
    String usecasesPath = srcBase + "/" + basePackagePath + "/domain/usecases";
    listJavaComponents(projectPath, usecasesPath, "Use Cases", "");

    getLogger().lifecycle("");
    getLogger().lifecycle("─────────────────────────────────────────────────");
  }

  /**
   * Lists adapter directories found under a base path.
   */
  private void listAdaptersInDirectory(Path projectPath, String relativePath, String sectionName) {
    Path baseDir = projectPath.resolve(relativePath);

    getLogger().lifecycle("  ┌─ {} ─────────────────────────────", sectionName);

    if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
      getLogger().lifecycle("  │  (none)");
      getLogger().lifecycle("  └──────────────────────────────────────");
      getLogger().lifecycle("");
      return;
    }

    List<String> adapters = listSubDirectories(baseDir);

    if (adapters.isEmpty()) {
      getLogger().lifecycle("  │  (none)");
    } else {
      for (String adapter : adapters) {
        Path adapterDir = baseDir.resolve(adapter);
        long javaFiles = countJavaFiles(adapterDir);
        getLogger().lifecycle("  │  • {} ({} files)", adapter, javaFiles);
      }
    }

    getLogger().lifecycle("  └──────────────────────────────────────");
    getLogger().lifecycle("");
  }

  /**
   * Lists Java components in a directory.
   */
  private void listJavaComponents(Path projectPath, String relativePath, String sectionName, String suffix) {
    Path dir = projectPath.resolve(relativePath);
    getLogger().lifecycle("  ┌─ {} ──────────────────────────", sectionName);

    if (!Files.exists(dir)) {
      getLogger().lifecycle("  │  (none)");
      getLogger().lifecycle("  └──────────────────────────────────────");
      getLogger().lifecycle("");
      return;
    }

    List<String> files = listJavaFilesWithSuffix(dir, suffix);
    if (files.isEmpty()) {
      getLogger().lifecycle("  │  (none)");
    } else {
      for (String file : files) {
        getLogger().lifecycle("  │  • {}", file);
      }
    }

    getLogger().lifecycle("  └──────────────────────────────────────");
    getLogger().lifecycle("");
  }

  /**
   * Resolves the source base directory based on architecture type.
   */
  private String resolveSrcBase(ProjectConfig config) {
    String archName = config.architecture().name();
    if (archName.contains("MULTI_GRANULAR")) {
      return "domain/model/src/main/java";
    } else if (archName.contains("MULTI")) {
      return "domain/src/main/java";
    }
    return "src/main/java";
  }

  // Helper methods

  private List<String> listSubDirectories(Path dir) {
    List<String> dirs = new ArrayList<>();
    try (Stream<Path> stream = Files.list(dir)) {
      stream.filter(Files::isDirectory)
          .map(p -> p.getFileName().toString())
          .sorted()
          .forEach(dirs::add);
    } catch (IOException e) {
      // ignore
    }
    return dirs;
  }

  private long countJavaFiles(Path dir) {
    try (Stream<Path> stream = Files.walk(dir)) {
      return stream.filter(p -> p.toString().endsWith(".java")).count();
    } catch (IOException e) {
      return 0;
    }
  }

  private List<String> listJavaFilesWithSuffix(Path dir, String suffix) {
    List<String> files = new ArrayList<>();
    try (Stream<Path> stream = Files.walk(dir, 1)) {
      stream.filter(p -> p.toString().endsWith(".java"))
          .map(p -> p.getFileName().toString().replace(".java", ""))
          .filter(name -> suffix.isEmpty() || name.contains(suffix))
          .sorted()
          .forEach(files::add);
    } catch (IOException e) {
      // ignore
    }
    return files;
  }
}
