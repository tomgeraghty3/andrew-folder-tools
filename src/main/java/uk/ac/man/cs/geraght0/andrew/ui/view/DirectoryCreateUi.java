package uk.ac.man.cs.geraght0.andrew.ui.view;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.FoldersCreateRequestResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationDirCreate;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationMove;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationNotNeeded;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationSkipped;
import uk.ac.man.cs.geraght0.andrew.service.FolderService;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.components.DirChooserPanel;

@Slf4j
public class DirectoryCreateUi extends AbsViewFolderFile {

  private DirChooserPanel layDirChooser;

  public DirectoryCreateUi(final UI ui) {
    super(ui);
  }

  @Override
  protected void populateFromConfig(final Config config) {

  }

  @Override
  protected List<Node> getComponentsToToggleDisableDuringProgress() {
    List<Node> list = super.getComponentsToToggleDisableDuringProgress();
    list.add(layDirChooser);
    return list;
  }

  @Override
  protected String getCaptionForDirInput() {
    return "Container names (one per line)";
  }

  @Override
  protected String getCaptionForDirOutput() {
    return "Results";
  }

  @Override
  protected String getCaptionForGoButton() {
    return "Create containers";
  }

  @Override
  protected void createViewSpecificUiComponents() {
    //Input
    layDirChooser = new DirChooserPanel("Root Directory to create containers in");
  }

  @Override
  public void addComponentsToView() {
    //Add to grid
    addToView(layDirChooser);
    addToView(txtDirInput);
    addToView(tblDirResults);
    addToView(layButtons);
  }

  @Override
  protected void resetUiToStart() {
    super.resetUiToStart();
    layDirChooser.reset();
  }

  @Override
  protected void onGoClick(final List<String> dirInput) {
    FolderService folderService = getBean(FolderService.class);
    //Test
    final File dir = new File("C:\\Users\\Tom\\Desktop\\T");
    final List<FolderCreateResult> results = Lists.newArrayList(new FolderCreateResult(new OperationMove(dir, dir),
                                                                                       Lists.newArrayList(
                                                                                           new OperationFailure(dir, new IllegalStateException("Stub")),
                                                                                           new OperationNotNeeded(dir,
                                                                                                                  OperationNotNeeded.DIR_ALREADY_EXISTS_DESC),
                                                                                           new OperationSkipped(dir, OperationSkipped.DIR_NOT_CREATED),
                                                                                           new OperationDirCreate(dir))));
    FoldersCreateRequestResult result = new FoldersCreateRequestResult(null, null, results);
//    FoldersCreateRequestResult result = folderService.createDirectories(layDirChooser.getChosenDirectory(), dirInput);
    Platform.runLater(() -> {
      setUiToResultsView();
      tblDirResults.get()
                   .populate(result.getDirectories());
    });
  }

  @Override
  public void configureUiForResults(final boolean resultsOnShow) {
    super.configureUiForResults(resultsOnShow);
    disable(resultsOnShow, layDirChooser);
  }
}
