package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.port.out.TemplateRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateAdapterDiscovery Tests")
class TemplateAdapterDiscoveryTest {

  @Mock
  private TemplateRepository templateRepository;

  @Mock
  private AdapterMetadataLoader metadataLoader;

  private TemplateAdapterDiscovery discovery;

  @BeforeEach
  void setUp() {
    discovery = new TemplateAdapterDiscovery(templateRepository, metadataLoader);
  }

  @Test
  @DisplayName("Should discover input adapters")
  void shouldDiscoverInputAdapters() {
    // Given
    AdapterMetadata restMetadata = AdapterMetadata.builder()
        .name("rest")
        .direction(AdapterDirection.INPUT)
        .displayName("REST API")
        .description("REST endpoints")
        .build();

    AdapterMetadata graphqlMetadata = AdapterMetadata.builder()
        .name("graphql")
        .direction(AdapterDirection.INPUT)
        .displayName("GraphQL API")
        .description("GraphQL endpoints")
        .build();

    when(templateRepository.templateExists("adapters/rest/metadata.yml")).thenReturn(true);
    when(templateRepository.templateExists("adapters/graphql/metadata.yml")).thenReturn(true);
    when(metadataLoader.loadMetadata("adapters/rest/metadata.yml")).thenReturn(restMetadata);
    when(metadataLoader.loadMetadata("adapters/graphql/metadata.yml")).thenReturn(graphqlMetadata);

    // When
    List<AdapterMetadata> adapters = discovery.discoverAdapters(AdapterDirection.INPUT);

    // Then
    assertTrue(adapters.size() >= 2);
    assertTrue(adapters.stream().anyMatch(a -> "rest".equals(a.getName())));
    assertTrue(adapters.stream().anyMatch(a -> "graphql".equals(a.getName())));
  }

  @Test
  @DisplayName("Should discover output adapters")
  void shouldDiscoverOutputAdapters() {
    // Given
    AdapterMetadata mongoMetadata = AdapterMetadata.builder()
        .name("mongodb")
        .direction(AdapterDirection.OUTPUT)
        .displayName("MongoDB")
        .description("MongoDB database")
        .build();

    when(templateRepository.templateExists("adapters/mongodb/metadata.yml")).thenReturn(true);
    when(metadataLoader.loadMetadata("adapters/mongodb/metadata.yml")).thenReturn(mongoMetadata);

    // When
    List<AdapterMetadata> adapters = discovery.discoverAdapters(AdapterDirection.OUTPUT);

    // Then
    assertTrue(adapters.stream().anyMatch(a -> "mongodb".equals(a.getName())));
  }

  @Test
  @DisplayName("Should filter adapters by direction")
  void shouldFilterAdaptersByDirection() {
    // Given - rest is INPUT, mongodb is OUTPUT
    AdapterMetadata restMetadata = AdapterMetadata.builder()
        .name("rest")
        .direction(AdapterDirection.INPUT)
        .displayName("REST API")
        .description("REST endpoints")
        .build();

    AdapterMetadata mongoMetadata = AdapterMetadata.builder()
        .name("mongodb")
        .direction(AdapterDirection.OUTPUT)
        .displayName("MongoDB")
        .description("MongoDB database")
        .build();

    when(templateRepository.templateExists(anyString())).thenReturn(true);
    when(metadataLoader.loadMetadata("adapters/rest/metadata.yml")).thenReturn(restMetadata);
    when(metadataLoader.loadMetadata("adapters/mongodb/metadata.yml")).thenReturn(mongoMetadata);

    // When
    List<AdapterMetadata> inputAdapters = discovery.discoverAdapters(AdapterDirection.INPUT);
    List<AdapterMetadata> outputAdapters = discovery.discoverAdapters(AdapterDirection.OUTPUT);

    // Then
    assertTrue(inputAdapters.stream().anyMatch(a -> "rest".equals(a.getName())));
    assertFalse(inputAdapters.stream().anyMatch(a -> "mongodb".equals(a.getName())));

    assertTrue(outputAdapters.stream().anyMatch(a -> "mongodb".equals(a.getName())));
    assertFalse(outputAdapters.stream().anyMatch(a -> "rest".equals(a.getName())));
  }

