package com.knrhenry.wordsearch;

import jakarta.enterprise.context.ApplicationScoped;

/** Factory for creating WordSearch instances. */
@ApplicationScoped
public class WordSearchFactory {
  public WordSearch create(java.util.List<String> words) {
    return new WordSearch(words);
  }
}
