package com.pragma.archetype.domain.model.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class TemplatePathsTest {

  @Test
  void shouldCreateTemplatePathsWithDefaults() {
    var templatePaths = TemplatePaths.builder().build();

    assertNotNull(templatePaths.getInputAdapter());
    assertNotNull(templatePaths.getOutputAdapter());
    assertNotNull(templatePaths.getEntity());
    assertNotNull(templatePaths.getUseCase());
    assertNotNull(templatePaths.getConfig());
  }

  @Test
  void shouldResolveInputAdapterPath() {
    var templatePaths = TemplatePaths.builder()
        .inputAdapter("frameworks/{framework}/{paradigm}/adapters/{adapterDir}/{type}")
        .build();

    var resolved = templatePaths.resolveInputAdapterPath(
        "spring",
        "reactive",
        "hexagonal-single",
        "rest",
        "entry-points");

    assertEquals("frameworks/spring/reactive/adapters/entry-points/rest", resolved);
  }

  @Test
  void shouldResolveOutputAdapterPath() {
    var templatePaths = TemplatePaths.builder()
        .outputAdapter("frameworks/{framework}/{paradigm}/adapters/{adapterDir}/{type}")
        .build();

    var resolved = templatePaths.resolveOutputAdapterPath(
        "spring",
        "imperative",
        "hexagonal-single",
        "postgresql",
        "driven-adapters");

    assertEquals("frameworks/spring/imperative/adapters/driven-adapters/postgresql", resolved);
  }

  @Test
  void shouldResolveEntityPath() {
    var templatePaths = TemplatePaths.builder()
        .entity("architectures/{architecture}/domain/entity")
        .build();

    var resolved = templatePaths.resolveEntityPath("hexagonal-single");

    assertEquals("architectures/hexagonal-single/domain/entity", resolved);
  }

  @Test
  void shouldResolveUseCasePath() {
    var templatePaths = TemplatePaths.builder()
        .useCase("architectures/{architecture}/application/usecase")
        .build();

    var resolved = templatePaths.resolveUseCasePath("onion-single");

    assertEquals("architectures/onion-single/application/usecase", resolved);
  }

  @Test
  void shouldResolveConfigPath() {
    var templatePaths = TemplatePaths.builder()
        .config("architectures/{architecture}/project")
        .build();

    var resolved = templatePaths.resolveConfigPath("hexagonal-multi");

    assertEquals("architectures/hexagonal-multi/project", resolved);
  }

  @Test
  void shouldHandleCaseInsensitiveVariables() {
    var templatePaths = TemplatePaths.builder()
        .inputAdapter("frameworks/{framework}/{paradigm}/adapters/{type}")
        .build();

    var resolved = templatePaths.resolveInputAdapterPath(
        "SPRING",
        "REACTIVE",
        "HEXAGONAL-SINGLE",
        "REST",
        "entry-points");

    assertEquals("frameworks/spring/reactive/adapters/rest", resolved);
  }

  @Test
  void shouldAllowCustomTemplatePaths() {
    var templatePaths = TemplatePaths.builder()
        .inputAdapter("custom/{framework}/input/{type}")
        .outputAdapter("custom/{framework}/output/{type}")
        .build();

    var inputResolved = templatePaths.resolveInputAdapterPath(
        "spring", "reactive", "hexagonal", "rest", "inputs");
    var outputResolved = templatePaths.resolveOutputAdapterPath(
        "spring", "imperative", "hexagonal", "redis", "outputs");

    assertEquals("custom/spring/input/rest", inputResolved);
    assertEquals("custom/spring/output/redis", outputResolved);
  }
}
