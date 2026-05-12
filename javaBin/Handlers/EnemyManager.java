package Handlers;

import Entities.EnemyFolder.Enemies;
import Entities.EnemyFolder.EnemiesType;
import GameCreation.LevelData;
import Entities.Player;
import GamePlatform.Platform;
import DataLoader.PlayerSaveData.EnemySnapshot;
import Weapons.Bullet;
import Weapons.Gun;
import Weapons.Sword;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EnemyManager {

    public static final int CONTACT_DAMAGE_COOLDOWN = 40;

    private final List<Enemies> enemies = new ArrayList<>();

    
    private final Set<Enemies> hitThisSwing = new HashSet<>();
    
    private Runnable onEnemyKilled = () -> {}; // no-op default

    public void setOnEnemyKilled(Runnable callback) {
        this.onEnemyKilled = callback;
    }
    
    public void spawnEnemies(LevelData levelData) {
        enemies.clear();
        for (LevelData.EnemySpawn spawn : levelData.getEnemies()) {
            enemies.add(new Enemies(spawn.type, spawn.x, spawn.y));
        }
    }

   
    public void loadEnemies(List<EnemySnapshot> snapshots) {
        enemies.clear();
        for (EnemySnapshot snap : snapshots) {
            if (!snap.alive) continue; // dead enemies are gone permanently
            Enemies e = new Enemies(snap.type, snap.x, snap.y);
            e.setHealth(snap.health);  // restore saved health, not full health
            enemies.add(e);
        }
    }

    public List<EnemySnapshot> buildSnapshots() {
        List<EnemySnapshot> snapshots = new ArrayList<>();
        for (int i = 0; i < enemies.size(); i++) {
            Enemies e = enemies.get(i);
            snapshots.add(new EnemySnapshot(
                i,
                e.getType(),
                e.getX(),
                e.getY(),
                e.getHealth(),
                e.isAlive()
            ));
        }
        return snapshots;
    }
  
    public void update(List<Platform> platforms, Player player) {
        float px = player.getX() + player.getWidth()  / 2f;
        float py = player.getY() + player.getHeight() / 2f;

        Iterator<Enemies> it = enemies.iterator();
        while (it.hasNext()) {
            Enemies e = it.next();
            e.update(platforms, px, py);
            if (e.isDeathAnimationFinished()) {
                e.onDeath();
                it.remove();
                
                onEnemyKilled.run();
                
            }
        }
    }

    public int handleEnemyPlayerInteraction(Player player, int contactDamageCooldown) {
        if (contactDamageCooldown > 0) return contactDamageCooldown;

        for (Enemies e : enemies) {
            if (!e.isAlive() && !e.isDying()) continue;
            if (!player.getBounds().intersects(e.getBounds())) continue;
            float dmg = e.isReadyToAttack() ? e.getDamage() * 2f : e.getDamage();
            player.takeDamage(dmg);
            return EnemyManager.CONTACT_DAMAGE_COOLDOWN;
        }

        return contactDamageCooldown;
    }



    public void handleWeaponHits(Player player, List<Platform> platforms) {
        Gun   gun   = player.getGun();
        Sword sword = player.getSword();

        for (Bullet b : gun.getActiveBullets()) {
            if (!b.isActive()) continue;
            // deactivate bullet if it hits a platform wall
            for (Platform plat : platforms) {
                if (b.getBounds().intersects(plat.getBounds())) {
                    b.deactivate();
                    break;
                }
            }
            if (!b.isActive()) continue;
            for (Enemies e : enemies) {
                if (!e.isAlive()) continue;
                if (b.getBounds().intersects(e.getBounds())) {
                    e.takeDamage(b.getDamage());
                    b.deactivate();
                    break;
                }
            }
        }

       
        java.awt.Rectangle swHb = sword.getAttackHitBox();
        if (swHb != null) {
            for (Enemies e : enemies) {
                if (!e.isAlive()) continue;
                if (hitThisSwing.contains(e)) continue;
                if (swHb.intersects(e.getBounds())) {
                    e.takeDamage(sword.doDamage());
                    hitThisSwing.add(e);
                }
            }
        } else {
            hitThisSwing.clear(); 
        }
    }

    
    public List<Enemies> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }
}