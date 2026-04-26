package Entities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

import java.awt.Graphics;
import java.awt.Rectangle;

public class Player extends LivingEntity {
    private static final int SPRITE_DRAW_Y_OFFSET = 48;

    // ── Hitbox constants ─────────────────────────────────────────────────────────
    // The sprite sheet is 112×168 px but the visible character occupies only
    // the lower portion.  SPRITE_DRAW_Y_OFFSET (48) pushes the image down when
    // drawing, so the logical hitbox starts 48 px into the sprite height.
    // These values are exposed via getHitboxOffsetY() / getHitboxHeight() so
    // Level.java can correctly snap the player onto platforms.
    private static final int HITBOX_OFFSET_Y = SPRITE_DRAW_Y_OFFSET; // 48 px from top of entity
    private static final int HITBOX_HEIGHT   = 168 - HITBOX_OFFSET_Y; // remaining 120 px

    private boolean facingRight = true;

    private boolean movingLeft;
    private boolean movingRight;
    private boolean movingUp;
    private boolean movingDown;

    private boolean isJumping;
    private boolean running;
    protected boolean isGrounded;

    private boolean isAttacking;
    private long lastAttackTime;
    private int attackCoolDown;

    private float velX;
    private float velY;
    private float gravity;

    private int score;
    private int goldCount;

    private HashMap<String, Animation> animations = new HashMap<>();
    private String currentState = "idle";

    private BufferedImage[] walkFrames;
    private BufferedImage[] idleFrames;
    private BufferedImage[] jumpFrames;
    private BufferedImage[] fallFrames;
    private BufferedImage[] rollingFrames;
    private BufferedImage[] ledgegrabFrames;

    private Animation currentAnimation;
    private static final int JUMP_BUFFER_FRAMES = 8;
    private static final int COYOTE_FRAMES = 6;
    private int jumpBufferFrames;
    private int coyoteFrames;

    public Player(float velX, float velY, float x, float y) {
        super(x, y, 112, 168, 20.0f, 5.0f, 6.0f, 14.0f);
        this.velX = velX;
        this.velY = velY;
        this.goldCount = 0;
        this.score = 0;
        this.attackCoolDown = 400;
        this.isAttacking = false;
        this.running = false;
        this.isJumping = false;
        this.gravity = 0.65f;
        this.jumpBufferFrames = 0;
        this.coyoteFrames = 0;
        initialize();
    }

    private void initialize() {
        BufferedImage sharedFrame = loadWalkBaseFrame();
        walkFrames     = loadWalkAnimationFrames(sharedFrame);
        idleFrames     = new BufferedImage[]{ sharedFrame };
        jumpFrames     = new BufferedImage[]{ sharedFrame };
        fallFrames     = new BufferedImage[]{ sharedFrame };
        rollingFrames  = new BufferedImage[]{ sharedFrame };
        ledgegrabFrames= new BufferedImage[]{ sharedFrame };

        animations.put("walk",      new Animation(walkFrames,      4,  true));
        animations.put("idle",      new Animation(idleFrames,      10, true));
        animations.put("jump",      new Animation(jumpFrames,      8,  false));
        animations.put("fall",      new Animation(fallFrames,      8,  true));
        animations.put("rolling",   new Animation(rollingFrames,   8,  false));
        animations.put("ledgegrab", new Animation(ledgegrabFrames, 8,  false));
        currentAnimation = animations.get("idle");
    }

    public void jump() {
        isJumping = true;
        jumpBufferFrames = JUMP_BUFFER_FRAMES;
    }

    @Override
    public void update() {
        velX = 0;
        if (movingLeft)  { velX = -walk_speed; facingRight = false; }
        if (movingRight) { velX =  walk_speed; facingRight = true;  }
        x += velX;

        if (isGrounded) {
            coyoteFrames = COYOTE_FRAMES;
        } else if (coyoteFrames > 0) {
            coyoteFrames--;
        }

        if (jumpBufferFrames > 0) {
            jumpBufferFrames--;
        }

        if (coyoteFrames > 0 && jumpBufferFrames > 0) {
            velY = -Math.abs(jump_height);
            isGrounded = false;
            coyoteFrames = 0;
            jumpBufferFrames = 0;
        }
        isJumping = false;

        velY += gravity;
        if (velY >= 20) { velY = 20; }
        y += velY;

        updateAnimation();
    }

    private void updateAnimation() {
        String curState = "idle";
        if (!isGrounded) {
            curState = (velY < 0) ? "jump" : "fall";
        } else if (Math.abs(velX) > 0.5f) {
            curState = "walk";
        }
        if (!curState.equals(currentState)) {
            currentState = curState;
            currentAnimation = animations.get(curState);
        }
        if (currentAnimation != null) {
            currentAnimation.animate();
        }
    }

