package DataLoader;

import java.io.Serializable;

/**
 * Serializable snapshot of player state written to .dat on save.
 * Only stores what changes per-playthrough — not static config like speed or sprite size.
 */
public class PlayerSaveData implements Serializable {

    private static final long serialVersionUID = 1L;

    public final float  health;
    public final int    score;
    public final int    goldCount;
    public final int    weaponSlot;
    public final float  x;
    public final float  y;
    public final String currentLevel;   // e.g. "level1.properties"

    public PlayerSaveData(float health, int score, int goldCount,
                          int weaponSlot, float x, float y,
                          String currentLevel) {
        this.health       = health;
        this.score        = score;
        this.goldCount    = goldCount;
        this.weaponSlot   = weaponSlot;
        this.x            = x;
        this.y            = y;
        this.currentLevel = currentLevel;
    }

    @Override
    public String toString() {
        return String.format(
            "PlayerSaveData{health=%.1f, score=%d, gold=%d, slot=%d, x=%.1f, y=%.1f, level=%s}",
            health, score, goldCount, weaponSlot, x, y, currentLevel
        );
    }
}