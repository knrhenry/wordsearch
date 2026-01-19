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
        throw new WordSearchException("No words provided");
      }
      WordSearch ws = WordSearch.create(words);
      byte[] pdfBytes = null;
      ObjectNode jsonNode = null;
      if (wantsPdf) {
        pdfBytes = new WordSearchPdfGenerator().generatePdf(ws);
      } else {
        jsonNode = new WordSearchJsonGenerator().generateJson(ws);
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
