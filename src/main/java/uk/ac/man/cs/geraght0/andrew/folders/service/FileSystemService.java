package uk.ac.man.cs.geraght0.andrew.folders.service;

import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationMove;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationNotNeeded;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationSuccess;

@Slf4j
@Service
public class FileSystemService {

  /**
   * If the specified directory does not exist, create it and return a {@link OperationSuccess} state if the creation
   * operation is successful or a {@link OperationFailure} state if it fails. If it already exists then do nothing
   * and return a {@link OperationNotNeeded} state.
   * @param directory The directory to create if not exists
   * @return An {@link OperationResult} describing the result of the operation - either {@link OperationNotNeeded} if the directory
   * already exists, {@link OperationSuccess} if the creation succeeded or {@link OperationFailure} if the creation failed
   */
  public OperationResult createDirectoryIfNotExist(final File directory) {
    if (directory.exists()) {
      log.debug("Directory already exists: {}", directory.getAbsolutePath());
      return new OperationNotNeeded(directory, OperationNotNeeded.DIR_ALREADY_EXISTS_DESC);
    } else {
      try {
        FileUtils.forceMkdir(directory);
        log.debug("Directory created: {}", directory.getAbsolutePath());
        return new OperationSuccess(directory);
      } catch (IOException e) {
        log.error("Could not create directory: {}", directory.getAbsolutePath(), e);
        return new OperationFailure(directory, e);
      }
    }
  }

  /**
   * Move the specified file to the specified directory
   * @param fileToMove The file to move
   * @param dirToMoveTo The directory to move the file to
   * @return An {@link OperationResult} describing the result of the operation - either {@link OperationSuccess}
   * if the move succeeded or {@link OperationFailure} if the move failed
   */
  public OperationResult moveFile(File fileToMove, File dirToMoveTo) {
    log.debug("Moving {} to {}", fileToMove.getName(), dirToMoveTo.getAbsolutePath());

    try {
      FileUtils.moveFileToDirectory(fileToMove, dirToMoveTo, false);
      return new OperationMove(fileToMove, dirToMoveTo);
    } catch (IOException e) {
      log.error("Could not move file from {} to {}", fileToMove.getAbsolutePath(), dirToMoveTo.getAbsolutePath(), e);
      return new OperationFailure(fileToMove, e);
    }
  }
}
