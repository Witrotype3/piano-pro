import java.awt.*;

public class Maze {
    private static final int WIDTH = 28;
    private static final int HEIGHT = 31;
    private static final int TILE_SIZE = 20;
    private final boolean[][] walls;
    private final boolean[][] pellets;
    private final boolean[][] powerPellets;

    public Maze() {
        walls = new boolean[WIDTH][HEIGHT];
        pellets = new boolean[WIDTH][HEIGHT];
        powerPellets = new boolean[WIDTH][HEIGHT];
        initializeMaze();
    }

    private void initializeMaze() {
        // Simplified maze (walls around edges and some interior)
        for (int x = 0; x < WIDTH; x++) {
            walls[x][0] = walls[x][HEIGHT - 1] = true;
        }
        for (int y = 0; y < HEIGHT; y++) {
            walls[0][y] = walls[WIDTH - 1][y] = true;
        }
        // Add some internal walls
        walls[5][5] = walls[5][6] = walls[6][5] = true;

        // Place pellets and power pellets
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                if (!walls[x][y]) pellets[x][y] = true;
            }
        }
        powerPellets[1][1] = powerPellets[1][HEIGHT - 2] = true;
        powerPellets[WIDTH - 2][1] = powerPellets[WIDTH - 2][HEIGHT - 2] = true;
    }

    public boolean isWall(int x, int y) {
        return x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT || walls[x][y];
    }

    public boolean eatPellet(int x, int y) {
        if (pellets[x][y]) {
            pellets[x][y] = false;
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

    public boolean allPelletsEaten() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (pellets[x][y]) return false;
            }
        }
        return true;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (walls[x][y]) {
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else if (pellets[x][y]) {
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(x * TILE_SIZE + 8, y * TILE_SIZE + 8, 4, 4);
                } else if (powerPellets[x][y]) {
                    g2d.setColor(Color.YELLOW);
                    g2d.fillOval(x * TILE_SIZE + 4, y * TILE_SIZE + 4, 12, 12);
                }
            }
        }
    }
}