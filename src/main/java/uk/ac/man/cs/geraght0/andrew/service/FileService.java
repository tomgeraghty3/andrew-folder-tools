package uk.ac.man.cs.geraght0.andrew.service;

import com.iberdrola.dtp.util.SpArrayUtils;
import com.iberdrola.dtp.util.SpCollectionUtils;
import com.iberdrola.dtp.util.SpNumberUtils;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.constants.ErrorMessages;
import uk.ac.man.cs.geraght0.andrew.model.FilesOrganiseResult;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationNotApplicable;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

  private final Config config;
  private final FolderService folderService;
  private final FileSystemService fileSystemService;

  public List<FilesOrganiseResult> organiseFiles(List<String> directories) {
    log.info("Received a request to organise files in the following directories: {}", directories);
    if (CollectionUtils.isEmpty(directories)) {
      throw new IllegalArgumentException(ErrorMessages.DIR_TO_ORGANISE_EMPTY.generateMsg());
    }
    List<File> dirs = directories.stream()
                                 .map(f -> new File(f).getAbsoluteFile())
                                 .collect(Collectors.toList());
    List<File> notExist = dirs.stream()
                              .filter(d -> !d.exists())
                              .collect(Collectors.toList());
    if (!notExist.isEmpty()) {
      throw new IllegalArgumentException(ErrorMessages.DIRS_NOT_EXIST.generateMsg(SpCollectionUtils.toString(notExist, File::getPath)));
    }

    return dirs.stream()
               .map(this::organiseFiles)
               .collect(Collectors.toList());
  }

  /**
   * For each file in the specified directory see if that filename ends with any of the configured patterns
   * ({@link Config#getDirectoryToFilenameFilter()}) and if so move that file to the mapped directory for that pattern.
   * This method will also create any of the missing subdirectories in the specified directory (i.e. the ones returned
   * from {@link Config#deduceSubDirectoryNames(String)} by delegating to {@link FolderService#handleDirAndSubDirs(File, Collection)}
   *
   * @param dirWithFiles The folder with files to organise
   * @return A {@link FilesOrganiseResult} containing the subdirectory state (A {@link FolderCreateResult})
   * and a {@link OperationResult} for each file in the specified directory
   */
  public FilesOrganiseResult organiseFiles(File dirWithFiles) {
    return organiseFiles(dirWithFiles, dirWithFiles.getName());
  }


  public FilesOrganiseResult organiseFiles(final File dirWithFiles, final String containerName) {
    log.info("Received a request to organise all files within directory \"{}\"", dirWithFiles.getAbsolutePath());

    //TODO dirWithFailes null?

    //Handle files
    List<OperationResult> fileResults;
    FolderCreateResult folderCreateResult;
    File[] files = dirWithFiles.listFiles(f -> !f.isDirectory());
    if (files == null || files.length == 0) {
      log.info("No files present in the directory - nothing to organise");
      throw handleNoFiles(dirWithFiles);
//      fileResults = Collections.emptyList();
//      folderCreateResult = FileFolderHelpers.createResultWhenSkippedAsOrganiseDirIsEmpty(dirWithFiles, subDirectories);
    } else {
      //Populate any missing subdirectories
      List<String> subDirectories = config.deduceSubDirectoryNames(containerName);        //TODO if empty?
      log.info("Checking directory and any missing subdirectories: {}", subDirectories);
      folderCreateResult = folderService.handleDirAndSubDirs(dirWithFiles, subDirectories);

      log.info("Organising {}", SpNumberUtils.descCount(files.length, "file"));
      fileResults = SpArrayUtils.stream(files)
                                .map(f -> organiseFile(f, containerName))
                                .collect(Collectors.toList());
      log.info(FileFolderHelpers.generateOperationResultsLog("File organisation results:", fileResults));
    }

    return new FilesOrganiseResult(dirWithFiles, folderCreateResult, fileResults);
  }

  RuntimeException handleNoFiles(final File dirWithFiles) {
    return new IllegalArgumentException(String.format("Root directory \"%s\" has no files to organise", dirWithFiles.getName()));
  }

  private OperationResult organiseFile(final File file, final String containerName) {
    Optional<String> fileEligible = FileFolderHelpers.isFileEligible(config.getDirectoryToFilenameFilter(), containerName, file.getName());
    return fileEligible.map(newDirName -> {
                         File originalDir = file.getParentFile();
                         File newDir = new File(originalDir, newDirName);
                         return fileSystemService.moveFile(file, newDir);
                       })
                       .orElse(new OperationNotApplicable(file));
  }
}