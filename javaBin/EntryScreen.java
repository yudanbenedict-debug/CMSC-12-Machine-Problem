import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class EntryScreen extends JFrame {

    private final ScrollingBackgroundPanel backgroundPanel;

    public EntryScreen() {
        setTitle("Island Escapers - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Matches with test java frame size
        setLocationRelativeTo(null);

        backgroundPanel = new ScrollingBackgroundPanel();
        setContentPane(backgroundPanel);
        backgroundPanel.setLayout(new GridBagLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Island Escaper");
        titleLabel.setFont(new Font("Helvetica", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(Box.createVerticalStrut(80));
        menuPanel.add(titleLabel);

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
        JOptionPane.showMessageDialog(null, "Game is starting...");
        // TODO: Launch the actual game here yall...
    }

   
    private static class ScrollingBackgroundPanel extends JPanel {
        private BufferedImage ocean;
        private int offset = 0;
        private Timer scrollTimer;
        private int scrollSpeed = 2;

        public ScrollingBackgroundPanel() {
            loadImage();
            startScrolling();
        }

        
      private void loadImage() {
    try {
    
        URL url = getClass().getResource("/Ocean.png");
        if (url != null) {
            ocean = ImageIO.read(url);
            System.out.println("Loaded from classpath: " + url);
            return;
        }
        
        File file = new File("Resources/Ocean.png");
        if (file.exists()) {
            ocean = ImageIO.read(file);
            System.out.println("Loaded from " + file.getAbsolutePath());
            return;
        }
        
        file = new File("../Resources/Ocean.png");
        if (file.exists()) {
            ocean = ImageIO.read(file);
            System.out.println("Loaded from " + file.getAbsolutePath());
            return;
        }
        
        file = new File("Ocean.png");
        if (file.exists()) {
            ocean = ImageIO.read(file);
            System.out.println("Loaded from " + file.getAbsolutePath());
            return;
        }
        
        System.err.println("Ocean.png not found in any expected location");
    } catch (Exception e) {
        e.printStackTrace();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EntryScreen().setVisible(true));
    }
}
