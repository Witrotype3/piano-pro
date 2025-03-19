import java.awt.*;
import java.util.Random;

public class Ghost {
    private String name;
    private Color color;
    private int x, y;
    private int startX, startY;
    private Direction direction;
    private boolean vulnerable;
    private long vulnerableStartTime;
    private static final long VULNERABLE_DURATION = 10000; // 10 seconds
    private Random random;
    
    public Ghost(String name, Color color, int startX, int startY) {
        this.name = name;
        this.color = color;
        this.startX = startX;
        this.startY = startY;
        this.x = startX;
        this.y = startY;
        this.direction = Direction.RIGHT;
        this.vulnerable = false;
        this.random = new Random();
    }
    
    public void update(int pacmanX, int pacmanY) {
        if (vulnerable && System.currentTimeMillis() - vulnerableStartTime > VULNERABLE_DURATION) {
            vulnerable = false;
        }
        
        // Simple movement logic - can be improved with pathfinding
        switch (name) {
            case "Blinky":
                chasePacman(pacmanX, pacmanY);
                break;
            case "Pinky":
                moveAheadOfPacman(pacmanX, pacmanY);
                break;
            case "Inky":
                if (random.nextBoolean()) {
                    chasePacman(pacmanX, pacmanY);
                } else {
                    moveAheadOfPacman(pacmanX, pacmanY);
                }
                break;
            case "Clyde":
                double distance = Math.sqrt(Math.pow(x - pacmanX, 2) + Math.pow(y - pacmanY, 2));
                if (distance < 8) {
                    moveAwayFromPacman(pacmanX, pacmanY);
                } else {
                    randomMove();
                }
                break;
        }
    }
    
    private void chasePacman(int pacmanX, int pacmanY) {
        if (Math.abs(x - pacmanX) > Math.abs(y - pacmanY)) {
            direction = x < pacmanX ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = y < pacmanY ? Direction.DOWN : Direction.UP;
        }
        move();
    }
    
    private void moveAheadOfPacman(int pacmanX, int pacmanY) {
        // Move 4 cells ahead of Pac-Man in its current direction
        int targetX = pacmanX;
        int targetY = pacmanY;
        
        switch (direction) {
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
        
        if (Math.abs(x - targetX) > Math.abs(y - targetY)) {
            direction = x < targetX ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = y < targetY ? Direction.DOWN : Direction.UP;
        }
        move();
    }
    
    private void moveAwayFromPacman(int pacmanX, int pacmanY) {
        if (Math.abs(x - pacmanX) > Math.abs(y - pacmanY)) {
            direction = x < pacmanX ? Direction.LEFT : Direction.RIGHT;
        } else {
            direction = y < pacmanY ? Direction.UP : Direction.DOWN;
        }
        move();
    }
    
    private void randomMove() {
        if (random.nextDouble() < 0.1) {
            direction = Direction.values()[random.nextInt(Direction.values().length)];
        }
        move();
    }
    
    private void move() {
        switch (direction) {
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
    
    public void draw(Graphics2D g2d, int cellSize) {
        if (vulnerable) {
            g2d.setColor(Color.BLUE);
        } else {
            g2d.setColor(color);
        }
        
        // Draw ghost body
        g2d.fillRoundRect(x * cellSize + 2, y * cellSize + 2, cellSize - 4, cellSize - 4, 10, 10);
        
        // Draw eyes
        g2d.setColor(Color.WHITE);
        int eyeSize = cellSize / 6;
        g2d.fillOval(x * cellSize + cellSize / 4, y * cellSize + cellSize / 3, eyeSize, eyeSize);
        g2d.fillOval(x * cellSize + cellSize * 2 / 3, y * cellSize + cellSize / 3, eyeSize, eyeSize);
    }
    
    public void makeVulnerable() {
        vulnerable = true;
        vulnerableStartTime = System.currentTimeMillis();
    }
    
    public boolean isVulnerable() {
        return vulnerable;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void reset() {
        x = startX;
        y = startY;
        direction = Direction.RIGHT;
        vulnerable = false;
    }
} 