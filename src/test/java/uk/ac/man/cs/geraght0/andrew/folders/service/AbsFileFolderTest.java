package uk.ac.man.cs.geraght0.andrew.folders.service;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.man.cs.geraght0.andrew.folders.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationMove;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationNotNeeded;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationSuccess;
import uk.ac.man.cs.geraght0.andrew.models.config.FolderConfig;

@ExtendWith(MockitoExtension.class)
abstract class AbsFileFolderTest<T> {
  protected static final RuntimeException SIMULATED_E = new IllegalArgumentException("Simulate");
  protected static final File DIR = new File("src/test/resources/test-dir").getAbsoluteFile();
  protected static final String SUB_DIR_ONE_NAME = "one";
  protected static final String SUB_DIR_TWO_NAME = "two";
  protected static final List<String> SUB_DIR_NAMES = Lists.newArrayList(SUB_DIR_ONE_NAME, SUB_DIR_TWO_NAME);

  protected T classUnderTest;

  @Mock
  protected FileSystemService fileSystemService;
  @Mock
  protected FolderConfig config;

  @BeforeAll
  static void beforeAll() throws IOException {
    if (!DIR.exists()) {
      FileUtils.forceMkdir(DIR);
    }
  }

  @BeforeEach
  void before() {
    lenient().when(config.deduceSubDirectoryNames())
             .thenReturn(new LinkedHashSet<>(SUB_DIR_NAMES));
    this.classUnderTest = createClassUnderTestInstance(config, fileSystemService);
  }

  protected abstract T createClassUnderTestInstance(final FolderConfig config, final FileSystemService fileSystemService);

  protected OperationFailure newOpFail(File location) {
    return new OperationFailure(location, SIMULATED_E);
  }

  protected OperationResult newOpSuccess(final File location) {
    return new OperationSuccess(location);
  }

  protected OperationResult newOpNotNeededForNewDir(final File location) {
    return new OperationNotNeeded(location, OperationNotNeeded.DIR_ALREADY_EXISTS_DESC);
  }

  protected OperationResult newOpNotNeededForFileAllocate(final File location) {
    return new OperationNotNeeded(location, OperationNotNeeded.FILE_NOT_MATCH_PATTERN_DESC);
  }

  protected OperationResult newOpMove(final File location, final File dirMovedTo) {
    return new OperationMove(location, dirMovedTo);
  }

  protected FolderCreateResult createFolderCreateResult(final OperationResult dirResult, final List<OperationResult> subDirResult) {
    return new FolderCreateResult(dirResult, subDirResult);
  }

  protected <R extends OperationResult> List<OperationResult> createSubDirsResult(final File topDir, final Function<File, R> resultFunction) {
    return createSubDirsResult(topDir, SUB_DIR_NAMES, resultFunction);
  }

  protected <R extends OperationResult> List<OperationResult> createSubDirsResult(final File topDir, final List<String> subDirNames, final Function<File, R> resultFunction) {
    return subDirNames.stream()
                      .map(sub -> {
                        File file = new File(topDir, sub);
                        return resultFunction.apply(file);
                      })
                      .collect(Collectors.toList());
  }

  protected OperationResult mockDirCreate(final File location, final Function<File, OperationResult> stateFunc) {
    OperationResult state = stateFunc.apply(location);
    when(fileSystemService.createDirectoryIfNotExist(location)).thenReturn(state);
    return state;
  }

  @AfterEach
  void after() throws IOException {
    File[] files = DIR.listFiles();
    if (files != null) {
      for (File f : files) {
        FileUtils.forceDelete(f);
      }
    }
  }

  @AfterAll
  static void afterAll() throws IOException {
    FileUtils.deleteDirectory(DIR);
  }
}
