package uk.ac.man.cs.geraght0.andrew.ui;

import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;
import static uk.ac.man.cs.geraght0.andrew.constants.UiConstants.HEIGHT_OVERALL;
import static uk.ac.man.cs.geraght0.andrew.constants.UiConstants.WIDTH_OVERALL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
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
import uk.ac.man.cs.geraght0.andrew.constants.UiConstants;
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
    final String[] args = getParameters().getRaw()
                                         .toArray(new String[0]);
    try {
      this.applicationContext =
          new SpringApplicationBuilder()
              .sources(AndrewFolderToolApplication.class)
              .web(WebApplicationType.NONE)
              .run(args);
    } catch (final Exception e) {
      final Runnable showDialog = () -> UiHelpers.alertError("The application cannot start: " + e.getMessage());

      final FutureTask<Void> showDialogTask = new FutureTask<>(showDialog, null);
      Platform.runLater(showDialogTask);
      showDialogTask.get();
      throw e;
    }

    executorService = Executors.newFixedThreadPool(2);
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

  public <B> B getBean(final Class<B> beanClass) {
    return applicationContext.getBean(beanClass);
  }

  @Override
  public void start(final Stage primaryStage) throws Exception {
    final Scene scene = generateScene();

    //Populate with default view
    final UiMode modeToDisplay;
    if (getBean(Config.class).isDisablePasswordProtect()) {
      modeToDisplay = UiConstants.VIEW_TO_SHOW_AFTER_PASSWORD;
    } else {
      modeToDisplay = UiMode.PASSWORD_PROTECT;
    }
    final AbsView view = modeToDisplay.createView(this);
    populateView(view);

    final URL resource = ResourceUtils.getURL(String.format("%sstyle.css", CLASSPATH_URL_PREFIX));
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

    executorService.submit(() -> {
      //Check for newer version
      final Optional<String> newVersion = getBean(Backend.class).checkForNewerVersion();
      newVersion.ifPresent(version ->
                               Platform.runLater(() -> {
                                 log.info("A new version was detected at \"{}\". Asking the user if they would like to visit that webpage", version);
                                 final Optional<ButtonType> button = UiHelpers.showAlert(AlertType.WARNING,
                                                                                         "A new version of the application was found. Do you want to open " +
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
                               })
      );
    });
  }

  private Scene generateScene() {
    root = new StackPane();
    root.setAlignment(Pos.CENTER);
    createMenu();
    final VBox mainLayout = new VBox(menuBar, root);
    return new Scene(mainLayout, WIDTH_OVERALL, HEIGHT_OVERALL);
  }

  private void createMenu() {
    final Menu menu = new Menu("Switch App Mode");

    final ToggleGroup toggleGroup = new ToggleGroup();
    final Map<Toggle, UiMode> map = new HashMap<>();
    final UiMode[] modes = UiMode.values();
    Toggle itemToSelect = null;
    for (final UiMode mode : modes) {
      if (mode.isDisplayInMenu()) {
        final RadioMenuItem item = new RadioMenuItem(mode.getDisplayName());
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
                 final UiMode mode = map.get(newValue);
                 final UiMode old = map.get(oldValue);
                 log.info("Changing view to mode {} from {}", mode, old);
                 final AbsView view = mode.createView(this);
                 populateView(view);
               });

    final MenuItem menuSeeConfiguredRules = new MenuItem("See file organise rules");
    final MenuItem menuPopLastUsed = new MenuItem("Populate with last used values");
    final MenuItem menuOpenConfig = new MenuItem("Open configuration file");
    final MenuItem menuOpenConfigDir = new MenuItem("Open configuration file directory");
    final MenuItem menuOpenLogDir = new MenuItem("Open log file directory");
    final MenuItem menuDisplayVersion = new MenuItem("Display version");
    final Menu menuOptions = new Menu("Options", null, menuSeeConfiguredRules, menuPopLastUsed, menuOpenConfig, menuOpenConfigDir, menuOpenLogDir,
                                      menuDisplayVersion);

    menuSeeConfiguredRules.setOnAction(e -> {
      final String rules = UiHelpers.getFileOrganiseFulesAsFriendlyString(getBean(Config.class));
      final String txt = String.format("The following rules for organising files are shown below:\n\n%s", rules);
      UiHelpers.showAlert(AlertType.INFORMATION, txt, "File organise rules");
    });

    menuPopLastUsed.setOnAction(e -> {
      try {
        currentView.populateFromConfig();
        UiHelpers.showAlert(AlertType.INFORMATION, "Populated with last values used (if any)");
      } catch (final Exception ex) {
        UiHelpers.alertError("Failed to populate with the last used values: " + ex.getMessage());
      }
    });
    menuOpenConfig.setOnAction(e -> {
      final String path = Config.getPropertiesFile()
                                .getAbsolutePath();
      runProgram("configuration file", "notepad", path);
    });

    menuOpenConfigDir.setOnAction(e -> {
      final String path = Config.getPropertiesFile()
                                .getParentFile()
                                .getAbsolutePath();
      runProgram("configuration file directory", "explorer", path);
    });
    menuOpenLogDir.setOnAction(e -> {
      final String path = new File(AndrewFolderToolApplication.LOG_DIR).getAbsolutePath();
      runProgram("log file directory", "explorer", path);
    });

    menuDisplayVersion.setOnAction(e -> {
      final Backend b = getBean(Backend.class);
      final String version = b.getBuildProperties()
                              .getVersion();
      UiHelpers.showAlert(AlertType.INFORMATION, String.format("Running version %s of %s", version, b.getBuildProperties()
                                                                                                     .getArtifact()), "Version Information");
    });

    menuBar = new MenuBar(menu, menuOptions);
  }

  private void runProgram(final String description, final String... commands) {
    log.debug("Attempting to run {}", Arrays.toString(commands));
    final String name = System.getProperty("os.name")
                              .toLowerCase();
    if (!name.contains("win")) {
      UiHelpers.alertError("Sorry this feature is only available on Windows");
    } else {
      final ProcessBuilder pb = new ProcessBuilder(commands);
      try {
        final Process process = pb.start();
        log.info("Opened {}. Process: {}", description, process);
      } catch (final IOException ex) {
        final String msg = String.format("Could not open %s. Error: %s", description, ex.getMessage());
        UiHelpers.alertError(msg);
        log.error(msg, ex);
      }
    }
  }

  public void populateView(final AbsView view) {
    currentView = view;
    root.getChildren()
        .clear();
    root.getChildren()
        .add(view);
  }
}