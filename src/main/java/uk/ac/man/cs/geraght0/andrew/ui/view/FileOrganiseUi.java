package uk.ac.man.cs.geraght0.andrew.ui.view;

import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import lombok.extern.slf4j.Slf4j;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.FilesOrganiseResult;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.service.FileService;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.components.CompWithCaption;
import uk.ac.man.cs.geraght0.andrew.ui.components.FileOrganiseResultsTreeTbl;

@Slf4j
public class FileOrganiseUi extends AbsViewFolderFile {

  private CompWithCaption<FileOrganiseResultsTreeTbl> tblFileResults;
  private TabPane tabResults;

  public FileOrganiseUi(final UI ui) {
    super(ui);
  }

  @Override
  protected void populateFromConfig(final Config config) {

  }

  @Override
  protected Integer getIndexOfProgressBarPlacement() {
    return super.getIndexOfProgressBarPlacement() - 1;
  }

  @Override
  protected List<Node> getComponentsToHideDuringProgress() {
    List<Node> nodes = super.getComponentsToHideDuringProgress();
    nodes.add(tabResults);
    return nodes;
  }

  @Override
  protected String getCaptionForDirInput() {
    return "Directories to organise (one per line)";
  }

  @Override
  protected String getCaptionForDirOutput() {
    return "Directory creation results";
  }

  @Override
  protected String getCaptionForGoButton() {
    return "Organise files in directories";
  }

  @Override
  protected void createViewSpecificUiComponents() {
    //Output tab 1
    tblFileResults = createTbl("File organise results", new FileOrganiseResultsTreeTbl(getHostServices()));
    Tab tabFileResults = new Tab("View File Results", tblFileResults);

    //Output tab 2
    Tab tabFolderResults = new Tab("View Folder Results", tblDirResults);
    //Pane
    tabResults = new TabPane(tabFileResults, tabFolderResults);
    tabResults.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
  }

  @Override
  protected void addComponentsToView() {
    //Add to grid
    addToView(txtDirInput);
    addToView(tabResults);
    addToView(layButtons);
  }

  @Override
  protected void resetUiToStart() {
    super.resetUiToStart();
    tblFileResults.get()
                  .reset();
  }

  @Override
  protected void onGoClick(final List<String> dirInput) {
    final FileService fileService = getBean(FileService.class);
    List<FilesOrganiseResult> results = fileService.organiseFiles(dirInput);
    List<FolderCreateResult> dirResults = results.stream()
                                                 .map(FilesOrganiseResult::getFolderCreateResult)
                                                 .collect(Collectors.toList());
    Platform.runLater(() -> {
      setUiToResultsView();

      tblDirResults.get()
                   .populate(dirResults);
      tblFileResults.get()
                    .populate(results);
    });
  }

  @Override
  public void configureUiForResults(final boolean resultsOnShow) {
    super.configureUiForResults(resultsOnShow);
    disable(!resultsOnShow, tblFileResults);
  }
}
