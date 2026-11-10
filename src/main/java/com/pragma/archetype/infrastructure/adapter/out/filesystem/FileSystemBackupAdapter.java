package com.pragma.archetype.infrastructure.adapter.out.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.domain.model.backup.BackupManifest;
import com.pragma.archetype.domain.model.backup.BackupManifest.BackupEntry;
import com.pragma.archetype.domain.model.backup.BackupManifest.BackupEntryType;
import com.pragma.archetype.domain.port.out.BackupPort;

/**
 * File system implementation of BackupPort.
 * Stores backup copies in a temporary directory under .cleanarch/backups/.
 *
 * <p>Backup directory structure:
 * <pre>
 * .cleanarch/backups/{uuid}/
 * ├── manifest.json          (not implemented yet, manifest is in-memory)
 * ├── application.yml        (backup of modified file)
 * └── build.gradle.kts       (backup of modified file)
 * </pre>
 */
public class FileSystemBackupAdapter implements BackupPort {

  private static final Logger logger = LoggerFactory.getLogger(FileSystemBackupAdapter.class);
  private static final String BACKUP_DIR_NAME = ".cleanarch/backups";

  @Override
  public BackupManifest createBackup(Path projectPath, List<Path> filesToBackup) {
    String backupId = UUID.randomUUID().toString().substring(0, 8);
    Path backupDir = projectPath.resolve(BACKUP_DIR_NAME).resolve(backupId);

    try {
      Files.createDirectories(backupDir);
    } catch (IOException e) {
      throw new BackupException("Failed to create backup directory: " + backupDir, e);
    }

    List<BackupEntry> entries = new ArrayList<>();

    for (Path fileToBackup : filesToBackup) {
      Path absolutePath = fileToBackup.isAbsolute() ? fileToBackup : projectPath.resolve(fileToBackup);

      if (Files.exists(absolutePath)) {
        try {
          // Preserve relative structure in backup dir
          Path relativePath = projectPath.relativize(absolutePath);
          Path backupFilePath = backupDir.resolve(relativePath);

          // Create parent directories in backup
          Files.createDirectories(backupFilePath.getParent());

          // Copy file to backup
          Files.copy(absolutePath, backupFilePath, StandardCopyOption.REPLACE_EXISTING);

          entries.add(new BackupEntry(absolutePath, backupFilePath, BackupEntryType.MODIFIED));
          logger.debug("Backed up file: {} -> {}", absolutePath, backupFilePath);
        } catch (IOException e) {
          // Clean up partial backup on failure
          cleanupDirectory(backupDir);
          throw new BackupException("Failed to backup file: " + absolutePath, e);
        }
      }
    }

    logger.info("Created backup '{}' with {} file(s)", backupId, entries.size());
    return new BackupManifest(backupId, Instant.now(), backupDir, entries);
  }

  @Override
  public BackupManifest trackCreatedFile(BackupManifest manifest, Path createdFile) {
    List<BackupEntry> updatedEntries = new ArrayList<>(manifest.entries());
    updatedEntries.add(new BackupEntry(createdFile, null, BackupEntryType.CREATED));
    logger.debug("Tracking created file for rollback: {}", createdFile);
    return new BackupManifest(manifest.id(), manifest.timestamp(), manifest.backupDir(), updatedEntries);
  }

  @Override
  public void restoreBackup(BackupManifest manifest) {
    logger.warn("Restoring backup '{}' — rolling back generation", manifest.id());

    List<String> errors = new ArrayList<>();

    for (BackupEntry entry : manifest.entries()) {
      try {
        switch (entry.type()) {
          case MODIFIED -> {
            // Restore original file from backup copy
            if (entry.backupPath() != null && Files.exists(entry.backupPath())) {
              Files.createDirectories(entry.originalPath().getParent());
              Files.copy(entry.backupPath(), entry.originalPath(), StandardCopyOption.REPLACE_EXISTING);
              logger.debug("Restored file: {}", entry.originalPath());
            }
          }
          case CREATED -> {
            // Delete file that was created during generation
            if (Files.exists(entry.originalPath())) {
              Files.deleteIfExists(entry.originalPath());
              logger.debug("Deleted created file: {}", entry.originalPath());

              // Clean up empty parent directories (but not project root)
              cleanupEmptyParents(entry.originalPath().getParent(), manifest.backupDir().getParent().getParent());
            }
          }
        }
      } catch (IOException e) {
        errors.add("Failed to restore " + entry.originalPath() + ": " + e.getMessage());
        logger.error("Failed to restore file during rollback: {}", entry.originalPath(), e);
      }
    }

    // Clean up backup directory
    cleanupDirectory(manifest.backupDir());

    if (!errors.isEmpty()) {
      logger.error("Backup restoration completed with {} error(s). Project may be in inconsistent state.", errors.size());
      throw new BackupException("Partial restoration failure: " + String.join("; ", errors));
    }

    logger.info("Backup '{}' restored successfully — {} file(s) rolled back", manifest.id(), manifest.entries().size());
  }

  @Override
  public void confirmBackup(BackupManifest manifest) {
    logger.debug("Confirming backup '{}' — generation successful, cleaning up", manifest.id());
    cleanupDirectory(manifest.backupDir());

    // Try to clean up the parent backups directory if empty
    try {
      Path backupsDir = manifest.backupDir().getParent();
      if (backupsDir != null && Files.exists(backupsDir) && isDirectoryEmpty(backupsDir)) {
        Files.deleteIfExists(backupsDir);
      }
    } catch (IOException e) {
      // Non-critical: backup dir cleanup is best-effort
      logger.debug("Could not clean up empty backups directory: {}", e.getMessage());
    }
  }

  /**
   * Recursively deletes a directory and all its contents.
   */
  private void cleanupDirectory(Path directory) {
    if (directory == null || !Files.exists(directory)) {
      return;
    }

    try (var walk = Files.walk(directory)) {
      walk.sorted(java.util.Comparator.reverseOrder())
          .forEach(path -> {
            try {
              Files.deleteIfExists(path);
            } catch (IOException e) {
              logger.debug("Failed to delete backup file: {}", path);
            }
          });
    } catch (IOException e) {
      logger.debug("Failed to walk backup directory for cleanup: {}", directory);
    }
  }

  /**
   * Removes empty parent directories up to (but not including) the stop directory.
   */
  private void cleanupEmptyParents(Path directory, Path stopAt) {
    Path current = directory;
    while (current != null && !current.equals(stopAt)) {
      try {
        if (Files.exists(current) && isDirectoryEmpty(current)) {
          Files.deleteIfExists(current);
          current = current.getParent();
        } else {
          break;
        }
      } catch (IOException e) {
        break;
      }
    }
  }

  /**
   * Checks if a directory is empty.
   */
  private boolean isDirectoryEmpty(Path directory) throws IOException {
    try (var entries = Files.list(directory)) {
      return entries.findFirst().isEmpty();
    }
  }
}
