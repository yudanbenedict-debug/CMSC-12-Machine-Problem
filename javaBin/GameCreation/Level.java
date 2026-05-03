package GameCreation;

import Entities.EnemyFolder.Enemies;
import Entities.Player;
import Exceptions.InvalidLevelDataException;
import GamePlatform.Platform;
import Handlers.CollisionHandler;
import Handlers.EnemyManager;
import DataLoader.LevelDataLoader;
import DataLoader.PlayerSaveData;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

public class Level {

    public static final int    DEFAULT_WORLD_WIDTH  = 4000;
    public static final int    DEFAULT_WORLD_HEIGHT = 720;
    public static final String PLAYER_DATA_PATH     = "javaBin/LevelFile/player-data.txt";

    public static final int CONTACT_DAMAGE_COOLDOWN = 40;

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
        this.currentLevelData = LevelDataLoader.load("level1.properties", worldWidth, worldHeight);
        // --------------------------------------------------
        this.futureLevels     = FutureLevelCatalog.loadFutureLevels();

        enemyManager.spawnEnemies(currentLevelData);
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
        collisionHandler.handleItemCollisions(player, currentLevelData.getItems());

        enemyManager.update(currentLevelData.getPlatforms(), player);
        contactDamageCooldown = enemyManager.handleEnemyPlayerInteraction(player, contactDamageCooldown);
        if (contactDamageCooldown > 0) contactDamageCooldown--;
        enemyManager.handleWeaponHits(player);
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
    public void respawnPlayer() {
        float spawnX = (viewportWidth  * 0.5f) - 16.0f;
        float spawnY = (viewportHeight * 0.5f) - 24.0f;
        player.respawn(spawnX, spawnY);
    }
 
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

    public List<Rectangle> getItemZones() {
        return Collections.unmodifiableList(currentLevelData.getItems());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Save / Load
    // ─────────────────────────────────────────────────────────────────────────

    /** Packages current level + player state into a save snapshot. */
    public PlayerSaveData buildSaveData() {
        return new PlayerSaveData(
            player.getHealth(),
            0,              // score — expand later
            goldCount,
            player.get_activeslots(),
            player.getX(),
            player.getY(),
            currentLevelFile
        );
    }

    /** Restores player state from a save snapshot. */
    public void loadFromSave(PlayerSaveData save) {
        // Reload the level layout for the saved level
        currentLevelFile  = save.currentLevel;
        currentLevelData  = LevelDataLoader.load(currentLevelFile, worldWidth, worldHeight);
        goldCount         = save.goldCount;

        // Restore player position and health
        player.respawn(save.x, save.y);
        player.setHealth(save.health);

        // Respawn enemies fresh for that level
        enemyManager.spawnEnemies(currentLevelData);
    }

    public int getGoldCount() { return goldCount; }

    public int getWorldWidth() { return worldWidth; }
}