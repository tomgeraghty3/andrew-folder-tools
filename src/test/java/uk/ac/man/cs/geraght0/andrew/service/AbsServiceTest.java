package uk.ac.man.cs.geraght0.andrew.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.man.cs.geraght0.andrew.AbsTest;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationDirCreate;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationMove;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationNotApplicable;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationNotNeeded;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationSkipped;

@ExtendWith(MockitoExtension.class)
abstract class AbsServiceTest<T> extends AbsTest {

  protected static final RuntimeException SIMULATED_E = new IllegalArgumentException("Simulate");
  protected static final String SUB_DIR_ONE_NAME = "one";
  protected static final String SUB_DIR_TWO_NAME = "two";
  protected static final List<String> SUB_DIR_NAMES = Lists.newArrayList(SUB_DIR_ONE_NAME, SUB_DIR_TWO_NAME);

  protected T classUnderTest;

  @Mock
  protected FileSystemService fileSystemService;
  @Mock
  protected Config config;

  @BeforeEach
  void before() {
    mockSubDirNames(config);
    this.classUnderTest = createClassUnderTestInstance(config, fileSystemService);
  }

  protected void mockSubDirNames(final Config config) {
    lenient().when(config.deduceSubDirectoryNames(anyString()))
             .thenReturn(SUB_DIR_NAMES);
  }

  protected abstract T createClassUnderTestInstance(final Config config, final FileSystemService fileSystemService);

  protected OperationFailure newOpFail(File location) {
    return new OperationFailure(location, SIMULATED_E);
  }

  protected OperationResult newOpSuccess(final File location) {
    return new OperationDirCreate(location);
  }

  protected OperationResult newOpSkipped(final File location, String desc) {
    return new OperationSkipped(location, desc);
  }

  protected OperationResult newOpNotNeededForNewDir(final File location) {
    return new OperationNotNeeded(location);
  }

  protected OperationResult newOpNotApplicableForFile(final File location) {
    return new OperationNotApplicable(location);
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
}
