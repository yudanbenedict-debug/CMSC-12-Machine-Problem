package Weapons;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Bullet {
    //make the bullet moving instead of hit-reg type of gun
    private static final float SPEED     = 18f;
    private static final int   MAX_RANGE = 1400;

    //bullet dimensions for drawing & collision
    private static final int BULLET_W = 10;
    private static final int BULLET_H =  4;

    //bullet pos/misc
    private float   x, y;
    private final float   dx;
    private float   travelled = 0f;
    private boolean active    = true;
    private final float   damage;

    private final Rectangle cachedBounds = new Rectangle(0, 0, BULLET_W, BULLET_H);

    public Bullet(float pos_x, float pos_y, boolean facingRight, float damage) {
        this.x      = pos_x;
        this.y      = pos_y;
        this.dx     = facingRight ? SPEED : -SPEED;
        this.damage = damage;
    }

    public void tick() {
        if (!active) return;
        x         += dx;
        travelled += Math.abs(dx);
        if (travelled >= MAX_RANGE) {
            active = false;
        }
    }
    public void draw(Graphics2D g2, int cameraX) {
        if (!active) return;

        int sx = (int) x - cameraX;
        int sy = (int) y;

        g2.setColor(new Color(255, 240, 80, 60));
        g2.fillRoundRect(sx - 2, sy - 2, BULLET_W + 4, BULLET_H + 4, 6, 6);

        g2.setColor(new Color(255, 230, 60));
        g2.fillRoundRect(sx, sy, BULLET_W, BULLET_H, 4, 4);

        g2.setColor(new Color(255, 255, 200, 180));
        g2.fillRoundRect(sx + 1, sy, BULLET_W - 2, BULLET_H / 2, 2, 2);
    }

    public Rectangle getBounds() {
        cachedBounds.setLocation((int) x, (int) y);
        return cachedBounds;
    }

    public void consume()    { deactivate(); }
    public void deactivate() { active = false; }
    public boolean isActive(){ return active; }
    public float   getDamage(){ return damage; }
}