package com.pragma.archetype.domain.model.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class AdapterMetadataTest {

  @Test
  void shouldCreateAdapterMetadataWithBuilder() {
    var metadata = AdapterMetadata.builder()
        .name("rest")
        .direction(AdapterDirection.INPUT)
        .displayName("REST API")
        .description("RESTful HTTP endpoints")
        .requiredParameters(List.of("useCase", "endpoints"))
        .optionalParameters(List.of("authentication"))
        .templates(List.of(
            TemplateMapping.builder()
                .source("Controller.java.ftl")
                .target("{adapterName}Controller.java")
                .build()))
        .build();

    assertEquals("rest", metadata.getName());
    assertEquals(AdapterDirection.INPUT, metadata.getDirection());
    assertEquals("REST API", metadata.getDisplayName());
    assertEquals("RESTful HTTP endpoints", metadata.getDescription());
    assertEquals(2, metadata.getRequiredParameters().size());
    assertEquals(1, metadata.getOptionalParameters().size());
    assertEquals(1, metadata.getTemplates().size());
  }

  @Test
  void shouldCheckIfParameterIsRequired() {
    var metadata = AdapterMetadata.builder()
        .name("rest")
        .direction(AdapterDirection.INPUT)
        .requiredParameters(List.of("useCase", "endpoints"))
        .build();

    assertTrue(metadata.isParameterRequired("useCase"));
    assertTrue(metadata.isParameterRequired("endpoints"));
    assertFalse(metadata.isParameterRequired("authentication"));
  }

  @Test
  void shouldCheckIfParameterIsOptional() {
    var metadata = AdapterMetadata.builder()
        .name("rest")
        .direction(AdapterDirection.INPUT)
        .optionalParameters(List.of("authentication", "cors"))
        .build();

    assertTrue(metadata.isParameterOptional("authentication"));
    assertTrue(metadata.isParameterOptional("cors"));
    assertFalse(metadata.isParameterOptional("useCase"));
  }

  @Test
  void shouldCheckIfHasRequiredParameters() {
    var withRequired = AdapterMetadata.builder()
        .name("rest")
        .direction(AdapterDirection.INPUT)
        .requiredParameters(List.of("useCase"))
        .build();

    var withoutRequired = AdapterMetadata.builder()
        .name("redis")
        .direction(AdapterDirection.OUTPUT)
        .requiredParameters(List.of())
        .build();

    assertTrue(withRequired.hasRequiredParameters());
    assertFalse(withoutRequired.hasRequiredParameters());
  }

  @Test
  void shouldGetFullIdentifier() {
    var inputAdapter = AdapterMetadata.builder()
        .name("rest")
        .direction(AdapterDirection.INPUT)
        .build();

    var outputAdapter = AdapterMetadata.builder()
        .name("postgresql")
        .direction(AdapterDirection.OUTPUT)
        .build();

    assertEquals("INPUT:rest", inputAdapter.getFullIdentifier());
    assertEquals("OUTPUT:postgresql", outputAdapter.getFullIdentifier());
  }

  @Test
  void shouldUseDefaultEmptyListsWhenNotProvided() {
    var metadata = AdapterMetadata.builder()
        .name("scheduled")
        .direction(AdapterDirection.INPUT)
        .build();

    assertNotNull(metadata.getRequiredParameters());
    assertNotNull(metadata.getOptionalParameters());
    assertNotNull(metadata.getTemplates());
    assertTrue(metadata.getRequiredParameters().isEmpty());
    assertTrue(metadata.getOptionalParameters().isEmpty());
    assertTrue(metadata.getTemplates().isEmpty());
  }
}
