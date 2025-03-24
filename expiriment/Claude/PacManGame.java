import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import javax.sound.sampled.*;
import javax.swing.*;

// Main class that launches the game
public class PacManGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow gameWindow = new GameWindow();
            gameWindow.setVisible(true);
        });
    }
}

// Game Window class
class GameWindow extends JFrame {
    private GamePanel gamePanel;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    
    public GameWindow() {
        setTitle("Pac-Man");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                gamePanel.handleKeyPress(e);
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                gamePanel.handleKeyRelease(e);
            }
        });
    }
}

// Game Panel where the game is rendered
class GamePanel extends JPanel {
    // Game constants
    private static final int GRID_WIDTH = 28;
    private static final int GRID_HEIGHT = 31;
    private static final int CELL_SIZE = 20;
    private static final int PANEL_WIDTH = GRID_WIDTH * CELL_SIZE;
    private static final int PANEL_HEIGHT = GRID_HEIGHT * CELL_SIZE;
    
    // Game objects
    private GameEngine gameEngine;
    private PacMan pacMan;
    private List<Ghost> ghosts;
    private Maze maze;
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private boolean paused = false;
    private HighScoreManager highScoreManager;
    
    // Menu items
    private Rectangle resumeButton;
    private Rectangle restartButton;
    
    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        
        resumeButton = new Rectangle(PANEL_WIDTH / 2 - 100, PANEL_HEIGHT / 2 - 50, 200, 40);
        restartButton = new Rectangle(PANEL_WIDTH / 2 - 100, PANEL_HEIGHT / 2 + 20, 200, 40);
        
        highScoreManager = new HighScoreManager();
        initializeGame();
        
