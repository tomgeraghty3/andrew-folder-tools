package uk.ac.man.cs.geraght0.andrew.service;

import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.CN_PLACEHOLDER;

import com.iberdrola.dtp.util.SpCollectionUtils;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationSkipped;

public class FileFolderHelpers {

  private FileFolderHelpers() {}

  /**
   * Create a {@link File} for each String using the String as the directory name and the <code>parentDirectory</code> as the parent directory
   *
   * @param parentDirectory   The parent directory
   * @param subDirectoryNames A list of directory names
   */
  static List<File> toSubDirectories(final File parentDirectory, final Collection<String> subDirectoryNames) {
    return subDirectoryNames.stream()
                            .map(n -> new File(parentDirectory, n))
                            .collect(Collectors.toList());
  }

  static Optional<String> isFileEligible(final Map<String, String> dirToPattern, final String containerName, final String filename) {
    return dirToPattern.entrySet()
                       .stream()
                       .filter(e -> FileFolderHelpers.isFileMatchingPattern(e.getValue(), filename))
                       .map(e -> FileFolderHelpers.mapDirWithContainerName(e.getKey(), containerName))
                       .findFirst();
  }

  static boolean isFileMatchingPattern(final String pattern, final String filename) {
    if (StringUtils.isBlank(pattern)) {
      return false;
    }

    return filename.endsWith(pattern);
  }

  static FolderCreateResult createResultWhenDirCreateFailure(final File dir, final List<File> subDirs, final Throwable failure) {
    final List<OperationResult> subDirState = subDirs.stream()
                                                     .map(d -> new OperationSkipped(d, OperationSkipped.DIR_NOT_CREATED))
                                                     .collect(Collectors.toList());
    return new FolderCreateResult(new OperationFailure(dir, failure), subDirState);
  }

  static FolderCreateResult createResultWhenSkippedAsOrganiseDirIsEmpty(final File dir, final Collection<String> subDirs) {
    final List<OperationResult> subDirState = subDirs.stream()
                                                     .map(d -> new OperationSkipped(new File(dir, d), OperationSkipped.DIR_EMPTY))
                                                     .collect(Collectors.toList());
    return new FolderCreateResult(new OperationSkipped(dir, OperationSkipped.DIR_EMPTY), subDirState);
  }

  static String generateOperationResultsLog(final String operationDesc, final List<OperationResult> subDirState) {
    return String.format("%s%s", operationDesc, SpCollectionUtils.toString(subDirState, s -> String.format("%s\t%s - %s", System.lineSeparator(),
                                                                                                           s.getLocation()
                                                                                                            .getAbsolutePath(),
                                                                                                           s.getResultDescription())));
  }

  public static String mapDirWithContainerName(final String directory, final String containerName) {
    return directory.replaceAll(CN_PLACEHOLDER, containerName);
  }
}
