package uk.ac.man.cs.geraght0.andrew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.constants.ErrorMessages;
import uk.ac.man.cs.geraght0.andrew.model.FilesOrganiseResult;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationSkipped;

class FileServiceTest extends AbsServiceTest<FileService> {

  private static final File SUB_DIR_ONE = new File(DIR, SUB_DIR_ONE_NAME);
  private static final File SUB_DIR_TWO = new File(DIR, SUB_DIR_TWO_NAME);
  private static final List<File> SUB_DIRS = Lists.newArrayList(SUB_DIR_ONE, SUB_DIR_TWO);
  private static final String TXT_EXT = "txt";
  private static final String FILENAME_END = "hello.data";

  @Override
  protected FileService createClassUnderTestInstance(final Config config, final FileSystemService fileSystemService) {
    FolderService folderService = new FolderService(config, fileSystemService);
    return new FileService(config, folderService, fileSystemService);
  }

  @Override
  @BeforeEach
  void before() {
    super.before();
    lenient().when(fileSystemService.createDirectoryIfNotExist(any(File.class))).thenCallRealMethod();
    lenient().when(fileSystemService.moveFile(any(File.class), any(File.class)))
             .thenCallRealMethod();
  }

  @Test
  void testOrganise_whenNoDirs_exception() {
    assertThatThrownBy(() -> classUnderTest.organiseFiles((List<String>) null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.DIR_TO_ORGANISE_EMPTY.generateMsg());

    final List<String> list = Collections.emptyList();
    assertThatThrownBy(() -> classUnderTest.organiseFiles(list))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.DIR_TO_ORGANISE_EMPTY.generateMsg());
  }

