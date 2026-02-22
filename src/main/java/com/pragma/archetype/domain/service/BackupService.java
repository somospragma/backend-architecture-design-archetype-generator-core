package com.pragma.archetype.domain.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.pragma.archetype.domain.port.out.FileSystemPort;

/**
 * Service for backing up and restoring files during generation operations.
 * Provides rollback capability if generation fails.
 */
public class BackupService {

  /**
   * Exception thrown when backup operations fail.
   */
  public static class BackupException extends RuntimeException {
    public BackupException(String message) {
      super(message);
    }

    public BackupException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private static final String BACKUP_DIR = ".cleanarch/backups";

  private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

  private final FileSystemPort fileSystemPort;

  public BackupService(FileSystemPort fileSystemPort) {
    this.fileSystemPort = fileSystemPort;
  }

  /**
   * Creates a backup of the specified files before modification.
   *
   * @param projectPath   the project root path
   * @param filesToBackup list of file paths to backup (relative to project root)
   * @return backup ID that can be used to restore or delete the backup
   * @throws BackupException if backup creation fails
   */
  public String createBackup(Path projectPath, List<Path> filesToBackup) {
    if (filesToBackup == null || filesToBackup.isEmpty()) {
      throw new BackupException("No files specified for backup");
    }

    String backupId = generateBackupId();
    Path backupPath = projectPath.resolve(BACKUP_DIR).resolve(backupId);

    try {
      // Create backup directory
      fileSystemPort.createDirectory(backupPath);

      // Backup each file
      Map<Path, Path> backupMap = new HashMap<>();
      for (Path fileToBackup : filesToBackup) {
        Path absoluteFilePath = projectPath.resolve(fileToBackup);

        // Only backup if file exists
        if (fileSystemPort.exists(absoluteFilePath)) {
          Path relativeBackupPath = backupPath.resolve(fileToBackup);
          backupFile(absoluteFilePath, relativeBackupPath);
          backupMap.put(fileToBackup, relativeBackupPath);
        }
      }

      // Store backup metadata
      storeBackupMetadata(backupPath, backupMap);

      return backupId;

    } catch (Exception e) {
      // Clean up partial backup on failure
      try {
        deleteBackupDirectory(backupPath);
      } catch (Exception cleanupException) {
        // Log but don't throw - original exception is more important
      }
      throw new BackupException("Failed to create backup: " + e.getMessage(), e);
    }
  }

  /**
   * Restores files from a backup.
   *
   * @param projectPath the project root path
   * @param backupId    the backup ID returned from createBackup()
   * @throws BackupException if restore fails
   */
  public void restoreBackup(Path projectPath, String backupId) {
    Path backupPath = projectPath.resolve(BACKUP_DIR).resolve(backupId);

    if (!fileSystemPort.directoryExists(backupPath)) {
      throw new BackupException("Backup not found: " + backupId + " at " + backupPath);
    }

    try {
      // Read backup metadata
      Map<Path, Path> backupMap = readBackupMetadata(backupPath);

      // Restore each file
      for (Map.Entry<Path, Path> entry : backupMap.entrySet()) {
        Path originalPath = projectPath.resolve(entry.getKey());
        Path backupFilePath = entry.getValue();

        if (fileSystemPort.exists(backupFilePath)) {
          restoreFile(backupFilePath, originalPath);
        }
      }

    } catch (Exception e) {
      throw new BackupException(
          "Failed to restore backup: " + backupId + ". " +
              "Manual recovery may be required. Backup location: " + backupPath,
          e);
    }
  }

  /**
   * Deletes a backup after successful generation.
   *
   * @param projectPath the project root path
   * @param backupId    the backup ID returned from createBackup()
   * @throws BackupException if deletion fails
   */
  public void deleteBackup(Path projectPath, String backupId) {
    Path backupPath = projectPath.resolve(BACKUP_DIR).resolve(backupId);

    if (!fileSystemPort.directoryExists(backupPath)) {
      // Backup doesn't exist - this is fine, nothing to delete
      return;
    }

    try {
      deleteBackupDirectory(backupPath);
    } catch (Exception e) {
      throw new BackupException("Failed to delete backup: " + backupId, e);
    }
  }

  /**
   * Generates a unique backup ID based on timestamp.
   */
  private String generateBackupId() {
    return "backup_" + LocalDateTime.now().format(TIMESTAMP_FORMAT);
  }

  /**
   * Backs up a single file to the backup location.
   */
  private void backupFile(Path source, Path destination) throws IOException {
    // Create parent directories for destination
    Path parent = destination.getParent();
    if (parent != null && !Files.exists(parent)) {
      Files.createDirectories(parent);
    }

    // Copy file
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
  }

  /**
   * Restores a single file from backup.
   */
  private void restoreFile(Path backupFile, Path destination) throws IOException {
    // Create parent directories for destination
    Path parent = destination.getParent();
    if (parent != null && !Files.exists(parent)) {
      Files.createDirectories(parent);
    }

    // Copy file back
    Files.copy(backupFile, destination, StandardCopyOption.REPLACE_EXISTING);
  }

  /**
   * Stores backup metadata (mapping of original paths to backup paths).
   */
  private void storeBackupMetadata(Path backupPath, Map<Path, Path> backupMap) throws IOException {
    StringBuilder metadata = new StringBuilder();
    metadata.append("# Backup Metadata\n");
    metadata.append("# Format: original_path|backup_path\n");

    for (Map.Entry<Path, Path> entry : backupMap.entrySet()) {
      metadata.append(entry.getKey().toString())
          .append("|")
          .append(entry.getValue().toString())
          .append("\n");
    }

    Path metadataFile = backupPath.resolve("backup_metadata.txt");

    // Atomic write: write to temporary file first, then rename
    Path tempFile = metadataFile.resolveSibling(metadataFile.getFileName() + ".tmp");
    Files.writeString(tempFile, metadata.toString());
    Files.move(tempFile, metadataFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
  }

  /**
   * Reads backup metadata.
   */
  private Map<Path, Path> readBackupMetadata(Path backupPath) throws IOException {
    Path metadataFile = backupPath.resolve("backup_metadata.txt");

    if (!Files.exists(metadataFile)) {
      throw new BackupException("Backup metadata not found: " + metadataFile);
    }

    Map<Path, Path> backupMap = new HashMap<>();
    List<String> lines = Files.readAllLines(metadataFile);

    for (String line : lines) {
      // Skip comments and empty lines
      if (line.startsWith("#") || line.trim().isEmpty()) {
        continue;
      }

      String[] parts = line.split("\\|");
      if (parts.length == 2) {
        backupMap.put(Path.of(parts[0]), Path.of(parts[1]));
      }
    }

    return backupMap;
  }

  /**
   * Recursively deletes a backup directory.
   */
  private void deleteBackupDirectory(Path directory) throws IOException {
    if (!Files.exists(directory)) {
      return;
    }

    try (Stream<Path> walk = Files.walk(directory)) {
      walk.sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete files before directories
          .forEach(path -> {
            try {
              Files.delete(path);
            } catch (IOException e) {
              throw new RuntimeException("Failed to delete: " + path, e);
            }
          });
    }
  }
}
