package uk.ac.man.cs.geraght0.andrew.model.result;

import java.io.File;

public class OperationNotApplicable extends OperationResult {

  public OperationNotApplicable(final File location) {
    super(location);
  }

  @Override
  public String getResultDescription() {
    return "File Does Not Match Any Pattern";
  }

  @Override
  public File getDirectoryToOpenOnAction() {
    File path = getLocation();
    if (!path.isDirectory()) {
      return path.getParentFile();
    }

    return path;
  }

  @Override
  public ResultIcon getResultIcon() {
    return ResultIcon.WARNING;
  }
}
