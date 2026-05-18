package Weapons;

import java.awt.Rectangle;

public class Rocket {
    private float x, y;
    private float vx, vy;
    private float damage;
    private int explosionRadius;
    private boolean active = true;
    private final Rectangle bounds;

    public Rocket(float x, float y, float vx, float vy, float damage, int explosionRadius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.explosionRadius = explosionRadius;
        this.bounds = new Rectangle((int) x - 5, (int) y - 5, 10, 10);
    }

    public void update() {
        if (!active) return;
        x += vx;
        y += vy;
        bounds.setLocation((int) x - 5, (int) y - 5);
    }

    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
    public Rectangle getBounds() { return bounds; }
    public float getDamage() { return damage; }
    public int getExplosionRadius() { return explosionRadius; }
    public float getX() { return x; }
    public float getY() { return y; }
}
