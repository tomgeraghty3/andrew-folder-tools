package uk.ac.man.cs.geraght0.andrew.folders.model.result;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
public abstract class OperationResult {

  private final File location;

  public OperationResult(final File location) {
    this.location = location.getAbsoluteFile();
  }

  public abstract String getResultDescription();

  public Optional<OperationFailure> isFailed() {
    if (OperationFailure.class.isAssignableFrom(this.getClass())) {
      return Optional.of((OperationFailure) this);
    }

    return Optional.empty();
  }

  @Override
  public String toString() {
    Class<? extends OperationResult> clazz = getClass();
    Map<String, Object> variableNameToValue = generateToStringVariableMap();
    String variableString = variableNameToValue.entrySet()
                                        .stream()
                                        .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                                        .collect(Collectors.joining(", "));
    return String.format("%s(%s)", clazz.getSimpleName(), variableString);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final OperationResult that = (OperationResult) o;
    return EqualsBuilder.reflectionEquals(this, that, getExcludeEqualsFields());
  }

  protected String[] getExcludeEqualsFields() {
    return null;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  protected Map<String, Object> generateToStringVariableMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("path", location.getName());
    return map;
  }
}