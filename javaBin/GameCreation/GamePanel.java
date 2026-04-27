package GameCreation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import Entities.Enemies;
import Entities.Player;
import GamePlatform.Platform;

public class GamePanel extends JPanel implements Runnable {
    private static final int    TARGET_FPS       = 60;
    private static final double NANOS_PER_UPDATE = 1_000_000_000.0 / TARGET_FPS;

    private final Level   level;
    private Thread        gameThread;
    private volatile boolean running;

    private volatile boolean moveLeft;
    private volatile boolean moveRight;
    private volatile boolean jumpHeld;

    private int cameraX;

    public GamePanel(int viewportWidth, int viewportHeight) {
        setPreferredSize(new Dimension(viewportWidth, viewportHeight));
        setBackground(new Color(105, 193, 255));
        setFocusable(true);
        setDoubleBuffered(true);
        level = new Level(viewportWidth, viewportHeight);
        installInput();
    }

    public void start() {
        if (running) return;
        running    = true;
        gameThread = new Thread(this, "engine-test-loop");
        gameThread.start();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    private void installInput() {
        InputMap  inputMap  = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        bindKey(inputMap, actionMap, "pressed A",     "left-pressed",       true,  false, false);
        bindKey(inputMap, actionMap, "released A",    "left-released",      false, false, false);
        bindKey(inputMap, actionMap, "pressed LEFT",  "leftArrow-pressed",  true,  false, false);
        bindKey(inputMap, actionMap, "released LEFT", "leftArrow-released", false, false, false);

        bindKey(inputMap, actionMap, "pressed D",      "right-pressed",       false, true,  false);
        bindKey(inputMap, actionMap, "released D",     "right-released",      false, false, false);
        bindKey(inputMap, actionMap, "pressed RIGHT",  "rightArrow-pressed",  false, true,  false);
        bindKey(inputMap, actionMap, "released RIGHT", "rightArrow-released", false, false, false);

        bindKey(inputMap, actionMap, "pressed SPACE",  "jump-space-pressed",  false, false, true);
        bindKey(inputMap, actionMap, "released SPACE", "jump-space-released", false, false, false);
        bindKey(inputMap, actionMap, "pressed W",      "jump-w-pressed",      false, false, true);
        bindKey(inputMap, actionMap, "released W",     "jump-w-released",     false, false, false);
        bindKey(inputMap, actionMap, "pressed UP",     "jump-up-pressed",     false, false, true);
        bindKey(inputMap, actionMap, "released UP",    "jump-up-released",    false, false, false);
    }

    private void bindKey(InputMap im, ActionMap am, String ks, String name,
                         boolean lv, boolean rv, boolean jv) {
        im.put(KeyStroke.getKeyStroke(ks), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (name.contains("left"))  moveLeft  = lv;
                else if (name.contains("right")) moveRight = rv;
                if (name.contains("jump"))  jumpHeld  = jv;
            }
        });
    }

    // ── Game loop ─────────────────────────────────────────────────────────────

    private void updateGame() {
        LevelInput input = new LevelInput(moveLeft, moveRight, jumpHeld);
        level.update(input);

        Player player    = level.getPlayer();
        int halfViewport = getWidth() / 2;
        int targetCamX   = (int) player.getX() - halfViewport + ((int) player.getWidth() / 2);
        int maxCamera    = Math.max(0, level.getWorldWidth() - getWidth());
        cameraX = Math.max(0, Math.min(targetCamX, maxCamera));
    }

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
        g2.setColor(new Color(120, 120, 120));
        for (Platform platform : level.getPlatforms()) {
            Rectangle b = platform.getBounds();
            g2.fillRoundRect(b.x - cameraX, b.y, b.width, b.height, 8, 8);
        }

        // ── Items ─────────────────────────────────────────────────────────────
        //still no logic for retrieving, implement asap
        g2.setColor(new Color(245, 220, 80));
        for (Rectangle item : level.getItemZones()) {
            g2.fillOval(item.x - cameraX, item.y, item.width, item.height);
        }

        //Enemies

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

    //for debugging
    private void drawHUD(Graphics2D g2) {
        // Player player = level.getPlayer();
        // g2.setColor(new Color(20, 20, 20));
        // g2.setFont(new Font("Monospaced", Font.BOLD, 15));
        // g2.drawString("Move: A/D or Arrow Keys  |  Jump: Space / W / Up", 24, 30);
        // g2.drawString("Player X: " + (int) player.getX()
        //               + "  Y: " + (int) player.getY(), 24, 52);
        // g2.drawString("Grounded: " + player.isGrounded()
        //               + "  HP: " + player.getHealth(), 24, 74);
        // g2.drawString("Enemies alive: " + level.getEnemies().size(), 24, 96);
    }
}