  @Test
  void testOrganise_whenDirectoryNotExist_exception() {
    final String f1 = "/notexist";
    final File file = new File(f1);
    final String f2 = "dir/dir2/my-file";
    final File file2 = new File(f2);
    final File file3 = new File("src");
    final List<String> list = Lists.newArrayList(f1, f2, file3.getName());
    assertThatThrownBy(() -> classUnderTest.organiseFiles(list))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.DIRS_NOT_EXIST.generateMsg(Lists.newArrayList(file.getAbsolutePath(), file2.getAbsolutePath())));
  }

  @Test
  void testOrganise_whenDirEmpty_folderCreateSkipped() {
    List<OperationResult> subDirResult = createSubDirsResult(DIR, f -> new OperationSkipped(f, OperationSkipped.DIR_EMPTY));
    FolderCreateResult folderCreateResult = createFolderCreateResult(newOpSkipped(DIR, OperationSkipped.DIR_EMPTY), subDirResult);
    final FilesOrganiseResult expected = new FilesOrganiseResult(DIR, folderCreateResult, Lists.newArrayList());

    assertFileOrganisation(expected);
  }

  @Test
  void testOraganise_whenOneFileAndDirsNotExist_folderCreateResultPopulatedAndFileMoved() {
    //Create stub files
    File file = createFile(String.format("tmp.%s", TXT_EXT));

    //Mock
    mockFilenameFilters(TXT_EXT, null);

    //Expected
    List<OperationResult> subDirResult = createSubDirsResult(DIR, this::newOpSuccess);
    FolderCreateResult folderCreateResult = createFolderCreateResult(newOpNotNeededForNewDir(DIR), subDirResult);
    List<OperationResult> fileResults = Lists.newArrayList(newOpMove(file, SUB_DIR_ONE));
    final FilesOrganiseResult expected = new FilesOrganiseResult(DIR, folderCreateResult, fileResults);

    //Actual
    assertFileOrganisation(expected);
  }

  @Test
  void testOraganise_whenFilesMatchAndDirsExist_filesMoved() {
    //Create stub files
    createSubDirs();
    File fileOne = createFile(String.format("tmp.%s", TXT_EXT));
    File fileTwo = createFile(String.format("other%s", FILENAME_END));

    //Mock
    mockFilenameFilters(TXT_EXT, FILENAME_END);

    //Expected
    List<OperationResult> subDirResult = createSubDirsResult(DIR, this::newOpNotNeededForNewDir);
    FolderCreateResult folderCreateResult = createFolderCreateResult(newOpNotNeededForNewDir(DIR), subDirResult);
    List<OperationResult> fileResults = Lists.newArrayList(newOpMove(fileTwo, SUB_DIR_TWO), newOpMove(fileOne, SUB_DIR_ONE));
    final FilesOrganiseResult expected = new FilesOrganiseResult(DIR, folderCreateResult, fileResults);

    //Actual
    assertFileOrganisation(expected);
  }

  @Test
  void testOraganise_whenFilesMatchAndSomeDontAndDirsExist_someFilesMoved() {
    //Create stub files
    createSubDirs();
    File fileOne = createFile(String.format("tmp.%s", TXT_EXT));
    File fileTwo = createFile(String.format("other%s", FILENAME_END));
    File fileThree = createFile("other-not-matching");

    //Mock
    mockFilenameFilters(TXT_EXT, FILENAME_END);

    //Expected
    List<OperationResult> subDirResult = createSubDirsResult(DIR, this::newOpNotNeededForNewDir);
    FolderCreateResult folderCreateResult = createFolderCreateResult(newOpNotNeededForNewDir(DIR), subDirResult);
    List<OperationResult> fileResults = Lists.newArrayList(newOpNotApplicableForFile(fileThree), newOpMove(fileTwo, SUB_DIR_TWO),
                                                           newOpMove(fileOne, SUB_DIR_ONE));
    final FilesOrganiseResult expected = new FilesOrganiseResult(DIR, folderCreateResult, fileResults);

    //Actual
    assertFileOrganisation(expected);
    assertThat(fileOne).doesNotExist();
    assertThat(new File(SUB_DIR_ONE, fileOne.getName())).exists();
    assertThat(fileTwo).doesNotExist();
    assertThat(new File(SUB_DIR_TWO, fileTwo.getName())).exists();
    assertThat(fileThree).exists();
  }

  @Test
  void testOrganise_whenFilesMatchButSomeFailToMove_someFilesMovedSomeFail() throws IOException {
    //This method will call the real method of FileSystemService so we need to use a Spy not a Mock. Create new mocks/spies for the beans
    Config config = mock(Config.class);
    FileSystemService fss = spy(new FileSystemService(config));
    FileService classUnderTestForThisTest = createClassUnderTestInstance(config, fss);
    mockFilenameFilters(config, FILENAME_END, TXT_EXT);
    mockSubDirNames(config);
    when(config.isDisallowOverwrite()).thenReturn(true);

    //Create Dirs
    FileUtils.forceMkdir(SUB_DIR_ONE);
    FileUtils.forceMkdir(SUB_DIR_TWO);

    //Create stub files
    File fileOne = createFile(String.format("tmp.%s", TXT_EXT));
    File fileTwo = createFile(String.format("other%s", FILENAME_END));
    //Now create an already existing "fileOne" in SUB_DIR_TWO
    File fileAtDestPath = new File(SUB_DIR_TWO, fileOne.getName());
    FileUtils.touch(fileAtDestPath);
    assertThat(fileAtDestPath).exists();

    //Expected
    //File one will fail to move coz it already exists at the destination path, capture error msg
    Throwable expectedException = null;
    try {
      FileUtils.moveFile(fileOne, fileAtDestPath);
      fail("Exception expected");
    } catch (Exception e) {
      expectedException = e;
    }
    OperationFailure fileOneOperationResult = new OperationFailure(fileOne, expectedException);
    List<OperationResult> fileResults = Lists.newArrayList(newOpMove(fileTwo, SUB_DIR_ONE), fileOneOperationResult);
    List<OperationResult> subDirResult = Lists.newArrayList(newOpNotNeededForNewDir(SUB_DIR_ONE), newOpNotNeededForNewDir(SUB_DIR_TWO));
    FolderCreateResult folderCreateResult = createFolderCreateResult(newOpNotNeededForNewDir(DIR), subDirResult);
    final FilesOrganiseResult expected = new FilesOrganiseResult(DIR, folderCreateResult, fileResults);

    //Actual
    assertFileOrganisation(classUnderTestForThisTest, expected);
    assertThat(fileOne).exists();
    assertThat(fileTwo).doesNotExist();
    assertThat(new File(SUB_DIR_ONE, fileTwo.getName())).exists();
  }

  private void assertFileOrganisation(final FilesOrganiseResult expected) {
    assertFileOrganisation(classUnderTest, expected);
  }

  private void assertFileOrganisation(final FileService serviceBeingTested, final FilesOrganiseResult expected) {
    final FilesOrganiseResult actual = serviceBeingTested.organiseFiles(DIR);
    assertThat(actual).isEqualTo(expected);
  }

  private void mockFilenameFilters(String dirOneFilter, String dirTwoFilter) {
    mockFilenameFilters(config, dirOneFilter, dirTwoFilter);
  }

  private void mockFilenameFilters(Config configInstance, String dirOneFilter, String dirTwoFilter) {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put(SUB_DIR_ONE_NAME, dirOneFilter == null ? "" : dirOneFilter);
    map.put(SUB_DIR_TWO_NAME, dirTwoFilter == null ? "" : dirTwoFilter);
    lenient().when(configInstance.getDirectoryToFilenameFilter())
             .thenReturn(map);
  }

  private void createSubDirs() {
    createSubDirs(SUB_DIRS);
  }
}