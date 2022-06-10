package uk.ac.man.cs.geraght0.andrew.ui.components;

import java.util.List;
import javafx.application.HostServices;
import uk.ac.man.cs.geraght0.andrew.model.FilesOrganiseResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;

public class FileOrganiseResultsTreeTbl extends AbsTreeTable<FilesOrganiseResult> { //NOSONAR - the parent hierarchy allows for UI reuse

  public FileOrganiseResultsTreeTbl(final HostServices hostServices) {
    super(hostServices);
  }

  @Override
  protected boolean displayResultDescForDirs() {
    return false;
  }

  @Override
  protected String getNameOfFirstColumn() {
    return "File";
  }

  @Override
  protected List<OperationResult> getSubItemResults(final FilesOrganiseResult item) {
    return item.getFileResults();
  }

  @Override
  protected OperationResult getDirResult(final FilesOrganiseResult item) {
    return item.getFolderCreateResult().getDirCreateResult();
  }

}