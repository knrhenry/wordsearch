package com.knrhenry.wordsearch;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/** Utility class for generating a PDF representation of a word search puzzle. */
@ApplicationScoped
public class WordSearchPdfGenerator {
  public WordSearchPdfGenerator() {}

  /**
   * Generates a PDF file of the word search grid and word list to an OutputStream using OpenPDF.
   * Adds a header to the PDF.
   *
   * @param out OutputStream to write PDF
   * @param grid The word search grid
   * @param gridSize The size of the grid
   * @param words List of words to include in the word list
   * @throws IOException If PDF generation fails
   */
  public void generatePdf(OutputStream out, char[][] grid, int gridSize, List<String> words)
      throws IOException {
    try {
      Document document = new Document();
      PdfWriter.getInstance(document, out);
      document.open();
      // Add a header to the PDF
      Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
      Paragraph header = new Paragraph("Word Search Puzzle", headerFont);
      header.setAlignment(Paragraph.ALIGN_CENTER);
      document.add(header);
      document.add(new Paragraph(" "));
      // Use a monospaced sans-serif font for the grid (e.g., "DejaVu Sans Mono")
      Font gridFont = FontFactory.getFont("DejaVu Sans Mono", "monospaced", true, 18, Font.BOLD);
      PdfPTable table = new PdfPTable(gridSize);
      table.setWidthPercentage(100);
      for (int r = 0; r < gridSize; r++) {
        for (int c = 0; c < gridSize; c++) {
          PdfPCell cell = new PdfPCell(new Paragraph(String.valueOf(grid[r][c]), gridFont));
          cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
          cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
          cell.setPadding(6f);
          cell.setBorder(PdfPCell.NO_BORDER); // Remove cell border lines
          table.addCell(cell);
        }
      }
      document.add(table);
      document.add(new Paragraph(" "));
      Font wordListFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
      document.add(new Paragraph("Word List:", wordListFont));
      Font wordFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
      StringBuilder wordLine = new StringBuilder();
      int wordsPerLine = 5;
      for (int i = 0; i < words.size(); i++) {
        wordLine.append(words.get(i));
        if ((i + 1) % wordsPerLine == 0 || i == words.size() - 1) {
          document.add(new Paragraph(wordLine.toString(), wordFont));
          wordLine.setLength(0);
        } else {
          wordLine.append(", ");
        }
      }
      document.close();
    } catch (DocumentException e) {
      throw new IOException("Failed to generate PDF", e);
    }
  }
}
