package uk.ac.man.cs.geraght0.andrew.folders.service;

import com.iberdrola.dtp.util.SpCollectionUtils;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.ac.man.cs.geraght0.andrew.folders.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.folders.model.result.OperationSkipped;

public class FileFolderHelpers {

  public static Optional<String> isFileEligible(Map<String, String> dirToPattern, String filename) {
    return dirToPattern.entrySet()
                       .stream()
                       .filter(e -> isFileMatchingPattern(e.getValue(), filename))
                       .map(Entry::getKey)
                       .findFirst();
  }

  static boolean isFileMatchingPattern(String pattern, String filename) {
    return filename.endsWith(pattern);
  }

  public static FolderCreateResult createResultWhenDirCreateFailure(File dir, List<File> subDirs, final Throwable failure) {
    List<OperationResult> subDirState = subDirs.stream()
                                               .map(OperationSkipped::new)
                                               .collect(Collectors.toList());
    return new FolderCreateResult(new OperationFailure(dir, failure), subDirState);
  }

  public static String generateOperationResultsLog(final String operationDesc, final List<OperationResult> subDirState) {
    return String.format("%s%s", operationDesc, SpCollectionUtils.toString(subDirState, s -> String.format("%s\t%s - %s", System.lineSeparator(),
                                                                                                           s.getLocation()
                                                                                                            .getName(), s.getResultDescription())));
  }
}
