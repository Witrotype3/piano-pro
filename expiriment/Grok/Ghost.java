import java.awt.*;
import java.util.Random;

public class Ghost {
    private int x, y, startX, startY;
    private final Color color;
    private final String name;
    private final PacMan pacMan;
    private final Maze maze;
    private boolean vulnerable;
    private final Random random = new Random();
    private static final int SIZE = 20;

    public Ghost(int x, int y, Color color, String name, PacMan pacMan, Maze maze) {
        this.x = startX = x;
        this.y = startY = y;
        this.color = color;
        this.name = name;
        this.pacMan = pacMan;
        this.maze = maze;
        vulnerable = false;
    }

    public void move() {
        int targetX = pacMan.getX();
        int targetY = pacMan.getY();
        int dx = 0, dy = 0;

        switch (name) {
            case "Blinky": // Chase Pac-Man directly
                dx = Integer.compare(targetX, x);
                dy = Integer.compare(targetY, y);
                break;
            case "Pinky": // Move ahead of Pac-Man
                dx = Integer.compare(targetX + 4, x); // 4 tiles ahead
                dy = Integer.compare(targetY + 4, y);
                break;
            case "Inky": // Mix of Blinky and Pinky
                dx = Integer.compare(targetX + 2, x);
                dy = Integer.compare(targetY, y);
                break;
            case "Clyde": // Random or retreat
                if (Math.abs(targetX - x) + Math.abs(targetY - y) < 8) {
                    dx = -Integer.compare(targetX, x);
                    dy = -Integer.compare(targetY, y);
                } else {
                    dx = random.nextInt(3) - 1;
                    dy = random.nextInt(3) - 1;
                }
                break;
        }

        if (!maze.isWall(x + dx, y + dy)) {
            x += dx;
            y += dy;
        }
    }

    public void reset() {
        x = startX;
        y = startY;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(vulnerable ? Color.BLUE : color);
        g2d.fillOval(x * SIZE, y * SIZE, SIZE, SIZE);
    }

    public boolean collidesWith(PacMan pacMan) {
        return x == pacMan.getX() && y == pacMan.getY();
    }

    public boolean isVulnerable() { return vulnerable; }
    public void setVulnerable(boolean vulnerable) { this.vulnerable = vulnerable; }
}