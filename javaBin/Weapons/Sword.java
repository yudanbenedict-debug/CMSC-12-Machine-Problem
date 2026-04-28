package Weapons;

import java.awt.Rectangle;

public class Sword extends Weapons{
    private static final int REACH = 75;
    private static final int ARC_H = 64;
    private static final int ACTIVE_FRAMES = 3;
    private static final float DAMAGE = 10f;

    private int timer = 0;
    private Rectangle hitbox = null;
    public Sword(){
        super(WeaponsType.SWORD, 4);
    }
    //override tick for different cd tick
    @Override
    public void tick(){
        super.tick();
        if(timer > 0){
            timer--;
            if(timer == 0) hitbox = null;
        }
    }
    @Override
    public boolean tryAttack(float hx, float hy, float hw, float hh, boolean facingRight){
        if(!isReady()){
            return false;
        }
        resetCoolown();
        timer = ACTIVE_FRAMES;
        //sorry for bad naming, but the logic is pretty simple.
        //if attack can be done, create hitbox infront of player, (rectangle), then compute vertical pos and horizontal pos
        //finally create hitbox.
        float midY = hy + hh / 2f - ARC_H / 2f;
        float startX =  facingRight ? hx + hw : hx - REACH;
        hitbox = new Rectangle((int) startX, (int) midY, REACH, ARC_H);
        return true;
         
    }

    public Rectangle getAttackHitBox(){
        if(timer > 0){
            return hitbox;
        }
        else{
            return null;
        }
    }
    public float doDamage(){
        return DAMAGE;
    }


}
