//package uk.ac.man.cs.geraght0.andrew.folders.model;
//
//import java.io.File;
//import lombok.EqualsAndHashCode;
//import lombok.Value;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Value
//@EqualsAndHashCode(callSuper = true)
//public class FolderCreateSuccessResult extends FolderCreateResult {
//  File createdDirectory;
//
//  public FolderCreateSuccessResult(final File createdDirectory) {
//    super(true, generateDisplayText(createdDirectory));
//    this.createdDirectory = createdDirectory;
//  }
//
//  public static String generateDisplayText(final File createdDirectory) {
//    return String.format("%s created", createdDirectory.getName());
//  }
//
//  @Override
//  public void onActionButtonClick() {
//    log.info("Clicked action for {}", this);
//  }
//}
