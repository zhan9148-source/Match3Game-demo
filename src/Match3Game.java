import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Match3Game extends JPanel {

  private static final int TOP_BAR_HEIGHT = 55;

  private static final int ANIMATION_STEPS = 10;
  private static final int DROP_STEPS = 12;
  private static final int CLEAR_STEPS = 14;

  private final GameBoard gameBoard = new GameBoard();

  private final int[][] newBoard =
      new int[GameBoard.ROWS][GameBoard.COLS];

  private final int[][] startRow =
      new int[GameBoard.ROWS][GameBoard.COLS];

  private boolean[][] clearingMatches =
      new boolean[GameBoard.ROWS][GameBoard.COLS];

  private int selectedRow = -1;
  private int selectedCol = -1;

  private boolean animating = false;
  private boolean swapBack = false;
  private boolean dropping = false;
  private boolean clearing = false;

  private int aRow, aCol, bRow, bCol;

  private int animationStep = 0;
  private int dropStep = 0;
  private int clearStep = 0;

  private int cellSize = 70;

  private int offsetX = 0;
  private int offsetY = TOP_BAR_HEIGHT;

  private final Color[] colors = {
      Color.RED,
      Color.BLUE,
      Color.GREEN,
      Color.YELLOW,
      Color.MAGENTA
  };

  public Match3Game() {

    setPreferredSize(new Dimension(900, 900));

    setBackground(Color.BLACK);

    addMouseListener(new MouseAdapter() {

      @Override
      public void mousePressed(MouseEvent e) {

        if (!animating) {
          handleClick(e.getX(), e.getY());
        }
      }
    });
  }

  private void updateLayoutValues() {

    int availableWidth = getWidth();

    int availableHeight =
        getHeight() - TOP_BAR_HEIGHT;

    cellSize = Math.min(
        availableWidth / GameBoard.COLS,
        availableHeight / GameBoard.ROWS
    );

    int boardWidth =
        cellSize * GameBoard.COLS;

    int boardHeight =
        cellSize * GameBoard.ROWS;

    offsetX =
        (getWidth() - boardWidth) / 2;

    offsetY =
        TOP_BAR_HEIGHT +
            (availableHeight - boardHeight) / 2;
  }

  private void handleClick(int x, int y) {

    updateLayoutValues();

    if (x < offsetX ||
        x >= offsetX + cellSize * GameBoard.COLS) {
      return;
    }

    if (y < offsetY ||
        y >= offsetY + cellSize * GameBoard.ROWS) {
      return;
    }

    int col =
        (x - offsetX) / cellSize;

    int row =
        (y - offsetY) / cellSize;

    if (selectedRow == -1) {

      selectedRow = row;
      selectedCol = col;

    } else {

      if (isAdjacent(
          selectedRow,
          selectedCol,
          row,
          col
      )) {

        startSwapAnimation(
            selectedRow,
            selectedCol,
            row,
            col
        );

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

  private boolean isAdjacent(
      int r1,
      int c1,
      int r2,
      int c2
  ) {

    return Math.abs(r1 - r2)
        + Math.abs(c1 - c2)
        == 1;
  }

  private void startSwapAnimation(
      int r1,
      int c1,
      int r2,
      int c2
  ) {

    aRow = r1;
    aCol = c1;

    bRow = r2;
    bCol = c2;

    animationStep = 0;

    swapBack = false;
    dropping = false;
    clearing = false;

    animating = true;

    Timer timer =
        new Timer(15, null);

    timer.addActionListener(e -> {

      animationStep++;

      repaint();

      if (animationStep >= ANIMATION_STEPS) {

        timer.stop();

        gameBoard.swap(
            aRow,
            aCol,
            bRow,
            bCol
        );

        if (gameBoard.hasMatch()) {

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

    Timer timer =
        new Timer(15, null);

    timer.addActionListener(e -> {

      animationStep++;

      repaint();

      if (animationStep >= ANIMATION_STEPS) {

        timer.stop();

        gameBoard.swap(
            aRow,
            aCol,
            bRow,
            bCol
        );

        animating = false;

        swapBack = false;

        repaint();
      }
    });

    timer.start();
  }

  private void resolveBoard() {

    clearingMatches =
        gameBoard.findMatches();

    startClearAnimation();
  }

  private void startClearAnimation() {

    clearStep = 0;

    clearing = true;

    animating = true;

    Timer timer =
        new Timer(25, null);

    timer.addActionListener(e -> {

      clearStep++;

      repaint();

      if (clearStep >= CLEAR_STEPS) {

        timer.stop();

        gameBoard.removeMatches(
            clearingMatches
        );

        clearing = false;

        startDropAnimation();
      }
    });

    timer.start();
  }

  private void startDropAnimation() {

    gameBoard.calculateDropResult(
        newBoard,
        startRow
    );

    dropStep = 0;

    dropping = true;

    animating = true;

    Timer timer =
        new Timer(15, null);

    timer.addActionListener(e -> {

      dropStep++;

      repaint();

      if (dropStep >= DROP_STEPS) {

        timer.stop();

        gameBoard.copyFrom(newBoard);

        dropping = false;

        animating = false;

        if (gameBoard.hasMatch()) {

          resolveBoard();

        } else if (!gameBoard.hasPossibleMove()) {

          gameBoard.initBoard();
        }

        repaint();
      }
    });

    timer.start();
  }

  @Override
  protected void paintComponent(Graphics g) {

    super.paintComponent(g);

    updateLayoutValues();

    Graphics2D g2 = (Graphics2D) g;

    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON
    );

    GradientPaint background = new GradientPaint(
        0, 0, new Color(35, 25, 18),
        0, getHeight(), new Color(10, 8, 6)
    );

    g2.setPaint(background);
    g2.fillRect(0, 0, getWidth(), getHeight());

    g2.setColor(new Color(255, 230, 170));
    g2.setFont(new Font("Arial", Font.BOLD, 32));

    g2.drawString(
        "Score: " + gameBoard.getScore(),
        25,
        38
    );

    double progress =
        animationStep / (double) ANIMATION_STEPS;

    if (swapBack) {
      progress = 1.0 - progress;
    }

    for (int r = 0; r < GameBoard.ROWS; r++) {
      for (int c = 0; c < GameBoard.COLS; c++) {

        int x = offsetX + c * cellSize;
        int y = offsetY + r * cellSize;

        if ((r + c) % 2 == 0) {
          g2.setColor(new Color(210, 190, 150));
        } else {
          g2.setColor(new Color(175, 155, 120));
        }

        g2.fillRoundRect(
            x + 2,
            y + 2,
            cellSize - 4,
            cellSize - 4,
            cellSize / 6,
            cellSize / 6
        );

        g2.setColor(new Color(70, 45, 25, 150));

        g2.drawRoundRect(
            x + 2,
            y + 2,
            cellSize - 4,
            cellSize - 4,
            cellSize / 6,
            cellSize / 6
        );

        if (dropping) {
          continue;
        }

        if (clearing && clearingMatches[r][c]) {

          drawBreakingGem(
              g2,
              gameBoard.getGem(r, c),
              x,
              y,
              clearStep / (double) CLEAR_STEPS
          );

          continue;
        }

        if (animating &&
            !clearing &&
            (
                (r == aRow && c == aCol)
                    ||
                    (r == bRow && c == bCol)
            )) {

          continue;
        }

        drawGem(
            g2,
            gameBoard.getGem(r, c),
            x,
            y
        );
      }
    }

    if (animating && !dropping && !clearing) {

      int ax = offsetX + aCol * cellSize;
      int ay = offsetY + aRow * cellSize;

      int bx = offsetX + bCol * cellSize;
      int by = offsetY + bRow * cellSize;

      int drawAX =
          (int) (ax + (bx - ax) * progress);

      int drawAY =
          (int) (ay + (by - ay) * progress);

      int drawBX =
          (int) (bx + (ax - bx) * progress);

      int drawBY =
          (int) (by + (ay - by) * progress);

      drawGem(
          g2,
          gameBoard.getGem(aRow, aCol),
          drawAX,
          drawAY
      );

      drawGem(
          g2,
          gameBoard.getGem(bRow, bCol),
          drawBX,
          drawBY
      );
    }

    if (dropping) {

      double dropProgress =
          dropStep / (double) DROP_STEPS;

      for (int r = 0; r < GameBoard.ROWS; r++) {
        for (int c = 0; c < GameBoard.COLS; c++) {

          int gem = newBoard[r][c];

          if (gem == -1) {
            continue;
          }

          int x = offsetX + c * cellSize;

          int fromY =
              offsetY + startRow[r][c] * cellSize;

          int toY =
              offsetY + r * cellSize;

          int y =
              (int) (fromY + (toY - fromY) * dropProgress);

          drawGem(g2, gem, x, y);
        }
      }
    }

    if (selectedRow != -1 && !animating) {

      int x =
          offsetX + selectedCol * cellSize;

      int y =
          offsetY + selectedRow * cellSize;

      g2.setStroke(
          new BasicStroke(
              Math.max(4, cellSize / 22)
          )
      );

      g2.setColor(new Color(40, 20, 5, 220));

      g2.drawRoundRect(
          x + 5,
          y + 5,
          cellSize - 10,
          cellSize - 10,
          cellSize / 5,
          cellSize / 5
      );

      g2.setColor(new Color(255, 190, 60));

      g2.drawRoundRect(
          x + 9,
          y + 9,
          cellSize - 18,
          cellSize - 18,
          cellSize / 5,
          cellSize / 5
      );

      g2.setStroke(new BasicStroke(1));
    }
  }

  private void drawGem(Graphics2D g2, int gem, int x, int y) {

    if (gem < 0) {
      return;
    }

    int padding = cellSize / 7;

    int size =
        cellSize - padding * 2;

    int gx = x + padding;
    int gy = y + padding;

    Color base = colors[gem];

    Color highlight =
        base.brighter();

    Color shadow =
        base.darker();

    GradientPaint gradient =
        new GradientPaint(
            gx,
            gy,
            highlight,
            gx + size,
            gy + size,
            shadow
        );

    g2.setPaint(gradient);

    switch (gem) {

      case 0:

        g2.fillOval(
            gx,
            gy,
            size,
            size
        );

        break;

      case 1:

        Polygon diamond =
            new Polygon();

        diamond.addPoint(
            gx + size / 2,
            gy
        );

        diamond.addPoint(
            gx + size,
            gy + size / 2
        );

        diamond.addPoint(
            gx + size / 2,
            gy + size
        );

        diamond.addPoint(
            gx,
            gy + size / 2
        );

        g2.fillPolygon(diamond);

        break;

      case 2:

        g2.fillRoundRect(
            gx,
            gy,
            size,
            size,
            size / 4,
            size / 4
        );

        break;

      case 3:

        Polygon triangle =
            new Polygon();

        triangle.addPoint(
            gx + size / 2,
            gy
        );

        triangle.addPoint(
            gx + size,
            gy + size
        );

        triangle.addPoint(
            gx,
            gy + size
        );

        g2.fillPolygon(triangle);

        break;

      case 4:

        Polygon hex =
            new Polygon();

        hex.addPoint(
            gx + size / 4,
            gy
        );

        hex.addPoint(
            gx + size * 3 / 4,
            gy
        );

        hex.addPoint(
            gx + size,
            gy + size / 2
        );

        hex.addPoint(
            gx + size * 3 / 4,
            gy + size
        );

        hex.addPoint(
            gx + size / 4,
            gy + size
        );

        hex.addPoint(
            gx,
            gy + size / 2
        );

        g2.fillPolygon(hex);

        break;
    }

    g2.setColor(
        new Color(
            255,
            255,
            255,
            90
        )
    );

    g2.fillOval(
        gx + size / 5,
        gy + size / 6,
        size / 4,
        size / 5
    );

    g2.setColor(
        new Color(
            0,
            0,
            0,
            60
        )
    );

    g2.setStroke(
        new BasicStroke(
            Math.max(
                2,
                cellSize / 35
            )
        )
    );

    if (gem == 0) {

      g2.drawOval(
          gx,
          gy,
          size,
          size
      );
    }

    g2.setStroke(
        new BasicStroke(1)
    );
  }

  private void drawBreakingGem(
      Graphics2D g2,
      int gem,
      int x,
      int y,
      double progress
  ) {

    if (gem < 0) {
      return;
    }

    Color color =
        colors[gem];

    int centerX =
        x + cellSize / 2;

    int centerY =
        y + cellSize / 2;

    int alpha =
        Math.max(
            0,
            255 -
                (int)(progress * 255)
        );

    int distance =
        (int)(
            progress *
                cellSize *
                0.5
        );

    int pieceSize =
        Math.max(
            4,
            (int)(
                (cellSize * 0.25)
                    *
                    (
                        1.0 -
                            progress * 0.5
                    )
            )
        );

    Color fadingColor =
        new Color(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            alpha
        );

    g2.setColor(fadingColor);

    g2.fillOval(
        centerX -
            pieceSize / 2 -
            distance,
        centerY -
            pieceSize / 2 -
            distance,
        pieceSize,
        pieceSize
    );

    g2.fillOval(
        centerX -
            pieceSize / 2 +
            distance,
        centerY -
            pieceSize / 2 -
            distance,
        pieceSize,
        pieceSize
    );

    g2.fillOval(
        centerX -
            pieceSize / 2 -
            distance,
        centerY -
            pieceSize / 2 +
            distance,
        pieceSize,
        pieceSize
    );

    g2.fillOval(
        centerX -
            pieceSize / 2 +
            distance,
        centerY -
            pieceSize / 2 +
            distance,
        pieceSize,
        pieceSize
    );
  }

  public static void main(String[] args) {

    JFrame frame =
        new JFrame(
            "Montezuma Style Match-3 Game"
        );

    frame.setDefaultCloseOperation(
        JFrame.EXIT_ON_CLOSE
    );

    frame.add(new Match3Game());

    frame.setResizable(true);

    frame.setExtendedState(
        JFrame.MAXIMIZED_BOTH
    );

    frame.setVisible(true);
  }
}