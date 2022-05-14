package uk.ac.man.cs.geraght0.andrew.ui.components;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.application.HostServices;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;

public abstract class AbsTreeTable<T> extends TreeTableView<OperationResult> {

  protected TreeItem<OperationResult> root;

  public AbsTreeTable(final HostServices hostServices) {
    createContent(hostServices);
  }

  private void createContent(final HostServices hostServices) {
    final TreeTableColumn<OperationResult, String> firstCol = new TreeTableColumn<>(getNameOfFirstColumn());
    firstCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("File"));
    final TreeTableColumn<OperationResult, OperationResult> secondCol = new TreeTableColumn<>("Result");
    secondCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("Result"));
    getColumns().add(firstCol);
    getColumns().add(secondCol);

    root = new TreeItem<>();
    root.setExpanded(true);
    setRoot(root);
    setShowRoot(false);

    firstCol.setCellValueFactory(param -> {
      TreeItem<OperationResult> treeItem = param.getValue();
      OperationResult value = treeItem.getValue();
      return new SimpleStringProperty(value.getLocation()
                                           .getAbsolutePath());
    });
    secondCol.setCellValueFactory(param -> {
      TreeItem<OperationResult> treeItem = param.getValue();
      OperationResult value = treeItem.getValue();
      return new ReadOnlyObjectWrapper<>(value);
    });

    secondCol.setCellFactory(tc -> new HyperlinkCell(hostServices, displayResultDescForDirs()));

    firstCol.prefWidthProperty()
            .bind(widthProperty()
                      .multiply(0.59));
    secondCol.prefWidthProperty()
             .bind(widthProperty()
                       .multiply(0.4));

    setStyle("-fx-selection-bar: white;");
  }

  protected abstract boolean displayResultDescForDirs();

  protected abstract String getNameOfFirstColumn();

  public void populate(final List<T> result) {
    root.getChildren()
        .clear();
    if (result != null) {
      List<TreeItem<OperationResult>> items = result.stream()
                                                    .map(this::mapFilesOrganiseRequestResult)
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.toList());

      root.getChildren()
          .addAll(items);
    }
  }

  private TreeItem<OperationResult> mapFilesOrganiseRequestResult(final T item) {
    TreeItem<OperationResult> dir = new TreeItem<>(getDirResult(item));
    List<OperationResult> subItems = getSubItemResults(item);
    if (subItems.isEmpty()) {
      return null;
    }
    List<TreeItem<OperationResult>> subTreeItems = subItems
        .stream()
        .map(TreeItem::new)
        .collect(Collectors.toList());
    dir.getChildren()
       .addAll(subTreeItems);
    dir.setExpanded(true);
    return dir;
  }

  protected abstract List<OperationResult> getSubItemResults(T item);

  protected abstract OperationResult getDirResult(T item);

  public void reset() {
    root.getChildren()
        .clear();
  }
}
