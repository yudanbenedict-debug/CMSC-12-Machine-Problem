package GameCreation;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import Entities.EnemyFolder.EnemiesType;
import GamePlatform.Platform;
import GamePlatform.PlatformType;
import Entities.Item;

public class LevelData {

    // ── Enemy spawn record ────────────────────────────────────────────────────
    public static class EnemySpawn {
        public final EnemiesType type;
        public final float       x, y;

        public EnemySpawn(EnemiesType type, float x, float y) {
            this.type = type;
            this.x    = x;
            this.y    = y;
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private final ArrayList<Platform>   platforms;
    private final ArrayList<EnemySpawn> enemies;
    private final ArrayList<Item>       items;

    private final int       minScore;
    private final String    nextLevelFile; // null = final level
    private final Rectangle exitZone;

    /*/----- CHANGE: added backgroundImage field -----/
     * PURPOSE: Each level now declares which background image to use via the
     * "background" key in its .properties file (e.g. background=Jungle.png).
     * The value is the filename inside Resources/Backgrounds/. null means no
     * image was specified and GamePanel falls back to its default colour.
     */
    private final String backgroundImage; // filename in Resources/Backgrounds/, or null
    /*/----- END CHANGE -----/*/

    // Backwards-compatible constructor — no background image
    public LevelData(ArrayList<Platform>   platforms,
                     ArrayList<EnemySpawn> enemies,
                     ArrayList<Item>       items,
                     int                   minScore,
                     String                nextLevelFile,
                     Rectangle             exitZone) {
        this(platforms, enemies, items, minScore, nextLevelFile, exitZone, null);
    }

    /*/----- CHANGE: full constructor now accepts backgroundImage -----/
     * PURPOSE: LevelDataLoader passes the parsed "background" property here.
     * The overload above delegates with null so createStarterLevel() and any
     * other existing callers compile without changes.
     */
    public LevelData(ArrayList<Platform>   platforms,
                     ArrayList<EnemySpawn> enemies,
                     ArrayList<Item>       items,
                     int                   minScore,
                     String                nextLevelFile,
                     Rectangle             exitZone,
                     String                backgroundImage) {
        this.platforms       = platforms;
        this.enemies         = enemies;
        this.items           = items;
        this.minScore        = minScore;
        this.nextLevelFile   = nextLevelFile;
        this.exitZone        = exitZone;
        this.backgroundImage = backgroundImage;
    }
    /*/----- END CHANGE -----/*/

    // ── Starter level factory ─────────────────────────────────────────────────
    public static LevelData createStarterLevel(int worldWidth, int worldHeight) {
        int floorTop = worldHeight - 60;

        ArrayList<Platform>   platforms = new ArrayList<>();
        ArrayList<EnemySpawn> enemies   = new ArrayList<>();
        ArrayList<Item>       items     = new ArrayList<>();

        // ── Floor ─────────────────────────────────────────────────────────────
        platforms.add(new Platform(worldWidth, 60, 0, floorTop, PlatformType.METAL)
                .setCollisionBox(0, 0, 0, 0));

        // ── Floating platforms ────────────────────────────────────────────────
        platforms.add(new Platform(220, 20,  260,  floorTop - 180, PlatformType.GRASS).setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(220, 32,  640,  floorTop - 280, PlatformType.GRASS).setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(220, 32,  980,  floorTop - 220, PlatformType.GRASS).setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(200, 32, 1400,  floorTop - 160, PlatformType.GRASS).setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(200, 32, 1800,  floorTop - 300, PlatformType.GRASS).setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(220, 32, 2200,  floorTop - 200, PlatformType.GRASS).setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(240, 32, 2600,  floorTop - 260, PlatformType.SAND) .setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(180, 32, 3100,  floorTop - 180, PlatformType.DIRT) .setCollisionBox(0, 0, 0, 0));

        // ── Enemy spawns ──────────────────────────────────────────────────────
        enemies.add(new EnemySpawn(EnemiesType.GUN_ENEMY,    800,  floorTop - 36));
        enemies.add(new EnemySpawn(EnemiesType.KICK_ENEMY,  1400,  floorTop - 64));
        enemies.add(new EnemySpawn(EnemiesType.GUN_ENEMY,   2000,  floorTop - 36));
        enemies.add(new EnemySpawn(EnemiesType.PUNCH_ENEMY, 2800,  floorTop - 80));
        enemies.add(new EnemySpawn(EnemiesType.SWORD_ENEMY, 3400,  floorTop - 72));

        // ── Coins ─────────────────────────────────────────────────────────────
        items.add(new Item(Item.Type.COIN, 320,  worldHeight - 220, 20, 20));
        items.add(new Item(Item.Type.COIN, 1040, worldHeight - 260, 20, 20));
        items.add(new Item(Item.Type.COIN, 2100, worldHeight - 220, 20, 20));
        items.add(new Item(Item.Type.COIN, 3200, worldHeight - 220, 20, 20));

        /*/----- CHANGE: added exit zone and level gate for starter level -----/
         * PURPOSE: The exit is a tall green zone at the far right edge of the
         * map. minScore of 10 means the player needs at least 3 kills + 1 coin
         * or any combination reaching 10 before the gate opens. nextLevelFile
         * points to level2.properties which must exist in Resources/Data/Level/.
         */
        Rectangle exitZone = new Rectangle(worldWidth - 60, 0, 60, worldHeight);
        return new LevelData(platforms, enemies, items, 10, "level2.properties", exitZone, "Jungle.png");
        /*/----- END CHANGE -----/*/
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public List<Platform>   getPlatforms()       { return platforms;       }
    public List<EnemySpawn> getEnemies()         { return enemies;         }
    public List<Item>       getItems()           { return items;           }
    public int              getMinScore()        { return minScore;        }
    public String           getNextLevel()       { return nextLevelFile;   }
    public Rectangle        getExitZone()        { return exitZone;        }
    /*/----- CHANGE: added getBackgroundImage() getter -----/
     * PURPOSE: GamePanel calls this to find out which image to draw behind the level.
     */
    public String           getBackgroundImage() { return backgroundImage; }
    /*/----- END CHANGE -----/*/
}
