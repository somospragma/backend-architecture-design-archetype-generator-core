package com.pragma.archetype.domain.model.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TemplateMappingTest {

  @Test
  void shouldCreateTemplateMappingWithBuilder() {
    var mapping = TemplateMapping.builder()
        .source("Controller.java.ftl")
        .target("{adapterName}Controller.java")
        .outputDir("src/main/java")
        .build();

    assertEquals("Controller.java.ftl", mapping.getSource());
    assertEquals("{adapterName}Controller.java", mapping.getTarget());
    assertEquals("src/main/java", mapping.getOutputDir());
  }

  @Test
  void shouldUseDefaultOutputDirWhenNotProvided() {
    var mapping = TemplateMapping.builder()
        .source("Controller.java.ftl")
        .target("{adapterName}Controller.java")
        .build();

    assertEquals("src/main/java", mapping.getOutputDir());
  }

  @Test
  void shouldResolveTargetWithAdapterName() {
    var mapping = TemplateMapping.builder()
        .source("Controller.java.ftl")
        .target("{adapterName}Controller.java")
        .build();

    var resolved = mapping.resolveTarget("User");

    assertEquals("UserController.java", resolved);
  }

  @Test
  void shouldResolveTargetWithAllPlaceholders() {
    var mapping = TemplateMapping.builder()
        .source("Repository.java.ftl")
        .target("{adapterName}{entityName}Repository.java")
        .build();

    var resolved = mapping.resolveTarget("Jpa", "User", null);

    assertEquals("JpaUserRepository.java", resolved);
  }

  @Test
  void shouldHandleNullPlaceholdersGracefully() {
    var mapping = TemplateMapping.builder()
        .source("Adapter.java.ftl")
        .target("{adapterName}Adapter.java")
        .build();

    var resolved = mapping.resolveTarget("Cache", null, null);

    assertEquals("CacheAdapter.java", resolved);
  }
}
