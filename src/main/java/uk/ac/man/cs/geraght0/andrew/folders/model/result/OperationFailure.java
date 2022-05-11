package uk.ac.man.cs.geraght0.andrew.folders.model.result;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

@Getter
public class OperationFailure extends OperationResult {

  private final Throwable failure;

  public OperationFailure(final File location, Throwable failure) {
    super(location);
    this.failure = failure;
  }

  @Override
  public String getResultDescription() {
    return String.format("Failed: %s", ObjectUtils.defaultIfNull(failure.getMessage(), "Unknown Error"));
  }

  @Override
  protected Map<String, Object> generateToStringVariableMap() {
    Map<String, Object> map = super.generateToStringVariableMap();
    map.put("failureMessage", failure.getMessage());
    return map;
  }

  // Custom logic for checking Throwable
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final OperationFailure that = (OperationFailure) o;
    boolean equals = super.equals(o);
    if (!equals) {
      return false;
    }

    if (ObjectUtils.allNull(failure, that.failure)) {
      return true;
    } else if (ObjectUtils.allNotNull(failure, that.failure)) {
      boolean classMatch = failure.getClass().equals(that.failure.getClass());
      boolean msgMatch = Objects.equals(failure.getMessage(), that.failure.getMessage());
      return classMatch && msgMatch;
    } else {
      return false;
    }
  }

  @Override
  protected String[] getExcludeEqualsFields() {
    return new String[]{"failure"};
  }
}
