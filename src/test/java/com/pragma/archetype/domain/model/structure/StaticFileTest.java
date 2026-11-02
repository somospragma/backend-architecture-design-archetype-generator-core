package com.pragma.archetype.domain.model.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StaticFileTest {

  @Test
  void shouldResolveRelativeSourceInsideTheArchitectureFolder() {
    var file = StaticFile.builder().source("project/Dockerfile.ftl").destination("Dockerfile").build();

    assertEquals("architectures/onion-single/project/Dockerfile.ftl",
        file.resolveSource("onion-single", "spring", "reactive"));
  }

  @Test
  void shouldResolveSharedSourceFromTheTemplatesRoot() {
    var file = StaticFile.builder()
        .source("/common/tests/architecture/DomainPurityTest.java.ftl")
        .destination("src/test/java/{basePackage}/architecture/DomainPurityTest.java")
        .build();

    assertEquals("common/tests/architecture/DomainPurityTest.java.ftl",
        file.resolveSource("hexagonal-single", "spring", "imperative"));
  }

  @Test
  void shouldExpandFrameworkAndParadigmPlaceholders() {
    var file = StaticFile.builder()
        .source("/frameworks/{framework}/{paradigm}/tests/architecture/ParadigmRulesTest.java.ftl")
        .destination("src/test/java/{basePackage}/architecture/ParadigmRulesTest.java")
        .build();

    assertEquals("frameworks/spring/reactive/tests/architecture/ParadigmRulesTest.java.ftl",
        file.resolveSource("hexagonal-multi", "spring", "reactive"));
  }
}