        // Mouse listener for pause menu
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (paused) {
                    Point p = e.getPoint();
                    if (resumeButton.contains(p)) {
                        paused = false;
                        gameEngine.resumeGame();
                    } else if (restartButton.contains(p)) {
                        initializeGame();
                        paused = false;
                    }
                }
            }
        });
    }
    
    public void initializeGame() {
        maze = new Maze(GRID_WIDTH, GRID_HEIGHT);
        pacMan = new PacMan(14, 23, maze);
        
        ghosts = new ArrayList<>();
        // Initialize ghosts with different colors, starting positions, and behaviors
        ghosts.add(new Ghost(13, 14, Color.RED, Ghost.GhostType.BLINKY, maze, pacMan)); // Blinky
        ghosts.add(new Ghost(14, 14, Color.PINK, Ghost.GhostType.PINKY, maze, pacMan)); // Pinky
        ghosts.add(new Ghost(13, 15, Color.CYAN, Ghost.GhostType.INKY, maze, pacMan)); // Inky
        ghosts.add(new Ghost(14, 15, Color.ORANGE, Ghost.GhostType.CLYDE, maze, pacMan)); // Clyde
        
        gameEngine = new GameEngine(this, pacMan, ghosts, maze);
        gameEngine.startGame();
        
        score = 0;
        lives = 3;
        gameOver = false;
        gameWon = false;
    }
    
    public void handleKeyPress(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // Toggle pause with ESCAPE key
        if (keyCode == KeyEvent.VK_ESCAPE) {
            if (paused) {
                paused = false;
                gameEngine.resumeGame();
            } else {
                paused = true;
                gameEngine.pauseGame();
            }
            return;
        }
        
        // Skip other inputs if game is paused or over
        if (paused || gameOver || gameWon) {
            return;
        }
        
        switch (keyCode) {
            case KeyEvent.VK_UP:
                pacMan.setNextDirection(Direction.UP);
                break;
            case KeyEvent.VK_DOWN:
                pacMan.setNextDirection(Direction.DOWN);
                break;
            case KeyEvent.VK_LEFT:
                pacMan.setNextDirection(Direction.LEFT);
                break;
            case KeyEvent.VK_RIGHT:
                pacMan.setNextDirection(Direction.RIGHT);
                break;
        }
    }
    
    public void handleKeyRelease(KeyEvent e) {
        // Can be used for more sophisticated controls
    }
    
    public void updateScore(int points) {
        score += points;
        // Check if all pellets are eaten
        if (maze.getRemainingPellets() == 0) {
            gameWon = true;
            gameEngine.pauseGame();
            highScoreManager.checkAndSaveHighScore(score);
            SoundManager.playSound(SoundEffect.WIN);
        }
    }
    
    public void loseLife() {
        lives--;
        if (lives <= 0) {
            gameOver = true;
            gameEngine.pauseGame();
            highScoreManager.checkAndSaveHighScore(score);
            SoundManager.playSound(SoundEffect.GAME_OVER);
        } else {
            resetPositions();
            SoundManager.playSound(SoundEffect.LOSE_LIFE);
        }
    }
    
    private void resetPositions() {
        pacMan.reset(14, 23);
        ghosts.get(0).reset(13, 14); // Blinky
        ghosts.get(1).reset(14, 14); // Pinky
        ghosts.get(2).reset(13, 15); // Inky
        ghosts.get(3).reset(14, 15); // Clyde
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the maze
        maze.draw(g2d, CELL_SIZE);
        
        // Draw Pac-Man
        pacMan.draw(g2d, CELL_SIZE);
        
        // Draw ghosts
        for (Ghost ghost : ghosts) {
            ghost.draw(g2d, CELL_SIZE);
        }
        
        // Draw score
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Score: " + score, 20, PANEL_HEIGHT - 10);
        
        // Draw lives
        for (int i = 0; i < lives; i++) {
            g2d.setColor(Color.YELLOW);
            g2d.fillArc(PANEL_WIDTH - 30 - (i * 25), PANEL_HEIGHT - 20, 20, 20, 30, 300);
        }
        
        // Draw high score
        g2d.setColor(Color.WHITE);
        g2d.drawString("High Score: " + highScoreManager.getHighScore(), PANEL_WIDTH / 2 - 60, PANEL_HEIGHT - 10);
        
        // Draw game over or win message
        if (gameOver) {
            drawCenteredMessage(g2d, "GAME OVER", Color.RED);
            drawCenteredMessage(g2d, "Press ESCAPE to restart", Color.WHITE, 30);
        } else if (gameWon) {
            drawCenteredMessage(g2d, "YOU WIN!", Color.GREEN);
            drawCenteredMessage(g2d, "Press ESCAPE to restart", Color.WHITE, 30);
        }
        
        // Draw pause menu
        if (paused) {
            drawPauseMenu(g2d);
        }
    }
    
    private void drawCenteredMessage(Graphics2D g2d, String message, Color color) {
        drawCenteredMessage(g2d, message, color, 0);
    }
    
    private void drawCenteredMessage(Graphics2D g2d, String message, Color color, int yOffset) {
        g2d.setColor(color);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (PANEL_WIDTH - metrics.stringWidth(message)) / 2;
        int y = (PANEL_HEIGHT - metrics.getHeight()) / 2 + metrics.getAscent() + yOffset;
        g2d.drawString(message, x, y);
    }
    
    private void drawPauseMenu(Graphics2D g2d) {
        // Transparent background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        
        // Pause title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        g2d.drawString("PAUSED", PANEL_WIDTH / 2 - 60, PANEL_HEIGHT / 2 - 80);
        
        // Resume button
        g2d.setColor(Color.BLUE);
        g2d.fill(resumeButton);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Resume", PANEL_WIDTH / 2 - 40, PANEL_HEIGHT / 2 - 25);
        
        // Restart button
        g2d.setColor(Color.RED);
        g2d.fill(restartButton);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Restart", PANEL_WIDTH / 2 - 40, PANEL_HEIGHT / 2 + 45);
    }
}

// Direction enum for movement
enum Direction {
    UP, DOWN, LEFT, RIGHT, NONE
}

// Pac-Man class
class PacMan {
    private int x, y;             // Grid position
    private int startX, startY;   // Starting position
    private Direction currentDirection = Direction.NONE;
    private Direction nextDirection = Direction.NONE;
    private int mouthAngle = 0;
    private boolean mouthOpening = true;
    private Maze maze;
    
