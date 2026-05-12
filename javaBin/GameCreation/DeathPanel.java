package GameCreation;

/*/----- CHANGE: added SaveManager import -----/
 * PURPOSE: DeathPanel now needs to check at runtime whether a save file
 * exists (via SaveManager.hasSave) so it can enable/disable the load button
 * and show a warning label. Without this import the refresh() method below
 * would not compile.
 */
import DataLoader.SaveManager;
/*/----- END CHANGE -----/*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DeathPanel extends JPanel {

    /*/----- CHANGE: replaced anonymous button with named fields -----/
     * PURPOSE: loadSaveBtn and noSaveLabel are stored as instance fields
     * instead of local variables so that refresh() — called every time the
     * panel is shown — can update them after construction. A local variable
     * would be unreachable outside the constructor.
     */

    private final JButton loadSaveBtn;
    private final JLabel  noSaveLabel;

    public DeathPanel(GamePanel gamePanel) {
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new GridLayout(3, 1, 10, 12));
        box.setPreferredSize(new Dimension(240, 150));

        
        JLabel titleLabel = new JLabel("YOU DIED", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
       
        loadSaveBtn = new JButton("Load Last Save");
        JButton menuBtn = new JButton("Main Menu");

        loadSaveBtn.setFont(new Font("Arial", Font.BOLD, 16));
        menuBtn.setFont(new Font("Arial", Font.BOLD, 16));

        loadSaveBtn.addActionListener(e -> gamePanel.loadSaveOrMenu());
        menuBtn.addActionListener(e -> gamePanel.returnToMenu());
   
        noSaveLabel = new JLabel("No save file found.", SwingConstants.CENTER);
        noSaveLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noSaveLabel.setForeground(new Color(255, 180, 180));
        noSaveLabel.setVisible(false); // hidden by default; refresh() toggles it

        box.add(loadSaveBtn);
        box.add(noSaveLabel);
        box.add(menuBtn);

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BorderLayout(0, 14));
        column.add(titleLabel, BorderLayout.NORTH);
        column.add(box, BorderLayout.CENTER);

        add(column);
    }
 
    public void refresh() {
        boolean hasSave = SaveManager.hasSave(1);
        loadSaveBtn.setEnabled(hasSave);   // grey out button if no save
        noSaveLabel.setVisible(!hasSave);  // show warning label instead
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        //red color. bit transparent
        g2.setColor(new Color(180, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}