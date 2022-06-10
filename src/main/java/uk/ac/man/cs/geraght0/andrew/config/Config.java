package uk.ac.man.cs.geraght0.andrew.config;

import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.CN_PLACEHOLDER;
import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.PREFIX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DefaultPropertiesPersister;
import uk.ac.man.cs.geraght0.andrew.service.FileFolderHelpers;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(PREFIX)
public class Config {

  private static File propertiesFile;    //Set during initialisation of the application context (through ConfigConfigurer)
  private static final Map<String, String> DIRECTORY_TO_FILENAME_FILTER;

  //Config values
  private boolean skipVersionUpdateCheck;
  private boolean disallowOverwrite;
  private boolean disableInfoPopupBetweenViews;
  private String lastDirsForFileOrganise;
  private String lastDirForDirCreate;
  private String lastDirNamesForDirCreate;
  private boolean disablePasswordProtect;


  static {
    DIRECTORY_TO_FILENAME_FILTER = new LinkedHashMap<>();
    //The order here is very important:
    DIRECTORY_TO_FILENAME_FILTER.put(String.format("tour/%s/o/videos", CN_PLACEHOLDER), "_preview.mp4");
    DIRECTORY_TO_FILENAME_FILTER.put(String.format("paid/%s/o/videos", CN_PLACEHOLDER), ".mp4");
    DIRECTORY_TO_FILENAME_FILTER.put(String.format("paid/%s/o/artwork", CN_PLACEHOLDER), "artwork_uncensored.jpg");
    DIRECTORY_TO_FILENAME_FILTER.put(String.format("tour/%s/o/artwork", CN_PLACEHOLDER), "artwork.jpg");
    DIRECTORY_TO_FILENAME_FILTER.put(String.format("paid/%s/o/gallery", CN_PLACEHOLDER), "uncensored.jpg");
    DIRECTORY_TO_FILENAME_FILTER.put(String.format("tour/%s/o/gallery", CN_PLACEHOLDER), ".jpg");
  }

  public List<String> deduceSubDirectoryNames(String containerName) {
    return DIRECTORY_TO_FILENAME_FILTER.keySet()
                                       .stream()
                                       .map(s -> FileFolderHelpers.mapDirWithContainerName(s, containerName))
                                       .collect(Collectors.toList());
  }

  public void save() {
    try (FileOutputStream out = createWriterToFile()) {
      // create and set properties into properties object
      Properties props = new Properties();
      props.put(String.format("%s.lastDirsForFileOrganise", PREFIX), StringUtils.isBlank(lastDirsForFileOrganise) ? "" : lastDirsForFileOrganise);
      props.put(String.format("%s.lastDirForDirCreate", PREFIX), StringUtils.isBlank(lastDirForDirCreate) ? "" : lastDirForDirCreate);
      props.put(String.format("%s.lastDirNamesForDirCreate", PREFIX), StringUtils.isBlank(lastDirNamesForDirCreate) ? "" : lastDirNamesForDirCreate);
      props.put(String.format("%s.skipVersionUpdateCheck", PREFIX), String.valueOf(skipVersionUpdateCheck));
      props.put(String.format("%s.disallowOverwrite", PREFIX), String.valueOf(disallowOverwrite));
      props.put(String.format("%s.disableInfoPopupBetweenViews", PREFIX), String.valueOf(disableInfoPopupBetweenViews));
      props.put(String.format("%s.disablePasswordProtect", PREFIX), String.valueOf(disablePasswordProtect));
//      if (DIRECTORY_TO_FILENAME_FILTER != null) {
//        for (Entry<String, String> e : DIRECTORY_TO_FILENAME_FILTER.entrySet()) {
//          props.put(String.format("%s.directoryToFilenameFilter.%s", PREFIX, e.getKey()), StringUtils.isBlank(e.getValue()) ? "" : e.getValue());
//        }
//      }
      // write into it
      DefaultPropertiesPersister p = new DefaultPropertiesPersister();
      p.store(props, out, "");
    } catch (Exception e) {
      log.error("Couldn't update properties file", e);
    }
  }

  private FileOutputStream createWriterToFile() throws IOException {
    File dir = propertiesFile.getParentFile();
    if (!dir.exists()) {
      try {
        FileUtils.forceMkdir(dir);
        log.info("Created missing config directories at \"{}\"", dir.getAbsolutePath());
      } catch (IOException e) {
        log.error("Could not create directories at path \"{}\". Error: {}", dir.getAbsolutePath(), e.getMessage(), e);
        throw e;
      }
    }

    return new FileOutputStream(propertiesFile);
  }

  public Map<String, String> getDirectoryToFilenameFilter() {
    return DIRECTORY_TO_FILENAME_FILTER;
  }

  @PostConstruct
  public void after() {
    if (propertiesFile == null) {
      throw new IllegalStateException("The Application was unable to start correctly. The configuration file was not set");
    }
  }

  public static File getPropertiesFile() {
    return propertiesFile;
  }

  public static void setPropertiesFile(final File propertiesFile) {
    Config.propertiesFile = propertiesFile;
  }
}