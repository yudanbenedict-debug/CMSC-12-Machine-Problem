package Animators;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import Entities.EnemyFolder.Enemies;
import Entities.EnemyFolder.EnemiesType;
import Loaders.EnemySpriteLoader;


public class EnemyAnimator {

    public static class SnapShot{

        public final boolean alive;
        public final int hitFlashTimer;
        public final Entities.EnemyFolder.Enemies.AIState aiState;
        public final int attackCD;
        public final float velX;
        public final int alertTimer;

        public SnapShot(boolean alive, int hitFlashTimer, Entities.EnemyFolder.Enemies.AIState aiState, int atk_cd, float velX, int alertTimer){
            this.alive        = alive;
            this.hitFlashTimer = hitFlashTimer;
            this.aiState      = aiState;
            this.attackCD     = atk_cd;
            this.velX         = velX;
            this.alertTimer   = alertTimer;
        }

    }
    private static final int ATK_CD = 50; //millis

    private final HashMap<String, Animation> animations = new HashMap<>();
    private Animation currAnim;
    private String currAnimKey = "";

    public EnemyAnimator(EnemiesType type){
        EnemySpriteLoader.EnemyFrameSet frames  = EnemySpriteLoader.get(type);
        animations.put("walk",    new Animation(frames.walk,   6, true));
        animations.put("idle",    new Animation(frames.idle,   3, true));
        animations.put("chase",   new Animation(frames.chase,  5, true));
        animations.put("alert",   new Animation(frames.alert,  4, false)); // plays once then holds last frame
        animations.put("attack",  new Animation(frames.attack, 4, true));
        animations.put("damaged", new Animation(frames.hurt,   1, true));
        animations.put("death",   new Animation(frames.death,  5, true));

       
        switchAnim("walk");
    }
    public void switchAnim(String key){
        if (key.equals(currAnimKey)) return;
        Animation next = animations.get(key);
        if (next == null) return;
        next.resetanimation();
        currAnim   = next;
        currAnimKey = key;
    }
    public void updateAnim(SnapShot s){
        String key = resolveState(s);
        switchAnim(key);
        if(currAnim != null) currAnim.animate();
    }

    private String resolveState(SnapShot s) {
        if (!s.alive)                                    return "death";
        if (s.hitFlashTimer > 0)                         return "damaged";
        if (s.aiState == Enemies.AIState.ALERT)          return "alert";
        if (s.aiState == Enemies.AIState.CHASE)          return "chase";
        if (s.aiState == Enemies.AIState.ATTACK
                && s.attackCD > ATK_CD - 20)             return "attack";
        if (Math.abs(s.velX) > 0.1f)                     return "walk";
        return "idle";
    }

    //draw

     /**
     * Draws the current frame with horizontal flip and hit-flash overlay.
     *
     * @param g            Graphics context already translated by -cameraX
     * @param x            enemy world X
     * @param y            enemy world Y
     * @param width        sprite draw width
     * @param height       sprite draw height
     * @param facingRight  whether the enemy faces right
     * @param hitFlashTimer ticks remaining on the hurt flash (0 = no flash)
     */
    public void draw(Graphics g, int x, int y, int width, int height, boolean facingRight, int hitFlashTimer) {
        if (currAnim == null) return;
        BufferedImage frame = currAnim.getCurrentFrame();
        if (frame == null) return;
        if (!(g instanceof Graphics2D)) return;
        Graphics2D g2 = (Graphics2D) g;

        // ── Hit-flash red overlay ─────────────────────────────────────────────
        if (hitFlashTimer > 0) {
        Composite orig = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2.setColor(new Color(255, 60, 60));
        g2.fillRect(x, y, width, height);
        g2.setComposite(orig);
        }

        // ── Sprite with horizontal flip ───────────────────────────────────────
        Graphics2D sg = (Graphics2D) g2.create();
        if (facingRight) {
        sg.drawImage(frame, x, y, width, height, null);
        } else {
        sg.translate(x + width, y);
        sg.scale(-1.0, 1.0);
        sg.drawImage(frame, 0, 0, width, height, null);
        }
        sg.dispose();
    }
    public String    getCurrentAnimKey() { return currAnimKey; }
    public Animation getCurrentAnim()    { return currAnim;    }
 
    /** Used by Enemies to check whether the death animation has fully played. */
    public boolean isFinished() {
        return currAnim != null && currAnim.isFinished();
    }
}