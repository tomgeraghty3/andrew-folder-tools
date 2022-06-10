package uk.ac.man.cs.geraght0.andrew.model.result;

import java.io.File;
import java.nio.charset.StandardCharsets;
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

  protected OperationResult(final File location) {
    this.location = location.getAbsoluteFile();
  }

  public abstract String getResultDescription();

  public ResultIcon getResultIcon() {
    return ResultIcon.TICK; //Default
  }

  public Optional<OperationFailure> isFailed() {
    return checkType(OperationFailure.class);
  }

  public Optional<OperationSkipped> isSkipped() {
    return checkType(OperationSkipped.class);
  }

  public Optional<OperationNotNeeded> isNotNeeded() {
    return checkType(OperationNotNeeded.class);
  }

  public Optional<OperationNotApplicable> isNotApplicable() {
    return checkType(OperationNotApplicable.class);
  }

  public Optional<OperationDirCreate> isCreateDirSuccess() {
    return checkType(OperationDirCreate.class);
  }

  private <C extends OperationResult> Optional<C> checkType(Class<C> typeToLookFor) {
    if (typeToLookFor.isAssignableFrom(this.getClass())) {
      return Optional.of(typeToLookFor.cast(this));
    }

    return Optional.empty();
  }

  public File getDirectoryToOpenOnAction() {
    return location;
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
    return null;    //NOSONAR - no need to make an empty array here, the using method can accept nulls
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  protected Map<String, Object> generateToStringVariableMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("path", location.getAbsolutePath());
    return map;
  }

  public enum ResultIcon {
    TICK(new String(new byte[]{(byte) 0xE2, (byte) 0x9C, (byte) 0x85}, StandardCharsets.UTF_8)),
    CROSS(new String(new byte[]{(byte) 0xE2, (byte) 0x9D, (byte) 0x8C}, StandardCharsets.UTF_8)),
    WARNING(new String(new byte[]{(byte) 0xE2, (byte) 0x9D, (byte) 0x97}, StandardCharsets.UTF_8));

    @Getter
    private final String icon;

    ResultIcon(final String icon) {
      this.icon = icon;
    }
  }
}