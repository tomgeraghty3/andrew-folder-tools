package uk.ac.man.cs.geraght0.andrew.it;

import org.junit.jupiter.api.Test;
import uk.ac.man.cs.geraght0.andrew.AbsSpringBootTest;

class FolderCreateIT extends AbsSpringBootTest {

  @Test
  void testE2E_whenOneContainer_shouldCreateAsExpected() {
    testSubDirCreationAndExists();
  }
}