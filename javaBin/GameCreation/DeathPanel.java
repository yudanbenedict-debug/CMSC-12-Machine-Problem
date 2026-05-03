package GameCreation;

import javax.swing.*;
import java.awt.*;

public class DeathPanel extends JPanel {

    public DeathPanel(GamePanel gamePanel) {
        // --------------------------------------------------
        // Red translucent overlay — you can still see the world behind it
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        // --------------------------------------------------
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new GridLayout(2, 1, 10, 16));
        box.setPreferredSize(new Dimension(220, 120));

        JButton respawnBtn = new JButton("Respawn");
        JButton menuBtn    = new JButton("Main Menu");

        respawnBtn.setFont(new Font("Arial", Font.BOLD, 16));
        menuBtn.setFont(new Font("Arial", Font.BOLD, 16));

        respawnBtn.addActionListener(e -> gamePanel.respawnPlayer());
        menuBtn.addActionListener(e -> gamePanel.returnToMenu());

        box.add(respawnBtn);
        box.add(menuBtn);

        add(box);
    }

    // ── Paint the red tint manually so the world behind stays visible ─────────
    @Override
    protected void paintComponent(Graphics g) {
        // --------------------------------------------------
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(180, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        // --------------------------------------------------
        super.paintComponent(g);
    }
}