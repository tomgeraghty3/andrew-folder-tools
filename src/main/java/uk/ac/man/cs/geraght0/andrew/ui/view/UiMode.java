package uk.ac.man.cs.geraght0.andrew.ui.view;

import java.util.List;
import java.util.function.Function;
import javafx.scene.Node;
import lombok.Getter;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.ui.UI;

@Getter
public enum UiMode {
  FILE_ORGANISE("Organise Files In Directories", FileOrganiseUi::new),
  CREATE_DIR("Create Directories", DirectoryCreateUi::new),
  BLANK("Blank test", BlankView::new);

  private final String displayName;
  private final Function<UI, AbsView> funcToCreateView;
  UiMode(final String displayName,
         final Function<UI, AbsView> funcToCreateView) {
    this.displayName = displayName;
    this.funcToCreateView = funcToCreateView;
  }

  public AbsView createView(final UI ui) {
    return funcToCreateView.apply(ui);
  }

  public static class BlankView extends AbsView {

    public BlankView(final UI ui) {
      super(ui);
    }

    @Override
    protected void populateFromConfig(final Config config) {
      //Do nothing
    }

    @Override
    protected void build() {
      //Do nothing
    }

    @Override
    protected List<Node> getComponentsToHideDuringProgress() {
      return null;
    }

    @Override
    protected List<Node> getComponentsToToggleDisableDuringProgress() {
      return null;
    }
  }
}
