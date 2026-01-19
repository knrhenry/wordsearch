package com.knrhenry.wordsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** WordSearch generates a word search puzzle grid and outputs it to the console or as a PDF. */
public class WordSearch {
  /** Default grid size for the puzzle. */
  private static final int DEFAULT_GRID_SIZE = 15;

  /** Maximum allowed word length. */
  private static final int MAX_WORD_LENGTH = 30;

  /** Maximum number of attempts to place a word. */
  private static final int MAX_ATTEMPTS = 100;

  /** Alphabet length for random fill. */
  private static final int ALPHABET_LENGTH = 26;

  /** The size of the grid. */
  private final int gridSize;

  /** The grid of characters. */
  private final char[][] grid;

  /** The list of words in the puzzle. */
  private final List<String> words;

  /** Random number generator. */
  private final Random random = new Random();

  /** Enum representing possible word placement directions. */
  public enum Direction {
    HORIZONTAL,
    VERTICAL,
    DIAGONAL_DOWN,
    DIAGONAL_UP
  }

  /**
   * Constructs a WordSearch grid from the given list of words.
   *
   * @param inputWords List of words to include in the grid
   */
  private WordSearch(final List<String> inputWords) {
    this.words = new ArrayList<>(inputWords);
    int maxLen = DEFAULT_GRID_SIZE;
    for (String word : words) {
      if (word.length() > maxLen) {
        maxLen = word.length();
      }
    }
    this.gridSize = maxLen;
    this.grid = new char[gridSize][gridSize];
  }

  /** Generates the word search grid by placing words and filling empty spaces. */
  private void generateGrid() throws WordSearchException {
    for (char[] row : grid) {
      Arrays.fill(row, ' ');
    }
    for (String word : words) {
      int len = word.length();
      boolean placed = false;
      for (int attempt = 0; attempt < MAX_ATTEMPTS && !placed; attempt++) {
        Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
        int rowBound;
        int colBound;
        switch (direction) {
          case HORIZONTAL:
            rowBound = gridSize;
            colBound = gridSize - len + 1;
            break;
          case VERTICAL:
            rowBound = gridSize - len + 1;
            colBound = gridSize;
            break;
          case DIAGONAL_DOWN, DIAGONAL_UP:
            rowBound = gridSize - len + 1;
            colBound = gridSize - len + 1;
            break;
          default:
            rowBound = 0;
            colBound = 0;
            break;
        }
        if (rowBound <= 0 || colBound <= 0) {
          continue;
        }
        int row = random.nextInt(rowBound);
        int col = random.nextInt(colBound);
        if (canPlace(word.toUpperCase(), row, col, direction)) {
          placeWord(word.toUpperCase(), row, col, direction);
          placed = true;
        }
      }
      // If not placed after max attempts, throw an exception
      if (!placed) {
        String msg = String.format("Error: Could not place word '%s' in the grid.", word);
        throw new WordSearchException(msg);
      }
    }
    fillEmptySpaces();
  }

  /** Fills empty spaces in the grid with random letters. */
  private void fillEmptySpaces() {
    for (int r = 0; r < gridSize; r++) {
      for (int c = 0; c < gridSize; c++) {
        if (grid[r][c] == ' ') {
          grid[r][c] = (char) ('A' + random.nextInt(ALPHABET_LENGTH));
        }
      }
    }
  }

  /**
   * Returns the word search grid.
   *
   * @return the word search grid
   */
  public char[][] getGrid() {
    return grid;
  }

  /**
   * Returns the word list used in this puzzle.
   *
   * @return a copy of the word list
   */
  public List<String> getWords() {
    return new ArrayList<>(words);
  }

  /**
   * Checks if a word can be placed at the given position and orientation in the grid.
   *
   * @param word the word to check
   * @param row starting row
   * @param col starting column
   * @param direction direction to place the word
   * @return true if the word can be placed, false otherwise
   */
  private boolean canPlace(String word, int row, int col, Direction direction) {
    int len = word.length();
    for (int i = 0; i < len; i++) {
      int r;
      int c;
      switch (direction) {
        case HORIZONTAL:
          r = row;
          c = col + i;
          break;
        case VERTICAL:
          r = row + i;
          c = col;
          break;
        case DIAGONAL_DOWN:
          r = row + i;
          c = col + i;
          break;
        case DIAGONAL_UP:
          r = row + len - 1 - i;
          c = col + i;
          break;
        default:
          return false;
      }
      if (r < 0 || r >= gridSize || c < 0 || c >= gridSize) {
        return false;
      }
      if (grid[r][c] != ' ' && grid[r][c] != word.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Places a word in the grid at the given position and orientation.
   *
   * @param word the word to place
   * @param row starting row
   * @param col starting column
   * @param direction direction to place the word
   */
  private void placeWord(String word, int row, int col, Direction direction) {
    int len = word.length();
    for (int i = 0; i < len; i++) {
      switch (direction) {
        case HORIZONTAL:
          grid[row][col + i] = word.charAt(i);
          break;
        case VERTICAL:
          grid[row + i][col] = word.charAt(i);
          break;
        case DIAGONAL_DOWN:
          grid[row + i][col + i] = word.charAt(i);
          break;
        case DIAGONAL_UP:
          grid[row + len - 1 - i][col + i] = word.charAt(i);
          break;
        default:
          // Defensive: do nothing
          break;
      }
    }
  }

  /**
   * Creates a new WordSearch instance with the given words.
   *
   * @param inputWords List of words to include in the grid
   * @return a new WordSearch instance
   * @throws WordSearchException if grid generation fails or any word exceeds MAX_WORD_LENGTH
   */
  public static WordSearch create(List<String> inputWords) throws WordSearchException {
    for (String word : inputWords) {
      if (word.length() > MAX_WORD_LENGTH) {
        String msg =
            String.format(
                "Error: Word '%s' exceeds max length of %d characters.", word, MAX_WORD_LENGTH);
        throw new WordSearchException(msg);
      }
    }
    WordSearch wordSearch = new WordSearch(inputWords);
    int attempts = 0;
    while (true) {
      try {
        wordSearch.generateGrid();
        break;
      } catch (WordSearchException e) {
        attempts++;
        if (attempts >= 5) {
          throw new WordSearchException(
              "Failed to generate grid after 5 attempts: " + e.getMessage(), e);
        }
      }
    }
    return wordSearch;
  }
}
