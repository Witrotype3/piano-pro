import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;

public class PacManGame extends JFrame {

    private GamePanel gamePanel;

    public PacManGame() {
        setTitle("Pac-Man");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);

        gamePanel = new GamePanel();
        add(gamePanel);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                gamePanel.keyPressed(e);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PacManGame());
    }
}

class GamePanel extends JPanel implements ActionListener {

    private final int GRID_SIZE = 20;
    private final int WIDTH = 28;
    private final int HEIGHT = 31;
    private final int SCREEN_WIDTH = WIDTH * GRID_SIZE;
    private final int SCREEN_HEIGHT = HEIGHT * GRID_SIZE;
    private final int PACMAN_SPEED = 3;
    private final int GHOST_SPEED = 2;

    private javax.swing.Timer timer;
    private PacMan pacMan;
    private Ghost blinky, pinky, inky, clyde;
    private int[][] maze;
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private boolean vulnerable = false;
    private long vulnerableStartTime;
    private final int VULNERABLE_DURATION = 5000; // 5 seconds
    private Clip munchSound, powerUpSound, deathSound;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();

        loadSounds();
        initializeGame();
    }

    public GamePanel(long vulnerableStartTime) {
        this.vulnerableStartTime = vulnerableStartTime;
    }

    private void loadSounds() {
        try {
            munchSound = AudioSystem.getClip();
            munchSound.open(AudioSystem.getAudioInputStream(new File("munch.wav"))); // Replace with your sound file
            powerUpSound = AudioSystem.getClip();
            powerUpSound.open(AudioSystem.getAudioInputStream(new File("powerup.wav"))); // Replace with your sound file
            deathSound = AudioSystem.getClip();
            deathSound.open(AudioSystem.getAudioInputStream(new File("death.wav"))); // Replace with your sound file
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeGame() {
        maze = MazeGenerator.generateMaze(WIDTH, HEIGHT);
        pacMan = new PacMan(GRID_SIZE * 14, GRID_SIZE * 23, PACMAN_SPEED); // Starting position
        blinky = new Ghost(GRID_SIZE * 13, GRID_SIZE * 11, GHOST_SPEED, Color.RED, "Blinky");
        pinky = new Ghost(GRID_SIZE * 14, GRID_SIZE * 11, GHOST_SPEED, Color.PINK, "Pinky");
        inky = new Ghost(GRID_SIZE * 12, GRID_SIZE * 14, GHOST_SPEED, Color.CYAN, "Inky");
        clyde = new Ghost(GRID_SIZE * 15, GRID_SIZE * 14, GHOST_SPEED, Color.ORANGE, "Clyde");

        timer = new javax.swing.Timer(40, this); // Game loop interval (milliseconds)
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !gameWon) {
            pacMan.move(maze);
            blinky.move(maze, pacMan.getX(), pacMan.getY());
            pinky.move(maze, pacMan.getX(), pacMan.getY());
            inky.move(maze, pacMan.getX(), pacMan.getY());
            clyde.move(maze, pacMan.getX(), pacMan.getY());
            checkCollisions();
            checkPellets();

            if (vulnerable && System.currentTimeMillis() - vulnerableStartTime > VULNERABLE_DURATION) {
                vulnerable = false;
            }
        }
        repaint();
    }

    private void checkCollisions() {
        if (!vulnerable) {
            if (pacMan.intersects(blinky) || pacMan.intersects(pinky) || pacMan.intersects(inky) || pacMan.intersects(clyde)) {
                lives--;
                deathSound.setFramePosition(0); // Rewind the sound
                deathSound.start();
                resetPositions();
                if (lives <= 0) {
                    gameOver = true;
                    timer.stop();
                }
            }
        } else {
            if (pacMan.intersects(blinky)) {
                score += 200; // Score for eating ghost
                blinky.resetPosition();
            }
            if (pacMan.intersects(pinky)) {
                score += 200;
                pinky.resetPosition();
            }
            if (pacMan.intersects(inky)) {
                score += 200;
                inky.resetPosition();
            }
            if (pacMan.intersects(clyde)) {
                score += 200;
                clyde.resetPosition();
            }

        }
    }

    private void resetPositions() {
        pacMan.setX(GRID_SIZE * 14);
        pacMan.setY(GRID_SIZE * 23);
        blinky.setX(GRID_SIZE * 13);
        blinky.setY(GRID_SIZE * 11);
        pinky.setX(GRID_SIZE * 14);
        pinky.setY(GRID_SIZE * 11);
        inky.setX(GRID_SIZE * 12);
        inky.setY(GRID_SIZE * 14);
        clyde.setX(GRID_SIZE * 15);
        clyde.setY(GRID_SIZE * 14);
    }

    private void checkPellets() {
        int pacManGridX = pacMan.getX() / GRID_SIZE;
        int pacManGridY = pacMan.getY() / GRID_SIZE;

        if (maze[pacManGridY][pacManGridX] == 0) { // Pellet
            score += 10;
            maze[pacManGridY][pacManGridX] = 2; // Mark as eaten
            munchSound.setFramePosition(0); // Rewind the sound
            munchSound.start();
        } else if (maze[pacManGridY][pacManGridX] == 3) { // Power Pellet
            score += 50;
            maze[pacManGridY][pacManGridX] = 2; // Mark as eaten
            vulnerable = true;
            vulnerableStartTime = System.currentTimeMillis();
            powerUpSound.setFramePosition(0);
            powerUpSound.start();
        }

        if (isGameWon()) {
            gameWon = true;
            timer.stop();
        }
    }

    private boolean isGameWon() {
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (maze[i][j] == 0 || maze[i][j] == 3) {
                    return false;
                }
            }
        }
        return true;
    }

    public void keyPressed(KeyEvent e) {
        pacMan.keyPressed(e);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Smooth graphics

        drawMaze(g2d);
        pacMan.draw(g2d);

        if(vulnerable){
            blinky.drawVulnerable(g2d);
            pinky.drawVulnerable(g2d);
            inky.drawVulnerable(g2d);
            clyde.drawVulnerable(g2d);

        } else{
            blinky.draw(g2d);
            pinky.draw(g2d);
            inky.draw(g2d);
            clyde.draw(g2d);
        }


        drawScore(g2d);
        drawLives(g2d);

        if (gameOver) {
            drawGameOver(g2d);
        }

        if (gameWon) {
            drawGameWon(g2d);
        }
    }

    private void drawMaze(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (maze[i][j] == 1) { // Wall
                    g2d.fillRect(j * GRID_SIZE, i * GRID_SIZE, GRID_SIZE, GRID_SIZE);
                } else if (maze[i][j] == 0) { // Pellet
                    g2d.setColor(Color.YELLOW);
                    g2d.fillOval(j * GRID_SIZE + GRID_SIZE / 3, i * GRID_SIZE + GRID_SIZE / 3, GRID_SIZE / 3, GRID_SIZE / 3);
                    g2d.setColor(Color.BLUE);
                } else if (maze[i][j] == 3) { // Power Pellet
                    g2d.setColor(Color.YELLOW);
                    g2d.fillOval(j * GRID_SIZE + GRID_SIZE / 4, i * GRID_SIZE + GRID_SIZE / 4, GRID_SIZE / 2, GRID_SIZE / 2);
                    g2d.setColor(Color.BLUE);
                }
            }
        }
    }

    private void drawScore(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 10, 20);
    }

    private void drawLives(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        for (int i = 0; i < lives; i++) {
            g2d.fillOval(10 + i * 25, 30, 20, 20);
        }
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        String gameOverText = "Game Over!";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(gameOverText);
        g2d.drawString(gameOverText, (SCREEN_WIDTH - textWidth) / 2, SCREEN_HEIGHT / 2);

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        String restartText = "Press Space to Restart";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(restartText);
        g2d.drawString(restartText, (SCREEN_WIDTH - textWidth) / 2, SCREEN_HEIGHT / 2 + 40);
    }

    private void drawGameWon(Graphics2D g2d) {
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        String winText = "You Win!";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(winText);
        g2d.drawString(winText, (SCREEN_WIDTH - textWidth) / 2, SCREEN_HEIGHT / 2);
    }

    public javax.swing.Timer getTimer() {
        return timer;
    }
}

