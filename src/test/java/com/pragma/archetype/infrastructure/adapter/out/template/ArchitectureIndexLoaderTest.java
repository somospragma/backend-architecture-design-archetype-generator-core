package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.port.out.TemplateRepository.TemplateNotFoundException;

@DisplayName("ArchitectureIndexLoader Tests")
class ArchitectureIndexLoaderTest {

  private final Map<String, String> templates = new HashMap<>();
  private ArchitectureIndexLoader loader;

  @BeforeEach
  void setUp() {
    loader = new ArchitectureIndexLoader(new TemplateContentProvider() {
      @Override
      public String getTemplateContent(String templatePath) {
        return templates.get(templatePath);
      }

      @Override
      public boolean templateExists(String templatePath) {
        return templates.containsKey(templatePath);
      }
    });
  }

  @Test
  @DisplayName("Should list the architectures declared in index.yml, including new ones")
  void shouldLoadArchitecturesFromIndex() {
    templates.put("architectures/index.yml", """
        schemaVersion: 1
        architectures:
          - name: hexagonal-single
            description: Single module
          - name: clean-multi
            description: Declared only by the templates
        """);

    List<ArchitectureType> architectures = loader.load();

    assertEquals(List.of(ArchitectureType.HEXAGONAL_SINGLE, ArchitectureType.fromValue("clean-multi")),
        architectures);
  }

  @Test
  @DisplayName("Should fall back to built-in architectures when templates predate the index")
  void shouldFallBackToBuiltInArchitectures() {
    assertEquals(List.of(ArchitectureType.values()), loader.load());
  }

  @Test
  @DisplayName("Should reject an index without architectures")
  void shouldRejectEmptyIndex() {
    templates.put("architectures/index.yml", "schemaVersion: 1\narchitectures: []\n");

    TemplateNotFoundException error = assertThrows(TemplateNotFoundException.class, loader::load);
    assertTrue(error.getMessage().contains("architectures"));
  }

  @Test
  @DisplayName("Should reject entries whose name is not a directory name")
  void shouldRejectInvalidNames() {
    templates.put("architectures/index.yml", """
        schemaVersion: 1
        architectures:
          - name: ../outside
        """);

    assertThrows(TemplateNotFoundException.class, loader::load);
  }
}