    public PacMan(int x, int y, Maze maze) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.maze = maze;
    }
    
    public void move() {
        // Try to change direction if requested
        if (nextDirection != currentDirection && canMove(nextDirection)) {
            currentDirection = nextDirection;
        }
        
        // Move in current direction if possible
        if (canMove(currentDirection)) {
            switch (currentDirection) {
                case UP:
                    y--;
                    break;
                case DOWN:
                    y++;
                    break;
                case LEFT:
                    x--;
                    break;
                case RIGHT:
                    x++;
                    break;
                case NONE:
                    break;
            }
            
            // Check for pellet collision
            if (maze.hasPellet(x, y)) {
                maze.eatPellet(x, y);
                SoundManager.playSound(SoundEffect.EAT_PELLET);
            }
            
            // Check for power pellet collision
            if (maze.hasPowerPellet(x, y)) {
                maze.eatPowerPellet(x, y);
                SoundManager.playSound(SoundEffect.EAT_POWER_PELLET);
            }
        }
        
        // Animate mouth
        if (mouthOpening) {
            mouthAngle += 5;
            if (mouthAngle >= 45) {
                mouthOpening = false;
            }
        } else {
            mouthAngle -= 5;
            if (mouthAngle <= 0) {
                mouthOpening = true;
            }
        }
    }
    
    private boolean canMove(Direction direction) {
        int newX = x;
        int newY = y;
        
        switch (direction) {
            case UP:
                newY--;
                break;
            case DOWN:
                newY++;
                break;
            case LEFT:
                newX--;
                break;
            case RIGHT:
                newX++;
                break;
            case NONE:
                return false;
        }
        
        // Check if the new position is valid (not a wall)
        return !maze.isWall(newX, newY);
    }
    
    public void draw(Graphics2D g2d, int cellSize) {
        int pixelX = x * cellSize;
        int pixelY = y * cellSize;
        
        g2d.setColor(Color.YELLOW);
        
        // Calculate mouth angle based on direction
        int startAngle = 0;
        switch (currentDirection) {
            case UP:
                startAngle = 90 - mouthAngle;
                break;
            case DOWN:
                startAngle = 270 - mouthAngle;
                break;
            case LEFT:
                startAngle = 180 - mouthAngle;
                break;
            case RIGHT:
                startAngle = 0 - mouthAngle;
                break;
            case NONE:
                startAngle = 0 - mouthAngle;
                break;
        }
        
        // Draw Pac-Man with animated mouth
        g2d.fillArc(pixelX, pixelY, cellSize, cellSize, startAngle, 360 - 2 * mouthAngle);
    }
    
    public void setNextDirection(Direction direction) {
        this.nextDirection = direction;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void reset(int x, int y) {
        this.x = x;
        this.y = y;
        this.currentDirection = Direction.NONE;
        this.nextDirection = Direction.NONE;
    }
    
    public Direction getDirection() {
        return currentDirection;
    }
}

// Ghost class
class Ghost {
    private int x, y;         // Grid position
    private int startX, startY; // Starting position
    private Color color;
    private Direction currentDirection = Direction.UP;
    private Maze maze;
    private PacMan pacMan;
    private GhostType type;
    private boolean vulnerable = false;
    private long vulnerableEndTime = 0;
    private Random random = new Random();
    
    public enum GhostType {
        BLINKY, PINKY, INKY, CLYDE
    }
    
