package GameCreation;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import Exceptions.InvalidLevelDataException;

public class Game extends JFrame {
    private static final int    VIEWPORT_WIDTH  = 1280;
    private static final int    VIEWPORT_HEIGHT = 720;
    private static final String WINDOW_TITLE    = "Island Escaper - Engine Test";

    public Game() {
        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        try {
            GamePanel panel = new GamePanel(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
            add(panel);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            panel.start();
        } catch (InvalidLevelDataException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Invalid level setup", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }


    public static void launch() {
        SwingUtilities.invokeLater(Game::new);
    }

    public static void main(String[] args) {
        launch();
    }
}