class PacMan {
    private int x, y, speed;
    private int currentDirection = 0; // 0: None, 1: Up, 2: Down, 3: Left, 4: Right
    private int nextDirection = 0;
    private int mouthAngle = 45;
    private boolean mouthOpen = true;
    private final int GRID_SIZE = 20;

    public PacMan(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, GRID_SIZE, GRID_SIZE);
    }
    public boolean intersects(Ghost ghost) {
        return getBounds().intersects(ghost.getBounds());
    }

    public void move(int[][] maze) {
        // Check if the next direction is valid before changing the current direction
        if (nextDirection != 0) {
            if (canMove(maze, nextDirection)) {
                currentDirection = nextDirection;
            }
        }

        if (canMove(maze, currentDirection)) {
            switch (currentDirection) {
                case 1: // Up
                    y -= speed;
                    break;
                case 2: // Down
                    y += speed;
                    break;
                case 3: // Left
                    x -= speed;
                    break;
                case 4: // Right
                    x += speed;
                    break;
            }

            // Keep Pac-Man within the screen bounds (wrapping)
            if (x < 0) {
                x = 560; // SCREEN_WIDTH - GRID_SIZE;
            } else if (x > 560) {
                x = 0;
            }

            // Animate mouth
            if (mouthOpen) {
                mouthAngle += 5;
                if (mouthAngle >= 90) {
                    mouthOpen = false;
                }
            } else {
                mouthAngle -= 5;
                if (mouthAngle <= 45) {
                    mouthOpen = true;
                }
            }
        }
    }

    private boolean canMove(int[][] maze, int direction) {
        int gridX = x / GRID_SIZE;
        int gridY = y / GRID_SIZE;

        switch (direction) {
            case 1: // Up
                if (gridY > 0 && maze[gridY - 1][gridX] != 1) return true;
                break;
            case 2: // Down
                if (gridY < maze.length - 1 && maze[gridY + 1][gridX] != 1) return true;
                break;
            case 3: // Left
                if (gridX > 0 && maze[gridY][gridX - 1] != 1) return true;
                break;
            case 4: // Right
                if (gridX < maze[0].length - 1 && maze[gridY][gridX + 1] != 1) return true;
                break;
        }
        return false;
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_UP:
                nextDirection = 1;
                break;
            case KeyEvent.VK_DOWN:
                nextDirection = 2;
                break;
            case KeyEvent.VK_LEFT:
                nextDirection = 3;
                break;
            case KeyEvent.VK_RIGHT:
                nextDirection = 4;
                break;
        }
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        int angle = mouthOpen ? mouthAngle : 90 - mouthAngle; // Adjust angle based on open/close state
        switch (currentDirection) {
            case 1: // Up
                g2d.fillArc(x, y, GRID_SIZE, GRID_SIZE, 90 + angle, 360 - 2 * angle);
                break;
            case 2: // Down
                g2d.fillArc(x, y, GRID_SIZE, GRID_SIZE, 270 + angle, 360 - 2 * angle);
                break;
            case 3: // Left
                g2d.fillArc(x, y, GRID_SIZE, GRID_SIZE, 180 + angle, 360 - 2 * angle);
                break;
            case 4: // Right
            default:
                g2d.fillArc(x, y, GRID_SIZE, GRID_SIZE, 0 + angle, 360 - 2 * angle);
                break;
        }
    }
}

