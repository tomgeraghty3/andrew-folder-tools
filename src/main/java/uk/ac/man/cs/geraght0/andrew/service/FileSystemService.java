package uk.ac.man.cs.geraght0.andrew.service;

import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationDirCreate;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationMove;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationNotNeeded;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileSystemService {

  private final Config config;

  /**
   * If the specified directory does not exist, create it and return a {@link OperationDirCreate} state if the creation
   * operation is successful or a {@link OperationFailure} state if it fails. If it already exists then do nothing
   * and return a {@link OperationNotNeeded} state.
   *
   * @param directory The directory to create if not exists
   * @return An {@link OperationResult} describing the result of the operation - either {@link OperationNotNeeded} if the directory
   * already exists, {@link OperationDirCreate} if the creation succeeded or {@link OperationFailure} if the creation failed
   */
  public OperationResult createDirectoryIfNotExist(final File directory) {
    if (directory.exists()) {
      log.debug("Directory already exists: {}", directory.getAbsolutePath());
      return new OperationNotNeeded(directory);
    } else {
      try {
        FileUtils.forceMkdir(directory);
        log.debug("Directory created: {}", directory.getAbsolutePath());
        return new OperationDirCreate(directory);
      } catch (IOException e) {
        log.error("Could not create directory: {}", directory.getAbsolutePath(), e);
        return new OperationFailure(directory, e);
      }
    }
  }

  /**
   * Move the specified file to the specified directory
   *
   * @param fileToMove  The file to move
   * @param dirToMoveTo The directory to move the file to
   * @return An {@link OperationResult} describing the result of the operation - either {@link OperationDirCreate}
   * if the move succeeded or {@link OperationFailure} if the move failed
   */
  public OperationResult moveFile(File fileToMove, File dirToMoveTo) {
    log.debug("Moving {} to {}", fileToMove.getName(), dirToMoveTo.getAbsolutePath());
    try {
      File dest = new File(dirToMoveTo, fileToMove.getName());
      if (dest.exists() && !config.isDisallowOverwrite()) {
        log.info("There is already a file in the destination. Overwriting it");
        FileUtils.forceDelete(dest);
      }

      FileUtils.moveFile(fileToMove, dest);
      return new OperationMove(fileToMove, dirToMoveTo);
    } catch (IOException e) {
      log.error("Could not move file from {} to {}", fileToMove.getAbsolutePath(), dirToMoveTo.getAbsolutePath(), e);
      return new OperationFailure(fileToMove, e);
    }
  }
}
