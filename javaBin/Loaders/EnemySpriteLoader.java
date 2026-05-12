package Loaders;


import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import Entities.EnemyFolder.EnemiesType;
import DataLoader.EnemyDataLoader;
import DataLoader.EnemyDataLoader.EnemyData;

/**
 * Static sprite cache for all enemy types.
 *
 * Frames are loaded from disk exactly once per EnemiesType and reused across
 * every instance of that type — so 5 slimes share the same BufferedImage arrays
 * instead of each loading their own copies.
 *
 * Naming convention expected under Resources/enemies/:
 *   {type_lc}_{state}_{NN}.png   e.g.  slime_walk_00.png
 */
public class EnemySpriteLoader {

    // ── Cache ─────────────────────────────────────────────────────────────────
    private static final Map<EnemiesType, EnemyFrameSet> cache =
            new EnumMap<>(EnemiesType.class);

    // ── Per-type frame bundle ─────────────────────────────────────────────────
    public static class EnemyFrameSet {
        public final BufferedImage[] walk;
        public final BufferedImage[] idle;
        public final BufferedImage[] chase;
        public final BufferedImage[] alert;
        public final BufferedImage[] attack;
        public final BufferedImage[] hurt;
        public final BufferedImage[] death;

        EnemyFrameSet(BufferedImage[] walk,   BufferedImage[] idle,
                      BufferedImage[] chase,  BufferedImage[] alert,
                      BufferedImage[] attack, BufferedImage[] hurt,
                      BufferedImage[] death) {
            this.walk   = walk;
            this.idle   = idle;
            this.chase  = chase;
            this.alert  = alert;
            this.attack = attack;
            this.hurt   = hurt;
            this.death  = death;
        }
    }

    // ── Public accessor ───────────────────────────────────────────────────────
    public static EnemyFrameSet get(EnemiesType type) {
        return cache.computeIfAbsent(type, EnemySpriteLoader::load);
    }

    // ── Internal loader ───────────────────────────────────────────────────────
    private static String getEnemyFolder(EnemiesType type) {
        return switch (type) {
            case GUN_ENEMY   -> "enemy1";
            case SWORD_ENEMY -> "enemy2";
            case PUNCH_ENEMY -> "enemy3";
            case KICK_ENEMY  -> "enemy4";
        };
    }
    
    private static String getAttackFolder(EnemiesType type) {
        return switch (type) {
            case GUN_ENEMY   -> "gun";
            case SWORD_ENEMY -> "sword";
            case PUNCH_ENEMY -> "punch";
            case KICK_ENEMY  -> "kick";
        };
    }

    private static EnemyFrameSet load(EnemiesType type) {
        String    enemy      = getEnemyFolder(type);
        String    basePath   = "Resources/Enemy-Sprites/" + enemy + "/";
        String    attackType = getAttackFolder(type);
        EnemyData data       = EnemyDataLoader.get(type);

        BufferedImage fb = SpriteLoader.loadWalkBaseFrame(
            data.spriteWidth,
            data.spriteHeight
        );

        BufferedImage[] walk = SpriteLoader.loadImages(
            basePath + "patrol", enemy + "_patrol",
            data.walkFrameCount, fb
        );

        BufferedImage[] idle = SpriteLoader.loadImages(
            basePath + "idle", enemy + "_idle",
            data.walkFrameCount, fb
        );

        BufferedImage[] chase = SpriteLoader.loadImages(
            basePath + "chase", enemy + "_chase",
            data.chaseFrameCount, fb
        );

        BufferedImage[] alert = SpriteLoader.loadImages(
            basePath + "alert", enemy + "_alert",
            data.alertFrameCount, fb
        );

        BufferedImage[] attack = SpriteLoader.loadImages(
            basePath + attackType, enemy + "_" + attackType,
            data.attackFrameCount, fb
        );

        BufferedImage[] hurt = SpriteLoader.loadImages(
            basePath + "hurt", enemy + "_hurt",
            4, fb
        );

        BufferedImage[] death = SpriteLoader.loadImages(
            basePath + "die", enemy + "_die",
            7, fb
        );

        return new EnemyFrameSet(walk, idle, chase, alert, attack, hurt, death);
    }
    

    // since ts can't be an abstract, just create a private constr to prevent instantiation.
    private EnemySpriteLoader() {}
}