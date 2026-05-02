package Animators;

import Loaders.SpriteLoader;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class PlayerAnimator {

    private static final int ATTACK_FRAME_TIME = 4;

    public static class StateSnapshot {
        public final boolean isGrounded;
        public final float   velX;
        public final float   velY;
        public final boolean sprinting;
        public final int     attackAnimTimer;
        public final int     weaponSlot;      // 1 = gun, 2 = sword

        public StateSnapshot(boolean isGrounded, float velX, float velY,
                             boolean sprinting, int attackAnimTimer, int weaponSlot) {
            this.isGrounded      = isGrounded;
            this.velX            = velX;
            this.velY            = velY;
            this.sprinting       = sprinting;
            this.attackAnimTimer = attackAnimTimer;
            this.weaponSlot      = weaponSlot;
        }
    }

    // ── Animation map ─────────────────────────────────────────────────────────
    private final HashMap<String, Animation> animations = new HashMap<>();
    private Animation currentAnimation;
    private String    currentState = "idle";

    // ── Sprite dimensions (needed for SpriteLoader calls) ────────────────────
    private final int spriteWidth;
    private final int spriteHeight;

    // ─────────────────────────────────────────────────────────────────────────
    //  Construction
    // ─────────────────────────────────────────────────────────────────────────

    public PlayerAnimator(int spriteWidth, int spriteHeight) {
        this.spriteWidth  = spriteWidth;
        this.spriteHeight = spriteHeight;
        loadAnimations();
        currentAnimation = animations.get("idle");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Sprite loading  (mirrors what Player.initialize() used to do)
    // ─────────────────────────────────────────────────────────────────────────

    private void loadAnimations() {
        BufferedImage sharedFrame = SpriteLoader.loadWalkBaseFrame(spriteWidth, spriteHeight);

        BufferedImage[] walkFrames        = SpriteLoader.loadImages("Player-Sprites/Player-Walk",        "player_walk",  8, sharedFrame);
        BufferedImage[] idleFrames        = SpriteLoader.loadImages("Player-Sprites/Player-Idle",        "player_idle",  6, sharedFrame);
        BufferedImage[] jumpFrames        = SpriteLoader.loadImages("Player-Sprites/Player-Jump",        "player_jump",  4, sharedFrame);
        BufferedImage[] fallFrames        = SpriteLoader.loadImages("Player-Sprites/Player-Fall",        "player_fall",  1, sharedFrame);
        BufferedImage[] sprintFrames      = SpriteLoader.loadImages("Player-Sprites/Player-Run",         "player_run",   8, sharedFrame);
        BufferedImage[] swordAttackFrames = SpriteLoader.loadImages("Player-Sprites/Player-Sword-Attack","player_sword", 5, sharedFrame);
        BufferedImage[] shootFrames       = SpriteLoader.loadImages("Player-Sprites/Player-Gun_Attack",  "player_gun",   5, sharedFrame);
        BufferedImage[] rollingFrames     = new BufferedImage[]{ sharedFrame };

        animations.put("idle",         new Animation(idleFrames,        6,                  true));
        animations.put("walk",         new Animation(walkFrames,        8,                  true));
        animations.put("sprint",       new Animation(sprintFrames,      6,                  true));
        animations.put("jump",         new Animation(jumpFrames,        4,                  true));
        animations.put("fall",         new Animation(fallFrames,        1,                  true));
        animations.put("rolling",      new Animation(rollingFrames,     8,                  true));
        animations.put("sword_attack", new Animation(swordAttackFrames, ATTACK_FRAME_TIME,  true));
        animations.put("shooting",     new Animation(shootFrames,       ATTACK_FRAME_TIME,  true));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Update  (called once per tick by Player)
    // ─────────────────────────────────────────────────────────────────────────

    public void update(StateSnapshot s) {
        String nextState = resolveState(s);
        switchTo(nextState);
        if (currentAnimation != null) currentAnimation.animate();
    }

    private String resolveState(StateSnapshot s) {
        if (s.attackAnimTimer > 0) {
            return s.weaponSlot == 1 ? "shooting" : "sword_attack";
        }
        // future: add "rolling", "hurt", "death" checks here before air checks
        if (!s.isGrounded) {
            return s.velY < 0 ? "jump" : "fall";
        }
        if (s.sprinting && Math.abs(s.velX) > 0.5f) {
            return "sprint";
        }
        if (Math.abs(s.velX) > 0.5f) {
            return "walk";
        }
        return "idle";
    }

    private void switchTo(String key) {
        if (key.equals(currentState)) return;
        Animation next = animations.get(key);
        if (next == null) return;
        next.resetanimation();
        currentAnimation = next;
        currentState     = key;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Draw  (called by Player.draw())
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws the current animation frame.
     *
     * @param g           Graphics context already translated by -cameraX
     * @param x           player world X
     * @param y           player world Y
     * @param width       draw width  (pass negative value or handle flip externally)
     * @param height      draw height
     * @param facingRight whether the player is facing right
     */
    public void draw(Graphics g, int x, int y, int width, int height, boolean facingRight) {
        if (currentAnimation == null) return;
        BufferedImage frame = currentAnimation.getCurrentFrame();
        if (frame == null) return;

        int drawX     = x;
        int drawWidth = width;

        if (!facingRight) {
            drawX     += drawWidth;
            drawWidth  = -drawWidth;
        }

        g.drawImage(frame, drawX, y, drawWidth, height, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Accessors
    // ─────────────────────────────────────────────────────────────────────────

    public String    getCurrentState()     { return currentState;     }
    public Animation getCurrentAnimation() { return currentAnimation; }
}