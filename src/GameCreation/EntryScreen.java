package GameCreation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.BoxLayout;

import Exceptions.ResourceLoadException;
import Loaders.MusicPlayer;
import DataLoader.SaveManager;

public class EntryScreen extends JFrame {
    private final ScrollingBackgroundPanel backgroundPanel;
    private CreditsPanel creditsOverlay;
    private HelpPanel helpOverlay;

    public EntryScreen() {
        setTitle("Island Escapers - Main Menu");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitGame();
            }
        });
        setUndecorated(true);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        backgroundPanel = new ScrollingBackgroundPanel();
        playMusic();

        //center button stack
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Island Escaper");
        titleLabel.setFont(new Font("Helvetica", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(Box.createVerticalStrut(80));
        inner.add(titleLabel);

        JButton playButton = new JButton("New Game");
        playButton.setFont(new Font("Arial", Font.BOLD, 20));
        playButton.setPreferredSize(new Dimension(200, 60));
        playButton.setMaximumSize(new Dimension(200, 60));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(e -> startGame(false));
        inner.add(Box.createVerticalStrut(40));
        inner.add(playButton);

        if (SaveManager.hasSave(1)) {
            JButton continueButton = new JButton("Continue");
            continueButton.setFont(new Font("Arial", Font.BOLD, 20));
            continueButton.setPreferredSize(new Dimension(200, 60));
            continueButton.setMaximumSize(new Dimension(200, 60));
            continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            continueButton.addActionListener(e -> startGame(true));
            inner.add(Box.createVerticalStrut(10));
            inner.add(continueButton);
        }

        inner.add(Box.createVerticalStrut(10));

        JButton exitButton = new JButton("Exit Game");
        exitButton.setFont(new Font("Arial", Font.BOLD, 20));
        exitButton.setPreferredSize(new Dimension(200, 60));
        exitButton.setMaximumSize(new Dimension(200, 60));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> exitGame());
        inner.add(Box.createVerticalStrut(10));
        inner.add(exitButton);

        backgroundPanel.setLayout(new java.awt.GridBagLayout());
        backgroundPanel.add(inner);

        //overlays
        creditsOverlay = new CreditsPanel(() -> creditsOverlay.setVisible(false));
        creditsOverlay.setVisible(false);

        helpOverlay = new HelpPanel(() -> helpOverlay.setVisible(false));
        helpOverlay.setVisible(false);

        JLayeredPane layered = getLayeredPane();
        creditsOverlay.setBounds(0, 0, 1, 1);
        helpOverlay.setBounds(0, 0, 1, 1);
        layered.add(creditsOverlay, JLayeredPane.POPUP_LAYER);
        layered.add(helpOverlay, JLayeredPane.POPUP_LAYER);

        //corner buttons — sized and placed on resize
        JButton helpBtn    = makeCornerButton("Help");
        JButton creditsBtn = makeCornerButton("Credits");
        helpBtn.addActionListener(e -> showHelp());
        creditsBtn.addActionListener(e -> showCredits());
        layered.add(helpBtn,    JLayeredPane.PALETTE_LAYER);
        layered.add(creditsBtn, JLayeredPane.PALETTE_LAYER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = getWidth();
                int h = getHeight();
                creditsOverlay.setBounds(0, 0, w, h);
                helpOverlay.setBounds(0, 0, w, h);

                int btnW = 120;
                int btnH = 40;
                int margin = 20;
                helpBtn.setBounds(margin, h - btnH - margin, btnW, btnH);
                creditsBtn.setBounds(w - btnW - margin, h - btnH - margin, btnW, btnH);
            }
        });

        setContentPane(backgroundPanel);
    }

    private JButton makeCornerButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(40, 40, 40, 200));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        return btn;
    }

    private void showCredits() {
        creditsOverlay.setBounds(0, 0, getWidth(), getHeight());
        creditsOverlay.setVisible(true);
        creditsOverlay.repaint();
    }

    private void showHelp() {
        helpOverlay.setBounds(0, 0, getWidth(), getHeight());
        helpOverlay.setVisible(true);
        helpOverlay.repaint();
    }

    private void exitGame() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Exit Game",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            MusicPlayer.stop();
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            gd.setFullScreenWindow(null);
            dispose();
            System.exit(0);
        }
    }

    private void startGame(boolean loadSave) {
        backgroundPanel.stopScrolling();
        MusicPlayer.stop();
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(null);
        dispose();
        if (loadSave) {
            Game.launchWithSave();
        } else {
            Game.launch();
        }
    }

    private void playMusic() {
        MusicPlayer.play("assets/Sounds/Newer-Days.wav");
    }

    private static class ScrollingBackgroundPanel extends JPanel {
        private BufferedImage ocean;
        private int offset = 0;
        private Timer scrollTimer;
        private int scrollSpeed = 1;

        public ScrollingBackgroundPanel() {
            try {
                loadImage();
            } catch (ResourceLoadException ignored) {
                ocean = null;
            }
            startScrolling();
        }

        private void loadImage() {
            try {
                File file = new File("assets/Ocean.png");
                if (!file.exists()) {
                    throw new ResourceLoadException("Ocean.png");
                }
                ocean = ImageIO.read(file);
            } catch (ResourceLoadException e) {
                throw e;
            } catch (Exception e) {
                throw new ResourceLoadException("Ocean.png", e);
            }
        }

        private void startScrolling() {
            if (ocean == null) return;
            scrollTimer = new Timer(16, e -> {
                offset -= scrollSpeed;
                repaint();
            });
            scrollTimer.start();
        }

        public void stopScrolling() {
            if (scrollTimer != null) scrollTimer.stop();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (ocean == null) {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.RED);
                g.drawString("Ocean.png not loaded", 20, 50);
                return;
            }

            int panelWidth  = getWidth();
            int panelHeight = getHeight();
            int imgWidth    = ocean.getWidth();
            if (imgWidth <= 0) return;

            int startX = offset % imgWidth;
            if (startX > 0) startX -= imgWidth;

            for (int x = startX; x < panelWidth; x += imgWidth) {
                g.drawImage(ocean, x, 0, imgWidth, panelHeight, null);
            }
        }
    }
}
