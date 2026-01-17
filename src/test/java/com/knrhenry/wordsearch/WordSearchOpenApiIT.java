package com.knrhenry.wordsearch;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the OpenAPI documentation of the WordSearch REST API. Verifies OpenAPI
 * schema, endpoints, and example responses.
 */
@QuarkusTest
class WordSearchOpenApiIT {

  private JsonNode root;
  private JsonNode post;
  private JsonNode schemas;

  @BeforeEach
  void setUp() throws Exception {
    String openApiJson =
        given()
            .accept(MediaType.APPLICATION_JSON)
            .when()
            .get("/q/openapi")
            .then()
            .statusCode(200)
            .extract()
            .asString();
    ObjectMapper mapper = new ObjectMapper();
    root = mapper.readTree(openApiJson);
    post = root.path("paths").path("/api/wordsearch").path("post");
    schemas = root.path("components").path("schemas");
  }

  @Test
  void openApiVersionAndInfoShouldBeCorrect() {
    assertThat(root.get("openapi").asText(), startsWith("3."));
    JsonNode info = root.get("info");
    // Actual title is "Word Search API"
    assertThat(info.get("title").asText(), is("Word Search API"));
    assertThat(info.get("version").asText(), not(emptyOrNullString()));
    // Actual description contains "API for generating word search"
    assertThat(info.get("description").asText(), containsString("API for generating word search"));
  }

  @Test
  void wordsearchPostPathShouldBePresentWithSummaryAndTags() {
    assertThat(post, notNullValue());
    // Actual summary is "Generate a word search puzzle"
    assertThat(post.path("summary").asText(), is("Generate a word search puzzle"));
    JsonNode tagsNode = post.path("tags");
    assertThat(tagsNode.isArray(), is(true));
    boolean foundTag = false;
    for (JsonNode tag : tagsNode) {
      if (tag.asText().equals("Word Search")) {
        foundTag = true;
        break;
      }
    }
    assertThat("Tags should contain 'Word Search'", foundTag, is(true));
  }

  @Test
  void requestBodyShouldReferenceSchemaAndHaveExample() {
    JsonNode requestBody = post.path("requestBody").path("content").path("application/json");
    assertThat(requestBody.has("schema"), is(true));
    assertThat(
        requestBody.path("schema").path("$ref").asText(), containsString("WordSearchRequest"));
    assertThat(requestBody.has("example") || requestBody.has("examples"), is(true));
    assertThat(post.path("requestBody").path("required").asBoolean(), is(true));
  }

  @Test
  void wordSearchRequestSchemaShouldBeCorrect() {
    assertThat(schemas.has("WordSearchRequest"), is(true));
    JsonNode reqSchema = schemas.path("WordSearchRequest");
    assertThat(reqSchema.path("type").asText(), is("object"));
    assertThat(reqSchema.path("properties").has("words"), is(true));
    assertThat(reqSchema.path("properties").has("pdf"), is(true));
    JsonNode words = reqSchema.path("properties").path("words");
    assertThat(words.path("type").asText(), is("array"));
    JsonNode pdf = reqSchema.path("properties").path("pdf");
    assertThat(pdf.path("type").asText(), is("boolean"));
  }

  @Test
  void response200ShouldHaveDescriptionAndExample() {
    JsonNode resp200 = post.path("responses").path("200");
    assertThat(resp200.get("description").asText(), not(emptyOrNullString()));
    // Assert application/json response
    JsonNode resp200Content = resp200.get("content").get("application/json");
    assertThat(resp200Content.has("examples"), is(true));
    JsonNode examples = resp200Content.get("examples");
    assertThat(examples, notNullValue());
    // Check for the expected example structure directly
    JsonNode jsonExample = examples.get("JSON Example");
    assertThat(jsonExample, notNullValue());
    JsonNode value = jsonExample.get("value");
    assertThat(value, notNullValue());
    assertThat(value.has("grid"), is(true));
    assertThat(value.get("grid").isArray(), is(true));
    assertThat(value.has("words"), is(true));
  }

  @Test
  void response200ShouldHavePdfOrOctetStreamSchema() {
    JsonNode resp200 = post.path("responses").path("200");
    JsonNode pdfContent = resp200.get("content").get("application/pdf");
    if (pdfContent == null) {
      pdfContent = resp200.get("content").get("application/octet-stream");
    }
    assertThat("Should have PDF or octet-stream content type", pdfContent, notNullValue());
    assertThat(pdfContent.has("schema"), is(true));
  }

  @Test
  void response400ShouldHaveDescriptionAndWordTooLongExample() {
    JsonNode resp400 = post.path("responses").path("400");
    assertThat(resp400.get("description").asText(), not(emptyOrNullString()));
    JsonNode resp400Content = resp400.get("content").get("application/json");
    assertThat(resp400Content.has("examples"), is(true));
    JsonNode examplesNode = resp400Content.get("examples");
    // Check "Word Too Long" example
    JsonNode wordTooLong = examplesNode.get("Word Too Long");
    assertThat(wordTooLong, notNullValue());
    assertThat(wordTooLong.get("summary").asText(), containsString("too long"));
    assertThat(
        wordTooLong.get("value").get("error").asText(),
        containsString("supercalifragilisticexpialidocious"));
    assertThat(
        wordTooLong.get("value").get("error").asText(),
        containsString("Maximum allowed is 30 characters"));
    // Check "Too Many Words" example
    JsonNode tooManyWords = examplesNode.get("Too Many Words");
    assertThat(tooManyWords, notNullValue());
    // Actual summary is "Too many words error"
    assertThat(tooManyWords.get("summary").asText(), is("Too many words error"));
    // Actual error is "Too many words. Maximum allowed is 20."
    assertThat(
        tooManyWords.get("value").get("error").asText(),
        is("Too many words. Maximum allowed is 20."));
  }

  @Test
  void response500ShouldHaveDescriptionAndPdfErrorExample() {
    JsonNode resp500 = post.path("responses").path("500");
    assertThat(resp500.get("description").asText(), not(emptyOrNullString()));
    JsonNode resp500Content = resp500.get("content").get("application/json");
    assertThat(resp500Content.has("examples"), is(true));
    JsonNode examplesNode = resp500Content.get("examples");
    // Check for the actual example key present in the OpenAPI spec
    JsonNode pdfError = examplesNode.get("PDF Error");
    assertThat(pdfError, notNullValue());
    assertThat(
        pdfError.get("summary").asText().toLowerCase(), containsString("pdf generation failed"));
    assertThat(
        pdfError.get("value").get("error").asText().toLowerCase(),
        containsString("pdf generation failed"));
  }
}
