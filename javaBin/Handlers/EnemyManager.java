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
import java.util.Iterator;
import java.util.List;

public class EnemyManager {

    public static final int CONTACT_DAMAGE_COOLDOWN = 40;

    private final List<Enemies> enemies = new ArrayList<>();

    /*/----- CHANGE: added hitThisSwing set to fix sword multi-hit bug -----/
     * PURPOSE: The sword hitbox lives for ACTIVE_FRAMES (3) ticks. Without
     * tracking which enemies were already hit, every alive enemy inside the
     * hitbox takes damage once per tick for the full duration of the swing —
     * up to 3x the intended damage per swing. hitThisSwing records which
     * enemies have already been damaged in the current swing. It is cleared
     * when the hitbox disappears (getAttackHitBox() returns null) so the set
     * is always fresh at the start of the next swing.
     */
    private final java.util.Set<Enemies> hitThisSwing = new java.util.HashSet<>();
    /*/----- END CHANGE -----/*/

    /*/----- CHANGE: added onEnemyKilled callback -----/
     * PURPOSE: EnemyManager needs to notify Level when an enemy dies so
     * Level can increment the score by 3. A Runnable callback keeps
     * EnemyManager decoupled from Level — it doesn't import or reference
     * Level at all, it just fires a hook that Level wires up.
     * setOnEnemyKilled() is called in the Level constructor and again after
     * loadFromSave() since loadEnemies() creates fresh instances.
     */
    private Runnable onEnemyKilled = () -> {}; // no-op default

    public void setOnEnemyKilled(Runnable callback) {
        this.onEnemyKilled = callback;
    }
    /*/----- END CHANGE -----/*/

    // ─────────────────────────────────────────────────────────────────────────
    //  Spawning
    // ─────────────────────────────────────────────────────────────────────────

    public void spawnEnemies(LevelData levelData) {
        enemies.clear();
        for (LevelData.EnemySpawn spawn : levelData.getEnemies()) {
            enemies.add(new Enemies(spawn.type, spawn.x, spawn.y));
        }
    }

    /*/----- CHANGE: added loadEnemies() method -----/
     * PURPOSE: spawnEnemies() always creates enemies at the coordinates
     * defined in the level file, which is correct for a fresh game start but
     * wrong for a load — it caused every enemy to teleport back to its
     * original spawn point regardless of where it actually was when the
     * player saved.
     *
     * loadEnemies() replaces spawnEnemies() during a load. Instead of
     * reading coordinates from the level file, it reads them from the
     * EnemySnapshot list stored in the save data. For each snapshot:
     *   - If alive == true : create the enemy at the saved x/y with saved
     *     health, preserving where it was in the world at save time.
     *   - If alive == false: skip it entirely — dead enemies stay dead and
     *     are not added to the live list at all.
     *
     * Enemies are matched back to their type via snapshot.type rather than
     * snapshot.spawnIndex because type is sufficient to reconstruct the
     * enemy — the index is kept in the snapshot for potential future use
     * (e.g. tracking which specific spawn slot was cleared).
     *
     * USAGE: Called by Level.loadFromSave() instead of spawnEnemies().
     */
    public void loadEnemies(List<EnemySnapshot> snapshots) {
        enemies.clear();
        for (EnemySnapshot snap : snapshots) {
            if (!snap.alive) continue; // dead enemies are gone permanently
            Enemies e = new Enemies(snap.type, snap.x, snap.y);
            e.setHealth(snap.health);  // restore saved health, not full health
            enemies.add(e);
        }
    }
    /*/----- END CHANGE -----/*/

    // ─────────────────────────────────────────────────────────────────────────
    //  Snapshot builder  (called by Level.buildSaveData)
    // ─────────────────────────────────────────────────────────────────────────

    /*/----- CHANGE: added buildSnapshots() method -----/
     * PURPOSE: Level.buildSaveData() needs to package the current enemy
     * state into the save file. buildSnapshots() iterates the live enemy
     * list and converts each Enemies instance into an EnemySnapshot,
     * recording its spawn index, type, current world position, health,
     * and alive status.
     *
     * Only enemies still in the live list are included — enemies that
     * finished their death animation are already removed by update(), so
     * they naturally don't appear in the snapshot and won't be recreated
     * on load.
     *
     * USAGE: Called by Level.buildSaveData() to populate
     * PlayerSaveData.enemies.
     */
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
    /*/----- END CHANGE -----/*/

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
                /*/----- CHANGE: fire kill callback for +3 score -----/
                 * PURPOSE: This is the single point where an enemy is
                 * confirmed dead and removed from the live list. Firing
                 * onEnemyKilled here (after removal) ensures the callback
                 * triggers exactly once per kill and only when the death
                 * animation has fully played — not on first hit.
                 */
                onEnemyKilled.run();
                /*/----- END CHANGE -----/*/
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Enemy-player interaction
    // ─────────────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────
    //  Weapon hits
    // ─────────────────────────────────────────────────────────────────────────

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

        /*/----- CHANGE: sword now hits each enemy at most once per swing -----/
         * PURPOSE: Previously the sword hitbox was checked every tick for its
         * full ACTIVE_FRAMES lifetime, so an enemy inside the arc took damage
         * on each of those ticks — up to 3x the intended damage. Now we check
         * hitThisSwing before applying damage and add the enemy to the set so
         * it can't be hit again in the same swing. When the hitbox disappears
         * (getAttackHitBox() returns null) we clear the set so it's clean for
         * the next swing.
         */
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
            hitThisSwing.clear(); // swing ended — reset for next attack
        }
        /*/----- END CHANGE -----/*/
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Getter
    // ─────────────────────────────────────────────────────────────────────────

    public List<Enemies> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }
}