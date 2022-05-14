package uk.ac.man.cs.geraght0.andrew.model.result;

import java.io.File;

/**
 * Used when creating a directory
 */
public class OperationDirCreate extends OperationResult {

  public OperationDirCreate(final File location) {
    super(location);
  }

  @Override
  public String getResultDescription() {
    return "Directory created";
  }
}
