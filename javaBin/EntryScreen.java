import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class EntryScreen extends JFrame {

    private final EntryScreenPanel backgroundPanel;

    public EntryScreen(String oceanpng) {
        setTitle("Island Escapers - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);

        backgroundPanel = new EntryScreenPanel("Ocean.png");
        setContentPane(backgroundPanel);

        backgroundPanel.setLayout(new GridBagLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false); 
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        //The Title
        JLabel titleLabel = new JLabel("Island Escaper");
        titleLabel.setFont(new Font("Hellvetica", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(Box.createVerticalStrut(80));
        menuPanel.add(titleLabel);

        //The Play Button
        JButton playButton = new JButton("Play");
        playButton.setFont(new Font("Arial", Font.BOLD, 20));
        playButton.setPreferredSize(new Dimension(200, 60));
        playButton.setMaximumSize(new Dimension(200, 60));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(e -> startGame());
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(playButton);
        menuPanel.add(Box.createVerticalStrut(80));

        backgroundPanel.add(menuPanel);
    }

    private void startGame() {
        backgroundPanel.stopScrolling();  
        dispose();
        // TODO: Implement the overall game here...
        JOptionPane.showMessageDialog(null, "Game is starting...");
        // new GameFrame();
    }

    // This is for looping the image
    private static class EntryScreenPanel extends JPanel {
        private Image backgroundImage;
        private int offset = 0;          
        private Timer scrollTimer;
        private final double scrollSpeed = 1.2;      

        public EntryScreenPanel(String imagePath) {
            loadImage("Ocean.png");
            startScrolling();
        }

        private void loadImage(String path) {
            try {
                backgroundImage = new ImageIcon(path).getImage();
                if (backgroundImage == null) {
                    System.err.println("Could not load image: " + path);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void startScrolling() {
            scrollTimer = new Timer(16, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    offset -= scrollSpeed; 
                    if (backgroundImage != null && offset <= -backgroundImage.getWidth(null)) {
                        offset += backgroundImage.getWidth(null);
                    }
                    repaint();
                }
            });
            scrollTimer.start();
        }

        public void stopScrolling() {
            if (scrollTimer != null) {
                scrollTimer.stop();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage == null) return;

            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imgWidth = backgroundImage.getWidth(null);
           // int imgHeight = backgroundImage.getHeight(null);

            g.drawImage(backgroundImage, offset, 0, imgWidth, panelHeight, this);

            // If the image doesn't cover the entire panel, draw a second copy to the right
            if (offset + imgWidth < panelWidth) {
                g.drawImage(backgroundImage, offset + imgWidth, 0, imgWidth, panelHeight, this);
            }

            // If the offset is negative, also draw a copy on the left side (for smooth loop)
            if (offset > 0) {
                g.drawImage(backgroundImage, offset - imgWidth, 0, imgWidth, panelHeight, this);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EntryScreen("ocean.png").setVisible(true);
        });
    }
}
