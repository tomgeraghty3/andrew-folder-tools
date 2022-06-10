package uk.ac.man.cs.geraght0.andrew.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Get the config location deduced from the Application version
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "spring.config.use-app-version", havingValue = "true")
public class ConfigBasedOnAppVersionConfigurer implements ConfigConfigurer {

  private ConfigBasedOnAppVersionConfigurer() {
  }

  private static BuildProperties buildProperties;

  @SneakyThrows
  private static BuildProperties getBuildProperties() {
    if (ConfigBasedOnAppVersionConfigurer.buildProperties != null) {
      return ConfigBasedOnAppVersionConfigurer.buildProperties;
    } else {
      final ResourceLoader resourceLoader = new DefaultResourceLoader();
      final Resource r = resourceLoader.getResource("classpath:META-INF/build-info.properties");
      if (r.exists()) {
        final Properties prop = PropertiesLoaderUtils.loadProperties(r);

        //Strip "build." from the property name
        final Map<Object, Object> map = prop.entrySet()
                                            .stream()
                                            .collect(Collectors.toMap(e -> {
                                              if (!(e.getKey() instanceof String)) {
                                                return e.getKey();
                                              } else {
                                                final String k = (String) e.getKey();
                                                final String str = "build.";
                                                if (k.startsWith(str)) {
                                                  return k.substring(str.length());
                                                } else {
                                                  return k;
                                                }
                                              }
                                            }, Entry::getValue));
        final Properties p = new Properties();
        p.putAll(map);
        ConfigBasedOnAppVersionConfigurer.buildProperties = new BuildProperties(p);
        return ConfigBasedOnAppVersionConfigurer.buildProperties;
      }
    }

    throw new IllegalStateException("Build properties aren't available");
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    final BuildProperties props = ConfigBasedOnAppVersionConfigurer.getBuildProperties();
    final String version = props.getVersion();
    return ConfigConfigurer.createConfigurer(version);
  }
}
