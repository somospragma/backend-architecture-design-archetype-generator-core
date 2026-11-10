package com.pragma.archetype.test;

import java.util.List;
import java.util.Map;

import com.pragma.archetype.domain.model.structure.LayerDependencies;
import com.pragma.archetype.domain.model.structure.ModuleMetadata;
import com.pragma.archetype.domain.model.structure.NamingConventions;
import com.pragma.archetype.domain.model.structure.PackageMappings;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.model.structure.TemplatePaths;

/**
 * Helper class for creating StructureMetadata instances in tests.
 * Simplifies test code by providing convenient factory methods.
 */
public class StructureMetadataTestHelper {

  private static final String DEFAULT_RESOURCES_PATH = "src/main/resources";

  /**
   * Creates a minimal StructureMetadata with only architecture type and adapter
   * paths.
   */
  public static StructureMetadata createMinimal(String architectureType, Map<String, String> adapterPaths) {
    return new StructureMetadata(
        architectureType,
        DEFAULT_RESOURCES_PATH,
        adapterPaths,
        null, // templatePaths
        null, // packageMappings
        null, // namingConventions
        null, // layerDependencies
        null, // packages
        null, // moduleMetadata
        null, // entityModule
        null, // useCaseModule
        null, // staticFiles
        null, // sourceDirectory
        null // testDirectory
    );
  }

  /**
   * Creates a StructureMetadata with template paths.
   */
  public static StructureMetadata withTemplatePaths(
      String architectureType,
      Map<String, String> adapterPaths,
      TemplatePaths templatePaths) {
    return new StructureMetadata(
        architectureType,
        DEFAULT_RESOURCES_PATH,
        adapterPaths,
        templatePaths,
        null, // packageMappings
        null, // namingConventions
        null, // layerDependencies
        null, // packages
        null, // moduleMetadata
        null, // entityModule
        null, // useCaseModule
        null, // staticFiles
        null, // sourceDirectory
        null // testDirectory
    );
  }

  /**
   * Creates a StructureMetadata with layer dependencies.
   */
  public static StructureMetadata withLayerDependencies(
      String architectureType,
      Map<String, String> adapterPaths,
      LayerDependencies layerDependencies) {
    return new StructureMetadata(
        architectureType,
        DEFAULT_RESOURCES_PATH,
        adapterPaths,
        null, // templatePaths
        null, // packageMappings
        null, // namingConventions
        layerDependencies,
        null, // packages
        null, // moduleMetadata
        null, // entityModule
        null, // useCaseModule
        null, // staticFiles
        null, // sourceDirectory
        null // testDirectory
    );
  }

  /**
   * Creates a StructureMetadata with module metadata.
   */
  public static StructureMetadata withModules(
      String architectureType,
      Map<String, String> adapterPaths,
      List<ModuleMetadata> moduleMetadata) {
    return new StructureMetadata(
        architectureType,
        DEFAULT_RESOURCES_PATH,
        adapterPaths,
        null, // templatePaths
        null, // packageMappings
        null, // namingConventions
        null, // layerDependencies
        null, // packages
        moduleMetadata,
        null, // entityModule
        null, // useCaseModule
        null, // staticFiles
        null, // sourceDirectory
        null // testDirectory
    );
  }

  /**
   * Creates a StructureMetadata with packages list.
   */
  public static StructureMetadata withPackages(
      String architectureType,
      Map<String, String> adapterPaths,
      List<String> packages) {
    return new StructureMetadata(
        architectureType,
        DEFAULT_RESOURCES_PATH,
        adapterPaths,
        null, // templatePaths
        null, // packageMappings
        null, // namingConventions
        null, // layerDependencies
        packages,
        null, // moduleMetadata
        null, // entityModule
        null, // useCaseModule
        null, // staticFiles
        null, // sourceDirectory
        null // testDirectory
    );
  }

  /**
   * Creates a full StructureMetadata with all parameters.
   */
  public static StructureMetadata createFull(
      String architectureType,
      String resourcesPath,
      Map<String, String> adapterPaths,
      TemplatePaths templatePaths,
      PackageMappings packageMappings,
      NamingConventions namingConventions,
      LayerDependencies layerDependencies,
      List<String> packages,
      List<ModuleMetadata> moduleMetadata,
      String entityModule,
      String useCaseModule) {
    return new StructureMetadata(
        architectureType,
        resourcesPath,
        adapterPaths,
        templatePaths,
        packageMappings,
        namingConventions,
        layerDependencies,
        packages,
        moduleMetadata,
        entityModule,
        useCaseModule,
        null, // staticFiles
        null, // sourceDirectory
        null // testDirectory
    );
  }
}
