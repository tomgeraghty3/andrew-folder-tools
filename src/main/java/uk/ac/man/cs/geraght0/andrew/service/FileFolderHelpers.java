package uk.ac.man.cs.geraght0.andrew.service;

import com.iberdrola.dtp.util.SpCollectionUtils;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationSkipped;

public class FileFolderHelpers {

  /**
   * Create a {@link File} for each String using the String as the directory name and the <code>parentDirectory</code> as the parent directory
   *
   * @param parentDirectory   The parent directory
   * @param subDirectoryNames A list of directory names
   */
  public static List<File> toSubDirectories(final File parentDirectory, Collection<String> subDirectoryNames) {
    return subDirectoryNames.stream()
                            .map(n -> new File(parentDirectory, n))
                            .collect(Collectors.toList());
  }

  public static Optional<String> isFileEligible(Map<String, String> dirToPattern, String filename) {
    return dirToPattern.entrySet()
                       .stream()
                       .filter(e -> isFileMatchingPattern(e.getValue(), filename))
                       .map(Entry::getKey)
                       .findFirst();
  }

  static boolean isFileMatchingPattern(String pattern, String filename) {
    if (StringUtils.isBlank(pattern)) {
      return false;
    }

    return filename.endsWith(pattern);
  }

  public static FolderCreateResult createResultWhenDirCreateFailure(File dir, List<File> subDirs, final Throwable failure) {
    List<OperationResult> subDirState = subDirs.stream()
                                               .map(d -> new OperationSkipped(d, OperationSkipped.DIR_NOT_CREATED))
                                               .collect(Collectors.toList());
    return new FolderCreateResult(new OperationFailure(dir, failure), subDirState);
  }

  public static FolderCreateResult createResultWhenSkippedAsOrganiseDirIsEmpty(File dir, Collection<String> subDirs) {
    List<OperationResult> subDirState = subDirs.stream()
                                               .map(d -> new OperationSkipped(new File(dir, d), OperationSkipped.DIR_EMPTY))
                                               .collect(Collectors.toList());
    return new FolderCreateResult(new OperationSkipped(dir, OperationSkipped.DIR_EMPTY), subDirState);
  }

  public static String generateOperationResultsLog(final String operationDesc, final List<OperationResult> subDirState) {
    return String.format("%s%s", operationDesc, SpCollectionUtils.toString(subDirState, s -> String.format("%s\t%s - %s", System.lineSeparator(),
                                                                                                           s.getLocation()
                                                                                                            .getName(), s.getResultDescription())));
  }
}
