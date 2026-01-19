package com.knrhenry.wordsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class WordSearchJsonGenerator {
  public String generateJson(WordSearch ws) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(
        Map.of(
            "grid", ws.getGrid(),
            "words", ws.getWords()));
  }
}
