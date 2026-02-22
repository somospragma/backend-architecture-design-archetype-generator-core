package com.pragma.archetype.infrastructure.adapter.out.filesystem;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.port.out.FileSystemPort.FileWriteException;

@DisplayName("LocalFileSystemAdapter Tests")
class LocalFileSystemAdapterTest {

  @Nested
  @DisplayName("Write File Operations")
  class WriteFileOperations {

    @Test
    @DisplayName("Should write file successfully")
    void shouldWriteFileSuccessfully(@TempDir Path tempDir) {
      // Given
      Path filePath = tempDir.resolve("test.txt");
      GeneratedFile file = GeneratedFile.create(filePath, "test content");

      // When
      adapter.writeFile(file);

      // Then
      assertTrue(Files.exists(filePath));
      assertEquals("test content", readFileContent(filePath));
    }

    @Test
    @DisplayName("Should create parent directories when writing file")
    void shouldCreateParentDirectoriesWhenWritingFile(@TempDir Path tempDir) {
      // Given
      Path filePath = tempDir.resolve("nested/dir/test.txt");
      GeneratedFile file = GeneratedFile.create(filePath, "content");

      // When
      adapter.writeFile(file);

      // Then
      assertTrue(Files.exists(filePath));
      assertTrue(Files.exists(filePath.getParent()));
      assertEquals("content", readFileContent(filePath));
    }

    @Test
    @DisplayName("Should overwrite existing file")
    void shouldOverwriteExistingFile(@TempDir Path tempDir) throws IOException {
      // Given
      Path filePath = tempDir.resolve("test.txt");
      Files.writeString(filePath, "old content");
      GeneratedFile file = GeneratedFile.create(filePath, "new content");

      // When
      adapter.writeFile(file);

      // Then
      assertEquals("new content", readFileContent(filePath));
    }

    @Test
    @DisplayName("Should write multiple files")
    void shouldWriteMultipleFiles(@TempDir Path tempDir) {
      // Given
      List<GeneratedFile> files = List.of(
          GeneratedFile.create(tempDir.resolve("file1.txt"), "content1"),
          GeneratedFile.create(tempDir.resolve("file2.txt"), "content2"),
          GeneratedFile.create(tempDir.resolve("file3.txt"), "content3"));

      // When
      adapter.writeFiles(files);

      // Then
      assertTrue(Files.exists(tempDir.resolve("file1.txt")));
      assertTrue(Files.exists(tempDir.resolve("file2.txt")));
      assertTrue(Files.exists(tempDir.resolve("file3.txt")));
      assertEquals("content1", readFileContent(tempDir.resolve("file1.txt")));
      assertEquals("content2", readFileContent(tempDir.resolve("file2.txt")));
      assertEquals("content3", readFileContent(tempDir.resolve("file3.txt")));
    }

    @Test
    @DisplayName("Should write empty file")
    void shouldWriteEmptyFile(@TempDir Path tempDir) {
      // Given
      Path filePath = tempDir.resolve("empty.txt");
      GeneratedFile file = GeneratedFile.create(filePath, "");

      // When
      adapter.writeFile(file);

      // Then
      assertTrue(Files.exists(filePath));
      assertEquals("", readFileContent(filePath));
    }

    @Test
    @DisplayName("Should write file with special characters")
    void shouldWriteFileWithSpecialCharacters(@TempDir Path tempDir) {
      // Given
      Path filePath = tempDir.resolve("special.txt");
      String content = "Special chars: áéíóú ñ €£¥ 中文 日本語";
      GeneratedFile file = GeneratedFile.create(filePath, content);

      // When
      adapter.writeFile(file);

      // Then
      assertEquals(content, readFileContent(filePath));
    }

    @Test
    @DisplayName("Should write large file")
    void shouldWriteLargeFile(@TempDir Path tempDir) {
      // Given
      Path filePath = tempDir.resolve("large.txt");
      String content = "x".repeat(10000);
      GeneratedFile file = GeneratedFile.create(filePath, content);

      // When
      adapter.writeFile(file);

      // Then
      assertEquals(content, readFileContent(filePath));
    }
  }

  @Nested
  @DisplayName("Directory Operations")
  class DirectoryOperations {

    @Test
    @DisplayName("Should create directory")
    void shouldCreateDirectory(@TempDir Path tempDir) {
      // Given
      Path dirPath = tempDir.resolve("new-dir");

      // When
      adapter.createDirectory(dirPath);

      // Then
      assertTrue(Files.exists(dirPath));
      assertTrue(Files.isDirectory(dirPath));
    }

    @Test
    @DisplayName("Should create nested directories")
    void shouldCreateNestedDirectories(@TempDir Path tempDir) {
      // Given
      Path dirPath = tempDir.resolve("level1/level2/level3");

      // When
      adapter.createDirectory(dirPath);

      // Then
      assertTrue(Files.exists(dirPath));
      assertTrue(Files.isDirectory(dirPath));
    }

