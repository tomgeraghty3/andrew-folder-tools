//package uk.ac.man.cs.geraght0.andrew.ui;
//
//import static uk.ac.man.cs.geraght0.andrew.constans.UiConstants.MIN_TXT_WIDTH;
//
//import com.google.common.collect.Lists;
//import com.iberdrola.dtp.util.SpCollectionUtils;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.TreeMap;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//import javafx.application.Platform;
//import javafx.beans.value.ObservableValue;
//import javafx.geometry.Pos;
//import javafx.scene.Node;
//import javafx.scene.control.Accordion;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.RadioButton;
//import javafx.scene.control.TextArea;
//import javafx.scene.control.TitledPane;
//import javafx.scene.control.ToggleGroup;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.scene.text.Font;
//import javafx.scene.text.TextAlignment;
//import javafx.stage.DirectoryChooser;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.MultiValuedMap;
//import uk.ac.man.cs.geraght0.andrew.config.Config;
//import uk.ac.man.cs.geraght0.andrew.ui.components.DirChooserPanel;
//import uk.ac.man.cs.geraght0.andrew.ui.view.AbsUiModeView;
//
//@Slf4j
//public class OldView extends AbsUiModeView {
//
//  private TextArea txtDirInput;
//  private DirectoryChooser fcInput;
//  private TextArea txtDirOutput;
//  private DirectoryChooser fcOutput;
//  private TextArea txtExtension;
//  private ToggleGroup groupByOption;
//  private Map<DirGroupOption, RadioButton> optionToButton;
//  private Accordion accordion;
//  private FilesMovedPanel filesMovedPanel;
//  private FilesFailedPanel filesProblemPanel;
//  private List<Button> selectButtons;
//
//  public OldView(final UI ui) {
//    super(ui);
//  }
//
//  @Override
//  protected void build() {
//    selectButtons = new ArrayList<>();
//    //Row 1
//    createDirInfo();
//    //Row 2
//    createDirInfo();
//    //Row 3
//    createRowThree();
//    //Row 4
//    createResultsAccordion();
//  }
//
//  private void createDirInfo() {
//    //Create layout
//    DirChooserPanel layDirChooserPanel = new DirChooserPanel("Root directory");
//    selectButtons.add(layDirChooserPanel.getBtnSelect());
//
//    //Add to grid
//    this.add(layDirChooserPanel, 0, currentRow);
//    currentRow++;
//  }
//
//  private void createRowThree() {
//    //Create info label
//    Label lblInfo = new Label("Directory names:");
//    //Create radio buttons
//    groupByOption = new ToggleGroup();
//    optionToButton = Arrays.stream(DirGroupOption.values())
//                           .collect(Collectors.toMap(Function.identity(), fgo -> {
//                             RadioButton rb = new RadioButton(fgo.getFriendlyStr());
//                             rb.setToggleGroup(groupByOption);
//                             return rb;
//                           }, (u, v) -> {
//                             throw new IllegalStateException(String.format("Duplicate key %s", u));
//                           }, TreeMap::new));
//    optionToButton.get(DirGroupOption.BEFORE_UNDERSCORE)
//                  .setSelected(true);
//
//    final HBox layOptions = new HBox(optionToButton.values()
//                                                   .toArray(new Node[0]));
//    layOptions.setSpacing(5);
//    VBox layControls = new VBox(5, lblInfo, layOptions);
//    layControls.setAlignment(Pos.CENTER_LEFT);
//
//    //Create extension box
//    txtExtension = new TextArea();
//    txtExtension.setMaxSize(50, 10);
//    txtExtension.setFont(new Font(13));
//    txtExtension.setWrapText(true);
//    txtExtension.textProperty()
//                .addListener((observable, oldValue, newValue) -> {
//                  if (newValue.length() > 4) {
//                    txtExtension.setText(newValue.substring(0, 4));
//                  }
//                });
//    lblInfo = new Label("File extension to look for:  ");
//    lblInfo.setLabelFor(txtExtension);
//    lblInfo.setAlignment(Pos.BOTTOM_RIGHT);
//    HBox layExtension = new HBox(lblInfo, txtExtension);
//    layExtension.setAlignment(Pos.CENTER);
//
//    BorderPane borderPane = new BorderPane();
//    borderPane.setMinWidth(MIN_TXT_WIDTH);
//    borderPane.setLeft(layControls);
//    borderPane.setRight(layExtension);
//
//    final Button btnGo = new Button("Pair files\ninto folders");
//    btnGo.setTextAlignment(TextAlignment.CENTER);
//    btnGo.setOnMouseClicked(this::onGoClick);
//    selectButtons.add(btnGo);
//    HBox layRow = new HBox(borderPane, btnGo);
//    layRow.setSpacing(20);
//    layRow.setAlignment(Pos.CENTER);
//
//    this.add(layRow, 0, currentRow);
//    currentRow++;
////    currentRow += 5;
//  }
//
//  private void createResultsAccordion() {
//    filesMovedPanel = new FilesMovedPanel();
//    filesProblemPanel = new FilesFailedPanel();
//    filesMovedPanel.setDisable(true);
//    filesProblemPanel.setDisable(true);
//    accordion = new Accordion(filesMovedPanel, filesProblemPanel);
//    accordion.expandedPaneProperty()
//             .addListener((ObservableValue<? extends TitledPane> property, final TitledPane oldPane, final TitledPane newPane) -> {
//               if (oldPane != null) {
//                 oldPane.setCollapsible(true);
//               }
//               if (newPane != null) {
//                 Platform.runLater(() -> newPane.setCollapsible(false));
//               }
//             });
//    accordion.setExpandedPane(filesMovedPanel);
////    grid.add(accordion, 0, currentRow);
//
////    TreeItem<String> rootItem = new TreeItem<String>("Inbox");
////    rootItem.setExpanded(true);
////    for (int i = 1; i < 6; i++) {
////      TreeItem<String> item = new TreeItem<String> ("Message" + i);
////      rootItem.getChildren().add(item);
////    }
////    TreeView<String> tree = new TreeView<String> (rootItem);
////
////
////    tree.setCellFactory(tv -> new TreeCell<String>() {
////
////      @Override
////      protected void updateItem(String item, boolean empty) {
////        super.updateItem(item, empty);d
////        if (empty || item == null) {
////        } else {
////          if (item.equals("Message1")) {
//////            setStyle("-fx-background-color: black; -fx-text");
//////            setStyle("-fx-background-color: black; -fx-text");
////          } else {
////            // update for filled cell
//////            setText(item);
//////            setStyle(null);
////          }
////          ;
////          setStyle(null);
////        }
////      }
////
////    });
//
////    grid.add(tree, 0, currentRow);
//
//    currentRow++;
//  }
//
//  @Override
//  protected void populateFromConfig(final Config config) {
//    if (config.getLastInputDirectory() != null) {
//      txtDirInput.setText(config.getLastInputDirectory());
//      File in = new File(config.getLastInputDirectory());
//      if (in.exists()) {
//        fcInput.setInitialDirectory(in);
//      }
//    }
//
//    if (config.getLastDirGroupOption() != null) {
//      final RadioButton radioButton = optionToButton.get(config.getLastDirGroupOption());
//      radioButton.setSelected(true);
//    }
//  }
//
//
//  private void onGoClick(final MouseEvent mouseEvent) {
//    RadioButton btn = (RadioButton) groupByOption.getSelectedToggle();
//    DirGroupOption option = DirGroupOption.parse(btn.getText())
//                                          .orElseThrow(() -> new IllegalStateException("Internal error parsing back from radio buttons"));
//    filesMovedPanel.setDisable(false);
//    filesProblemPanel.setDisable(false);
//    startProcess(() -> {
//      List<FileResult> results = getBackend().process(txtDirInput.getText(), txtDirOutput.getText(), option, txtExtension.getText());
//      MultiValuedMap<Boolean, FileResult> problemToFileResult = SpCollectionUtils.toMultiMap(results, fr -> fr.getProblem() == null,
//                                                                                             Function.identity());
//      Collection<FileResult> problemFiles = problemToFileResult.get(false);
//      Collection<FileResult> happyFiles = problemToFileResult.get(true);
//      Platform.runLater(() -> updateResults(happyFiles, problemFiles));
//    });
//  }
//
//  private void updateResults(final Collection<FileResult> happyFiles, final Collection<FileResult> problemFiles) {
//    filesMovedPanel.setItems(happyFiles);
//    filesProblemPanel.setItems(problemFiles);
//    if (happyFiles.isEmpty() && !problemFiles.isEmpty()) {
//      accordion.setExpandedPane(filesProblemPanel);
//    } else {
//      accordion.setExpandedPane(filesMovedPanel);
//    }
//  }
//
//  @Override
//  protected List<Node> getComponentsToHideDuringProgress() {
//    List<Node> components = Lists.newArrayList(selectButtons);
//    components.add(txtExtension);
//    components.addAll(optionToButton.values());
//    return components;
//  }
//
//  @Override
//  protected List<Node> getComponentsToToggleDisableDuringProgress() {
//    return null;
//  }
//}