    public Ghost(int x, int y, Color color, GhostType type, Maze maze, PacMan pacMan) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.color = color;
        this.type = type;
        this.maze = maze;
        this.pacMan = pacMan;
    }
    
    public void move() {
        // Check if vulnerable time has expired
        if (vulnerable && System.currentTimeMillis() >= vulnerableEndTime) {
            vulnerable = false;
        }
        
        // Choose direction based on ghost type and vulnerability
        Direction newDirection;
        if (vulnerable) {
            // When vulnerable, move randomly
            newDirection = getRandomDirection();
        } else {
            // Use specific AI based on ghost type
            switch (type) {
                case BLINKY:
                    // Blinky directly targets Pac-Man
                    newDirection = moveTowardsPacMan();
                    break;
                case PINKY:
                    // Pinky tries to move ahead of Pac-Man
                    newDirection = moveAheadOfPacMan();
                    break;
                case INKY:
                    // Inky uses a mix of strategies
                    newDirection = moveInkyStyle();
                    break;
                case CLYDE:
                    // Clyde moves randomly unless close to Pac-Man, then moves away
                    newDirection = moveClydeStyle();
                    break;
                default:
                    newDirection = getRandomDirection();
            }
        }
        
        // If we can move in the new direction, do so
        if (canMove(newDirection)) {
            currentDirection = newDirection;
        } else if (!canMove(currentDirection)) {
            // If we can't move in current direction, try a random one
            Direction randomDir = getRandomDirection();
            while (!canMove(randomDir)) {
                randomDir = getRandomDirection();
            }
            currentDirection = randomDir;
        }
        
        // Move in current direction
        switch (currentDirection) {
            case UP:
                y--;
                break;
            case DOWN:
                y++;
                break;
            case LEFT:
                x--;
                break;
            case RIGHT:
                x++;
                break;
        }
    }
    
    private Direction moveTowardsPacMan() {
        // Simple BFS implementation for Blinky
        return findPathToPacMan();
    }
    
    private Direction moveAheadOfPacMan() {
        // Pinky tries to be ahead of Pac-Man by 4 tiles
        int targetX = pacMan.getX();
        int targetY = pacMan.getY();
        
        // Adjust target based on Pac-Man's direction
        switch (pacMan.getDirection()) {
            case UP:
                targetY -= 4;
                break;
            case DOWN:
                targetY += 4;
                break;
            case LEFT:
                targetX -= 4;
                break;
            case RIGHT:
                targetX += 4;
                break;
        }
        
        // Make sure target is within bounds
        targetX = Math.max(0, Math.min(maze.getWidth() - 1, targetX));
        targetY = Math.max(0, Math.min(maze.getHeight() - 1, targetY));
        
        return findPathToTarget(targetX, targetY);
    }
    
    private Direction moveInkyStyle() {
        // With 50% chance, chase directly; otherwise, move ahead like Pinky
        if (random.nextBoolean()) {
            return moveTowardsPacMan();
        } else {
            return moveAheadOfPacMan();
        }
    }
    
    private Direction moveClydeStyle() {
        // Calculate Manhattan distance to Pac-Man
        int distance = Math.abs(x - pacMan.getX()) + Math.abs(y - pacMan.getY());
        
        // If close to Pac-Man, move away; otherwise, move randomly
        if (distance < 8) {
            // Move away from Pac-Man
            int dx = x - pacMan.getX();
            int dy = y - pacMan.getY();
            
            if (Math.abs(dx) > Math.abs(dy)) {
                // Move horizontally away
                return dx > 0 ? Direction.RIGHT : Direction.LEFT;
            } else {
                // Move vertically away
                return dy > 0 ? Direction.DOWN : Direction.UP;
            }
        } else {
            // Move randomly
            return getRandomDirection();
        }
    }
    
    private Direction findPathToPacMan() {
        return findPathToTarget(pacMan.getX(), pacMan.getY());
    }
    
    private Direction findPathToTarget(int targetX, int targetY) {
        // BFS algorithm to find shortest path
        Queue<Node> queue = new LinkedList<>();
        boolean[][] visited = new boolean[maze.getWidth()][maze.getHeight()];
        Node[][] parent = new Node[maze.getWidth()][maze.getHeight()];
        
        // Start from current position
        queue.add(new Node(x, y, null, null));
        visited[x][y] = true;
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            // If we've reached the target
            if (current.x == targetX && current.y == targetY) {
                // Reconstruct path
                while (current.parent != null && current.parent.parent != null) {
                    current = current.parent;
                }
                return current.direction;
            }
            
            // Try all four directions
            tryDirection(queue, visited, parent, current, Direction.UP);
            tryDirection(queue, visited, parent, current, Direction.DOWN);
            tryDirection(queue, visited, parent, current, Direction.LEFT);
            tryDirection(queue, visited, parent, current, Direction.RIGHT);
        }
        
        // If no path found, return random direction
        return getRandomDirection();
    }
    
    private void tryDirection(Queue<Node> queue, boolean[][] visited, Node[][] parent, 
                             Node current, Direction direction) {
        int newX = current.x;
        int newY = current.y;
        
        switch (direction) {
            case UP:
                newY--;
                break;
            case DOWN:
                newY++;
                break;
            case LEFT:
                newX--;
                break;
            case RIGHT:
                newX++;
                break;
        }
        
        // Check if valid move and not visited
        if (newX >= 0 && newX < maze.getWidth() && newY >= 0 && newY < maze.getHeight() 
            && !maze.isWall(newX, newY) && !visited[newX][newY]) {
            
            Direction newDirection = current.direction == null ? direction : current.direction;
            Node newNode = new Node(newX, newY, current, newDirection);
            
            queue.add(newNode);
            visited[newX][newY] = true;
            parent[newX][newY] = newNode;
        }
    }
    
    private Direction getRandomDirection() {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        Direction oppositeDir = getOppositeDirection(currentDirection);
        
        Direction newDir;
        do {
            newDir = directions[random.nextInt(directions.length)];
            // Avoid going backwards
        } while (newDir == oppositeDir && canMove(currentDirection));
        
        return newDir;
    }
    
    private Direction getOppositeDirection(Direction direction) {
        switch (direction) {
            case UP:
                return Direction.DOWN;
            case DOWN:
                return Direction.UP;
            case LEFT:
                return Direction.RIGHT;
            case RIGHT:
                return Direction.LEFT;
            default:
                return Direction.NONE;
        }
    }
    
    private boolean canMove(Direction direction) {
        int newX = x;
        int newY = y;
        
        switch (direction) {
            case UP:
                newY--;
                break;
            case DOWN:
                newY++;
                break;
            case LEFT:
                newX--;
                break;
            case RIGHT:
                newX++;
                break;
        }
        
        // Check if the new position is valid (not a wall)
        return !maze.isWall(newX, newY);
    }
    
    public void draw(Graphics2D g2d, int cellSize) {
        int pixelX = x * cellSize;
        int pixelY = y * cellSize;
        
        // Draw ghost body
        if (vulnerable) {
            // Blinking effect near end of vulnerability
            if (vulnerableEndTime - System.currentTimeMillis() < 2000 && 
                System.currentTimeMillis() % 300 < 150) {
                g2d.setColor(Color.WHITE);
            } else {
                g2d.setColor(Color.BLUE);
            }
        } else {
            g2d.setColor(color);
        }
        
        // Ghost body (arc)
        g2d.fillArc(pixelX, pixelY, cellSize, cellSize, 0, 180);
        
        // Ghost "skirt"
        int skirtY = pixelY + cellSize / 2;
        int skirtHeight = cellSize / 2;
        int waveWidth = cellSize / 3;
        
        // Three waves at the bottom
        g2d.fillRect(pixelX, skirtY, cellSize, skirtHeight);
        
        g2d.setColor(Color.BLACK);
        
        // Draw eyes
        int eyeSize = cellSize / 4;
        int leftEyeX = pixelX + cellSize / 4 - eyeSize / 2;
        int rightEyeX = pixelX + 3 * cellSize / 4 - eyeSize / 2;
        int eyeY = pixelY + cellSize / 3 - eyeSize / 2;
        
        g2d.setColor(Color.WHITE);
        g2d.fillOval(leftEyeX, eyeY, eyeSize, eyeSize);
        g2d.fillOval(rightEyeX, eyeY, eyeSize, eyeSize);
        
        // Draw pupils
        int pupilSize = eyeSize / 2;
        int leftPupilX = leftEyeX + eyeSize / 4;
        int rightPupilX = rightEyeX + eyeSize / 4;
        int pupilY = eyeY + eyeSize / 4;
        
        g2d.setColor(Color.BLACK);
        g2d.fillOval(leftPupilX, pupilY, pupilSize, pupilSize);
        g2d.fillOval(rightPupilX, pupilY, pupilSize, pupilSize);
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setVulnerable(boolean vulnerable, int durationMillis) {
        this.vulnerable = vulnerable;
        if (vulnerable) {
            this.vulnerableEndTime = System.currentTimeMillis() + durationMillis;
        }
    }
    
    public boolean isVulnerable() {
        return vulnerable;
    }
    
    public void reset(int x, int y) {
        this.x = x;
        this.y = y;
        this.vulnerable = false;
        this.currentDirection = Direction.UP;
    }
    
    // Inner class for BFS
    private class Node {
        int x, y;
        Node parent;
        Direction direction;
        
        Node(int x, int y, Node parent, Direction direction) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.direction = direction;
        }
    }
}

