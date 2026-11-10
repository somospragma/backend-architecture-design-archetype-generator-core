package com.pragma.archetype.domain.model.file;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Keeps generated Java file names in sync with the public type they declare.
 * javac requires {@code public class Foo} to live in {@code Foo.java}; templates
 * own the class name, so the file name is derived from the rendered source
 * instead of a naming convention that can drift from it.
 */
public final class JavaSourceNames {

  private static final Pattern PUBLIC_TOP_LEVEL_TYPE = Pattern.compile(
      "^public\\s+(?:(?:final|abstract|sealed|non-sealed|strictfp)\\s+)*(?:class|interface|enum|record|@interface)\\s+([A-Za-z_$][\\w$]*)",
      Pattern.MULTILINE);

  private JavaSourceNames() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Finds the top-level public type declared in a Java source.
   * Only declarations starting at column 0 count, so nested types are ignored.
   */
  public static Optional<String> publicTypeName(String source) {
    if (source == null) {
      return Optional.empty();
    }
    Matcher matcher = PUBLIC_TOP_LEVEL_TYPE.matcher(source);
    return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
  }

  /**
   * Returns the path the source must be written to: same directory, named after
   * its public type. Non-Java paths and sources without a public type are
   * returned unchanged.
   */
  public static Path alignWithPublicType(Path path, String source) {
    Path fileName = path.getFileName();
    if (fileName == null || !fileName.toString().endsWith(".java")) {
      return path;
    }
    return publicTypeName(source)
        .map(type -> type + ".java")
        .filter(expected -> !expected.equals(fileName.toString()))
        .map(path::resolveSibling)
        .orElse(path);
  }
}
