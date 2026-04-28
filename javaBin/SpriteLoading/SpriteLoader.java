package SpriteLoading;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class SpriteLoader {

    public static BufferedImage loadWalkBaseFrame(int width, int height) {
        BufferedImage img;
        img = loadFrameOrNull("/Resources/00_character_walk.png");    if (img != null) return img;
        img = loadFrameFromFile("Resources/00_character_walk.png");   if (img != null) return img;
        img = loadFrameFromFile("../Resources/00_character_walk.png");if (img != null) return img;
        img = loadFrameFromFile("00_character_walk.png");             if (img != null) return img;
        return createFallbackFrame(width, height);
    }

    public static BufferedImage[] loadImages(String folderPath, String baseName, int frameCount, BufferedImage fallback) {
        BufferedImage[] frames = new BufferedImage[frameCount];

        for (int i = 0; i < frameCount; i++) {
            String fileName = String.format("%02d_%s.png", i, baseName);
            String fullPath = folderPath + "/" + fileName;

            BufferedImage f = loadFrameOrNull("/Resources/" + fullPath);
            if (f == null) f = loadFrameFromFile("Resources/" + fullPath);
            if (f == null) f = loadFrameFromFile("../Resources/" + fullPath);
            if (f == null) f = loadFrameFromFile(fullPath);

            frames[i] = (f != null) ? f : fallback;
        }

        return frames;
    }

    private static BufferedImage loadFrameOrNull(String resourcePath) {
        try {
            URL url = SpriteLoader.class.getResource(resourcePath);
            return (url != null) ? ImageIO.read(url) : null;
        } catch (IOException e) { return null; }
    }

    private static BufferedImage loadFrameFromFile(String path) {
        try {
            File f = new File(path);
            return f.exists() ? ImageIO.read(f) : null;
        } catch (IOException e) { return null; }
    }

    private static BufferedImage createFallbackFrame(int width, int height) {
        BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fallback.createGraphics();
        g2.setColor(new Color(45, 95, 250));
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.WHITE);
        g2.fillRect(6, 10, 6, 6);
        g2.fillRect(width - 12, 10, 6, 6);
        g2.dispose();
        return fallback;
    }
}
