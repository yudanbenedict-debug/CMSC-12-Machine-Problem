package Loaders;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Loads and caches background images from Resources/Backgrounds/.
 *
 * Usage:
 *   BufferedImage bg = BackgroundLoader.get("Jungle.png");
 *
 * Returns null if the file is missing or unreadable — callers should
 * fall back to a solid color in that case.
 */
public class BackgroundLoader {

    private static final String BASE_PATH = "Resources/Backgrounds/";

    // Cache so each image is read from disk only once per session
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    public static BufferedImage get(String filename) {
        if (filename == null || filename.isBlank()) return null;

        // Return cached entry (may be null if we already tried and failed)
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
