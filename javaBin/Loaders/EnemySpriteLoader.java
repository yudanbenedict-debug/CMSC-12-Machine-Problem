package Loaders;


import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import Entities.EnemyFolder.EnemiesType;

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
        public final BufferedImage[] attack;
        public final BufferedImage[] hurt;
        public final BufferedImage[] death;

        EnemyFrameSet(BufferedImage[] walk,   BufferedImage[] idle,
                      BufferedImage[] attack, BufferedImage[] hurt,
                      BufferedImage[] death) {
            this.walk   = walk;
            this.idle   = idle;
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
    
    private static EnemyFrameSet load(EnemiesType type) {
        String enemy = getEnemyFolder(type); // enemy1, enemy2, etc.
        String basePath = "Enemy-Sprites/" + enemy + "/";
    
        BufferedImage fb = SpriteLoader.loadWalkBaseFrame(
            type.spriteWidth,
            type.spriteHeight
        );
    
        BufferedImage[] walk = SpriteLoader.loadImages(
            basePath + "patrol", enemy + "_walk",
            type.walkFrameCount, fb
        );
    
        BufferedImage[] idle = SpriteLoader.loadImages(
            basePath + "idle/", enemy + "_idle",
            10, fb
        );
    
        BufferedImage[] attack = SpriteLoader.loadImages(
            basePath + "attack/", enemy + "_attack",
            type.attackFrameCount, fb
        );
    
        BufferedImage[] hurt = SpriteLoader.loadImages(
            basePath + "hurt/", enemy + "_hurt",
            4, fb
        );
    
        BufferedImage[] death = SpriteLoader.loadImages(
            basePath + "die/", enemy + "_die",
            7, fb
        );
    
        return new EnemyFrameSet(walk, idle, attack, hurt, death);
    }
    

    // since ts can't be an abstract, just create a private constr to prevent instantiation.
    private EnemySpriteLoader() {}
}