package GameCreation;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import GamePlatform.Platform;
import GamePlatform.PlatformType;
//add file reader here
public class LevelData {
    private final ArrayList<Platform> platforms;
    private final ArrayList<Rectangle> enemies;
    private final ArrayList<Rectangle> items;

    public LevelData(ArrayList<Platform> platforms, ArrayList<Rectangle> enemies, ArrayList<Rectangle> items) {
        this.platforms = platforms;
        this.enemies = enemies;
        this.items = items;
    }

    public static LevelData createStarterLevel(int worldWidth, int worldHeight) {
        int floorTop = worldHeight - 60;
        ArrayList<Platform> platforms = new ArrayList<>();
        ArrayList<Rectangle> enemies  = new ArrayList<>();
        ArrayList<Rectangle> items    = new ArrayList<>();

        // Floor
        platforms.add(new Platform(worldWidth, 60, 0, floorTop, PlatformType.METAL)
        .setCollisionBox(0, 0, 0, 0));

        // Floating platforms — WOOD swapped to GRASS
        platforms.add(new Platform(220, 20, 260,  floorTop - 180, PlatformType.GRASS)
                .setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(220, 32, 640,  floorTop - 280, PlatformType.GRASS)
                .setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(220, 32, 980,  floorTop - 220, PlatformType.GRASS)
                .setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(200, 32, 1400, floorTop - 160, PlatformType.GRASS)
                .setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(200, 32, 1800, floorTop - 300, PlatformType.GRASS)
                .setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(220, 32, 2200, floorTop - 200, PlatformType.GRASS)
                .setCollisionBox(0, 0, 0, 0));

        // SAND platform
        platforms.add(new Platform(240, 32, 2600, floorTop - 260, PlatformType.SAND)
                .setCollisionBox(0, 0, 0, 0));

        // DIRT raised block
        platforms.add(new Platform(180, 32, 3100, floorTop - 180, PlatformType.DIRT)
                .setCollisionBox(0, 0, 0, 0));

        enemies.add(new Rectangle(800,  floorTop - 36, 40, 1));   // SLIME
        enemies.add(new Rectangle(1400, floorTop - 64, 48, 1));   // GOBLIN
        enemies.add(new Rectangle(2000, floorTop - 36, 40, 1));   // SLIME
        enemies.add(new Rectangle(2800, floorTop - 80, 56, 1));   // ORC
        enemies.add(new Rectangle(3400, floorTop - 72, 64, 1));   // SKELETON

        // ── Items ────────────────────────────────────────────────────────────────
        items.add(new Rectangle(320,  worldHeight - 220, 20, 20));
        items.add(new Rectangle(1040, worldHeight - 260, 20, 20));

        return new LevelData(platforms, enemies, items);
    }

    public List<Platform> getPlatforms() { return platforms; }
    public List<Rectangle> getEnemies()  { return enemies;   }
    public List<Rectangle> getItems()    { return items;     }
}