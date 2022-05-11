package uk.ac.man.cs.geraght0.andrew.folders.model;

import java.io.File;
import java.util.List;
import lombok.Value;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationResult;

/**
 * Represents a request to allocate the files in a specific directory to their configured directories
 */
@Value
public class FilesAllocateRequestResult {
  File directory;                             //The directory containing the files to allocate to the subdirectories
  FolderCreateResult folderCreateResult;      //The result of the creation of the subdirectories (if they don't exist etc.)
  List<OperationResult> fileResults;          //The result of each of the files being allocated
}
