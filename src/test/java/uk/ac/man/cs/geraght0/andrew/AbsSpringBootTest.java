package uk.ac.man.cs.geraght0.andrew;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.integration.IntegrationProperties.Jdbc;
import org.springframework.boot.jdbc.DataSourceInitializationMode;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.FoldersCreateRequestResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationDirCreate;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationNotNeeded;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.service.FolderService;

public abstract class AbsSpringBootTest extends AbsTest {

  protected static final String CONTAINER_NAME = "container";

  @Autowired
  protected FolderService folderService;
  Jdbc i;
  DataSourceInitializationMode m;
  org.springframework.boot.sql.init.DatabaseInitializationMode m2;

  protected File testSubDirCreationAndExists() {
    final List<String> dirList = Lists.newArrayList(CONTAINER_NAME);
    FoldersCreateRequestResult actual = folderService.createDirectories(DIR, dirList);

    File topDir = new File(DIR, CONTAINER_NAME);
    final List<OperationResult> subDirResult = toSubDirFolders().stream()
                                                                .map(OperationDirCreate::new)
                                                                .collect(Collectors.toList());
    FolderCreateResult one = new FolderCreateResult(new OperationDirCreate(topDir), subDirResult);
    FoldersCreateRequestResult expected = new FoldersCreateRequestResult(DIR, dirList, Lists.newArrayList(one));
    assertThat(actual).isEqualTo(expected);

    testSubDirFoldersExist();
    return topDir;
  }

  protected List<File> toSubDirFolders() {
    return doForEachActualDirectory(this::toFile);
  }

  protected List<OperationResult> toSubDirFolderAsNotNeeded() {
    return doForEachActualDirectory((prefix, dirName) -> {
      File dir = toFile(prefix, dirName);
      return new OperationNotNeeded(dir);
    });
  }

  protected File toFile(final String prefix, final String dirName) {
    return new File(DIR, String.format("%s/%s/%s", CONTAINER_NAME, prefix, dirName));
  }

  protected void testSubDirFoldersExist() {
    doForEachActualDirectory((prefix, dirName) -> {
      File file = toFile(prefix, dirName);
      assertThat(file).exists();
      return null;
    });
  }

  private <T> List<T> doForEachActualDirectory(BiFunction<String, String, T> func) {
    List<T> result = new ArrayList<>();
    final String paidPrefix = getPaidPrefix();
    final String tourPrefix = getTourPrefix();

    result.add(func.apply(tourPrefix, "videos"));
    result.add(func.apply(paidPrefix, "videos"));
    result.add(func.apply(paidPrefix, "artwork"));
    result.add(func.apply(tourPrefix, "artwork"));
    result.add(func.apply(paidPrefix, "gallery"));
    result.add(func.apply(tourPrefix, "gallery"));
    return result;
  }

  protected String getTourPrefix() {
    return String.format("tour/%s/o", CONTAINER_NAME);
  }

  protected String getPaidPrefix() {
    return String.format("paid/%s/o", CONTAINER_NAME);
  }
}
