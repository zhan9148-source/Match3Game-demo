import java.util.Random;

public class GameBoard {
  public static final int ROWS = 8;
  public static final int COLS = 8;
  public static final int GEM_TYPES = 5;

  private final int[][] board = new int[ROWS][COLS];
  private final Random random = new Random();
  private int score = 0;

  public GameBoard() {
    initBoard();
  }

  public int getGem(int row, int col) {
    return board[row][col];
  }

  public int getScore() {
    return score;
  }

  public int randomGem() {
    return random.nextInt(GEM_TYPES);
  }

  public void initBoard() {
    do {
      for (int r = 0; r < ROWS; r++) {
        for (int c = 0; c < COLS; c++) {
          board[r][c] = randomGem();
        }
      }
    } while (hasMatch() || !hasPossibleMove());
  }

  public void swap(int r1, int c1, int r2, int c2) {
    int temp = board[r1][c1];
    board[r1][c1] = board[r2][c2];
    board[r2][c2] = temp;
  }

  public boolean hasMatch() {
    boolean[][] matches = findMatches();

    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        if (matches[r][c]) return true;
      }
    }

    return false;
  }

  public boolean[][] findMatches() {
    boolean[][] matches = new boolean[ROWS][COLS];

    for (int r = 0; r < ROWS; r++) {
      int count = 1;

      for (int c = 1; c < COLS; c++) {
        if (board[r][c] == board[r][c - 1] && board[r][c] != -1) {
          count++;
        } else {
          if (count >= 3) {
            for (int k = 0; k < count; k++) {
              matches[r][c - 1 - k] = true;
            }
          }
          count = 1;
        }
      }

      if (count >= 3) {
        for (int k = 0; k < count; k++) {
          matches[r][COLS - 1 - k] = true;
        }
      }
    }

    for (int c = 0; c < COLS; c++) {
      int count = 1;

      for (int r = 1; r < ROWS; r++) {
        if (board[r][c] == board[r - 1][c] && board[r][c] != -1) {
          count++;
        } else {
          if (count >= 3) {
            for (int k = 0; k < count; k++) {
              matches[r - 1 - k][c] = true;
            }
          }
          count = 1;
        }
      }

      if (count >= 3) {
        for (int k = 0; k < count; k++) {
          matches[ROWS - 1 - k][c] = true;
        }
      }
    }

    return matches;
  }

  public void removeMatches(boolean[][] matches) {
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        if (matches[r][c]) {
          board[r][c] = -1;
          score += 10;
        }
      }
    }
  }

  public void calculateDropResult(int[][] newBoard, int[][] startRow) {
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        newBoard[r][c] = -1;
        startRow[r][c] = r;
      }
    }

    for (int c = 0; c < COLS; c++) {
      int writeRow = ROWS - 1;

      for (int r = ROWS - 1; r >= 0; r--) {
        if (board[r][c] != -1) {
          newBoard[writeRow][c] = board[r][c];
          startRow[writeRow][c] = r;
          writeRow--;
        }
      }

      while (writeRow >= 0) {
        newBoard[writeRow][c] = randomGem();
        startRow[writeRow][c] = writeRow - ROWS;
        writeRow--;
      }
    }
  }

  public void copyFrom(int[][] source) {
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        board[r][c] = source[r][c];
      }
    }
  }

  public boolean hasPossibleMove() {
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        if (c + 1 < COLS) {
          swap(r, c, r, c + 1);
          boolean createsMatch = hasMatch();
          swap(r, c, r, c + 1);
          if (createsMatch) return true;
        }

        if (r + 1 < ROWS) {
          swap(r, c, r + 1, c);
          boolean createsMatch = hasMatch();
          swap(r, c, r + 1, c);
          if (createsMatch) return true;
        }
      }
    }

    return false;
  }
}
