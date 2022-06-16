package uk.ac.man.cs.geraght0.andrew.model;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
@Value
public class DirectoryCriteria {

  String dirToMoveTo;
  String endsWith;
  String contains;

  public DirectoryCriteria(final String dirToMoveTo, final String endsWith) {
    this.dirToMoveTo = dirToMoveTo;
    this.endsWith = endsWith;
    this.contains = null;
  }

  public Optional<String> getContainsOp() {
    return Optional.ofNullable(contains);
  }

  @Override
  public String toString() {
    return "Move to \"" + dirToMoveTo + "\" if " +
           " ends with=\"" + endsWith + '\"' +
           getContainsOp().map(s -> " and contains \"" + s + "\" anywhere in name")
                          .orElse("");
  }
}
