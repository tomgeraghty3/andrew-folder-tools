package uk.ac.man.cs.geraght0.andrew.ui;

import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;
import static uk.ac.man.cs.geraght0.andrew.constans.UiConstants.HEIGHT_OVERALL;
import static uk.ac.man.cs.geraght0.andrew.constans.UiConstants.WIDTH_OVERALL;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ResourceUtils;
import uk.ac.man.cs.geraght0.andrew.AndrewFolderToolApplication;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.service.Backend;
import uk.ac.man.cs.geraght0.andrew.ui.view.AbsView;
import uk.ac.man.cs.geraght0.andrew.ui.view.UiMode;

@Slf4j
@Getter
public class UI extends Application {
  private static final UiMode PRE_SELECTED = UiMode.values()[0];

  private ConfigurableApplicationContext applicationContext;

  //UI Components
  private MenuBar menuBar;
  private StackPane root;

  //State
  private Config config;
  private ExecutorService executorService;

  @Override
  public void init() {
    String[] args = getParameters().getRaw()
                                   .toArray(new String[0]);
    this.applicationContext =
        new SpringApplicationBuilder()
            .sources(AndrewFolderToolApplication.class)
            .run(args);
    config = getBean(Config.class);
    executorService = Executors.newSingleThreadExecutor();
  }

  @Override
  public void stop() {
    this.applicationContext.close();
    Platform.exit();
  }

  public <B> B getBean(Class<B> beanClass) {
    return applicationContext.getBean(beanClass);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Scene scene = generateScene();

    //Populate with default view
    AbsView view = PRE_SELECTED.createView(this);
    populateView(view);

    URL resource = ResourceUtils.getURL(String.format("%sstyle.css", CLASSPATH_URL_PREFIX));
    scene.getStylesheets()
         .addAll(resource.toExternalForm());

    //Check for newer version
    Optional<String> newVersion = getBean(Backend.class).checkForNewerVersion();
    newVersion.ifPresent(version -> {
      log.info("A new version was detected at \"{}\". Asking the user if they would like to visit that webpage", version);
      Alert alert = new Alert(AlertType.WARNING, "A new version of the application was found. Do you want to go to GitHub to download the new version?",
                              ButtonType.YES, ButtonType.NO);
      alert.setHeaderText("New version found");
      alert.setTitle("New version found");
      Optional<ButtonType> button = alert.showAndWait();
      if (button.isPresent()) {
        log.info("The user clicked button: \"{}\"", button.get()
                                                          .getText());
        if (button.get()
                  .equals(ButtonType.YES)) {
          getHostServices().showDocument(version);
        }
      }
    });

    primaryStage.getIcons()
                .add(new Image(UI.class.getResourceAsStream("/images/icon.png")));
    primaryStage.setResizable(false);
    primaryStage.setTitle("Tools for Andrew Ward-Jones");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private Scene generateScene() {
    root = new StackPane();
    root.setAlignment(Pos.CENTER);
    createMenu();
    VBox mainLayout = new VBox(menuBar, root);
    return new Scene(mainLayout, WIDTH_OVERALL, HEIGHT_OVERALL);
  }

  private void createMenu() {
    Menu menu = new Menu("Menu 1");
    menu.setGraphic(new ImageView("file:icon.png"));

    ToggleGroup toggleGroup = new ToggleGroup();
    final Map<Toggle, UiMode> map = new HashMap<>();
    final UiMode[] modes = UiMode.values();
    Toggle first = null;
    for (UiMode mode : modes) {
      RadioMenuItem item = new RadioMenuItem(mode.getDisplayName());
      map.put(item, mode);
      toggleGroup.getToggles()
                 .add(item);
      menu.getItems()
          .add(item);

      if (mode == PRE_SELECTED) {
        first = item;
      }
    }

    toggleGroup.selectToggle(first);
    toggleGroup.selectedToggleProperty()
               .addListener((o, oldValue, newValue) -> {
                 UiMode mode = map.get(newValue);
                 UiMode old = map.get(oldValue);
                 log.info("Changing view to mode {} from {}", mode, old);
                 AbsView view = mode.createView(this);
                 populateView(view);
               });
    menuBar = new MenuBar(menu);
  }

  private void populateView(final AbsView view) {
    root.getChildren()
        .clear();
    root.getChildren()
        .add(view);
  }
}