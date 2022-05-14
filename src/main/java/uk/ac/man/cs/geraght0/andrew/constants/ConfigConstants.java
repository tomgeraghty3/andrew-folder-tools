package uk.ac.man.cs.geraght0.andrew.constants;

import java.io.File;
import net.harawata.appdirs.AppDirsFactory;
import uk.ac.man.cs.geraght0.andrew.config.Config;

public class ConfigConstants {

  //Constants
  public static final String PREFIX = "folder-tool";
  public static final String PROP_FILE_NAME = String.format("%s.properties", PREFIX);
  public static final String APP_AUTHOR = "GERAGHT0";
  public static final String APP_NAME = "ANDREW_TOOLS";
  public static final String VERSION = "1";
  public static final String PROPERTIES_FILE_LOC = String.format("%s\\%s", AppDirsFactory.getInstance()
                                                                                         .getSiteDataDir(APP_NAME, VERSION, APP_AUTHOR), PROP_FILE_NAME);
  public static final File PROPERTIES_FILE = new File(PROPERTIES_FILE_LOC);
  public static final Config DEFAULT_CONFIG_VALUES = new Config();
}
