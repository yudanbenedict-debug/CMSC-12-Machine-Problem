package GameCreation;

import Entities.EnemyFolder.Enemies;
import Entities.Player;
import GamePlatform.Platform;
import Handlers.InputHandler;
import Loaders.TileAssetLoader;

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
        setBackground(new Color(105, 193, 255));
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
        // Wire the player's death callback to start our delay timer
        level.getPlayer().setOnDeathCallback(() -> {
            playerDead      = true;
            deathDelayTimer = DEATH_DISPLAY_DELAY;
        });
    }

    public void start() {
        if (running) return;
        running    = true;
        gameThread = new Thread(this, "engine-test-loop");
        gameThread.start();
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
        requestFocusInWindow(); // regain input
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
    public void respawnPlayer() {
        playerDead = false;
        deathPanel.setVisible(false);
        level.respawnPlayer();
        // re-wire callback after respawn
        level.getPlayer().setOnDeathCallback(() -> {
            playerDead      = true;
            deathDelayTimer = DEATH_DISPLAY_DELAY;
        });
        requestFocusInWindow();
    }
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
                SwingUtilities.invokeLater(() -> deathPanel.setVisible(true));
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

        // ── Items ─────────────────────────────────────────────────────────────
        g2.setColor(new Color(245, 220, 80));
        for (Rectangle item : level.getItemZones()) {
            g2.fillOval(item.x - cameraX, item.y, item.width, item.height);
        }

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
    }
}