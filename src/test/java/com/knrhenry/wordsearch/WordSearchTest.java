package com.knrhenry.wordsearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for WordSearch core logic. */
public class WordSearchTest {

  /** Tests grid size calculation and exception for too long word. */
  @ParameterizedTest(name = "Grid size: {1}")
  @MethodSource("gridSizeProvider")
  public void testGridSize(List<String> words, int expectedSize, boolean expectException) {
    if (expectException) {
      assertThrows(
          IllegalArgumentException.class,
          () -> new WordSearch(words),
          "Should throw IllegalArgumentException for too long word");
    } else {
      WordSearch ws = new WordSearch(words);
      assertThat("Grid size should match expected", ws.getGrid().length, is(expectedSize));
    }
  }

  /** Provides test cases for grid size calculation. */
  static Stream<Arguments> gridSizeProvider() {
    return Stream.of(
        Arguments.argumentSet("Short words", List.of("cat", "dog"), 15, false),
        Arguments.argumentSet("Long word", List.of("encyclopedia"), 15, false),
        Arguments.argumentSet(
            "Too long word", List.of("supercalifragilisticexpialidocious"), 15, true));
  }

  /** Tests if a word can be placed in the grid at a given position and direction. */
  @ParameterizedTest(name = "Can place {3}")
  @MethodSource("canPlaceProvider")
  public void testCanPlace(String word, int row, int col, WordSearch.Direction direction) {
    int gridSize = 15;
    char[][] grid = new char[gridSize][gridSize];
    for (char[] r : grid) {
      Arrays.fill(r, ' ');
    }
    assertThat(
        "Should be able to place word",
        WordSearch.canPlace(grid, word, row, col, direction),
        is(true));
  }

  /** Provides test cases for canPlace. */
  static Stream<Arguments> canPlaceProvider() {
    return Stream.of(
        Arguments.argumentSet("Horizontal", "CAT", 0, 0, WordSearch.Direction.HORIZONTAL),
        Arguments.argumentSet("Vertical", "CAT", 0, 0, WordSearch.Direction.VERTICAL),
        Arguments.argumentSet("Diagonal-down", "CAT", 0, 0, WordSearch.Direction.DIAGONAL_DOWN),
        Arguments.argumentSet("Diagonal-up", "CAT", 2, 0, WordSearch.Direction.DIAGONAL_UP));
  }

  /** Tests correct placement of a word in the grid. */
  @ParameterizedTest(name = "Word placement {5}")
  @MethodSource("wordPlacementProvider")
  public void testWordPlacement(
      String word, int row, int col, WordSearch.Direction direction, int gridSize) {
    char[][] grid = new char[gridSize][gridSize];
    for (char[] r : grid) {
      Arrays.fill(r, ' ');
    }
    WordSearch.placeWord(grid, word, row, col, direction);
    boolean found = true;
    for (int i = 0; i < word.length(); i++) {
      int rr = row;
      int cc = col;
      switch (direction) {
        case HORIZONTAL:
          cc = col + i;
          break;
        case VERTICAL:
          rr = row + i;
          break;
        case DIAGONAL_DOWN:
          rr = row + i;
          cc = col + i;
          break;
        case DIAGONAL_UP:
          rr = row + word.length() - 1 - i;
          cc = col + i;
          break;
        default:
          // Defensive: do nothing
          break;
      }
      if (grid[rr][cc] != word.charAt(i)) {
        found = false;
        break;
      }
    }
    assertThat("Word should be placed correctly in the grid", found, is(true));
  }

  /** Provides test cases for word placement. */
  static Stream<Arguments> wordPlacementProvider() {
    return Stream.of(
        Arguments.argumentSet("Horizontal", "CAT", 0, 0, WordSearch.Direction.HORIZONTAL, 15),
        Arguments.argumentSet("Vertical", "CAT", 0, 0, WordSearch.Direction.VERTICAL, 15),
        Arguments.argumentSet("Diagonal-down", "CAT", 0, 0, WordSearch.Direction.DIAGONAL_DOWN, 15),
        Arguments.argumentSet("Diagonal-up", "CAT", 2, 0, WordSearch.Direction.DIAGONAL_UP, 15));
  }
}
