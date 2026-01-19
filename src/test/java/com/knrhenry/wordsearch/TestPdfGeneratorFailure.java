package com.knrhenry.wordsearch;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.io.IOException;

/** Test alternative for WordSearchPdfGenerator that always throws an IOException. */
@Alternative
@ApplicationScoped
public class TestPdfGeneratorFailure extends WordSearchPdfGenerator {
  /** Always throws an IOException to simulate PDF generation failure. */
  @Override
  public byte[] generatePdf(WordSearch wordSearch) throws IOException {
    throw new IOException("Simulated PDF failure");
  }
}
