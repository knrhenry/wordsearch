package com.knrhenry.wordsearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for WordSearch core logic. */
public class WordSearchTest {

  /** Tests grid size calculation and exception for too long word. */
  @ParameterizedTest
  @MethodSource("gridSizeProvider")
  void testGridSize(List<String> words, int expectedSize, boolean expectException)
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

  @Test
  void testCreateSuccess() throws Exception {
    List<String> words = List.of("cat", "dog", "bird");
    WordSearch ws = WordSearch.create(words);
    assertThat("WordSearch should not be null", ws, notNullValue());
    assertThat("Grid size should be 15 for default", ws.getGrid().length, is(15));
    assertThat("Words should match input", ws.getWords(), is(words));
  }

  @Test
  void testCreateWordTooLong() {
    // Use a word with 31 characters
    List<String> words = List.of("abcdefghijklmnopqrstuvwxyzabcde", "cat");
    Exception ex = assertThrows(WordSearchException.class, () -> WordSearch.create(words));
    assertThat(
        "Error message should mention max length",
        ex.getMessage(),
        containsString("exceeds max length"));
  }

  @Test
  void testCreateRetryLimit() {
    // 16 words, each 15 characters, no intersecting letters
    List<String> words =
        List.of(
            "AAAAAAAAAAAAAAA",
            "BBBBBBBBBBBBBBB",
            "CCCCCCCCCCCCCCC",
            "DDDDDDDDDDDDDDD",
            "EEEEEEEEEEEEEEE",
            "FFFFFFFFFFFFFFF",
            "GGGGGGGGGGGGGGG",
            "HHHHHHHHHHHHHHH",
            "IIIIIIIIIIIIIII",
            "JJJJJJJJJJJJJJJ",
            "KKKKKKKKKKKKKKK",
            "LLLLLLLLLLLLLLL",
            "MMMMMMMMMMMMMMM",
            "NNNNNNNNNNNNNNN",
            "OOOOOOOOOOOOOOO",
            "PPPPPPPPPPPPPPP");
    Exception ex = assertThrows(WordSearchException.class, () -> WordSearch.create(words));
    assertThat(
        "Error message should mention failed attempts",
        ex.getMessage(),
        containsString("Failed to generate grid after 5 attempts"));
  }

  @Test
  void testGetGridSize() throws Exception {
    List<String> words = List.of("cat", "encyclopedia");
    WordSearch ws = WordSearch.create(words);
    assertThat("Grid size should be 15 for default", ws.getGrid().length, is(15));
  }

  @Test
  void testGetWords() throws Exception {
    List<String> words = List.of("cat", "dog");
    WordSearch ws = WordSearch.create(words);
    assertThat("Words should match input", ws.getWords(), is(words));
  }

  @Test
  void testToJson() throws Exception {
    List<String> words = List.of("cat", "dog");
    WordSearch ws = WordSearch.create(words);
    WordSearchJsonGenerator jsonGen = new WordSearchJsonGenerator();
    ObjectNode json = jsonGen.generateJson(ws);
    assertThat("JSON should contain grid", json.has("grid"));
    assertThat("JSON should contain words", json.has("words"));
    assertThat(
        "Words in JSON should match input", json.get("words").toString(), containsString("cat"));
    assertThat(
        "Words in JSON should match input", json.get("words").toString(), containsString("dog"));
  }

  @Test
  void testCreateEmptyWordList() throws Exception {
    WordSearch ws = WordSearch.create(List.of());
    assertThat("WordSearch should not be null", ws, notNullValue());
    assertThat("Grid size should be 15 for default", ws.getGrid().length, is(15));
    assertThat("Words should be empty list", ws.getWords(), is(List.of()));
  }

  @Test
  void testCreateMaxLengthWords() throws Exception {
    String longWord = "pseudopseudohypoparathyroidism"; // 30 chars
    List<String> words = List.of(longWord, longWord);
    WordSearch ws = WordSearch.create(words);
    assertThat("WordSearch should not be null", ws, notNullValue());
    assertThat("Grid size should be 30 for max length word", ws.getGrid().length, is(30));
    assertThat("Words should match input", ws.getWords(), is(words));
  }
}
