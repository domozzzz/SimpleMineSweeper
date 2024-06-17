import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
    private final int SCREEN_HEIGHT = 600;
    private final int SCREEN_WIDTH = 600;
	private final int ROWS = 5;
    private final int COLS = 5;
    private final int X_SCALE = SCREEN_HEIGHT / ROWS;
    private final int Y_SCALE = SCREEN_WIDTH / COLS;
    private final int BOMBS = 3;
    
    private final int BOMB = 2;
    
    private int[][] grid;
    private boolean[][] uncoveredGrid;
    private boolean gameWon, gameOver;
    
    private Random random;

    public GamePanel() {
    	random = new Random();
    	
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setFocusable(true);
        this.addMouseListener(new GameMouseAdapter());

        start();
    }
    
    private void start() {
       	new Timer(0, this).start();
        setup();
    }

    private void addBombs() {
        int row;
        int col;

        //place specified number of bombs without doubling up on tiles
        for (int i = BOMBS; i > 0; i--) {
            row = random.nextInt(ROWS);
            col = random.nextInt(COLS);
            if (grid[row][col] != BOMB) {
                grid[row][col] = BOMB;
            }
            else i++;
        }
    }

    private void removeStartingTile() {
    	int startX = random.nextInt(COLS);
        int startY = random.nextInt(ROWS);

        if (grid[startX][startY] != BOMB) {
            removeTile(startX, startY);
        } else {
            removeStartingTile();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {

        // draw uncovered tiles
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                // draw uncovered safe tiles
                if (uncoveredGrid[i][j]) {
                    g.setColor(Color.green);
                    g.fillRect(i * X_SCALE, j * Y_SCALE, X_SCALE, Y_SCALE);
                }
                
                // draw uncovered bombs
                if (grid[i][j] == 2 && uncoveredGrid[i][j]) {
                    g.setColor(Color.red);
                    g.fillRect(i * X_SCALE, j * Y_SCALE, X_SCALE, Y_SCALE);
                }
            }
        }

        // draw text for number of surrounding bombs
        g.setColor(Color.black);
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int bombsSurrounding = 0;
                // when the game is running, don't draw bombs and uncovered tiles
                if (!(gameWon || gameOver) && grid[i][j] != BOMB && uncoveredGrid[i][j]) {
                    if (i < ROWS - 1 && grid[i + 1][j] == BOMB) {
                        bombsSurrounding++;
                    }
                    if (i < ROWS - 1 && j < COLS - 1 && grid[i + 1][j + 1] == BOMB) {
                        bombsSurrounding++;
                    }
                    if (j < COLS - 1 && grid[i][j + 1] == BOMB) {
                        bombsSurrounding++;
                    }
                    if (j > 0 && grid[i][j - 1] == BOMB) {
                        bombsSurrounding++;
                    }
                    if (i > 0 && grid[i - 1][j] == BOMB) {
                        bombsSurrounding++;
                    }
                    if (i > 0 && j < COLS - 1 && grid[i - 1][j + 1] == BOMB) {
                        bombsSurrounding++;
                    }
                    if (j > 0 && i < COLS - 1 && grid[i + 1][j - 1] == BOMB) {
                        bombsSurrounding++;
                    }
                    if (j > 0 && i > 0 && grid[i - 1][j - 1] == BOMB) {
                        bombsSurrounding++;
                    }
                    if (bombsSurrounding > 0) {
                        g.setFont(new Font("TimesRoman", Font.PLAIN, 26));
                        // draw in center of tile
                        g.drawString(Integer.toString(bombsSurrounding),
                        		i*X_SCALE + X_SCALE/2, j * Y_SCALE + Y_SCALE/2);
                    }
                }
            }
        }

        String text = "";
        // game over text
        if (gameOver) {
            g.setColor(Color.black);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 32));
            text = "Game Over";
        }
        // game won text
        else if (gameWon) {
            g.setColor(Color.black);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 32));
            text = "Game Won";
        }
        
        FontMetrics fm = g.getFontMetrics();
        
        // draw game end message, center text on x and y
        g.drawString(text, SCREEN_WIDTH / 2 - fm.stringWidth(text) / 2,
        		(SCREEN_HEIGHT - fm.getHeight()) / 2);

        
        // play again text
        if (gameWon || gameOver) {
        	g.setColor(Color.black);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 32));
            String playAgainText = "Click to Play Again";
            //center text on x, center text on y and offset
            g.drawString(playAgainText, SCREEN_WIDTH / 2 - fm.stringWidth(playAgainText)/2,
            		(SCREEN_HEIGHT - fm.getHeight()) / 2 + fm.getHeight());
        }
    }
    
	private void selectTile(int row, int col) {		

        removeTile(row, col);
        
        //lose: bomb uncovered
        if (grid[row][col] == BOMB) {
            gameOver = true;
            return;
        }
        
		int uncoveredSafeTiles = 0;
        // count safe tiles uncovered
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (uncoveredGrid[i][j]) {
                	uncoveredSafeTiles++;
                }
            }
        }

        // win: all safe tiles uncovered
        if (uncoveredSafeTiles == ROWS*COLS - BOMBS) {
            gameWon = true;
        }
		
	}

    private void removeTile(int row, int col) {
        uncoveredGrid[row][col] = true;
    }

    private void setup() {
        grid = new int[ROWS][COLS];
        uncoveredGrid = new boolean[ROWS][COLS];
        gameOver = false;
        gameWon = false;
        addBombs();
        removeStartingTile();
    }
    
    private class GameMouseAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {

        	// click m1 to place tile if game is not over
            if (!(gameOver || gameWon) && e.getButton() == MouseEvent.BUTTON1) {
                int row = e.getX()/X_SCALE;
                int col = e.getY()/Y_SCALE;
                selectTile(row, col);

            // click m1 to restart if game is over
            } else if ((gameOver || gameWon) && e.getButton() == MouseEvent.BUTTON1) {
            	setup();
            }
        }
    }
}
