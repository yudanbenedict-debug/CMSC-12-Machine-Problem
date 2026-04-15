import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.util.HaspMap;
import java.util.Map;

public class Player extends Entity {
    // Player stats
    private double health;
    private int lives;
    private int coins;
    private int score;
    private double damage;

    // Animation
    private int a_time;
    private int a_frame;
    private boolean walkingAnimation;
    
    // Sprites for player
    private BufferedImage sp_left_S;
    private BufferedImage sp_left_w1;
    private BufferedImage sp_left_w2;
    private BufferedImage sp_right_S;
    private BufferedImage sp_right_w1;
    private BufferedImage sp_right_w2;
    
    // Current sprite
    private BufferedImage c_sp;
    private boolean sp_loading;
    private boolean facingRight;  // Track which direction player is facing

    // Effect Tracking
    private Map<Item.EffectType, Integer> activeEffects = new HaspMap<>();
    private int baseSpeed = 5;
    private int currentSpeed = 5;
    private boolean hasDoubleJump = false;
    private boolean canDoubleJump = false;

    
    public Player(int x, int y) {
        super(x, y, 30, 40); 

        health = 10;
        lives = 3;
        coins = 0;
        score = 0;
        damage = 2;
        
        this.a_frame = 10;
        this.a_time = 0;
        this.walkingAnimation = true;
        this.facingRight = true;
        
        sp_loading = true;
        loadSprites();
        
        c_sp = sp_right_S;
    }
    
    // Load sprites
    public void loadSprites() {
        try {
            sp_left_S = ImageIO.read(getClass().getResource("/sp_left_S.png"));
            sp_left_w1 = ImageIO.read(getClass().getResource("/sp_left_w1.png"));
            sp_left_w2 = ImageIO.read(getClass().getResource("/sp_left_w2.png"));
            sp_right_S = ImageIO.read(getClass().getResource("/sp_right_S.png"));
            sp_right_w1 = ImageIO.read(getClass().getResource("/sp_right_w1.png"));
            sp_right_w2 = ImageIO.read(getClass().getResource("/sp_right_w2.png"));
            sp_loading = false;  // Set to false after successful load
            c_sp = sp_right_S;
        } catch (Exception e) {
            System.out.println("Failed to load sprites: " + e.getMessage());
            sp_loading = false;
        }
    }
    
    @Override
    public void update() {
        updateEffects(); // update buff and debuff mechanic
        applyGravity();
        checkIfGrounded(550);  // Pass ground Y level
        x += velX;
        
        // Keep on screen
        if (x < 0) x = 0;
        if (x + width > 800) x = 800 - width;
        
        // Update animation based on movement
        updateAnimation();
    }
    
    private void updateAnimation() {
        // Check if moving
        if (velX != 0 && isGrounded) {
            a_time++;
            if (a_time >= a_frame) {
                a_time = 0;
                walkingAnimation = !walkingAnimation;
                
                // Set walking sprite based on direction
                if (velX > 0) {
                    facingRight = true;
                    if (walkingAnimation) {
                        c_sp = sp_right_w1;
                    } else {
                        c_sp = sp_right_w2;
                    }
                } else if (velX < 0) {
                    facingRight = false;
                    if (walkingAnimation) {
                        c_sp = sp_left_w1;
                    } else {
                        c_sp = sp_left_w2;
                    }
                }
            }
        } else {
            // Not moving - use standing sprite
            if (facingRight) {
                c_sp = sp_right_S;
            } else {
                c_sp = sp_left_S;
            }
            a_time = 0;
        }
    }
    
    @Override
    public void draw(Graphics g) {
        if (c_sp != null) {
            g.drawImage(c_sp, x, y, width, height, null);
        } else {
            // Fallback rectangle
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
        }
    }
    
    // Movement methods
    public void moveLeft() {
        velX = -currentSpeed;
        facingRight = false;
    }
    
    public void moveRight() {
        velX = currentSpeed;
        facingRight = true;
    }
    
    public void stop() {
        velX = 0;
    }
    
    public void jump() {
        if (isGrounded) {
            velY = -14;
            isGrounded = false;
            canDoubleJump = true; // allows doubke jump after the first jump
        } else if (hasDoubleJump && canDoubleJump){
            velY = -14;       // force of double jump is the same as noraml jump
            canDoubleJump = false;
        }
    }
    
    //Getters and setters
    public double getHealth() { return health; }
    public void setHealth(double health) { this.health = health; }
    
    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }
    
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = damage; }
    
    public void addCoins(int amount) {
        this.coins += amount;
        this.score += amount * 100;
    }
    
    public void takeDamage(double amount) {
        this.health -= amount;
        if (this.health <= 0) {
            this.lives--;
            this.health = 10;
            if (this.lives <= 0) {
                die();
            }
        }
    }

    public void applyBuff(Item.EffectType type, int duration) {
        int frames = (duration == 0) ? Integer.MAX_VALUE : duration * 60;
        activeEffects.put(type, frames);
    
        if (type == Item.EffectType.SPEED_BOOST) {
            currentSpeed = baseSpeed * 2;
        } else if (type == Item.EffectType.DOUBLE_JUMP) {
            hasDoubleJump = true;
        } else if (type == Item.EffectType.INVINCIBILITY) {
            // to add soon...
        }
}

    public void applyDebuff(Item.EffectType type, int duration) {
        int frames = (duration == 0) ? Integer.MAX_VALUE : duration * 60;
        activeEffects.put(type, frames);
        
        if (type == Item.EffectType.DEBUFF_SLOWNESS) {
            currentSpeed = baseSpeed / 2;
        } else if (type == Item.EffectType.DEBUFF_REVERSE_CONTROLS) {
            // to add soon...
        }
}

    private void updateEffects() {
    // Use a copy of entry set to avoid concurrent modification
    for (Map.Entry<Item.EffectType, Integer> entry : new HashMap<>(activeEffects).entrySet()) {
        Item.EffectType type = entry.getKey();
        int remaining = entry.getValue();
        
        if (remaining == Integer.MAX_VALUE) {
            continue; // permanent effect stays
        }
        
        if (remaining <= 1) {
            revertEffect(type);
            activeEffects.remove(type);
        } else {
            activeEffects.put(type, remaining - 1);
        }
}

    private void revertEffect(Item.EffectType type) {
        if (type == Item.EffectType.SPEED_BOOST) {
            currentSpeed = baseSpeed;
        } else if (type == Item.EffectType.DOUBLE_JUMP) {
            hasDoubleJump = false;
        } else if (type == Item.EffectType.DEBUFF_SLOWNESS) {
            currentSpeed = baseSpeed;
        } else if (type == Item.EffectType.DEBUFF_REVERSE_CONTROLS) {
            // to add soon...
        }
    
    public void die() {
        System.out.println("Game Over!");
        //added here for no reason
    }
}
