package uk.ac.man.cs.geraght0.andrew.service;

import com.iberdrola.dtp.util.SpArrayUtils;
import com.iberdrola.dtp.util.SpCollectionUtils;
import com.iberdrola.dtp.util.SpNumberUtils;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.geraght0.andrew.constans.ErrorMessages;
import uk.ac.man.cs.geraght0.andrew.model.FilesOrganiseResult;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationNotNeeded;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.models.config.FolderConfig;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

  private final FolderConfig config;
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

    return dirs.stream().map(this::allocateFiles).collect(Collectors.toList());
  }

  /**
   * For each file in the specified directory see if that filename ends with any of the configured patterns
   * ({@link FolderConfig#getDirectoryToFilenameFilter()}) and if so move that file to the mapped directory for that pattern.
   * This method will also create any of the missing subdirectories in the specified directory (i.e. the ones returned
   * from {@link FolderConfig#deduceSubDirectoryNames()}) by delegating to {@link FolderService#handleDirAndSubDirs(File, Collection)}
   *
   * @param dirWithFiles The folder with files to allocate
   * @return A {@link FilesOrganiseResult} containing the subdirectory state (A {@link FolderCreateResult})
   * and a {@link OperationResult} for each file in the specified directory
   */
  public FilesOrganiseResult allocateFiles(File dirWithFiles) {
    log.info("Received a request to allocate all files within directory \"{}\"", dirWithFiles.getAbsolutePath());

    //Handle files
    Set<String> subDirectories = config.deduceSubDirectoryNames();        //TODO if empty?
    List<OperationResult> fileResults;
    FolderCreateResult folderCreateResult = null;
    File[] files = dirWithFiles.listFiles(f -> !f.isDirectory());
    if (files == null || files.length == 0) {
      log.info("No files present in the directory - nothing to allocate");
      fileResults = Collections.emptyList();
      folderCreateResult = FileFolderHelpers.createResultWhenSkippedAsOrganiseDirIsEmpty(dirWithFiles, subDirectories);
    } else {
      //Populate any missing subdirectories
      log.info("Checking directory and any missing subdirectories: {}", subDirectories);
      folderCreateResult = folderService.handleDirAndSubDirs(dirWithFiles, subDirectories);

      log.info("Allocating {}", SpNumberUtils.descCount(files.length, "file"));
      fileResults = SpArrayUtils.stream(files)
                                .map(this::allocateFile)
                                .collect(Collectors.toList());
      log.info(FileFolderHelpers.generateOperationResultsLog("File allocation results:", fileResults));
    }

    return new FilesOrganiseResult(dirWithFiles, folderCreateResult, fileResults);
  }

  private OperationResult allocateFile(final File file) {
    Optional<String> fileEligible = FileFolderHelpers.isFileEligible(config.getDirectoryToFilenameFilter(), file.getName());
    return fileEligible.map(newDirName -> {
                         File originalDir = file.getParentFile();
                         File newDir = new File(originalDir, newDirName);
                         return fileSystemService.moveFile(file, newDir);
                       })
                       .orElse(new OperationNotNeeded(file, OperationNotNeeded.FILE_NOT_MATCH_PATTERN_DESC));
  }
}