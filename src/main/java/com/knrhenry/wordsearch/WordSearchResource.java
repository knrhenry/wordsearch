package com.knrhenry.wordsearch;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/** REST API for generating word search puzzles. */
@Tag(name = "Word Search", description = "Word Search puzzle generator API")
@Path("/api")
public class WordSearchResource {

  private static final String APPLICATION_PDF = "application/pdf";

  /** Request object for word search generation. */
  public static class WordSearchRequest {
    @Schema(
        description =
            "List of words to include in the puzzle. Max 20 words, each up to 30 characters.",
        maxItems = 20,
        minItems = 1,
        examples = {"[\"apple\",\"banana\",\"cherry\"]"})
    public List<String> words;

    @Schema(
        description = "Set to true to generate a PDF. If false, returns plain text.",
        defaultValue = "false",
        examples = {"false"})
    public boolean pdf;
  }

  @Inject WordSearchFactory wordSearchFactory;
  @Inject WordSearchPdfGenerator pdfGenerator;

  /**
   * Generates a word search puzzle grid or PDF from a list of words.
   *
   * @param req the word search request
   * @return the word search grid as text or PDF
   */
  @POST
  @Path("/wordsearch")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces({MediaType.APPLICATION_JSON, APPLICATION_PDF})
  @Operation(
      summary = "Generate a word search puzzle",
      description = "Generates a word search puzzle as JSON or PDF based on the input word list.")
  @RequestBody(
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = WordSearchRequest.class),
              examples = {
                @ExampleObject(
                    name = "Basic Example",
                    summary = "A simple word search request",
                    value = "{\"words\":[\"apple\",\"banana\",\"cherry\"],\"pdf\":false}")
              }))
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Word search generated successfully",
        content = {
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              examples =
                  @ExampleObject(
                      name = "JSON Example",
                      summary = "JSON word search grid",
                      value =
                          """
                {
                  "grid": [["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O"],
                           ["P","Q","R","S","T","U","V","W","X","Y","Z","A","B","C","D"],
                           ["E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S"],
                           ["T","U","V","W","X","Y","Z","A","B","C","D","E","F","G","H"],
                           ["I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W"],
                           ["X","Y","Z","A","B","C","D","E","F","G","H","I","J","K","L"],
                           ["M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","A"],
                           ["B","C","D","E","F","G","H","I","J","K","L","M","N","O","P"],
                           ["Q","R","S","T","U","V","W","X","Y","Z","A","B","C","D","E"],
                           ["F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T"],
                           ["U","V","W","X","Y","Z","A","B","C","D","E","F","G","H","I"],
                           ["J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X"],
                           ["Y","Z","A","B","C","D","E","F","G","H","I","J","K","L","M"],
                           ["N","O","P","Q","R","S","T","U","V","W","X","Y","Z","A","B"],
                           ["C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q"]],
                  "words": ["apple", "banana", "cherry"]
                }
                """),
              schema = @Schema(type = SchemaType.OBJECT)),
          @Content(
              mediaType = APPLICATION_PDF,
              examples =
                  @ExampleObject(
                      name = "PDF Example",
                      summary = "PDF binary data",
                      value = "PDF binary data"),
              schema = @Schema(type = SchemaType.STRING, format = "binary"))
        }),
    @APIResponse(
        responseCode = "400",
        description = "Invalid input",
        content =
            @Content(
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      name = "Too Many Words",
                      summary = "Too many words error",
                      value = "{\"error\":\"Too many words. Maximum allowed is 20.\"}"),
                  @ExampleObject(
                      name = "Word Too Long",
                      summary = "Word too long error",
                      value =
                          """
                          {
                            "error":"Word 'supercalifragilisticexpialidocious' is too long. Maximum allowed is 30 characters."
                          }
                          """)
                },
                schema = @Schema(type = SchemaType.OBJECT, implementation = Map.class))),
    @APIResponse(
        responseCode = "500",
        description = "Server error",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        name = "PDF Error",
                        summary = "PDF generation failed",
                        value = "{\"error\":\"PDF generation failed.\"}"),
                schema = @Schema(type = SchemaType.OBJECT, implementation = Map.class)))
  })
  public Response generateWordSearch(WordSearchRequest req) {
    if (req.words == null || req.words.isEmpty()) {
      return Response.status(BAD_REQUEST)
          .entity(Collections.singletonMap("error", "Word list must not be empty."))
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    if (req.words.size() > 20) {
      return Response.status(BAD_REQUEST)
          .entity(Collections.singletonMap("error", "Too many words. Maximum allowed is 20."))
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    try {
      WordSearch ws = wordSearchFactory.create(req.words);
      char[][] grid = ws.getGrid();
      if (req.pdf) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pdfGenerator.generatePdf(baos, grid, grid.length, req.words);
        return Response.ok(baos.toByteArray(), APPLICATION_PDF)
            .header("Content-Disposition", "inline; filename=wordsearch.pdf")
            .header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            .build();
      } else {
        String[][] gridStr = new String[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++) {
          for (int j = 0; j < grid[i].length; j++) {
            gridStr[i][j] = String.valueOf(grid[i][j]);
          }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("grid", gridStr);
        response.put("words", req.words);
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
      }
    } catch (IllegalArgumentException e) {
      return Response.status(BAD_REQUEST)
          .entity(Collections.singletonMap("error", e.getMessage()))
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (IOException e) {
      return Response.serverError()
          .entity(Collections.singletonMap("error", "PDF generation failed."))
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
  }
}
