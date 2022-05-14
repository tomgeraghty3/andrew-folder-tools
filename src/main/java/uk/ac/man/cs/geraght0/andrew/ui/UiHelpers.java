package uk.ac.man.cs.geraght0.andrew.ui;

import com.sun.javafx.scene.control.behavior.TextAreaBehavior;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import uk.ac.man.cs.geraght0.andrew.constants.UiConstants;

public class UiHelpers {

  public static void alertError(final String msg) {
    showAlert(AlertType.ERROR, msg);
  }

  public static Optional<ButtonType> showAlert(final AlertType type, String msg, ButtonType... buttons) {
    return showAlert(type, msg, null, buttons);
  }

  public static Optional<ButtonType> showAlert(final AlertType type, String msg, String title, ButtonType... buttons) {
    if (Platform.isFxApplicationThread()) {
      return createAndShow(type, msg, title, buttons);
    } else {
      Platform.runLater(() -> createAndShow(type, msg, title, buttons));
      return Optional.empty();
    }
  }

  private static Optional<ButtonType> createAndShow(final AlertType type, String msg, final String title, ButtonType... buttons) {
    Alert alert = create(type, msg, title, true, buttons);
    return alert.showAndWait();
  }

  private static Alert create(final AlertType type, String msg, final String title, boolean setSize, ButtonType... buttons) {
    Alert alert = new Alert(type, msg, buttons);
    if (title != null) {
      alert.setTitle(title);
      alert.setHeaderText(title);
    }
    alert.setResizable(true);
    if (setSize) {
      alert.getDialogPane()
           .setMinHeight(Region.USE_PREF_SIZE);
      alert.getDialogPane()
           .getChildren()
           .stream()
           .filter(node -> node instanceof Label)
           .forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
    }
    return alert;
  }

  public static void createAlertWithStackTrace(Throwable throwable) {
    String reason = ExceptionUtils.getStackTrace(throwable);
    String stack = StringUtils.isBlank(reason) ? "Unexpected error" : reason;
    Alert alert = create(AlertType.ERROR, stack, "Error", false);
    final double width = UiConstants.WIDTH_OVERALL * .75;
    alert.setWidth(width);
    alert.setHeight(UiConstants.HEIGHT_OVERALL - 100);
    alert.getDialogPane()
         .setMinHeight(width);
    alert.getDialogPane()
         .getChildren()
         .stream()
         .filter(node -> node instanceof Label)
         .forEach(node -> ((Label) node).setMinHeight(width));
    alert.showAndWait();
  }

  public static void addTabKeyNavigationBehaviourToTextArea(TextArea textArea) {
    textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode() == KeyCode.TAB) {
        TextAreaBehavior behaviour = ((TextAreaSkin) textArea.getSkin()).getBehavior();
        if (event.isControlDown()) {
          behaviour.callAction("InsertTab");
        } else if (event.isShiftDown()) {
          behaviour.callAction("TraversePrevious");
        } else {
          behaviour.callAction("TraverseNext");
        }
        event.consume();
      }
    });
  }
}
