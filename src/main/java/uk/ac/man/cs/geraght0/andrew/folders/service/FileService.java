package uk.ac.man.cs.geraght0.andrew.folders.service;

import com.iberdrola.dtp.util.SpArrayUtils;
import com.iberdrola.dtp.util.SpNumberUtils;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.geraght0.andrew.folders.model.FilesAllocateRequestResult;
import uk.ac.man.cs.geraght0.andrew.folders.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationNotNeeded;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.models.config.FolderConfig;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

  private final FolderConfig config;
  private final FolderService folderService;
  private final FileSystemService fileSystemService;

  /**
   * For each file in the specified directory see if that filename ends with any of the configured patterns
   * ({@link FolderConfig#getDirectoryToFilenameFilter()}) and if so move that file to the mapped directory for that pattern.
   * This method will also create any of the missing subdirectories in the specified directory (i.e. the ones returned
   * from {@link FolderConfig#deduceSubDirectoryNames()}) by delegating to {@link FolderService#handleDirAndSubDirs(File, Collection)}
   * @param dirWithFiles The folder with files to allocate
   * @return A {@link FilesAllocateRequestResult} containing the subdirectory state (A {@link FolderCreateResult})
   * and a {@link OperationResult} for each file in the specified directory
   */
  public FilesAllocateRequestResult allocateFiles(File dirWithFiles) {
    log.info("Received a request to allocate all files within directory \"{}\"", dirWithFiles.getAbsolutePath());

    //Populate any missing subdirectories
    Set<String> subDirectories = config.deduceSubDirectoryNames();        //TODO if empty?
    log.info("Checking directory and any missing subdirectories: {}", subDirectories);
    FolderCreateResult folderCreateResult = folderService.handleDirAndSubDirs(dirWithFiles, subDirectories);

    //Handle files
    List<OperationResult> fileResults;
    File[] files = dirWithFiles.listFiles(f -> !f.isDirectory());
    if (files == null || files.length == 0) {
      log.info("No files present in the directory - nothing to allocate");
      fileResults = Collections.emptyList();
    } else {
      log.info("Allocating {}", SpNumberUtils.descCount(files.length, "file"));
      fileResults = SpArrayUtils.stream(files)
                                .map(this::allocateFile)
                                .collect(Collectors.toList());
      log.info(FileFolderHelpers.generateOperationResultsLog("File allocation results:", fileResults));
    }

    return new FilesAllocateRequestResult(dirWithFiles, folderCreateResult, fileResults);
  }

  private OperationResult allocateFile(final File file) {
    Optional<String> fileEligible = FileFolderHelpers.isFileEligible(config.getDirectoryToFilenameFilter(), file.getName());
    return fileEligible.map(newDirName -> {
      File originalDir = file.getParentFile();
      File newDir = new File(originalDir, newDirName);
      return fileSystemService.moveFile(file, newDir);
    }).orElse(new OperationNotNeeded(file, OperationNotNeeded.FILE_NOT_MATCH_PATTERN_DESC));
  }
}