// Maze class
class Maze {
    private int width;
    private int height;
    private boolean[][] walls;
    private boolean[][] pellets;
    private boolean[][] powerPellets;
    private int remainingPellets;
    
    public Maze(int width, int height) {
        this.width = width;
        this.height = height;
        this.walls = new boolean[width][height];
        this.pellets = new boolean[width][height];
        this.powerPellets = new boolean[width][height];
        
        initializeMaze();
    }
    
    private void initializeMaze() {
        // Initialize with a standard Pac-Man maze layout
        // 1 = Wall, 0 = Path, 2 = Power Pellet
        int[][] mazeLayout = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,2,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,2,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,0,1},
            {1,0,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,0,1},
            {1,0,0,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,0,1,1,1,0,0,1,1,1,0,1,1,0,1,1,1,1,1,1},
            {0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0},
            {1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,2,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,2,1},
            {1,1,1,0,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,0,1,1,1},
            {1,1,1,0,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,0,1,1,1},
            {1,0,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,1,1,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,1,1,0,1},
            {1,0,1,1,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,1,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        };
        
        remainingPellets = 0;
        
        // Convert the layout to our data structures
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (y < mazeLayout.length && x < mazeLayout[y].length) {
                    walls[x][y] = mazeLayout[y][x] == 1;
                    pellets[x][y] = mazeLayout[y][x] == 0;
                    powerPellets[x][y] = mazeLayout[y][x] == 2;
                    
                    if (pellets[x][y]) {
                        remainingPellets++;
                    }
                }
            }
        }
        
        // Remove pellets from ghost house and Pac-Man starting position
        for (int x = 13; x <= 14; x++) {
            for (int y = 14; y <= 15; y++) {
                if (pellets[x][y]) {
                    pellets[x][y] = false;
                    remainingPellets--;
                }
            }
        }
        
        // Remove pellet from Pac-Man's starting position
        if (pellets[14][23]) {
            pellets[14][23] = false;
            remainingPellets--;
        }
    }
    
    public void draw(Graphics2D g2d, int cellSize) {
        // Draw walls
        g2d.setColor(Color.BLUE);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (walls[x][y]) {
                    g2d.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }
            }
        }
        
        // Draw pellets
        g2d.setColor(Color.WHITE);
        int pelletSize = cellSize / 5;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (pellets[x][y]) {
                    int pelletX = x * cellSize + (cellSize - pelletSize) / 2;
                    int pelletY = y * cellSize + (cellSize - pelletSize) / 2;
                    g2d.fillOval(pelletX, pelletY, pelletSize, pelletSize);
                }
            }
        }
        
        // Draw power pellets
        int powerPelletSize = cellSize / 2;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (powerPellets[x][y]) {
                    int pelletX = x * cellSize + (cellSize - powerPelletSize) / 2;
                    int pelletY = y * cellSize + (cellSize - powerPelletSize) / 2;
                    
                    // Make power pellets blink
                    if (System.currentTimeMillis() % 500 < 250) {
                        g2d.fillOval(pelletX, pelletY, powerPelletSize, powerPelletSize);
                    }
                }
            }
        }
    }
    
    public boolean isWall(int x, int y) {
        // Check if coordinates are out of bounds
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true;
        }
        return walls[x][y];
    }
    
    public boolean hasPellet(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        return pellets[x][y];
    }
    
    public boolean hasPowerPellet(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        return powerPellets[x][y];
    }
    
    public void eatPellet(int x, int y) {
        if (hasPellet(x, y)) {
            pellets[x][y] = false;
            remainingPellets--;
        }
    }
    
    public void eatPowerPellet(int x, int y) {
        if (hasPowerPellet(x, y)) {
            powerPellets[x][y] = false;
        }
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getRemainingPellets() {
        return remainingPellets;
    }
}

