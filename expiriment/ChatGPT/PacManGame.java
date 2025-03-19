import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class PacManGame extends JPanel implements ActionListener {
    private final int TILE_SIZE = 20;
    private final int GRID_COLS = 28;
    private final int GRID_ROWS = 31;
    private final int SCREEN_WIDTH = GRID_COLS * TILE_SIZE;
    private final int SCREEN_HEIGHT = GRID_ROWS * TILE_SIZE;
    
    private final int[][] maze = new int[GRID_ROWS][GRID_COLS]; // Placeholder maze
    private int pacX = 14 * TILE_SIZE, pacY = 23 * TILE_SIZE; // Starting position
    private int pacDirX = 0, pacDirY = 0;
    private Timer timer;

    public PacManGame() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new PacKeyAdapter());
        loadMaze();
        timer = new Timer(100, this);
        timer.start();
    }

    private void loadMaze() {
        // Example simple maze layout (0: path, 1: wall)
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                if (i == 0 || i == GRID_ROWS - 1 || j == 0 || j == GRID_COLS - 1) {
                    maze[i][j] = 1; // Borders as walls
                } else {
                    maze[i][j] = 0; // Paths
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        movePacMan();
        repaint();
    }

    private void movePacMan() {
        int newX = pacX + pacDirX * TILE_SIZE;
        int newY = pacY + pacDirY * TILE_SIZE;
        
        if (maze[newY / TILE_SIZE][newX / TILE_SIZE] == 0) { // Check wall collision
            pacX = newX;
            pacY = newY;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawMaze(g);
        drawPacMan(g);
    }

    private void drawMaze(Graphics g) {
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                if (maze[i][j] == 1) {
                    g.setColor(Color.BLUE);
                    g.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawPacMan(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval(pacX, pacY, TILE_SIZE, TILE_SIZE);
    }

    private class PacKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT) { pacDirX = -1; pacDirY = 0; }
            if (key == KeyEvent.VK_RIGHT) { pacDirX = 1; pacDirY = 0; }
            if (key == KeyEvent.VK_UP) { pacDirX = 0; pacDirY = -1; }
            if (key == KeyEvent.VK_DOWN) { pacDirX = 0; pacDirY = 1; }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pac-Man");
        PacManGame game = new PacManGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}