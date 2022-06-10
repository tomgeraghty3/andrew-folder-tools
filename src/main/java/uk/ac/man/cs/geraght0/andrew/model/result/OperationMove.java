package uk.ac.man.cs.geraght0.andrew.model.result;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;

public class OperationMove extends OperationResult {    //NOSONAR - equals() in super uses reflection

  @Getter
  private final File dirMovedTo;

  public OperationMove(final File location, final File dirMovedTo) {
    super(location);
    this.dirMovedTo = dirMovedTo;
  }

  @Override
  public String getResultDescription() {
    File dirStop = getLocation().getParentFile();
    List<String> names = new ArrayList<>();
    File current = dirMovedTo;
    while (!current.equals(dirStop)) {
      names.add(current.getName());
      current = current.getParentFile();
      if (current == null) {
        throw new IllegalStateException("Cannot find the matching directory between " + dirMovedTo.getAbsolutePath() + " & " + getLocation().getAbsolutePath());
      }
    }

    Collections.reverse(names);
    String movedTo = String.join(File.separator, names);

    return String.format("File Moved To Directory \"%s\"", movedTo);
  }

  @Override
  protected Map<String, Object> generateToStringVariableMap() {
    Map<String, Object> variableMap = super.generateToStringVariableMap();
    variableMap.put("dirMovedTo", dirMovedTo);
    return variableMap;
  }

  @Override
  public File getDirectoryToOpenOnAction() {
    return dirMovedTo;
  }
}
