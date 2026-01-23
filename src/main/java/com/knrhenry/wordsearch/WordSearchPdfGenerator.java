package com.knrhenry.wordsearch;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/** Utility class for generating a PDF representation of a word search puzzle. */
@ApplicationScoped
public class WordSearchPdfGenerator {
  public WordSearchPdfGenerator() {}

  /**
   * Generates a PDF file of the word search grid and word list and returns it as a byte array. Adds
   * a header to the PDF.
   *
   * @param wordSearch The WordSearch puzzle instance
   * @return PDF as byte array
   * @throws IOException If PDF generation fails
   * @throws NullPointerException if the WordSearch is null
   */
  public byte[] generatePdf(WordSearch wordSearch, String footerUrl) throws IOException {
    if (wordSearch == null) {
      throw new NullPointerException("WordSearch must not be null");
    }
    char[][] grid = wordSearch.getGrid();
    int gridSize = grid.length;
    List<String> words = wordSearch.getWords();
    try (PDDocument doc = new PDDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      PDPage page = new PDPage(PDRectangle.LETTER);
      doc.addPage(page);
      PDRectangle mediaBox = page.getMediaBox();
      float margin = 36; // 0.5 inch
      float usableWidth = mediaBox.getWidth() - 2 * margin;
      // Calculate cell size so grid fits page width
      float cellSize = usableWidth / gridSize;
      float headerFontSize = 22f;
      PDFont headerFont = PDType1Font.HELVETICA_BOLD;
      String headerText = "Word Search Puzzle";
      float headerTextWidth = headerFont.getStringWidth(headerText) / 1000 * headerFontSize;
      float headerAscent = headerFont.getFontDescriptor().getAscent() / 1000 * headerFontSize;
      float gridTopPadding = 60f; // reduced padding below header for clarity
      float headerY = mediaBox.getHeight() - margin - headerAscent;
      float startY = headerY - gridTopPadding;
      // Calculate grid font size to fit cell
      PDFont gridFont = PDType1Font.COURIER_BOLD;
      float maxFontSizeByWidth = cellSize / (gridFont.getStringWidth("W") / 1000);
      float maxFontSizeByHeight = cellSize / (gridFont.getFontDescriptor().getCapHeight() / 1000);
      int gridFontSize = (int) Math.min(Math.min(maxFontSizeByWidth, maxFontSizeByHeight), 18);
      if (gridFontSize < 8) {
        gridFontSize = 8;
      }
      try (PDPageContentStream content =
          new PDPageContentStream(doc, page, AppendMode.APPEND, true, true)) {
        // Header
        content.beginText();
        content.setFont(headerFont, (int) headerFontSize);
        content.newLineAtOffset(margin + (usableWidth - headerTextWidth) / 2, headerY);
        content.showText(headerText);
        content.endText();
        // Draw grid
        for (int row = 0; row < gridSize; row++) {
          for (int col = 0; col < gridSize; col++) {
            float x = margin + col * cellSize;
            float y = startY - row * cellSize;
            // Draw letter centered in cell
            String letter = String.valueOf(grid[row][col]);
            float textWidth = gridFont.getStringWidth(letter) / 1000 * gridFontSize;
            float textHeight = gridFont.getFontDescriptor().getCapHeight() / 1000 * gridFontSize;
            float textX = x + (cellSize - textWidth) / 2;
            float textY = y + (cellSize - textHeight) / 2;
            content.beginText();
            content.setFont(gridFont, gridFontSize);
            content.newLineAtOffset(textX, textY);
            content.showText(letter);
            content.endText();
          }
        }
        // Draw word list
        float wordListY = startY - gridSize * cellSize - 30;
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 14);
        content.newLineAtOffset(margin, wordListY);
        content.showText("Word List:");
        content.endText();
        content.setFont(PDType1Font.HELVETICA, 12);
        int wordsPerLine = 5;
        StringBuilder wordLine = new StringBuilder();
        float wordY = wordListY - 18;
        for (int i = 0; i < words.size(); i++) {
          wordLine.append(words.get(i));
          if ((i + 1) % wordsPerLine == 0 || i == words.size() - 1) {
            content.beginText();
            content.newLineAtOffset(margin, wordY);
            content.showText(wordLine.toString());
            content.endText();
            wordLine.setLength(0);
            wordY -= 16;
          } else {
            wordLine.append(", ");
          }
        }
        // Draw footer URL if provided
        if (footerUrl != null && !footerUrl.isBlank()) {
          float footerFontSize = 9f;
          PDFont footerFont = PDType1Font.HELVETICA_OBLIQUE;
          float footerY = margin + 8; // 8pt above bottom margin
          float footerTextWidth = footerFont.getStringWidth(footerUrl) / 1000 * footerFontSize;
          float footerX = margin + (usableWidth - footerTextWidth) / 2;
          content.beginText();
          content.setFont(footerFont, footerFontSize);
          content.newLineAtOffset(footerX, footerY);
          content.showText(footerUrl);
          content.endText();
        }
      }
      doc.save(out);
      return out.toByteArray();
    }
  }
}
