package uk.ac.man.cs.geraght0.andrew.config;

import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.APP_AUTHOR;
import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.APP_NAME;
import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.PROP_FILE_NAME;

import java.io.File;
import net.harawata.appdirs.AppDirsFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

public interface ConfigConfigurer {

  Logger log = LoggerFactory.getLogger(ConfigConfigurer.class);

  static PropertySourcesPlaceholderConfigurer createConfigurer(final String version) {
    PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
    properties.setIgnoreResourceNotFound(false);
    properties.setIgnoreUnresolvablePlaceholders(false);

    Config.setPropertiesFile(deducePropertiesFile(version));
    log.info("Reading properties at location {}", Config.getPropertiesFile()
                                                        .getAbsolutePath());

    if (!Config.getPropertiesFile()
               .exists()) {
      log.info("Properties file doesn't exist. Creating it with default values");
      new Config().save();
    }

    properties.setLocation(new FileSystemResource(Config.getPropertiesFile()));
    return properties;
  }

  static File deducePropertiesFile(String version) {
    if (StringUtils.isBlank(version)) {
      throw new IllegalStateException("Version is missing");
    }
    String filePath = String.format("%s\\%s", AppDirsFactory.getInstance()
                                                            .getSiteDataDir(APP_NAME, version, APP_AUTHOR), PROP_FILE_NAME);
    return new File(filePath);
  }
}
