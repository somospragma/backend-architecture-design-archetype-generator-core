package com.pragma.archetype.domain.port.out;

import java.nio.file.Path;
import java.util.List;

import com.pragma.archetype.domain.model.file.GeneratedFile;

/**
 * Port for file system operations.
 * This is an output port (drives file system).
 */
public interface FileSystemPort {

  /**
   * Exception thrown when file operations fail.
   */
  class FileWriteException extends RuntimeException {
    public FileWriteException(String message) {
      super(message);
    }

    public FileWriteException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Writes a file to the file system.
   *
   * @param file the file to write
   * @throws FileWriteException if writing fails
   */
  void writeFile(GeneratedFile file);

  /**
   * Writes multiple files to the file system.
   *
   * @param files the files to write
   * @throws FileWriteException if writing fails
   */
  void writeFiles(List<GeneratedFile> files);

  /**
   * Creates a directory.
   *
   * @param path the directory path
   * @throws FileWriteException if creation fails
   */
  void createDirectory(Path path);

  /**
   * Checks if a path exists.
   *
   * @param path the path to check
   * @return true if exists, false otherwise
   */
  boolean exists(Path path);

  /**
   * Checks if a directory exists.
   *
   * @param path the directory path
   * @return true if exists and is a directory, false otherwise
   */
  boolean directoryExists(Path path);

  /**
   * Checks if a directory is empty (only contains Gradle files).
   *
   * @param path the directory path
   * @return true if empty or only contains allowed files, false otherwise
   */
  boolean isProjectEmpty(Path path);

  /**
   * Lists files in a directory.
   *
   * @param path the directory path
   * @return list of file paths
   */
  List<Path> listFiles(Path path);

  /**
   * Reads the content of a file.
   *
   * @param path the file path
   * @return the file content as string
   * @throws FileWriteException if reading fails
   */
  String readFile(Path path);

  /**
   * Appends content to an existing file.
   *
   * @param path    the file path
   * @param content the content to append
   * @throws FileWriteException if appending fails
   */
  void appendToFile(Path path, String content);
}