    @Test
    @DisplayName("Should not fail when directory already exists")
    void shouldNotFailWhenDirectoryAlreadyExists(@TempDir Path tempDir) throws IOException {
      // Given
      Path dirPath = tempDir.resolve("existing-dir");
      Files.createDirectory(dirPath);

      // When/Then
      assertDoesNotThrow(() -> adapter.createDirectory(dirPath));
      assertTrue(Files.exists(dirPath));
    }

    @Test
    @DisplayName("Should check if directory exists")
    void shouldCheckIfDirectoryExists(@TempDir Path tempDir) throws IOException {
      // Given
      Path existingDir = tempDir.resolve("existing");
      Path nonExistingDir = tempDir.resolve("non-existing");
      Files.createDirectory(existingDir);

      // When/Then
      assertTrue(adapter.directoryExists(existingDir));
      assertFalse(adapter.directoryExists(nonExistingDir));
    }

    @Test
    @DisplayName("Should return false for file when checking directory exists")
    void shouldReturnFalseForFileWhenCheckingDirectoryExists(@TempDir Path tempDir) throws IOException {
      // Given
      Path filePath = tempDir.resolve("file.txt");
      Files.writeString(filePath, "content");

      // When/Then
      assertFalse(adapter.directoryExists(filePath));
    }
  }

  @Nested
  @DisplayName("File Existence Operations")
  class FileExistenceOperations {

    @Test
    @DisplayName("Should check if file exists")
    void shouldCheckIfFileExists(@TempDir Path tempDir) throws IOException {
      // Given
      Path existingFile = tempDir.resolve("existing.txt");
      Path nonExistingFile = tempDir.resolve("non-existing.txt");
      Files.writeString(existingFile, "content");

      // When/Then
      assertTrue(adapter.exists(existingFile));
      assertFalse(adapter.exists(nonExistingFile));
    }

    @Test
    @DisplayName("Should check if directory exists using exists method")
    void shouldCheckIfDirectoryExistsUsingExistsMethod(@TempDir Path tempDir) throws IOException {
      // Given
      Path dir = tempDir.resolve("dir");
      Files.createDirectory(dir);

      // When/Then
      assertTrue(adapter.exists(dir));
    }
  }

  @Nested
  @DisplayName("List Files Operations")
  class ListFilesOperations {

    @Test
    @DisplayName("Should list files in directory")
    void shouldListFilesInDirectory(@TempDir Path tempDir) throws IOException {
      // Given
      Files.writeString(tempDir.resolve("file1.txt"), "content1");
      Files.writeString(tempDir.resolve("file2.txt"), "content2");
      Files.createDirectory(tempDir.resolve("subdir"));

      // When
      List<Path> files = adapter.listFiles(tempDir);

      // Then
      assertEquals(3, files.size());
      assertTrue(files.stream().anyMatch(p -> p.getFileName().toString().equals("file1.txt")));
      assertTrue(files.stream().anyMatch(p -> p.getFileName().toString().equals("file2.txt")));
      assertTrue(files.stream().anyMatch(p -> p.getFileName().toString().equals("subdir")));
    }

