package uk.ac.man.cs.geraght0.andrew.ui.view;

import com.google.common.collect.Lists;
import java.io.InputStream;
import java.util.List;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;
import uk.ac.man.cs.geraght0.andrew.constants.UiConstants;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.UiHelpers;

@Slf4j
public class PasswordEntryView extends AbsView {  //NOSONAR - the parent hierarchy allows for UI reuse

  private static final String PASSWORD = "andrew";
  private PasswordField passwordField;

  public PasswordEntryView(final UI ui) {
    super(ui);
    ui.getMenuBar()
      .setDisable(true);
  }

  @Override
  public void populateFromConfig() {
    //Nothing to update
  }

  @Override
  protected void build() {
    final InputStream res = UI.class.getResourceAsStream("/images/lock.png");
    if (res != null) {
      final Image img = new Image(res);
      ImageView imageView = new ImageView(img);
      imageView.setPreserveRatio(true);
      imageView.setFitHeight(100);
      addToView(imageView, HPos.CENTER);
    }

    Label lbl = new Label("This application is password protected\nPlease enter the password to continue");
    lbl.setMaxWidth(Double.MAX_VALUE);
    lbl.setFont(new Font(20));
    lbl.setAlignment(Pos.CENTER);
    addToView(lbl, HPos.CENTER);

    passwordField = new PasswordField();
    Button btn = new Button("Unlock");
    btn.setOnAction(e -> onAction());
    btn.setOnMouseClicked(e -> onAction());

    passwordField.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        btn.fire();
      }
    });

    HBox lay = new HBox(passwordField, btn);
    lay.setMinSize(UiConstants.WIDTH_OVERALL, Region.USE_COMPUTED_SIZE);
    lay.setAlignment(Pos.CENTER);
    lay.setSpacing(10);
    addToView(lay, HPos.CENTER);
  }

  private void onAction() {
    String txt = passwordField.getText();
    if (!PASSWORD.equals(txt)) {
      UiHelpers.alertError("Invalid password");
    } else {
      parentUi.getMenuBar()
              .setDisable(false);
      final UiMode mode = UiMode.BOTH;
      log.info("Password valid. Loading application and populating view with mode: {}", mode);
      AbsView newView = mode.createView(parentUi);
      parentUi.populateView(newView);
    }
  }

  @Override
  protected List<Node> getComponentsToHideDuringProgress() {
    return Lists.newArrayList();
  }

  @Override
  protected List<Node> getComponentsToToggleDisableDuringProgress() {
    return Lists.newArrayList();
  }
}
