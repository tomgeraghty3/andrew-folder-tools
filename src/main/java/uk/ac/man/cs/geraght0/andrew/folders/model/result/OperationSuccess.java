package uk.ac.man.cs.geraght0.andrew.folders.model.result;

import java.io.File;

public class OperationSuccess extends OperationResult {

  public OperationSuccess(final File location) {
    super(location);
  }

  @Override
  public String getResultDescription() {
    return "Success";
  }
}
