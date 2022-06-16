package uk.ac.man.cs.geraght0.andrew.model;

import java.util.List;
import lombok.Value;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;

/**
 * Represents the creation of a directory and the configured subdirectories
 */
@Value
public class FolderCreateResult implements Comparable<FolderCreateResult> {

  OperationResult dirCreateResult;
  List<OperationResult> subDirectoriesCreateResult;

  @Override
  public int compareTo(final FolderCreateResult o) {
    return dirCreateResult.compareTo(o.getDirCreateResult());
  }
}