    @Override
    public void draw(Graphics g) {
        if (currentAnimation == null) return;
        BufferedImage frame = currentAnimation.getCurrentFrame();
        if (frame == null) return;

        int drawX     = (int) x;
        int drawWidth = (int) width;
        if (!facingRight) {
            drawX    += drawWidth;
            drawWidth = -drawWidth;
        }
        g.drawImage(frame, drawX, (int) y + SPRITE_DRAW_Y_OFFSET, drawWidth, (int) height, null);
    }

    // ── Hitbox helpers (used by Level.handleCollisions) ──────────────────────────

    /**
     * Returns the Y offset (in pixels) from the entity's top edge (this.y) to
     * the top of its logical collision hitbox.
     * <p>
     * Because the sprite sheet has blank padding above the character equal to
     * {@code SPRITE_DRAW_Y_OFFSET}, the hitbox starts at the same offset so
     * that visual and physical positions stay in sync.
     */
    public int getHitboxOffsetY() {
        return HITBOX_OFFSET_Y;
    }

    /**
     * Returns the pixel height of the logical collision hitbox (i.e. the
     * portion of the sprite that actually represents the character's body).
     */
    public int getHitboxHeight() {
        return HITBOX_HEIGHT;
    }

    /**
     * Returns the collision rectangle aligned to the visible character body,
     * taking the hitbox offset into account.
     */
    @Override
    public Rectangle getBounds() {
        return new Rectangle(
            (int) x,
            (int) y + HITBOX_OFFSET_Y,
            (int) width,
            HITBOX_HEIGHT
        );
    }

    // ── Getters / setters ────────────────────────────────────────────────────────

    public boolean isGrounded() { return isGrounded; }

    public float getHealth() { return health; }

    public float getVerticalVelocity() { return velY; }
    public void  setVerticalVelocity(float velY) { this.velY = velY; }

    public void setGrounded(boolean grounded) { this.isGrounded = grounded; }

    public void applyEngineState(boolean moveLeft, boolean moveRight, boolean jumpPressed) {
        this.movingLeft  = moveLeft;
        this.movingRight = moveRight;
        if (jumpPressed) jump();
    }

    public void applyBuff(String buffType, int durationSeconds) {
        if (buffType == null) return;
        switch (buffType) {
            case "SPEED_BOOST":   walk_speed  = Math.max(1.0f, walk_speed  + 2.0f); break;
            case "DOUBLE_JUMP":   jump_height = Math.max(1.0f, jump_height + 5.0f); break;
            case "INVINCIBILITY": break; // placeholder
            default: break;
        }
    }

    public void applyDebuff(String buffType, int durationSeconds) {
        if (buffType == null) return;
        switch (buffType) {
            case "DEBUFF_SLOWNESS":
                walk_speed = Math.max(1.0f, walk_speed - 2.0f);
                break;
            case "DEBUFF_REVERSE_CONTROLS":
                boolean temp = movingLeft;
                movingLeft  = movingRight;
                movingRight = temp;
                break;
            default: break;
        }
    }

    // ── Sprite loading ───────────────────────────────────────────────────────────

    private BufferedImage loadWalkBaseFrame() {
        BufferedImage img;
        img = loadFrameOrNull("/Resources/00_character_walk.png");    if (img != null) return img;
        img = loadFrameFromFile("Resources/00_character_walk.png");   if (img != null) return img;
        img = loadFrameFromFile("../Resources/00_character_walk.png");if (img != null) return img;
        img = loadFrameFromFile("00_character_walk.png");             if (img != null) return img;
        return createFallbackFrame();
    }

    private BufferedImage[] loadWalkAnimationFrames(BufferedImage fallbackFrame) {
        BufferedImage[] frames = new BufferedImage[8];
        for (int i = 0; i < frames.length; i++) {
            String name = String.format("%02d_character_walk.png", i);
            BufferedImage f = loadFrameOrNull("/Resources/" + name);
            if (f == null) f = loadFrameFromFile("Resources/" + name);
            if (f == null) f = loadFrameFromFile("../Resources/" + name);
            if (f == null) f = loadFrameFromFile(name);
            frames[i] = (f != null) ? f : fallbackFrame;
        }
        return frames;
    }

    private BufferedImage loadFrameOrNull(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            return (url != null) ? ImageIO.read(url) : null;
        } catch (IOException e) { return null; }
    }

    private BufferedImage loadFrameFromFile(String path) {
        try {
            File f = new File(path);
            return f.exists() ? ImageIO.read(f) : null;
        } catch (IOException e) { return null; }
    }

    private BufferedImage createFallbackFrame() {
        BufferedImage fallback = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fallback.createGraphics();
        g2.setColor(new Color(45, 95, 250));
        g2.fillRect(0, 0, (int) width, (int) height);
        g2.setColor(Color.WHITE);
        g2.fillRect(6, 10, 6, 6);
        g2.fillRect((int) width - 12, 10, 6, 6);
        g2.dispose();
        return fallback;
    }

    @Override
    public void onDeath() { }
}