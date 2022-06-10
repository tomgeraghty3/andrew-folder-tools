package uk.ac.man.cs.geraght0.andrew.model.result;

import java.io.File;
import java.util.Map;

public class OperationSkipped extends OperationResult {   //NOSONAR - not needed as super equals uses reflection

  public static final String DIR_NOT_CREATED = "due to the parent directory failing";
  public static final String DIR_EMPTY = "due to there being 0 files to organise";
  private final String detail;

  public OperationSkipped(final File location, final String detail) {
    super(location);
    this.detail = detail;
  }

  @Override
  public String getResultDescription() {
    return String.format("Skipped %s", detail);
  }

  @Override
  public ResultIcon getResultIcon() {
    return ResultIcon.WARNING;
  }

  @Override
  protected Map<String, Object> generateToStringVariableMap() {
    final Map<String, Object> map = super.generateToStringVariableMap();
    map.put("detail", detail);
    return map;
  }

  @Override
  public File getDirectoryToOpenOnAction() {
    return null;
  }
}
