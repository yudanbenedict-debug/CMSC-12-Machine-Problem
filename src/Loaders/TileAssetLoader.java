package Loaders;

import GamePlatform.PlatformType;
import GamePlatform.Platform;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class TileAssetLoader {
    public static final int TILE_SIZE = 32;

    private static final Map<PlatformType, String> TILE_PATHS = new EnumMap<>(PlatformType.class);
    private static final Map<PlatformType, Color> FALLBACKS = new EnumMap<>(PlatformType.class);
    private static final Map<PlatformType, BufferedImage> cache = new EnumMap<>(PlatformType.class);
    private static boolean initialized = false;

    static{
    TILE_PATHS.put(PlatformType.GRASS, "assets/Block-Assets/GrassBlock_Assets.png");
    TILE_PATHS.put(PlatformType.DIRT,  "assets/Block-Assets/DirtBlock_Assets.png");
    TILE_PATHS.put(PlatformType.METAL, "assets/Block-Assets/IronBlock_Assets.png");
    TILE_PATHS.put(PlatformType.SAND,  "assets/Block-Assets/SandBlock01_Assets.png");
    TILE_PATHS.put(PlatformType.WOOD,  "assets/Block-Assets/StoneBlock_Assets.png");

    FALLBACKS.put(PlatformType.GRASS, new Color(72,  130,  50));
    FALLBACKS.put(PlatformType.DIRT,  new Color(120,  80,  40));
    FALLBACKS.put(PlatformType.METAL, new Color(90,   90,  90));
    FALLBACKS.put(PlatformType.SAND,  new Color(219, 183, 115));
    FALLBACKS.put(PlatformType.WOOD,  new Color(130,  84,  45));
    }

    public static void init(){
        if(initialized) return;

        for(PlatformType type : PlatformType.values()){
            cache.put(type, loadOrFallback(type));
        }
        initialized = true;
        System.out.println("Loaded asset!!!!!");
    }


    public static BufferedImage getTile(PlatformType type) {
        if (!initialized) {
            System.err.println("[TileAssetLoader] getTile() called before init() — initializing now.");
            init();
        }
        BufferedImage tile = cache.get(type);
        return tile != null ? tile : makeFallback(FALLBACKS.getOrDefault(type, Color.MAGENTA));
    }

    private static BufferedImage loadOrFallback(PlatformType type) {
        String path = TILE_PATHS.get(type);
        if (path == null) {
            System.err.println("[TileAssetLoader] No path registered for " + type);
            return makeFallback(FALLBACKS.getOrDefault(type, Color.MAGENTA));
        }
    
        try {
            File file = new File(path);
            System.out.println("[TileAssetLoader] Trying: " + file.getAbsolutePath());
    
            if (!file.exists()) {
                System.err.println("[TileAssetLoader] File not found: " + file.getAbsolutePath());
                return makeFallback(FALLBACKS.getOrDefault(type, Color.MAGENTA));
            }
    
            BufferedImage raw = ImageIO.read(file);
            if (raw == null) {
                System.err.println("[TileAssetLoader] ImageIO returned null for: " + path);
                return makeFallback(FALLBACKS.getOrDefault(type, Color.MAGENTA));
            }
            return scaleTo(raw, TILE_SIZE, TILE_SIZE);
    
        } catch (IOException e) {
            System.err.println("[TileAssetLoader] IOException: " + e.getMessage());
            return makeFallback(FALLBACKS.getOrDefault(type, Color.MAGENTA));
        }
    }

    private static BufferedImage makeFallback(Color base) {
        BufferedImage img = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(base);
        g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        g.setColor(base.darker());
        g.drawRect(0, 0, TILE_SIZE - 1, TILE_SIZE - 1);
        g.dispose();
        return img;
    }

    private static BufferedImage scaleTo(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }
}