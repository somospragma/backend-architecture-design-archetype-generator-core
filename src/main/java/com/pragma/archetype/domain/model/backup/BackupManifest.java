package com.pragma.archetype.domain.model.backup;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Represents a backup manifest that tracks all operations performed during
 * adapter generation. Used to restore the project to its previous state
 * if generation fails.
 *
 * @param id        unique identifier for this backup
 * @param timestamp when the backup was created
 * @param backupDir directory where backup files are stored
 * @param entries   list of backup entries (files backed up or created)
 */
public record BackupManifest(
    String id,
    Instant timestamp,
    Path backupDir,
    List<BackupEntry> entries) {

  /**
   * Represents a single file operation tracked by the backup.
   *
   * @param originalPath path of the original file in the project
   * @param backupPath   path where the backup copy is stored (null for new files)
   * @param type         type of operation (MODIFIED or CREATED)
   */
  public record BackupEntry(
      Path originalPath,
      Path backupPath,
      BackupEntryType type) {
  }

  /**
   * Type of backup entry.
   */
  public enum BackupEntryType {
    /** File existed before generation and was modified. Backup contains original content. */
    MODIFIED,
    /** File was newly created during generation. Rollback should delete it. */
    CREATED
  }
}
