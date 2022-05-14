package uk.ac.man.cs.geraght0.andrew.constants;

import com.iberdrola.dtp.util.ConstantMsgGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorMessages implements ConstantMsgGenerator {
  DIR_NULL("A directory was not provided"),
  DIR_NAMES_TO_CREATE_EMPTY("No directory names to create in directory \"%s\" were provided"),
  DIR_NOT_EXIST("The directory at path %s does not exist"),
  DIRS_NOT_EXIST("The directories do not exist: %s"),
  DIR_TO_ORGANISE_EMPTY("The names of the directories to organise is empty"),
  DIR_NAMES_REPEATED("The following directory names appear more than once: %s")
  ;
  private final String msg;

  @Override
  public String getConstantMsg() {
    return msg;
  }
}
