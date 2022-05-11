package uk.ac.man.cs.geraght0.andrew.folders.model;

import java.io.File;
import java.util.List;
import lombok.Value;

/**
 * Represents the result of creating multiple directories (and the configured subdirectories) in a root directory
 */
@Value
public class FoldersCreateRequestResult {

  File rootDirectoryToCreateIn;             //The root to put all directories in
  List<String> dirsToCreate;                //The requested names of the directories to create
  List<FolderCreateResult> directories;     //A FolderCreateResult for each requested directory with the result of creating it and the configured subdirectories
}