// Game Engine - handles game loop, collisions, and game logic
class GameEngine {
    private GamePanel gamePanel;
    private PacMan pacMan;
    private List<Ghost> ghosts;
    private Maze maze;
    private Timer gameTimer;
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;
    private static final int VULNERABLE_DURATION = 10000; // 10 seconds for vulnerable ghosts
    
    public GameEngine(GamePanel gamePanel, PacMan pacMan, List<Ghost> ghosts, Maze maze) {
        this.gamePanel = gamePanel;
        this.pacMan = pacMan;
        this.ghosts = ghosts;
        this.maze = maze;
    }
    
    public void startGame() {
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
                gamePanel.repaint();
            }
        }, 0, FRAME_DELAY);
        
        SoundManager.playSound(SoundEffect.GAME_START);
    }
    
    public void pauseGame() {
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
    }
    
    public void resumeGame() {
        if (gameTimer == null) {
            startGame();
        }
    }
    
    private void update() {
        // Update Pac-Man
        pacMan.move();
        
        // Check for pellet collection
        if (maze.hasPellet(pacMan.getX(), pacMan.getY())) {
            maze.eatPellet(pacMan.getX(), pacMan.getY());
            gamePanel.updateScore(10);
            SoundManager.playSound(SoundEffect.EAT_PELLET);
        }
        
        // Check for power pellet collection
        if (maze.hasPowerPellet(pacMan.getX(), pacMan.getY())) {
            maze.eatPowerPellet(pacMan.getX(), pacMan.getY());
            gamePanel.updateScore(50);
            makeGhostsVulnerable();
            SoundManager.playSound(SoundEffect.EAT_POWER_PELLET);
        }
        
        // Update ghosts
        for (Ghost ghost : ghosts) {
            ghost.move();
            
            // Check for collision with Pac-Man
            if (ghost.getX() == pacMan.getX() && ghost.getY() == pacMan.getY()) {
                if (ghost.isVulnerable()) {
                    // Eat the ghost
                    ghost.reset(13 + (ghost.getX() % 2), 14 + (ghost.getY() % 2));
                    ghost.setVulnerable(false, 0);
                    gamePanel.updateScore(200);
                    SoundManager.playSound(SoundEffect.EAT_GHOST);
                } else {
                    // Pac-Man loses a life
                    gamePanel.loseLife();
                    return;
                }
            }
        }
    }
    
    private void makeGhostsVulnerable() {
        for (Ghost ghost : ghosts) {
            ghost.setVulnerable(true, VULNERABLE_DURATION);
        }
    }
}

