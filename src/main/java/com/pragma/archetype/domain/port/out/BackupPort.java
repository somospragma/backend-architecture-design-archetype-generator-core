package com.pragma.archetype.domain.port.out;

import java.nio.file.Path;
import java.util.List;

import com.pragma.archetype.domain.model.backup.BackupManifest;

/**
 * Port for backup and restore operations during code generation.
 * This is an output port that provides rollback capability when generation fails.
 *
 * <p>The backup lifecycle is:
 * <ol>
 *   <li>{@link #createBackup(Path, List)} — Before generation, back up files that will be modified</li>
 *   <li>Generation happens (may succeed or fail)</li>
 *   <li>{@link #confirmBackup(BackupManifest)} — On success, delete backup files</li>
 *   <li>{@link #restoreBackup(BackupManifest)} — On failure, restore original files</li>
 * </ol>
 *
 * <p>Additionally, files created during generation can be tracked via
 * {@link #trackCreatedFile(BackupManifest, Path)} so they can be deleted on rollback.
 */
public interface BackupPort {

  /**
   * Exception thrown when backup operations fail.
   */
  class BackupException extends RuntimeException {
    public BackupException(String message) {
      super(message);
    }

    public BackupException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Creates a backup of the specified files before they are modified.
   * Copies each existing file to a temporary backup directory.
   *
   * @param projectPath    the root directory of the project
   * @param filesToBackup  list of file paths (relative or absolute) to back up
   * @return a backup manifest that can be used to restore or confirm
   * @throws BackupException if backup creation fails
   */
  BackupManifest createBackup(Path projectPath, List<Path> filesToBackup);

  /**
   * Tracks a newly created file so it can be deleted during rollback.
   * Should be called after each file is written during generation.
   *
   * @param manifest    the active backup manifest
   * @param createdFile path to the file that was just created
   * @return updated manifest with the new entry
   */
  BackupManifest trackCreatedFile(BackupManifest manifest, Path createdFile);

  /**
   * Restores the project to its state before generation.
   * - Files that were modified are restored from backup copies
   * - Files that were newly created are deleted
   *
   * @param manifest the backup manifest to restore from
   * @throws BackupException if restoration fails (project may be in inconsistent state)
   */
  void restoreBackup(BackupManifest manifest);

  /**
   * Confirms that generation was successful and cleans up backup files.
   * After this call, rollback is no longer possible for this manifest.
   *
   * @param manifest the backup manifest to confirm and clean up
   */
  void confirmBackup(BackupManifest manifest);
}
