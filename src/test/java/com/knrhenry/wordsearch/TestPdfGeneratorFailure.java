package com.knrhenry.wordsearch;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/** Test alternative for WordSearchPdfGenerator that always throws an IOException. */
@Alternative
@ApplicationScoped
public class TestPdfGeneratorFailure extends WordSearchPdfGenerator {
  @Override
  public void generatePdf(OutputStream out, char[][] grid, int gridSize, List<String> words)
      throws IOException {
    throw new IOException("Simulated PDF failure");
  }
}