// Helper class to manage sound effects
enum SoundEffect {
    GAME_START("game_start.wav"),
    EAT_PELLET("eat_pellet.wav"),
    EAT_POWER_PELLET("eat_power_pellet.wav"),
    EAT_GHOST("eat_ghost.wav"),
    LOSE_LIFE("lose_life.wav"),
    GAME_OVER("game_over.wav"),
    WIN("win.wav");
    
    private final String fileName;
    
    SoundEffect(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
}

class SoundManager {
    private static Map<SoundEffect, Clip> soundClips = new HashMap<>();
    
    static {
        // Pre-load all sound clips
        for (SoundEffect effect : SoundEffect.values()) {
            try {
                // First try to load from file system
                File soundFile = new File("sounds/" + effect.getFileName());
                if (soundFile.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    soundClips.put(effect, clip);
                } else {
                    // If file not found, try to load from resources
                    InputStream inputStream = SoundManager.class.getResourceAsStream("/sounds/" + effect.getFileName());
                    if (inputStream != null) {
                        AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                                new BufferedInputStream(inputStream));
                        Clip clip = AudioSystem.getClip();
                        clip.open(audioStream);
                        soundClips.put(effect, clip);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading sound: " + effect.getFileName());
                // Don't crash the game if sound files are missing
            }
        }
    }
    
    public static void playSound(SoundEffect effect) {
        Clip clip = soundClips.get(effect);
        if (clip != null) {
            clip.setFramePosition(0); // Rewind
            clip.start();
        }
    }
}

// High Score manager
class HighScoreManager {
    private static final String HIGH_SCORE_FILE = "pacman_highscore.dat";
    private int highScore = 0;
    
    public HighScoreManager() {
        loadHighScore();
    }
    
    private void loadHighScore() {
        try {
            File file = new File(HIGH_SCORE_FILE);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                if (line != null) {
                    highScore = Integer.parseInt(line);
                }
                reader.close();
            }
        } catch (Exception e) {
            System.err.println("Error loading high score: " + e.getMessage());
        }
    }
    
    private void saveHighScore() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE));
            writer.write(String.valueOf(highScore));
            writer.close();
        } catch (Exception e) {
            System.err.println("Error saving high score: " + e.getMessage());
        }
    }
    
    public int getHighScore() {
        return highScore;
    }
    
    public boolean checkAndSaveHighScore(int score) {
        if (score > highScore) {
            highScore = score;
            saveHighScore();
            return true;
        }
        return false;
    }
}