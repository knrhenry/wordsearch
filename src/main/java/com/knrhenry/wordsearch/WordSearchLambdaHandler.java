package com.knrhenry.wordsearch;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

  private final WordSearchPdfGenerator pdfGenerator;
  private final WordSearchJsonGenerator jsonGenerator;

  // Default constructor for Lambda
  public WordSearchLambdaHandler() {
    this.pdfGenerator = new WordSearchPdfGenerator();
    this.jsonGenerator = new WordSearchJsonGenerator();
  }

  // Constructor for tests (manual injection)
  WordSearchLambdaHandler(
      WordSearchPdfGenerator pdfGenerator, WordSearchJsonGenerator jsonGenerator) {
    this.pdfGenerator = pdfGenerator;
    this.jsonGenerator = jsonGenerator;
  }

  @Override
  public void handleRequest(InputStream input, OutputStream output, Context context)
      throws IOException {
    try {
      JsonNode event = objectMapper.readTree(input);
      List<String> words = null;
      boolean wantsPdf = false;
      // Expect only body as JSON
      if (!event.has("body")) {
        throw new WordSearchException("No request body provided");
      }
      JsonNode bodyNode = event.get("body");
      if (bodyNode.isTextual()) {
        bodyNode = objectMapper.readTree(bodyNode.asText());
      }
      if (!bodyNode.has("words") || !bodyNode.get("words").isArray()) {
        throw new WordSearchException("'words' must be an array in the request body");
      }
      words =
          objectMapper.convertValue(
              bodyNode.get("words"),
              objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
      if (bodyNode.has("pdf")) {
        wantsPdf = bodyNode.get("pdf").asBoolean(false);
      }
      String footerUrl = bodyNode.has("footerUrl") ? bodyNode.get("footerUrl").asText("") : "";
      WordSearch ws = WordSearch.create(words);
      byte[] pdfBytes = null;
      ObjectNode jsonNode = null;
      if (wantsPdf) {
        pdfBytes = pdfGenerator.generatePdf(ws, footerUrl);
      } else {
        jsonNode = jsonGenerator.generateJson(ws);
      }
      writeSuccessResponse(output, wantsPdf, pdfBytes, jsonNode);
    } catch (WordSearchException e) {
      writeErrorResponse(output, 400, e.getMessage());
    } catch (Exception e) {
      writeErrorResponse(output, 500, e.getMessage());
    }
  }

  private static List<String> parseWords(String wordsStr) {
    // Split by comma, trim, and filter out empty
    return Arrays.stream(wordsStr.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
  }

  private void writeSuccessResponse(
      OutputStream output, boolean isPdf, byte[] pdfBytes, ObjectNode json) throws IOException {
    Map<String, Object> lambdaResp = new HashMap<>();
    lambdaResp.put("statusCode", 200);
    Map<String, String> headers = new HashMap<>();
    if (isPdf) {
      headers.put("Content-Type", "application/pdf");
      headers.put("Content-Disposition", "attachment; filename=wordsearch.pdf");
      lambdaResp.put("isBase64Encoded", true);
      lambdaResp.put("body", Base64.getEncoder().encodeToString(pdfBytes));
    } else {
      headers.put("Content-Type", "application/json");
      lambdaResp.put("isBase64Encoded", false);
      lambdaResp.put("body", objectMapper.writeValueAsString(json));
    }
    lambdaResp.put("headers", headers);
    objectMapper.writeValue(output, lambdaResp);
  }

  private void writeErrorResponse(OutputStream output, int statusCode, String errorMsg)
      throws IOException {
    Map<String, Object> errorResp = new HashMap<>();
    errorResp.put("statusCode", statusCode);
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    errorResp.put("headers", headers);
    errorResp.put("isBase64Encoded", false);
    errorResp.put("body", String.format("{\"error\":\"%s\"}", errorMsg));
    objectMapper.writeValue(output, errorResp);
  }
}
