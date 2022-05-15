package uk.ac.man.cs.geraght0.andrew;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.client.RestTemplate;
import uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants;
import uk.ac.man.cs.geraght0.andrew.ui.UI;

@Slf4j
@SpringBootApplication
public class AndrewFolderToolApplication {

  public static void main(String[] args) {
    try {
      Application.launch(UI.class, args);
    } catch (Exception e) {
      log.error("Couldn't start the Spring application context", e);
    }
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
    properties.setIgnoreResourceNotFound(true);
    log.info("Reading properties at location {}", ConfigConstants.PROPERTIES_FILE.getAbsolutePath());

    if (!ConfigConstants.PROPERTIES_FILE.exists()) {
      log.info("Properties file doesn't exist. Creating it with default values");
      ConfigConstants.DEFAULT_CONFIG_VALUES.save();
    }

    properties.setLocation(new FileSystemResource(ConfigConstants.PROPERTIES_FILE));
    return properties;
  }

  @Bean
  public RestTemplate rest() {
    return new RestTemplateBuilder().build();
  }
}