package GameCreation;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import Entities.EnemyFolder.EnemiesType;
import GamePlatform.Platform;
import GamePlatform.PlatformType;

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
    private final ArrayList<Rectangle>  items;

    public LevelData(ArrayList<Platform>   platforms,
                     ArrayList<EnemySpawn> enemies,
                     ArrayList<Rectangle>  items) {
        this.platforms = platforms;
        this.enemies   = enemies;
        this.items     = items;
    }

    // ── Starter level factory ─────────────────────────────────────────────────
    public static LevelData createStarterLevel(int worldWidth, int worldHeight) {
        int floorTop = worldHeight - 60;

        ArrayList<Platform>   platforms = new ArrayList<>();
        ArrayList<EnemySpawn> enemies   = new ArrayList<>();
        ArrayList<Rectangle>  items     = new ArrayList<>();

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
        enemies.add(new EnemySpawn(EnemiesType.PUNCH_ENEMY,     2800,  floorTop - 80));
        enemies.add(new EnemySpawn(EnemiesType.SWORD_ENEMY,3400,  floorTop - 72));

        // ── Items ─────────────────────────────────────────────────────────────
        items.add(new Rectangle(320,  worldHeight - 220, 20, 20));
        items.add(new Rectangle(1040, worldHeight - 260, 20, 20));

        return new LevelData(platforms, enemies, items);
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public List<Platform>   getPlatforms() { return platforms; }
    public List<EnemySpawn> getEnemies()   { return enemies;   }
    public List<Rectangle>  getItems()     { return items;     }
}