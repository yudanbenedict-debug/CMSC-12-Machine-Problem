package Handlers;

import Entities.EnemyFolder.Boss;
import Entities.EnemyFolder.Enemies;
import Entities.EnemyFolder.EnemiesType;
import GameCreation.LevelData;
import Entities.Player;
import GamePlatform.Platform;
import DataLoader.PlayerSaveData.EnemySnapshot;
import Weapons.Bullet;
import Weapons.Gun;
import Weapons.Rocket;
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
    private final List<Rocket> rockets = new ArrayList<>();

    private Runnable onEnemyKilled = () -> {};
    private boolean enemiesHaveSpawned = false;

    public void addRocket(Rocket rocket) {
        rockets.add(rocket);
    }

    public void setOnEnemyKilled(Runnable callback) {
        this.onEnemyKilled = callback;
    }

    public void spawnEnemies(LevelData levelData, int levelIndex) {
        enemies.clear();
        rockets.clear();
        enemiesHaveSpawned = false;

        boolean isBossLevel = levelData.getEnemies().stream()
            .anyMatch(s -> s.type == EnemiesType.BAZOOKA_BOSS);

        //no multiplier on boss level — boss already has tuned stats in properties
        float healthMult = isBossLevel ? 1.0f : 1.0f + (levelIndex * 0.1f);
        float damageMult = isBossLevel ? 1.0f : 1.0f + (levelIndex * 0.1f);

        for (LevelData.EnemySpawn spawn : levelData.getEnemies()) {
            Enemies e;
            if (spawn.type == EnemiesType.BAZOOKA_BOSS) {
                Boss boss = new Boss(spawn.type, spawn.x, spawn.y);
                boss.setEnemyManager(this);
                e = boss;
            } else {
                e = new Enemies(spawn.type, spawn.x, spawn.y);
                e.setHealth(e.getMaxHealth() * healthMult);
                e.setDamageMultiplier(damageMult);
            }
            enemies.add(e);
        }
        enemiesHaveSpawned = !enemies.isEmpty();
    }

    public void loadEnemies(List<EnemySnapshot> snapshots) {
        enemies.clear();
        rockets.clear();
        for (EnemySnapshot snap : snapshots) {
            if (!snap.alive) continue;
            Enemies e;
            if (snap.type == EnemiesType.BAZOOKA_BOSS) {
                Boss boss = new Boss(snap.type, snap.x, snap.y);
                boss.setEnemyManager(this);
                e = boss;
            } else {
                e = new Enemies(snap.type, snap.x, snap.y);
            }
            e.setHealth(snap.health);
            e.setX(snap.x);
            e.setY(snap.y);
            enemies.add(e);
        }
    }

    public List<EnemySnapshot> buildSnapshots() {
        List<EnemySnapshot> snapshots = new ArrayList<>();
        for (int i = 0; i < enemies.size(); i++) {
            Enemies e = enemies.get(i);
            snapshots.add(new EnemySnapshot(
                i, e.getType(), e.getX(), e.getY(), e.getHealth(), e.isAlive()
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

        //update rockets and check collision with player and platforms
        Iterator<Rocket> rit = rockets.iterator();
        while (rit.hasNext()) {
            Rocket r = rit.next();
            r.update();
            if (!r.isActive()) {
                rit.remove();
                continue;
            }

            if (player.getBounds().intersects(r.getBounds())) {
                player.takeDamage(r.getDamage());
                explode(r.getX(), r.getY(), r.getExplosionRadius(), player);
                r.deactivate();
                rit.remove();
                continue;
            }

            boolean hitPlatform = false;
            for (Platform p : platforms) {
                if (r.getBounds().intersects(p.getBounds())) {
                    explode(r.getX(), r.getY(), r.getExplosionRadius(), player);
                    r.deactivate();
                    hitPlatform = true;
                    break;
                }
            }
            if (hitPlatform) {
                rit.remove();
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

    private void explode(float cx, float cy, int radius, Player player) {
        float px = player.getX() + player.getWidth() / 2;
        float py = player.getY() + player.getHeight() / 2;
        float dx = px - cx;
        float dy = py - cy;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < radius) {
            float damage = 25f * (1 - dist / radius);
            player.takeDamage(damage);
        }
    }

    public boolean areAllEnemiesDead() {
        return enemiesHaveSpawned && enemies.isEmpty();
    }

    public List<Enemies> getEnemies() {
        return new ArrayList<>(enemies);
    }

    public List<Rocket> getRockets() {
        return new ArrayList<>(rockets);
    }
}