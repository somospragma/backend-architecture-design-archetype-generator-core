package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.logging.Logger;

import com.pragma.archetype.domain.model.config.TemplateConfig;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.template.FreemarkerTemplateRepository;

/**
 * Factory for creating TemplateRepository instances.
 * Centralizes the template resolution logic used by all Gradle tasks.
 *
 * Resolution order:
 * 1. .cleanarch.yml with localPath → local filesystem
 * 2. .cleanarch.yml with repository/branch → remote GitHub with custom config
 * 3. No .cleanarch.yml or no templates section → default GitHub repo (main branch)
 */
public final class TemplateRepositoryFactory {

  private TemplateRepositoryFactory() {
    // Utility class
  }

  /**
   * Creates a TemplateRepository based on project configuration.
   *
   * @param projectDir the project directory path
   * @param logger     Gradle logger for output
   * @return configured TemplateRepository
   */
  public static TemplateRepository create(Path projectDir, Logger logger) {
    YamlConfigurationAdapter configAdapter = new YamlConfigurationAdapter();

    // Read template configuration (returns defaultConfig if no .cleanarch.yml or no templates section)
    TemplateConfig templateConfig = configAdapter.readTemplateConfiguration(projectDir);

    if (templateConfig.isLocalMode()) {
      Path localPath = Path.of(templateConfig.localPath());
      if (Files.exists(localPath)) {
        logger.lifecycle("✓ Using local templates: {}", localPath.toAbsolutePath());
        return new FreemarkerTemplateRepository(localPath);
      } else {
        throw new RuntimeException("Template localPath does not exist: " + localPath);
      }
    }

    // Remote mode — download from GitHub repository
    logger.lifecycle("✓ Using remote templates from: {}", templateConfig.repository());
    logger.lifecycle("  Branch: {}", templateConfig.getEffectiveBranch());
    return new FreemarkerTemplateRepository(templateConfig);
  }
}
