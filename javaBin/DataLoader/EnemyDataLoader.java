package DataLoader;

import Entities.EnemyFolder.EnemiesType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

/**
 * Loads enemy stat data from .properties files at runtime.
 * Replaces the hardcoded values in EnemiesType enum fields.
 *
 * Expected path: Resources/data/enemies/{type_lc}.properties
 * e.g. Resources/data/enemies/gun_enemy.properties
 */
public class EnemyDataLoader {

    // ── Loaded data bundle ────────────────────────────────────────────────────
    public static class EnemyData {
        public final float baseHealth;
        public final float baseDamage;
        public final float baseSpeed;
        public final int   patrolRange;
        public final int   aggroRange;
        public final int   spriteWidth;
        public final int   spriteHeight;
        public final int   walkFrameCount;
        public final int   chaseFrameCount;
        public final int   alertFrameCount;
        public final int   attackFrameCount;
        public final int   deathFrameCount;

        public EnemyData(float baseHealth, float baseDamage, float baseSpeed,
                         int patrolRange,  int aggroRange,
                         int spriteWidth,  int spriteHeight,
                         int walkFrameCount,  int chaseFrameCount,
                         int alertFrameCount, int attackFrameCount,
                         int deathFrameCount) {
            this.baseHealth       = baseHealth;
            this.baseDamage       = baseDamage;
            this.baseSpeed        = baseSpeed;
            this.patrolRange      = patrolRange;
            this.aggroRange       = aggroRange;
            this.spriteWidth      = spriteWidth;
            this.spriteHeight     = spriteHeight;
            this.walkFrameCount   = walkFrameCount;
            this.chaseFrameCount  = chaseFrameCount;
            this.alertFrameCount  = alertFrameCount;
            this.attackFrameCount = attackFrameCount;
            this.deathFrameCount  = deathFrameCount;
        }
    }

    private static final String BASE_PATH = "Resources/Data/Enemies/";

    // ── Cache — loaded once per session ──────────────────────────────────────
    private static final Map<EnemiesType, EnemyData> cache =
            new EnumMap<>(EnemiesType.class);

    // ── Public accessor ───────────────────────────────────────────────────────

    /**
     * Returns the EnemyData for the given type, loading from disk if needed.
     * Falls back to hardcoded defaults if the file is missing.
     */
    public static EnemyData get(EnemiesType type) {
        return cache.computeIfAbsent(type, EnemyDataLoader::load);
    }

    // ── Internal loader ───────────────────────────────────────────────────────

    private static EnemyData load(EnemiesType type) {
        String fileName = type.name().toLowerCase() + ".properties";
        File   file     = new File(BASE_PATH + fileName);

        Properties props = new Properties();

        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                System.out.println("[EnemyDataLoader] Loaded " + fileName);
            } catch (IOException e) {
                System.err.println("[EnemyDataLoader] Failed to read " + fileName + " — using hardcoded fallback");
                return hardcodedFallback(type);
            }
        } else {
            System.err.println("[EnemyDataLoader] Missing " + file.getPath() + " — using hardcoded fallback");
            return hardcodedFallback(type);
        }

        return new EnemyData(
            Float.parseFloat(props.getProperty("baseHealth")),
            Float.parseFloat(props.getProperty("baseDamage")),
            Float.parseFloat(props.getProperty("baseSpeed")),
            Integer.parseInt(props.getProperty("patrolRange")),
            Integer.parseInt(props.getProperty("aggroRange")),
            Integer.parseInt(props.getProperty("spriteWidth")),
            Integer.parseInt(props.getProperty("spriteHeight")),
            Integer.parseInt(props.getProperty("walkFrameCount")),
            Integer.parseInt(props.getProperty("chaseFrameCount")),
            Integer.parseInt(props.getProperty("alertFrameCount")),
            Integer.parseInt(props.getProperty("attackFrameCount")),
            Integer.parseInt(props.getProperty("deathFrameCount"))
        );
    }

    private static EnemyData hardcodedFallback(EnemiesType type) {
        // Default stats used when .properties file is missing or unreadable
        return new EnemyData(
            20f, 4f, 1.2f,
            80, 140,
            32, 48,
            8, 8, 1, 5, 10
        );
    }

    private EnemyDataLoader() {}
}