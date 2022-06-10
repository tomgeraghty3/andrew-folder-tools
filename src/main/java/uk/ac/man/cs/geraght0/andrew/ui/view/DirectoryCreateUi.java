package uk.ac.man.cs.geraght0.andrew.ui.view;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.FoldersCreateRequestResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.service.FileFolderHelpers;
import uk.ac.man.cs.geraght0.andrew.service.FolderService;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.UiHelpers;
import uk.ac.man.cs.geraght0.andrew.ui.components.DirChooserPanel;

@Slf4j
public class DirectoryCreateUi extends AbsViewFolderFile<FolderCreateResult> {    //NOSONAR - the parent hierarchy allows for UI reuse

  private DirChooserPanel layDirChooser;

  public DirectoryCreateUi(final UI ui) {
    super(ui);
  }

  @Override
  public void populateFromConfig() {
    log.info("Populating the UI with the last used values");
    Config config = getBean(Config.class);
    String dirNames = config.getLastDirNamesForDirCreate();
    if (!StringUtils.isBlank(dirNames)) {
      txtDirInput.get()
                 .setText(dirNames);
    }
    String dir = config.getLastDirForDirCreate();
    if (!StringUtils.isBlank(dir)) {
      layDirChooser.populateSelectedDir(dir);
    }
  }

  @Override
  protected void updateConfig(final Config config) {
    String dirNames = txtDirInput.get()
                                 .getText();
    if (!StringUtils.isBlank(dirNames)) {
      config.setLastDirNamesForDirCreate(dirNames);
    }
    String dir = layDirChooser.getChosenDirectory() == null ? null : layDirChooser.getChosenDirectory()
                                                                                  .getAbsolutePath();
    if (!StringUtils.isBlank(dir)) {
      config.setLastDirForDirCreate(dir);
    }
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
  protected void configureUiForNonStandaloneInResultsView() {
    btnGo.setDisable(false);
    btnGo.setText("Next");
    btnGo.setOnMouseClicked(e -> {
      if (lastResults == null) {
        throw new IllegalStateException("Next button should not be available because lastResults is null");
      }

      List<File> dirCreated = lastResults.stream()
                                         .map(FolderCreateResult::getDirCreateResult)
                                         .filter(r -> r.isCreateDirSuccess()
                                                       .isPresent() || r.isNotNeeded()
                                                                        .isPresent())
                                         .map(OperationResult::getLocation)
                                         .collect(Collectors.toList());
      if (dirCreated.isEmpty()) {
        UiHelpers.alertError("No directories were successfully created so cannot move onto the next step. Try restarting and creating directories again");
      } else {
        String dirsAsText = dirCreated.stream()
                                      .map(File::getAbsolutePath)
                                      .collect(Collectors.joining("\n"));

        boolean popupDisabled = getBean(Config.class).isDisableInfoPopupBetweenViews();
        boolean continueToNextView = popupDisabled;
        if (!popupDisabled) {
          String subDirs = getBean(Config.class).getDirectoryToFilenameFilter()
                                                .entrySet()
                                                .stream()
                                                .filter(entry -> !StringUtils.isBlank(entry.getValue()))
                                                .map(entry -> {
                                                  String dir = FileFolderHelpers.mapDirWithContainerName(entry.getKey(), "[container]");
                                                  return String.format("%s - files ending with \"%s\"", dir, entry.getValue());
                                                })
                                                .sorted()
                                                .collect(Collectors.joining("\n"));

          final String txt = String.format("The following containers were successfully created:\n%s\n\n\nPopulate these with files now. The next screen will " +
                                           "organise any files into the configured folders:\n\n%s", dirsAsText, subDirs);
          Optional<ButtonType> clickType = UiHelpers.showAlert(AlertType.CONFIRMATION, txt, "Populate Containers With Files",
                                                               ButtonType.NEXT, ButtonType.CANCEL);
          continueToNextView = clickType.isPresent() && clickType.get()
                                                                 .equals(ButtonType.NEXT);
        }

        if (continueToNextView) {
          FileOrganiseUi newUi = new FileOrganiseUi(parentUi);
          newUi.prepareUiWhenNotStandalone(dirsAsText, this);
          log.info("{} complete, moving to next stage: {}", this.getClass()
                                                                .getSimpleName(), newUi.getClass()
                                                                                       .getSimpleName());

          parentUi.populateView(newUi);
        }
      }
    });
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
  protected void resetUiToStart(final boolean clearDownValues) {
    super.resetUiToStart(clearDownValues);
    if (clearDownValues) {
      layDirChooser.reset();
    }
  }

  @Override
  protected void onGoClick(final List<String> dirInput) {
    FolderService folderService = getBean(FolderService.class);
    FoldersCreateRequestResult result = folderService.createDirectories(layDirChooser.getChosenDirectory(), dirInput);
    Platform.runLater(() -> {
      setUiToResultsView(result.getDirectories());
      tblDirResults.get()
                   .populate(result.getDirectories());
    });
  }

  @Override
  public void configureUiForResults(final boolean resultsOnShow) {
    super.configureUiForResults(resultsOnShow);
    disable(resultsOnShow, layDirChooser);
  }

  public void setStandalone(final boolean isStandalone) {
    this.isStandalone = isStandalone;
  }
}
