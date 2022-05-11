package uk.ac.man.cs.geraght0.andrew.folders.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FileFolderHelpersTest {

  @Test
  void testMatching_withDiffScenarios_expected() {
    test("artwork.jpg", "artwork.jpg", true);
    test("artwork.jpg", "my_artwork.jpg", true);
    test("jpg", "file.txt", false);
    test("jpg", "my.jpg", true);
    test("jpg", "my_artwork.jpg", true);
    test("preview.jpg", "image_preview.jpg", true);
  }

  @Test
  void testFileEligible_whenAllDiffScenarios_expected() {
    Map<String, String> map = new LinkedHashMap<>();
    final String one = "one";
    final String two = "two";
    final String three = "three";
    final String four = "four";
    map.put(one, "artwork.jpg");
    map.put(two, ".jpg");
    map.put(three, "_preview.mp4");
    map.put(four, ".mp4");
    Optional<String> actual = FileFolderHelpers.isFileEligible(map, "myfile");
    assertThat(actual).isEmpty();
    actual = FileFolderHelpers.isFileEligible(map, "artwork.jpg");
    assertThat(actual).contains(one);
    actual = FileFolderHelpers.isFileEligible(map, "my_artwork.jpg");
    assertThat(actual).contains(one);
    actual = FileFolderHelpers.isFileEligible(map, "file.jpg");
    assertThat(actual).contains(two);
    actual = FileFolderHelpers.isFileEligible(map, "myfile.jpg");
    assertThat(actual).contains(two);
    actual = FileFolderHelpers.isFileEligible(map, "file_preview.mp4");
    assertThat(actual).contains(three);
    actual = FileFolderHelpers.isFileEligible(map, "_preview.mp4");
    assertThat(actual).contains(three);
    actual = FileFolderHelpers.isFileEligible(map, "file_preview.wrong");
    assertThat(actual).isEmpty();
    actual = FileFolderHelpers.isFileEligible(map, "file.mp4");
    assertThat(actual).contains(four);
    actual = FileFolderHelpers.isFileEligible(map, "preview.mp4");
    assertThat(actual).contains(four);
  }

  private void test(final String pattern, final String string, final boolean expectedMatch) {
    boolean matches = FileFolderHelpers.isFileMatchingPattern(pattern, string);
    assertThat(matches).isEqualTo(expectedMatch);
  }
}