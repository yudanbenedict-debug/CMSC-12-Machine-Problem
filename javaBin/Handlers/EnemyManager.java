package Handlers;

import Entities.EnemyFolder.Enemies;
import GameCreation.Level;
import GameCreation.LevelData;
import Entities.Player;
import GamePlatform.Platform;
import Weapons.Bullet;
import Weapons.Gun;
import Weapons.Sword;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EnemyManager {

    private final List<Enemies> enemies = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    //  Spawning
    // ─────────────────────────────────────────────────────────────────────────

    public void spawnEnemies(LevelData levelData) {
        enemies.clear();
        for (LevelData.EnemySpawn spawn : levelData.getEnemies()) {
            enemies.add(new Enemies(spawn.type, spawn.x, spawn.y));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Update + removal
    // ─────────────────────────────────────────────────────────────────────────

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
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Enemy-player interaction
    //  Returns the new contactDamageCooldown value for Level to store.
    // ─────────────────────────────────────────────────────────────────────────

    public int handleEnemyPlayerInteraction(Player player, int contactDamageCooldown) {
        if (contactDamageCooldown > 0) return contactDamageCooldown;

        for (Enemies e : enemies) {
            if (!e.isAlive() && !e.isDying()) continue;
            if (!player.getBounds().intersects(e.getBounds())) continue;
            // ── Contact / attack damage ───────────────────────────────────────
            float dmg = e.isReadyToAttack() ? e.getDamage() * 2f : e.getDamage();
            player.takeDamage(dmg);
            return Level.CONTACT_DAMAGE_COOLDOWN;
        }

        return contactDamageCooldown;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Weapon hits
    // ─────────────────────────────────────────────────────────────────────────

    public void handleWeaponHits(Player player) {
        Gun   gun   = player.getGun();
        Sword sword = player.getSword();

        // ── Bullets vs enemies ────────────────────────────────────────────────
        for (Bullet b : gun.getActiveBullets()) {
            if (!b.isActive()) continue;
            for (Enemies e : enemies) {
                if (!e.isAlive()) continue;
                if (b.getBounds().intersects(e.getBounds())) {
                    e.takeDamage(b.getDamage());
                    b.deactivate();
                    break;  // one enemy per bullet
                }
            }
        }

        // ── Sword vs enemies ──────────────────────────────────────────────────
        java.awt.Rectangle swHb = sword.getAttackHitBox();
        if (swHb != null) {
            for (Enemies e : enemies) {
                if (!e.isAlive()) continue;
                if (swHb.intersects(e.getBounds())) {
                    e.takeDamage(sword.doDamage());
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Getter
    // ─────────────────────────────────────────────────────────────────────────

    public List<Enemies> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }
}