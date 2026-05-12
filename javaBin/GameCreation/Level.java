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

    private int score = 0;
   
    private Runnable onLevelComplete  = () -> {};
    private boolean  levelComplete    = false;

    public void setOnLevelComplete(Runnable callback) {
        this.onLevelComplete = callback;
    }
   
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

        this.currentLevelData = LevelDataLoader.load(currentLevelFile, worldWidth, worldHeight);
  
        this.futureLevels     = FutureLevelCatalog.loadFutureLevels();

        enemyManager.spawnEnemies(currentLevelData);

        enemyManager.setOnEnemyKilled(() -> score += 3);
    }

    private void validateDimensions(int vw, int vh, int ww, int wh) {
        if (vw <= 0 || vh <= 0)
            throw new InvalidLevelDataException("Viewport dimensions must be positive.");
        if (ww < vw || wh < vh)
            throw new InvalidLevelDataException("World dimensions must be >= viewport dimensions.");
    }

    
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

        enemyManager.update(currentLevelData.getPlatforms(), player);
        contactDamageCooldown = enemyManager.handleEnemyPlayerInteraction(player, contactDamageCooldown);
        if (contactDamageCooldown > 0) contactDamageCooldown--;
        enemyManager.handleWeaponHits(player, currentLevelData.getPlatforms());

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
        } else if (player.getY() + player.getHeight() > worldHeight) {
            player.setY(worldHeight - player.getHeight());
            player.setVerticalVelocity(0);
            player.setGrounded(true);
        }
    }

    
    /** Ticks only enemies so the world keeps moving behind the red overlay. */
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

  
    public String getBackgroundImage() { return currentLevelData.getBackgroundImage(); }
 
    public Rectangle getExitZone() { return currentLevelData.getExitZone(); }

    //next level
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