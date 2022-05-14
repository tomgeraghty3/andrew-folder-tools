package uk.ac.man.cs.geraght0.andrew.config;

import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.PREFIX;
import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.PROPERTIES_FILE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DefaultPropertiesPersister;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(PREFIX)
public class Config {

  //Config
  private static final Map<String, String> DIRECTORY_TO_FILENAME_FILTER;
  private boolean skipVersionUpdateCheck;
  private boolean disallowOverwrite;
  private boolean disableInfoPopupBetweenViews;
  private String lastDirsForFileOrganise;
  private String lastDirForDirCreate;
  private String lastDirNamesForDirCreate;

  static {
    DIRECTORY_TO_FILENAME_FILTER = new LinkedHashMap<>();
 /*
    DVR001 - paid/o/videos (5 videos that don’t have _preview in the name)  .mp4
           - tour/o/videos (5 videos that have _preview in the name)        .mp4
            artwork (artwork.jpg)
            gallery (6 gallery images)                 .jpg,.jpeg
     */
    DIRECTORY_TO_FILENAME_FILTER.put("tour/o/videos", "_preview.mp4");
    DIRECTORY_TO_FILENAME_FILTER.put("paid/o/videos", ".mp4");
    DIRECTORY_TO_FILENAME_FILTER.put("artwork", "artwork.jpg");
    DIRECTORY_TO_FILENAME_FILTER.put("gallery", ".jpg");
  }

  public Set<String> deduceSubDirectoryNames() {
    return DIRECTORY_TO_FILENAME_FILTER.keySet();
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
    File dir = PROPERTIES_FILE.getParentFile();
    if (!dir.exists()) {
      try {
        FileUtils.forceMkdir(dir);
        log.info("Created missing config directories at \"{}\"", dir.getAbsolutePath());
      } catch (IOException e) {
        log.error("Could not create directories at path \"{}\". Error: {}", dir.getAbsolutePath(), e.getMessage(), e);
        throw e;
      }
    }

    return new FileOutputStream(PROPERTIES_FILE);
  }

  public Map<String, String> getDirectoryToFilenameFilter() {
    return DIRECTORY_TO_FILENAME_FILTER;
  }
}