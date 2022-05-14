package uk.ac.man.cs.geraght0.andrew.ui.view;

import java.util.function.Function;
import lombok.Getter;
import uk.ac.man.cs.geraght0.andrew.ui.UI;

@Getter
public enum UiMode {
  BOTH("Create Directories and Organise Files", ui -> {
    DirectoryCreateUi view = new DirectoryCreateUi(ui);
    view.setStandalone(false);
    return view;
  }),
  CREATE_DIR("Just Create Directories", DirectoryCreateUi::new),
  FILE_ORGANISE("Just Organise Files In Directories", FileOrganiseUi::new),
  ;

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

}
