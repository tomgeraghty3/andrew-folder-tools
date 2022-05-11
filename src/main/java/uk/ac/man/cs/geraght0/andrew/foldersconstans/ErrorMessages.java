package uk.ac.man.cs.geraght0.andrew.foldersconstans;

import com.iberdrola.dtp.util.ConstantMsgGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorMessages implements ConstantMsgGenerator {
  DIR_NULL("A directory was not provided"),
  DIR_NOT_EXIST("The directory at path %s does not exist"),
  DIR_NAMES_REPEATED("The following directory names appear more than once: %s")
  ;
  private final String msg;

  @Override
  public String getConstantMsg() {
    return msg;
  }
}
