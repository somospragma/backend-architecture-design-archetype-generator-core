package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.logging.Logger;

import com.pragma.archetype.domain.model.config.PluginVersion;
import com.pragma.archetype.domain.model.config.TemplateConfig;
import com.pragma.archetype.domain.model.template.TemplatesContract;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.http.OkHttpClientAdapter;
import com.pragma.archetype.infrastructure.adapter.out.template.FreemarkerTemplateRepository;
import com.pragma.archetype.infrastructure.adapter.out.template.GitHubTemplateTagResolver;

/**
 * Factory for creating TemplateRepository instances.
 * Centralizes the template resolution logic used by all Gradle tasks.
 *
 * Resolution order:
 * 1. .cleanarch.yml with localPath → local filesystem
 * 2. .cleanarch.yml with repository and templates.version (a tag) or
 *    templates.branch → that ref of the remote repository
 * 3. Otherwise → the latest release tag v{major}.x.y of this plugin's major
 *    in the configured (or default) GitHub repository
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
    if (templateConfig.needsLatestTag()) {
      int major = TemplatesContract.majorOf(PluginVersion.current()).getAsInt();
      String tag = new GitHubTemplateTagResolver(new OkHttpClientAdapter())
          .latestTag(templateConfig.repository(), major);
      templateConfig = templateConfig.withVersion(tag);
    }
    logger.lifecycle("✓ Using remote templates from: {}", templateConfig.repository());
    logger.lifecycle("  Ref: {}", templateConfig.getEffectiveBranch());
    return new FreemarkerTemplateRepository(templateConfig);
  }
}
