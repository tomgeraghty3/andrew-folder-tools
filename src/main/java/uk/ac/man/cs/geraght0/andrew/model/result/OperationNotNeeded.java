package uk.ac.man.cs.geraght0.andrew.model.result;

import java.io.File;

public class OperationNotNeeded extends OperationResult {

  public OperationNotNeeded(final File location) {
    super(location);
  }

  @Override
  public String getResultDescription() {
    return "Directory Already Exists";
  }

  @Override
  public File getDirectoryToOpenOnAction() {
    File path = getLocation();
    if (!path.isDirectory()) {
      return path.getParentFile();
    }

    return path;
  }
}
