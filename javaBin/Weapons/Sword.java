package Weapons;

import java.awt.Rectangle;

public class Sword extends Weapons{
    private static final REACH = 75;
    private static final ARC_H = 64;
    private static final ACTIVE_FRAMES = 3;
    private static final float DAMAGE = 10f;

    private int timer = 0;
    private Rectangle hitbox = null;
    public Sword(){
        super(WeaponsType.SWORD, 4);
    }
}
