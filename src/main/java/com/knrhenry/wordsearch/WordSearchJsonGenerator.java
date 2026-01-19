package com.knrhenry.wordsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;

/** Generates JSON representation of a WordSearch puzzle. */
@ApplicationScoped
public class WordSearchJsonGenerator {
  /**
   * Generates a JSON string for the given WordSearch puzzle.
   *
   * @param ws the WordSearch puzzle
   * @return JSON string representing the puzzle
   * @throws JsonProcessingException if JSON serialization fails
   * @throws NullPointerException if the WordSearch is null
   */
  public ObjectNode generateJson(WordSearch ws) throws JsonProcessingException {
    if (ws == null) {
      throw new NullPointerException("WordSearch must not be null");
    }
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    node.putPOJO("grid", ws.getGrid());
    node.putPOJO("words", ws.getWords());
    return node;
  }
}
