package uk.ac.man.cs.geraght0.andrew.folders.model.result;

import java.io.File;

public class OperationSkipped extends OperationResult {

  public OperationSkipped(final File location) {
    super(location);
  }

  @Override
  public String getResultDescription() {
    return "Skipped";
  }
}
