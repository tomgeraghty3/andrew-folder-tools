package uk.ac.man.cs.geraght0.andrew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.CN_PLACEHOLDER;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.ac.man.cs.geraght0.andrew.model.DirectoryCriteria;

class FileFolderHelpersTest {

  @Test
  void testMatching_withDiffScenarios_expected() {
    test("artwork", "jpg", "artwork.jpg", true);
    test("artwork", "jpg", "my_artwork.jpg", true);
    test("jpg", "file.txt", false);
    test("jpg", "my.jpg", true);
    test("jpg", "my_artwork.jpg", true);
    test("preview", "jpg", "image_preview.jpg", true);
  }

  @Test
  void testFileEligible_whenAllDiffScenarios_expected() {
    List<DirectoryCriteria> list = new ArrayList<>();
    final String container = "containerName";
    final String one = "one";
    final String two = "two";
    final String three = "three";
    final String four = "four";
    list.add(new DirectoryCriteria(one, "jpg", "artwork"));
    list.add(new DirectoryCriteria(two, "jpg"));
    list.add(new DirectoryCriteria(three, "mp4", "preview"));
    list.add(new DirectoryCriteria(four, "mp4"));
    Optional<String> actual = FileFolderHelpers.isFileEligible(list, container, "myfile");
    assertThat(actual).isEmpty();
    actual = FileFolderHelpers.isFileEligible(list, container, "artwork.jpg");
    assertThat(actual).contains(one);
    actual = FileFolderHelpers.isFileEligible(list, container, "my_artwork.jpg");
    assertThat(actual).contains(one);
    actual = FileFolderHelpers.isFileEligible(list, container, "file.jpg");
    assertThat(actual).contains(two);
    actual = FileFolderHelpers.isFileEligible(list, container, "myfile.jpg");
    assertThat(actual).contains(two);
    actual = FileFolderHelpers.isFileEligible(list, container, "file_preview.mp4");
    assertThat(actual).contains(three);
    actual = FileFolderHelpers.isFileEligible(list, container, "_preview.mp4");
    assertThat(actual).contains(three);
    actual = FileFolderHelpers.isFileEligible(list, container, "file_preview.wrong");
    assertThat(actual).isEmpty();
    actual = FileFolderHelpers.isFileEligible(list, container, "file.mp4");
    assertThat(actual).contains(four);
    actual = FileFolderHelpers.isFileEligible(list, container, "preview.mp4");
    assertThat(actual).contains(three);
    actual = FileFolderHelpers.isFileEligible(list, container, "when preview anywhere in name.mp4");
    assertThat(actual).contains(three);

    String key = String.format("%s/folder", CN_PLACEHOLDER);
    list.add(new DirectoryCriteria(key, ".txt"));
    actual = FileFolderHelpers.isFileEligible(list, container, "artwork.txt");
    String expected = FileFolderHelpers.mapDirWithContainerName(key, container);
    assertThat(actual).contains(expected);
  }

  private void test(final String contains, final String endsWith, final String string, final boolean expectedMatch) {
    boolean matches = FileFolderHelpers.isFileMatchingPattern(endsWith, contains, string);
    assertThat(matches).isEqualTo(expectedMatch);
  }

  private void test(final String endsWith, final String string, final boolean expectedMatch) {
    test(null, endsWith, string, expectedMatch);
  }
}