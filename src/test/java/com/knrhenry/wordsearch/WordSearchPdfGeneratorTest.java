package com.knrhenry.wordsearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

class WordSearchPdfGeneratorTest {

  @Test
  void testGeneratePdfWithTypicalWords() throws Exception {
    List<String> words = List.of("apple", "banana", "cherry");
    WordSearch ws = WordSearch.create(words);
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    byte[] pdfBytes = pdfGen.generatePdf(ws);
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  void testGeneratePdfWithMaxNumberOfShortWords() throws Exception {
    List<String> words =
        List.of(
            "cat", "dog", "bat", "rat", "owl", "fox", "ant", "bee", "cow", "pig", "hen", "elk",
            "ape", "emu", "gnu", "yak", "eel", "ram", "hog", "jay");
    WordSearch ws = WordSearch.create(words);
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    byte[] pdfBytes = pdfGen.generatePdf(ws);
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  void testGeneratePdfWithEmptyWords() throws Exception {
    WordSearch ws = WordSearch.create(List.of("A")); // minimal grid
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    byte[] pdfBytes = pdfGen.generatePdf(ws);
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  void testGeneratePdfWithLongWord() throws Exception {
    String longWord = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCD"; // 30 chars
    List<String> words = List.of(longWord);
    WordSearch ws = WordSearch.create(words);
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    byte[] pdfBytes = pdfGen.generatePdf(ws);
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
    // Ensure only one page is generated
    try (PDDocument doc = PDDocument.load(pdfBytes)) {
      assertThat("PDF should have only one page", doc.getNumberOfPages(), is(1));
      // Extract text and check for all grid rows (allow for spacing between letters)
      PDFTextStripper stripper = new PDFTextStripper();
      String pdfText = stripper.getText(doc);
      char[][] grid = ws.getGrid();
      for (char[] chars : grid) {
        StringBuilder spacedRow = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
          spacedRow.append(chars[i]);
          if (i < chars.length - 1) {
            spacedRow.append(" ");
          }
        }
        assertThat(
            "PDF should contain grid row: " + spacedRow,
            pdfText,
            containsString(String.valueOf(spacedRow)));
      }
    }
  }

  @Test
  void testNullGrid() {
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    assertThrows(
        NullPointerException.class,
        () -> pdfGen.generatePdf(null),
        "Should throw NullPointerException for null WordSearch");
  }

  @Test
  void testSpecialCharactersInWords() throws Exception {
    List<String> words = List.of("café", "naïve", "coöperate", "façade");
    WordSearch ws = WordSearch.create(words);
    WordSearchPdfGenerator pdfGen = new WordSearchPdfGenerator();
    byte[] pdfBytes = pdfGen.generatePdf(ws);
    assertThat("PDF should be larger than 100 bytes", pdfBytes.length, greaterThan(100));
    assertThat("PDF should start with %PDF-", new String(pdfBytes, 0, 5), is("%PDF-"));
  }
}
