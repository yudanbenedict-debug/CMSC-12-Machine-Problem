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

public class Enemies extends LivingEntity {

    
    private boolean dying = false;
    
    public enum AIState { PATROL, ALERT, CHASE, ATTACK, DEAD }

   
    private static final float GRAVITY        = 0.65f;
    private static final float MAX_FALL_SPEED = 20f;
    private static final float SNAP_TOL       = 6f;

    
    private static final int   MIN_IDLE        = 40;
    private static final int   MAX_IDLE        = 120;
    private static final int   ATK_CD          = 50;
    private static final int   ATK_REACH       = 30;
    private static final int   HIT_FLASH       = 12;
    private static final float CHASE_MULTIPLIER = 1.6f;

   //stats
    private final EnemiesType type;
    private final EnemyData   data;
    private final float       spawnX;
    private final float       maxHealth;
    private final Random      rng;

    //movement
    protected float   velX, velY;
    protected boolean grounded;
    protected boolean facingRight;

    //states for animation and loaders
    private boolean alive = true;
    private AIState aiState;
    private int     idleTimer;
    private int     alertTimer;
    private int     attackCooldown;
    private int     hitFlashTimer;

    
    private final EnemyAnimator animator;



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
        this.alertTimer     = 0;
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
        updateAI(platforms, playerX, playerY);
        applyPhysics();
        resolvePlatforms(platforms);
        updateTimers();
        animator.updateAnim(buildSnapshot());
    }

    @Override
    public void update() {
        if (!alive) return;
        applyPhysics();
        animator.updateAnim(buildSnapshot());
    }


    private void updateAI(List<Platform> platforms, float playerX, float playerY) {
        float cx   = x + width  / 2f;
        float cy   = y + height / 2f;
        float dx   = playerX - cx;
        float dy   = playerY - cy;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        switch (aiState) {

            case PATROL:
                if (dist <= data.aggroRange) {
                    aiState    = AIState.ALERT;
                    alertTimer = data.alertFrameCount * 4;
                    velX       = 0f;
                    facingRight = dx > 0;
                    break;
                }
                if (edgeAhead(platforms)) {
                    bounce();
                    break;
                }
                doPatrol(cx);
                break;

            case ALERT:
                velX = 0f;
                facingRight = dx > 0;
                if (alertTimer <= 0) {
                    aiState = AIState.CHASE;
                }
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
                if (edgeAhead(platforms)) {
                    velX = 0f;
                    break;
                }
                float chaseSpeed = walk_speed * CHASE_MULTIPLIER;
                velX = facingRight ? chaseSpeed : -chaseSpeed;
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

    private boolean edgeAhead(List<Platform> platforms) {
        if (!grounded) return false;
        float stepX  = facingRight ? (x + width + 2) : (x - 2);
        float footY  = y + height + 4;
        for (Platform plat : platforms) {
            Rectangle pb = plat.getBounds();
            if (stepX >= pb.x && stepX <= pb.x + pb.width
                    && footY >= pb.y && footY <= pb.y + pb.height + 8) {
                return false;
            }
        }
        return true;
    }

    //flip when hitting wall
    private void bounce() {
        facingRight = !facingRight;
        velX        = facingRight ? walk_speed : -walk_speed;
        idleTimer   = MIN_IDLE / 2 + rng.nextInt(MIN_IDLE / 2);
    }

    private int randIdle() {
        return MIN_IDLE + rng.nextInt(MAX_IDLE - MIN_IDLE);
    }


    //movement and gravity
    protected void applyPhysics() {
        x += velX;
        if (!grounded) {
            velY += GRAVITY;
            if (velY > MAX_FALL_SPEED) velY = MAX_FALL_SPEED;
        }
        y       += velY;
        grounded = false;
    }
        public boolean isDying() {
        return dying;
    }
  

    protected void resolvePlatforms(List<Platform> platforms) {

        for (Platform plat : platforms) {
            Rectangle pb = plat.getBounds();
            //get platform (p) bounds, then enemy (my) bounds.

            float pLeft   = pb.x, pRight  = pb.x + pb.width;
            float pTop    = pb.y, pBottom = pb.y + pb.height;
            float myLeft  = x, myRight  = x + width;
            float myTop   = y, myBottom = y + height;

            
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

            // Ceiling (if only the enemy can jump)
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

    //
    private void updateTimers() {
        if (idleTimer      > 0) idleTimer--;
        if (alertTimer     > 0) alertTimer--;
        if (attackCooldown > 0) attackCooldown--;
        if (hitFlashTimer  > 0) hitFlashTimer--;
    }

    //back to animator
    private SnapShot buildSnapshot() {
        return new SnapShot(
            alive, hitFlashTimer, aiState, attackCooldown, velX, alertTimer
        );
    }


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


    public boolean isReadyToAttack() {
        return alive && aiState == AIState.ATTACK && attackCooldown == ATK_CD;
    }

    private float damageMultiplier = 1.0f;

    public void setDamageMultiplier(float multiplier) {
        this.damageMultiplier = multiplier;
    }

    public float getDamage() { return damage * damageMultiplier; }

    public boolean overlaps(Rectangle other) {
        return alive && getBounds().intersects(other);
    }
    @Override
    public void die() {
        alive   = false;
        aiState = AIState.DEAD;
        velX    = 0f;
    }

    @Override
    public void onDeath() { } //empty forgot to use

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    // True once dead AND the death animation has fully played through.
    public boolean isDeathAnimationFinished() {
        return !alive && animator.isFinished();
    }

    //using cachebounds here aswell
    @Override
    public Rectangle getBounds() {
        cachedBounds.setBounds((int) x, (int) y, (int) width, (int) height);
        return cachedBounds;
    }

    public EnemiesType getType()      { return type;      }
    public float       getHealth()    { return health;    }

    //set health for enemy manager
    public void setHealth(float value) {
        this.health = Math.max(0, Math.min(value, maxHealth));
    }
    public float       getMaxHealth() { return maxHealth; }
    public boolean     isGrounded()   { return grounded;  }
    public AIState     getAIState()   { return aiState;   }
}