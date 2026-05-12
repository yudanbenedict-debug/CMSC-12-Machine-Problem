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

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GamePanel extends JPanel implements Runnable {
    private static final int    TARGET_FPS       = 60;
    private static final double NANOS_PER_UPDATE = 1_000_000_000.0 / TARGET_FPS; //about 16.67 millis ~ 60fps
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
        setBackground(Color.BLACK);

        setFocusable(true);
        setDoubleBuffered(true);
        setLayout(null);

        pauseMenu = new PausePanel(this);
        pauseMenu.setBounds(0, 0, viewportWidth, viewportHeight);
        pauseMenu.setVisible(false);
        add(pauseMenu);

       
        deathPanel = new DeathPanel(this);
        deathPanel.setBounds(0, 0, viewportWidth, viewportHeight);
        deathPanel.setVisible(false);
        add(deathPanel);
     

        level        = new Level(viewportWidth, viewportHeight);
        inputHandler = new InputHandler(this);

        // re-wire death callback
        level.getPlayer().setOnDeathCallback(() -> {
            playerDead = true;
            deathDelayTimer = DEATH_DISPLAY_DELAY;
        });

        
        level.setOnLevelComplete(() -> {
            if (level.getNextLevel() != null) {
                level.loadNextLevel();
                // re-wire death callback for new level's player state
                level.getPlayer().setOnDeathCallback(() -> {
                    playerDead      = true;
                    deathDelayTimer = DEATH_DISPLAY_DELAY;
                });
            } else {
                // add a "you win screen"
                returnToMenu();
            }
        });
    }

    public void start() {
        if (running) return;
        running    = true;
        gameThread = new Thread(this, "Island Escaper!");
        gameThread.start();
        MusicPlayer.play("Resources/Sounds/gameplay.wav");
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    public void pauseGame() {
        paused = true;
        pauseMenu.setVisible(true);
    }

    public void returnToMenu() {
        running = false;
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.getWindowAncestor(this).dispose();
            onReturnToMenu.run();
        });
    }

    public void confirmAndReturnToMenu() {
        Object[] options = { "Save and Exit", "Exit Without Saving", "Cancel" };
        int choice = JOptionPane.showOptionDialog(
            this,
            "Are you sure you want to exit without saving?",
            "Exit to Main Menu",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[0]
        );
        if (choice == 0) {         // Save and Exit
            gamePanel_saveGame();
            returnToMenu();
        } else if (choice == 1) {  // Exit Without Saving
            returnToMenu();
        }
        // choice == 2 or closed dialog = Cancel, do nothing
    }

    private void gamePanel_saveGame() {
        boolean ok = saveGame();
        if (!ok) {
            JOptionPane.showMessageDialog(this,
                "Save failed — exiting anyway.",
                "Save Error",
                JOptionPane.WARNING_MESSAGE);
        }
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


    @Override
    public void run() {
        //last update
        long   lastTime = System.nanoTime();
        //updates needed
        double delta    = 0.0;

        while (running) {
            //now - prev / 60fps is the delta
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
    //load past save -> discard previous save (deathCallBack)
    public void loadSaveOrMenu() {
        if (SaveManager.hasSave(1)) {
            level.loadFromSave(SaveManager.load(1));
            // re-wire death callback so future deaths still show the panel (updates player)
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
                SwingUtilities.invokeLater(() -> {
                    deathPanel.refresh();
                    deathPanel.setVisible(true);
                });
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

       //exit part
        Rectangle exit = level.getExitZone();
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
         //add coin and health. health will not be shwon
        for (Item item : level.getItems()) {
            if (item.isCollected()) continue;
            switch (item.type) {
                case COIN:   g2.setColor(new Color(245, 220, 80)); break;
                case HEALTH: g2.setColor(new Color(220, 60,  60)); break;
                default:     g2.setColor(Color.WHITE);             break;
            }
            g2.fillOval(item.getX() - cameraX, item.getY(), item.getWidth(), item.getHeight());
        }

        // ── Enemies ───────────────────────────────────────────────────────────
        Graphics2D enemyG = (Graphics2D) g2.create();
        enemyG.translate(-cameraX, 0);
        for (Enemies enemy : level.getEnemies()) {
            enemy.draw(enemyG);
        }
        enemyG.dispose();

        
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
        g2.drawString(" HP: " + player.getHealth(), 24, 30);
        g2.drawString("Enemies alive: " + level.getEnemies().size(), 24, 96);
        //hud for score
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
    }
}