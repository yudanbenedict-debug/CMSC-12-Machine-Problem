package GameCreation;

import Entities.EnemyFolder.*;
import Entities.Player;
import Exceptions.InvalidLevelDataException;
import GamePlatform.Platform;
import Weapons.*;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Level {

    public static final int    DEFAULT_WORLD_WIDTH  = 4000;
    public static final int    DEFAULT_WORLD_HEIGHT = 720;
    public static final String PLAYER_DATA_PATH     = "javaBin/LevelFile/player-data.txt";

    private static final float FLOOR_SNAP_TOLERANCE    = 4.0f;
    private static final int   CONTACT_DAMAGE_COOLDOWN = 40;

    private final int viewportWidth;
    private final int viewportHeight;
    private final int worldWidth;
    private final int worldHeight;

    private final Player          player;
    private       LevelData       currentLevelData;
    private final List<LevelData> futureLevels;

    private final List<Enemies> enemies = new ArrayList<>();

    private int contactDamageCooldown = 0;

    // ─────────────────────────────────────────────────────────────────────────
    //  Construction
    // ─────────────────────────────────────────────────────────────────────────

    public Level(int viewportWidth, int viewportHeight) {
        this(viewportWidth, viewportHeight, DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT);
    }

    public Level(int viewportWidth, int viewportHeight, int worldWidth, int worldHeight) {
        validateDimensions(viewportWidth, viewportHeight, worldWidth, worldHeight);
        this.viewportWidth  = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.worldWidth     = worldWidth;
        this.worldHeight    = worldHeight;

        float spawnX = (viewportWidth  * 0.5f) - 16.0f;
        float spawnY = (viewportHeight * 0.5f) - 24.0f;
        this.player  = new Player(0.0f, 0.0f, spawnX, spawnY);

        this.currentLevelData = LevelData.createStarterLevel(worldWidth, worldHeight);
        this.futureLevels     = FutureLevelCatalog.loadFutureLevels();

        spawnEnemies();
    }

    private void validateDimensions(int vw, int vh, int ww, int wh) {
        if (vw <= 0 || vh <= 0)
            throw new InvalidLevelDataException("Viewport dimensions must be positive.");
        if (ww < vw || wh < vh)
            throw new InvalidLevelDataException("World dimensions must be >= viewport dimensions.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Enemy spawning
    // ─────────────────────────────────────────────────────────────────────────

    private void spawnEnemies() {
        for (LevelData.EnemySpawn spawn : currentLevelData.getEnemies()) {
            enemies.add(new Enemies(spawn.type, spawn.x, spawn.y));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Main update
    // ─────────────────────────────────────────────────────────────────────────

    public void update(LevelInput inputState) {
        player.applyEngineState(
            inputState.isMoveLeft(),
            inputState.isMoveRight(),
            inputState.isJumpPressed(),
            inputState.isRunning(),
            inputState.isAttackPressed(),
            inputState.getWeaponSlot(),
            inputState.isReloadPressed()
        );

        float previousX = player.getX();
        float previousY = player.getY();
        player.update();

        handleWorldBounds();
        handlePlayerPlatformCollisions(previousX, previousY);
        handleItemCollisions();

        updateEnemies();
        handleEnemyPlayerInteraction();
        handleWeaponHits();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  World bounds
    // ─────────────────────────────────────────────────────────────────────────

    private void handleWorldBounds() {
        if (player.getX() < 0) {
            player.setX(0);
        } else if (player.getX() + player.getWidth() > worldWidth) {
            player.setX(worldWidth - player.getWidth());
        }

        if (player.getY() < 0) {
            player.setY(0);
            player.setVerticalVelocity(0);
        } else if (player.getY() + player.getHeight() > worldHeight) {
            player.setY(worldHeight - player.getHeight());
            player.setVerticalVelocity(0);
            player.setGrounded(true);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Player-platform collision
    // ─────────────────────────────────────────────────────────────────────────

    private void handlePlayerPlatformCollisions(float previousX, float previousY) {
        player.setGrounded(false);

        float previousBottom = previousY + player.getHeight();
        float previousTop    = previousY;
        float previousRight  = previousX + player.getWidth();
        float previousLeft   = previousX;

        for (Platform platform : currentLevelData.getPlatforms()) {
            Rectangle pb = platform.getBounds();

            float platTop    = pb.y;
            float platBottom = pb.y + pb.height;
            float platLeft   = pb.x;
            float platRight  = pb.x + pb.width;

            float curBottom = player.getY() + player.getHeight();
            float curTop    = player.getY();
            float curLeft   = player.getX();
            float curRight  = player.getX() + player.getWidth();

            boolean overlapsH = curLeft < platRight && curRight > platLeft;
            boolean overlapsV = curTop  < platBottom && curBottom > platTop;

            if (!overlapsH || !overlapsV) continue;

            boolean fromTop    = previousBottom <= platTop    + FLOOR_SNAP_TOLERANCE;
            boolean fromBottom = previousTop    >= platBottom - FLOOR_SNAP_TOLERANCE;
            boolean fromLeft   = previousRight  <= platLeft   + FLOOR_SNAP_TOLERANCE;
            boolean fromRight  = previousLeft   >= platRight  - FLOOR_SNAP_TOLERANCE;

            if (fromTop && player.getVerticalVelocity() >= 0) {
                player.setY(platTop - player.getHeight());
                player.setVerticalVelocity(0);
                player.setGrounded(true);
            } else if (fromBottom && player.getVerticalVelocity() < 0) {
                player.setY(platBottom);
                player.setVerticalVelocity(0);
            } else if (fromLeft) {
                player.setX(platLeft - player.getWidth());
            } else if (fromRight) {
                player.setX(platRight);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Item collisions
    // ─────────────────────────────────────────────────────────────────────────

    private void handleItemCollisions() {
        Rectangle playerBounds = player.getBounds();
        for (Rectangle itemBounds : currentLevelData.getItems()) {
            if (playerBounds.intersects(itemBounds)) {
                // TODO: item collection logic
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Enemy update + removal
    // ─────────────────────────────────────────────────────────────────────────

    private void updateEnemies() {
        List<Platform> platforms = currentLevelData.getPlatforms();
        float px = player.getX() + player.getWidth()  / 2f;
        float py = player.getY() + player.getHeight() / 2f;

        Iterator<Enemies> it = enemies.iterator();
        while (it.hasNext()) {
            Enemies e = it.next();
            e.update(platforms, px, py);
            
            if (e.isDeathAnimationFinished()) {
                e.onDeath();   // hook for loot drops, score, sound, etc.
                it.remove();
            }
        }

        if (contactDamageCooldown > 0) contactDamageCooldown--;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Enemy-player damage
    // ─────────────────────────────────────────────────────────────────────────

    private void handleEnemyPlayerInteraction() {
        if (contactDamageCooldown > 0) return;

        Rectangle playerBounds = player.getBounds();

        for (Enemies e : enemies) {
            if (!e.isAlive() && !e.isDying()){
                updateEnemies();
            }
            if (!playerBounds.intersects(e.getBounds())) continue;

            // CHECK: player is falling
            if (player.getVerticalVelocity() > 0) {

                // CHECK: player is above enemy
                if (player.getY() + player.getHeight() <= e.getY() + 10) {

                    e.registerStomp(); // kill enemy

                    player.setVerticalVelocity(-10); // bounce up

                    continue; // skip damage
                }
            }

           
            float dmg = e.isReadyToAttack() ? e.getDamage() * 2f : e.getDamage();
            player.takeDamage(dmg);
            contactDamageCooldown = CONTACT_DAMAGE_COOLDOWN;
            break;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Weapon hits
    // ─────────────────────────────────────────────────────────────────────────

    private void handleWeaponHits() {
        Gun   gun   = player.getGun();
        Sword sword = player.getSword();

        // ── Bullets vs enemies ────────────────────────────────────────────────
        for (Bullet b : gun.getActiveBullets()) {
            if (!b.isActive()) continue;
            Rectangle bRect = b.getBounds();
            for (Enemies e : enemies) {
                if (!e.isAlive()) continue;
                if (bRect.intersects(e.getBounds())) {
                    e.takeDamage(b.getDamage());
                    b.deactivate();
                    break;  // one enemy per bullet
                }
            }
        }

        // ── Sword vs enemies ──────────────────────────────────────────────────
        Rectangle swHb = sword.getAttackHitBox();
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
    //  Getters
    // ─────────────────────────────────────────────────────────────────────────

    public Player getPlayer() { return player; }

    public List<Platform> getPlatforms() {
        return Collections.unmodifiableList(currentLevelData.getPlatforms());
    }

    public List<LevelData.EnemySpawn> getEnemySpawns() {
        return Collections.unmodifiableList(currentLevelData.getEnemies());
    }

    public List<Enemies> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public List<Rectangle> getItemZones() {
        return Collections.unmodifiableList(currentLevelData.getItems());
    }

    public int getWorldWidth() { return worldWidth; }
}