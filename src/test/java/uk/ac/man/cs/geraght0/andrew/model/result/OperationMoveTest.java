package uk.ac.man.cs.geraght0.andrew.model.result;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.Test;

class OperationMoveTest {
  @Test
  void testEquals_whenDirMoveToMatch_true() {
    OperationMove one = new OperationMove(new File("."), new File("other"));
    OperationMove two = new OperationMove(new File("."), new File("other"));
    assertThat(one).isEqualTo(two);
  }

  @Test
  void testEquals_whenDirMoveToDoNotMatch_false() {
    final File loc = new File(".");
    OperationMove one = new OperationMove(loc, new File("/other"));
    OperationMove two = new OperationMove(loc, new File("/otherpath"));
    assertThat(one).isNotEqualTo(two);
  }
}