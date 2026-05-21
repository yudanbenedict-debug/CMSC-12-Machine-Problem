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
import Weapons.Bullet;
import Weapons.Rocket;

import java.awt.Color;
import java.awt.Dimension;
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
    private static final int    DEATH_DISPLAY_DELAY = 10;

    private final Level        level;
    private final InputHandler inputHandler;
    private final HUDRenderer  hudRenderer = new HUDRenderer();
    private Thread             gameThread;
    private volatile boolean   running;
    private int                cameraX;
    private boolean            paused = false;
    private PausePanel         pauseMenu;
    private final Runnable     onReturnToMenu;

    private boolean    playerDead      = false;
    private int        deathDelayTimer = 0;
    private DeathPanel deathPanel;

    private boolean  playerWon  = false;
    private WinPanel winPanel;

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

        winPanel = new WinPanel(this);
        winPanel.setBounds(0, 0, viewportWidth, viewportHeight);
        winPanel.setVisible(false);
        add(winPanel);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = getWidth();
                int h = getHeight();
                pauseMenu.setBounds(0, 0, w, h);
                deathPanel.setBounds(0, 0, w, h);
                winPanel.setBounds(0, 0, w, h);
            }
        });

        level        = new Level(viewportWidth, viewportHeight);
        inputHandler = new InputHandler(this);

        level.getPlayer().setOnDeathCallback(() -> {
            playerDead      = true;
            deathDelayTimer = DEATH_DISPLAY_DELAY;
        });

        level.setOnLevelComplete(() -> SwingUtilities.invokeLater(() -> handleLevelComplete()));
    }

    public void start() {
        if (running) return;
        running    = true;
        gameThread = new Thread(this, "Island Escaper!");
        gameThread.start();
        MusicPlayer.play("assets/Sounds/gameplay.wav");
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
        paused = true;
        pauseMenu.setVisible(false);
        ModalDialog dlg = new ModalDialog(
            this,
            "Exit to Main Menu",
            "Are you sure you want to exit?",
            new String[]{"Save and Exit", "Exit Without Saving", "Cancel"},
            choice -> {
                if (choice == 0) {
                    boolean ok = saveGame();
                    if (!ok) {
                        ModalDialog errDlg = new ModalDialog(
                            this,
                            "Save Error",
                            "Save failed — exiting anyway.",
                            new String[]{"OK"},
                            c -> returnToMenu()
                        );
                        errDlg.showDialog();
                    } else {
                        returnToMenu();
                    }
                } else if (choice == 1) {
                    returnToMenu();
                } else {
                    resumeGame();
                }
            }
        );
        dlg.showDialog();
    }

    public void resumeGame() {
        paused = false;
        pauseMenu.setVisible(false);
        inputHandler.consumePause();
        requestFocusInWindow();
    }

    private void handleLevelComplete() {
        if (level.getNextLevel() != null) {
            level.loadNextLevel();
            cameraX = 0;
            level.getPlayer().setOnDeathCallback(() -> {
                playerDead      = true;
                deathDelayTimer = DEATH_DISPLAY_DELAY;
            });
            level.setOnLevelComplete(() -> SwingUtilities.invokeLater(() -> handleLevelComplete()));
        } else {
            //boss beaten and no next level — show the win screen
            playerWon = true;
            MusicPlayer.stop();
            SwingUtilities.invokeLater(() -> winPanel.setVisible(true));
        }
    }

    public boolean saveGame() {
        PlayerSaveData data = level.buildSaveData();
        return SaveManager.save(data, 1);
    }

    public boolean loadFromSave() {
        PlayerSaveData data = SaveManager.load(1);
        if (data == null) return false;
        level.loadFromSave(data);
        return true;
    }

    @Override
    public void run() {
        long   lastTime = System.nanoTime();
        double delta    = 0.0;

        while (running) {
            long now = System.nanoTime();
            delta   += (now - lastTime) / NANOS_PER_UPDATE;
            lastTime = now;

            while (delta >= 1.0) {
                updateGame(Math.min(delta, 2.0));
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

    public void loadSaveOrMenu() {
        if (SaveManager.hasSave(1)) {
            level.loadFromSave(SaveManager.load(1));
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

    private void updateGame(double delta) {
        LevelInput input = inputHandler.buildInput();

        if (input.isPausedPressed()) {
            if (paused) resumeGame();
            else        pauseGame();
        }

        if (paused || playerWon) return;

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

        level.update(input, delta);

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
        hudRenderer.draw(g2, level.getPlayer(), level, getWidth(), getHeight());
        g2.dispose();
    }

    private void drawLevel(Graphics2D g2) {
        BufferedImage bgImage = BackgroundLoader.get(level.getBackgroundImage());
        int vpW = getWidth();
        int vpH = getHeight();
        if (bgImage != null) {
            int imgW      = bgImage.getWidth();
            int parallaxX = (int)(cameraX * 0.4);
            int offsetX   = parallaxX % imgW;
            for (int tx = -offsetX; tx < vpW; tx += imgW) {
                g2.drawImage(bgImage, tx, 0, imgW, vpH, null);
            }
        } else {
            g2.setColor(new Color(105, 193, 255));
            g2.fillRect(0, 0, vpW, vpH);
        }

        int tileSize = TileAssetLoader.TILE_SIZE;
        for (Platform platform : level.getPlatforms()) {
            Rectangle     b         = platform.getBounds();
            BufferedImage tile      = TileAssetLoader.getTile(platform.getType());
            int           platRight = b.x + b.width;

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

        //only draw exit zone on non-boss levels
        if (!level.isBossLevel()) {
            Rectangle exit = level.getExitZone();
            if (exit != null) {
                boolean gateOpen = level.getScore() >= level.getMinScore();
                g2.setColor(gateOpen ? new Color(0, 255, 80, 60) : new Color(180, 0, 0, 60));
                g2.fillRect(exit.x - cameraX, 0, exit.width, getHeight());
                g2.setColor(gateOpen ? new Color(0, 200, 60, 160) : new Color(160, 0, 0, 160));
                g2.drawRect(exit.x - cameraX, 0, exit.width - 1, getHeight() - 1);
            }
        }

        for (Item item : level.getItems()) {
            if (item.isCollected()) continue;
            switch (item.type) {
                case COIN:   g2.setColor(new Color(245, 220, 80)); break;
                case HEALTH: g2.setColor(new Color(220, 60,  60)); break;
                default:     g2.setColor(Color.WHITE);             break;
            }
            g2.fillOval(item.getX() - cameraX, item.getY(), item.getWidth(), item.getHeight());
        }

        Graphics2D enemyG = (Graphics2D) g2.create();
        enemyG.translate(-cameraX, 0);
        for (Enemies enemy : level.getEnemies()) {
            enemy.draw(enemyG);
        }
        enemyG.dispose();

        //draw rockets in world space
        g2.setColor(new Color(255, 140, 0));
        for (Rocket r : level.getRockets()) {
            g2.fillOval((int) r.getX() - cameraX - 5, (int) r.getY() - 5, 10, 10);
        }

        for (Bullet b : level.getPlayer().getGun().getActiveBullets()) {
            b.draw(g2, cameraX);
        }

        Player player = level.getPlayer();
        Graphics2D playerG = (Graphics2D) g2.create();
        playerG.translate(-cameraX, 0);
        player.draw(playerG);
        playerG.dispose();
    }
}
