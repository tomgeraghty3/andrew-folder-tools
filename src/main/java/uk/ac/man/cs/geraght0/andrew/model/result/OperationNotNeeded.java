package uk.ac.man.cs.geraght0.andrew.model.result;

import java.io.File;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class OperationNotNeeded extends OperationResult {
  public static final String FILE_NOT_MATCH_PATTERN_DESC = "File Does Not Match Any Pattern";
  public static final String DIR_ALREADY_EXISTS_DESC = "Directory Already Exists";

  private final String description;

  public OperationNotNeeded(final File location, final String description) {
    super(location);
    this.description = StringUtils.isBlank(description) ? DIR_ALREADY_EXISTS_DESC : description;
  }

  @Override
  public String getResultDescription() {
    return description;
  }

  @Override
  protected Map<String, Object> generateToStringVariableMap() {
    Map<String, Object> variableMap = super.generateToStringVariableMap();
    variableMap.put("description", description);
    return variableMap;
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
