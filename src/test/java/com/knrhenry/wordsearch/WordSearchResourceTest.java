package com.knrhenry.wordsearch;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the WordSearchResource REST API, including PDF generation and error cases.
 */
@QuarkusTest
public class WordSearchResourceTest {

  private static String createRequestAsJsonString(String[] words, boolean pdf)
      throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode json = mapper.createObjectNode();
    ArrayNode wordArray = mapper.createArrayNode();
    for (String word : words) {
      wordArray.add(word);
    }
    json.set("words", wordArray);
    json.put("pdf", pdf);
    return mapper.writeValueAsString(json);
  }

  @Test
  public void testGenerateWordSearchText() throws Exception {
    given()
        .contentType(ContentType.JSON)
        .body(createRequestAsJsonString(new String[] {"apple", "banana", "cherry"}, false))
        .post("/api/wordsearch")
        .then()
        .statusCode(200)
        .body(
            containsString("apple"),
            containsString("banana"),
            containsString("cherry"),
            containsString("words"));
  }

  @Test
  public void testGenerateWordSearchPdf() throws Exception {
    byte[] pdf =
        given()
            .contentType(ContentType.JSON)
            .body(createRequestAsJsonString(new String[] {"apple", "banana", "cherry"}, true))
            .post("/api/wordsearch")
            .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/pdf"))
            .header(
                "Content-Disposition",
                allOf(containsString("wordsearch.pdf"), containsString("inline")))
            .extract()
            .asByteArray();
    assertThat("PDF should be larger than 100 bytes", pdf.length, greaterThan(100));
  }

  @Test
  public void testTooManyWords() throws Exception {
    String[] words = new String[21];
    for (int i = 0; i < 21; i++) {
      words[i] = "word" + i;
    }
    given()
        .contentType(ContentType.JSON)
        .body(createRequestAsJsonString(words, false))
        .post("/api/wordsearch")
        .then()
        .statusCode(400)
        .body(containsString("Too many words"));
  }

  @Test
  public void testEmptyWordList() throws Exception {
    given()
        .contentType(ContentType.JSON)
        .body(createRequestAsJsonString(new String[] {}, false))
        .post("/api/wordsearch")
        .then()
        .statusCode(400)
        .body(containsString("Word list must not be empty"));
  }

  @Test
  public void testWordTooLong() throws Exception {
    String longWord = "supercalifragilisticexpialidocious"; // >30 chars
    given()
        .contentType(ContentType.JSON)
        .body(createRequestAsJsonString(new String[] {longWord}, false))
        .post("/api/wordsearch")
        .then()
        .statusCode(400)
        .body(
            containsString(
                "Error: Word 'supercalifragilisticexpialidocious' exceeds max length of 30 characters."));
  }

  @Test
  public void testJsonResponseStructure() throws Exception {
    var response =
        given()
            .contentType(ContentType.JSON)
            .body(createRequestAsJsonString(new String[] {"apple", "banana"}, false))
            .post("/api/wordsearch")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .asString();
    assertThat("Response should contain 'grid'", response, containsString("grid"));
    assertThat("Response should contain 'words'", response, containsString("words"));
  }

  @Test
  public void testPdfResponseHeaders() throws Exception {
    given()
        .contentType(ContentType.JSON)
        .body(createRequestAsJsonString(new String[] {"apple", "banana"}, true))
        .post("/api/wordsearch")
        .then()
        .statusCode(200)
        .header("Content-Type", containsString("application/pdf"))
        .header("Content-Disposition", containsString("wordsearch.pdf"));
  }

  @Test
  public void testPdfGenerationFailureErrorStructure() throws Exception {
    given()
        .contentType(ContentType.JSON)
        .body(createRequestAsJsonString(new String[] {"exception", "banana"}, true))
        .post("/api/wordsearch")
        .then()
        .statusCode(500)
        .body(containsString("PDF generation failed"));
  }

  /** Alternative PDF generator that simulates failure for testing. */
  @Alternative
  @Priority(1)
  @ApplicationScoped
  public static class TestPdfGeneratorFailure extends WordSearchPdfGenerator {
    @Override
    public void generatePdf(OutputStream out, char[][] grid, int gridSize, List<String> words)
        throws IOException {
      if (words != null && !words.isEmpty() && words.get(0).equals("exception")) {
        throw new IOException("PDF generation failed");
      } else {
        super.generatePdf(out, grid, gridSize, words);
      }
    }
  }
}
