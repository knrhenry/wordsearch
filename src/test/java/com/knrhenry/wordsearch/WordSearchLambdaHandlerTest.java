package com.knrhenry.wordsearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for WordSearchLambdaHandler. */
class WordSearchLambdaHandlerTest {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static String buildEventBody(
      String[] words, boolean pdf, boolean includeWords, boolean includePdf, boolean includeBody)
      throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode eventNode = mapper.createObjectNode();
    if (includeBody) {
      ObjectNode bodyNode = mapper.createObjectNode();
      if (includeWords) {
        ArrayNode wordsArray = mapper.createArrayNode();
        for (String w : words) wordsArray.add(w);
        bodyNode.set("words", wordsArray);
      }
      if (includePdf) {
        bodyNode.put("pdf", pdf);
      }
      eventNode.set("body", bodyNode);
    }
    return mapper.writeValueAsString(eventNode);
  }

  private static JsonNode buildExpectedResponse(
      int statusCode, ObjectNode headersNode, boolean isBase64Encoded, JsonNode body)
      throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode expected = mapper.createObjectNode();
    expected.put("statusCode", statusCode);
    expected.set("headers", headersNode);
    expected.put("isBase64Encoded", isBase64Encoded);
    expected.put("body", body.isTextual() ? body.asText() : mapper.writeValueAsString(body));
    return expected;
  }

  static Stream<Arguments> lambdaTestCases() throws Exception {
    String[] words = {"apple", "banana", "cherry"};
    String[][] fixedGrid = new String[][] {{"A", "B"}, {"C", "D"}};
    ObjectNode expectedBodyJson = objectMapper.createObjectNode();
    expectedBodyJson.set("words", objectMapper.valueToTree(words));
    expectedBodyJson.set("grid", objectMapper.valueToTree(fixedGrid));
    byte[] fixedPdfBytes = "%PDF-mock".getBytes(StandardCharsets.UTF_8);
    ObjectNode errorWords =
        objectMapper
            .createObjectNode()
            .put("error", "'words' must be an array in the request body");
    ObjectNode errorBody = objectMapper.createObjectNode().put("error", "No request body provided");
    ObjectNode errorTooLong =
        objectMapper
            .createObjectNode()
            .put(
                "error",
                "Error: Word 'ABCDEFGHIJKLMNOPQRSTUVWXYZABCDE' exceeds max length of 30 characters.");
    ObjectNode errorSimulated = objectMapper.createObjectNode().put("error", "Simulated failure");

    ObjectNode jsonHeaders = objectMapper.createObjectNode();
    jsonHeaders.put("Content-Type", "application/json");
    ObjectNode pdfHeaders = objectMapper.createObjectNode();
    pdfHeaders.put("Content-Type", "application/pdf");
    pdfHeaders.put("Content-Disposition", "attachment; filename=wordsearch.pdf");

    return Stream.of(
        Arguments.argumentSet(
            "jsonSuccess",
            buildEventBody(words, false, true, true, true),
            expectedBodyJson,
            null,
            buildExpectedResponse(200, jsonHeaders, false, expectedBodyJson)),
        Arguments.argumentSet(
            "pdfSuccess",
            buildEventBody(words, true, true, true, true),
            null,
            fixedPdfBytes,
            buildExpectedResponse(
                200,
                pdfHeaders,
                true,
                objectMapper
                    .getNodeFactory()
                    .textNode(Base64.getEncoder().encodeToString(fixedPdfBytes)))),
        Arguments.argumentSet(
            "missingWords",
            buildEventBody(new String[] {}, false, false, true, true),
            errorWords,
            null,
            buildExpectedResponse(400, jsonHeaders, false, errorWords)),
        Arguments.argumentSet(
            "missingBody",
            buildEventBody(new String[] {}, false, false, false, false),
            errorBody,
            null,
            buildExpectedResponse(400, jsonHeaders, false, errorBody)),
        Arguments.argumentSet(
            "wordTooLong",
            buildEventBody(
                new String[] {"ABCDEFGHIJKLMNOPQRSTUVWXYZABCDE"}, false, true, true, true),
            errorTooLong,
            null,
            buildExpectedResponse(400, jsonHeaders, false, errorTooLong)),
        Arguments.argumentSet(
            "simulate500",
            buildEventBody(words, false, true, true, true),
            errorSimulated,
            null,
            buildExpectedResponse(500, jsonHeaders, false, errorSimulated)));
  }

  @ParameterizedTest
  @MethodSource("lambdaTestCases")
  void testLambdaHandlerParameterized(
      String event, ObjectNode expectedBody, byte[] pdfBytes, JsonNode expectedResponse)
      throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WordSearchJsonGenerator mockJsonGen = mock(WordSearchJsonGenerator.class);
    WordSearchPdfGenerator mockPdfGen = mock(WordSearchPdfGenerator.class);
    String contentType = expectedResponse.get("headers").get("Content-Type").asText();
    if (pdfBytes != null) {
      when(mockPdfGen.generatePdf(any(WordSearch.class), any())).thenReturn(pdfBytes);
    } else if (contentType.equals("application/json")
        && expectedResponse.get("statusCode").asInt() == 200) {
      // Only mock for success JSON
      when(mockJsonGen.generateJson(any(WordSearch.class))).thenReturn(expectedBody);
    } else if (contentType.equals("application/json")
        && expectedResponse.get("statusCode").asInt() != 200) {
      when(mockJsonGen.generateJson(any(WordSearch.class)))
          .thenThrow(new RuntimeException("Simulated failure"));
    }
    WordSearchLambdaHandler handler = new WordSearchLambdaHandler(mockPdfGen, mockJsonGen);
    handler.handleRequest(in, out, null);
    JsonNode actual = objectMapper.readTree(out.toString(StandardCharsets.UTF_8));
    assertThat(actual, is(expectedResponse));
  }
}
