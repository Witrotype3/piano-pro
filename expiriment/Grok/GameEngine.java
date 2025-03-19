import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class GameEngine extends JPanel {
    private final Maze maze;
    private final PacMan pacMan;
    private final Ghost[] ghosts;
    private int score;
    private int lives;
    private boolean gameOver;
    private boolean win;

    public GameEngine() {
        maze = new Maze();
        pacMan = new PacMan(14, 23, maze); // Starting position
        ghosts = new Ghost[] {
            new Ghost(14, 11, Color.RED, "Blinky", pacMan, maze),
            new Ghost(14, 12, Color.PINK, "Pinky", pacMan, maze),
            new Ghost(14, 13, Color.CYAN, "Inky", pacMan, maze),
            new Ghost(14, 14, Color.ORANGE, "Clyde", pacMan, maze)
        };
        score = 0;
        lives = 3;
        gameOver = false;
        win = false;

        setFocusable(true);
        startGameLoop();
    }

    private void startGameLoop() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateGame();
                repaint();
            }
        }, 0, 100); // 10 FPS
    }

    private void updateGame() {
        if (gameOver || win) return;

        pacMan.move();
        for (Ghost ghost : ghosts) {
            ghost.move();
            if (ghost.collidesWith(pacMan) && !ghost.isVulnerable()) {
                lives--;
                if (lives <= 0) gameOver = true;
                resetPositions();
            } else if (ghost.collidesWith(pacMan) && ghost.isVulnerable()) {
                score += 200;
                ghost.reset();
            }
        }

        if (maze.eatPellet(pacMan.getX(), pacMan.getY())) {
            score += 10;
            if (maze.allPelletsEaten()) win = true;
        }
        if (maze.eatPowerPellet(pacMan.getX(), pacMan.getY())) {
            score += 50;
            for (Ghost ghost : ghosts) ghost.setVulnerable(true);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    for (Ghost ghost : ghosts) ghost.setVulnerable(false);
                }
            }, 5000); // Vulnerable for 5 seconds
        }
    }

    private void resetPositions() {
        pacMan.reset();
        for (Ghost ghost : ghosts) ghost.reset();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        maze.draw(g2d);
        pacMan.draw(g2d);
        for (Ghost ghost : ghosts) ghost.draw(g2d);

        g2d.setColor(Color.WHITE);
        g2d.drawString("Score: " + score, 10, 20);
        g2d.drawString("Lives: " + lives, 10, 40);

        if (gameOver) {
            g2d.drawString("Game Over", 350, 300);
        } else if (win) {
            g2d.drawString("You Win!", 350, 300);
        }
    }

    public void handleKeyPress(int keyCode) {
        pacMan.setDirection(keyCode);
    }
}