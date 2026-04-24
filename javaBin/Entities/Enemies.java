
package Entities;

import java.awt.Graphics;

public abstract class Enemies extends LivingEntity{
    //stats
        private int health;
        private int damage;
        private int movementPattern; // right = 1, left = -1
        private int leftBound, rightBound;
        private boolean isActive = true;
        private boolean running;
        private boolean isGrounded;
        private float gravity = 0.75f;

        //Animation subject to changes same as player
        // protected HashMap<String, Animation> enemy_animations = new HashMap<>
        // protected Animation currentAnimation;
        //protected String currentState = "walk";

    public Enemies(float x, float y, int movementPattern, int leftBound, int rightBound) {
        super(x, y, 10, 10, 10, 10, 5, 0); // jump_height = 0
        this.health = (int)health;
        this.damage = (int)damage;
        this.movementPattern = 1; // this makes is move to the right
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.gravity = 0.75f;
        this.running = false;
        initialize();
    }   

    private void initialize() {
        //enemy animation...
    }

    @Override
    public void update() { // this the movement stuff aight
       /*  if(!isActive) return;

        // 1. Patrol movement 

        x += walk_speed *  movementPattern;
        if(x >= rightBound) { 
            movementPattern = -1;
            x = rightBound;
        } else if (x <= leftBound) {
            movementPattern = 1;
            x = leftBound;
        }
 
        //2. Gravity andd ground collision
        isGrounded = true;

        
        //Animate 
        if (currentAnimation != null) currentAnimation.animate();
        */
    }

    @Override
    public void draw(Graphics g) { 

    }

    @Override
    public void takeDamage(float damage){
       
    }

   @Override
    public void onDeath() { 

    }

    //Utilility methods too

    public boolean isActive() {
        return isActive;
    }

    public int getDamage() {
        return damage;
    }

    
}


