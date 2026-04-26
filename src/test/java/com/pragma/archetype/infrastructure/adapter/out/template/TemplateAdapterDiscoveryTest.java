package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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

  private static final String FRAMEWORK = "spring";
  private static final String PARADIGM = "reactive";
  private static final String INPUT_INDEX = "frameworks/spring/reactive/adapters/entry-points/index.json";
  private static final String OUTPUT_INDEX = "frameworks/spring/reactive/adapters/driven-adapters/index.json";

  @BeforeEach
  void setUp() {
    discovery = new TemplateAdapterDiscovery(templateRepository, metadataLoader);
  }

  private String inputIndexJson(String... adapterNames) {
    StringBuilder sb = new StringBuilder("{\"adapters\":[");
    for (int i = 0; i < adapterNames.length; i++) {
      if (i > 0) sb.append(",");
      sb.append("{\"name\":\"").append(adapterNames[i])
        .append("\",\"displayName\":\"").append(adapterNames[i])
        .append("\",\"description\":\"").append(adapterNames[i]).append(" adapter\"}");
    }
    sb.append("]}");
    return sb.toString();
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

    when(templateRepository.templateExists(INPUT_INDEX)).thenReturn(true);
    when(templateRepository.getTemplateContent(INPUT_INDEX))
        .thenReturn(inputIndexJson("rest", "graphql"));
    when(metadataLoader.loadMetadata("frameworks/spring/reactive/adapters/entry-points/rest/metadata.yml"))
        .thenReturn(restMetadata);
    when(metadataLoader.loadMetadata("frameworks/spring/reactive/adapters/entry-points/graphql/metadata.yml"))
        .thenReturn(graphqlMetadata);

    // When
    List<AdapterMetadata> adapters = discovery.discoverAdapters(AdapterDirection.INPUT, FRAMEWORK, PARADIGM);

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

    when(templateRepository.templateExists(OUTPUT_INDEX)).thenReturn(true);
    when(templateRepository.getTemplateContent(OUTPUT_INDEX))
        .thenReturn(inputIndexJson("mongodb"));
    when(metadataLoader.loadMetadata("frameworks/spring/reactive/adapters/driven-adapters/mongodb/metadata.yml"))
        .thenReturn(mongoMetadata);

    // When
    List<AdapterMetadata> adapters = discovery.discoverAdapters(AdapterDirection.OUTPUT, FRAMEWORK, PARADIGM);

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

    when(templateRepository.templateExists(INPUT_INDEX)).thenReturn(true);
    when(templateRepository.getTemplateContent(INPUT_INDEX))
        .thenReturn(inputIndexJson("rest"));
    when(metadataLoader.loadMetadata("frameworks/spring/reactive/adapters/entry-points/rest/metadata.yml"))
        .thenReturn(restMetadata);

    when(templateRepository.templateExists(OUTPUT_INDEX)).thenReturn(true);
    when(templateRepository.getTemplateContent(OUTPUT_INDEX))
        .thenReturn(inputIndexJson("mongodb"));
    when(metadataLoader.loadMetadata("frameworks/spring/reactive/adapters/driven-adapters/mongodb/metadata.yml"))
        .thenReturn(mongoMetadata);

    // When
    List<AdapterMetadata> inputAdapters = discovery.discoverAdapters(AdapterDirection.INPUT, FRAMEWORK, PARADIGM);
    List<AdapterMetadata> outputAdapters = discovery.discoverAdapters(AdapterDirection.OUTPUT, FRAMEWORK, PARADIGM);

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

    when(templateRepository.templateExists(INPUT_INDEX)).thenReturn(true);
    when(templateRepository.getTemplateContent(INPUT_INDEX))
        .thenReturn(inputIndexJson("rest"));
    when(metadataLoader.loadMetadata("frameworks/spring/reactive/adapters/entry-points/rest/metadata.yml"))
        .thenReturn(restMetadata);
    lenient().when(templateRepository.loadAdapterMetadata("rest", FRAMEWORK, PARADIGM, "entry-points"))
        .thenReturn(restMetadata);
    lenient().when(templateRepository.listTemplatesInDirectory(anyString()))
        .thenReturn(List.of("Controller.java.ftl"));

    // When
    Optional<AdapterMetadata> result = discovery.findAdapter("rest", AdapterDirection.INPUT, FRAMEWORK, PARADIGM);

    // Then
    assertTrue(result.isPresent());
    assertEquals("rest", result.get().getName());
  }

  @Test
  @DisplayName("Should return empty when adapter not found")
  void shouldReturnEmptyWhenAdapterNotFound() {
    // Given
    when(templateRepository.templateExists("frameworks/spring/reactive/adapters/entry-points/index.json"))
        .thenReturn(false);

    // When
    Optional<AdapterMetadata> result = discovery.findAdapter("nonexistent", AdapterDirection.INPUT, FRAMEWORK, PARADIGM);

    // Then
    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Should return empty when adapter has wrong direction")
  void shouldReturnEmptyWhenAdapterHasWrongDirection() {
    // Given - rest is INPUT but we're looking for OUTPUT
    when(templateRepository.templateExists(OUTPUT_INDEX)).thenReturn(true);
    when(templateRepository.getTemplateContent(OUTPUT_INDEX))
        .thenReturn(inputIndexJson()); // empty adapters list for output

    // When
    Optional<AdapterMetadata> result = discovery.findAdapter("rest", AdapterDirection.OUTPUT, FRAMEWORK, PARADIGM);

    // Then
    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Should check if adapter exists")
  void shouldCheckIfAdapterExists() {
    // Given
    when(templateRepository.templateExists(INPUT_INDEX)).thenReturn(true);
    when(templateRepository.getTemplateContent(INPUT_INDEX))
        .thenReturn(inputIndexJson("rest"));

    // When & Then
    assertTrue(discovery.adapterExists("rest", AdapterDirection.INPUT, FRAMEWORK, PARADIGM));
    assertFalse(discovery.adapterExists("nonexistent", AdapterDirection.INPUT, FRAMEWORK, PARADIGM));
  }

  @Test
  @DisplayName("Should list adapter names")
  void shouldListAdapterNames() {
    // Given
    when(templateRepository.templateExists(INPUT_INDEX)).thenReturn(true);
    when(templateRepository.getTemplateContent(INPUT_INDEX))
        .thenReturn(inputIndexJson("graphql", "rest"));

    // When
    List<String> names = discovery.listAdapterNames(AdapterDirection.INPUT, FRAMEWORK, PARADIGM);

    // Then
    assertTrue(names.contains("rest"));
    assertTrue(names.contains("graphql"));
    // Names should be sorted
    assertEquals(names, names.stream().sorted().toList());
  }

  @Test
  @DisplayName("Should handle errors gracefully during discovery")
  void shouldHandleErrorsGracefullyDuringDiscovery() {
    // Given - index exists but adapter metadata fails
    when(templateRepository.templateExists(INPUT_INDEX)).thenReturn(true);
    when(templateRepository.getTemplateContent(INPUT_INDEX))
        .thenReturn(inputIndexJson("rest"));
    when(metadataLoader.loadMetadata("frameworks/spring/reactive/adapters/entry-points/rest/metadata.yml"))
        .thenThrow(new RuntimeException("Failed to load"));

    // When - should not throw, just skip the failed adapter
    List<AdapterMetadata> adapters = discovery.discoverAdapters(AdapterDirection.INPUT, FRAMEWORK, PARADIGM);

    // Then - should return empty or partial list, not throw
    assertTrue(adapters != null);
  }
}
