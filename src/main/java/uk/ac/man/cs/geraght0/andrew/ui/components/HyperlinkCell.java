package uk.ac.man.cs.geraght0.andrew.ui.components;

import java.io.File;
import java.util.Optional;
import javafx.application.HostServices;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationFailure;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.ui.UiHelpers;

@Slf4j
public class HyperlinkCell extends TreeTableCell<OperationResult, OperationResult> {

  private HostServices hostServices;
  private final Hyperlink link;

  private final EventHandler<? super MouseEvent> openDir = e -> {
    String path = getItem().getDirectoryToOpenOnAction()
                           .toURI()
                           .toString();
    log.debug("Opening: {}", path);
    hostServices.showDocument(path);
  };

  private final EventHandler<? super MouseEvent> openError = e -> {
    Optional<OperationFailure> failedOp = getItem().isFailed();
    if (!failedOp.isPresent()) {
      throw new IllegalStateException("The error event handler was registered on click of result but the operation did not fail");
    }
    OperationFailure failed = failedOp.get();
    Throwable failure = failed.getFailure();
    String reason = ExceptionUtils.getStackTrace(failure);
    String msg = StringUtils.isBlank(reason) ? "Unexpected error" : reason;
    UiHelpers.alert(msg);
  };

  private final static Tooltip OPEN_DIR_TOOLTIP = new Tooltip("Click to open the directory");
  private final static Tooltip SEE_FAILURE_TOOLTIP = new Tooltip("Click to see failure details");

  public HyperlinkCell(HostServices hostServices) {
    this.hostServices = hostServices;
    link = new Hyperlink();
  }


  @Override
  protected void updateItem(final OperationResult item, final boolean empty) {
    super.updateItem(item, empty);
    boolean setEmpty = true;
    if (!empty) {
      setText(null);
      File dir = item.getDirectoryToOpenOnAction();
      link.setText(item.getResultDescription());
      if (dir != null) {
        setEmpty = false;
        setTooltip(OPEN_DIR_TOOLTIP);
        link.setOnMouseClicked(openDir);
      } else if (item.isFailed()
                     .isPresent()) {
        setEmpty = false;
        setTooltip(SEE_FAILURE_TOOLTIP);
        link.setOnMouseClicked(openError);
      } else if (item.isSkipped()
                     .isPresent()) {
        setTooltip(null);
        setText(item.getResultDescription());
      } else {
        throw new IllegalStateException("There is no logic considered for this type of operation result: " + item.getClass()
                                                                                                                 .getSimpleName());
      }
    }
    setGraphic(setEmpty ? null : link);
  }
}