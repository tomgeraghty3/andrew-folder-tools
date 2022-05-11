package uk.ac.man.cs.geraght0.andrew.folders.model.old.model;//package uk.ac.man.cs.geraght0.andrew.folders.model;
//
//import lombok.EqualsAndHashCode;
//import lombok.Value;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Value
//@EqualsAndHashCode(callSuper = true)
//public class FolderCreateUnsuccessfulResult extends FolderCreateResult {
//  Throwable exception;
//
//  public FolderCreateUnsuccessfulResult(Throwable exception) {
//    super(false, generateDisplayText(exception));
//    this.exception = exception;
//  }
//
//  public static String generateDisplayText(final Throwable exception) {
//    return String.format("Directory couldn't be created: %s", exception.getMessage());
//  }
//
//  @Override
//  public void onActionButtonClick() {
//    log.info("Clicked action for {}", this);
//  }
//}
