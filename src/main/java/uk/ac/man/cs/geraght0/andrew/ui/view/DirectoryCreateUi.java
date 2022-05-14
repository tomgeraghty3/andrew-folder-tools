package uk.ac.man.cs.geraght0.andrew.ui.view;

import com.google.common.collect.Lists;
import com.iberdrola.dtp.util.SpArrayUtils;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.FoldersCreateRequestResult;
import uk.ac.man.cs.geraght0.andrew.service.FolderService;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.components.CompWithCaption;
import uk.ac.man.cs.geraght0.andrew.ui.components.DirChooserPanel;
import uk.ac.man.cs.geraght0.andrew.ui.components.DirCreateResultsTreeTbl;

@Slf4j
public class DirectoryCreateUi extends AbsUiModeView {

  private DirChooserPanel layDirChooser;
  private CompWithCaption<TextArea> txtDirNamesInput;
  private CompWithCaption<DirCreateResultsTreeTbl> tblResults;
  private Button btnRestartOrReset;
  private Button btnCreate;

  //State
  private boolean isResultsShow;

  public DirectoryCreateUi(final UI ui) {
    super(ui);
  }

  @Override
  protected void populateFromConfig(final Config config) {

  }

  @Override
  protected void build() {
    createUiComponents();
    addListeners();
  }

  @Override
  protected List<Node> getComponentsToHideDuringProgress() {
    return Lists.newArrayList(tblResults);
  }

  @Override
  protected List<Node> getComponentsToToggleDisableDuringProgress() {
    return Lists.newArrayList(layDirChooser, txtDirNamesInput, btnRestartOrReset, btnCreate);
  }

  @Override
  protected Integer getIndexOfProgressBarPlacement() {
    return 2;
  }

  private void createUiComponents() {
    //Input
    layDirChooser = new DirChooserPanel("Root Directory to create containers in");

    final TextArea txtDirNames = new TextArea();
    txtDirNames.setMaxHeight(75);
    txtDirNamesInput = new CompWithCaption<>("Container names (one per line)", txtDirNames);

    //Output
    final DirCreateResultsTreeTbl tbl = new DirCreateResultsTreeTbl(getHostServices());
    tbl.setMaxHeight(500);
    tbl.setDisable(true);
    tblResults = new CompWithCaption<>("Results", tbl);

    //Controls
    BorderPane layButtons = new BorderPane();
    btnRestartOrReset = new Button("Restart");
    btnCreate = new Button("Create containers");
    layButtons.setLeft(btnRestartOrReset);
    layButtons.setRight(btnCreate);
    configureUiForResults(false);

    //Add to grid
    addToView(layDirChooser);
    addToView(txtDirNamesInput);
    addToView(tblResults);
    addToView(layButtons);
  }

  private void addListeners() {
    btnRestartOrReset.setOnMouseClicked(e -> {
      if (isResultsShow) {
        configureUiForResults(false);
      } else {
        layDirChooser.reset();
        txtDirNamesInput.get()
                        .setText("");
        tblResults.get()
                  .reset();
      }
    });

    btnCreate.setOnMouseClicked(this::onGoClick);
  }

  private void onGoClick(final MouseEvent mouseEvent) {
    startProcess(() -> {
      log.info("Waiting 2 seconds");
      String text = txtDirNamesInput.get()
                                    .getText();
      List<String> containerNames = StringUtils.isBlank(text) ? null : SpArrayUtils.stream(text.split("\n"))
                                                                                   .collect(Collectors.toList());
      FolderService folderService = getBean(FolderService.class);
      //Test
//      final File dir = new File("C:\\Users\\Tom\\Desktop\\T");
//      final List<FolderCreateResult> results = Lists.newArrayList(new FolderCreateResult(new OperationMove(dir, dir),
//                                                                                         Lists.newArrayList(
//                                                                                             new OperationFailure(dir, new IllegalStateException("Stub")),
//                                                                                                                  new OperationNotNeeded(dir,
//                                                                                                                                         OperationNotNeeded.DIR_ALREADY_EXISTS_DESC),
//                                                                                                                  new OperationSkipped(dir),
//                                                                                                                  new OperationSuccess(dir))));
//      FoldersCreateRequestResult result = new FoldersCreateRequestResult(null, null, results);
      FoldersCreateRequestResult result = folderService.createDirectories(layDirChooser.getChosenDirectory(), containerNames);
      Platform.runLater(() -> {
        configureUiForResults(true);
        tblResults.get()
                  .populate(result.getDirectories());
      });
    });
  }

  public void configureUiForResults(boolean resultsOnShow) {
    disable(resultsOnShow, txtDirNamesInput, layDirChooser);
    disable(!resultsOnShow, tblResults);
    isResultsShow = resultsOnShow;
    btnCreate.setDisable(isResultsShow);
    btnRestartOrReset.setText(isResultsShow ? "Restart" : "Reset");
  }
}
