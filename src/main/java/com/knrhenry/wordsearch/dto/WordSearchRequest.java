package com.knrhenry.wordsearch.dto;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** DTO for word search puzzle generation requests. */
public class WordSearchRequest {
  @Schema(
      description =
          "List of words to include in the puzzle. Max 20 words, each up to 30 characters.",
      maxItems = 20,
      minItems = 1,
      examples = {"[\"apple\",\"banana\",\"cherry\"]"})
  private List<String> words;

  @Schema(
      description = "Set to true to generate a PDF. If false, returns plain text.",
      defaultValue = "false",
      examples = {"false"})
  private boolean pdf;

  @Schema(
      description = "URL for the footer. This will be appended at the bottom of the puzzle.",
      examples = {"http://example.com/footer"})
  private String footerUrl;

  public List<String> getWords() {
    return words;
  }

  public void setWords(List<String> words) {
    this.words = words;
  }

  public boolean isPdf() {
    return pdf;
  }

  public void setPdf(boolean pdf) {
    this.pdf = pdf;
  }

  public String getFooterUrl() {
    return footerUrl;
  }

  public void setFooterUrl(String footerUrl) {
    this.footerUrl = footerUrl;
  }
}
