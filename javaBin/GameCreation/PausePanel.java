package GameCreation;

import javax.swing.*;
import java.awt.*;

public class PausePanel extends JPanel {

    public PausePanel(GamePanel gamePanel) {
        setOpaque(true);
        setBackground(new Color(0, 0, 0, 180));
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setLayout(new GridLayout(3, 1, 10, 10));
        box.setPreferredSize(new Dimension(200, 120));

        JButton resumeBtn = new JButton("Continue");
        JButton exitBtn   = new JButton("Main Menu");

        resumeBtn.addActionListener(e -> gamePanel.resumeGame());
        exitBtn.addActionListener(e -> gamePanel.returnToMenu());

        box.add(resumeBtn);
        box.add(exitBtn);

        add(box);
    }
}