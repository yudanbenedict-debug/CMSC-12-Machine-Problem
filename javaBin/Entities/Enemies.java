package Entities;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import GamePlatform.Platform;

/**
 * Concrete enemy class.
 *
 * <h3>AI state machine</h3>
 * <pre>
 *  PATROL ──(player enters aggroRange)──▶ CHASE
 *  CHASE  ──(player leaves aggroRange)──▶ PATROL
 *  CHASE  ──(within ATTACK_REACH)──────▶ ATTACK
 *  ATTACK ──(player moves away)─────────▶ CHASE / PATROL
 *  any    ──(health ≤ 0)────────────────▶ DEAD
 * </pre>
 *
 * <h3>Randomisation</h3>
 * Each instance gets its own {@link Random} seeded from type + spawn position,
 * so two enemies of the same type placed at the same X still behave differently.
 *
 * <h3>Collision</h3>
 * Call {@link #update(List, float, float)} each tick.  The enemy will not pass
 * through any solid platform and will turn around on wall contact.
 */
public class Enemies extends LivingEntity {

    // ── AI states ────────────────────────────────────────────────────────────────
    public enum AIState { PATROL, CHASE, ATTACK, DEAD }

    // ── Physics constants ────────────────────────────────────────────────────────
    private static final float GRAVITY        = 0.65f;
    private static final float MAX_FALL_SPEED = 20f;
    /** Snap tolerance for landing detection (pixels). */
    private static final float SNAP_TOL       = 6f;

    // ── AI constants ─────────────────────────────────────────────────────────────
    private static final int MIN_IDLE   = 40;
    private static final int MAX_IDLE   = 120;
    /** Ticks between attacks. */
    private static final int ATK_CD     = 50;
    /** Pixel distance from player centre to trigger an attack. */
    private static final int ATK_REACH  = 55;
    /** Ticks to show the hurt-flash overlay after taking damage. */
    private static final int HIT_FLASH  = 12;

    // ── State ────────────────────────────────────────────────────────────────────
    private final EnemiesType type;
    private final float       spawnX;
    private final float       maxHealth;
    private final Random      rng;

    private float   velX, velY;
    private boolean grounded;
    private boolean facingRight;
    private boolean alive;

    private AIState aiState;
    private int     idleTimer;
    private int     attackCooldown;
    private int     hitFlashTimer;

    // ── Animation ────────────────────────────────────────────────────────────────
    private final Map<String, Animation> animations = new HashMap<>();
    private Animation currentAnim;
    private String    currentAnimKey = "";

    // ─────────────────────────────────────────────────────────────────────────────
    //  Construction
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * @param type  which enemy variant
     * @param x     world left-edge X
     * @param y     world top-edge Y  (set so bottom == floor top for instant grounding)
     */
    public Enemies(EnemiesType type, float x, float y) {
        super(x, y,
              spriteW(type), spriteH(type),
              type.baseHealth, type.baseDamage,
              type.baseSpeed,  type.baseSpeed);

        this.type      = type;
        this.spawnX    = x;
        this.maxHealth = type.baseHealth;
        this.rng       = new Random((long)(type.ordinal() * 1_000_003L + (long)x * 31 + (long)y));

        this.alive         = true;
        this.aiState       = AIState.PATROL;
        this.facingRight   = rng.nextBoolean();
        this.velX          = 0f;
        this.velY          = 0f;
        this.idleTimer     = 0;
        this.attackCooldown= 0;
        this.hitFlashTimer = 0;

        initAnimations();
        switchAnim("walk");
    }

    // ── Sprite dimensions per type ────────────────────────────────────────────────
    private static int spriteW(EnemiesType t) {
        switch (t) {
            case SLIME:    return 48;
            case GOBLIN:   return 56;
            case ORC:      return 72;
            case SKELETON: return 56;
            default:       return 48;
        }
    }

