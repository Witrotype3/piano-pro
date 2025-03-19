import java.awt.*;

public class Maze {
    private static final int MAZE_WIDTH = 28;
    private static final int MAZE_HEIGHT = 31;
    private boolean[][] walls;
    private boolean[][] dots;
    private boolean[][] powerPellets;
    
    public Maze() {
        walls = new boolean[MAZE_WIDTH][MAZE_HEIGHT];
        dots = new boolean[MAZE_WIDTH][MAZE_HEIGHT];
        powerPellets = new boolean[MAZE_WIDTH][MAZE_HEIGHT];
        
        // Initialize maze layout (simplified version)
        initializeMaze();
    }
    
    private void initializeMaze() {
        // Create outer walls
        for (int x = 0; x < MAZE_WIDTH; x++) {
            walls[x][0] = true;
            walls[x][MAZE_HEIGHT - 1] = true;
        }
        for (int y = 0; y < MAZE_HEIGHT; y++) {
            walls[0][y] = true;
            walls[MAZE_WIDTH - 1][y] = true;
        }
        
        // Add some inner walls (simplified)
        for (int x = 5; x < MAZE_WIDTH - 5; x++) {
            walls[x][5] = true;
            walls[x][MAZE_HEIGHT - 6] = true;
        }
        for (int y = 5; y < MAZE_HEIGHT - 5; y++) {
            walls[5][y] = true;
            walls[MAZE_WIDTH - 6][y] = true;
        }
        
        // Add dots
        for (int x = 1; x < MAZE_WIDTH - 1; x++) {
            for (int y = 1; y < MAZE_HEIGHT - 1; y++) {
                if (!walls[x][y]) {
                    dots[x][y] = true;
                }
            }
        }
        
        // Add power pellets in corners
        powerPellets[1][1] = true;
        powerPellets[MAZE_WIDTH - 2][1] = true;
        powerPellets[1][MAZE_HEIGHT - 2] = true;
        powerPellets[MAZE_WIDTH - 2][MAZE_HEIGHT - 2] = true;
    }
    
    public void draw(Graphics2D g2d, int cellSize) {
        // Draw walls
        g2d.setColor(Color.BLUE);
        for (int x = 0; x < MAZE_WIDTH; x++) {
            for (int y = 0; y < MAZE_HEIGHT; y++) {
                if (walls[x][y]) {
                    g2d.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }
            }
        }
    }
    
    public void drawDots(Graphics2D g2d, int cellSize) {
        // Draw dots
        g2d.setColor(Color.WHITE);
        for (int x = 0; x < MAZE_WIDTH; x++) {
            for (int y = 0; y < MAZE_HEIGHT; y++) {
                if (dots[x][y]) {
                    g2d.fillOval(x * cellSize + cellSize / 3, y * cellSize + cellSize / 3,
                               cellSize / 3, cellSize / 3);
                }
                if (powerPellets[x][y]) {
                    g2d.fillOval(x * cellSize + cellSize / 4, y * cellSize + cellSize / 4,
                               cellSize / 2, cellSize / 2);
                }
            }
        }
    }
    
    public boolean eatDot(int x, int y) {
        if (dots[x][y]) {
            dots[x][y] = false;
            return true;
        }
        return false;
    }
    
    public boolean eatPowerPellet(int x, int y) {
        if (powerPellets[x][y]) {
            powerPellets[x][y] = false;
            return true;
        }
        return false;
    }
    
    public boolean isWall(int x, int y) {
        return walls[x][y];
    }
    
    public boolean areAllDotsEaten() {
        for (int x = 0; x < MAZE_WIDTH; x++) {
            for (int y = 0; y < MAZE_HEIGHT; y++) {
                if (dots[x][y] || powerPellets[x][y]) {
                    return false;
                }
            }
        }
        return true;
    }
} 