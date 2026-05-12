package GameCreation;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;

import Exceptions.ResourceLoadException;
import Loaders.MusicPlayer;
import DataLoader.SaveManager;



public class EntryScreen extends JFrame {
    private final ScrollingBackgroundPanel backgroundPanel;

    public EntryScreen() {
        setTitle("Island Escapers - Main Menu");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitGame();
            }
        });
        setSize(800, 600); // Matches with test java frame size
        setLocationRelativeTo(null);

        backgroundPanel = new ScrollingBackgroundPanel();
        setContentPane(backgroundPanel);
        backgroundPanel.setLayout(new GridBagLayout());
        playMusic();
        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Island Escaper");
        titleLabel.setFont(new Font("Helvetica", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(Box.createVerticalStrut(80));
        menuPanel.add(titleLabel);

        JButton playButton = new JButton("New Game");
        playButton.setFont(new Font("Arial", Font.BOLD, 20));
        playButton.setPreferredSize(new Dimension(200, 60));
        playButton.setMaximumSize(new Dimension(200, 60));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(e -> startGame(false));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(playButton);

        // Only show Continue if a save exists
        if (SaveManager.hasSave(1)) {
            JButton continueButton = new JButton("Continue");
            continueButton.setFont(new Font("Arial", Font.BOLD, 20));
            continueButton.setPreferredSize(new Dimension(200, 60));
            continueButton.setMaximumSize(new Dimension(200, 60));
            continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            continueButton.addActionListener(e -> startGame(true));
            menuPanel.add(Box.createVerticalStrut(10));
            menuPanel.add(continueButton);
        }

        menuPanel.add(Box.createVerticalStrut(80));

        JButton exitButton = new JButton("Exit Game");
        exitButton.setFont(new Font("Arial", Font.BOLD, 20));
        exitButton.setPreferredSize(new Dimension(200, 60));
        exitButton.setMaximumSize(new Dimension(200, 60));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> exitGame());
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(exitButton);

        backgroundPanel.add(menuPanel);
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
            dispose();
            System.exit(0);
        }
    }

    private void startGame(boolean loadSave) {
        backgroundPanel.stopScrolling();
        MusicPlayer.stop();
        dispose();
        if (loadSave) {
            Game.launchWithSave();
        } else {
            Game.launch();
        }
    }
    

    private void playMusic() {
        MusicPlayer.play("Resources/Sounds/Newer-Days.wav");
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
            File file = new File("Resources/Ocean.png");
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

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int imgWidth = ocean.getWidth();
        if (imgWidth <= 0) return;

        // Hrizontal Tilling this
        int startX = offset % imgWidth;
        if (startX > 0) startX -= imgWidth;

        for (int x = startX; x < panelWidth; x += imgWidth) {
            g.drawImage(ocean, x, 0, imgWidth, panelHeight, null);
        }
     }
    }


}