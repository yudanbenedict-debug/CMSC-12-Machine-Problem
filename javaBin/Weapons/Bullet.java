package Weapons;
import java.awt.Rectangle;

public class Bullet {
    //make the bullet moving instead of hit-reg type of gun
    private static final float SPEED = 18f;
    private static final int MAX_RANGE = 140000; //i still don't know why this isn't registering unless close range.

    //bullet pos/miscs
    private float x,y;
    private final float dx;
    private float travelled = 0f;
    private boolean active = true;
    private final float damage;

    public Bullet(float pos_x, float pos_y, boolean facingRight, float DAMAGE){
        this.x = pos_x;
        this.y = pos_y;
        this.dx = facingRight ? SPEED : -SPEED;
        this.damage = DAMAGE;
    }

    //separate tick for bullet
    public void tick(){
        if(!active){
            return;
        }
        x += dx;
        travelled += Math.abs(dx);
        if(travelled >=  MAX_RANGE){
            active = false;
        }
    }
    public Rectangle getBounds(){
        //static width and height. waiting if needed to be final'd in instance variable or should the bullet size be upgradable.
        return new Rectangle((int) x, (int) y, 10, 6);

    }

    public void consume(){
        active = false;
    }
    public boolean isActive(){
        return active;

    }
    public void deactivate()  { 
        active = false;
        
     }
    public float getDamage(){
        return damage;
    }
}
