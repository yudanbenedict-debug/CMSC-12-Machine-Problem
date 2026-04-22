import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import java.awt.Graphics;
import java.awt.Graphics2D;

/*
    spire loading: 
     sp_left_S = ImageIO.read(getClass().getResource("/sp_left_S.png"));
            sp_left_w1 = ImageIO.read(getClass().getResource("/sp_left_w1.png"));
            sp_left_w2 = ImageIO.read(getClass().getResource("/sp_left_w2.png"));
            sp_right_S = ImageIO.read(getClass().getResource("/sp_right_S.png"));
            sp_right_w1 = ImageIO.read(getClass().getResource("/sp_right_w1.png"));
            sp_right_w2 = ImageIO.read(getClass().getResource("/sp_right_w2.png"));
*/  

//STILL NO CATCHER HERE ADD AN EXCEPTION CLASS PLEASE!!!!!!!!!!!!!!!!

public class Player extends LivingEntity{

    private boolean facingRight = true;

    private boolean movingLeft;
    private boolean movingRight;
    private boolean movingUp;
    private boolean movingDown;

    private boolean isJumping;
    private boolean running;
    protected boolean isGrounded;

    //attacking time will be in ms.
    private boolean isAttacking;
    private long lastAttackTime;
    private int attackCoolDown;
    
    private float velX;
    private float velY;
    private float gravity;

    private int score;
    private int goldCount;

    //subject to changes for animation

    private HashMap<String, Animation> animations = new HashMap<>();
    private String currentState = "idle";
    // private Animation walkingAnimation;
    // private Animation idleAnimation;
    // private Animation jumpAnimation;
    // private Animation fallAnimation;
    // private Animation rollingAnimation;
    // private Animation ledgegrabAnimation;
    private BufferedImage[] walkFrames;
    private BufferedImage[] idleFrames;
    private BufferedImage[] jumpFrames;
    private BufferedImage[] fallFrames;
    private BufferedImage[] rollingFrames;
    private BufferedImage[] ledgegrabFrames;

    private Animation currentAnimation;

    public Player(float velX, float velY, float x, float y){
        super(x, y, 32, 48, 20.0f, 5.0f, 20.0f, 10.0f);
        this.velX = velX;
        this.velY = velY;
        this.goldCount = 0;
        this.score = 0;
        this.attackCoolDown = 400; //400ms
        this.isAttacking = false;
        this.running = false;
        this.isJumping = false;
        this.gravity = 0.75f;
        initialize();
        //initalize animation here


    }

    private void initialize(){
         // private Animation walkingAnimation;
        // private Animation idleAnimation;
        // private Animation jumpAnimation;
        // private Animation fallAnimation;
        // private Animation rollingAnimation;
        // private Animation ledgegrabAnimation;
        try{
        walkFrames = new BufferedImage[8];
        walkFrames[0] = ImageIO.read(getClass().getResource("Resources/00_character_walk.png"));
        walkFrames[1] = ImageIO.read(getClass().getResource("Resources/01_character_walk.png"));
        walkFrames[2] = ImageIO.read(getClass().getResource("Resources/02_character_walk.png"));
        walkFrames[3] = ImageIO.read(getClass().getResource("Resources/03_character_walk.png"));
        walkFrames[4] = ImageIO.read(getClass().getResource("Resources/04_character_walk.png"));
        walkFrames[5] = ImageIO.read(getClass().getResource("Resources/05_character_walk.png"));
        walkFrames[6] = ImageIO.read(getClass().getResource("Resources/06_character_walk.png"));
        walkFrames[7] = ImageIO.read(getClass().getResource("Resources/07_character_walk.png"));

        idleFrames = new BufferedImage[2];

        jumpFrames = new BufferedImage[3];
        fallFrames = new BufferedImage[3];
        rollingFrames = new BufferedImage[3];
        ledgegrabFrames = new BufferedImage[3];

        }catch(IOException e){
            e.printStackTrace();
            System.out.print("Broken stuff");
        }

        animations.put("walk", new Animation(walkFrames, 6, true));
        animations.put("idle", new Animation(idleFrames, 6, true));
        animations.put("jump", new Animation(jumpFrames, 6, false));
        animations.put("fall", new Animation(fallFrames, 6, true));
        animations.put("rolling", new Animation(rollingFrames, 8, false));
        animations.put("ledgegrab", new Animation(ledgegrabFrames, 8, false));

        
    }
    //sprite loading
    
    public void jump(){
        isJumping = true;
    }
    //gravity logic
    @Override
    public void update(){
        //passing movement logic (if moving left or right)
        if(movingLeft){ velX = -walk_speed; facingRight = false;}
        if(movingRight){ velX = walk_speed; facingRight = true;}
        x += velX;

        //apply gravity
        velY += gravity;
        if(velY >= 20){
            velY = 20;
        }
        y += velY;

        if(isGrounded && isJumping){
            velY = jump_height;
            isGrounded = false;
        }
        isJumping = false;

        isGrounded = false;

        updateAnimation();


    }

    private void updateAnimation(){
        String curState = "idle";
        //check if grounded
        if(!isGrounded){
            curState = (velX < 0) ? "jump" : "fall";
        }
        //if moving 
        else if(Math.abs(velX) > 0.5f){
            curState = "walk";
        }
        //compare
        if(!curState.equals(currentState)){
            currentState = curState;
            currentAnimation = animations.get(curState);


        }
    }

    @Override
    public void draw(Graphics g){
        
        if(currentAnimation == null) return;
        
        BufferedImage frame = currentAnimation.getCurrentFrame();
        if (frame == null) return;

        int drawX = (int)x;
        int drawWidth = (int)width;
        //change the orientation
        if(!facingRight){
            drawX += drawWidth;
            drawWidth = -drawWidth;
        }

        g.drawImage(frame, drawX, (int)y, drawWidth, (int)height, null);
        ////walking sprites; 3 frames, withing 60 frames, 5 full rotations.
        //falling sprites; 3 frames, 2 frames for landing, 1 frame for raising arms. 

        //
    }
    //getters and setters.
    public boolean isGrounded(){
        return isGrounded;
    }
    @Override
    public void onDeath(){
        
    }



}