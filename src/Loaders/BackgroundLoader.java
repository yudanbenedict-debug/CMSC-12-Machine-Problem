package Loaders;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class BackgroundLoader {

    private static final String BASE_PATH = "assets/Backgrounds/";

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    public static BufferedImage get(String filename) {
        if (filename == null || filename.isBlank()) return null;

        if (CACHE.containsKey(filename)) return CACHE.get(filename);

        File file = new File(BASE_PATH + filename);
        if (!file.exists()) {
            System.err.println("[BackgroundLoader] Missing: " + file.getPath());
            CACHE.put(filename, null);
            return null;
        }

        try {
            BufferedImage img = ImageIO.read(file);
            CACHE.put(filename, img);
            System.out.println("[BackgroundLoader] Loaded " + filename);
            return img;
        } catch (IOException e) {
            System.err.println("[BackgroundLoader] Failed to read " + filename + ": " + e.getMessage());
            CACHE.put(filename, null);
            return null;
        }
    }

    private BackgroundLoader() {}
}