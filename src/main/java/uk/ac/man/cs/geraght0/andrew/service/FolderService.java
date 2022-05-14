package uk.ac.man.cs.geraght0.andrew.service;

import com.iberdrola.dtp.util.SpCollectionUtils;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.FoldersCreateRequestResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.constans.ErrorMessages;
import uk.ac.man.cs.geraght0.andrew.models.config.FolderConfig;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

  private final FolderConfig config;
  private final FileSystemService fileSystemService;

  /**
   * For each of the Strings specified in the list:
   * <ol>
   *   <li>Create the directory as a child of the specified <code>parentDirectory</code></li>
   *   <li>Under that newly created directory, create subdirectories for all the configured directory names in {@link FolderConfig#deduceSubDirectoryNames()}</li>
   * </ol>
   *
   * @param parentDirectory     The parent directory
   * @param directoriesToCreate A list of directory names to create under the parent directory
   * @return A {@link FoldersCreateRequestResult} containing the result of the creation of each directory and the configured
   * subdirectories for each directory
   */
  public FoldersCreateRequestResult createDirectories(File parentDirectory, List<String> directoriesToCreate) {
    //Check parent dir argument is valid first
    if (parentDirectory == null) {
      throw new IllegalArgumentException(ErrorMessages.DIR_NULL.generateMsg());
    } else if (CollectionUtils.isEmpty(directoriesToCreate)) {
      throw new IllegalArgumentException(ErrorMessages.DIR_NAMES_TO_CREATE_EMPTY.generateMsg(parentDirectory.getName()));
    }

    final File parentDirectoryAbs = parentDirectory.getAbsoluteFile();
    log.info("Request to create \"container\" directories: {} in parent directory: {}", directoriesToCreate, parentDirectoryAbs);
    if (!parentDirectoryAbs.exists()) {
      throw new IllegalArgumentException(ErrorMessages.DIR_NOT_EXIST.generateMsg(parentDirectoryAbs.getAbsolutePath()));
    }

    //Check for duplicates
    Set<String> dirs = new HashSet<>();
    final List<String> dirsAppearingMoreThanOnce = directoriesToCreate.stream()
                                                                      .map(String::toLowerCase)
                                                                      .filter(e -> !dirs.add(e))
                                                                      .collect(Collectors.toList());
    if (!dirsAppearingMoreThanOnce.isEmpty()) {
      throw new IllegalArgumentException(ErrorMessages.DIR_NAMES_REPEATED.generateMsg(dirsAppearingMoreThanOnce));
    }

    //Simply call the helper method here
    List<FolderCreateResult> results = directoriesToCreate.stream()
                                                          .map(d -> createTopLevelDir(d, parentDirectoryAbs))
                                                          .collect(Collectors.toList());

    return new FoldersCreateRequestResult(parentDirectoryAbs, directoriesToCreate, results);
  }

  private FolderCreateResult createTopLevelDir(final String dirName, final File parentDirectory) {
    final Set<String> subDirNames = config.deduceSubDirectoryNames();
    log.info("Request to create \"{}\" (with subdirectories {}) in directory \"{}\"", dirName,
             subDirNames, parentDirectory.getAbsolutePath());
    File topLevel = new File(parentDirectory, dirName);
    return handleDirAndSubDirs(topLevel, subDirNames);
  }

  public FolderCreateResult handleDirAndSubDirs(final File topLevel, final Collection<String> subDirNames) {
    final List<File> subDirectories = FileFolderHelpers.toSubDirectories(topLevel, subDirNames);
    log.debug("Analysing directory: {}\n\tAnd subdirectories: {}", topLevel.getAbsolutePath(),
              SpCollectionUtils.toString(subDirectories, File::getAbsolutePath));

    //Handle top level first
    OperationResult dirState = fileSystemService.createDirectoryIfNotExist(topLevel);
    log.info("Top level directory creation: {}", dirState.getResultDescription());
    //Special case... if top level failed then skip the subdirectories
    final Optional<OperationFailure> isFailedOp = dirState.isFailed();
    if (isFailedOp.isPresent()) {
      OperationFailure failed = isFailedOp.get();
      log.warn("The creation of the top level directory failed so skipping creating the subdirectories: {}", subDirectories);
      return FileFolderHelpers.createResultWhenDirCreateFailure(topLevel, subDirectories, failed.getFailure());
    }

    //Now the subdirectories
    List<OperationResult> subDirState = subDirectories.stream()
                                                      .map(fileSystemService::createDirectoryIfNotExist)
                                                      .collect(Collectors.toList());
    log.info(FileFolderHelpers.generateOperationResultsLog("Subdirectory creation:", subDirState));

    return new FolderCreateResult(dirState, subDirState);
  }
}