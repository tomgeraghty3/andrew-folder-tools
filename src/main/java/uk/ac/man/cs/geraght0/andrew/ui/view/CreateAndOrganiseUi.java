package uk.ac.man.cs.geraght0.andrew.ui.view;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.FilesOrganiseResult;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.service.FileService;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.components.CompWithCaption;
import uk.ac.man.cs.geraght0.andrew.ui.components.DirChooserPanel;
import uk.ac.man.cs.geraght0.andrew.ui.components.FileOrganiseResultsTreeTbl;

@Slf4j
public class CreateAndOrganiseUi extends AbsViewFolderFile<FilesOrganiseResult, TextField> {//NOSONAR - the parent hierarchy allows for UI reuse

  //UI components
  private DirChooserPanel layDirChooser;
  private CompWithCaption<FileOrganiseResultsTreeTbl> tblFileResults;
  private TabPane tabResults;
  private TextField txtContainerName;

  public CreateAndOrganiseUi(final UI ui) {
    super(ui);
  }

  @Override
  protected List<Node> getComponentsToToggleDisableDuringProgress() {
    List<Node> list = super.getComponentsToToggleDisableDuringProgress();
    list.add(layDirChooser);
    return list;
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

    String dir = config.getLastDirForDirCreate();
    if (!StringUtils.isBlank(dir)) {
      layDirChooser.populateSelectedDir(dir);
    }
  }

  @Override
  protected void updateConfig(final Config config) {
    String dirs = txtDirInput.get()
                             .getText();
    if (!StringUtils.isBlank(dirs)) {
      config.setLastDirsForFileOrganise(dirs);
    }

    String dir = layDirChooser.getChosenDirectory() == null ? null : layDirChooser.getChosenDirectory()
                                                                                  .getAbsolutePath();
    if (!StringUtils.isBlank(dir)) {
      config.setLastDirForDirCreate(dir);
    }
  }

  @Override
  protected TextField generateTextInputComponent() {
    txtContainerName = new TextField();
    return txtContainerName;
  }

  @Override
  protected List<Node> getComponentsToHideDuringProgress() {
    List<Node> nodes = super.getComponentsToHideDuringProgress();
    nodes.add(tabResults);
    return nodes;
  }

  @Override
  protected String getCaptionForDirInput() {
    return "Container name";
  }

  @Override
  protected String getCaptionForDirOutput() {
    return "Directory creation results";
  }

  @Override
  protected String getCaptionForGoButton() {
    return "Create container and organise files";
  }

  @Override
  protected void configureUiForNonStandaloneInResultsView() {
    btnGo.setVisible(false);
    //TODO go back with back button
  }

  @Override
  protected void createViewSpecificUiComponents() {
    //Input
    layDirChooser = new DirChooserPanel("Root Directory containing files to organise");

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
    addToView(layDirChooser);
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
      layDirChooser.reset();
    }
  }

  @Override
  protected void onGoClick() {
    final File rootDir = layDirChooser.getChosenDirectory();
    final String cn = txtContainerName.getText();
    if (StringUtils.isBlank(cn)) {
      throw new IllegalArgumentException("A Container name was not provided");
    }
    final FileService fileService = getBean(FileService.class);
    FilesOrganiseResult organiseResult = fileService.organiseFiles(rootDir, cn);
    FolderCreateResult dirResult = organiseResult.getFolderCreateResult();
    Platform.runLater(() -> {
      setUiToResultsView(Lists.newArrayList(organiseResult));

      tblDirResults.get()
                   .populate(dirResult);
      tblFileResults.get()
                    .populate(organiseResult);
    });
  }

  @Override
  public void configureUiForResults(final boolean resultsOnShow) {
    super.configureUiForResults(resultsOnShow);
    disable(!resultsOnShow, tblFileResults);
    disable(resultsOnShow, layDirChooser);
  }
}