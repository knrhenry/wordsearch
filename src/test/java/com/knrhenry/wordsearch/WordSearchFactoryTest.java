package com.knrhenry.wordsearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WordSearchFactoryTest {

  @Test
  @DisplayName("Factory.create succeeds with valid words")
  void testFactoryCreateSuccess() throws Exception {
    List<String> words = List.of("cat", "dog", "bird");
    WordSearch ws = WordSearch.create(words);
    assertThat("WordSearch should not be null", ws, notNullValue());
    assertThat("Grid size should be 15 for default", ws.getGrid().length, is(15));
    assertThat("Words should match input", ws.getWords(), is(words));
  }

  @Test
  @DisplayName("Factory.create throws for word > 30 chars")
  void testFactoryCreateWordTooLong() {
    // Use a word with 31 characters
    List<String> words = List.of("abcdefghijklmnopqrstuvwxyzabcde", "cat");
    Exception ex = assertThrows(WordSearchException.class, () -> WordSearch.create(words));
    assertThat(
        "Error message should mention max length",
        ex.getMessage(),
        containsString("exceeds max length"));
  }

  @Test
  @DisplayName("Factory.create throws after 5 failed grid attempts")
  void testFactoryCreateRetryLimit() {
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
  @DisplayName("getGrid returns correct size")
  void testGetGridSize() throws Exception {
    List<String> words = List.of("cat", "encyclopedia");
    WordSearch ws = WordSearch.create(words);
    assertThat("Grid size should be 15 for default", ws.getGrid().length, is(15));
  }

  @Test
  @DisplayName("getWords returns correct list")
  void testGetWords() throws Exception {
    List<String> words = List.of("cat", "dog");
    WordSearch ws = WordSearch.create(words);
    assertThat("Words should match input", ws.getWords(), is(words));
  }

  @Test
  @DisplayName("toJson returns valid JSON")
  void testToJson() throws Exception {
    List<String> words = List.of("cat", "dog");
    WordSearch ws = WordSearch.create(words);
    WordSearchJsonGenerator jsonGen = new WordSearchJsonGenerator();
    String json = jsonGen.generateJson(ws);
    assertThat(
        "JSON should contain grid, words, and input words",
        json,
        allOf(
            containsString("grid"),
            containsString("words"),
            containsString("cat"),
            containsString("dog")));
  }

  @Test
  @DisplayName("Factory.create succeeds with empty word list")
  void testFactoryCreateEmptyWordList() throws Exception {
    WordSearch ws = WordSearch.create(List.of());
    assertThat("WordSearch should not be null", ws, notNullValue());
    assertThat("Grid size should be 15 for default", ws.getGrid().length, is(15));
    assertThat("Words should be empty list", ws.getWords(), is(List.of()));
  }

  @Test
  @DisplayName("Factory.create succeeds with all words at max length")
  void testFactoryCreateMaxLengthWords() throws Exception {
    String longWord = "pseudopseudohypoparathyroidism"; // 30 chars
    List<String> words = List.of(longWord, longWord);
    WordSearch ws = WordSearch.create(words);
    assertThat("WordSearch should not be null", ws, notNullValue());
    assertThat("Grid size should be 30 for max length word", ws.getGrid().length, is(30));
    assertThat("Words should match input", ws.getWords(), is(words));
  }
}
