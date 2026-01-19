package com.knrhenry.wordsearch;

import static java.util.stream.IntStream.range;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.knrhenry.wordsearch.dto.WordSearchRequest;
import com.knrhenry.wordsearch.dto.WordSearchResult;
import com.lowagie.text.DocumentException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WordSearchServiceTest {

  @Mock WordSearchPdfGenerator pdfGenerator;
  @Mock WordSearchJsonGenerator jsonGenerator;

  @InjectMocks WordSearchService service;

  @Test
  void testGeneratePuzzleReturnsGridWordsAndJsonForValidRequest() throws Exception {
    WordSearchRequest req = new WordSearchRequest();
    req.setWords(List.of("apple", "banana", "cherry"));
    req.setPdf(false);
    ObjectNode expectedJson =
        new ObjectMapper().createObjectNode().put("key", UUID.randomUUID().toString());
    prepareJsonGeneratorMock(req.getWords(), expectedJson);
    WordSearchResult result = service.generatePuzzle(req);
    assertThat("Grid should not be null", result.getGrid(), notNullValue());
    assertThat(
        "Words should match input", result.getWords(), is(List.of("apple", "banana", "cherry")));
    assertThat("Error should be null for valid request", result.getError(), nullValue());
    assertThat("PDF flag should be false for non-PDF request", result.isPdf(), is(false));
    assertThat("Expected JSON should be returned", result.getJson(), is(expectedJson));
  }

  @Test
  void testGeneratePuzzleReturnsErrorForNullRequest() {
    WordSearchResult result = service.generatePuzzle(null);
    assertThat("Should be error for empty word list", result.isError(), is(true));
    assertThat("Error message should mention empty", result.getError(), containsString("empty"));
  }

  @Test
  void testGeneratePuzzleReturnsErrorForEmptyWordList() {
    WordSearchRequest req = new WordSearchRequest();
    req.setWords(List.of());
    req.setPdf(false);
    WordSearchResult result = service.generatePuzzle(req);
    assertThat("Should be error for empty word list", result.isError(), is(true));
    assertThat("Error message should mention empty", result.getError(), containsString("empty"));
  }

  @Test
  void testGeneratePuzzleReturnsErrorForNullWordList() {
    WordSearchRequest req = new WordSearchRequest();
    req.setWords(null);
    req.setPdf(false);
    WordSearchResult result = service.generatePuzzle(req);
    assertThat("Should be error for empty word list", result.isError(), is(true));
    assertThat("Error message should mention empty", result.getError(), containsString("empty"));
  }

  @Test
  void testGeneratePuzzleReturnsErrorForTooManyWords() {
    WordSearchRequest req = new WordSearchRequest();
    req.setWords(range(0, 21).mapToObj(i -> "word" + i).toList());
    req.setPdf(false);
    WordSearchResult result = service.generatePuzzle(req);
    assertThat("Should be error for too many words", result.isError(), is(true));
    assertThat(
        "Error message should mention too many words",
        result.getError(),
        containsString("Too many words"));
  }

  @Test
  void testGeneratePuzzleReturnsErrorForWordExceedingMaxLength() {
    WordSearchRequest req = new WordSearchRequest();
    req.setWords(List.of("abcdefghijklmnopqrstuvwxyzabcde")); // 31 chars
    req.setPdf(false);
    WordSearchResult result = service.generatePuzzle(req);
    assertThat("Should be error for word too long", result.isError(), is(true));
    assertThat(
        "Error message should mention max length",
        result.getError(),
        containsString("exceeds max length"));
  }

  @Test
  void testGeneratePuzzleSetsPdfBytesAndValidatesWordSearchObject() throws Exception {
    WordSearchRequest req = new WordSearchRequest();
    List<String> expectedWords = List.of("apple", "banana");
    req.setWords(expectedWords);
    req.setPdf(true);
    byte[] expectedBytes = new byte[200];
    preparePdfGeneratorMock(expectedWords, expectedBytes);
    WordSearchResult result = service.generatePuzzle(req);
    assertThat(
        "PDF bytes should be the same as mocked",
        result.getPdfBytes(),
        sameInstance(expectedBytes));
    assertThat("PDF bytes length should be > 100", result.getPdfBytes().length, greaterThan(100));
    assertThat("PDF flag should be true for PDF request", result.isPdf(), is(true));
  }

  @Test
  void testGeneratePuzzleReturnsErrorForIOExceptionDuringPdfGeneration() throws Exception {
    doThrow(new IOException("Simulated PDF failure"))
        .when(pdfGenerator)
        .generatePdf(any(WordSearch.class));
    WordSearchRequest req = new WordSearchRequest();
    req.setWords(List.of("apple"));
    req.setPdf(true);
    WordSearchResult result = service.generatePuzzle(req);
    assertThat("Should be error for PDF generation failure", result.isError(), is(true));
    assertThat(
        "Error message should mention PDF generation failed",
        result.getError(),
        containsString("PDF generation failed: Simulated PDF failure"));
  }

  @Test
  void testGeneratePuzzleReturnsErrorForDocumentExceptionDuringPdfGeneration() throws Exception {
    doThrow(new DocumentException("Simulated PDF failure"))
        .when(pdfGenerator)
        .generatePdf(any(WordSearch.class));
    WordSearchRequest req = new WordSearchRequest();
    req.setWords(List.of("apple"));
    req.setPdf(true);
    WordSearchResult result = service.generatePuzzle(req);
    assertThat("Should be error for PDF generation failure", result.isError(), is(true));
    assertThat(
        "Error message should mention PDF generation failed",
        result.getError(),
        containsString("PDF generation failed: Simulated PDF failure"));
  }

  @Test
  void testGeneratePuzzleReturnsErrorForJsonProcessingExceptionDuringJsonGeneration()
      throws Exception {
    doThrow(new JsonProcessingException("Simulated JSON failure") {})
        .when(jsonGenerator)
        .generateJson(any(WordSearch.class));
    WordSearchRequest req = new WordSearchRequest();
    req.setWords(List.of("apple"));
    req.setPdf(false);
    WordSearchResult result = service.generatePuzzle(req);
    assertThat("Should be error for JSON generation failure", result.isError(), is(true));
    assertThat(
        "Error message should mention JSON generation failed",
        result.getError(),
        containsString("JSON generation failed: Simulated JSON failure"));
  }

  private void preparePdfGeneratorMock(List<String> expectedWords, byte[] expectedBytes)
      throws Exception {
    doAnswer(
            invocation -> {
              WordSearch ws = invocation.getArgument(0);
              assertWordSearchIsAsExpected(expectedWords, ws);
              return expectedBytes;
            })
        .when(pdfGenerator)
        .generatePdf(any(WordSearch.class));
  }

  private void prepareJsonGeneratorMock(List<String> expectedWords, ObjectNode expectedJson)
      throws Exception {
    doAnswer(
            invocation -> {
              WordSearch ws = invocation.getArgument(0);
              assertWordSearchIsAsExpected(expectedWords, ws);
              return expectedJson;
            })
        .when(jsonGenerator)
        .generateJson(any(WordSearch.class));
  }

  private static void assertWordSearchIsAsExpected(List<String> expectedWords, WordSearch ws) {
    assertThat("WordSearch object passed to generator should not be null", ws, notNullValue());
    assertThat("Words in WordSearch should match expected", ws.getWords(), is(expectedWords));
    char[][] grid = ws.getGrid();
    assertThat("Grid in WordSearch should not be null", grid, notNullValue());
    assertThat(
        "Grid should be square", Arrays.stream(grid).allMatch(row -> row.length == grid.length));
    int longestWord = expectedWords.stream().mapToInt(String::length).max().orElse(0);
    assertThat(
        "Grid size should be max of 15 or length of longest word",
        grid.length,
        is(Math.max(15, longestWord)));
  }
}
