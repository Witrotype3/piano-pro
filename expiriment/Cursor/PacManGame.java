import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class PacManGame extends JFrame {
    private GamePanel gamePanel;
    private Timer gameTimer;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int FPS = 60;

    public PacManGame() {
        setTitle("Pac-Man");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        gamePanel = new GamePanel();
        add(gamePanel);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                gamePanel.handleKeyPress(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        setFocusable(true);

        // Initialize game timer
        gameTimer = new Timer(1000 / FPS, e -> {
            gamePanel.update();
            gamePanel.repaint();
        });
        gameTimer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PacManGame().setVisible(true);
        });
    }
} 