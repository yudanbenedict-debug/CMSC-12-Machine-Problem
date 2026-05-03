package DataLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Handles writing and reading PlayerSaveData to/from .dat files.
 *
 * Save path: javaBin/saves/save{slot}.dat
 * e.g.       javaBin/saves/save1.dat
 */
public class SaveManager {

    private static final String SAVE_DIR = "Resources/Data/Saves/";

    // ── Save ──────────────────────────────────────────────────────────────────

    /**
     * Writes the save data to disk.
     *
     * @param data the player state to save
     * @param slot save slot number (1, 2, 3...)
     * @return true if saved successfully
     */
    public static boolean save(PlayerSaveData data, int slot) {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();  // create saves/ folder if missing

        File file = saveFile(slot);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
            System.out.println("[SaveManager] Saved to " + file.getPath());
            return true;
        } catch (IOException e) {
            System.err.println("[SaveManager] Failed to save: " + e.getMessage());
            return false;
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Reads save data from disk.
     *
     * @param slot save slot number
     * @return the loaded PlayerSaveData, or null if no save exists or load failed
     */
    public static PlayerSaveData load(int slot) {
        File file = saveFile(slot);

        if (!file.exists()) {
            System.out.println("[SaveManager] No save found at slot " + slot);
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            PlayerSaveData data = (PlayerSaveData) ois.readObject();
            System.out.println("[SaveManager] Loaded " + data);
            return data;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[SaveManager] Failed to load slot " + slot + ": " + e.getMessage());
            return null;
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * Deletes a save slot.
     *
     * @param slot save slot number
     * @return true if deleted successfully
     */
    public static boolean deleteSave(int slot) {
        File file = saveFile(slot);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) System.out.println("[SaveManager] Deleted slot " + slot);
            return deleted;
        }
        return false;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /** Returns true if a save file exists for the given slot. */
    public static boolean hasSave(int slot) {
        return saveFile(slot).exists();
    }

    private static File saveFile(int slot) {
        return new File(SAVE_DIR + "save" + slot + ".dat");
    }

    private SaveManager() {}
}