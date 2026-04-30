import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Match3Game extends JPanel {
  private static final int ROWS = 8, COLS = 8;
  private static final int CELL_SIZE = 70;
  private static final int GEM_TYPES = 5;

  private static final int ANIMATION_STEPS = 10;
  private static final int DROP_STEPS = 12;
  private static final int CLEAR_STEPS = 14;

  private final int[][] board = new int[ROWS][COLS];
  private final int[][] newBoard = new int[ROWS][COLS];
  private final int[][] startRow = new int[ROWS][COLS];

  private boolean[][] clearingMatches = new boolean[ROWS][COLS];

  private final Random random = new Random();

  private int selectedRow = -1, selectedCol = -1;
  private int score = 0;

  private boolean animating = false;
  private boolean swapBack = false;
  private boolean dropping = false;
  private boolean clearing = false;

  private int aRow, aCol, bRow, bCol;
  private int animationStep = 0;
  private int dropStep = 0;
  private int clearStep = 0;

  private final Color[] colors = {
      Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA
  };

  public Match3Game() {
    setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE + 40));
    initBoard();

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (!animating) {
          handleClick(e.getX(), e.getY());
        }
      }
    });
  }

  private void initBoard() {
    do {
      for (int r = 0; r < ROWS; r++) {
        for (int c = 0; c < COLS; c++) {
          board[r][c] = randomGem();
        }
      }
    } while (hasMatch());
  }

  private int randomGem() {
    return random.nextInt(GEM_TYPES);
  }

  private void handleClick(int x, int y) {
    if (y >= ROWS * CELL_SIZE) return;

    int row = y / CELL_SIZE;
    int col = x / CELL_SIZE;

    if (selectedRow == -1) {
      selectedRow = row;
      selectedCol = col;
    } else {
      if (isAdjacent(selectedRow, selectedCol, row, col)) {
        startSwapAnimation(selectedRow, selectedCol, row, col);
      } else {
        selectedRow = row;
        selectedCol = col;
        repaint();
        return;
      }

      selectedRow = -1;
      selectedCol = -1;
    }

    repaint();
  }

  private boolean isAdjacent(int r1, int c1, int r2, int c2) {
    return Math.abs(r1 - r2) + Math.abs(c1 - c2) == 1;
  }

  private void startSwapAnimation(int r1, int c1, int r2, int c2) {
    aRow = r1;
    aCol = c1;
    bRow = r2;
    bCol = c2;

    animationStep = 0;
    swapBack = false;
    dropping = false;
    clearing = false;
    animating = true;

    Timer timer = new Timer(15, null);
    timer.addActionListener(e -> {
      animationStep++;
      repaint();

      if (animationStep >= ANIMATION_STEPS) {
        timer.stop();

        swap(aRow, aCol, bRow, bCol);

        if (hasMatch()) {
          resolveBoard();
        } else {
          startSwapBackAnimation();
        }
      }
    });

    timer.start();
  }

  private void startSwapBackAnimation() {
    animationStep = 0;
    swapBack = true;

    Timer timer = new Timer(15, null);
    timer.addActionListener(e -> {
      animationStep++;
      repaint();

      if (animationStep >= ANIMATION_STEPS) {
        timer.stop();

        swap(aRow, aCol, bRow, bCol);

        animating = false;
        swapBack = false;
        repaint();
      }
    });

    timer.start();
  }

  private void swap(int r1, int c1, int r2, int c2) {
    int temp = board[r1][c1];
    board[r1][c1] = board[r2][c2];
    board[r2][c2] = temp;
  }

  private boolean hasMatch() {
    boolean[][] matches = findMatches();

    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        if (matches[r][c]) return true;
      }
    }

    return false;
  }

  private boolean[][] findMatches() {
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

  private void resolveBoard() {
    clearingMatches = findMatches();
    startClearAnimation();
  }

  private void startClearAnimation() {
    clearStep = 0;
    clearing = true;
    animating = true;

    Timer timer = new Timer(25, null);
    timer.addActionListener(e -> {
      clearStep++;
      repaint();

      if (clearStep >= CLEAR_STEPS) {
        timer.stop();

        removeMatches(clearingMatches);

        clearing = false;
        startDropAnimation();
      }
    });

    timer.start();
  }

  private void removeMatches(boolean[][] matches) {
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        if (matches[r][c]) {
          board[r][c] = -1;
          score += 10;
        }
      }
    }
  }

  private void startDropAnimation() {
    calculateDropResult();

    dropStep = 0;
    dropping = true;
    animating = true;

    Timer timer = new Timer(15, null);
    timer.addActionListener(e -> {
      dropStep++;
      repaint();

      if (dropStep >= DROP_STEPS) {
        timer.stop();

        copyBoard(newBoard, board);

        dropping = false;
        animating = false;

        if (hasMatch()) {
          resolveBoard();
        }

        repaint();
      }
    });

    timer.start();
  }

  private void calculateDropResult() {
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

  private void copyBoard(int[][] from, int[][] to) {
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        to[r][c] = from[r][c];
      }
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    double progress = animationStep / (double) ANIMATION_STEPS;
    if (swapBack) {
      progress = 1.0 - progress;
    }

    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        int x = c * CELL_SIZE;
        int y = r * CELL_SIZE;

        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(x, y, CELL_SIZE, CELL_SIZE);

        if (dropping) {
          continue;
        }

        if (clearing && clearingMatches[r][c]) {
          drawBreakingGem(g2, board[r][c], x, y, clearStep / (double) CLEAR_STEPS);
          continue;
        }

        if (animating && !clearing && ((r == aRow && c == aCol) || (r == bRow && c == bCol))) {
          continue;
        }

        drawGem(g2, board[r][c], x, y);
      }
    }

    if (animating && !dropping && !clearing) {
      int ax = aCol * CELL_SIZE;
      int ay = aRow * CELL_SIZE;
      int bx = bCol * CELL_SIZE;
      int by = bRow * CELL_SIZE;

      int drawAX = (int) (ax + (bx - ax) * progress);
      int drawAY = (int) (ay + (by - ay) * progress);
      int drawBX = (int) (bx + (ax - bx) * progress);
      int drawBY = (int) (by + (ay - by) * progress);

      drawGem(g2, board[aRow][aCol], drawAX, drawAY);
      drawGem(g2, board[bRow][bCol], drawBX, drawBY);
    }

    if (dropping) {
      double dropProgress = dropStep / (double) DROP_STEPS;

      for (int r = 0; r < ROWS; r++) {
        for (int c = 0; c < COLS; c++) {
          int gem = newBoard[r][c];
          if (gem == -1) continue;

          int x = c * CELL_SIZE;
          int fromY = startRow[r][c] * CELL_SIZE;
          int toY = r * CELL_SIZE;

          int y = (int) (fromY + (toY - fromY) * dropProgress);

          drawGem(g2, gem, x, y);
        }
      }
    }

    if (selectedRow != -1 && !animating) {
      int x = selectedCol * CELL_SIZE;
      int y = selectedRow * CELL_SIZE;

      g2.setColor(Color.BLACK);
      g2.drawRect(x + 3, y + 3, CELL_SIZE - 6, CELL_SIZE - 6);

      g2.setColor(Color.ORANGE);
      g2.drawRect(x + 5, y + 5, CELL_SIZE - 10, CELL_SIZE - 10);
    }

    g2.setColor(Color.BLACK);
    g2.fillRect(0, ROWS * CELL_SIZE, COLS * CELL_SIZE, 40);

    g2.setColor(Color.WHITE);
    g2.setFont(new Font("Arial", Font.BOLD, 20));
    g2.drawString("Score: " + score, 20, ROWS * CELL_SIZE + 27);
  }

  private void drawGem(Graphics2D g2, int gem, int x, int y) {
    if (gem < 0) return;

    g2.setColor(colors[gem]);
    g2.fillOval(x + 10, y + 10, CELL_SIZE - 20, CELL_SIZE - 20);
  }

  private void drawBreakingGem(Graphics2D g2, int gem, int x, int y, double progress) {
    if (gem < 0) return;

    Color color = colors[gem];

    int centerX = x + CELL_SIZE / 2;
    int centerY = y + CELL_SIZE / 2;

    int alpha = Math.max(0, 255 - (int) (progress * 255));
    int distance = (int) (progress * 35);
    int pieceSize = Math.max(4, (int) ((CELL_SIZE - 20) / 3.0 * (1.0 - progress * 0.5)));

    Color fadingColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    g2.setColor(fadingColor);

    g2.fillOval(centerX - pieceSize / 2 - distance, centerY - pieceSize / 2 - distance, pieceSize, pieceSize);
    g2.fillOval(centerX - pieceSize / 2 + distance, centerY - pieceSize / 2 - distance, pieceSize, pieceSize);
    g2.fillOval(centerX - pieceSize / 2 - distance, centerY - pieceSize / 2 + distance, pieceSize, pieceSize);
    g2.fillOval(centerX - pieceSize / 2 + distance, centerY - pieceSize / 2 + distance, pieceSize, pieceSize);

    int sparkleAlpha = Math.max(0, 200 - (int) (progress * 200));
    g2.setColor(new Color(255, 255, 255, sparkleAlpha));

    int sparkleSize = Math.max(2, (int) (8 * (1.0 - progress)));
    g2.fillOval(centerX - distance, centerY - sparkleSize / 2, sparkleSize, sparkleSize);
    g2.fillOval(centerX + distance, centerY - sparkleSize / 2, sparkleSize, sparkleSize);
    g2.fillOval(centerX - sparkleSize / 2, centerY - distance, sparkleSize, sparkleSize);
    g2.fillOval(centerX - sparkleSize / 2, centerY + distance, sparkleSize, sparkleSize);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Montezuma Style Match-3 Game");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);
    frame.add(new Match3Game());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}