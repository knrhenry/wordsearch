package com.knrhenry.wordsearch;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** AWS Lambda handler for WordSearch puzzle generation. */
public class WordSearchLambdaHandler implements RequestStreamHandler {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void handleRequest(InputStream input, OutputStream output, Context context)
      throws IOException {
    try {
      JsonNode event = objectMapper.readTree(input);
      boolean wantsPdf = false;
      List<String> words = null;
      // Parse input for words and pdf flag
      if (event.has("queryStringParameters")) {
        JsonNode qsp = event.get("queryStringParameters");
        if (qsp.has("pdf")) {
          wantsPdf = qsp.get("pdf").asBoolean(false);
        }
        if (qsp.has("words")) {
          String wordsStr = qsp.get("words").asText("");
          words = parseWords(wordsStr);
        }
      }
      if (words == null && event.has("body")) {
        JsonNode bodyNode = event.get("body");
        if (bodyNode.isTextual()) {
          try {
            bodyNode = objectMapper.readTree(bodyNode.asText());
          } catch (Exception ignored) {
            // Exception ignored intentionally: fallback to original bodyNode
          }
        }
        if (bodyNode.has("pdf")) {
          wantsPdf = bodyNode.get("pdf").asBoolean(false);
        }
        if (bodyNode.has("words")) {
          JsonNode wordsNode = bodyNode.get("words");
          if (wordsNode.isArray()) {
            words =
                objectMapper.convertValue(
                    wordsNode,
                    objectMapper
                        .getTypeFactory()
                        .constructCollectionType(List.class, String.class));
          } else {
            String wordsStr = wordsNode.asText("");
            words = parseWords(wordsStr);
          }
        }
      }
      if (words == null) {
        throw new IllegalArgumentException("No words provided");
      }
      WordSearch ws = new WordSearch(words);
      if (wantsPdf) {
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        new WordSearchPdfGenerator()
            .generatePdf(pdfOut, ws.getGrid(), ws.getGrid().length, ws.getWords());
        final byte[] pdfBytes = pdfOut.toByteArray();
        Map<String, Object> lambdaResp = new HashMap<>();
        lambdaResp.put("statusCode", 200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/pdf");
        headers.put("Content-Disposition", "attachment; filename=wordsearch.pdf");
        lambdaResp.put("headers", headers);
        lambdaResp.put("isBase64Encoded", true);
        lambdaResp.put("body", Base64.getEncoder().encodeToString(pdfBytes));
        objectMapper.writeValue(output, lambdaResp);
      } else {
        // JSON response
        final String result = ws.toJson();
        Map<String, Object> lambdaResp = new HashMap<>();
        lambdaResp.put("statusCode", 200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        lambdaResp.put("headers", headers);
        lambdaResp.put("isBase64Encoded", false);
        lambdaResp.put("body", result);
        objectMapper.writeValue(output, lambdaResp);
      }
    } catch (IllegalArgumentException e) {
      Map<String, Object> errorResp = new HashMap<>();
      errorResp.put("statusCode", 400);
      Map<String, String> headers = new HashMap<>();
      headers.put("Content-Type", "application/json");
      errorResp.put("headers", headers);
      errorResp.put("isBase64Encoded", false);
      errorResp.put("body", String.format("{\"error\":\"%s\"}", e.getMessage()));
      objectMapper.writeValue(output, errorResp);
    } catch (Exception e) {
      Map<String, Object> errorResp = new HashMap<>();
      errorResp.put("statusCode", 500);
      Map<String, String> headers = new HashMap<>();
      headers.put("Content-Type", "application/json");
      errorResp.put("headers", headers);
      errorResp.put("isBase64Encoded", false);
      errorResp.put("body", String.format("{\"error\":\"%s\"}", e.getMessage()));
      objectMapper.writeValue(output, errorResp);
    }
  }

  /**
   * Parses a comma-separated string of words into a list.
   *
   * @param wordsStr the comma-separated string
   * @return list of words
   */
  private static List<String> parseWords(String wordsStr) {
    // Split by comma, trim, and filter out empty
    return Arrays.stream(wordsStr.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
  }
}
