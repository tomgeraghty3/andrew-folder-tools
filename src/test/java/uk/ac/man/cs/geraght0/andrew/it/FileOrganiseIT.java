package uk.ac.man.cs.geraght0.andrew.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.man.cs.geraght0.andrew.AbsSpringBootTest;
import uk.ac.man.cs.geraght0.andrew.model.FilesOrganiseResult;
import uk.ac.man.cs.geraght0.andrew.model.FolderCreateResult;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationMove;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationNotApplicable;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationNotNeeded;
import uk.ac.man.cs.geraght0.andrew.model.result.OperationResult;
import uk.ac.man.cs.geraght0.andrew.service.FileService;

@SpringBootTest
class FileOrganiseIT extends AbsSpringBootTest {

  @Autowired
  private FileService fileService;

  @Test
  void testE2E_whenOneContainer_shouldCreateAsExpected() {
    //Setup test
    File topDirectory = testSubDirCreationAndExists();
    File videoTour = createFile(topDirectory, "_preview.mp4");
    File artworkTour = createFile(topDirectory, "fileWith_artwork.jpg");
    File galleryTour = createFile(topDirectory, "fileWithoutArtworkInEnding.jpg");
    File videoPaid = createFile(topDirectory, "noPreviewEndingInName.mp4");
    File artworkPaid = createFile(topDirectory, "fileWith_artwork-uncensored.jpg");
    File galleryPaid = createFile(topDirectory, "other-two-uncensored.jpg");
    File unmatchedFile = createFile(topDirectory, "_unmatched-file.txt");

    //Create expected
    OperationMove videoTourExpected = new OperationMove(videoTour, toFile(getTourPrefix(), "videos"));
    OperationMove artworkTourExpected = new OperationMove(artworkTour, toFile(getTourPrefix(), "artwork"));
    OperationMove galleryTourExpected = new OperationMove(galleryTour, toFile(getTourPrefix(), "gallery"));
    OperationMove videoPaidExpected = new OperationMove(videoPaid, toFile(getPaidPrefix(), "videos"));
    OperationMove artworkPaidExpected = new OperationMove(artworkPaid, toFile(getPaidPrefix(), "artwork"));
    OperationMove galleryPaidExpected = new OperationMove(galleryPaid, toFile(getPaidPrefix(), "gallery"));
    OperationNotApplicable unmatchedFileExpected = new OperationNotApplicable(unmatchedFile);

    //Ordered by name
    final List<OperationResult> subDirOperations = Lists.newArrayList(galleryTourExpected, artworkPaidExpected, artworkTourExpected, videoPaidExpected,
                                                                      galleryPaidExpected, videoTourExpected, unmatchedFileExpected);
    FolderCreateResult folderCreateResult = new FolderCreateResult(new OperationNotNeeded(topDirectory), toSubDirFolderAsNotNeeded());
    FilesOrganiseResult expected = new FilesOrganiseResult(topDirectory, folderCreateResult, subDirOperations);

    //check
    FilesOrganiseResult actual = fileService.organiseFiles(topDirectory);
    assertThat(actual).isEqualTo(expected);
    subDirOperations.forEach(o -> {
      File f;
      if (o.getLocation()
           .equals(unmatchedFile)) {
        assertThat(o).isInstanceOf(OperationNotApplicable.class);
        OperationNotApplicable notNeeded = (OperationNotApplicable) o;
        f = notNeeded.getLocation();
      } else {
        assertThat(o).isInstanceOf(OperationMove.class);
        OperationMove move = (OperationMove) o;
        f = new File(move.getDirMovedTo(), o.getLocation()
                                            .getName());
      }
      assertThat(f).exists();
    });
  }
}