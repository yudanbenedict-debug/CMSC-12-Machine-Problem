package GameCreation;

import Entities.EnemyFolder.Enemies;
import Entities.EnemyFolder.EnemiesType;
import Entities.Player;
import Exceptions.InvalidLevelDataException;
import GamePlatform.Platform;
import Handlers.CollisionHandler;
import Handlers.EnemyManager;
import DataLoader.LevelDataLoader;
import DataLoader.PlayerSaveData;
import Entities.Item;
import Weapons.Rocket;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

public class Level {

    public static final int    DEFAULT_WORLD_WIDTH  = 4000;
    public static final int    DEFAULT_WORLD_HEIGHT = 720;
    public static final String PLAYER_DATA_PATH     = "javaBin/LevelFile/player-data.txt";

    private final int viewportWidth;
    private final int viewportHeight;
    private int worldWidth;
    private int worldHeight;

    private final Player          player;
    private       LevelData       currentLevelData;
    private final List<LevelData> futureLevels;

    private final EnemyManager     enemyManager     = new EnemyManager();
    private final CollisionHandler collisionHandler = new CollisionHandler();

    private int contactDamageCooldown = 10;
    private String currentLevelFile = "level1.properties";
    private int goldCount = 0;
    private int levelIndex = 0;

    private int score = 0;

    private boolean isBossLevel = false;
    private Runnable onLevelComplete = () -> {};
    private boolean  levelComplete   = false;

    public void setOnLevelComplete(Runnable callback) {
        this.onLevelComplete = callback;
    }

    public Level(int viewportWidth, int viewportHeight) {
        this(viewportWidth, viewportHeight, DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT);
    }

    public Level(int viewportWidth, int viewportHeight, int worldWidth, int worldHeight) {
        this.viewportWidth  = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.worldWidth     = worldWidth;
        this.worldHeight    = worldHeight;

        float spawnX = (viewportWidth  * 0.5f) - 16.0f;
        float spawnY = (viewportHeight * 0.5f) - 24.0f;
        this.player  = new Player(0.0f, 0.0f, spawnX, spawnY);

        this.currentLevelData = LevelDataLoader.load(currentLevelFile, worldWidth, worldHeight);
        this.worldWidth       = currentLevelData.getWorldWidth();
        this.worldHeight      = currentLevelData.getWorldHeight();

        validateDimensions(viewportWidth, viewportHeight, this.worldWidth, this.worldHeight);

        this.futureLevels = FutureLevelCatalog.loadFutureLevels();

        isBossLevel = detectBossLevel(currentLevelData);
        enemyManager.spawnEnemies(currentLevelData, levelIndex);
        enemyManager.setOnEnemyKilled(() -> score += 3);
    }

    private void validateDimensions(int vw, int vh, int ww, int wh) {
        if (vw <= 0 || vh <= 0)
            throw new InvalidLevelDataException("Viewport dimensions must be positive.");
        if (ww <= 0 || wh <= 0)
            throw new InvalidLevelDataException("World dimensions must be positive.");
    }

    private boolean detectBossLevel(LevelData data) {
        return data.getEnemies().stream().anyMatch(s -> s.type == EnemiesType.BAZOOKA_BOSS);
    }

    public void update(LevelInput inputState, double delta) {
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
        player.update(delta);

        handleWorldBounds();
        collisionHandler.handlePlayerPlatformCollisions(
            player, currentLevelData.getPlatforms(), previousX, previousY
        );

        for (Item item : currentLevelData.getItems()) {
            if (item.isCollected()) continue;
            if (player.getBounds().intersects(item.bounds)) {
                item.markCollected();
                switch (item.type) {
                    case COIN:
                        score += 1;
                        break;
                    case HEALTH:
                        break;
                }
            }
        }

        enemyManager.update(currentLevelData.getPlatforms(), player);
        contactDamageCooldown = enemyManager.handleEnemyPlayerInteraction(player, contactDamageCooldown);
        if (contactDamageCooldown > 0) contactDamageCooldown--;
        enemyManager.handleWeaponHits(player, currentLevelData.getPlatforms());

        //boss level completes when the boss is dead, not when player reaches exit
        if (!levelComplete && isBossLevel) {
            if (enemyManager.areAllEnemiesDead()) {
                levelComplete = true;
                onLevelComplete.run();
            }
            return;
        }

        if (!levelComplete && currentLevelData.getExitZone() != null) {
            if (player.getBounds().intersects(currentLevelData.getExitZone())) {
                if (score >= currentLevelData.getMinScore()) {
                    levelComplete = true;
                    onLevelComplete.run();
                }
            }
        }
    }

    private void handleWorldBounds() {
        if (player.getX() < 0) {
            player.setX(0);
        } else if (player.getX() + player.getWidth() > worldWidth) {
            player.setX(worldWidth - player.getWidth());
        }

        if (player.getY() < 0) {
            player.setY(0);
            player.setVerticalVelocity(0);
        } else if (player.getY() > worldHeight) {
            player.die();
        }
    }

    public void updateEnemiesOnly() {
        enemyManager.update(currentLevelData.getPlatforms(), player);
    }

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

    public List<Rocket> getRockets() {
        return enemyManager.getRockets();
    }

    public boolean isBossLevel() {
        return isBossLevel;
    }

    public PlayerSaveData buildSaveData() {
        return new PlayerSaveData(
            player.getHealth(),
            score,
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
        score            = save.score;

        player.respawn(save.x, save.y);
        player.setHealth(save.health);

        isBossLevel = detectBossLevel(currentLevelData);
        enemyManager.loadEnemies(save.enemies);
        enemyManager.setOnEnemyKilled(() -> score += 3);
    }

    public int getGoldCount()  { return goldCount;  }
    public int getScore()      { return score;      }
    public int getWorldWidth() { return worldWidth; }
    public int getMinScore()   { return currentLevelData.getMinScore();  }
    public String getNextLevel(){ return currentLevelData.getNextLevel(); }
    public String getBackgroundImage() { return currentLevelData.getBackgroundImage(); }
    public Rectangle getExitZone() { return currentLevelData.getExitZone(); }

    public void loadNextLevel() {
        String nextFile = currentLevelData.getNextLevel();
        if (nextFile == null) return;

        currentLevelFile = nextFile;
        currentLevelData = LevelDataLoader.load(currentLevelFile, worldWidth, worldHeight);
        levelComplete    = false;

        worldWidth  = currentLevelData.getWorldWidth();
        worldHeight = currentLevelData.getWorldHeight();
        levelIndex++;

        float spawnX = currentLevelData.getSpawnX();
        float spawnY = currentLevelData.getSpawnY();
        player.respawn(spawnX, spawnY);

        isBossLevel = detectBossLevel(currentLevelData);
        enemyManager.spawnEnemies(currentLevelData, levelIndex);
        enemyManager.setOnEnemyKilled(() -> score += 3);
    }
}
