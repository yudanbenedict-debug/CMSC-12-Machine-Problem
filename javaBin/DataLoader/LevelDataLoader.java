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

/*/----- CHANGE: replaced java.awt.Rectangle import with Item -----/
 * PURPOSE: Items are no longer stored as raw Rectangle objects — they are
 * now Item instances with a Type. The Rectangle import is no longer needed
 * here because Item.bounds handles the rectangle internally.
 */
import Entities.Item;
/*/----- END CHANGE -----/*/

/**
 * Loads level layout from a .properties file at runtime.
 * Replaces LevelData.createStarterLevel() — level layout is no longer hardcoded.
 *
 * Expected path: Resources/Data/Level/level1.properties
 *
 * Format:
 *   worldWidth=4000
 *   worldHeight=720
 *   platform.count=N
 *   platform.0=width,height,x,y,TYPE
 *   enemy.count=N
 *   enemy.0=TYPE,x,y
 *   item.count=N
 *   item.0=x,y,width,height,TYPE   (TYPE optional — defaults to COIN)
 */
public class LevelDataLoader {

    private static final String BASE_PATH = "Resources/Data/Level/";

    // ── Public loader ─────────────────────────────────────────────────────────

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
        ArrayList<Platform>             platforms = new ArrayList<>();
        ArrayList<LevelData.EnemySpawn> enemies   = new ArrayList<>();

        /*/----- CHANGE: items list changed from ArrayList<Rectangle> to ArrayList<Item> -----/
         * PURPOSE: LevelData now holds List<Item> instead of List<Rectangle>,
         * so this parser must build Item objects instead of Rectangle objects.
         * Each item entry in the .properties file can optionally include a
         * TYPE field (e.g. "320,500,20,20,COIN"). If the TYPE field is absent
         * it defaults to COIN so existing level files don't need to be updated.
         */
        ArrayList<Item> items = new ArrayList<>();
        /*/----- END CHANGE -----/*/

        // ── Platforms ─────────────────────────────────────────────────────────
        int platCount = Integer.parseInt(p.getProperty("platform.count", "0"));
        for (int i = 0; i < platCount; i++) {
            String[]     parts = p.getProperty("platform." + i).split(",");
            int          pw    = Integer.parseInt(parts[0].trim());
            int          ph    = Integer.parseInt(parts[1].trim());
            int          px    = Integer.parseInt(parts[2].trim());
            int          py    = Integer.parseInt(parts[3].trim());
            PlatformType type  = PlatformType.valueOf(parts[4].trim());
            platforms.add(new Platform(pw, ph, px, py, type).setCollisionBox(0, 0, 0, 0));
        }

        // ── Enemies ───────────────────────────────────────────────────────────
        int enemyCount = Integer.parseInt(p.getProperty("enemy.count", "0"));
        for (int i = 0; i < enemyCount; i++) {
            String[]    parts = p.getProperty("enemy." + i).split(",");
            EnemiesType type  = EnemiesType.valueOf(parts[0].trim());
            float       ex    = Float.parseFloat(parts[1].trim());
            float       ey    = Float.parseFloat(parts[2].trim());
            enemies.add(new LevelData.EnemySpawn(type, ex, ey));
        }

        // ── Items ─────────────────────────────────────────────────────────────
        /*/----- CHANGE: item parsing now creates Item instead of Rectangle -----/
         * PURPOSE: Each item entry is parsed into an Item(type, x, y, w, h).
         * A 5th optional field in the .properties value specifies the type
         * (e.g. "320,500,20,20,HEALTH"). If absent, defaults to COIN so all
         * existing level files continue to work without modification.
         */
        int itemCount = Integer.parseInt(p.getProperty("item.count", "0"));
        for (int i = 0; i < itemCount; i++) {
            String[]  parts    = p.getProperty("item." + i).split(",");
            int       ix       = Integer.parseInt(parts[0].trim());
            int       iy       = Integer.parseInt(parts[1].trim());
            int       iw       = Integer.parseInt(parts[2].trim());
            int       ih       = Integer.parseInt(parts[3].trim());
            Item.Type itemType = parts.length > 4
                                 ? Item.Type.valueOf(parts[4].trim())
                                 : Item.Type.COIN; // default to COIN
            items.add(new Item(itemType, ix, iy, iw, ih));
        }
        /*/----- END CHANGE -----/*/

        /*/----- CHANGE: parse minScore, nextLevel, exitZone from properties -----/
         * PURPOSE: LevelData now requires these three fields. They are read
         * from the .properties file with safe defaults: minScore defaults to 0
         * (exit always open), nextLevel defaults to null (final level),
         * exitZone defaults to a 60px strip at the far right of the world.
         * This means old level files without these keys still load cleanly.
         */
        int       minScore     = Integer.parseInt(p.getProperty("minScore",    "0"));
        String    rawNext    = p.getProperty("nextLevel", null);
        String    nextLevel  = (rawNext == null || rawNext.trim().equals("null")) ? null : rawNext.trim();
        int       exitX        = Integer.parseInt(p.getProperty("exitZone.x",  String.valueOf(worldWidth - 60)));
        int       exitY        = Integer.parseInt(p.getProperty("exitZone.y",  "0"));
        int       exitW        = Integer.parseInt(p.getProperty("exitZone.w",  "60"));
        int       exitH        = Integer.parseInt(p.getProperty("exitZone.h",  String.valueOf(worldHeight)));
        Rectangle exitZone     = new Rectangle(exitX, exitY, exitW, exitH);
        /*/----- END CHANGE -----/*/

        /*/----- CHANGE: parse background image filename -----/
         * PURPOSE: "background=Jungle.png" in a .properties file tells
         * GamePanel which image from Resources/Backgrounds/ to draw behind
         * the level. Missing or blank means no image — GamePanel falls back
         * to its solid colour. BackgroundLoader handles caching.
         */
        String rawBg          = p.getProperty("background", null);
        String backgroundImage = (rawBg == null || rawBg.isBlank()) ? null : rawBg.trim();
        /*/----- END CHANGE -----/*/

        return new LevelData(platforms, enemies, items, minScore, nextLevel, exitZone, backgroundImage);
    }

    private LevelDataLoader() {}
}