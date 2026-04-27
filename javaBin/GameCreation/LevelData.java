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
        ArrayList<Platform> platforms = new ArrayList<>();
        ArrayList<Rectangle> enemies  = new ArrayList<>();
        ArrayList<Rectangle> items    = new ArrayList<>();

        platforms.add(new Platform(worldWidth, 60, 0, worldHeight - 60, PlatformType.METAL)
                .setCollisionBox(0, 0, 0, 0));

        platforms.add(new Platform(220, 30, 260,  worldHeight - 180, PlatformType.WOOD)
                .setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(220, 30, 640,  worldHeight - 280, PlatformType.WOOD)
                .setCollisionBox(0, 0, 0, 0));
        platforms.add(new Platform(220, 30, 980,  worldHeight - 220, PlatformType.WOOD)
                .setCollisionBox(0, 0, 0, 0));

        int floorTop = worldHeight - 60;

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