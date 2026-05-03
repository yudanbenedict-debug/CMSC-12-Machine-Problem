package Entities.EnemyFolder;

import Animators.EnemyAnimator;
import Animators.EnemyAnimator.SnapShot;
import Entities.LivingEntity;
import GamePlatform.Platform;
import DataLoader.EnemyDataLoader;
import DataLoader.EnemyDataLoader.EnemyData;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;

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
 */
public class Enemies extends LivingEntity {

    
    // private int stompCount = 0;
    // private final int maxStomps = 3;
    private boolean dying = false;
    // ── AI states ─────────────────────────────────────────────────────────────
    public enum AIState { PATROL, CHASE, ATTACK, DEAD }

    // ── Physics constants ─────────────────────────────────────────────────────
    private static final float GRAVITY        = 0.65f;
    private static final float MAX_FALL_SPEED = 20f;
    private static final float SNAP_TOL       = 6f;

    // ── AI constants ──────────────────────────────────────────────────────────
    private static final int MIN_IDLE  = 40;
    private static final int MAX_IDLE  = 120;
    private static final int ATK_CD    = 50;
    private static final int ATK_REACH = 55;
    private static final int HIT_FLASH = 12;

    // ── Identity ──────────────────────────────────────────────────────────────
    private final EnemiesType type;
    private final EnemyData   data;
    private final float       spawnX;
    private final float       maxHealth;
    private final Random      rng;

    // ── Physics state ─────────────────────────────────────────────────────────
    private float   velX, velY;
    private boolean grounded;
    private boolean facingRight;

    // ── AI state ──────────────────────────────────────────────────────────────
    private boolean alive = true;
    private AIState aiState;
    private int     idleTimer;
    private int     attackCooldown;
    private int     hitFlashTimer;

    // ── Animator ──────────────────────────────────────────────────────────────
    private final EnemyAnimator animator;

    // ─────────────────────────────────────────────────────────────────────────
    //  Construction
    // ─────────────────────────────────────────────────────────────────────────

    public Enemies(EnemiesType type, float x, float y) {
        this(type, x, y, EnemyDataLoader.get(type));
    }

