package uk.ac.man.cs.geraght0.andrew.ui;

import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;
import static uk.ac.man.cs.geraght0.andrew.constants.UiConstants.HEIGHT_OVERALL;
import static uk.ac.man.cs.geraght0.andrew.constants.UiConstants.WIDTH_OVERALL;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
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

  private ConfigurableApplicationContext applicationContext;

  //UI Components
  private MenuBar menuBar;
  private StackPane root;

  //State
  private ExecutorService executorService;
  private AbsView currentView;

  @SneakyThrows
  @Override
  public void init() {
    String[] args = getParameters().getRaw()
                                   .toArray(new String[0]);
    try {
      this.applicationContext =
          new SpringApplicationBuilder()
              .sources(AndrewFolderToolApplication.class)
              .web(WebApplicationType.NONE)
              .run(args);
    } catch (Exception e) {
      final Runnable showDialog = () -> UiHelpers.alertError("The application cannot start: " + e.getMessage());

      FutureTask<Void> showDialogTask = new FutureTask<>(showDialog, null);
      Platform.runLater(showDialogTask);
      showDialogTask.get();
      throw e;
    }

    executorService = Executors.newSingleThreadExecutor();
    TooltipsDefaultsFixer.setTooltipTimers(400, 10000, 200);
  }

  @Override
  @SneakyThrows
  public void stop() {
    log.info("Stopping application");
    executorService.shutdownNow();
    applicationContext.close();
    Platform.exit();
    log.info("Application stopped");
  }

  public <B> B getBean(Class<B> beanClass) {
    return applicationContext.getBean(beanClass);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Scene scene = generateScene();

    //Populate with default view
    UiMode modeToDisplay;
    if (getBean(Config.class).isDisablePasswordProtect()) {
      modeToDisplay = UiMode.BOTH;
    } else {
      modeToDisplay = UiMode.PASSWORD_PROTECT;
    }
    AbsView view = modeToDisplay.createView(this);
    populateView(view);

    URL resource = ResourceUtils.getURL(String.format("%sstyle.css", CLASSPATH_URL_PREFIX));
    scene.getStylesheets()
         .addAll(resource.toExternalForm());

    final InputStream res = UI.class.getResourceAsStream("/images/icon.png");
    if (res != null) {
      primaryStage.getIcons()
                  .add(new Image(res));
    }
    primaryStage.setResizable(false);
    primaryStage.setTitle("Folder Tools for Andrew Ward-Jones");
    primaryStage.setScene(scene);
    primaryStage.show();

    executorService.submit(() -> Platform.runLater(() -> {
      //Check for newer version
      Optional<String> newVersion = getBean(Backend.class).checkForNewerVersion();
      newVersion.ifPresent(version -> {
        log.info("A new version was detected at \"{}\". Asking the user if they would like to visit that webpage", version);
        Optional<ButtonType> button = UiHelpers.showAlert(AlertType.WARNING, "A new version of the application was found. Do you want to open " +
                                                                             "GitHub to download the new version?", "New version found",
                                                          ButtonType.YES, ButtonType.NO);
        if (button.isPresent()) {
          log.info("The user clicked button: \"{}\"", button.get()
                                                            .getText());
          if (button.get()
                    .equals(ButtonType.YES)) {
            getHostServices().showDocument(version);
          }
        }
      });
    }));
  }

  private Scene generateScene() {
    root = new StackPane();
    root.setAlignment(Pos.CENTER);
    createMenu();
    VBox mainLayout = new VBox(menuBar, root);
    return new Scene(mainLayout, WIDTH_OVERALL, HEIGHT_OVERALL);
  }

  private void createMenu() {
    Menu menu = new Menu("Switch App Mode");

    ToggleGroup toggleGroup = new ToggleGroup();
    final Map<Toggle, UiMode> map = new HashMap<>();
    final UiMode[] modes = UiMode.values();
    Toggle itemToSelect = null;
    for (UiMode mode : modes) {
      if (mode.isDisplayInMenu()) {
        RadioMenuItem item = new RadioMenuItem(mode.getDisplayName());
        map.put(item, mode);
        toggleGroup.getToggles()
                   .add(item);
        menu.getItems()
            .add(item);

        if (mode == UiMode.BOTH) {
          itemToSelect = item;
        }
      }
    }

    toggleGroup.selectToggle(itemToSelect);
    toggleGroup.selectedToggleProperty()
               .addListener((o, oldValue, newValue) -> {
                 UiMode mode = map.get(newValue);
                 UiMode old = map.get(oldValue);
                 log.info("Changing view to mode {} from {}", mode, old);
                 AbsView view = mode.createView(this);
                 populateView(view);
               });

    MenuItem menuItem = new MenuItem("Populate with last used values");
    Menu menuOptions = new Menu("Options", null, menuItem);
    menuItem.setOnAction(e -> {
      try {
        currentView.populateFromConfig();
        UiHelpers.showAlert(AlertType.INFORMATION, "Populated with last values used (if any)");
      } catch (Exception ex) {
        UiHelpers.alertError("Failed to populate with the last used values: " + ex.getMessage());
      }
    });

    menuBar = new MenuBar(menu, menuOptions);
  }

  public void populateView(final AbsView view) {
    currentView = view;
    root.getChildren()
        .clear();
    root.getChildren()
        .add(view);
  }
}