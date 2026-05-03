package DataLoader;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import Entities.EnemyFolder.EnemiesType;
import GameCreation.LevelData;
import GamePlatform.Platform;
import GamePlatform.PlatformType;

/**
 * Loads level layout from a .properties file at runtime.
 * Replaces LevelData.createStarterLevel() — level layout is no longer hardcoded.
 *
 * Expected path: Resources/data/levels/level1.properties
 *
 * Format:
 *   worldWidth=4000
 *   worldHeight=720
 *   platform.count=N
 *   platform.0=width,height,x,y,TYPE
 *   enemy.count=N
 *   enemy.0=TYPE,x,y
 *   item.count=N
 *   item.0=x,y,width,height
 */
public class LevelDataLoader {

    private static final String BASE_PATH = "Resources/Data/Level/";

    // ── Public loader ─────────────────────────────────────────────────────────

    /**
     * Loads the given level file and returns a LevelData.
     * Falls back to LevelData.createStarterLevel() if the file is missing.
     *
     * @param levelFile e.g. "level1.properties"
     * @param worldWidth  used for fallback only
     * @param worldHeight used for fallback only
     */
    public static LevelData load(String levelFile, int worldWidth, int worldHeight) {
        File file = new File(BASE_PATH + levelFile);

        if (!file.exists()) {
            System.err.println("[LevelDataLoader] Missing " + file.getPath() + " — using hardcoded fallback");
            return LevelData.createStarterLevel(worldWidth, worldHeight);
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            System.out.println("[LevelDataLoader] Loaded " + levelFile);
        } catch (IOException e) {
            System.err.println("[LevelDataLoader] Failed to read " + levelFile + " — using hardcoded fallback");
            return LevelData.createStarterLevel(worldWidth, worldHeight);
        }

        return parse(props, worldWidth, worldHeight);
    }

    // ── Parser ────────────────────────────────────────────────────────────────

    private static LevelData parse(Properties p, int worldWidth, int worldHeight) {
        ArrayList<Platform>          platforms = new ArrayList<>();
        ArrayList<LevelData.EnemySpawn> enemies = new ArrayList<>();
        ArrayList<Rectangle>         items     = new ArrayList<>();

        int floorTop = worldHeight - 60;

        // ── Platforms ─────────────────────────────────────────────────────────
        int platCount = Integer.parseInt(p.getProperty("platform.count", "0"));
        for (int i = 0; i < platCount; i++) {
            String   raw   = p.getProperty("platform." + i);
            String[] parts = raw.split(",");
            int pw   = Integer.parseInt(parts[0].trim());
            int ph   = Integer.parseInt(parts[1].trim());
            int px   = Integer.parseInt(parts[2].trim());
            int py   = Integer.parseInt(parts[3].trim());
            PlatformType type = PlatformType.valueOf(parts[4].trim());
            platforms.add(new Platform(pw, ph, px, py, type).setCollisionBox(0, 0, 0, 0));
        }

        // ── Enemies ───────────────────────────────────────────────────────────
        int enemyCount = Integer.parseInt(p.getProperty("enemy.count", "0"));
        for (int i = 0; i < enemyCount; i++) {
            String   raw   = p.getProperty("enemy." + i);
            String[] parts = raw.split(",");
            EnemiesType type = EnemiesType.valueOf(parts[0].trim());
            float ex = Float.parseFloat(parts[1].trim());
            float ey = Float.parseFloat(parts[2].trim());
            enemies.add(new LevelData.EnemySpawn(type, ex, ey));
        }

        // ── Items ─────────────────────────────────────────────────────────────
        int itemCount = Integer.parseInt(p.getProperty("item.count", "0"));
        for (int i = 0; i < itemCount; i++) {
            String   raw   = p.getProperty("item." + i);
            String[] parts = raw.split(",");
            int ix = Integer.parseInt(parts[0].trim());
            int iy = Integer.parseInt(parts[1].trim());
            int iw = Integer.parseInt(parts[2].trim());
            int ih = Integer.parseInt(parts[3].trim());
            items.add(new Rectangle(ix, iy, iw, ih));
        }

        return new LevelData(platforms, enemies, items);
    }

    private LevelDataLoader() {}
}