    private Enemies(EnemiesType type, float x, float y, EnemyData data) {
        super(x, y,
              data.spriteWidth, data.spriteHeight,
              data.baseHealth,  data.baseDamage,
              data.baseSpeed,   data.baseSpeed);

        this.type      = type;
        this.data      = data;
        this.spawnX    = x;
        this.maxHealth = data.baseHealth;
        this.rng       = new Random((long)(type.ordinal() * 1_000_003L + (long)x * 31 + (long)y));

        this.alive          = true;
        this.aiState        = AIState.PATROL;
        this.facingRight    = rng.nextBoolean();
        this.velX           = 0f;
        this.velY           = 0f;
        this.idleTimer      = 0;
        this.attackCooldown = 0;
        this.hitFlashTimer  = 0;

        this.animator = new EnemyAnimator(type);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Public update  (called by Level every tick)
    // ─────────────────────────────────────────────────────────────────────────

    public void update(List<Platform> platforms, float playerX, float playerY) {
        if (!alive) {
            animator.updateAnim(buildSnapshot());
            return;
        }
        updateAI(playerX, playerY);
        applyPhysics();
        resolvePlatforms(platforms);
        updateTimers();
        animator.updateAnim(buildSnapshot());
    }

    /** Minimal overload used when no platform list is available. */
    @Override
    public void update() {
        if (!alive) return;
        applyPhysics();
        animator.updateAnim(buildSnapshot());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  AI
    // ─────────────────────────────────────────────────────────────────────────

    private void updateAI(float playerX, float playerY) {
        float cx   = x + width / 2f;
        float dx   = playerX - cx;
        float dist = Math.abs(dx);

        switch (aiState) {

            case PATROL:
                if (dist <= data.aggroRange) {
                    aiState   = AIState.CHASE;
                    idleTimer = 0;
                    break;
                }
                doPatrol(cx);
                break;

            case CHASE:
                if (dist > data.aggroRange * 1.3f) {
                    aiState   = AIState.PATROL;
                    idleTimer = randIdle();
                    velX      = 0f;
                    break;
                }
                if (dist <= ATK_REACH) {
                    aiState = AIState.ATTACK;
                    velX    = 0f;
                    break;
                }
                facingRight = dx > 0;
                velX = facingRight ? walk_speed : -walk_speed;
                break;

            case ATTACK:
                velX = 0f;
                if (attackCooldown <= 0) attackCooldown = ATK_CD;
                if (dist > ATK_REACH + 10) {
                    aiState = (dist <= data.aggroRange) ? AIState.CHASE : AIState.PATROL;
                }
                break;

            case DEAD:
                velX = 0f;
                break;
        }
    }

    private void doPatrol(float cx) {
        if (idleTimer > 0) {
            velX = 0f;
            return;
        }
        float half = data.patrolRange;
        if (cx < spawnX - half) {
            facingRight = true;
            velX        =  walk_speed;
        } else if (cx > spawnX + half) {
            facingRight = false;
            velX        = -walk_speed;
        } else {
            velX = facingRight ? walk_speed : -walk_speed;
        }
    }

    private void bounce() {
        facingRight = !facingRight;
        velX        = facingRight ? walk_speed : -walk_speed;
        idleTimer   = MIN_IDLE / 2 + rng.nextInt(MIN_IDLE / 2);
    }

    private int randIdle() {
        return MIN_IDLE + rng.nextInt(MAX_IDLE - MIN_IDLE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Physics
    // ─────────────────────────────────────────────────────────────────────────

    private void applyPhysics() {
        x += velX;
        if (!grounded) {
            velY += GRAVITY;
            if (velY > MAX_FALL_SPEED) velY = MAX_FALL_SPEED;
        }
        y       += velY;
        grounded = false;
    }
    
    // public void registerStomp() {
    //     if (dying) return;

    //     stompCount++;

    //     if (stompCount >= maxStomps) {
    //         dying = true;
    //         setAlive(false); // stop normal behavior
            
    //     }
    // }
    public boolean isDying() {
        return dying;
    }
    // ─────────────────────────────────────────────────────────────────────────
    //  Platform collision
    // ─────────────────────────────────────────────────────────────────────────

    private void resolvePlatforms(List<Platform> platforms) {
        for (Platform plat : platforms) {
            Rectangle pb = plat.getBounds();

            float pLeft   = pb.x,          pRight  = pb.x + pb.width;
            float pTop    = pb.y,           pBottom = pb.y + pb.height;
            float myLeft  = x,              myRight  = x + width;
            float myTop   = y,              myBottom = y + height;

            boolean hOverlap = myRight > pLeft  && myLeft < pRight;
            boolean vOverlap = myBottom > pTop  && myTop  < pBottom;

            // Land on top
            if (hOverlap && velY >= 0
                    && myBottom >= pTop
                    && myBottom - velY <= pTop + SNAP_TOL) {
                y        = pTop - height;
                velY     = 0f;
                grounded = true;
                continue;
            }

            if (!hOverlap || !vOverlap) continue;

            // Ceiling
            if (velY < 0 && myTop <= pBottom && myTop - velY >= pBottom - SNAP_TOL) {
                y    = pBottom;
                velY = 0f;
                continue;
            }

            // Left wall
            if (velX > 0 && myRight > pLeft && myRight - velX <= pLeft + SNAP_TOL) {
                x    = pLeft - width;
                velX = 0f;
                bounce();
                continue;
            }

            // Right wall
            if (velX < 0 && myLeft < pRight && myLeft - velX >= pRight - SNAP_TOL) {
                x    = pRight;
                velX = 0f;
                bounce();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Timers
    // ─────────────────────────────────────────────────────────────────────────

    private void updateTimers() {
        if (idleTimer      > 0) idleTimer--;
        if (attackCooldown > 0) attackCooldown--;
        if (hitFlashTimer  > 0) hitFlashTimer--;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Snapshot builder
    // ─────────────────────────────────────────────────────────────────────────

    private SnapShot buildSnapshot() {
        return new SnapShot(
            alive, hitFlashTimer, aiState, attackCooldown, velX
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Drawing
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void draw(Graphics g) {
        animator.draw(g, (int) x, (int) y, (int) width, (int) height,
                      facingRight, hitFlashTimer);
        if (g instanceof Graphics2D) {
            drawHealthBar((Graphics2D) g);
        }
    }

    private void drawHealthBar(Graphics2D g2) {
        if (!alive || health >= maxHealth) return;

        int   barW  = (int) width;
        int   barH  = 5;
        int   barX  = (int) x;
        int   barY  = (int) y - 10;
        float ratio = Math.max(0f, health / maxHealth);

        g2.setColor(new Color(40, 40, 40, 180));
        g2.fillRect(barX, barY, barW, barH);

        Color fill;
        if      (ratio > 0.5f)  fill = new Color(60,  200,  60);
        else if (ratio > 0.25f) fill = new Color(220, 180,  30);
        else                    fill = new Color(200,  50,  50);
        g2.setColor(fill);
        g2.fillRect(barX, barY, (int)(barW * ratio), barH);

        g2.setColor(Color.BLACK);
        g2.drawRect(barX, barY, barW, barH);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Combat
    // ─────────────────────────────────────────────────────────────────────────

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
     * Returns true on the exact tick the attack cooldown fires.
     * Level reads this to apply damage to the player.
     */
    public boolean isReadyToAttack() {
        return alive && aiState == AIState.ATTACK && attackCooldown == ATK_CD;
    }

    public float getDamage() { return damage; }

    public boolean overlaps(Rectangle other) {
        return alive && getBounds().intersects(other);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Death
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void die() {
        alive   = false;
        aiState = AIState.DEAD;
        velX    = 0f;
    }

    @Override
    public void onDeath() { }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /** True once dead AND the death animation has fully played through. */
    public boolean isDeathAnimationFinished() {
        return !alive && animator.isFinished();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Hitbox
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, (int) width, (int) height);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Accessors
    // ─────────────────────────────────────────────────────────────────────────

    public EnemiesType getType()      { return type;      }
    public float       getHealth()    { return health;    }
    public float       getMaxHealth() { return maxHealth; }
    public boolean     isGrounded()   { return grounded;  }
    public AIState     getAIState()   { return aiState;   }
}