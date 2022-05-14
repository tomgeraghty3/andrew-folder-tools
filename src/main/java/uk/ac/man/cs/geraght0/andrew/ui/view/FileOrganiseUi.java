package uk.ac.man.cs.geraght0.andrew.ui.view;

import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.FilesOrganiseResult;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.service.FileService;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.components.CompWithCaption;
import uk.ac.man.cs.geraght0.andrew.ui.components.FileOrganiseResultsTreeTbl;

@Slf4j
public class FileOrganiseUi extends AbsViewFolderFile<FilesOrganiseResult> {

  //UI components
  private CompWithCaption<FileOrganiseResultsTreeTbl> tblFileResults;
  private TabPane tabResults;
  //State
  private DirectoryCreateUi previousUi;

  public FileOrganiseUi(final UI ui) {
    super(ui);
  }

  @Override
  public void populateFromConfig() {
    log.info("Populating the UI with the last used values");
    Config config = getBean(Config.class);
    String dirs = config.getLastDirsForFileOrganise();
    if (!StringUtils.isBlank(dirs)) {
      txtDirInput.get()
                 .setText(dirs);
    }
  }

  @Override
  protected void updateConfig(final Config config) {
    String dirs = txtDirInput.get()
                             .getText();
    if (!StringUtils.isBlank(dirs)) {
      config.setLastDirsForFileOrganise(dirs);
    }
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
  protected void configureUiForNonStandaloneInResultsView() {
    btnGo.setVisible(false);
    //TODO go back with back button
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
  protected void resetUiToStart(final boolean clearDownValues) {
    super.resetUiToStart(clearDownValues);
    if (clearDownValues) {
      tblFileResults.get()
                    .reset();
    }
  }

  @Override
  protected void onGoClick(final List<String> dirInput) {
    final FileService fileService = getBean(FileService.class);
    List<FilesOrganiseResult> results = fileService.organiseFiles(dirInput);
    List<FolderCreateResult> dirResults = results.stream()
                                                 .map(FilesOrganiseResult::getFolderCreateResult)
                                                 .collect(Collectors.toList());
    Platform.runLater(() -> {
      setUiToResultsView(results);

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

  public void prepareUiWhenNotStandalone(final String dirs, final DirectoryCreateUi directoryCreateUi) {
    isStandalone = false;
    this.previousUi = directoryCreateUi;
    resetUiToStart(true);
    btnRestartOrReset.setText("Back");
    txtDirInput.get()
               .setText(dirs);
  }

  @Override
  protected void restartResetOnClick() {
    if (isStandalone) {
      super.restartResetOnClick();
    } else {
      log.info("Going back to the previous view");
      parentUi.populateView(previousUi);
      if (isResultsShow) {
        previousUi.resetUiToStart(true);
      }
    }
  }
}
