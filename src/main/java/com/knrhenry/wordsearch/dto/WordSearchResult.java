package com.knrhenry.wordsearch.dto;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** DTO for word search puzzle generation results. */
public class WordSearchResult {
  @Schema(description = "The generated word search grid.")
  private char[][] grid;

  @Schema(description = "The list of words included in the puzzle.")
  private List<String> words;

  @Schema(description = "PDF bytes if a PDF was requested, otherwise null.")
  private byte[] pdfBytes;

  @Schema(description = "Error message if an error occurred, otherwise null.")
  private String error;

  @Schema(description = "True if the result is a PDF, false for JSON.")
  private boolean isPdf;

  @Schema(description = "JSON representation of the puzzle, if requested.")
  private ObjectNode json;

  public char[][] getGrid() {
    return grid;
  }

  public void setGrid(char[][] grid) {
    this.grid = grid;
  }

  public List<String> getWords() {
    return words;
  }

  public void setWords(List<String> words) {
    this.words = words;
  }

  public byte[] getPdfBytes() {
    return pdfBytes;
  }

  public void setPdfBytes(byte[] pdfBytes) {
    this.pdfBytes = pdfBytes;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public boolean isPdf() {
    return isPdf;
  }

  public void setPdf(boolean isPdf) {
    this.isPdf = isPdf;
  }

  public ObjectNode getJson() {
    return json;
  }

  public void setJson(ObjectNode json) {
    this.json = json;
  }

  public boolean isError() {
    return error != null;
  }
}
