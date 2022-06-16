package uk.ac.man.cs.geraght0.andrew.model;

import java.io.File;
import java.util.List;
import lombok.Value;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;

/**
 * Represents a request to organise the files in a specific directory to their configured directories
 */
@Value
public class FilesOrganiseResult implements Comparable<FilesOrganiseResult> {

  File directory;                             //The directory containing the files to organise into the subdirectories
  FolderCreateResult folderCreateResult;      //The result of the creation of the subdirectories (if they don't exist etc.)
  List<OperationResult> fileResults;          //The result of each of the files being organised

  @Override
  public int compareTo(final FilesOrganiseResult o) {
    return directory.compareTo(o.directory);
  }
}
