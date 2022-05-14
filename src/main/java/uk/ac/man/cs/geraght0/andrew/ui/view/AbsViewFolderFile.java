package uk.ac.man.cs.geraght0.andrew.ui.view;

import com.google.common.collect.Lists;
import com.iberdrola.dtp.util.SpArrayUtils;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.components.CompWithCaption;
import uk.ac.man.cs.geraght0.andrew.ui.components.DirCreateResultsTreeTbl;

@Slf4j
public abstract class AbsViewFolderFile extends AbsView {

  //UI comps
  protected CompWithCaption<TextArea> txtDirInput;
  protected CompWithCaption<DirCreateResultsTreeTbl> tblDirResults;
  protected Button btnRestartOrReset;
  protected Button btnCreate;
  protected BorderPane layButtons;

  //State
  protected boolean isResultsShow;

  public AbsViewFolderFile(final UI ui) {
    super(ui);
  }

  @Override
  protected void build() {
    createUiComponents();
    createViewSpecificUiComponents();
    addComponentsToView();
    configureUiForResults(false);
    addListeners();
  }

  protected abstract void createViewSpecificUiComponents();

  protected abstract void addComponentsToView();

  @Override
  protected List<Node> getComponentsToHideDuringProgress() {
    return Lists.newArrayList(tblDirResults);
  }

  @Override
  protected List<Node> getComponentsToToggleDisableDuringProgress() {
    return Lists.newArrayList(txtDirInput, btnRestartOrReset, btnCreate);
  }

  @Override
  protected Integer getIndexOfProgressBarPlacement() {
    return 2;
  }

  protected void createUiComponents() {
    final TextArea txtDirNames = new TextArea();
    txtDirNames.setMaxHeight(75);
    txtDirInput = new CompWithCaption<>(getCaptionForDirInput(), txtDirNames);

    //Output
    tblDirResults = createTbl(getCaptionForDirOutput(), new DirCreateResultsTreeTbl(getHostServices()));

    //Controls
    layButtons = new BorderPane();
    btnRestartOrReset = new Button("Restart");
    btnCreate = new Button(getCaptionForGoButton());
    layButtons.setLeft(btnRestartOrReset);
    layButtons.setRight(btnCreate);
  }

  protected abstract String getCaptionForDirInput();

  protected abstract String getCaptionForDirOutput();

  protected abstract String getCaptionForGoButton();

  protected <T extends TreeTableView<OperationResult>> CompWithCaption<T> createTbl(String caption, final T tbl) {
    tbl.setMaxHeight(500);
    tbl.setDisable(true);
    return new CompWithCaption<>(caption, tbl);
  }

  private void addListeners() {
    btnRestartOrReset.setOnMouseClicked(e -> {
      restartResetOnClick();
    });

    btnCreate.setOnMouseClicked(this::onGoClick);
  }

  protected void restartResetOnClick() {
    if (isResultsShow) {
      configureUiForResults(false);
    } else {
      txtDirInput.get()
                 .setText("");
      tblDirResults.get()
                   .reset();
    }
  }

  private void onGoClick(final MouseEvent mouseEvent) {
    startProcess(() -> {
      log.info("Beginning process for {}", this.getClass()
                                               .getSimpleName());
      String text = txtDirInput.get()
                               .getText();
      List<String> dirInput = StringUtils.isBlank(text) ? null : SpArrayUtils.stream(text.split("\n"))
                                                                             .collect(Collectors.toList());
      onGoClick(dirInput);
    });
  }

  protected abstract void onGoClick(final List<String> dirInput);

  public void configureUiForResults(boolean resultsOnShow) {
    disable(resultsOnShow, txtDirInput);
    disable(!resultsOnShow, tblDirResults);
    isResultsShow = resultsOnShow;
    btnCreate.setDisable(isResultsShow);
    btnRestartOrReset.setText(isResultsShow ? "Restart" : "Reset");
  }

}