  @Test
  @DisplayName("Should find adapter by name and direction")
  void shouldFindAdapterByNameAndDirection() {
    // Given
    AdapterMetadata restMetadata = AdapterMetadata.builder()
        .name("rest")
        .direction(AdapterDirection.INPUT)
        .displayName("REST API")
        .description("REST endpoints")
        .build();

    when(templateRepository.templateExists("adapters/rest/metadata.yml")).thenReturn(true);
    when(metadataLoader.loadMetadata("adapters/rest/metadata.yml")).thenReturn(restMetadata);

    // When
    Optional<AdapterMetadata> result = discovery.findAdapter("rest", AdapterDirection.INPUT);

    // Then
    assertTrue(result.isPresent());
    assertEquals("rest", result.get().getName());
    assertEquals(AdapterDirection.INPUT, result.get().getDirection());
  }

  @Test
  @DisplayName("Should return empty when adapter not found")
  void shouldReturnEmptyWhenAdapterNotFound() {
    // Given
    when(templateRepository.templateExists("adapters/nonexistent/metadata.yml")).thenReturn(false);

    // When
    Optional<AdapterMetadata> result = discovery.findAdapter("nonexistent", AdapterDirection.INPUT);

    // Then
    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Should return empty when adapter has wrong direction")
  void shouldReturnEmptyWhenAdapterHasWrongDirection() {
    // Given - rest is INPUT but we're looking for OUTPUT
    AdapterMetadata restMetadata = AdapterMetadata.builder()
        .name("rest")
        .direction(AdapterDirection.INPUT)
        .displayName("REST API")
        .description("REST endpoints")
        .build();

    when(templateRepository.templateExists("adapters/rest/metadata.yml")).thenReturn(true);
    when(metadataLoader.loadMetadata("adapters/rest/metadata.yml")).thenReturn(restMetadata);

    // When
    Optional<AdapterMetadata> result = discovery.findAdapter("rest", AdapterDirection.OUTPUT);

    // Then
    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Should check if adapter exists")
  void shouldCheckIfAdapterExists() {
    // Given
    AdapterMetadata restMetadata = AdapterMetadata.builder()
        .name("rest")
        .direction(AdapterDirection.INPUT)
        .displayName("REST API")
        .description("REST endpoints")
        .build();

    when(templateRepository.templateExists("adapters/rest/metadata.yml")).thenReturn(true);
    when(metadataLoader.loadMetadata("adapters/rest/metadata.yml")).thenReturn(restMetadata);

    // When & Then
    assertTrue(discovery.adapterExists("rest", AdapterDirection.INPUT));
    assertFalse(discovery.adapterExists("rest", AdapterDirection.OUTPUT));
    assertFalse(discovery.adapterExists("nonexistent", AdapterDirection.INPUT));
  }

  @Test
  @DisplayName("Should list adapter names")
  void shouldListAdapterNames() {
    // Given
    AdapterMetadata restMetadata = AdapterMetadata.builder()
        .name("rest")
        .direction(AdapterDirection.INPUT)
        .displayName("REST API")
        .description("REST endpoints")
        .build();

    AdapterMetadata graphqlMetadata = AdapterMetadata.builder()
        .name("graphql")
        .direction(AdapterDirection.INPUT)
        .displayName("GraphQL API")
        .description("GraphQL endpoints")
        .build();

    when(templateRepository.templateExists("adapters/rest/metadata.yml")).thenReturn(true);
    when(templateRepository.templateExists("adapters/graphql/metadata.yml")).thenReturn(true);
    when(metadataLoader.loadMetadata("adapters/rest/metadata.yml")).thenReturn(restMetadata);
    when(metadataLoader.loadMetadata("adapters/graphql/metadata.yml")).thenReturn(graphqlMetadata);

    // When
    List<String> names = discovery.listAdapterNames(AdapterDirection.INPUT);

    // Then
    assertTrue(names.contains("rest"));
    assertTrue(names.contains("graphql"));
    // Names should be sorted
    assertEquals(names, names.stream().sorted().toList());
  }

  @Test
  @DisplayName("Should handle errors gracefully during discovery")
  void shouldHandleErrorsGracefullyDuringDiscovery() {
    // Given - one adapter throws exception
    when(templateRepository.templateExists("adapters/rest/metadata.yml")).thenReturn(true);
    when(metadataLoader.loadMetadata("adapters/rest/metadata.yml"))
        .thenThrow(new RuntimeException("Failed to load"));

    // When - should not throw, just skip the failed adapter
    List<AdapterMetadata> adapters = discovery.discoverAdapters(AdapterDirection.INPUT);

    // Then - should return empty or partial list, not throw
    assertTrue(adapters != null);
  }
}
