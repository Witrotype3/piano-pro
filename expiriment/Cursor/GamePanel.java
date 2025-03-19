import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {
    private static final int CELL_SIZE = 20;
    private static final int MAZE_WIDTH = 28;
    private static final int MAZE_HEIGHT = 31;
    
    private PacMan pacman;
    private Ghost[] ghosts;
    private Maze maze;
    private int score;
    private int lives;
    private boolean gameOver;
    private boolean gameWon;
    
    public GamePanel() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(MAZE_WIDTH * CELL_SIZE, MAZE_HEIGHT * CELL_SIZE));
        
        // Initialize game objects
        maze = new Maze();
        pacman = new PacMan(MAZE_WIDTH / 2, MAZE_HEIGHT / 2);
        ghosts = new Ghost[] {
            new Ghost("Blinky", Color.RED, 0, 0),
            new Ghost("Pinky", Color.PINK, MAZE_WIDTH - 1, 0),
            new Ghost("Inky", Color.CYAN, 0, MAZE_HEIGHT - 1),
            new Ghost("Clyde", Color.ORANGE, MAZE_WIDTH - 1, MAZE_HEIGHT - 1)
        };
        
        score = 0;
        lives = 3;
        gameOver = false;
        gameWon = false;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw maze
        maze.draw(g2d, CELL_SIZE);
        
        // Draw dots and power pellets
        maze.drawDots(g2d, CELL_SIZE);
        
        // Draw Pac-Man
        pacman.draw(g2d, CELL_SIZE);
        
        // Draw ghosts
        for (Ghost ghost : ghosts) {
            ghost.draw(g2d, CELL_SIZE);
        }
        
        // Draw score and lives
        g2d.setColor(Color.WHITE);
        g2d.drawString("Score: " + score, 10, 20);
        g2d.drawString("Lives: " + lives, 10, 40);
        
        // Draw game over or win message
        if (gameOver || gameWon) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            String message = gameOver ? "GAME OVER" : "YOU WIN!";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2d.drawString(message, x, y);
        }
    }
    
    public void update() {
        if (gameOver || gameWon) return;
        
        // Update Pac-Man
        pacman.update();
        
        // Update ghosts
        for (Ghost ghost : ghosts) {
            ghost.update(pacman.getX(), pacman.getY());
        }
        
        // Check collisions
        checkCollisions();
        
        // Check win condition
        if (maze.areAllDotsEaten()) {
            gameWon = true;
        }
    }
    
    private void checkCollisions() {
        // Check Pac-Man collision with dots
        if (maze.eatDot(pacman.getX(), pacman.getY())) {
            score += 10;
        }
        
        // Check Pac-Man collision with power pellets
        if (maze.eatPowerPellet(pacman.getX(), pacman.getY())) {
            score += 50;
            makeGhostsVulnerable();
        }
        
        // Check Pac-Man collision with ghosts
        for (Ghost ghost : ghosts) {
            if (pacman.collidesWith(ghost)) {
                if (ghost.isVulnerable()) {
                    ghost.reset();
                    score += 200;
                } else {
                    lives--;
                    if (lives <= 0) {
                        gameOver = true;
                    } else {
                        resetLevel();
                    }
                }
            }
        }
    }
    
    private void makeGhostsVulnerable() {
        for (Ghost ghost : ghosts) {
            ghost.makeVulnerable();
        }
    }
    
    private void resetLevel() {
        pacman.reset();
        for (Ghost ghost : ghosts) {
            ghost.reset();
        }
    }
    
    public void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                pacman.setDirection(Direction.UP);
                break;
            case KeyEvent.VK_DOWN:
                pacman.setDirection(Direction.DOWN);
                break;
            case KeyEvent.VK_LEFT:
                pacman.setDirection(Direction.LEFT);
                break;
            case KeyEvent.VK_RIGHT:
                pacman.setDirection(Direction.RIGHT);
                break;
        }
    }
} 