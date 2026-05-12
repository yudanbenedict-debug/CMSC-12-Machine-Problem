import GameCreation.EntryScreen;
import javax.swing.SwingUtilities;

/**
 * Application entry point for Island Escaper.
 * Launches the main menu on the Swing event dispatch thread.
 */
public class IslandEscaperApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EntryScreen().setVisible(true));
    }
}
