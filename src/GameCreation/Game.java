package GameCreation;

import Exceptions.InvalidLevelDataException;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class Game extends JFrame {
    private static final int    VIEWPORT_WIDTH;
    private static final int    VIEWPORT_HEIGHT;

    static {
        java.awt.DisplayMode dm = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDisplayMode();
        VIEWPORT_WIDTH  = dm.getWidth();
        VIEWPORT_HEIGHT = dm.getHeight();
    }
    private static final String WINDOW_TITLE    = "Island Escaper";

    public Game(boolean loadSave) {
        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        LoadingPanel loadingPanel = new LoadingPanel();
        setContentPane(loadingPanel);
        setVisible(true);
        loadingPanel.repaint();

        SwingWorker<GamePanel, Void> worker = new SwingWorker<>() {
            @Override
            protected GamePanel doInBackground() throws Exception {
                return new GamePanel(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, () ->
                    SwingUtilities.invokeLater(() -> {
                        GraphicsDevice gd2 = GraphicsEnvironment
                            .getLocalGraphicsEnvironment().getDefaultScreenDevice();
                        gd2.setFullScreenWindow(null);
                        dispose();
                        EntryScreen es = new EntryScreen();
                        es.setVisible(true);
                        SwingUtilities.invokeLater(() -> {
                            es.toFront();
                            es.requestFocus();
                        });
                    })
                );
            }

            @Override
            protected void done() {
                try {
                    GamePanel panel = get();

                    addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            panel.confirmAndReturnToMenu();
                        }
                    });

                    setContentPane(panel);
                    revalidate();
                    panel.requestFocusInWindow();

                    if (loadSave) panel.loadFromSave();
                    panel.start();

                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof InvalidLevelDataException) {
                        JOptionPane.showMessageDialog(Game.this, cause.getMessage(),
                            "Invalid level setup", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(Game.this,
                            "Failed to load game: " + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    GraphicsDevice gd2 = GraphicsEnvironment
                        .getLocalGraphicsEnvironment().getDefaultScreenDevice();
                    gd2.setFullScreenWindow(null);
                    dispose();
                }
            }
        };
        worker.execute();
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new Game(false));
    }

    public static void launchWithSave() {
        SwingUtilities.invokeLater(() -> new Game(true));
    }

    private static class LoadingPanel extends JPanel {
        private static final int BAR_W = 400;
        private static final int BAR_H = 20;

        private int pulse = 0;
        private final javax.swing.Timer pulseTimer;

        LoadingPanel() {
            setBackground(Color.BLACK);
            pulseTimer = new javax.swing.Timer(40, e -> {
                pulse = (pulse + 1) % 60;
                repaint();
            });
            pulseTimer.start();
        }

        public void stopPulse() { pulseTimer.stop(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();

            g.setFont(new Font("Helvetica", Font.BOLD, 42));
            g.setColor(Color.WHITE);
            String title = "Island Escaper";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(title, (w - fm.stringWidth(title)) / 2, h / 2 - 60);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            fm = g.getFontMetrics();
            String loading = "Loading...";
            g.setColor(new Color(180, 180, 180));
            g.drawString(loading, (w - fm.stringWidth(loading)) / 2, h / 2 + 10);

            int barX = (w - BAR_W) / 2;
            int barY = h / 2 + 30;
            g.setColor(new Color(60, 60, 60));
            g.fillRoundRect(barX, barY, BAR_W, BAR_H, BAR_H, BAR_H);

            double progress = (Math.sin(pulse * Math.PI / 30.0) + 1.0) / 2.0;
            int fill = (int) (BAR_W * 0.2 + BAR_W * 0.8 * progress);
            g.setColor(new Color(80, 180, 255));
            g.fillRoundRect(barX, barY, fill, BAR_H, BAR_H, BAR_H);
        }
    }
}