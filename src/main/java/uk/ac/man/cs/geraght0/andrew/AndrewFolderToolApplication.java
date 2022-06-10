package uk.ac.man.cs.geraght0.andrew;

import static uk.ac.man.cs.geraght0.andrew.config.ConfigBasedOnConfigVersionConfigurer.CONFIG_VERSION;
import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.APP_AUTHOR;
import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.APP_NAME;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import net.harawata.appdirs.AppDirsFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.ac.man.cs.geraght0.andrew.ui.UI;

@Slf4j
@Configuration
@SpringBootApplication
public class AndrewFolderToolApplication {

  public static void main(String[] args) {
    String logDir = String.format("%s", AppDirsFactory.getInstance()
                                                      .getSiteDataDir(APP_NAME, CONFIG_VERSION, APP_AUTHOR));
    System.setProperty("LOGS", logDir);
    System.out.println("Setting log directory to: " + logDir);      //NOSONAR this is to print the log path before SLF4J has instantiated
    try {
      Application.launch(UI.class, args);
    } catch (Exception e) {
      log.error("Couldn't start the Spring application context", e);
    }
  }

  @Bean
  public RestTemplate rest() {
    return new RestTemplateBuilder().build();
  }
}