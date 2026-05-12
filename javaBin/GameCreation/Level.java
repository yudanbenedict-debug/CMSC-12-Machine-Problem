package GameCreation;

import Entities.EnemyFolder.Enemies;
import Entities.Player;
import Exceptions.InvalidLevelDataException;
import GamePlatform.Platform;
import Handlers.CollisionHandler;
import Handlers.EnemyManager;
import DataLoader.LevelDataLoader;
import DataLoader.PlayerSaveData;
import Entities.Item;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

public class Level {

    public static final int    DEFAULT_WORLD_WIDTH  = 4000;
    public static final int    DEFAULT_WORLD_HEIGHT = 720;
    public static final String PLAYER_DATA_PATH     = "javaBin/LevelFile/player-data.txt";

    private final int viewportWidth;
    private final int viewportHeight;
    private final int worldWidth;
    private final int worldHeight;

    private final Player          player;
    private       LevelData       currentLevelData;
    private final List<LevelData> futureLevels;

    private final EnemyManager    enemyManager     = new EnemyManager();
    private final CollisionHandler collisionHandler = new CollisionHandler();

    private int contactDamageCooldown = 10;
    private String currentLevelFile = "level1.properties";
    private int goldCount = 0;

    /*/----- CHANGE: added score field -----/
     * PURPOSE: Score is owned by Level (not Player) because Level manages
     * both score sources — coins (item zones) and enemy kills. Coins grant
     * +1, enemy kills grant +3 via the onEnemyKilled callback wired in the
     * constructor below.
     */
    private int score = 0;
    /*/----- END CHANGE -----/*/

    /*/----- CHANGE: added onLevelComplete callback and levelComplete flag -----/
     * PURPOSE: When the player enters the exit zone with enough score, Level
     * needs to notify GamePanel to load the next level. A Runnable callback
     * keeps Level decoupled from GamePanel — Level doesn't import GamePanel
     * at all. levelComplete is a one-shot flag that prevents the callback
     * from firing every tick once the exit is triggered.
     * setOnLevelComplete() is called by GamePanel after constructing Level.
     */
    private Runnable onLevelComplete  = () -> {};
    private boolean  levelComplete    = false;

    public void setOnLevelComplete(Runnable callback) {
        this.onLevelComplete = callback;
    }
    /*/----- END CHANGE -----/*/

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

        // --------------------------------------------------
        this.currentLevelData = LevelDataLoader.load(currentLevelFile, worldWidth, worldHeight);
        // --------------------------------------------------
        this.futureLevels     = FutureLevelCatalog.loadFutureLevels();

        enemyManager.spawnEnemies(currentLevelData);

