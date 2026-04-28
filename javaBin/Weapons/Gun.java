package Weapons;
import java.util.ArrayList;
import java.util.List;


public class Gun extends Weapons{
    private static final int CD = 18;
    private static final int MAX_AMMO = 12;

    private int ammo = MAX_AMMO;
    private final List<Bullet> bullets = new ArrayList<>();
    //for super purposes
    public Gun(){
        super(WeaponsType.GUN, CD);    }
    @Override
    public void tick(){
        super.tick();
        for(Bullet b : bullets){
            b.tick();
        }

        bullets.removeIf(b -> !b.isActive());
    }
    @Override
    public boolean tryAttack(float hx, float hy, float hw, float hh, boolean facingRight){
        if(!isReady() || ammo <= 0){
            return false;
        }
        resetCoolown();
        ammo--;
        //same logic for sword except the hitbox drawing pos.
        float bx = facingRight ? hx + hw : hx;
        float by = hy + hh / 2f;

        bullets.add(new Bullet(bx, by, facingRight, 18f));
        return true;
    }

    public List<Bullet> getActiveBullets() {
        return bullets;
    }

    public int getAmmo() {
        return ammo;
    }

    public void reload() {
        ammo = MAX_AMMO;
    }
}