    private static int spriteH(EnemiesType t) {
        switch (t) {
            case SLIME:    return 36;
            case GOBLIN:   return 64;
            case ORC:      return 80;
            case SKELETON: return 72;
            default:       return 48;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Animation initialisation
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Tries to load real sprite sheets from the classpath.
     * Falls back to a generated coloured rectangle if any file is missing —
     * the enemy is always visible regardless of asset state.
     *
     * Naming convention (PNG files in /Resources/enemies/):
     *   {type_lc}_{state}_{NN}.png   e.g. slime_walk_00.png
     */
    private void initAnimations() {
        String p = type.name().toLowerCase();
        animations.put("walk",   makeAnim(p, "walk",   4,  true,  walkFrames()));
        animations.put("idle",   makeAnim(p, "idle",   10, true,  1));
        animations.put("attack", makeAnim(p, "attack", 5,  false, atkFrames()));
        animations.put("hurt",   makeAnim(p, "hurt",   4,  false, 2));
        animations.put("death",  makeAnim(p, "death",  7,  false, 4));
    }

    private int walkFrames() {
        switch (type) { case SLIME: return 4; default: return 6; }
    }
    private int atkFrames()  {
        switch (type) { case SLIME: return 3; default: return 5; }
    }

    private Animation makeAnim(String prefix, String state,
                               int frameTime, boolean loop, int count) {
        BufferedImage[] frames = new BufferedImage[count];
        boolean ok = true;
        for (int i = 0; i < count; i++) {
            String path = String.format("/Resources/enemies/%s_%s_%02d.png",
                                        prefix, state, i);
            BufferedImage img = loadImg(path);
            if (img == null) { ok = false; break; }
            frames[i] = img;
        }
        // Fill any missing slots with the fallback sprite
        if (!ok) {
            BufferedImage fb = buildFallback();
            for (int i = 0; i < count; i++) {
                if (frames[i] == null) frames[i] = fb;
            }
        }
        return new Animation(frames, frameTime, loop);
    }

    private BufferedImage loadImg(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return null;
            return javax.imageio.ImageIO.read(url);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Procedurally generated fallback sprite — visible even with no art assets.
     * Each enemy type has a distinct colour.
     */
    private BufferedImage buildFallback() {
        int w = (int) width, h = (int) height;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Body colour per type
        Color body;
        switch (type) {
            case SLIME:    body = new Color(60,  200,  80); break;
            case GOBLIN:   body = new Color(90,  160,  60); break;
            case ORC:      body = new Color(70,  110,  50); break;
            case SKELETON: body = new Color(230, 220, 200); break;
            default:       body = Color.GRAY;
        }

        // Body block
        g.setColor(body);
        g.fillRoundRect(2, h / 3, w - 4, (h * 2 / 3) - 2, 8, 8);

        // Head circle
        int headR = w / 3;
        int headCX = w / 2;
        int headCY = headR + 2;
        g.setColor(body.brighter());
        g.fillOval(headCX - headR, 2, headR * 2, headR * 2);

        // Eyes (always face right in sheet; flipping handled at draw time)
        int eyeY  = headCY - 2;
        int eyeOff= headR / 3;
        g.setColor(Color.WHITE);
        g.fillOval(headCX + eyeOff - 2,     eyeY, 5, 5);
        g.fillOval(headCX + eyeOff + 6,     eyeY, 5, 5);
        g.setColor(new Color(30, 30, 30));
        g.fillOval(headCX + eyeOff - 1,     eyeY + 1, 3, 3);
        g.fillOval(headCX + eyeOff + 7,     eyeY + 1, 3, 3);

        // Outline
        g.setColor(body.darker().darker());
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(2, h / 3, w - 4, (h * 2 / 3) - 2, 8, 8);
        g.drawOval(headCX - headR, 2, headR * 2, headR * 2);

        g.dispose();
        return img;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Public update  (called by Level every tick)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Full update: AI decisions → physics → platform collision → timers → animation.
     *
     * @param platforms current level platforms for solid collision
     * @param playerX   player world centre X
     * @param playerY   player world centre Y
     */
    public void update(List<Platform> platforms, float playerX, float playerY) {
        if (!alive) {
            tickAnim();
            return;
        }
        tickAI(playerX, playerY);
        applyPhysics();
        resolvePlatforms(platforms);
        tickTimers();
        tickAnim();
    }

    /** Minimal overload (no platform list). Gravity only — no wall resolution. */
    @Override
    public void update() {
        if (!alive) return;
        applyPhysics();
        tickAnim();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  AI
    // ─────────────────────────────────────────────────────────────────────────────

    private void tickAI(float playerX, float playerY) {
        float cx   = x + width  / 2f;
        float dx   = playerX - cx;
        float dist = Math.abs(dx);

        switch (aiState) {

            case PATROL:
                if (dist <= type.aggroRange) {
                    aiState   = AIState.CHASE;
                    idleTimer = 0;
                    break;
                }
                doPatrol(cx);
                break;

            case CHASE:
                // Lost player — go back to patrol
                if (dist > type.aggroRange * 1.3f) {
                    aiState   = AIState.PATROL;
                    idleTimer = randIdle();
                    velX      = 0f;
                    break;
                }
                // Close enough to attack
                if (dist <= ATK_REACH) {
                    aiState = AIState.ATTACK;
                    velX    = 0f;
                    break;
                }
                // Move toward player
                facingRight = dx > 0;
                velX = facingRight ? walk_speed : -walk_speed;
                break;

            case ATTACK:
                velX = 0f;
                if (attackCooldown <= 0) {
                    attackCooldown = ATK_CD; // damage is read by Level via isReadyToAttack()
                }
                // Player moved away
                if (dist > ATK_REACH + 10) {
                    aiState = (dist <= type.aggroRange) ? AIState.CHASE : AIState.PATROL;
                }
                break;

            case DEAD:
                velX = 0f;
                break;
        }
    }

    private void doPatrol(float cx) {
        // Wait during idle pause
        if (idleTimer > 0) {
            velX = 0f;
            return;
        }
        float half = type.patrolRange;
        if (cx < spawnX - half) {
            // Drifted left of zone — come back right
            facingRight = true;
            velX        =  walk_speed;
        } else if (cx > spawnX + half) {
            // Drifted right of zone — come back left
            facingRight = false;
            velX        = -walk_speed;
        } else {
            // Walk in current direction
            velX = facingRight ? walk_speed : -walk_speed;
        }
    }

    /**
     * Called by the collision resolver when the enemy walks into a solid wall.
     * Flips direction and adds a brief random pause so it doesn't instantly
     * bounce back and forth in a corner.
     */
    private void bounce() {
        facingRight = !facingRight;
        velX        = facingRight ? walk_speed : -walk_speed;
        idleTimer   = MIN_IDLE / 2 + rng.nextInt(MIN_IDLE / 2);
    }

    private int randIdle() {
        return MIN_IDLE + rng.nextInt(MAX_IDLE - MIN_IDLE);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Physics
    // ─────────────────────────────────────────────────────────────────────────────

    private void applyPhysics() {
        x += velX;

        if (!grounded) {
            velY += GRAVITY;
            if (velY > MAX_FALL_SPEED) velY = MAX_FALL_SPEED;
        }
        y += velY;

        grounded = false;   // collision resolver sets this back to true if on ground
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Platform collision  (solid — cannot pass through)
    // ─────────────────────────────────────────────────────────────────────────────

    private void resolvePlatforms(List<Platform> platforms) {
        for (Platform plat : platforms) {
            Rectangle pb = plat.getBounds();

            float pLeft   = pb.x;
            float pRight  = pb.x + pb.width;
            float pTop    = pb.y;
            float pBottom = pb.y + pb.height;

            float myLeft   = x;
            float myRight  = x + width;
            float myBottom = y + height;
            float myTop    = y;

            // Overlap on each axis
            boolean hOverlap = myRight > pLeft && myLeft < pRight;
            boolean vOverlap = myBottom > pTop  && myTop  < pBottom;

            // ── Land on top surface ───────────────────────────────────────────────
            if (hOverlap && velY >= 0
                    && myBottom >= pTop
                    && myBottom - velY <= pTop + SNAP_TOL) {
                y        = pTop - height;
                velY     = 0f;
                grounded = true;
                continue;   // no further push-out needed this platform
            }

            if (!hOverlap || !vOverlap) continue;

            // ── Ceiling (hit underside of platform) ───────────────────────────────
            if (velY < 0 && myTop <= pBottom && myTop - velY >= pBottom - SNAP_TOL) {
                y    = pBottom;
                velY = 0f;
                continue;
            }

            // ── Left wall (enemy walking right into the left face) ────────────────
            if (velX > 0 && myRight > pLeft && myRight - velX <= pLeft + SNAP_TOL) {
                x    = pLeft - width;
                velX = 0f;
                bounce();
                continue;
            }

            // ── Right wall (enemy walking left into the right face) ───────────────
            if (velX < 0 && myLeft < pRight && myLeft - velX >= pRight - SNAP_TOL) {
                x    = pRight;
                velX = 0f;
                bounce();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Timers
    // ─────────────────────────────────────────────────────────────────────────────

    private void tickTimers() {
        if (idleTimer      > 0) idleTimer--;
        if (attackCooldown > 0) attackCooldown--;
        if (hitFlashTimer  > 0) hitFlashTimer--;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Animation
    // ─────────────────────────────────────────────────────────────────────────────

    private void tickAnim() {
        String key;
        if (!alive) {
            key = "death";
        } else if (hitFlashTimer > 0) {
            key = "hurt";
        } else if (aiState == AIState.ATTACK && attackCooldown > ATK_CD - 20) {
            key = "attack";
        } else if (Math.abs(velX) > 0.1f) {
            key = "walk";
        } else {
            key = "idle";
        }
        switchAnim(key);
        if (currentAnim != null) currentAnim.animate();
    }

    private void switchAnim(String key) {
        if (!key.equals(currentAnimKey)) {
            Animation next = animations.get(key);
            if (next != null) {
                next.resetanimation();
                currentAnim    = next;
                currentAnimKey = key;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Drawing
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Draw the enemy.  The Graphics must already be translated by -cameraX
     * (same pattern as the player in Game.java).
     */
    @Override
    public void draw(Graphics g) {
        if (currentAnim == null) return;
        BufferedImage frame = currentAnim.getCurrentFrame();
        if (frame == null) return;

        // Always need Graphics2D — Game.java always provides one via g2.create().
        if (!(g instanceof Graphics2D)) return;
        Graphics2D g2 = (Graphics2D) g;

        // ── Hit-flash red overlay ─────────────────────────────────────────────────
        if (hitFlashTimer > 0) {
            Composite orig = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.setColor(new Color(255, 60, 60));
            g2.fillRect((int) x, (int) y, (int) width, (int) height);
            g2.setComposite(orig);
        }

        // ── Sprite with correct horizontal flip ───────────────────────────────────
        // IMPORTANT: drawImage(img, x, y, negativeWidth, h, null) silently draws
        // nothing in Java2D.  Use a child Graphics2D with scale(-1,1) instead.
        Graphics2D sg = (Graphics2D) g2.create();
        if (facingRight) {
            sg.drawImage(frame, (int) x, (int) y, (int) width, (int) height, null);
        } else {
            // Translate so the right edge of the sprite is at the right place,
            // then flip the X axis so the image mirrors left-right.
            sg.translate((int) x + (int) width, (int) y);
            sg.scale(-1.0, 1.0);
            sg.drawImage(frame, 0, 0, (int) width, (int) height, null);
        }
        sg.dispose();

        // ── Health bar ───────────────────────────────────────────────────────────
        drawHealthBar(g2);
    }

    private void drawHealthBar(Graphics2D g2) {
        if (!alive || health >= maxHealth) return;

        int barW = (int) width;
        int barH = 5;
        int barX = (int) x;
        int barY = (int) y - 10;   // 10 px above the sprite top

        float ratio = Math.max(0f, health / maxHealth);

        // Background
        g2.setColor(new Color(40, 40, 40, 180));
        g2.fillRect(barX, barY, barW, barH);

        // Fill — colour shifts green → yellow → red
        Color fill;
        if      (ratio > 0.5f)  fill = new Color(60, 200,  60);
        else if (ratio > 0.25f) fill = new Color(220, 180, 30);
        else                    fill = new Color(200,  50, 50);
        g2.setColor(fill);
        g2.fillRect(barX, barY, (int)(barW * ratio), barH);

        // Border
        g2.setColor(Color.BLACK);
        g2.drawRect(barX, barY, barW, barH);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Damage / combat
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Inflict damage on this enemy.  Triggers the hurt flash and death if HP ≤ 0.
     */
    @Override
    public void takeDamage(float dmg) {
        if (!alive) return;
        health -= dmg;
        hitFlashTimer = HIT_FLASH;
        if (health <= 0f) {
            health = 0f;
            die();
        }
    }

    /**
     * Returns {@code true} on the exact tick the enemy's attack cooldown fires —
     * the Level loop reads this to apply damage to the player.
     */
    public boolean isReadyToAttack() {
        return alive
            && aiState == AIState.ATTACK
            && attackCooldown == ATK_CD;   // fires on the tick the CD resets
    }

    /** The damage value this enemy deals per attack / on contact. */
    public float getDamage() { return damage; }

    /**
     * Convenience overlap check.
     * @param other  a rectangle in world coordinates (e.g. player bounds)
     * @return true if alive and hitboxes intersect
     */
    public boolean overlaps(Rectangle other) {
        return alive && getBounds().intersects(other);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Death
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void die() {
        alive   = false;
        aiState = AIState.DEAD;
        velX    = 0f;
        switchAnim("death");
    }

    /**
     * Hook called by the Level after the death animation finishes, just before
     * the enemy is removed from the entity list.  Override or handle externally
     * for loot drops, score increments, sounds, etc.
     */
    @Override
    public void onDeath() { }

    /** @return true while the enemy is still participating in the game. */
    public boolean isAlive() { return alive; }

    /**
     * @return true once the enemy is dead AND the death animation has played
     *         through — safe to remove from the entity list.
     */
    public boolean isDeathAnimationFinished() {
        return !alive && currentAnim != null && currentAnim.isFinished();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Hitbox
    // ─────────────────────────────────────────────────────────────────────────────

    /** Collision rectangle aligned with the full visible sprite. */
    @Override
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, (int) width, (int) height);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Accessors
    // ─────────────────────────────────────────────────────────────────────────────

    public EnemiesType getType()     { return type;      }
    public float       getHealth()   { return health;    }
    public float       getMaxHealth(){ return maxHealth; }
    public boolean     isGrounded()  { return grounded;  }
    public AIState     getAIState()  { return aiState;   }
}