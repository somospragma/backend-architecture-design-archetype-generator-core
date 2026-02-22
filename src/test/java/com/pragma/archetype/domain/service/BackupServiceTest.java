package com.pragma.archetype.domain.service;

import com.pragma.archetype.domain.port.out.FileSystemPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BackupService.
 */
class BackupServiceTest {

  @TempDir
  Path tempDir;

  private BackupService backupService;
  private TestFileSystemPort fileSystemPort;

  @BeforeEach
  void setUp() {
    fileSystemPort = new TestFileSystemPort();
    backupService = new BackupService(fileSystemPort);
  }

  @Test
  void createBackup_shouldBackupExistingFiles() throws IOException {
    // Given: Project with files to backup
    Path projectPath = tempDir;
    Path file1 = projectPath.resolve("src/main/File1.java");
    Path file2 = projectPath.resolve("src/test/File2.java");

    Files.createDirectories(file1.getParent());
    Files.createDirectories(file2.getParent());
    Files.writeString(file1, "content1");
    Files.writeString(file2, "content2");

    List<Path> filesToBackup = List.of(
        Path.of("src/main/File1.java"),
        Path.of("src/test/File2.java"));

    // When: Creating backup
    String backupId = backupService.createBackup(projectPath, filesToBackup);

    // Then: Backup should be created
    assertNotNull(backupId);
    assertTrue(backupId.startsWith("backup_"));

    Path backupPath = projectPath.resolve(".cleanarch/backups").resolve(backupId);
    assertTrue(Files.exists(backupPath));
    assertTrue(Files.exists(backupPath.resolve("backup_metadata.txt")));

    // Verify backed up files exist
    Path backedUpFile1 = backupPath.resolve("src/main/File1.java");
    Path backedUpFile2 = backupPath.resolve("src/test/File2.java");
    assertTrue(Files.exists(backedUpFile1));
    assertTrue(Files.exists(backedUpFile2));
    assertEquals("content1", Files.readString(backedUpFile1));
    assertEquals("content2", Files.readString(backedUpFile2));
  }

  @Test
  void createBackup_shouldSkipNonExistentFiles() throws IOException {
    // Given: Project with only one file existing
    Path projectPath = tempDir;
    Path file1 = projectPath.resolve("src/main/File1.java");

    Files.createDirectories(file1.getParent());
    Files.writeString(file1, "content1");

    List<Path> filesToBackup = List.of(
        Path.of("src/main/File1.java"),
        Path.of("src/main/NonExistent.java"));

    // When: Creating backup
    String backupId = backupService.createBackup(projectPath, filesToBackup);

    // Then: Only existing file should be backed up
    assertNotNull(backupId);

    Path backupPath = projectPath.resolve(".cleanarch/backups").resolve(backupId);
    Path backedUpFile1 = backupPath.resolve("src/main/File1.java");
    Path backedUpFile2 = backupPath.resolve("src/main/NonExistent.java");

    assertTrue(Files.exists(backedUpFile1));
    assertFalse(Files.exists(backedUpFile2));
  }

  @Test
  void createBackup_shouldThrowExceptionForEmptyFileList() {
    // Given: Empty file list
    Path projectPath = tempDir;
    List<Path> filesToBackup = List.of();

    // When/Then: Should throw exception
    assertThrows(BackupService.BackupException.class, () -> {
      backupService.createBackup(projectPath, filesToBackup);
    });
  }

  @Test
  void createBackup_shouldThrowExceptionForNullFileList() {
    // Given: Null file list
    Path projectPath = tempDir;

    // When/Then: Should throw exception
    assertThrows(BackupService.BackupException.class, () -> {
      backupService.createBackup(projectPath, null);
    });
  }

  @Test
  void restoreBackup_shouldRestoreBackedUpFiles() throws IOException {
    // Given: Backup exists
    Path projectPath = tempDir;
    Path file1 = projectPath.resolve("src/main/File1.java");
    Path file2 = projectPath.resolve("src/test/File2.java");

    Files.createDirectories(file1.getParent());
    Files.createDirectories(file2.getParent());
    Files.writeString(file1, "original1");
    Files.writeString(file2, "original2");

    List<Path> filesToBackup = List.of(
        Path.of("src/main/File1.java"),
        Path.of("src/test/File2.java"));

    String backupId = backupService.createBackup(projectPath, filesToBackup);

    // Modify files
    Files.writeString(file1, "modified1");
    Files.writeString(file2, "modified2");

    // When: Restoring backup
    backupService.restoreBackup(projectPath, backupId);

    // Then: Files should be restored to original content
    assertEquals("original1", Files.readString(file1));
    assertEquals("original2", Files.readString(file2));
  }

