package GameCreation;

import Entities.Item;
import Entities.EnemyFolder.Enemies;
import Entities.Player;
import GamePlatform.Platform;
import Handlers.InputHandler;
import Loaders.MusicPlayer;
import Loaders.TileAssetLoader;
import Loaders.BackgroundLoader;
import DataLoader.PlayerSaveData;
import DataLoader.SaveManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GamePanel extends JPanel implements Runnable {
    private static final int    TARGET_FPS       = 60;
    private static final double NANOS_PER_UPDATE = 1_000_000_000.0 / TARGET_FPS;
    private static final int DEATH_DISPLAY_DELAY = 10;

    private final Level        level;
    private final InputHandler inputHandler;
    private Thread             gameThread;
    private volatile boolean   running;
    private int                cameraX;
    private boolean paused = false;
    private PausePanel pauseMenu;
    private final Runnable onReturnToMenu;

    private boolean   playerDead      = false;
    private int       deathDelayTimer = 0;
    private DeathPanel deathPanel;

    public GamePanel(int viewportWidth, int viewportHeight, Runnable onReturnToMenu) {

        this.onReturnToMenu = onReturnToMenu;

        setPreferredSize(new Dimension(viewportWidth, viewportHeight));
        /*/----- CHANGE: removed setBackground(solid blue) -----/
         * PURPOSE: The background is now drawn as an image in drawLevel().
         * Keeping a solid colour here would only show through if the image
         * fails to load — we set black as a neutral fallback instead.
         */
        setBackground(Color.BLACK);
        /*/----- END CHANGE -----/*/
        setFocusable(true);
        setDoubleBuffered(true);
        setLayout(null);

        pauseMenu = new PausePanel(this);
        pauseMenu.setBounds(0, 0, viewportWidth, viewportHeight);
        pauseMenu.setVisible(false);
        add(pauseMenu);

        // --------------------------------------------------
        deathPanel = new DeathPanel(this);
        deathPanel.setBounds(0, 0, viewportWidth, viewportHeight);
        deathPanel.setVisible(false);
        add(deathPanel);
        // --------------------------------------------------

        level        = new Level(viewportWidth, viewportHeight);
        inputHandler = new InputHandler(this);

        // --------------------------------------------------
        // re-wire death callback
        level.getPlayer().setOnDeathCallback(() -> {
            playerDead      = true;
            deathDelayTimer = DEATH_DISPLAY_DELAY;
        });

        /*/----- CHANGE: wire level complete callback -----/
         * PURPOSE: Level fires onLevelComplete when the player reaches the
         * exit with enough score. GamePanel handles it here: if a next level
         * exists, loadNextLevel() is called and the game continues seamlessly
         * with score carried over. If nextLevel is null it's the final level
         * and returnToMenu() is called as a temporary placeholder — a proper
         * "You Win" screen can replace this later.
         */
        level.setOnLevelComplete(() -> {
            if (level.getNextLevel() != null) {
                level.loadNextLevel();
                // re-wire death callback for new level's player state
                level.getPlayer().setOnDeathCallback(() -> {
                    playerDead      = true;
                    deathDelayTimer = DEATH_DISPLAY_DELAY;
                });
            } else {
                // TODO: show a proper "You Win" screen
                returnToMenu();
            }
        });
        /*/----- END CHANGE -----/*/
    }

    public void start() {
        if (running) return;
        running    = true;
        gameThread = new Thread(this, "engine-test-loop");
        gameThread.start();
        MusicPlayer.play("Resources/Sounds/gameplay.wav");
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    public void pauseGame() {
        paused = true;
        pauseMenu.setVisible(true);
    }

    public void returnToMenu() {
        // --------------------------------------------------
        running = false;
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.getWindowAncestor(this).dispose();
            onReturnToMenu.run();
        });
        // --------------------------------------------------
    }

    public void resumeGame() {
        paused = false;
        pauseMenu.setVisible(false);
        inputHandler.consumePause(); // prevent ESC held during pause from re-pausing immediately
        requestFocusInWindow();
    }

    /** Saves current game state to slot 1. Returns true on success. */
    public boolean saveGame() {
        PlayerSaveData data = level.buildSaveData();
        return SaveManager.save(data, 1);
    }

    /** Loads save slot 1 into the running level. Returns true if a save existed. */
    public boolean loadFromSave() {
        PlayerSaveData data = SaveManager.load(1);
        if (data == null) return false;
        level.loadFromSave(data);
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Game loop
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void run() {
        long   lastTime = System.nanoTime();
        double delta    = 0.0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / NANOS_PER_UPDATE;
            lastTime = now;

            while (delta >= 1.0) {
                updateGame();
                delta -= 1.0;
            }

            repaint();

            try { Thread.sleep(2L); }
            catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    /*/----- CHANGE: replaced respawnPlayer() with loadSaveOrMenu() -----/
     * PURPOSE: respawnPlayer() put the player back at a hardcoded viewport
     * center with full health — completely ignoring the save system. This
     * made saving pointless because dying had no real consequence.
     *
     * loadSaveOrMenu() instead checks whether a save file exists in slot 1:
     *   - If YES: restores the full game state (player position, health,
     *     level layout, enemies) from the save, re-wires the death callback
     *     so dying again will re-trigger this flow, hides the death panel,
     *     and returns keyboard focus to the game.
     *   - If NO:  calls returnToMenu() so the player is not left stuck on a
     *     death screen with no usable button.
     *
     * The death callback must be re-wired after loadFromSave() because
     * loadFromSave() calls player.respawn(), which resets player state but
     * does NOT touch the callback — so re-wiring here is safe and necessary
     * to ensure the next death also triggers the panel correctly.
     *
     * USAGE: Called by DeathPanel's "Load Last Save" button.
     */
    public void loadSaveOrMenu() {
        if (SaveManager.hasSave(1)) {
            level.loadFromSave(SaveManager.load(1));
            // re-wire death callback so future deaths still show the panel
            level.getPlayer().setOnDeathCallback(() -> {
                playerDead      = true;
                deathDelayTimer = DEATH_DISPLAY_DELAY;
            });
            playerDead = false;
            SwingUtilities.invokeLater(() -> deathPanel.setVisible(false));
            requestFocusInWindow();
        } else {
            returnToMenu();
        }
    }
    /*/----- END CHANGE -----/*/

    private void updateGame() {
        LevelInput input = inputHandler.buildInput();

        if (input.isPausedPressed()) {
            if (paused) resumeGame();
            else pauseGame();
        }

        if (paused) return;

        if (playerDead) {
            level.updateEnemiesOnly();
            if (deathDelayTimer > 0) {
                deathDelayTimer--;
            } else {
                /*/----- CHANGE: added deathPanel.refresh() before setVisible(true) -----/
                 * PURPOSE: The death panel is constructed once at game start, so
                 * its button/label state could be stale by the time the player
                 * dies. refresh() re-checks SaveManager.hasSave(1) at the exact
                 * moment the panel is about to appear, ensuring the "Load Last
                 * Save" button is only enabled when there is actually something
                 * to load. Without this call the button could be enabled on a
                 * first death (before any save exists) or disabled after a save
                 * was created mid-session.
                 */
                SwingUtilities.invokeLater(() -> {
                    deathPanel.refresh();
                    deathPanel.setVisible(true);
                });
                /*/----- END CHANGE -----/*/
            }
            return;
        }

        level.update(input);

        Player player    = level.getPlayer();
        int halfViewport = getWidth() / 2;
        int targetCamX   = (int) player.getX() - halfViewport + ((int) player.getWidth() / 2);
        int maxCamera    = Math.max(0, level.getWorldWidth() - getWidth());
        cameraX = Math.max(0, Math.min(targetCamX, maxCamera));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Rendering
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawLevel(g2);
        drawHUD(g2);
        g2.dispose();
    }

    private void drawLevel(Graphics2D g2) {
        /*/----- CHANGE: draw background image before platforms -----/
         * PURPOSE: Previously the background was just the solid colour set in
         * the constructor. Now each level declares a background image in its
         * .properties file (e.g. background=Jungle.png). The image is loaded
         * once via BackgroundLoader and drawn to fill the entire viewport.
         *
         * Parallax: the image scrolls at half the camera speed so it appears
         * to sit behind the gameplay layer, giving a sense of depth.
         * If no image is available (null or missing file) we fall back to a
         * sky-blue fill so the screen is never blank.
         *
         * The image is tiled horizontally so wide levels never run out of
         * background. Vertical tiling is skipped — the image is always
         * stretched to fill the viewport height.
         */
        BufferedImage bgImage = BackgroundLoader.get(level.getBackgroundImage());
        int vpW = getWidth();
        int vpH = getHeight();
        if (bgImage != null) {
            int imgW = bgImage.getWidth();
            // Parallax: scroll at 40% of camera speed
            int parallaxX = (int)(cameraX * 0.4);
            // tile offset within the image
            int offsetX = parallaxX % imgW;
            // draw enough tiles to cover the viewport width
            for (int tx = -offsetX; tx < vpW; tx += imgW) {
                g2.drawImage(bgImage, tx, 0, imgW, vpH, null);
            }
        } else {
            // fallback solid sky colour
            g2.setColor(new Color(105, 193, 255));
            g2.fillRect(0, 0, vpW, vpH);
        }
        /*/----- END CHANGE -----/*/

        // ── Platforms ─────────────────────────────────────────────────────────
        int tileSize = TileAssetLoader.TILE_SIZE;
        for (Platform platform : level.getPlatforms()) {
            Rectangle b       = platform.getBounds();
            BufferedImage tile = TileAssetLoader.getTile(platform.getType());
            int platRight     = b.x + b.width;

            for (int tileX = b.x; tileX < platRight; tileX += tileSize) {
                int drawX    = tileX - cameraX;
                int colWidth = Math.min(tileSize, platRight - tileX);

                for (int tileY = b.y; tileY < b.y + b.height; tileY += tileSize) {
                    int rowHeight = Math.min(tileSize, b.y + b.height - tileY);
                    g2.drawImage(tile,
                        drawX,            tileY,
                        drawX + colWidth, tileY + rowHeight,
                        0,        0,
                        colWidth, rowHeight,
                        null);
                }
            }
        }

        // ── Exit zone ──────────────────────────────────────────────────────────
        /*/----- CHANGE: render exit zone as a colored overlay -----/
         * PURPOSE: Without a visual the player has no idea the exit exists.
         * The exit zone is drawn as a semi-transparent vertical strip —
         * green if the score gate is met (walk in to advance), red/dark if
         * not yet met (gate is locked). getExitZone() returns null only if
         * somehow no exit is defined, so we null-check before drawing.
         */
        java.awt.Rectangle exit = level.getExitZone();
        if (exit != null) {
            boolean gateOpen = level.getScore() >= level.getMinScore();
            g2.setColor(gateOpen
                ? new Color(0, 255, 80,  60)   // green tint — open
                : new Color(180, 0,  0,  60));  // red tint  — locked
            g2.fillRect(exit.x - cameraX, exit.y, exit.width, exit.height);
            // draw a border so it's visible even at low opacity
            g2.setColor(gateOpen
                ? new Color(0, 200, 60,  160)
                : new Color(160, 0, 0,   160));
            g2.drawRect(exit.x - cameraX, exit.y, exit.width - 1, exit.height - 1);
        }
        /*/----- END CHANGE -----/*/
        /*/----- CHANGE: renderer now uses List<Item> and skips collected items -----/
         * PURPOSE: Items were previously raw Rectangles so there was no way
         * to know if one had been picked up — collected coins stayed visible.
         * Now we check item.isCollected() before drawing, so coins disappear
         * on pickup. Color branches on item.type so future item types (e.g.
         * HEALTH drawn in red) render differently with no extra renderer code.
         */
        for (Item item : level.getItems()) {
            if (item.isCollected()) continue;
            switch (item.type) {
                case COIN:   g2.setColor(new Color(245, 220, 80)); break;
                case HEALTH: g2.setColor(new Color(220, 60,  60)); break;
                default:     g2.setColor(Color.WHITE);             break;
            }
            g2.fillOval(item.getX() - cameraX, item.getY(), item.getWidth(), item.getHeight());
        }
        /*/----- END CHANGE -----/*/

        // ── Enemies ───────────────────────────────────────────────────────────
        Graphics2D enemyG = (Graphics2D) g2.create();
        enemyG.translate(-cameraX, 0);
        for (Enemies enemy : level.getEnemies()) {
            enemy.draw(enemyG);
        }
        enemyG.dispose();

        // ── Player ────────────────────────────────────────────────────────────
        Player player = level.getPlayer();
        Graphics2D playerG = (Graphics2D) g2.create();
        playerG.translate(-cameraX, 0);
        player.draw(playerG);
        playerG.dispose();
    }

    private void drawHUD(Graphics2D g2) {
        Player player = level.getPlayer();
        g2.setColor(new Color(20, 20, 20));
        g2.setFont(new Font("Monospaced", Font.BOLD, 15));
        g2.drawString("Move: A/D or Arrow Keys  |  Jump: Space / W / Up", 24, 30);
        g2.drawString("Player X: " + (int) player.getX()
                      + "  Y: " + (int) player.getY(), 24, 52);
        g2.drawString("Grounded: " + player.isGrounded()
                      + "  HP: " + player.getHealth(), 24, 74);
        g2.drawString("Enemies alive: " + level.getEnemies().size(), 24, 96);

        /*/----- CHANGE: HUD now shows score and min score gate -----/
         * PURPOSE: Players need to know the score gate so they understand why
         * the exit might not be working. Score is shown top-right. Below it,
         * the min score requirement is shown in green if met (exit is open)
         * or red if not yet met (exit is locked). This gives clear feedback
         * without needing a separate UI panel.
         */
        g2.setFont(new Font("Monospaced", Font.BOLD, 20));
        String scoreText = "SCORE: " + level.getScore();
        int scoreWidth = g2.getFontMetrics().stringWidth(scoreText);
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, getWidth() - scoreWidth - 24, 36);

        int    minScore  = level.getMinScore();
        String gateText  = "EXIT MIN: " + minScore;
        int    gateWidth = g2.getFontMetrics().stringWidth(gateText);
        g2.setColor(level.getScore() >= minScore
                    ? new Color(80, 220, 80)   // green = exit open
                    : new Color(220, 80, 80));  // red = exit locked
        g2.drawString(gateText, getWidth() - gateWidth - 24, 62);
        /*/----- END CHANGE -----/*/
    }
}