package uk.ac.man.cs.geraght0.andrew.ui;

import com.sun.javafx.scene.control.behavior.TextAreaBehavior;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import java.util.Optional;
import java.util.stream.Collectors;
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
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.constants.UiConstants;
import uk.ac.man.cs.geraght0.andrew.service.FileFolderHelpers;

public class UiHelpers {

  private UiHelpers() {}

  public static void alertError(final String msg) {
    UiHelpers.showAlert(AlertType.ERROR, msg);
  }

  static void showAlert(final AlertType type, final String msg, final ButtonType... buttons) {
    UiHelpers.showAlert(type, msg, null, buttons);
  }

  public static Optional<ButtonType> showAlert(final AlertType type, final String msg, final String title, final ButtonType... buttons) {
    if (Platform.isFxApplicationThread()) {
      return UiHelpers.createAndShow(type, msg, title, buttons);
    } else {
      Platform.runLater(() -> UiHelpers.createAndShow(type, msg, title, buttons));
      return Optional.empty();
    }
  }

  private static Optional<ButtonType> createAndShow(final AlertType type, final String msg, final String title, final ButtonType... buttons) {
    final Alert alert = UiHelpers.create(type, msg, title, true, buttons);
    return alert.showAndWait();
  }

  private static Alert create(final AlertType type, final String msg, final String title, final boolean setSize, final ButtonType... buttons) {
    final Alert alert = new Alert(type, msg, buttons);
    if (title != null) {
      alert.setTitle(title);
      alert.setHeaderText(title);
    }
    alert.setResizable(true);
    alert.setWidth(Region.USE_PREF_SIZE);
    if (setSize) {
      alert.getDialogPane()
           .setMinHeight(Region.USE_PREF_SIZE);
      alert.getDialogPane()
           .setMinWidth(Region.USE_PREF_SIZE);
      alert.getDialogPane()
           .getChildren()
           .stream()
           .filter(Label.class::isInstance)
           .forEach(node -> {
             final Label lbl = (Label) node;
             lbl.setMinHeight(Region.USE_PREF_SIZE);
             lbl.setMinWidth(Region.USE_PREF_SIZE);
           });
    }
    return alert;
  }

  public static void createAlertWithStackTrace(final Throwable throwable) {
    final String reason = ExceptionUtils.getStackTrace(throwable);
    final String stack = StringUtils.isBlank(reason) ? "Unexpected error" : reason;
    final Alert alert = UiHelpers.create(AlertType.ERROR, stack, "Error", false);
    final double width = UiConstants.WIDTH_OVERALL * .75;
    alert.setWidth(width);
    alert.setHeight(UiConstants.HEIGHT_OVERALL - 100.0);
    alert.getDialogPane()
         .setMinHeight(width);
    alert.getDialogPane()
         .getChildren()
         .stream()
         .filter(Label.class::isInstance)
         .forEach(node -> ((Label) node).setMinHeight(width));
    alert.showAndWait();
  }

  public static void addTabKeyNavigationBehaviourToTextArea(final TextArea textArea) {
    textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode() == KeyCode.TAB) {
        final TextAreaBehavior behaviour = ((TextAreaSkin) textArea.getSkin()).getBehavior();
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

  public static String getFileOrganiseFulesAsFriendlyString(Config config) {
    return config.getDirectoryToFilenameFilter()
                 .stream()
                 .filter(entry -> !StringUtils.isBlank(entry.getEndsWith()))
                 .map(entry -> {
                   String dir = FileFolderHelpers.mapDirWithContainerName(entry.getDirToMoveTo(), "[container]");
                   return entry.getContainsOp()
                               .map(s -> String.format("%s - files of type \"%s\" and containing the word \"%s\"",
                                                       dir, entry.getEndsWith(), s))
                               .orElse(String.format("%s - any other files of type \"%s\"", dir, entry.getEndsWith()));
                 })
                 .collect(Collectors.joining("\n"));
  }
}
