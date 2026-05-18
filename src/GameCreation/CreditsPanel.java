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

public class CreditsPanel extends JPanel {

    public CreditsPanel(Runnable onClose) {
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
        card.setPreferredSize(new Dimension(480, 420));
        card.setMaximumSize(new Dimension(480, 420));

        JLabel title = new JLabel("Credits");
        title.setFont(new Font("Helvetica", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(30));
        card.add(title);
        card.add(Box.createVerticalStrut(30));

        card.add(sectionLabel("Music"));
        card.add(Box.createVerticalStrut(6));
        card.add(nameLabel("Lorenzo Badinas"));
        card.add(Box.createVerticalStrut(24));

        card.add(sectionLabel("Developers"));
        card.add(Box.createVerticalStrut(6));
        card.add(nameLabel("Dan Benedict Yu"));
        card.add(Box.createVerticalStrut(4));
        card.add(nameLabel("Chester Gabriel Dapuran"));
        card.add(Box.createVerticalStrut(4));
        card.add(nameLabel("Junewel Codillo"));
        card.add(Box.createVerticalStrut(30));

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

    private JLabel nameLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 20));
        lbl.setForeground(Color.WHITE);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
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
