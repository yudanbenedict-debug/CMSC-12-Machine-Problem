package GameCreation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class HelpPanel extends JPanel {

    public HelpPanel(Runnable onClose) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        addMouseListener(new java.awt.event.MouseAdapter() {});
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 15, 15, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(500, 520));
        card.setMaximumSize(new Dimension(500, 520));

        JLabel title = new JLabel("Help");
        title.setFont(new Font("Helvetica", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(30));
        card.add(title);
        card.add(Box.createVerticalStrut(24));

        card.add(sectionLabel("Controls"));
        card.add(Box.createVerticalStrut(12));
        card.add(tipLabel("A / D", "Move left and right"));
        card.add(Box.createVerticalStrut(6));
        card.add(tipLabel("W / Space", "Jump"));
        card.add(Box.createVerticalStrut(6));
        card.add(tipLabel("P", "Run"));
        card.add(Box.createVerticalStrut(6));
        card.add(tipLabel("Left Click", "Attack with current weapon"));
        card.add(Box.createVerticalStrut(18));

        card.add(sectionLabel("Weapons"));
        card.add(Box.createVerticalStrut(12));
        card.add(tipLabel("1", "Switch to Gun (default on spawn)"));
        card.add(Box.createVerticalStrut(6));
        card.add(tipLabel("2", "Switch to Sword"));
        card.add(Box.createVerticalStrut(18));

        card.add(sectionLabel("Tips"));
        card.add(Box.createVerticalStrut(12));
        card.add(tipLabel("Score", "Collect coins and kill enemies to reach the exit score"));
        card.add(Box.createVerticalStrut(6));
        card.add(tipLabel("Boss", "Kill the boss to win — the exit zone won't appear"));
        card.add(Box.createVerticalStrut(24));

        JButton closeBtn = new JButton("Back");
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.setPreferredSize(new Dimension(160, 50));
        closeBtn.setMaximumSize(new Dimension(160, 50));
        closeBtn.addActionListener(e -> onClose.run());
        card.add(closeBtn);
        card.add(Box.createVerticalStrut(24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        add(card, gbc);
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        lbl.setForeground(new Color(160, 160, 160));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    //key + description row
    private JPanel tipLabel(String key, String desc) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(440, 30));

        JLabel keyLbl = new JLabel("[" + key + "]");
        keyLbl.setFont(new Font("Arial", Font.BOLD, 16));
        keyLbl.setForeground(new Color(255, 220, 80));
        keyLbl.setPreferredSize(new Dimension(140, 24));
        keyLbl.setMaximumSize(new Dimension(140, 24));

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(new Font("Arial", Font.PLAIN, 16));
        descLbl.setForeground(Color.WHITE);

        row.add(Box.createHorizontalStrut(30));
        row.add(keyLbl);
        row.add(Box.createHorizontalStrut(10));
        row.add(descLbl);
        row.add(Box.createHorizontalGlue());

        return row;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}