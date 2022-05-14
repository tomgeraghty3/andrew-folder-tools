package uk.ac.man.cs.geraght0.andrew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.FoldersCreateRequestResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationSkipped;
import uk.ac.man.cs.geraght0.andrew.constans.ErrorMessages;
import uk.ac.man.cs.geraght0.andrew.models.config.FolderConfig;

class FolderServiceTest extends AbsFileFolderTest<FolderService> {

  private static final String TOP_DIR_NAME = "topLevelDir";
  private static final File TOP_DIR = new File(DIR, TOP_DIR_NAME).getAbsoluteFile();
  private static final List<String> DIRS_TO_CREATE = Lists.newArrayList(TOP_DIR_NAME);
  private static final List<File> SUB_DIRS = FileFolderHelpers.toSubDirectories(TOP_DIR, SUB_DIR_NAMES);

  @Override
  protected FolderService createClassUnderTestInstance(final FolderConfig config, final FileSystemService fileSystemService) {
    return new FolderService(config, fileSystemService);
  }

  @Test
  void testCreate_whenDirectoryNull_exception() {
    assertThatThrownBy(() -> classUnderTest.createDirectories(null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.DIR_NULL.generateMsg());
  }

  @Test
  void testCreate_whenNoDirNames_exception() {
    assertThatThrownBy(() -> classUnderTest.createDirectories(DIR, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.DIR_NAMES_TO_CREATE_EMPTY.generateMsg(DIR.getName()));

    assertThatThrownBy(() -> classUnderTest.createDirectories(DIR, Lists.newArrayList()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.DIR_NAMES_TO_CREATE_EMPTY.generateMsg(DIR.getName()));
  }

  @Test
  void testCreate_whenDirectoryNotExist_exception() {
    final File file = new File("/notexist");
    assertThatThrownBy(() -> classUnderTest.createDirectories(file, Lists.newArrayList("dir")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.DIR_NOT_EXIST.generateMsg(file.getAbsolutePath()));
  }

  @Test
  void testCreate_whenRepeatingDirNames_exception() {
    final String repeatOne = "repeated";
    final String repeatTwo = "repeated2";
    final List<String> input = Lists.newArrayList(repeatOne, repeatTwo, repeatTwo.toUpperCase(), repeatOne.toUpperCase());
    assertThatThrownBy(() -> classUnderTest.createDirectories(new File("."), input))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.DIR_NAMES_REPEATED.generateMsg(Lists.newArrayList(repeatTwo, repeatOne)));
  }

  @Test
  void testCreate_whenDirCreateFails_noSubDirsAreAttempted() {
    when(fileSystemService.createDirectoryIfNotExist(TOP_DIR)).thenReturn(newOpFail(TOP_DIR));

    List<OperationResult> subDirState = createSubDirsState(f -> new OperationSkipped(f, OperationSkipped.DIR_NOT_CREATED));
    FolderCreateResult expectedTopLevelState = createFolderCreateResult(new OperationFailure(TOP_DIR, SIMULATED_E), subDirState);

    assertCreation(expectedTopLevelState);
  }

  @Test
  void testCreate_whenSomeSubDirsFail_othersCreatedAndExpectedResult() {
    OperationResult topLevelState = mockDirCreate(TOP_DIR, this::newOpSuccess);
    List<File> subDirs = FileFolderHelpers.toSubDirectories(TOP_DIR, SUB_DIR_NAMES);
    OperationResult subToFail = null;
    List<OperationResult> subsToPass = new ArrayList<>();
    for (File sub : subDirs) {
      if (subToFail == null) {
        subToFail = mockDirCreate(sub, this::newOpFail);
      } else {
        subsToPass.add(mockDirCreate(sub, this::newOpSuccess));
      }
    }

    subsToPass.add(0, subToFail);
    FolderCreateResult result = createFolderCreateResult(topLevelState, subsToPass);
    assertCreation(result);
  }


  @Test
  void testCreate_whenHappyPath_allDirsCreatedAndExpectedResult() {
    when(fileSystemService.createDirectoryIfNotExist(any())).thenCallRealMethod();
    List<OperationResult> expectedSubDirState = createSubDirsState(this::newOpSuccess);
    FolderCreateResult expected = createFolderCreateResult(newOpSuccess(TOP_DIR), expectedSubDirState);
    assertCreation(expected);
    assertThat(TOP_DIR).exists();
    SUB_DIRS.forEach(d -> assertThat(d).exists());
  }

  private void assertCreation(final FolderCreateResult result) {
    FoldersCreateRequestResult expected = new FoldersCreateRequestResult(DIR, DIRS_TO_CREATE, Lists.newArrayList(result));
    FoldersCreateRequestResult actual = classUnderTest.createDirectories(DIR, DIRS_TO_CREATE);
    assertThat(actual).isEqualTo(expected);
  }

  private <T extends OperationResult> List<OperationResult> createSubDirsState(final Function<File, T> resultFunction) {
    return createSubDirsResult(TOP_DIR, SUB_DIR_NAMES, resultFunction);
  }
}