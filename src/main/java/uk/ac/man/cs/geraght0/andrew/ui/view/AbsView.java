package uk.ac.man.cs.geraght0.andrew.ui.view;

import static uk.ac.man.cs.geraght0.andrew.constants.UiConstants.HEIGHT_OVERALL;
import static uk.ac.man.cs.geraght0.andrew.constants.UiConstants.MIN_TXT_WIDTH;
import static uk.ac.man.cs.geraght0.andrew.constants.UiConstants.WIDTH_OVERALL;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.UiHelpers;
import uk.ac.man.cs.geraght0.andrew.ui.components.WrapperComp;

@Slf4j
public abstract class AbsView extends GridPane { //NOSONAR - the parent hierarchy allows for UI reuse

  protected final UI parentUi;
  protected int currentRow;
  private final ProgressBar progressBar;

  protected AbsView(final UI ui) {
    this.parentUi = ui;
    configure(getRootPane());
    build();
    progressBar = new ProgressBar();
    progressBar.setMinWidth(MIN_TXT_WIDTH);
    progressBar.setVisible(false);
    addToView(progressBar, getIndexOfProgressBarPlacement());
    setHalignment(progressBar, HPos.CENTER);
  }

  protected Integer getIndexOfProgressBarPlacement() {
    return null;
  }

  protected StackPane getRootPane() {
    return parentUi.getRoot();
  }

  protected <B> B getBean(final Class<B> beanClass) {
    return parentUi.getBean(beanClass);
  }

  protected ExecutorService getExecutorService() {
    return parentUi.getExecutorService();
  }

  public HostServices getHostServices() {
    return parentUi.getHostServices();
  }

  public abstract void populateFromConfig();

  protected abstract void build();

  private void configure(final StackPane parent) {
    this.setHgap(10);
    this.setVgap(10);
    this.setAlignment(Pos.CENTER);
    this.setPadding(new Insets(10, 10, 10, 10));
    this.setMinSize(WIDTH_OVERALL, HEIGHT_OVERALL);
    NumberBinding maxScale = Bindings.min(parent.widthProperty()
                                                .divide(WIDTH_OVERALL),
                                          parent.heightProperty()
                                                .divide(HEIGHT_OVERALL));
    this.scaleXProperty()
        .bind(maxScale);
    this.scaleYProperty()
        .bind(maxScale);
  }

  protected void addToView(final Node component) {
    addToView(component, (HPos) null);
  }

  protected void addToView(final Node component, HPos alignment) {
    this.add(component, 0, currentRow);
    if (alignment != null) {
      setHalignment(component, alignment);
    }
    currentRow++;
  }

  protected void addToView(final Node component, final Integer place) {
    add(component, place, null);
  }

  protected void add(final Node component, final Integer place, final HPos alignment) {
    if (place == null) {
      addToView(component, alignment);
    } else {
      this.add(component, 0, place);
      if (alignment != null) {
        setHalignment(component, alignment);
      }
    }
  }

  protected void disable(final boolean disable, Node... components) {
    for (Node n : components) {
      n.setDisable(disable);
      if (n instanceof WrapperComp) {
        WrapperComp componentWrappingAJavaFxNode = (WrapperComp) n;
        componentWrappingAJavaFxNode.disableComps(disable);
      }
    }
  }

  protected void startProcess(Runnable toRun) {
    List<Node> toggleComponents = getComponentsToToggleDisableDuringProgress();
    Map<Node, Boolean> nodeToExistingDisableState = toggleComponents.stream()
                                                 .collect(Collectors.toMap(Function.identity(), Node::isDisabled));
    getExecutorService().submit(() -> {
      try {
        getComponentsToShowDuringProgress().forEach(n -> n.setVisible(true));
        getComponentsToHideDuringProgress().forEach(n -> n.setVisible(false));
        getComponentsToToggleDisableDuringProgress().forEach(b -> b.setDisable(true));

        toRun.run();
      } catch (Exception e) {
        log.error("Uncaught error", e);
        String msg = e.getMessage() == null ? "Unexpected error" : e.getMessage();
        UiHelpers.alertError(msg);
      } finally {
        //Revert
        getComponentsToShowDuringProgress().forEach(n -> n.setVisible(false));
        getComponentsToHideDuringProgress().forEach(n -> n.setVisible(true));
        for (Entry<Node, Boolean> e : nodeToExistingDisableState.entrySet()) {
          Node component = e.getKey();
          boolean wasDisabledBeforeProgress = e.getValue();
          component.setDisable(wasDisabledBeforeProgress);
        }
      }
    });
  }

  protected List<Node> getComponentsToShowDuringProgress() {
    return Lists.newArrayList(progressBar);
  }

  protected abstract List<Node> getComponentsToHideDuringProgress();

  protected abstract List<Node> getComponentsToToggleDisableDuringProgress();
}
