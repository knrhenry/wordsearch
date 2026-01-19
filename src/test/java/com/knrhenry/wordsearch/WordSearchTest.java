package com.knrhenry.wordsearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for WordSearch core logic. */
public class WordSearchTest {

  /** Tests grid size calculation and exception for too long word. */
  @ParameterizedTest
  @MethodSource("gridSizeProvider")
  public void testGridSize(List<String> words, int expectedSize, boolean expectException)
      throws Exception {
    if (expectException) {
      assertThrows(
          WordSearchException.class,
          () -> WordSearch.create(words),
          "Should throw WordSearchException for too long word");
    } else {
      WordSearch ws = WordSearch.create(words);
      assertThat("Grid size should match expected", ws.getGrid().length, is(expectedSize));
    }
  }

  /** Provides test cases for grid size calculation. */
  static Stream<Arguments> gridSizeProvider() {
    return Stream.of(
        Arguments.argumentSet("Short words should fit", List.of("cat", "dog"), 15, false),
        Arguments.argumentSet("Medium word should fit", List.of("encyclopedia"), 15, false),
        Arguments.argumentSet(
            "Word 18 characters should resize to 18", List.of("characteristically"), 18, false),
        Arguments.argumentSet(
            "Word 30 characters should resize to 30",
            List.of("pseudopseudohypoparathyroidism"),
            30,
            false),
        Arguments.argumentSet(
            "Too long word should throw Exception",
            List.of("supercalifragilisticexpialidocious"),
            15,
            true));
  }
}
