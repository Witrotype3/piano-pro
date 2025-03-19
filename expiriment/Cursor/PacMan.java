 import java.awt.*;
import java.awt.geom.Arc2D;

public class PacMan {
    private int x, y;
    private Direction direction;
    private double mouthAngle;
    private boolean mouthClosing;
    private static final double MOUTH_SPEED = 0.2;
    private static final double MAX_MOUTH_ANGLE = 45.0;
    private static final double MIN_MOUTH_ANGLE = 0.0;
    
    public PacMan(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = Direction.RIGHT;
        this.mouthAngle = MAX_MOUTH_ANGLE;
        this.mouthClosing = true;
    }
    
    public void update() {
        // Update mouth animation
        if (mouthClosing) {
            mouthAngle -= MOUTH_SPEED;
            if (mouthAngle <= MIN_MOUTH_ANGLE) {
                mouthAngle = MIN_MOUTH_ANGLE;
                mouthClosing = false;
            }
        } else {
            mouthAngle += MOUTH_SPEED;
            if (mouthAngle >= MAX_MOUTH_ANGLE) {
                mouthAngle = MAX_MOUTH_ANGLE;
                mouthClosing = true;
            }
        }
        
        // Move Pac-Man based on direction
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
        g2d.setColor(Color.YELLOW);
        
        // Calculate the starting angle based on direction
        double startAngle = getStartAngle();
        
        // Draw Pac-Man as a filled arc
        Arc2D.Double pacman = new Arc2D.Double(
            x * cellSize + 2,
            y * cellSize + 2,
            cellSize - 4,
            cellSize - 4,
            startAngle,
            360 - mouthAngle * 2,
            Arc2D.PIE
        );
        
        g2d.fill(pacman);
    }
    
    private double getStartAngle() {
        switch (direction) {
            case UP:
                return 90;
            case DOWN:
                return 270;
            case LEFT:
                return 180;
            case RIGHT:
                return 0;
            default:
                return 0;
        }
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void reset() {
        x = 14; // Center of the maze
        y = 15;
        direction = Direction.RIGHT;
    }
    
    public boolean collidesWith(Ghost ghost) {
        return x == ghost.getX() && y == ghost.getY();
    }
}