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
    private final String    nextLevelFile;
    private final Rectangle exitZone;
    private final int       worldWidth;
    private final int       worldHeight;
    private final float     spawnX;
    private final float     spawnY;

    private final String backgroundImage;


    public LevelData(ArrayList<Platform>   platforms,
                     ArrayList<EnemySpawn> enemies,
                     ArrayList<Item>       items,
                     int                   minScore,
                     String                nextLevelFile,
                     Rectangle             exitZone) {
        this(platforms, enemies, items, minScore, nextLevelFile, exitZone, null);
    }

    public LevelData(ArrayList<Platform>   platforms,
                     ArrayList<EnemySpawn> enemies,
                     ArrayList<Item>       items,
                     int                   minScore,
                     String                nextLevelFile,
                     Rectangle             exitZone,
                     String                backgroundImage) {
        this(platforms, enemies, items, minScore, nextLevelFile, exitZone, backgroundImage, 4000, 720);
    }

    public LevelData(ArrayList<Platform>   platforms,
                     ArrayList<EnemySpawn> enemies,
                     ArrayList<Item>       items,
                     int                   minScore,
                     String                nextLevelFile,
                     Rectangle             exitZone,
                     String                backgroundImage,
                     int                   worldWidth,
                     int                   worldHeight) {
        this(platforms, enemies, items, minScore, nextLevelFile, exitZone, backgroundImage, worldWidth, worldHeight, 100f, worldHeight - 120f);
    }

    public LevelData(ArrayList<Platform>   platforms,
                     ArrayList<EnemySpawn> enemies,
                     ArrayList<Item>       items,
                     int                   minScore,
                     String                nextLevelFile,
                     Rectangle             exitZone,
                     String                backgroundImage,
                     int                   worldWidth,
                     int                   worldHeight,
                     float                 spawnX,
                     float                 spawnY) {
        this.platforms       = platforms;
        this.enemies         = enemies;
        this.items           = items;
        this.minScore        = minScore;
        this.nextLevelFile   = nextLevelFile;
        this.exitZone        = exitZone;
        this.backgroundImage = backgroundImage;
        this.worldWidth      = worldWidth;
        this.worldHeight     = worldHeight;
        this.spawnX          = spawnX;
        this.spawnY          = spawnY;
    }

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

        //point where player can progress to the next level
        Rectangle exitZone = new Rectangle(worldWidth - 60, 0, 60, worldHeight);
        //property goes to level2 for now.
        return new LevelData(platforms, enemies, items, 10, "level2.properties", exitZone, "Jungle.png");
      
    }


    public List<Platform>   getPlatforms()       { return platforms;       }
    public List<EnemySpawn> getEnemies()         { return enemies;         }
    public List<Item>       getItems()           { return items;           }
    public int              getMinScore()        { return minScore;        }
    public String           getNextLevel()       { return nextLevelFile;   }
    public Rectangle        getExitZone()        { return exitZone;        }
    public int              getWorldWidth()      { return worldWidth;      }
    public int              getWorldHeight()     { return worldHeight;     }
    public float            getSpawnX()          { return spawnX;          }
    public float            getSpawnY()          { return spawnY;          }
    public String           getBackgroundImage() { return backgroundImage; }

}