        /*/----- CHANGE: wire enemy kill callback for +3 score -----/
         * PURPOSE: EnemyManager fires onEnemyKilled each time an enemy's
         * death animation finishes and the enemy is removed from the list.
         * Level increments score by 3 here so neither EnemyManager nor
         * Enemies need to know anything about scoring — the callback keeps
         * concerns separated.
         */
        enemyManager.setOnEnemyKilled(() -> score += 3);
        /*/----- END CHANGE -----/*/
    }

    private void validateDimensions(int vw, int vh, int ww, int wh) {
        if (vw <= 0 || vh <= 0)
            throw new InvalidLevelDataException("Viewport dimensions must be positive.");
        if (ww < vw || wh < vh)
            throw new InvalidLevelDataException("World dimensions must be >= viewport dimensions.");
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
        collisionHandler.handlePlayerPlatformCollisions(
            player, currentLevelData.getPlatforms(), previousX, previousY
        );

        /*/----- CHANGE: coin collection now uses Item type instead of raw Rectangle -----/
         * PURPOSE: Items are now typed (Item.Type.COIN, HEALTH, etc.) so the
         * collection logic branches on type rather than treating every item as
         * a coin. COIN gives +1 score. HEALTH is stubbed with a comment for
         * future implementation. Items are skipped if already collected, and
         * markCollected() is called instead of removing from the list — this
         * is safer during iteration and also means the renderer can choose to
         * draw a "picked up" state later if needed.
         */
        for (Item item : currentLevelData.getItems()) {
            if (item.isCollected()) continue;
            if (player.getBounds().intersects(item.bounds)) {
                item.markCollected();
                switch (item.type) {
                    case COIN:
                        score += 1;
                        break;
                    case HEALTH:
                        // TODO: restore player health when health pack is implemented
                        break;
                }
            }
        }
        /*/----- END CHANGE -----/*/

        enemyManager.update(currentLevelData.getPlatforms(), player);
        contactDamageCooldown = enemyManager.handleEnemyPlayerInteraction(player, contactDamageCooldown);
        if (contactDamageCooldown > 0) contactDamageCooldown--;
        enemyManager.handleWeaponHits(player, currentLevelData.getPlatforms());

        /*/----- CHANGE: exit zone check triggers level transition -----/
         * PURPOSE: Each LevelData declares an exitZone rectangle and a
         * minScore gate. If the player's bounds intersect the exit zone and
         * the current score meets or exceeds minScore, the onLevelComplete
         * callback fires exactly once (levelComplete flag prevents re-firing
         * every tick). If minScore isn't met the exit is simply ignored —
         * the player walks through with no effect, which is the intended
         * "gate is locked" behavior since they can't progress visually past
         * the world edge anyway.
         */
        if (!levelComplete && currentLevelData.getExitZone() != null) {
            if (player.getBounds().intersects(currentLevelData.getExitZone())) {
                if (score >= currentLevelData.getMinScore()) {
                    levelComplete = true;
                    onLevelComplete.run();
                }
            }
        }
        /*/----- END CHANGE -----/*/
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  World bounds  (moves to CollisionHandler later)
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

    /*/----- CHANGE: removed respawnPlayer() -----/
     * PURPOSE: respawnPlayer() placed the player at the viewport center with
     * full health, which had nothing to do with save data. It was the old
     * in-place respawn that made saving pointless.
     *
     * It has been removed because player position restoration is now handled
     * exclusively by loadFromSave() below, which reads the actual saved x/y
     * coordinates and calls player.respawn(save.x, save.y). Having both
     * methods live side-by-side risked future callers using the wrong one.
     *
     * If a true fresh-spawn ever needs to happen again (e.g. new game), it
     * should go through loadFromSave() with a default PlayerSaveData, or be
     * added back as a clearly named newGame() method.
     */
    // respawnPlayer() intentionally removed — use loadFromSave() instead.
    /*/----- END CHANGE -----/*/

    /** Ticks only enemies so the world keeps moving behind the red overlay. */
    public void updateEnemiesOnly() {
        enemyManager.update(currentLevelData.getPlatforms(), player);
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
        return enemyManager.getEnemies();
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(currentLevelData.getItems());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Save / Load
    // ─────────────────────────────────────────────────────────────────────────

    /*/----- CHANGE: buildSaveData() now passes real score; loadFromSave() restores it -----/
     * PURPOSE: score was previously hardcoded to 0 in buildSaveData() with a
     * "expand later" comment. Now it passes the live score field so progress
     * is not lost on save/load. loadFromSave() also re-wires the kill callback
     * because loadEnemies() creates fresh Enemies instances that have no
     * callback reference — without re-wiring, kills after a load would not
     * increment score.
     */
    public PlayerSaveData buildSaveData() {
        return new PlayerSaveData(
            player.getHealth(),
            score,          // real score, not hardcoded 0
            goldCount,
            player.get_activeslots(),
            player.getX(),
            player.getY(),
            currentLevelFile,
            enemyManager.buildSnapshots()
        );
    }

    public void loadFromSave(PlayerSaveData save) {
        currentLevelFile = save.currentLevel;
        currentLevelData = LevelDataLoader.load(currentLevelFile, worldWidth, worldHeight);
        goldCount        = save.goldCount;
        score            = save.score;      // restore saved score

        player.respawn(save.x, save.y);
        player.setHealth(save.health);

        enemyManager.loadEnemies(save.enemies);
        enemyManager.setOnEnemyKilled(() -> score += 3); // re-wire after load
    }
    /*/----- END CHANGE -----/*/

    public int getGoldCount()  { return goldCount;  }
    public int getScore()      { return score;      }
    public int getWorldWidth() { return worldWidth; }
    public int getMinScore()   { return currentLevelData.getMinScore();  }
    public String getNextLevel(){ return currentLevelData.getNextLevel(); }

    /*/----- CHANGE: expose background image filename from current level data -----/
     * PURPOSE: GamePanel needs to know which background image to draw.
     * Delegating through Level keeps GamePanel decoupled from LevelData.
     */
    public String getBackgroundImage() { return currentLevelData.getBackgroundImage(); }
    /*/----- END CHANGE -----/*/
    public Rectangle getExitZone() { return currentLevelData.getExitZone(); }

    /*/----- CHANGE: added loadNextLevel() -----/
     * PURPOSE: Called by GamePanel when onLevelComplete fires. Loads the next
     * level file declared in currentLevelData.getNextLevel(), resets
     * levelComplete so the flag is clean for the new level, respawns enemies
     * from the new level's spawn list, re-wires the kill callback, and places
     * the player at the left spawn of the new level. Score is deliberately
     * NOT reset — it carries over from the previous level as requested.
     * If nextLevelFile is null this is the final level and GamePanel should
     * show a win screen instead of calling this method.
     */
    public void loadNextLevel() {
        String nextFile = currentLevelData.getNextLevel();
        if (nextFile == null) return; // final level, GamePanel handles this

        currentLevelFile = nextFile;
        currentLevelData = LevelDataLoader.load(currentLevelFile, worldWidth, worldHeight);
        levelComplete    = false;

        // spawn player at left side of new level
        float spawnX = 100f;
        float spawnY = worldHeight - 60 - player.getHeight() - 10;
        player.respawn(spawnX, spawnY);

        enemyManager.spawnEnemies(currentLevelData);
        enemyManager.setOnEnemyKilled(() -> score += 3); // re-wire callback
    }
    /*/----- END CHANGE -----/*/
}