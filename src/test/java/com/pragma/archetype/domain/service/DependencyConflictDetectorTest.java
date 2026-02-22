package com.pragma.archetype.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.AdapterMetadata;

class DependencyConflictDetectorTest {

  private DependencyConflictDetector detector;

  @BeforeEach
  void setUp() {
    detector = new DependencyConflictDetector();
  }

  @Test
  void shouldDetectVersionConflict() {
    // Given
    List<AdapterMetadata.Dependency> existing = List.of(
        new AdapterMetadata.Dependency("org.springframework.boot", "spring-boot-starter", "3.1.0", "compile"));

    List<AdapterMetadata.Dependency> newDeps = List.of(
        new AdapterMetadata.Dependency("org.springframework.boot", "spring-boot-starter", "3.2.0", "compile"));

    // When
    List<String> conflicts = detector.detectVersionConflicts(existing, newDeps);

    // Then
    assertFalse(conflicts.isEmpty());
    assertTrue(conflicts.get(0).contains("Version conflict"));
    assertTrue(conflicts.get(0).contains("3.1.0"));
    assertTrue(conflicts.get(0).contains("3.2.0"));
  }

  @Test
  void shouldNotDetectConflictWhenVersionsMatch() {
    // Given
    List<AdapterMetadata.Dependency> existing = List.of(
        new AdapterMetadata.Dependency("org.springframework.boot", "spring-boot-starter", "3.1.0", "compile"));

    List<AdapterMetadata.Dependency> newDeps = List.of(
        new AdapterMetadata.Dependency("org.springframework.boot", "spring-boot-starter", "3.1.0", "compile"));

    // When
    List<String> conflicts = detector.detectVersionConflicts(existing, newDeps);

    // Then
    assertTrue(conflicts.isEmpty());
  }

  @Test
  void shouldDetectFrameworkConflictForSpring() {
    // Given
    List<AdapterMetadata.Dependency> newDeps = List.of(
        new AdapterMetadata.Dependency("io.quarkus", "quarkus-arc", "3.0.0", "compile"));

    // When
    List<String> conflicts = detector.detectFrameworkConflicts("spring", newDeps);

    // Then
    assertFalse(conflicts.isEmpty());
    assertTrue(conflicts.get(0).contains("Framework conflict"));
    assertTrue(conflicts.get(0).contains("spring"));
  }

  @Test
  void shouldDetectFrameworkConflictForQuarkus() {
    // Given
    List<AdapterMetadata.Dependency> newDeps = List.of(
        new AdapterMetadata.Dependency("org.springframework.boot", "spring-boot-starter", "3.1.0", "compile"));

    // When
    List<String> conflicts = detector.detectFrameworkConflicts("quarkus", newDeps);

    // Then
    assertFalse(conflicts.isEmpty());
    assertTrue(conflicts.get(0).contains("Framework conflict"));
  }

  @Test
  void shouldNotDetectFrameworkConflictForCompatibleDeps() {
    // Given
    List<AdapterMetadata.Dependency> newDeps = List.of(
        new AdapterMetadata.Dependency("com.fasterxml.jackson.core", "jackson-databind", "2.15.0", "compile"));

    // When
    List<String> conflicts = detector.detectFrameworkConflicts("spring", newDeps);

    // Then
    assertTrue(conflicts.isEmpty());
  }

  @Test
  void shouldSuggestResolutionForVersionConflicts() {
    // Given
    List<String> conflicts = List.of("Version conflict for org.springframework.boot:spring-boot-starter");

    // When
    List<String> suggestions = detector.suggestResolution(conflicts);

    // Then
    assertFalse(suggestions.isEmpty());
    assertTrue(suggestions.stream().anyMatch(s -> s.contains("version conflicts")));
    assertTrue(suggestions.stream().anyMatch(s -> s.contains("dependencyOverrides")));
  }

  @Test
  void shouldSuggestResolutionForFrameworkConflicts() {
    // Given
    List<String> conflicts = List.of("Framework conflict: io.quarkus:quarkus-arc");

    // When
    List<String> suggestions = detector.suggestResolution(conflicts);

    // Then
    assertFalse(suggestions.isEmpty());
    assertTrue(suggestions.stream().anyMatch(s -> s.contains("framework conflicts")));
  }

  @Test
  void shouldReturnEmptySuggestionsForNoConflicts() {
    // Given
    List<String> conflicts = List.of();

    // When
    List<String> suggestions = detector.suggestResolution(conflicts);

    // Then
    assertTrue(suggestions.isEmpty());
  }

  @Test
  void shouldGetVersionOverride() {
    // Given
    AdapterMetadata.Dependency dep = new AdapterMetadata.Dependency(
        "org.springframework.boot", "spring-boot-starter", "3.1.0", "compile");
    Map<String, String> overrides = Map.of(
        "org.springframework.boot:spring-boot-starter", "3.2.0");

    // When
    String override = detector.getVersionOverride(dep, overrides);

    // Then
    assertEquals("3.2.0", override);
  }

  @Test
  void shouldReturnNullWhenNoOverride() {
    // Given
    AdapterMetadata.Dependency dep = new AdapterMetadata.Dependency(
        "org.springframework.boot", "spring-boot-starter", "3.1.0", "compile");
    Map<String, String> overrides = Map.of();

    // When
    String override = detector.getVersionOverride(dep, overrides);

    // Then
    assertNull(override);
  }

  @Test
  void shouldApplyVersionOverrides() {
    // Given
    List<AdapterMetadata.Dependency> deps = List.of(
        new AdapterMetadata.Dependency("org.springframework.boot", "spring-boot-starter", "3.1.0", "compile"),
        new AdapterMetadata.Dependency("com.fasterxml.jackson.core", "jackson-databind", "2.15.0", "compile"));

    Map<String, String> overrides = Map.of(
        "org.springframework.boot:spring-boot-starter", "3.2.0");

    // When
    List<AdapterMetadata.Dependency> result = detector.applyVersionOverrides(deps, overrides);

    // Then
    assertEquals(2, result.size());
    assertEquals("3.2.0", result.get(0).version());
    assertEquals("2.15.0", result.get(1).version());
  }

  @Test
  void shouldReturnOriginalDepsWhenNoOverrides() {
    // Given
    List<AdapterMetadata.Dependency> deps = List.of(
        new AdapterMetadata.Dependency("org.springframework.boot", "spring-boot-starter", "3.1.0", "compile"));

    // When
    List<AdapterMetadata.Dependency> result = detector.applyVersionOverrides(deps, null);

    // Then
    assertEquals(1, result.size());
    assertEquals("3.1.0", result.get(0).version());
  }
}
