package uk.ac.man.cs.geraght0.andrew.ui.components;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;

@Getter
public class CompWithCaption<T extends Node> extends VBox implements WrapperComp { //NOSONAR - the parent hierarchy allows for UI reuse

  private final Label lblInfo;
  private final T component;

  public CompWithCaption(final String caption, final T component) {
    this.lblInfo = new Label(caption);
    lblInfo.setLabelFor(component);
    this.component = component;
    this.getChildren()
        .addAll(lblInfo, component);
  }

  public T get() {
    return component;
  }

  @Override
  public void disableComps(final boolean disable) {
    lblInfo.setDisable(disable);
    component.setDisable(disable);
  }
}
