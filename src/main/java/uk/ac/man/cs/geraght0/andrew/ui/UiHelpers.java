package uk.ac.man.cs.geraght0.andrew.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class UiHelpers {

  public static void alert(final String msg) {
    Platform.runLater(() -> {
      Alert alert = new Alert(AlertType.ERROR, msg);
      alert.setResizable(true);
      alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
      alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
      alert.showAndWait();
    });
  }
}
