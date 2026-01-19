package com.knrhenry.wordsearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WordSearchPdfGeneratorTest {

  @Test
  @DisplayName("PDF generation with typical word list should succeed and produce non-empty output")
  void testGeneratePdfWithTypicalWords() throws Exception {
    List<String> words = List.of("apple", "banana", "cherry");
    WordSearch ws = WordSearch.create(words);
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    byte[] pdfBytes = pdfGen.generatePdf(ws);
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  @DisplayName("PDF generation with typical word list should succeed and produce non-empty output")
  void testGeneratePdfWithMaxNumberOfWords() throws Exception {
    List<String> words =
        List.of(
            "apple",
            "banana",
            "cherry",
            "date",
            "fig",
            "grape",
            "kiwi",
            "lemon",
            "mango",
            "nectarine",
            "orange",
            "papaya",
            "quince",
            "raspberry",
            "strawberry",
            "tangerine",
            "ugli",
            "voavanga",
            "watermelon",
            "xigua");
    WordSearch ws = WordSearch.create(words);
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    byte[] pdfBytes = pdfGen.generatePdf(ws);
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  @DisplayName("PDF generation with empty word list should succeed")
  void testGeneratePdfWithEmptyWords() throws Exception {
    WordSearch ws = WordSearch.create(List.of("A")); // minimal grid
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    byte[] pdfBytes = pdfGen.generatePdf(ws);
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  @DisplayName("PDF generation with long word should succeed")
  void testGeneratePdfWithLongWord() throws Exception {
    String longWord = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCD"; // 30 chars
    List<String> words = List.of(longWord);
    WordSearch ws = WordSearch.create(words);
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    byte[] pdfBytes = pdfGen.generatePdf(ws);
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  @DisplayName("Null grid should throw NullPointerException")
  void testNullGrid() {
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    assertThrows(
        NullPointerException.class,
        () -> pdfGen.generatePdf(null),
        "Should throw NullPointerException for null WordSearch");
  }

  @Test
  @DisplayName("PDF generation with special characters in words should succeed")
  void testSpecialCharactersInWords() throws Exception {
    List<String> words = List.of("café", "naïve", "coöperate", "façade");
    WordSearch ws = WordSearch.create(words);
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    byte[] pdfBytes = pdfGen.generatePdf(ws);
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }
}
