package uk.ac.man.cs.geraght0.andrew.model.result;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.Test;

class OperationFailureTest {

  @Test
  void testEquals_whenFailuresMatch_true() {
    final File loc = new File(".");
    OperationFailure one = new OperationFailure(loc, new IllegalStateException("msg"));
    OperationFailure two = new OperationFailure(loc, new IllegalStateException("msg"));
    assertThat(one).isEqualTo(two);
  }

  @Test
  void testEquals_whenFailuresDoNotMatch_false() {
    final File loc = new File(".");
    OperationFailure one = new OperationFailure(loc, new IllegalStateException("msg"));
    OperationFailure two = new OperationFailure(loc, new IllegalArgumentException("msg"));
    assertThat(one).isNotEqualTo(two);
  }

}