class Ghost {
    private int x, y, speed;
    private Color color;
    private String name;
    private int currentDirection = 0;
    private final int GRID_SIZE = 20;
    private Random random = new Random();
    private int originalX, originalY;
    public Ghost(int x, int y, int speed, Color color, String name) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.color = color;
        this.name = name;
        this.originalX = x;
        this.originalY = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, GRID_SIZE, GRID_SIZE);
    }
    public void resetPosition() {
        this.x = originalX;
        this.y = originalY;
        this.currentDirection = 0; // Reset direction as well
    }
    public void move(int[][] maze, int pacManX, int pacManY) {
        if(name.equals("Blinky")){
            moveBlinky(maze, pacManX, pacManY);
        } else if(name.equals("Pinky")){
            movePinky(maze, pacManX, pacManY);
        } else if(name.equals("Inky")){
            moveInky(maze, pacManX, pacManY, 14, 23); //Estimate Pacmans position to the middle for simplicity
        }else {
            moveClyde(maze, pacManX, pacManY);
        }
    }
    private void moveBlinky(int[][] maze, int pacManX, int pacManY) {
        // Blinky chases Pac-Man directly
        int dx = pacManX - x;
        int dy = pacManY - y;

        if (Math.abs(dx) > Math.abs(dy)) {
            // Move horizontally
            if (dx > 0 && canMove(maze, 4)) { // Right
                currentDirection = 4;
            } else if (dx < 0 && canMove(maze, 3)) { // Left
                currentDirection = 3;
            } else if (dy > 0 && canMove(maze, 2)) { // Down
                currentDirection = 2;
            } else if (dy < 0 && canMove(maze, 1)) { // Up
                currentDirection = 1;
            }
        } else {
            // Move vertically
            if (dy > 0 && canMove(maze, 2)) { // Down
                currentDirection = 2;
            } else if (dy < 0 && canMove(maze, 1)) { // Up
                currentDirection = 1;
            } else if (dx > 0 && canMove(maze, 4)) { // Right
                currentDirection = 4;
            } else if (dx < 0 && canMove(maze, 3)) { // Left
                currentDirection = 3;
            }
        }

        moveInDirection(maze);
    }
    private void movePinky(int[][] maze, int pacManX, int pacManY) {
        // Pinky tries to move ahead of Pac-Man by 4 tiles
        int dx = pacManX - x;
        int dy = pacManY - y;

        if (Math.abs(dx) > Math.abs(dy)) {
            // Move horizontally
            if (dx > 0 && canMove(maze, 4)) { // Right
                currentDirection = 4;
            } else if (dx < 0 && canMove(maze, 3)) { // Left
                currentDirection = 3;
            } else if (dy > 0 && canMove(maze, 2)) { // Down
                currentDirection = 2;
            } else if (dy < 0 && canMove(maze, 1)) { // Up
                currentDirection = 1;
            }
        } else {
            // Move vertically
            if (dy > 0 && canMove(maze, 2)) { // Down
                currentDirection = 2;
            } else if (dy < 0 && canMove(maze, 1)) { // Up
                currentDirection = 1;
            } else if (dx > 0 && canMove(maze, 4)) { // Right
                currentDirection = 4;
            } else if (dx < 0 && canMove(maze, 3)) { // Left
                currentDirection = 3;
            }
        }

        moveInDirection(maze);
    }
    private void moveInky(int[][] maze, int pacManX, int pacManY, int blinkyX, int blinkyY) {
    // Inky's target is calculated using a combination of Blinky's and Pac-Man's positions.
    // First, determine the vector from Blinky to Pac-Man. Then, double this vector and add it to Blinky's position to get Inky's target.
        int dx = pacManX - x;
        int dy = pacManY - y;

        if (Math.abs(dx) > Math.abs(dy)) {
            // Move horizontally
            if (dx > 0 && canMove(maze, 4)) { // Right
                currentDirection = 4;
            } else if (dx < 0 && canMove(maze, 3)) { // Left
                currentDirection = 3;
            } else if (dy > 0 && canMove(maze, 2)) { // Down
                currentDirection = 2;
            } else if (dy < 0 && canMove(maze, 1)) { // Up
                currentDirection = 1;
            }
        } else {
            // Move vertically
            if (dy > 0 && canMove(maze, 2)) { // Down
                currentDirection = 2;
            } else if (dy < 0 && canMove(maze, 1)) { // Up
                currentDirection = 1;
            } else if (dx > 0 && canMove(maze, 4)) { // Right
                currentDirection = 4;
            } else if (dx < 0 && canMove(maze, 3)) { // Left
                currentDirection = 3;
            }
        }

        moveInDirection(maze);
    }
    private void moveClyde(int[][] maze, int pacManX, int pacManY) {
    //Clyde moves randomly unless he is close to pac man
        int dx = pacManX - x;
        int dy = pacManY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if(distance < 80){
            // Move away from Pac-Man
            if (Math.abs(dx) > Math.abs(dy)) {
                // Move horizontally
                if (dx < 0 && canMove(maze, 4)) { // Right
                    currentDirection = 4;
                } else if (dx > 0 && canMove(maze, 3)) { // Left
                    currentDirection = 3;
                } else if (dy < 0 && canMove(maze, 2)) { // Down
                    currentDirection = 2;
                } else if (dy > 0 && canMove(maze, 1)) { // Up
                    currentDirection = 1;
                }
            } else {
                // Move vertically
                if (dy < 0 && canMove(maze, 2)) { // Down
                    currentDirection = 2;
                } else if (dy > 0 && canMove(maze, 1)) { // Up
                    currentDirection = 1;
                } else if (dx < 0 && canMove(maze, 4)) { // Right
                    currentDirection = 4;
                } else if (dx > 0 && canMove(maze, 3)) { // Left
                    currentDirection = 3;
                }
            }

        } else{
             // Move randomly
            if (random.nextInt(50) == 0) { // Change direction randomly
                int newDirection = random.nextInt(4) + 1;
                if (canMove(maze, newDirection)) {
                    currentDirection = newDirection;
                }
            }
        }
        moveInDirection(maze);
    }

    private void moveInDirection(int[][] maze) {
        if (canMove(maze, currentDirection)) {
            switch (currentDirection) {
                case 1: // Up
                    y -= speed;
                    break;
                case 2: // Down
                    y += speed;
                    break;
                case 3: // Left
                    x -= speed;
                    break;
                case 4: // Right
                    x += speed;
                    break;
            }

            // Keep ghost within the screen bounds (wrapping)
            if (x < 0) {
                x = 560; // SCREEN_WIDTH - GRID_SIZE;
            } else if (x > 560) {
                x = 0;
            }
        } else {
            // If can't move in the current direction, try a new direction
            changeDirectionRandomly(maze);
        }
    }
    private void changeDirectionRandomly(int[][] maze){
         int newDirection = random.nextInt(4) + 1;
         if(canMove(maze, newDirection)){
              currentDirection = newDirection;
         }
    }

    private boolean canMove(int[][] maze, int direction) {
        int gridX = x / GRID_SIZE;
        int gridY = y / GRID_SIZE;

        switch (direction) {
            case 1: // Up
                if (gridY > 0 && maze[gridY - 1][gridX] != 1) return true;
                break;
            case 2: // Down
                if (gridY < maze.length - 1 && maze[gridY + 1][gridX] != 1) return true;
                break;
            case 3: // Left
                if (gridX > 0 && maze[gridY][gridX - 1] != 1) return true;
                break;
            case 4: // Right
                if (gridX < maze[0].length - 1 && maze[gridY][gridX + 1] != 1) return true;
                break;
        }
        return false;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillRect(x, y, GRID_SIZE, GRID_SIZE);
    }
     public void drawVulnerable(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);
        g2d.fillRect(x, y, GRID_SIZE, GRID_SIZE);
    }

}

class MazeGenerator {
    public static int[][] generateMaze(int width, int height) {
        int[][] maze = new int[height][width];

        // Fill with walls
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                maze[i][j] = 1;
            }
        }

        // Set path and pellets
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                maze[i][j] = 0; // Path

            }
        }
                // Add power pellets in corners
        maze[1][1] = 3; // Top-left
        maze[1][width - 2] = 3; // Top-right
        maze[height - 2][1] = 3; // Bottom-left
        maze[height - 2][width - 2] = 3; // Bottom-right
        // Example: Creating a simple path structure - This is a basic implementation
        // and needs to be replaced with actual maze generation logic.
        return maze;
    }
}