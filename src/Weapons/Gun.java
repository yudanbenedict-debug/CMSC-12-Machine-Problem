package Weapons;

import java.util.ArrayList;
import java.util.List;

public class Gun extends Weapons {
    private static final int CD           = 18;
    private static final int MAX_AMMO     = 12;
    private static final int MAX_CLIPS    = 3;
    private static final int RELOAD_TICKS = 90; // ~1.5 s @ 60 fps

    private int     ammo            = MAX_AMMO;
    private int     clipsRemaining  = MAX_CLIPS;
    private boolean reloading       = false;
    private int     reloadTimer     = 0;

    private final List<Bullet> bullets = new ArrayList<>();

    public Gun() {
        super(WeaponsType.GUN, CD);
    }

    @Override
    public void tick() {
        super.tick();

        if (reloading) {
            reloadTimer--;
            if (reloadTimer <= 0) {
                ammo      = MAX_AMMO;
                reloading = false;
            }
        }

        for (Bullet b : bullets) b.tick();
        bullets.removeIf(b -> !b.isActive());
    }

    @Override
    public boolean tryAttack(float hx, float hy, float hw, float hh, boolean facingRight) {
        if (!isReady() || ammo <= 0) return false;
        resetCoolown();
        ammo--;

        float bx = facingRight ? hx + hw : hx;
        float by = hy + hh / 2f;
        bullets.add(new Bullet(bx, by, facingRight, 10f));
        return true;
    }

    public boolean reload() {
        if (reloading || clipsRemaining <= 0 || ammo == MAX_AMMO) return false;
        clipsRemaining--;
        ammo        = 0;
        reloading   = true;
        reloadTimer = RELOAD_TICKS;
        return true;
    }

    public List<Bullet> getActiveBullets() { return bullets; }
    public int          getAmmo()          { return ammo; }
    public int          getClips()         { return clipsRemaining; }
    public int          getMaxAmmo()       { return MAX_AMMO; }
    public int          getMaxClips()      { return MAX_CLIPS; }
    public void         refill()           { ammo = MAX_AMMO; clipsRemaining = MAX_CLIPS; reloading = false; }
    public boolean      isReloading()      { return reloading; }

    public float getReloadProgress() {
        if (!reloading) return 1f;
        return 1f - ((float) reloadTimer / RELOAD_TICKS);
    }
}