package uk.ac.man.cs.geraght0.andrew.folders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.iberdrola.dtp.util.SpArrayUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.man.cs.geraght0.andrew.folders.model.FilesAllocateRequestResult;
import uk.ac.man.cs.geraght0.andrew.folders.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.models.config.FolderConfig;

class FileServiceTest extends AbsFileFolderTest<FileService> {

  private static final File SUB_DIR_ONE = new File(DIR, SUB_DIR_ONE_NAME);
  private static final File SUB_DIR_TWO = new File(DIR, SUB_DIR_TWO_NAME);
  private static final List<File> SUB_DIRS = Lists.newArrayList(SUB_DIR_ONE, SUB_DIR_TWO);
  private static final String TXT_EXT = "txt";
  private static final String FILENAME_END = "hello.data";

  @Override
  protected FileService createClassUnderTestInstance(final FolderConfig config, final FileSystemService fileSystemService) {
    FolderService folderService = new FolderService(config, fileSystemService);
    return new FileService(config, folderService, fileSystemService);
  }

  @Override
  @BeforeEach
  void before() {
    super.before();
    when(fileSystemService.createDirectoryIfNotExist(any(File.class))).thenCallRealMethod();
    lenient().when(fileSystemService.moveFile(any(File.class), any(File.class)))
             .thenCallRealMethod();
  }

  @Test
  void testAllocate_whenNoFilesButDirsNotExist_folderCreateResultPopulated() throws IOException {
    //@BeforeAll will have created dir - reverse this
    FileUtils.deleteDirectory(DIR);
    mockFilenameFilters(FILENAME_END, TXT_EXT);

    List<OperationResult> subDirResult = createSubDirsResult(DIR, this::newOpSuccess);
    FolderCreateResult folderCreateResult = createFolderCreateResult(newOpSuccess(DIR), subDirResult);
    final FilesAllocateRequestResult expected = new FilesAllocateRequestResult(DIR, folderCreateResult, Lists.newArrayList());

    assertFileAllocations(expected);
  }

  @Test
  void testAllocate_whenOneFileAndDirsNotExist_folderCreateResultPopulatedAndFileMoved() {
    //Create stub files
    File file = createFile(String.format("tmp.%s", TXT_EXT));

    //Mock
    mockFilenameFilters(TXT_EXT, null);

    //Expected
    List<OperationResult> subDirResult = createSubDirsResult(DIR, this::newOpSuccess);
    FolderCreateResult folderCreateResult = createFolderCreateResult(newOpNotNeededForNewDir(DIR), subDirResult);
    List<OperationResult> fileResults = Lists.newArrayList(newOpMove(file, SUB_DIR_ONE));
    final FilesAllocateRequestResult expected = new FilesAllocateRequestResult(DIR, folderCreateResult, fileResults);

    //Actual
    assertFileAllocations(expected);
  }

  @Test
  void testAllocate_whenFilesMatchAndDirsExist_filesMoved() throws IOException {
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
    final FilesAllocateRequestResult expected = new FilesAllocateRequestResult(DIR, folderCreateResult, fileResults);

    //Actual
    assertFileAllocations(expected);
  }

  @Test
  void testAllocate_whenFilesMatchAndSomeDontAndDirsExist_someFilesMoved() throws IOException {
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
    List<OperationResult> fileResults = Lists.newArrayList(newOpNotNeededForFileAllocate(fileThree), newOpMove(fileTwo, SUB_DIR_TWO),
                                                           newOpMove(fileOne, SUB_DIR_ONE));
    final FilesAllocateRequestResult expected = new FilesAllocateRequestResult(DIR, folderCreateResult, fileResults);

    //Actual
    assertFileAllocations(expected);
    assertThat(fileOne).doesNotExist();
    assertThat(new File(SUB_DIR_ONE, fileOne.getName())).exists();
    assertThat(fileTwo).doesNotExist();
    assertThat(new File(SUB_DIR_TWO, fileTwo.getName())).exists();
    assertThat(fileThree).exists();
  }

  @Test
  void testAllocate_whenFilesMatchButSomeFailToMove_someFilesMovedSomeFail() throws IOException {
    //Create Dir one only
    FileUtils.forceMkdir(SUB_DIR_ONE);
    //Make dir 2 fail dir creation
    final OperationFailure failForDirTwo = newOpFail(SUB_DIR_TWO);
    doReturn(failForDirTwo).when(fileSystemService)
                           .createDirectoryIfNotExist(SUB_DIR_TWO);

    //Create stub files
    File fileOne = createFile(String.format("tmp.%s", TXT_EXT));
    File fileTwo = createFile(String.format("other%s", FILENAME_END));

    //Mock - purposely put these the other way round
    mockFilenameFilters(FILENAME_END, TXT_EXT);

    //Expected
    //File one will fail to move coz dir 2 doesn't exist, capture error msg
    Throwable expectedException = null;
    try {
      FileUtils.moveFileToDirectory(fileOne, SUB_DIR_TWO, false);
      fail("Exception expected");
    } catch (Exception e) {
      expectedException = e;
    }
    OperationFailure fileOneOperationResult = new OperationFailure(fileOne, expectedException);
    List<OperationResult> fileResults = Lists.newArrayList(newOpMove(fileTwo, SUB_DIR_ONE), fileOneOperationResult);
    List<OperationResult> subDirResult = Lists.newArrayList(newOpNotNeededForNewDir(SUB_DIR_ONE), failForDirTwo);
    FolderCreateResult folderCreateResult = createFolderCreateResult(newOpNotNeededForNewDir(DIR), subDirResult);
    final FilesAllocateRequestResult expected = new FilesAllocateRequestResult(DIR, folderCreateResult, fileResults);

    //Actual
    assertFileAllocations(expected);
    assertThat(fileOne).exists();
    assertThat(fileTwo).doesNotExist();
    assertThat(new File(SUB_DIR_ONE, fileTwo.getName())).exists();
  }

  private void assertFileAllocations(final FilesAllocateRequestResult expected) {
    final FilesAllocateRequestResult actual = classUnderTest.allocateFiles(DIR);
    assertThat(actual).isEqualTo(expected);
  }

  private void mockFilenameFilters(String dirOneFilter, String dirTwoFilter) {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put(SUB_DIR_ONE_NAME, dirOneFilter == null ? "" : dirOneFilter);
    map.put(SUB_DIR_TWO_NAME, dirTwoFilter == null ? "" : dirTwoFilter);
    when(config.getDirectoryToFilenameFilter()).thenReturn(map);
  }

  private void createSubDirs() throws IOException {
    for (File subDir : SUB_DIRS) {
      FileUtils.forceMkdir(subDir);
    }
  }

  private File createFile(final String fileName) {
    List<File> files = createFiles(fileName);
    assertThat(files).hasSize(1);
    return files.get(0);
  }

  private List<File> createFiles(final String... fileNames) {
    return SpArrayUtils.stream(fileNames)
                       .map(f -> {
                         File file = new File(DIR, f);
                         try {
                           FileUtils.touch(file);
                         } catch (IOException e) {
                           fail("Cannot create file: " + file.getAbsolutePath(), e);
                         }
                         return file;
                       })
                       .collect(Collectors.toList());
  }
}