    @Test
    @DisplayName("Should return empty list for non-existing directory")
    void shouldReturnEmptyListForNonExistingDirectory(@TempDir Path tempDir) {
      // Given
      Path nonExistingDir = tempDir.resolve("non-existing");

      // When
      List<Path> files = adapter.listFiles(nonExistingDir);

      // Then
      assertTrue(files.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list for empty directory")
    void shouldReturnEmptyListForEmptyDirectory(@TempDir Path tempDir) throws IOException {
      // Given
      Path emptyDir = tempDir.resolve("empty");
      Files.createDirectory(emptyDir);

      // When
      List<Path> files = adapter.listFiles(emptyDir);

      // Then
      assertTrue(files.isEmpty());
    }
  }

  @Nested
  @DisplayName("Project Empty Check Operations")
  class ProjectEmptyCheckOperations {

    @Test
    @DisplayName("Should return true for empty directory")
    void shouldReturnTrueForEmptyDirectory(@TempDir Path tempDir) {
      // When/Then
      assertTrue(adapter.isProjectEmpty(tempDir));
    }

    @Test
    @DisplayName("Should return true for directory with only allowed files")
    void shouldReturnTrueForDirectoryWithOnlyAllowedFiles(@TempDir Path tempDir) throws IOException {
      // Given
      Files.writeString(tempDir.resolve("build.gradle.kts"), "");
      Files.writeString(tempDir.resolve("settings.gradle.kts"), "");
      Files.writeString(tempDir.resolve(".gitignore"), "");
      Files.createDirectory(tempDir.resolve(".git"));

      // When/Then
      assertTrue(adapter.isProjectEmpty(tempDir));
    }

    @Test
    @DisplayName("Should return false for directory with non-allowed files")
    void shouldReturnFalseForDirectoryWithNonAllowedFiles(@TempDir Path tempDir) throws IOException {
      // Given
      Files.writeString(tempDir.resolve("build.gradle.kts"), "");
      Files.createDirectories(tempDir.resolve("src"));
      Files.writeString(tempDir.resolve("src/Main.java"), "");

      // When/Then
      assertFalse(adapter.isProjectEmpty(tempDir));
    }

    @Test
    @DisplayName("Should return false for non-existing directory")
    void shouldReturnFalseForNonExistingDirectory(@TempDir Path tempDir) {
      // Given
      Path nonExisting = tempDir.resolve("non-existing");

      // When/Then
      assertFalse(adapter.isProjectEmpty(nonExisting));
    }

    @Test
    @DisplayName("Should return false for file path")
    void shouldReturnFalseForFilePath(@TempDir Path tempDir) throws IOException {
      // Given
      Path filePath = tempDir.resolve("file.txt");
      Files.writeString(filePath, "content");

      // When/Then
      assertFalse(adapter.isProjectEmpty(filePath));
    }
  }

  @Nested
  @DisplayName("Read File Operations")
  class ReadFileOperations {

    @Test
    @DisplayName("Should read file content")
    void shouldReadFileContent(@TempDir Path tempDir) throws IOException {
      // Given
      Path filePath = tempDir.resolve("test.txt");
      String content = "test content";
      Files.writeString(filePath, content);

      // When
      String result = adapter.readFile(filePath);

      // Then
      assertEquals(content, result);
    }

    @Test
    @DisplayName("Should read empty file")
    void shouldReadEmptyFile(@TempDir Path tempDir) throws IOException {
      // Given
      Path filePath = tempDir.resolve("empty.txt");
      Files.writeString(filePath, "");

      // When
      String result = adapter.readFile(filePath);

      // Then
      assertEquals("", result);
    }

    @Test
    @DisplayName("Should read file with special characters")
    void shouldReadFileWithSpecialCharacters(@TempDir Path tempDir) throws IOException {
      // Given
      Path filePath = tempDir.resolve("special.txt");
      String content = "Special: áéíóú ñ €£¥ 中文";
      Files.writeString(filePath, content);

      // When
      String result = adapter.readFile(filePath);

      // Then
      assertEquals(content, result);
    }

    @Test
    @DisplayName("Should throw exception when reading non-existing file")
    void shouldThrowExceptionWhenReadingNonExistingFile(@TempDir Path tempDir) {
      // Given
      Path nonExisting = tempDir.resolve("non-existing.txt");

      // When/Then
      assertThrows(FileWriteException.class, () -> adapter.readFile(nonExisting));
    }
  }

  @Nested
  @DisplayName("Append to File Operations")
  class AppendToFileOperations {

    @Test
    @DisplayName("Should append content to file")
    void shouldAppendContentToFile(@TempDir Path tempDir) throws IOException {
      // Given
      Path filePath = tempDir.resolve("test.txt");
      Files.writeString(filePath, "initial content");

      // When
      adapter.appendToFile(filePath, "\nappended content");

      // Then
      String result = Files.readString(filePath);
      assertEquals("initial content\nappended content", result);
    }

    @Test
    @DisplayName("Should append to empty file")
    void shouldAppendToEmptyFile(@TempDir Path tempDir) throws IOException {
      // Given
      Path filePath = tempDir.resolve("empty.txt");
      Files.writeString(filePath, "");

      // When
      adapter.appendToFile(filePath, "new content");

      // Then
      assertEquals("new content", Files.readString(filePath));
    }

    @Test
    @DisplayName("Should append multiple times")
    void shouldAppendMultipleTimes(@TempDir Path tempDir) throws IOException {
      // Given
      Path filePath = tempDir.resolve("test.txt");
      Files.writeString(filePath, "line1");

      // When
      adapter.appendToFile(filePath, "\nline2");
      adapter.appendToFile(filePath, "\nline3");

      // Then
      assertEquals("line1\nline2\nline3", Files.readString(filePath));
    }

    @Test
    @DisplayName("Should throw exception when appending to non-existing file")
    void shouldThrowExceptionWhenAppendingToNonExistingFile(@TempDir Path tempDir) {
      // Given
      Path nonExisting = tempDir.resolve("non-existing.txt");

      // When/Then
      assertThrows(FileWriteException.class,
          () -> adapter.appendToFile(nonExisting, "content"));
    }
  }

  private LocalFileSystemAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new LocalFileSystemAdapter();
  }

  // Helper method
  private String readFileContent(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
