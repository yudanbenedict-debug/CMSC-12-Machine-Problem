package GameCreation;
import Exceptions.InvalidLevelDataException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Game extends JFrame {
    private static final int    VIEWPORT_WIDTH  = 1280;
    private static final int    VIEWPORT_HEIGHT = 720;
    private static final String WINDOW_TITLE    = "Island Escaper";

    public Game(boolean loadSave) {
        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        try {
            GamePanel panel = new GamePanel(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, () -> {
                SwingUtilities.invokeLater(() -> new EntryScreen().setVisible(true));
            });

            // X button routes back to main menu, not hard exit
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    panel.returnToMenu();
                }
            });

            add(panel);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            if (loadSave) panel.loadFromSave();
            panel.start();
        } catch (InvalidLevelDataException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Invalid level setup", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new Game(false));
    }

    public static void launchWithSave() {
        SwingUtilities.invokeLater(() -> new Game(true));
    }
}