package GameCreation;

import DataLoader.SaveManager;

import javax.swing.*;
import java.awt.*;

public class PausePanel extends JPanel {

    public PausePanel(GamePanel gamePanel) {
        setOpaque(true);
        setBackground(new Color(0, 0, 0, 180));
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
            JOptionPane.showMessageDialog(gamePanel,
                ok ? "Game saved!" : "Save failed.",
                ok ? "Saved" : "Error",
                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        });

        exitBtn.addActionListener(e -> gamePanel.returnToMenu());

        box.add(resumeBtn);
        box.add(saveBtn);
        box.add(exitBtn);

        add(box);
    }
}