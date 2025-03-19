import java.awt.*;
import java.awt.event.KeyEvent;

public class PacMan {
    private int x, y, dx, dy;
    private final int startX, startY;
    private final Maze maze;
    private boolean mouthOpen;
    private static final int SIZE = 20;

    public PacMan(int x, int y, Maze maze) {
        this.x = startX = x;
        this.y = startY = y;
        this.maze = maze;
        dx = dy = 0;
        mouthOpen = true;
    }

    public void setDirection(int keyCode) {
        int newDx = dx, newDy = dy;
        switch (keyCode) {
            case KeyEvent.VK_UP: newDx = 0; newDy = -1; break;
            case KeyEvent.VK_DOWN: newDx = 0; newDy = 1; break;
            case KeyEvent.VK_LEFT: newDx = -1; newDy = 0; break;
            case KeyEvent.VK_RIGHT: newDx = 1; newDy = 0; break;
        }
        if (!maze.isWall(x + newDx, y + newDy)) {
            dx = newDx;
            dy = newDy;
        }
    }

    public void move() {
        if (!maze.isWall(x + dx, y + dy)) {
            x += dx;
            y += dy;
        }
        mouthOpen = !mouthOpen; // Toggle mouth animation
    }

    public void reset() {
        x = startX;
        y = startY;
        dx = dy = 0;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        int startAngle = (dx == 1) ? 45 : (dx == -1) ? 225 : (dy == -1) ? 315 : 135;
        int arcAngle = mouthOpen ? 270 : 360;
        g2d.fillArc(x * SIZE, y * SIZE, SIZE, SIZE, startAngle, arcAngle);
    }

    public int getX() { return x; }
    public int getY() { return y; }
}