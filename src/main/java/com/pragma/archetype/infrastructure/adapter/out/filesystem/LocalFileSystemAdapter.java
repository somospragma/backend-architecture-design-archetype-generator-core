package com.pragma.archetype.infrastructure.adapter.out.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.port.out.FileSystemPort;

/**
 * Adapter for local file system operations.
 * Implements FileSystemPort using Java NIO.
 */
public class LocalFileSystemAdapter implements FileSystemPort {

  @Override
  public void writeFile(GeneratedFile file) {
    try {
      // Create parent directories if they don't exist
      Path parent = file.path().getParent();
      if (parent != null && !Files.exists(parent)) {
        Files.createDirectories(parent);
      }

      // Atomic write: write to temporary file first, then rename
      Path tempFile = file.path().resolveSibling(file.path().getFileName() + ".tmp");
      Files.writeString(tempFile, file.content());
      Files.move(tempFile, file.path(), java.nio.file.StandardCopyOption.REPLACE_EXISTING,
          java.nio.file.StandardCopyOption.ATOMIC_MOVE);

    } catch (IOException e) {
      throw new FileWriteException(
          "Failed to write file: " + file.path(),
          e);
    }
  }

  @Override
  public void writeFiles(List<GeneratedFile> files) {
    for (GeneratedFile file : files) {
      writeFile(file);
    }
  }

  @Override
  public void createDirectory(Path path) {
    try {
      if (!Files.exists(path)) {
        Files.createDirectories(path);
      }
    } catch (IOException e) {
      throw new FileWriteException(
          "Failed to create directory: " + path,
          e);
    }
  }

  @Override
  public boolean exists(Path path) {
    return Files.exists(path);
  }

  @Override
  public boolean directoryExists(Path path) {
    return Files.exists(path) && Files.isDirectory(path);
  }

  @Override
  public boolean isProjectEmpty(Path path) {
    if (!directoryExists(path)) {
      return false;
    }

    List<Path> files = listFiles(path);

    // Allowed files in an "empty" project
    List<String> allowedFiles = List.of(
        "build.gradle.kts",
        "build.gradle",
        "settings.gradle.kts",
        "settings.gradle",
        "gradlew",
        "gradlew.bat",
        ".gitignore",
        ".git",
        "gradle",
        ".gradle");

    return files.stream()
        .map(file -> file.getFileName().toString())
        .allMatch(allowedFiles::contains);
  }

  @Override
  public List<Path> listFiles(Path path) {
    try {
      if (!directoryExists(path)) {
        return List.of();
      }

      try (Stream<Path> stream = Files.list(path)) {
        return stream.toList();
      }

    } catch (IOException e) {
      throw new FileWriteException(
          "Failed to list files in directory: " + path,
          e);
    }
  }

  @Override
  public String readFile(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new FileWriteException(
          "Failed to read file: " + path,
          e);
    }
  }

  @Override
  public void appendToFile(Path path, String content) {
    try {
      String existingContent = readFile(path);
      String newContent = existingContent + content;

      // Atomic write: write to temporary file first, then rename
      Path tempFile = path.resolveSibling(path.getFileName() + ".tmp");
      Files.writeString(tempFile, newContent);
      Files.move(tempFile, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
          java.nio.file.StandardCopyOption.ATOMIC_MOVE);

    } catch (IOException e) {
      throw new FileWriteException(
          "Failed to append to file: " + path,
          e);
    }
  }
}
