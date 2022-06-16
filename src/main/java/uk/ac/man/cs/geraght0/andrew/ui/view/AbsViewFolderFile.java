package uk.ac.man.cs.geraght0.andrew.ui.view;

import com.google.common.collect.Lists;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import lombok.extern.slf4j.Slf4j;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.components.CompWithCaption;
import uk.ac.man.cs.geraght0.andrew.ui.components.DirCreateResultsTreeTbl;

@Slf4j
public abstract class AbsViewFolderFile<T, C extends TextInputControl> extends AbsView {    //NOSONAR - the parent hierarchy allows for UI reuse

  //UI comps
  protected CompWithCaption<C> txtDirInput;
  protected CompWithCaption<DirCreateResultsTreeTbl> tblDirResults;
  protected Button btnRestartOrReset;
  protected Button btnGo;
  protected BorderPane layButtons;

  //State
  protected boolean isResultsShow;
  protected boolean isStandalone;
  protected List<T> lastResults;
  private EventHandler<MouseEvent> goClickEvent;


  protected AbsViewFolderFile(final UI ui) {
    super(ui);
    this.isStandalone = true;     //By default
  }

  @Override
  protected void build() {
    createUiComponents();
    createViewSpecificUiComponents();
    addComponentsToView();
    addListeners();
    resetUiToStart(true);
  }

  protected abstract void createViewSpecificUiComponents();

  protected abstract void addComponentsToView();

  @Override
  protected List<Node> getComponentsToHideDuringProgress() {
    return Lists.newArrayList(tblDirResults);
  }

  @Override
  protected List<Node> getComponentsToToggleDisableDuringProgress() {
    return Lists.newArrayList(txtDirInput, btnRestartOrReset, btnGo);
  }

  @Override
  protected Integer getIndexOfProgressBarPlacement() {
    return 2;
  }

  protected void createUiComponents() {
    final C txtDirNames = generateTextInputComponent();
    txtDirInput = new CompWithCaption<>(getCaptionForDirInput(), txtDirNames);

    //Output
    tblDirResults = createTbl(getCaptionForDirOutput(), new DirCreateResultsTreeTbl(getHostServices()));

    //Controls
    layButtons = new BorderPane();
    btnRestartOrReset = new Button("Reset");
    btnGo = new Button(getCaptionForGoButton());
    layButtons.setLeft(btnRestartOrReset);
    layButtons.setRight(btnGo);
  }

  protected abstract C generateTextInputComponent();

  protected abstract String getCaptionForDirInput();

  protected abstract String getCaptionForDirOutput();

  protected abstract String getCaptionForGoButton();

  protected <U extends TreeTableView<OperationResult>> CompWithCaption<U> createTbl(String caption, final U tbl) {
    tbl.setMaxHeight(500);
    tbl.setDisable(true);
    return new CompWithCaption<>(caption, tbl);
  }

  private void addListeners() {
    btnRestartOrReset.setOnMouseClicked(e -> restartResetOnClick());
    goClickEvent = this::onGoClick;
    btnGo.setOnMouseClicked(goClickEvent);
  }

  protected void restartResetOnClick() {
    resetUiToStart(!isResultsShow);
  }

  protected void setUiToResultsView(List<T> results) {
    btnRestartOrReset.setText("Restart");
    configureUiForResults(true);
    this.lastResults = results;
    if (isStandalone) {
      btnGo.setDisable(true);
    } else {
      configureUiForNonStandaloneInResultsView();
    }
  }

  protected abstract void configureUiForNonStandaloneInResultsView();

  protected void resetUiToStart(final boolean clearDownValues) {
    if (clearDownValues) {
      txtDirInput.get()
                 .setText("");
      tblDirResults.get()
                   .reset();
    }

    btnRestartOrReset.setText("Clear");
    btnGo.setDisable(false);
    btnGo.setText(getCaptionForGoButton());
    btnGo.setOnMouseClicked(goClickEvent);
    configureUiForResults(false);
  }

  private void onGoClick(final MouseEvent mouseEvent) {
    startProcess(() -> {
      log.info("Beginning process for {}", this.getClass()
                                               .getSimpleName());
      Config config = getBean(Config.class);
      try {
        updateConfig(config);
        config.save();
      } catch (Exception e) {
        log.error("Error updating config - continuing. Error: ", e);
      }
      onGoClick();
    });
  }

  protected abstract void onGoClick();

  protected abstract void updateConfig(final Config config);


  public void configureUiForResults(boolean resultsOnShow) {
    disable(resultsOnShow, txtDirInput);
    disable(!resultsOnShow, tblDirResults);
    btnGo.setDisable(false);
    isResultsShow = resultsOnShow;
  }
}
