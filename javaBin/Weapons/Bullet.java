package Weapons;
import java.awt.Rectangle;

public class Bullet {
    //make the bullet moving instead of hit-reg type of gun
    private static final float SPEED = 18f;
    private static final int MAX_RANGE = 1400;

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
}
