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
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class ModalDialog extends JPanel {

    public interface Callback {
        void onChoice(int buttonIndex);
    }

    private final JPanel parent;

    public ModalDialog(JPanel parent, String title, String message,
                       String[] buttons, Callback callback) {
        this.parent = parent;

        setOpaque(false);
        setLayout(new GridBagLayout());

        addMouseListener(new java.awt.event.MouseAdapter() {});
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});

        blockKey(KeyEvent.VK_ESCAPE);
        blockKey(KeyEvent.VK_A);
        blockKey(KeyEvent.VK_D);
        blockKey(KeyEvent.VK_W);
        blockKey(KeyEvent.VK_SPACE);
        blockKey(KeyEvent.VK_LEFT);
        blockKey(KeyEvent.VK_RIGHT);
        blockKey(KeyEvent.VK_UP);
        blockKey(KeyEvent.VK_P);
        blockKey(KeyEvent.VK_R);
        blockKey(KeyEvent.VK_1);
        blockKey(KeyEvent.VK_2);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 20, 20, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        int cardH = 180 + (message != null && !message.isEmpty() ? 36 : 0);
        card.setPreferredSize(new Dimension(420, cardH));
        card.setMaximumSize(new Dimension(420, cardH));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Helvetica", Font.BOLD, 24));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(28));
        card.add(titleLbl);

        if (message != null && !message.isEmpty()) {
            JLabel msgLbl = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
            msgLbl.setFont(new Font("Arial", Font.PLAIN, 15));
            msgLbl.setForeground(new Color(210, 210, 210));
            msgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(Box.createVerticalStrut(14));
            card.add(msgLbl);
        }

        card.add(Box.createVerticalStrut(24));

        JPanel btnRow = new JPanel();
        btnRow.setOpaque(false);
        btnRow.setLayout(new BoxLayout(btnRow, BoxLayout.X_AXIS));
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRow.add(Box.createHorizontalGlue());

        for (int i = 0; i < buttons.length; i++) {
            final int idx = i;
            JButton btn = new JButton(buttons[i]);
            btn.setFont(new Font("Arial", Font.BOLD, 15));
            btn.setPreferredSize(new Dimension(140, 42));
            btn.setMaximumSize(new Dimension(140, 42));
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                hideDialog();
                callback.onChoice(idx);
            });
            btnRow.add(btn);
            if (i < buttons.length - 1) btnRow.add(Box.createHorizontalStrut(12));
        }

        btnRow.add(Box.createHorizontalGlue());
        card.add(btnRow);
        card.add(Box.createVerticalStrut(24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        add(card, gbc);
    }

    private void blockKey(int keyCode) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(keyCode, 0, false), "blocked");
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(keyCode, 0, true), "blocked");
        getActionMap().put("blocked", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { /* consume */ }
        });
    }

        public void showDialog() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(parent);
        if (frame == null) return;
        JLayeredPane layeredPane = frame.getLayeredPane();
        setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
        layeredPane.add(this, JLayeredPane.POPUP_LAYER);
        setVisible(true);
        revalidate();
        repaint();
    }

        public void hideDialog() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(parent);
        setVisible(false);
        if (frame != null) {
            frame.getLayeredPane().remove(this);
            frame.getLayeredPane().repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
