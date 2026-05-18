package GameCreation;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

public class WinPanel extends JPanel {

    public WinPanel(GamePanel gamePanel) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (getParent() != null) {
                    setBounds(0, 0, getParent().getWidth(), getParent().getHeight());
                }
            }
        });

        JButton menuBtn = new JButton("Return to Menu");
        menuBtn.setFont(new Font("Arial", Font.BOLD, 20));
        menuBtn.setForeground(Color.WHITE);
        menuBtn.setBackground(new Color(40, 120, 60));
        menuBtn.setFocusPainted(false);
        menuBtn.setBorderPainted(false);
        menuBtn.setOpaque(true);
        menuBtn.addActionListener(e -> gamePanel.returnToMenu());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(220, 0, 20, 0);
        add(new JPanel() {{ setOpaque(false); }}, gbc); // spacer to push button down

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(menuBtn, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //dark overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());

        //YOU WIN title
        g2.setFont(new Font("Arial", Font.BOLD, 72));
        g2.setColor(new Color(255, 220, 50));
        String title = "YOU WIN!";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(title)) / 2;
        g2.drawString(title, tx, getHeight() / 2 - 60);

        //subtitle
        g2.setFont(new Font("Arial", Font.PLAIN, 28));
        g2.setColor(new Color(200, 200, 200));
        String sub = "You escaped the island!";
        fm = g2.getFontMetrics();
        int sx = (getWidth() - fm.stringWidth(sub)) / 2;
        g2.drawString(sub, sx, getHeight() / 2);

        g2.dispose();
        super.paintComponent(g);
    }
}
