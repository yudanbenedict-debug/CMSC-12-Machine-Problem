package Entities;

import Animators.PlayerAnimator;
import Animators.PlayerAnimator.StateSnapshot;
import Weapons.*;

import java.awt.Graphics;
import java.awt.Rectangle;

public class Player extends LivingEntity {
    // private static final int SPRITE_DRAW_Y_OFFSET = 48;
    // private static final int HITBOX_OFFSET_X = 50;   // trim 28px from each side
    // private static final int HITBOX_WIDTH    = 112 - (HITBOX_OFFSET_X * 2); 

    // private static final int HITBOX_OFFSET_Y = SPRITE_DRAW_Y_OFFSET; // 48 px from top of entity
    // private static final int HITBOX_HEIGHT   = 120; // remaining 120 px
    
    private static final int ATTACK_ANIMATION_FRAMETIMES = 4;
    private static final int ATTACK_ANIMATION_FRAMES = 5;
    private static final int ATTACK_ANIM_DURATION = ATTACK_ANIMATION_FRAMES * ATTACK_ANIMATION_FRAMETIMES;

    private static final int NORMAL_WIDTH = 32;
    private static final int NORMAL_HEIGHT = 48;
    //increase width when shooting
    private static final int SHOOT_WIDTH = 40;

    private static final float SPRINT_MULTIPILIER = 1.6F;

    private int attackAnimTimer = 0;
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

    //
    private Gun gun = new Gun();
    private Sword sword = new Sword();
    private boolean attackPressedLastFrame = false;

    private int slot = 1; // slot is 1/2 (1 for gun, 2 for sword... TODO: make sure gun ammo is not infinite).
    private PlayerAnimator animations;
   
    //fancy-schmancy stuff
    private static final int JUMP_BUFFER_FRAMES = 8;
    private static final int COYOTE_FRAMES = 6;
    private int jumpBufferFrames;
    private int coyoteFrames;

    public Player(float velX, float velY, float x, float y) {
        super(x, y, NORMAL_WIDTH, NORMAL_HEIGHT, 20.0f, 5.0f, 3.0f, 14.0f);
        this.velX = velX;
        this.velY = velY;
        this.goldCount = 0;
        this.score = 0;
        this.sprinting = false;
        this.isJumping = false;
        this.gravity = 0.65f;
        this.jumpBufferFrames = 0;
        this.coyoteFrames = 0;
        this.animations = new PlayerAnimator(NORMAL_WIDTH, NORMAL_HEIGHT);
    }

    
    public void jump() {
        isJumping = true;
        jumpBufferFrames = JUMP_BUFFER_FRAMES;
    }
    public void sprint(){
        if(isGrounded);
    }
    public void attack(){

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

        // if(currentState.equals("sword_attack")){
        //     width = SHOOT_WIDTH;
        // }
        // else{
        //     width = NORMAL_WIDTH;
        // }
        gun.tick();
        sword.tick();
        
        if (attackAnimTimer > 0) attackAnimTimer--;
        updateAnimation();
    }

    private void updateAnimation() {
        //use the inner class snapshot
        PlayerAnimator.StateSnapshot snapshot = new StateSnapshot(isGrounded, velX, velY, sprinting, attackAnimTimer, slot);
        animations.update(snapshot);
     
    }

    @Override
    public void draw(Graphics g) {
        animations.draw(g, (int) x, (int) y, (int) width, (int) height, facingRight);
    }
    
    // public int getHitboxOffsetY() {
    //     return HITBOX_OFFSET_Y;
    // }

    // public int getHitboxHeight() {
    //     return HITBOX_HEIGHT;
    // }

    
    @Override
    public Rectangle getBounds() {
       //dbugging

        return new Rectangle(
            (int) x,
            (int) y,
            (int) width,
            (int) height
        );
    }




    public boolean isGrounded() { return isGrounded; }

    public float getHealth() { return health; }

    public float getVerticalVelocity() { return velY; }
    public void  setVerticalVelocity(float velY) { this.velY = velY; }
    
    public Gun getGun(){ return gun; }
    public Sword getSword(){return sword;}
    public int get_activeslots(){return slot;};

    public void setGrounded(boolean grounded) { this.isGrounded = grounded; }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }
    //VERY IMPORTANT--------------------
    public void applyEngineState(boolean moveLeft, boolean moveRight, boolean jumpPressed, boolean sprintPressed, boolean attackPressed, int weaponSlot, boolean reloadPressed ) {
        this.movingLeft  = moveLeft;
        this.movingRight = moveRight;
        if (jumpPressed) jump();
        this.sprinting = sprintPressed;

        if(weaponSlot == 1 || weaponSlot == 2){
            slot = weaponSlot;
        }
        if(reloadPressed && slot == 1){
            gun.reload();
        }
        if(attackPressed && !attackPressedLastFrame){
            tryAttack();
        }

        attackPressedLastFrame = attackPressed;
    }
    public void tryAttack(){
        Rectangle atk_rect = getBounds();
        boolean fired;
        float hx = atk_rect.x, hy = atk_rect.y, hw = atk_rect.width, hh = atk_rect.height;
        if(slot == 1){
            fired = gun.tryAttack(hx, hy, hw, hh, facingRight);
        }
        else{
            fired = sword.tryAttack(hx, hy, hw, hh, facingRight);
        }

        if(fired){
            attackAnimTimer = ATTACK_ANIM_DURATION;
        }
    }
    //END OF VERY IMPORTANT STUFF;
    @Override
    public void onDeath() { 
        
    }
}