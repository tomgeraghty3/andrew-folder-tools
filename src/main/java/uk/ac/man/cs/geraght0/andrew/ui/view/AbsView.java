package uk.ac.man.cs.geraght0.andrew.ui.view;

import static uk.ac.man.cs.geraght0.andrew.constans.UiConstants.HEIGHT_OVERALL;
import static uk.ac.man.cs.geraght0.andrew.constans.UiConstants.MIN_TXT_WIDTH;
import static uk.ac.man.cs.geraght0.andrew.constans.UiConstants.WIDTH_OVERALL;

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
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.UiHelpers;
import uk.ac.man.cs.geraght0.andrew.ui.components.WrapperComp;

@Slf4j
public abstract class AbsView extends GridPane {

  private final UI parent;
  //  protected final StackPane parent;
  protected int currentRow;
  private final ProgressBar progressBar;

  public AbsView(final UI ui) {
    this.parent = ui;
    configure(getRootPane());
    build();
    populateFromConfig(getConfig());
    progressBar = new ProgressBar();
    progressBar.setMinWidth(MIN_TXT_WIDTH);
    progressBar.setVisible(false);
    addToView(progressBar, getIndexOfProgressBarPlacement());
    setHalignment(progressBar, HPos.CENTER);
  }

  protected Integer getIndexOfProgressBarPlacement() {
    return null;
  }

  protected Config getConfig() {
    return parent.getConfig();
  }

  protected StackPane getRootPane() {
    return parent.getRoot();
  }

  protected <B> B getBean(final Class<B> beanClass) {
    return parent.getBean(beanClass);
  }

  protected ExecutorService getExecutorService() {
    return parent.getExecutorService();
  }

  public HostServices getHostServices() {
    return parent.getHostServices();
  }

  protected abstract void populateFromConfig(final Config config);

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
    this.add(component, 0, currentRow);
    currentRow++;
  }

  protected void addToView(final Node component, final Integer place) {
    if (place == null) {
      addToView(component);
    } else {
      this.add(component, 0, place);
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
        UiHelpers.alert(msg);
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
