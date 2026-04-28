package GameCreation;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import Exceptions.InvalidLevelDataException;
import Entities.Enemies;
import Entities.EnemiesType;
import Entities.Player;
import GamePlatform.Platform;

public class Level {

    public static final int    DEFAULT_WORLD_WIDTH  = 4000;
    public static final int    DEFAULT_WORLD_HEIGHT = 720;
    //add player-data.txt, 
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

    // ─────────────────────────────────────────────────────────────────────────────

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

    //Handle enemy spawning
    private void spawnEnemies() {
        for (Rectangle spawn : currentLevelData.getEnemies()) {
            EnemiesType type;
            switch (spawn.width) {
                case 48:  type = EnemiesType.GOBLIN;   break;
                case 56:  type = EnemiesType.ORC;      break;
                case 64:  type = EnemiesType.SKELETON; break;
                default:  type = EnemiesType.SLIME;    break;
            }
            enemies.add(new Enemies(type, spawn.x, spawn.y));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Update loop
    // ─────────────────────────────────────────────────────────────────────────────

    public void update(LevelInput inputState) {
        player.applyEngineState(
            inputState.isMoveLeft(),
            inputState.isMoveRight(),
            inputState.isJumpPressed(),
            inputState.isRunning()
        );

        float previousX = player.getX();
        float previousY = player.getY();
        player.update();
        handleWorldBounds();
        handlePlayerPlatformCollisions(previousX, previousY);
        handleItemCollisions();

        updateEnemies();
        handleEnemyPlayerInteraction();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  World bounds
    // ─────────────────────────────────────────────────────────────────────────────

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

   //player collission with platform
   //to be fixed: platform bottom isn't registered.
    //debug

    // private void handlePlayerPlatformCollisions(float previousX, float previousY) {
    //     player.setGrounded(false);
    //     Rectangle playerBounds = player.getBounds();
    //     float previousBottom   = previousY + player.getHeight();
    //     float previousTop      = previousY;

    //     for (Platform platform : currentLevelData.getPlatforms()) {
    //         Rectangle pb      = platform.getBounds();
    //         float platLeft    = pb.x;
    //         float platRight   = pb.x + pb.width;
    //         float platTop     = pb.y;
    //         float platBottom  = pb.y + pb.height;

    //         float curBottom   = player.getY() + player.getHeight();
    //         float curLeft     = player.getX();
    //         float curRight    = player.getX() + player.getWidth();

    //         boolean overlapsH  = curLeft < platRight && curRight > platLeft;
    //         boolean crossesTop = previousBottom <= platTop + FLOOR_SNAP_TOLERANCE
    //                              && curBottom >= platTop;

    //         if (overlapsH && player.getVerticalVelocity() >= 0 && crossesTop) {
    //             player.setY(platTop - player.getHitboxOffsetY() - player.getHitboxHeight());
    //             player.setVerticalVelocity(0);
    //             player.setGrounded(true);
    //             playerBounds = player.getBounds();
    //         }

    //         if (!playerBounds.intersects(pb)) continue;

    //         // Vertical push-out
    //         if (previousBottom <= platTop) {
    //             player.setY(platTop - player.getHitboxOffsetY() - player.getHitboxHeight());
    //             player.setVerticalVelocity(0);
    //             player.setGrounded(true);
    //         } else if (previousTop >= platBottom) {
    //             player.setY(platBottom - player.getHitboxOffsetY());
    //             if (player.getVerticalVelocity() < 0) player.setVerticalVelocity(0);
    //         }

    //         playerBounds = player.getBounds();
    //     }
    // }

    // end of debug

    private void handlePlayerPlatformCollisions(float previousX, float previousY) {
        player.setGrounded(false);
        Rectangle playerBounds = player.getBounds();
    
        // Use hitbox bottom/top, not raw sprite coords
        float previousHitboxBottom = previousY + player.getHitboxOffsetY() + player.getHitboxHeight();
        float previousHitboxTop    = previousY + player.getHitboxOffsetY();
    
        for (Platform platform : currentLevelData.getPlatforms()) {
            Rectangle pb     = platform.getBounds();
            float platTop    = pb.y;
            float platBottom = pb.y + pb.height;
    
            float curBottom  = playerBounds.y + playerBounds.height;
            float curLeft    = playerBounds.x;
            float curRight   = playerBounds.x + playerBounds.width;
    
            boolean overlapsH  = curLeft < pb.x + pb.width && curRight > pb.x;
            boolean crossesTop = previousHitboxBottom <= platTop + FLOOR_SNAP_TOLERANCE
                                 && curBottom >= platTop;
    
            if (overlapsH && player.getVerticalVelocity() >= 0 && crossesTop) {
                // Snap feet (hitbox bottom) to platform top
                player.setY(platTop - player.getHitboxOffsetY() - player.getHitboxHeight());
                player.setVerticalVelocity(0);
                player.setGrounded(true);
                playerBounds = player.getBounds();
            }
    
            if (!playerBounds.intersects(pb)) continue;
    
            if (previousHitboxBottom <= platTop) {
                player.setY(platTop - player.getHitboxOffsetY() - player.getHitboxHeight());
                player.setVerticalVelocity(0);
                player.setGrounded(true);
            } else if (previousHitboxTop >= platBottom) {
                player.setY(platBottom - player.getHitboxOffsetY());
                if (player.getVerticalVelocity() < 0) player.setVerticalVelocity(0);
            }
    
            playerBounds = player.getBounds();
        }
    }
    private void handleItemCollisions() {
        Rectangle playerBounds = player.getBounds();
        for (Rectangle itemBounds : currentLevelData.getItems()) {
            if (playerBounds.intersects(itemBounds)) {
                // Placeholder: item collection logic goes here.
            }
        }
    }



    private void updateEnemies() {
        List<Platform> platforms = currentLevelData.getPlatforms();
        float px = player.getX() + player.getWidth()  / 2f;
        float py = player.getY() + player.getHeight() / 2f;

        Iterator<Enemies> it = enemies.iterator();
        while (it.hasNext()) {
            Enemies e = it.next();
            e.update(platforms, px, py);
            if (e.isDeathAnimationFinished()) {
                e.onDeath();    // hook for loot drops, score, sound, etc.
                it.remove();
            }
        }

        if (contactDamageCooldown > 0) contactDamageCooldown--;
    }

    //Stomping Interacrtion
    //Jumps on enemy then damages
    //Change this later using the weapon class.
    private void handleEnemyPlayerInteraction() {
        Rectangle playerBounds = player.getBounds();
        float playerBottom     = player.getY() + player.getHeight();
        float playerVelY       = player.getVerticalVelocity();

        for (Enemies e : enemies) {
            if (!e.isAlive()) continue;

            Rectangle eb = e.getBounds();
            if (!playerBounds.intersects(eb)) continue;

            float enemyTop   = eb.y;
            boolean stomping = playerVelY > 0
                               && playerBottom - playerVelY <= enemyTop + 8f;
            if (stomping) {
                float stompDmg = Math.max(5f, playerVelY * 1.5f);
                e.takeDamage(stompDmg);
                player.setVerticalVelocity(-8f);
                continue;   // skip contact damage this tick
            }

            if (contactDamageCooldown <= 0) {
                player.takeDamage(e.getDamage());
                contactDamageCooldown = CONTACT_DAMAGE_COOLDOWN;
            }

            if (e.isReadyToAttack()) {
                player.takeDamage(e.getDamage());
                contactDamageCooldown = CONTACT_DAMAGE_COOLDOWN;
            }
        }
    }

    //getters
    public Player getPlayer() { return player; }

    public List<Platform> getPlatforms() {
        return Collections.unmodifiableList(currentLevelData.getPlatforms());
    }

   
    public List<Rectangle> getEnemyZones() {
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