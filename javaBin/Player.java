import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

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
        velX = -5;
        facingRight = false;
    }
    
    public void moveRight() {
        velX = 5;
        facingRight = true;
    }
    
    public void stop() {
        velX = 0;
    }
    
    public void jump() {
        if (isGrounded) {
            velY = -12;
            isGrounded = false;
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
    
    public void die() {
        System.out.println("Game Over!");
        // Reset position or handle game over
    }
}