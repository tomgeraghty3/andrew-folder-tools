package uk.ac.man.cs.geraght0.andrew;

import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class AbsTest {

  protected static final File DIR = new File("src/test/resources/test-dir").getAbsoluteFile();

  @BeforeAll
  static void beforeAll() throws IOException {
    if (!DIR.exists()) {
      FileUtils.forceMkdir(DIR);
    }
  }

  @AfterEach
  void after() throws IOException {
    if (remove()) {
      File[] files = DIR.listFiles();
      if (files != null) {
        for (File f : files) {
          FileUtils.forceDelete(f);
        }
      }
    }
  }

  protected static boolean remove() {
    return true;
  }

  @AfterAll
  static void afterAll() throws IOException {
    if (remove()) {
      FileUtils.deleteDirectory(DIR);
    }
  }

  @SneakyThrows
  protected void createSubDirs(final List<File> subDirs) {
    for (File subDir : subDirs) {
      FileUtils.forceMkdir(subDir);
    }
  }

  protected File createFile(final String fileName) {
    return createFile(DIR, fileName);
  }

  protected File createFile(final File dir, final String fileName) {
    File file = new File(dir, fileName);
    try {
      FileUtils.touch(file);
    } catch (IOException e) {
      fail("Cannot create file: " + file.getAbsolutePath(), e);
    }
    return file;
  }
}
