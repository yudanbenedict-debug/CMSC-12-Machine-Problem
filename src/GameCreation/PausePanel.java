package GameCreation;

import DataLoader.SaveManager;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;

public class PausePanel extends JPanel {

    public PausePanel(GamePanel gamePanel) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setLayout(new GridLayout(3, 1, 10, 10));
        box.setPreferredSize(new Dimension(200, 160));

        JButton resumeBtn = new JButton("Continue");
        JButton saveBtn   = new JButton("Save Game");
        JButton exitBtn   = new JButton("Main Menu");

        resumeBtn.addActionListener(e -> gamePanel.resumeGame());

        saveBtn.addActionListener(e -> {
            boolean ok = gamePanel.saveGame();
            ModalDialog dlg = new ModalDialog(
                gamePanel,
                ok ? "Saved" : "Error",
                ok ? "Game saved!" : "Save failed.",
                new String[]{"OK"},
                choice -> {}
            );
            dlg.showDialog();
        });

        exitBtn.addActionListener(e -> gamePanel.confirmAndReturnToMenu());

        box.add(resumeBtn);
        box.add(saveBtn);
        box.add(exitBtn);

        add(box);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}