package com.knrhenry.wordsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.knrhenry.wordsearch.dto.WordSearchRequest;
import com.knrhenry.wordsearch.dto.WordSearchResult;
import com.lowagie.text.DocumentException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;

/** Service for generating word search puzzles and PDFs. */
@ApplicationScoped
public class WordSearchService {

  @Inject WordSearchPdfGenerator pdfGenerator;
  @Inject WordSearchJsonGenerator jsonGenerator;

  public WordSearchResult generatePuzzle(WordSearchRequest request) {
    WordSearchResult result = new WordSearchResult();
    if (request == null || request.getWords() == null || request.getWords().isEmpty()) {
      result.setError("Word list must not be empty.");
      return result;
    }
    if (request.getWords().size() > 20) {
      result.setError("Too many words. Maximum allowed is 20.");
      return result;
    }
    try {
      WordSearch ws = WordSearch.create(request.getWords());
      result.setGrid(ws.getGrid());
      result.setWords(request.getWords());
      result.setPdf(request.isPdf());
      if (request.isPdf()) {
        result.setPdfBytes(pdfGenerator.generatePdf(ws));
      } else {
        result.setJson(jsonGenerator.generateJson(ws));
      }
    } catch (WordSearchException e) {
      result.setError("Failed to generate Puzzle: " + e.getMessage());
    } catch (JsonProcessingException e) {
      result.setError("JSON generation failed: " + e.getMessage());
    } catch (DocumentException | IOException e) {
      result.setError("PDF generation failed: " + e.getMessage());
    }
    return result;
  }
}
