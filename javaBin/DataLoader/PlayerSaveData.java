package DataLoader;

import Entities.EnemyFolder.EnemiesType;

import java.io.Serializable;
import java.util.List;

/**
 * Serializable snapshot of player state written to .dat on save.
 * Only stores what changes per-playthrough — not static config like speed or sprite size.
 */
public class PlayerSaveData implements Serializable {

    private static final long serialVersionUID = 2L;

    public final float  health;
    public final int    score;
    public final int    goldCount;
    public final int    weaponSlot;
    public final float  x;
    public final float  y;
    public final String currentLevel;   // e.g. "level1.properties"

    /*/----- CHANGE: added EnemySnapshot inner class and enemies field -----/
     * PURPOSE: Previously enemies were never saved — only player state was
     * serialized. On load, enemyManager.spawnEnemies() always rebuilt the
     * enemy list from the level file's original spawn coordinates, causing
     * every enemy (alive or dead) to teleport back to where they started.
     *
     * EnemySnapshot records the minimum state needed to reconstruct each
     * enemy as it was at save time: its index in the spawn list (used to
     * match it back to the right entry on load), type, current world
     * position, remaining health, and whether it was alive.
     *
     * The enemies field carries one snapshot per enemy that existed at save
     * time. Dead enemies that had already been removed from the live list
     * are NOT included — they simply won't be recreated on load, which is
     * the correct behavior (dead = gone permanently).
     *
     * serialVersionUID bumped to 2L because adding a new field changes the
     * serialized form — old save files written with serialVersionUID 1L will
     * fail to deserialize cleanly, which is expected and acceptable.
     */
    public static class EnemySnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        public final int         spawnIndex; // position in LevelData.getEnemies() list
        public final EnemiesType type;
        public final float       x, y;
        public final float       health;
        public final boolean     alive;

        public EnemySnapshot(int spawnIndex, EnemiesType type,
                             float x, float y, float health, boolean alive) {
            this.spawnIndex = spawnIndex;
            this.type       = type;
            this.x          = x;
            this.y          = y;
            this.health     = health;
            this.alive      = alive;
        }
    }

    public final List<EnemySnapshot> enemies; // one per live enemy at save time
    /*/----- END CHANGE -----/*/

    public PlayerSaveData(float health, int score, int goldCount,
                          int weaponSlot, float x, float y,
                          String currentLevel,
                          List<EnemySnapshot> enemies) {
        this.health       = health;
        this.score        = score;
        this.goldCount    = goldCount;
        this.weaponSlot   = weaponSlot;
        this.x            = x;
        this.y            = y;
        this.currentLevel = currentLevel;
        this.enemies      = enemies;
    }

    @Override
    public String toString() {
        return String.format(
            "PlayerSaveData{health=%.1f, score=%d, gold=%d, slot=%d, x=%.1f, y=%.1f, level=%s, enemies=%d}",
            health, score, goldCount, weaponSlot, x, y, currentLevel,
            enemies != null ? enemies.size() : 0
        );
    }
}