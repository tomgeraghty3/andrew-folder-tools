package uk.ac.man.cs.geraght0.andrew.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Deduce the config location based on a static version value in this class
 */
@Slf4j
@Configuration
@ConditionalOnMissingBean(ConfigBasedOnAppVersionConfigurer.class)
@ConditionalOnProperty(value = "spring.config.use-config-version", havingValue = "true", matchIfMissing = true)
public class ConfigBasedOnConfigVersionConfigurer implements ConfigConfigurer {//NOSONAR - needs public otherwise Spring produces "No visible constructors" err

  public static final String CONFIG_VERSION = "1.0";

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return ConfigConfigurer.createConfigurer(ConfigBasedOnConfigVersionConfigurer.CONFIG_VERSION);
  }
}
