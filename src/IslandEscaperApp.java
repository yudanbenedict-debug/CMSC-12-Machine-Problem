import GameCreation.EntryScreen;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

public class IslandEscaperApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Button.gradient",          null);
        UIManager.put("Button.select",            new Color(60, 60, 60));
        UIManager.put("Button.background",        new Color(45, 45, 45));
        UIManager.put("Button.foreground",        Color.WHITE);
        UIManager.put("Button.border",            javax.swing.BorderFactory.createLineBorder(new Color(100, 100, 100), 1));
        UIManager.put("Button.font",              new Font("Arial", Font.BOLD, 22));
        UIManager.put("Button.focus",             new Color(0, 0, 0, 0));
        UIManager.put("Button.margin",            new Insets(8, 20, 8, 20));

        SwingUtilities.invokeLater(() -> new EntryScreen().setVisible(true));
    }
}
