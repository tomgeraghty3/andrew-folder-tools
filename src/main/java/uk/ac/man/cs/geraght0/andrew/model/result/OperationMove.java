package uk.ac.man.cs.geraght0.andrew.model.result;

import java.io.File;
import java.util.Map;

public class OperationMove extends OperationResult {
  private final File dirMovedTo;

  public OperationMove(final File location, final File dirMovedTo) {
    super(location);
    this.dirMovedTo = dirMovedTo;
  }

  @Override
  public String getResultDescription() {
    return String.format("File moved to directory \"%s\"", dirMovedTo.getName());
  }

  @Override
  protected Map<String, Object> generateToStringVariableMap() {
    Map<String, Object> variableMap = super.generateToStringVariableMap();
    variableMap.put("dirMovedTo", dirMovedTo.getName());
    return variableMap;
  }

  @Override
  public File getDirectoryToOpenOnAction() {
    return dirMovedTo;
  }
}
