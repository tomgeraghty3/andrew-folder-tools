package uk.ac.man.cs.geraght0.andrew.model;

import java.util.List;
import lombok.Value;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;

/**
 * Represents the creation of a directory and the configured subdirectories
 */
@Value
public class FolderCreateResult {

  OperationResult dirCreateResult;
  List<OperationResult> subDirectoriesCreateResult;
}