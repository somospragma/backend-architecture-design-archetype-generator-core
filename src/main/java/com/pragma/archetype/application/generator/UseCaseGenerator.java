package com.pragma.archetype.application.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.model.usecase.UseCaseConfig;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Generates use case files (port interface and implementation).
 * Application service that orchestrates file generation.
 */
public class UseCaseGenerator {

  private final TemplateRepository templateRepository;
  private final StructureMetadata structureMetadata;

  public UseCaseGenerator(TemplateRepository templateRepository, StructureMetadata structureMetadata) {
    this.templateRepository = templateRepository;
    this.structureMetadata = structureMetadata;
  }

  /**
   * Generates use case files based on configuration.
   */
  public List<GeneratedFile> generate(Path projectPath, UseCaseConfig config) {
    List<GeneratedFile> generatedFiles = new ArrayList<>();

    // Prepare template data
    Map<String, Object> data = prepareTemplateData(config);

    // Generate port interface if requested
    if (config.generatePort()) {
      GeneratedFile portFile = generatePort(projectPath, config, data);
      generatedFiles.add(portFile);
    }

    // Generate implementation if requested
    if (config.generateImpl()) {
      GeneratedFile implFile = generateImplementation(projectPath, config, data);
      generatedFiles.add(implFile);
    }

    return generatedFiles;
  }

  private GeneratedFile generatePort(Path projectPath, UseCaseConfig config, Map<String, Object> data) {
    // Determine template path based on paradigm
    String paradigmPath = config.paradigm().name().toLowerCase();
    String templatePath = String.format("frameworks/spring/%s/usecase/InputPort.java.ftl", paradigmPath);

    // Process template with new structure
    String content = templateRepository.processTemplate(templatePath, data);

    // Calculate file path using module metadata for multi-module support
    String packagePath = config.packageName().replace('.', '/');
    
    // Resolve module path prefix for multi-module architectures
    String modulePath = resolveModulePath("ports");
    
    Path filePath = projectPath
        .resolve(modulePath)
        .resolve(structureMetadata.getSourceDirectory())
        .resolve(packagePath)
        .resolve("I" + config.name() + "UseCase.java");

    return GeneratedFile.javaSource(filePath, content);
  }

  private GeneratedFile generateImplementation(Path projectPath, UseCaseConfig config, Map<String, Object> data) {
    // Calculate implementation package: replace ports with usecases in the package name
    String implPackage = config.packageName()
        .replace("domain.ports", "domain.usecases")
        .replace("domain.port.in", "domain.usecases");
    
    // For multi-module, the package won't contain "domain." prefix, so handle "ports" → "usecases" directly
    if (!implPackage.equals(config.packageName())) {
      // Already replaced successfully
    } else {
      // Multi-module case: package might be like "com.pragma.ports" → "com.pragma.usecases"
      implPackage = config.packageName().replace(".ports", ".usecases");
    }

    // Update data with implementation package
    data.put("implPackage", implPackage);

    // Determine template path based on paradigm
    String paradigmPath = config.paradigm().name().toLowerCase();
    String templatePath = String.format("frameworks/spring/%s/usecase/UseCase.java.ftl", paradigmPath);

    // Process template with new structure
    String content = templateRepository.processTemplate(templatePath, data);

    // Calculate file path using module metadata for multi-module support
    String packagePath = implPackage.replace('.', '/');
    
    // Resolve module path prefix for multi-module architectures
    String modulePath = resolveModulePath("usecases");
    
    Path filePath = projectPath
        .resolve(modulePath)
        .resolve(structureMetadata.getSourceDirectory())
        .resolve(packagePath)
        .resolve(config.name() + "UseCase.java");

    return GeneratedFile.javaSource(filePath, content);
  }

  /**
   * Resolves the module filesystem path for a given package type.
   * For multi-module architectures, returns the module's path (e.g., "domain").
   * For single-module architectures, returns "." (current directory).
   */
  private String resolveModulePath(String packageType) {
    try {
      com.pragma.archetype.domain.model.structure.ModuleMetadata useCaseModule = structureMetadata.getUseCaseModule();
      if (useCaseModule != null && !".".equals(useCaseModule.getPath())) {
        return useCaseModule.getPath();
      }
    } catch (IllegalStateException e) {
      // useCaseModule not configured, fall back to root
    }
    return ".";
  }

  private Map<String, Object> prepareTemplateData(UseCaseConfig config) {
    Map<String, Object> data = new HashMap<>();
    data.put("useCaseName", config.name());
    data.put("packageName", config.packageName());

    // Convert methods to Maps for Freemarker
    List<Map<String, Object>> methodMaps = new ArrayList<>();
    for (UseCaseConfig.UseCaseMethod method : config.methods()) {
      Map<String, Object> methodMap = new HashMap<>();
      methodMap.put("name", method.name());
      methodMap.put("returnType", method.returnType());

      // Convert parameters to Maps
      List<Map<String, Object>> paramMaps = new ArrayList<>();
      if (method.parameters() != null) {
        for (UseCaseConfig.MethodParameter param : method.parameters()) {
          Map<String, Object> paramMap = new HashMap<>();
          paramMap.put("name", param.name());
          paramMap.put("type", param.type());
          paramMaps.add(paramMap);
        }
      }
      methodMap.put("parameters", paramMaps);
      methodMaps.add(methodMap);
    }
    data.put("methods", methodMaps);

    data.put("generatePort", config.generatePort());
    data.put("generateImpl", config.generateImpl());
    return data;
  }
}
