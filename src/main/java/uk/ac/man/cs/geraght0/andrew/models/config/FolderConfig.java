package uk.ac.man.cs.geraght0.andrew.models.config;

import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "app")
public class FolderConfig {
  @Getter
  @Setter
  private Map<String, String> directoryToFilenameFilter;

  public Set<String> deduceSubDirectoryNames() {
    return directoryToFilenameFilter.keySet();
  }
}
