package com.pragma.archetype.domain.model.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JavaSourceNames Tests")
class JavaSourceNamesTest {

  private static final Path DIR = Path.of("/project/src/main/java/com/acme");

  @Test
  @DisplayName("Should rename the file after the public class the template declares")
  void shouldRenameFileAfterPublicClass() {
    String source = """
        package com.acme;

        @Component
        public class ProductDynamoDbAdapter implements IProductGateway {
        }
        """;

    assertEquals(DIR.resolve("ProductDynamoDbAdapter.java"),
        JavaSourceNames.alignWithPublicType(DIR.resolve("ProductAdapter.java"), source));
  }

  @Test
  @DisplayName("Should recognize modifiers, records and interfaces")
  void shouldRecognizeOtherTypeDeclarations() {
    assertEquals("SchedulingConfig", JavaSourceNames.publicTypeName("public final class SchedulingConfig {}").orElseThrow());
    assertEquals("ProductDto", JavaSourceNames.publicTypeName("public record ProductDto(String id) {}").orElseThrow());
    assertEquals("IProductGateway", JavaSourceNames.publicTypeName("public interface IProductGateway {}").orElseThrow());
    assertEquals("Status", JavaSourceNames.publicTypeName("public sealed interface Status permits A {}").orElseThrow());
  }

  @Test
  @DisplayName("Should ignore nested public types and keep the path when it already matches")
  void shouldIgnoreNestedTypes() {
    String source = """
        public class Outer {
          public static class Inner {}
        }
        """;
    Path path = DIR.resolve("Outer.java");

    assertEquals(path, JavaSourceNames.alignWithPublicType(path, source));
  }

  @Test
  @DisplayName("Should leave non-Java files and sources without a public type untouched")
  void shouldLeaveOtherFilesUntouched() {
    Path yaml = DIR.resolve("application.yml");
    Path java = DIR.resolve("Helper.java");

    assertEquals(yaml, JavaSourceNames.alignWithPublicType(yaml, "public class Foo {}"));
    assertEquals(java, JavaSourceNames.alignWithPublicType(java, "class PackagePrivate {}"));
    assertTrue(JavaSourceNames.publicTypeName("template content").isEmpty());
  }

  @Test
  @DisplayName("GeneratedFile should apply the public type name to its path")
  void generatedFileShouldUsePublicTypeName() {
    GeneratedFile file = GeneratedFile.javaSource(DIR.resolve("ProductgrpcService.java"),
        "public class ProductgrpcGrpcService extends Base {}");

    assertEquals(DIR.resolve("ProductgrpcGrpcService.java"), file.path());
  }
}
