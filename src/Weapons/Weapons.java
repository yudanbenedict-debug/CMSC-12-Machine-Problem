package Weapons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class Weapons{
    protected final WeaponsType type;
    protected final int cooldownMAX;
    protected int cd_timer = 0;

    public Weapons(WeaponsType type, int cooldownMAX){
        this.type = type;
        this.cooldownMAX = cooldownMAX;

    }

    public void tick(){
        if (cd_timer > 0) cd_timer--;
    }
    public boolean     isReady()  { return cd_timer <= 0; }
    public WeaponsType  getType()  { return type; }
    protected void resetCoolown(){
        cd_timer = cooldownMAX;
    }


    public abstract boolean  tryAttack(float hx, float hy, float hw, float hh, boolean facingRight);
}