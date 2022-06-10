package uk.ac.man.cs.geraght0.andrew.ui.view;

import java.util.function.Function;
import lombok.Getter;
import uk.ac.man.cs.geraght0.andrew.ui.UI;

@Getter
public enum UiMode {
  PASSWORD_PROTECT("PasswordProtectView", false, PasswordEntryView::new),
  BOTH("Create Directories and Organise Files", true, ui -> {
    DirectoryCreateUi view = new DirectoryCreateUi(ui);
    view.setStandalone(false);
    return view;
  }),
  CREATE_DIR("Just Create Directories", true, DirectoryCreateUi::new),
  FILE_ORGANISE("Just Organise Files In Directories", true, FileOrganiseUi::new),
  ;

  private final String displayName;
  private final boolean displayInMenu;
  private final Function<UI, AbsView> funcToCreateView;

  UiMode(final String displayName, final boolean displayInMenu,
         final Function<UI, AbsView> funcToCreateView) {
    this.displayName = displayName;
    this.displayInMenu = displayInMenu;
    this.funcToCreateView = funcToCreateView;
  }

  public AbsView createView(final UI ui) {
    return funcToCreateView.apply(ui);
  }

}
