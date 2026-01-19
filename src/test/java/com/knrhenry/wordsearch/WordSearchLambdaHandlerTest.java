package com.knrhenry.wordsearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/** Unit tests for WordSearchLambdaHandler. */
class WordSearchLambdaHandlerTest {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("Lambda handler returns JSON for valid input")
  void testJsonResponse() throws Exception {
    String event = "{\"body\":{\"words\":\"apple,banana,cherry\",\"pdf\":false}}";
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.get("statusCode").asInt(), is(200));
    assertThat(
        root.get("headers").get("Content-Type").asText(), containsString("application/json"));
    assertThat(root.get("isBase64Encoded").asBoolean(), is(false));
    JsonNode body = objectMapper.readTree(root.get("body").asText());
    assertThat(body.has("grid"), is(true));
    assertThat(body.has("words"), is(true));
    assertThat(body.get("words").toString(), containsString("apple"));
  }

  @Test
  @DisplayName("Lambda handler returns PDF as base64 for pdf=true")
  void testPdfResponse() throws Exception {
    String event = "{\"body\":{\"words\":\"apple,banana,cherry\",\"pdf\":true}}";
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.get("statusCode").asInt(), is(200));
    assertThat(root.get("headers").get("Content-Type").asText(), containsString("application/pdf"));
    assertThat(root.get("isBase64Encoded").asBoolean(), is(true));
    String base64 = root.get("body").asText();
    byte[] pdfBytes = Base64.getDecoder().decode(base64);
    assertThat(pdfBytes.length, greaterThan(100));
    assertThat(new String(pdfBytes, 0, 5), is("%PDF-"));
  }

  @Test
  @DisplayName("Lambda handler returns error for missing words")
  void testMissingWords() throws Exception {
    String event = "{\"body\":{\"pdf\":false}}";
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.has("statusCode"), is(true));
    assertThat(root.get("statusCode").asInt(), greaterThanOrEqualTo(400));
    String body = root.get("body").asText();
    assertThat(body, anyOf(containsString("No words provided"), containsString("error")));
  }

  @Test
  @DisplayName("Lambda handler handles queryStringParameters input")
  void testQueryStringParameters() throws Exception {
    String event = "{\"queryStringParameters\":{\"words\":\"apple,banana\",\"pdf\":false}}";
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.has("statusCode"), is(true));
    String body = root.get("body").asText();
    assertThat(body, containsString("apple"));
    assertThat(body, containsString("banana"));
  }

  @Test
  @DisplayName("Lambda handler handles queryStringParameters input")
  void testQueryStringParametersMissingWords() throws Exception {
    String event = "{\"queryStringParameters\":{\"pdf\":false}}";
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.has("statusCode"), is(true));
    assertThat(root.get("statusCode").asInt(), greaterThanOrEqualTo(400));
    String body = root.get("body").asText();
    assertThat(body, anyOf(containsString("No words provided"), containsString("error")));
  }

  @Test
  @DisplayName("Lambda handler handles queryStringParameters input")
  void testQueryStringParametersMissingPdfParameter() throws Exception {
    String event = "{\"queryStringParameters\":{\"words\":\"apple,banana\"}}";
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.has("statusCode"), is(true));
    String body = root.get("body").asText();
    assertThat(body, containsString("apple"));
    assertThat(body, containsString("banana"));
  }

  @Test
  @DisplayName("Lambda handler handles queryStringParameters input")
  void testTextualBody() throws Exception {
    String event = "{\"body\":\"{\\\"words\\\":\\\"apple,banana\\\",\\\"pdf\\\":false}\"}";
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.has("statusCode"), is(true));
    String body = root.get("body").asText();
    assertThat(body, containsString("apple"));
    assertThat(body, containsString("banana"));
  }

  @Test
  @DisplayName("Lambda handler handles queryStringParameters input")
  void testWordsAsArrayInBody() throws Exception {
    String event = "{\"body\":{\"words\":[\"apple\",\"banana\"],\"pdf\":false}}";
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.has("statusCode"), is(true));
    String body = root.get("body").asText();
    assertThat(body, containsString("apple"));
    assertThat(body, containsString("banana"));
  }

  @Test
  @DisplayName("Lambda handler handles queryStringParameters input")
  void testMissingPdfParameter() throws Exception {
    String event = "{\"body\":{\"words\":\"apple,banana\"}}";
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.has("statusCode"), is(true));
    String body = root.get("body").asText();
    assertThat(body, containsString("apple"));
    assertThat(body, containsString("banana"));
  }

  @Test
  @DisplayName("Lambda handler returns error for missing words")
  void testMissingBodyAndNoQueryStringParameters() throws Exception {
    String event = "{}";
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.has("statusCode"), is(true));
    assertThat(root.get("statusCode").asInt(), greaterThanOrEqualTo(400));
    String body = root.get("body").asText();
    assertThat(body, anyOf(containsString("No words provided"), containsString("error")));
  }

  @Test
  @DisplayName("Lambda handler returns error for long word")
  void testWordTooLong() throws Exception {
    String longWord = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDE"; // 31 chars (exceeds max)
    String event = String.format("{\"body\":{\"words\":\"%s\",\"pdf\":false}}", longWord);
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.has("statusCode"), is(true));
    assertThat(root.get("statusCode").asInt(), greaterThanOrEqualTo(400));
    String body = root.get("body").asText();
    assertThat(body, anyOf(containsString("exceeds max length"), containsString("error")));
  }

  @Test
  @DisplayName("Lambda handler returns error for long word")
  void test500Error() throws Exception {
    String event = "{\"body\":{\"words\":\"apple,banana,cherry\",\"pdf\":false}}";
    ByteArrayInputStream in = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    MockedStatic<WordSearch> mockedStatic = mockStatic(WordSearch.class);
    mockedStatic
        .when(() -> WordSearch.create(anyList()))
        .thenThrow(new RuntimeException("Simulated failure"));
    new WordSearchLambdaHandler().handleRequest(in, out, null);
    mockedStatic.close();
    String resp = out.toString(StandardCharsets.UTF_8);
    JsonNode root = objectMapper.readTree(resp);
    assertThat(root.has("statusCode"), is(true));
    assertThat(root.get("statusCode").asInt(), greaterThanOrEqualTo(400));
    String body = root.get("body").asText();
    assertThat(body, anyOf(containsString("exceeds max length"), containsString("error")));
  }
}
