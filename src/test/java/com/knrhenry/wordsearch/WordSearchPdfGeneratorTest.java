package com.knrhenry.wordsearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lowagie.text.ExceptionConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WordSearchPdfGeneratorTest {

  @Test
  @DisplayName("PDF generation with typical word list should succeed and produce non-empty output")
  void testGeneratePdfWithTypicalWords() throws IOException {
    List<String> words = Arrays.asList("apple", "banana", "cherry");
    WordSearch ws = new WordSearch(words);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    pdfGen.generatePdf(out, ws.getGrid(), ws.getGrid().length, words);
    byte[] pdfBytes = out.toByteArray();
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  @DisplayName("PDF generation with empty word list should succeed")
  void testGeneratePdfWithEmptyWords() throws IOException {
    List<String> words = Collections.emptyList();
    WordSearch ws = new WordSearch(List.of("A")); // minimal grid
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    pdfGen.generatePdf(out, ws.getGrid(), ws.getGrid().length, words);
    byte[] pdfBytes = out.toByteArray();
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  @DisplayName("PDF generation with long word should succeed")
  void testGeneratePdfWithLongWord() throws IOException {
    String longWord = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCD"; // 30 chars
    List<String> words = List.of(longWord);
    WordSearch ws = new WordSearch(words);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    pdfGen.generatePdf(out, ws.getGrid(), ws.getGrid().length, words);
    byte[] pdfBytes = out.toByteArray();
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  @DisplayName("Null OutputStream should throw NullPointerException")
  void testNullOutputStream() {
    List<String> words = List.of("apple");
    WordSearch ws = new WordSearch(words);
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    assertThrows(
        NullPointerException.class,
        () -> pdfGen.generatePdf(null, ws.getGrid(), ws.getGrid().length, words),
        "Should throw NullPointerException for null OutputStream");
  }

  @Test
  @DisplayName("Null grid should throw NullPointerException")
  void testNullGrid() {
    List<String> words = List.of("apple");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    assertThrows(
        NullPointerException.class,
        () -> pdfGen.generatePdf(out, null, 1, words),
        "Should throw NullPointerException for null grid");
  }

  @Test
  @DisplayName("Null words list should throw NullPointerException")
  void testNullWords() {
    List<String> words = null;
    WordSearch ws = new WordSearch(List.of("apple"));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    assertThrows(
        NullPointerException.class,
        () -> pdfGen.generatePdf(out, ws.getGrid(), ws.getGrid().length, words),
        "Should throw NullPointerException for null words list");
  }

  @Test
  @DisplayName(
      "Malformed grid (inconsistent row lengths) should throw ArrayIndexOutOfBoundsException")
  void testMalformedGrid() {
    char[][] grid = new char[][] {{'A', 'B'}, {'C'}}; // second row is shorter
    List<String> words = List.of("AB", "C");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> pdfGen.generatePdf(out, grid, 2, words),
        "Should throw ArrayIndexOutOfBoundsException for malformed grid");
  }

  @Test
  @DisplayName(
      "OutputStream that throws IOException should propagate as ExceptionConverter with IOException inside its wrapper")
  void testOutputStreamThrowsIOException() {
    List<String> words = List.of("apple");
    WordSearch ws = new WordSearch(words);
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    try (OutputStream out =
        new OutputStream() {
          @Override
          public void write(int b) throws IOException {
            throw new IOException("fail");
          }
        }) {
      ExceptionConverter ex =
          assertThrows(
              ExceptionConverter.class,
              () -> pdfGen.generatePdf(out, ws.getGrid(), ws.getGrid().length, words),
              "Should throw ExceptionConverter for OutputStream IOException");
      Throwable exception = ex.getException();
      assertThat("Exception should be IOException", exception, instanceOf(IOException.class));
    } catch (IOException e) {
      assertThat("Unexpected IOException: " + e.getMessage(), false);
    }
  }

  @Test
  @DisplayName("PDF generation with special characters in words should succeed")
  void testSpecialCharactersInWords() throws IOException {
    List<String> words = List.of("café", "naïve", "coöperate", "façade");
    WordSearch ws = new WordSearch(words);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    pdfGen.generatePdf(out, ws.getGrid(), ws.getGrid().length, words);
    byte[] pdfBytes = out.toByteArray();
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  @DisplayName("PDF generation with single-cell grid should succeed")
  void testSingleCellGrid() throws IOException {
    char[][] grid = new char[][] {{'X'}};
    List<String> words = List.of("X");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    pdfGen.generatePdf(out, grid, 1, words);
    byte[] pdfBytes = out.toByteArray();
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }
}
