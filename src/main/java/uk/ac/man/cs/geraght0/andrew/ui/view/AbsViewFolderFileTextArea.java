package uk.ac.man.cs.geraght0.andrew.ui.view;

import com.iberdrola.dtp.util.SpArrayUtils;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.TextArea;
import org.apache.commons.lang3.StringUtils;
import uk.ac.man.cs.geraght0.andrew.ui.UI;
import uk.ac.man.cs.geraght0.andrew.ui.UiHelpers;

/**
 * For any views that need multiple lines of input
 */
public abstract class AbsViewFolderFileTextArea<T> extends AbsViewFolderFile<T, TextArea> {

  protected AbsViewFolderFileTextArea(final UI ui) {
    super(ui);
  }

  @Override
  protected TextArea generateTextInputComponent() {
    TextArea txt = new TextArea();
    txt.setMaxHeight(75);
    UiHelpers.addTabKeyNavigationBehaviourToTextArea(txt);
    return txt;
  }

  @Override
  protected void onGoClick() {
    String text = txtDirInput.get()
                             .getText();
    List<String> dirInput = StringUtils.isBlank(text) ? null : SpArrayUtils.stream(text.split("\n"))
                                                                           .collect(Collectors.toList());
    onGoClick(dirInput);
  }

  protected abstract void onGoClick(final List<String> dirInput);
}
