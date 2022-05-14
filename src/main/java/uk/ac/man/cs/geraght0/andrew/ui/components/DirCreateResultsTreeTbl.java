package uk.ac.man.cs.geraght0.andrew.ui.components;

import java.util.List;
import javafx.application.HostServices;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;

public class DirCreateResultsTreeTbl extends AbsTreeTable<FolderCreateResult> {

  public DirCreateResultsTreeTbl(final HostServices hostServices) {
    super(hostServices);
  }

  @Override
  protected boolean displayResultDescForDirs() {
    return true;
  }

  @Override
  protected String getNameOfFirstColumn() {
    return "Directory";
  }

  @Override
  protected List<OperationResult> getSubItemResults(final FolderCreateResult item) {
    return item.getSubDirectoriesCreateResult();
  }

  @Override
  protected OperationResult getDirResult(final FolderCreateResult item) {
    return item.getDirCreateResult();
  }
}