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
    /*/----- END CHANGE -----/*/

    public DeathPanel(GamePanel gamePanel) {
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new GridLayout(3, 1, 10, 12));
        box.setPreferredSize(new Dimension(240, 150));

        /*/----- CHANGE: added "YOU DIED" title label -----/
         * PURPOSE: Previously the panel had no title — the player had no
         * visual confirmation of what happened. This label makes the death
         * state immediately clear and gives the screen a proper header.
         */
        JLabel titleLabel = new JLabel("YOU DIED", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        /*/----- END CHANGE -----/*/

        /*/----- CHANGE: "Respawn" button renamed to "Load Last Save", wired to loadSaveOrMenu() -----/
         * PURPOSE: The old button called gamePanel.respawnPlayer(), which put
         * the player back in the center of the viewport with full health but
         * no actual save data — making the save system pointless. The new
         * button calls gamePanel.loadSaveOrMenu(), which either restores the
         * last real save or falls back to the main menu if no save exists.
         * This gives death actual consequence and makes saving meaningful.
         */
        loadSaveBtn = new JButton("Load Last Save");
        JButton menuBtn = new JButton("Main Menu");

        loadSaveBtn.setFont(new Font("Arial", Font.BOLD, 16));
        menuBtn.setFont(new Font("Arial", Font.BOLD, 16));

        loadSaveBtn.addActionListener(e -> gamePanel.loadSaveOrMenu());
        menuBtn.addActionListener(e -> gamePanel.returnToMenu());
        /*/----- END CHANGE -----/*/

        /*/----- CHANGE: added noSaveLabel -----/
         * PURPOSE: When the player dies before ever saving, the "Load Last
         * Save" button is disabled (see refresh() below). Without any
         * explanation the player would just see a greyed-out button and not
         * know why. This label appears in its place to communicate that no
         * save file was found, so the player understands they must go to the
         * main menu instead.
         */
        noSaveLabel = new JLabel("No save file found.", SwingConstants.CENTER);
        noSaveLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noSaveLabel.setForeground(new Color(255, 180, 180));
        noSaveLabel.setVisible(false); // hidden by default; refresh() toggles it
        /*/----- END CHANGE -----/*/

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

    /*/----- CHANGE: added refresh() method -----/
     * PURPOSE: The save file may or may not exist at the moment the player
     * dies. Checking at construction time would be wrong because the panel
     * is built once at game start, not on every death. refresh() is called
     * by GamePanel right before setVisible(true) so the button and label
     * always reflect the actual current state of the save slot.
     *
     * USAGE: GamePanel.updateGame() calls deathPanel.refresh() inside the
     * SwingUtilities.invokeLater block that shows the panel.
     */
    public void refresh() {
        boolean hasSave = SaveManager.hasSave(1);
        loadSaveBtn.setEnabled(hasSave);   // grey out button if no save
        noSaveLabel.setVisible(!hasSave);  // show warning label instead
    }
    /*/----- END CHANGE -----/*/

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(180, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}