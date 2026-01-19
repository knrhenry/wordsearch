package com.knrhenry.wordsearch;

/** Custom exception for WordSearch-specific errors. */
public class WordSearchException extends Exception {
  public WordSearchException(String message) {
    super(message);
  }

  public WordSearchException(String message, Throwable cause) {
    super(message, cause);
  }
}
