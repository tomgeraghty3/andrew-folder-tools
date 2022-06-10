package uk.ac.man.cs.geraght0.andrew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.man.cs.geraght0.andrew.constants.ConfigConstants.CN_PLACEHOLDER;

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
    final String container = "containerName";
    final String one = "one";
    final String two = "two";
    final String three = "three";
    final String four = "four";
    map.put(one, "artwork.jpg");
    map.put(two, ".jpg");
    map.put(three, "_preview.mp4");
    map.put(four, ".mp4");
    Optional<String> actual = FileFolderHelpers.isFileEligible(map, container, "myfile");
    assertThat(actual).isEmpty();
    actual = FileFolderHelpers.isFileEligible(map, container, "artwork.jpg");
    assertThat(actual).contains(one);
    actual = FileFolderHelpers.isFileEligible(map, container, "my_artwork.jpg");
    assertThat(actual).contains(one);
    actual = FileFolderHelpers.isFileEligible(map, container, "file.jpg");
    assertThat(actual).contains(two);
    actual = FileFolderHelpers.isFileEligible(map, container, "myfile.jpg");
    assertThat(actual).contains(two);
    actual = FileFolderHelpers.isFileEligible(map, container, "file_preview.mp4");
    assertThat(actual).contains(three);
    actual = FileFolderHelpers.isFileEligible(map, container, "_preview.mp4");
    assertThat(actual).contains(three);
    actual = FileFolderHelpers.isFileEligible(map, container, "file_preview.wrong");
    assertThat(actual).isEmpty();
    actual = FileFolderHelpers.isFileEligible(map, container, "file.mp4");
    assertThat(actual).contains(four);
    actual = FileFolderHelpers.isFileEligible(map, container, "preview.mp4");
    assertThat(actual).contains(four);

    String key = String.format("%s/folder", CN_PLACEHOLDER);
    map.put(key, ".txt");
    actual = FileFolderHelpers.isFileEligible(map, container, "artwork.txt");
    String expected = FileFolderHelpers.mapDirWithContainerName(key, container);
    assertThat(actual).contains(expected);
  }

  private void test(final String pattern, final String string, final boolean expectedMatch) {
    boolean matches = FileFolderHelpers.isFileMatchingPattern(pattern, string);
    assertThat(matches).isEqualTo(expectedMatch);
  }
}