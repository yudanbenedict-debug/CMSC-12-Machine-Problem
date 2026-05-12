package DataLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

//read/write .dat files for player/enemy save
public class SaveManager {

    private static final String SAVE_DIR = "Resources/Data/Saves/";
    //save slot
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

   //load slot
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

    //delete slot
    public static boolean deleteSave(int slot) {
        File file = saveFile(slot);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) System.out.println("[SaveManager] Deleted slot " + slot);
            return deleted;
        }
        return false;
    }

    //utility and debugging
    public static boolean hasSave(int slot) {
        return saveFile(slot).exists();
    }

    private static File saveFile(int slot) {
        return new File(SAVE_DIR + "save" + slot + ".dat");
    }

    private SaveManager() {}
}