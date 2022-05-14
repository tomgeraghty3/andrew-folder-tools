package uk.ac.man.cs.geraght0.andrew.ui.view;

import com.google.common.collect.Lists;
import com.iberdrola.dtp.util.SpArrayUtils;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.FilesOrganiseResult;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.service.FileService;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.components.CompWithCaption;
import uk.ac.man.cs.geraght0.andrew.ui.components.DirCreateResultsTreeTbl;
import uk.ac.man.cs.geraght0.andrew.ui.components.FileOrganiseResultsTreeTbl;

@Slf4j
public class FileOrganiseUi extends AbsUiModeView {

  private CompWithCaption<TextArea> txtDirInput;
  private CompWithCaption<DirCreateResultsTreeTbl> tblDirResults;
  private CompWithCaption<FileOrganiseResultsTreeTbl> tblFileResults;
  private Button btnRestartOrReset;
  private Button btnAllocate;

  //State
  private boolean isResultsShow;

  public FileOrganiseUi(final UI ui) {
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
    return Lists.newArrayList(tblDirResults);
  }

  @Override
  protected List<Node> getComponentsToToggleDisableDuringProgress() {
    return Lists.newArrayList(txtDirInput, btnRestartOrReset, btnAllocate);
  }

  @Override
  protected Integer getIndexOfProgressBarPlacement() {
    return 2;
  }

  private void createUiComponents() {
    //Input
    final TextArea txtDirNames = new TextArea();
    txtDirNames.setMaxHeight(75);
    txtDirInput = new CompWithCaption<>("Directories to organise (one per line)", txtDirNames);

    //Output tab 1
    tblFileResults = createTbl("File organise results", new FileOrganiseResultsTreeTbl(getHostServices()));
    Tab tabFileResults = new Tab("View File Results", tblFileResults);
    //Output tab 2
    tblDirResults = createTbl("Directory creation results", new DirCreateResultsTreeTbl(getHostServices()));
    Tab tabFolderResults = new Tab("View Folder Results", tblDirResults);
    //Pane
    TabPane tabResults = new TabPane(tabFileResults, tabFolderResults);
    tabResults.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

    //Controls
    BorderPane layButtons = new BorderPane();
    btnRestartOrReset = new Button("Restart");
    btnAllocate = new Button("Organise files in directories");
    layButtons.setLeft(btnRestartOrReset);
    layButtons.setRight(btnAllocate);
    configureUiForResults(false);

    //Add to grid
    addToView(txtDirInput);
    addToView(tabResults);
    addToView(layButtons);
  }

  private <T extends TreeTableView<OperationResult>> CompWithCaption<T> createTbl(String caption, final T tbl) {
    tbl.setMaxHeight(500);
    tbl.setDisable(true);
    return new CompWithCaption<>(caption, tbl);
  }

  private void addListeners() {
    btnRestartOrReset.setOnMouseClicked(e -> {
      if (isResultsShow) {
        configureUiForResults(false);
      } else {
        txtDirInput.get()
                   .setText("");
        tblDirResults.get()
                     .reset();
      }
    });

    btnAllocate.setOnMouseClicked(this::onGoClick);
  }

  private void onGoClick(final MouseEvent mouseEvent) {
    startProcess(() -> {
      log.info("Waiting 2 seconds");
      String text = txtDirInput.get()
                               .getText();
      List<String> dirNames = StringUtils.isBlank(text) ? null : SpArrayUtils.stream(text.split("\n"))
                                                                                   .collect(Collectors.toList());
      final FileService fileService = getBean(FileService.class);
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
      List<FilesOrganiseResult> results = fileService.organiseFiles(dirNames);
      List<FolderCreateResult> dirResults = results.stream()
                                                .map(FilesOrganiseResult::getFolderCreateResult)
                                                .collect(Collectors.toList());
      Platform.runLater(() -> {
        configureUiForResults(true);
        tblDirResults.get()
                  .populate(dirResults);
        tblFileResults.get().populate(results);
      });
    });
  }

  public void configureUiForResults(boolean resultsOnShow) {
    disable(resultsOnShow, txtDirInput);
    disable(!resultsOnShow, tblFileResults, tblDirResults);
    isResultsShow = resultsOnShow;
    btnAllocate.setDisable(isResultsShow);
    btnRestartOrReset.setText(isResultsShow ? "Restart" : "Reset");
  }
}