  @Test
  void restoreBackup_shouldThrowExceptionForNonExistentBackup() {
    // Given: Non-existent backup ID
    Path projectPath = tempDir;
    String backupId = "backup_nonexistent";

    // When/Then: Should throw exception
    BackupService.BackupException exception = assertThrows(
        BackupService.BackupException.class,
        () -> backupService.restoreBackup(projectPath, backupId));

    assertTrue(exception.getMessage().contains("Backup not found"));
  }

  @Test
  void deleteBackup_shouldRemoveBackupDirectory() throws IOException {
    // Given: Backup exists
    Path projectPath = tempDir;
    Path file1 = projectPath.resolve("src/main/File1.java");

    Files.createDirectories(file1.getParent());
    Files.writeString(file1, "content1");

    List<Path> filesToBackup = List.of(Path.of("src/main/File1.java"));
    String backupId = backupService.createBackup(projectPath, filesToBackup);

    Path backupPath = projectPath.resolve(".cleanarch/backups").resolve(backupId);
    assertTrue(Files.exists(backupPath));

    // When: Deleting backup
    backupService.deleteBackup(projectPath, backupId);

    // Then: Backup directory should be removed
    assertFalse(Files.exists(backupPath));
  }

  @Test
  void deleteBackup_shouldNotThrowExceptionForNonExistentBackup() {
    // Given: Non-existent backup ID
    Path projectPath = tempDir;
    String backupId = "backup_nonexistent";

    // When/Then: Should not throw exception
    assertDoesNotThrow(() -> backupService.deleteBackup(projectPath, backupId));
  }

@Test
  void backupAndRestore_shouldHandleNestedDirectories() throws IOException {
    // Given: Files in nested directories
    Path projectPath = tempDir;
    Path nestedFile = projectPath.resolve("src/main/java/com/example/File.java");
    
    Files.createDirectories(nestedFile.getParent());
    Files.writeString(nestedFile, "nested content");

    List<Path> filesToBackup = List.of(Path.of("src/main/java/com/example/File.java"));

    // When: Creating and restoring backup
    String backupId = backupService.createBackup(projectPath, filesToBackup);
    Files.writeString(nestedFile, "modified content");
    backupService.restoreBackup(projectPath, backupId);

    // Then: File should be restored
    assertEquals("nested content", Files.readString(nestedFile));
  }

  @Test
  void createBackup_shouldGenerateUniqueBackupIds() throws IOException {
    // Given: Project with file
    Path projectPath = tempDir;
    Path file1 = projectPath.resolve("File.java");
    Files.writeString(file1, "content");

    List<Path> filesToBackup = List.of(Path.of("File.java"));

    // When: Creating multiple backups
    String backupId1 = backupService.createBackup(projectPath, filesToBackup);
    String backupId2 = backupService.createBackup(projectPath, filesToBackup);

    // Then: Backup IDs should be different
    assertNotEquals(backupId1, backupId2);
  }

  /**
   * Test implementation of FileSystemPort that uses real file system.
   */
  private static class TestFileSystemPort implements FileSystemPort {

    @Override
    public void writeFile(com.pragma.archetype.domain.model.GeneratedFile file) {
      try {
        Path parent = file.path().getParent();
        if (parent != null && !Files.exists(parent)) {
          Files.createDirectories(parent);
        }
        Files.writeString(file.path(), file.content());
      } catch (IOException e) {
        throw new FileWriteException("Failed to write file: " + file.path(), e);
      }
    }

    @Override
    public void writeFiles(List<com.pragma.archetype.domain.model.GeneratedFile> files) {
      files.forEach(this::writeFile);
    }

    @Override
    public void createDirectory(Path path) {
      try {
        if (!Files.exists(path)) {
          Files.createDirectories(path);
        }
      } catch (IOException e) {
        throw new FileWriteException("Failed to create directory: " + path, e);
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
      return false;
    }

    @Override
    public List<Path> listFiles(Path path) {
      try {
        return Files.list(path).toList();
      } catch (IOException e) {
        throw new FileWriteException("Failed to list files: " + path, e);
      }
    }

    @Override
    public String readFile(Path path) {
      try {
        return Files.readString(path);
      } catch (IOException e) {
        throw new FileWriteException("Failed to read file: " + path, e);
      }
    }

    @Override
    public void appendToFile(Path path, String content) {
      try {
        String existing = readFile(path);
        Files.writeString(path, existing + content);
      } catch (IOException e) {
        throw new FileWriteException("Failed to append to file: " + path, e);
      }
    }
  }
}
