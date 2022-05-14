package uk.ac.man.cs.geraght0.andrew.ui.components;

import static uk.ac.man.cs.geraght0.andrew.constants.UiConstants.MAX_TXT_HEIGHT;
import static uk.ac.man.cs.geraght0.andrew.constants.UiConstants.MIN_TXT_WIDTH;

import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import uk.ac.man.cs.geraght0.andrew.ui.UiHelpers;

@Getter
public class DirChooserPanel extends VBox implements WrapperComp {

  private TextArea txtDirInput;
  private DirectoryChooser fcInput;
  private Button btnSelect;

  public DirChooserPanel(String caption) {
    this(caption, null);
  }

  public DirChooserPanel(String caption, File initialDirectory) {
    build(caption, initialDirectory);
  }

  private void build(String caption, final File initialDirectory) {
    Label lblInfo = new Label(caption);

    txtDirInput = new TextArea();
    txtDirInput.setMinWidth(MIN_TXT_WIDTH);
    txtDirInput.setMaxHeight(MAX_TXT_HEIGHT);
    UiHelpers.addTabKeyNavigationBehaviourToTextArea(txtDirInput);

    fcInput = new DirectoryChooser();
    if (initialDirectory != null && initialDirectory.isDirectory()) {
      fcInput.setInitialDirectory(initialDirectory);
    }

    //Create Button
    btnSelect = new Button("Select");
    Font font = new Font(13);
    btnSelect.setFont(font);
    btnSelect.setOnMouseClicked(e -> {
      File selected = fcInput.showDialog(null);
      if (selected != null) {
        txtDirInput.setText(selected.getAbsolutePath());
        fcInput.setInitialDirectory(selected);
      }
    });

    //Create layout
    HBox laySelected = new HBox(txtDirInput, btnSelect);
    laySelected.setSpacing(20);
    laySelected.setAlignment(Pos.CENTER);

    getChildren().addAll(lblInfo, laySelected);
  }

  public void reset() {
    txtDirInput.setText("");
  }

  public void populateSelectedDir(String dir) {
    txtDirInput.setText(dir);
  }

  public File getChosenDirectory() {
    String txt = txtDirInput.getText();
    if (StringUtils.isBlank(txt)) {
      return null;
    } else {
      return new File(txt);
    }
  }

  @Override
  public void disableComps(final boolean disable) {
    txtDirInput.setDisable(disable);
    btnSelect.setDisable(disable);
  }
}
