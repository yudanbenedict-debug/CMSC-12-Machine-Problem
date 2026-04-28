package Entities;

import SpriteLoading.SpriteLoader;

import java.awt.Color;
import Weapons.*;
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
    private static final int HITBOX_OFFSET_X = 50;   // trim 28px from each side
    private static final int HITBOX_WIDTH    = 112 - (HITBOX_OFFSET_X * 2); 


    // ── Hitbox constants ─────────────────────────────────────────────────────────
    // The sprite sheet is 112×168 px but the visible character occupies only
    // the lower portion.  SPRITE_DRAW_Y_OFFSET (48) pushes the image down when
    // drawing, so the logical hitbox starts 48 px into the sprite height.
    // These values are exposed via getHitboxOffsetY() / getHitboxHeight() so
    // Level.java can correctly snap the player onto platforms.
    private static final int HITBOX_OFFSET_Y = SPRITE_DRAW_Y_OFFSET; // 48 px from top of entity
    private static final int HITBOX_HEIGHT   = 120; // remaining 120 px

    private static final float SPRINT_MULTIPILIER = 1.6F;

    private boolean facingRight = true;

    private boolean movingLeft;
    private boolean movingRight;
    private boolean sprinting;
    protected boolean isGrounded;
    private boolean isJumping;

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
    private BufferedImage[] sprintingFrames;
    private BufferedImage[] rollingFrames;
    private BufferedImage[] sword_attack_frames;
    private BufferedImage[] shootingFrames;

    private Animation currentAnimation;
    //coyote frames 
    private static final int JUMP_BUFFER_FRAMES = 8;
    private static final int COYOTE_FRAMES = 6;
    private int jumpBufferFrames;
    private int coyoteFrames;

    public Player(float velX, float velY, float x, float y) {
        super(x, y, 112, 168, 20.0f, 5.0f, 3.0f, 14.0f);
        this.velX = velX;
        this.velY = velY;
        this.goldCount = 0;
        this.score = 0;
        this.sprinting = false;
        this.isJumping = false;
        this.gravity = 0.65f;
        this.jumpBufferFrames = 0;
        this.coyoteFrames = 0;
        initialize();
    }

    private void initialize() {
        BufferedImage sharedFrame = SpriteLoader.loadWalkBaseFrame((int) width, (int) height);

        walkFrames     = SpriteLoader.loadImages("Player-Sprites/Player-Walk", "character_walk", 8, sharedFrame);
        idleFrames     = SpriteLoader.loadImages("Player-Sprites/Player-Idle", "character_idle-", 6, sharedFrame);
        jumpFrames     = SpriteLoader.loadImages("Player-Sprites/Player-Jump", "character_jump", 4, sharedFrame);
        fallFrames     = SpriteLoader.loadImages("Player-Sprites/Player-Fall", "character_jump", 1, sharedFrame);
        sprintingFrames = SpriteLoader.loadImages("Player-Sprites/Player-Run", "character_run", 8, sharedFrame);
        sword_attack_frames = SpriteLoader.loadImages("Player-Sprites/Player-Sword", "SwordAttack", 6, sharedFrame);
        rollingFrames  = new BufferedImage[]{ sharedFrame };

        animations.put("walk",      new Animation(walkFrames,      8,  true));
        animations.put("idle",      new Animation(idleFrames,      6, true));
        animations.put("jump",      new Animation(jumpFrames,      4,  false));
        animations.put("fall",      new Animation(fallFrames,      1,  true));
        animations.put("rolling",   new Animation(rollingFrames,   8,  false));
        animations.put("sword_attack", new Animation(sword_attack_frames, 2, false));
        animations.put("shooting", new Animation(shootingFrames, 2, false));
        animations.put("sprint", new Animation(sprintingFrames, 6, true));
        currentAnimation = animations.get("idle");
    }

    public void jump() {
        isJumping = true;
        jumpBufferFrames = JUMP_BUFFER_FRAMES;
    }
    public void sprint(){
        if(isGrounded);

    }

    @Override
    public void update() {
        float currentSpeed;

        if (sprinting) {
                currentSpeed = walk_speed * SPRINT_MULTIPILIER;
        } else {
            currentSpeed = walk_speed;
        }
        

        velX = 0;
        if (movingLeft)  { velX = -currentSpeed; facingRight = false; }
        if (movingRight) { velX =  currentSpeed; facingRight = true;  }
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

        //debug
        //end of debug
        String curState = "idle";
        if (!isGrounded) {
            curState = (velY < 0) ? "jump" : "fall";
        } else if (sprinting && Math.abs(velX) > 0.5f) {
            curState = "sprint";
        } 
        else if (Math.abs(velX) > 0.5f) {
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
    
        int drawX = (int) x;
        int drawWidth = (int) width;
    
        if (!facingRight) {
            drawX += drawWidth;
            drawWidth = -drawWidth;
        }
    //FOR DEBUGGING
        int drawY = (int) y + SPRITE_DRAW_Y_OFFSET;
        g.drawImage(frame, drawX, drawY, drawWidth, (int) height, null);
    
   
        Rectangle hitbox = getBounds();
        g.setColor(Color.RED);
        g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
    
        
        g.setColor(Color.GREEN);
        g.fillOval((int)x - 3, (int)y - 3, 6, 6);
    
        g.setColor(Color.BLUE);
        g.fillOval(drawX - 3, drawY - 3, 6, 6);
    //END OF DEBUGGING
    }
    
    public int getHitboxOffsetY() {
        return HITBOX_OFFSET_Y;
    }

    public int getHitboxHeight() {
        return HITBOX_HEIGHT;
    }

    
    @Override
    public Rectangle getBounds() {
        int shrinkTop = 40;     //dbugging

        return new Rectangle(
            (int) x + HITBOX_OFFSET_X,
            (int) y + HITBOX_OFFSET_Y + shrinkTop,
            HITBOX_WIDTH,
            HITBOX_HEIGHT - (shrinkTop)
        );
    }



    // ── Getters / setters ────────────────────────────────────────────────────────

    public boolean isGrounded() { return isGrounded; }

    public float getHealth() { return health; }

    public float getVerticalVelocity() { return velY; }
    public void  setVerticalVelocity(float velY) { this.velY = velY; }
    public int getHitboxOffsetX() { return HITBOX_OFFSET_X; }
    public int getHitboxWidth()   { return HITBOX_WIDTH; }


    public void setGrounded(boolean grounded) { this.isGrounded = grounded; }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }
    public void applyEngineState(boolean moveLeft, boolean moveRight, boolean jumpPressed, boolean sprintPressed) {
        this.movingLeft  = moveLeft;
        this.movingRight = moveRight;
        if (jumpPressed) jump();
        this.sprinting = sprintPressed;
    }
    //item logic still not finished
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
    //sprite loading
    //only walkanimation for now since the other sprites are still not uploaded
    //for walk frames since it is pretty necessary

    @Override
    public void onDeath() { 
        
    }
}