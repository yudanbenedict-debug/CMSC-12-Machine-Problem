package DataLoader;

import Entities.EnemyFolder.EnemiesType;

import java.io.Serializable;
import java.util.List;

//savaable data for the player
public class PlayerSaveData implements Serializable {

    private static final long serialVersionUID = 2L;

    public final float  health;
    public final int    score;
    public final int    goldCount;
    public final int    weaponSlot;
    public final float  x;
    public final float  y;
    public final String currentLevel;   // e.g. "level1.properties"

   //inner class for the enemy aswell since the it'd be better to save enemy pos aswell and stats. (could rename it to level save data)
    public static class EnemySnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        public final int         spawnIndex; 
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