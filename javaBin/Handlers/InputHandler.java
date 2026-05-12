package Handlers;


 
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
 
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import GameCreation.LevelInput;

public class InputHandler {
    private volatile boolean moveLeft;
    private volatile boolean moveRight;
    private volatile boolean jumpHeld;
    private volatile boolean isRunning;
    private volatile boolean attackPressed;
    private volatile boolean pausedPressed;
    private volatile int     weaponSlot = 1;
    private volatile boolean reloadPressed;

    public InputHandler(JComponent comp){
        installKeyboard(comp);
        installMouse(comp);
    }
    private void installKeyboard(JComponent target){
        InputMap  im = target.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = target.getActionMap();
 
        // ── Movement ──────────────────────────────────────────────────────────
        bindKey(im, am, "pressed A",     "left-pressed",       true,  false, false);
        bindKey(im, am, "released A",    "left-released",      false, false, false);
        bindKey(im, am, "pressed LEFT",  "leftArrow-pressed",  true,  false, false);
        bindKey(im, am, "released LEFT", "leftArrow-released", false, false, false);
 
        bindKey(im, am, "pressed D",      "right-pressed",       false, true,  false);
        bindKey(im, am, "released D",     "right-released",      false, false, false);
        bindKey(im, am, "pressed RIGHT",  "rightArrow-pressed",  false, true,  false);
        bindKey(im, am, "released RIGHT", "rightArrow-released", false, false, false);
 
        // ── Jump ──────────────────────────────────────────────────────────────
        bindKey(im, am, "pressed SPACE",  "jump-space-pressed",  false, false, true);
        bindKey(im, am, "released SPACE", "jump-space-released", false, false, false);
        bindKey(im, am, "pressed W",      "jump-w-pressed",      false, false, true);
        bindKey(im, am, "released W",     "jump-w-released",     false, false, false);
        bindKey(im, am, "pressed UP",     "jump-up-pressed",     false, false, true);
        bindKey(im, am, "released UP",    "jump-up-released",    false, false, false);
 
        // ── Sprint (P for debug, swap to SHIFT when ready) ───────────────────
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false), "sprint-pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, true),  "sprint-released");
        am.put("sprint-pressed",  new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { isRunning = true;  }
        });
        am.put("sprint-released", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { isRunning = false; }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), "pause-toggle");

        am.put("pause-toggle", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                pausedPressed = true;
            }
        });

 
        // ── Weapon slots ──────────────────────────────────────────────────────
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0, false), "slot-1");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0, false), "slot-2");
        am.put("slot-1", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { weaponSlot = 1; }
        });
        am.put("slot-2", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { weaponSlot = 2; }
        });
 
        // ── Reload ────────────────────────────────────────────────────────────
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0, false), "reload-pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0, true),  "reload-released");
        am.put("reload-pressed",  new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { reloadPressed = true;  }
        });
        am.put("reload-released", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { reloadPressed = false; }
        });
    }
    

    private void bindKey(InputMap im, ActionMap am, String ks, String name, boolean lv, boolean rv, boolean jv) {

        im.put(KeyStroke.getKeyStroke(ks), name);
        am.put(name, new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if      (name.contains("left"))   moveLeft  = lv;
            else if (name.contains("right"))  moveRight = rv;
            else if (name.contains("jump"))   jumpHeld  = jv;
            else if (name.contains("sprint")) isRunning = name.contains("pressed");
            }
        });
    }

    private void installMouse(JComponent target) {
        target.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) attackPressed = true;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) attackPressed = false;
            }
        });
    }

    /** Clears any pending pause signal — call this when resuming so a held ESC doesn't immediately re-pause. */
    public void consumePause() {
        pausedPressed = false;
    }

    public LevelInput buildInput() {
        boolean pause  = pausedPressed;
        boolean reload = reloadPressed;

        pausedPressed = false;
        reloadPressed = false;   // consume it — one press = one reload attempt
        return new LevelInput(
            moveLeft, moveRight, jumpHeld, isRunning,
            attackPressed, weaponSlot, reload, pause
        );
    }
}