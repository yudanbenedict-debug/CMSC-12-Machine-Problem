package GameCreation;

import Entities.Player;
import Weapons.Gun;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;


public class HUDRenderer {
    private static final int   MARGIN         = 20;

    // Health bar
    private static final int   HP_BAR_W       = 160;
    private static final int   HP_BAR_H       = 14;
    private static final int   HP_BAR_X       = MARGIN + 22;   // leave room for icon
    private static final int   HP_BAR_Y       = 18;
    private static final float PLAYER_MAX_HP  = 20f;            // matches Player constructor

    // Ammo row
    private static final int   AMMO_ROW_Y     = 52;
    private static final int   PIP_W          = 18;             // each ammo-block width
    private static final int   PIP_H          = 10;
    private static final int   PIP_GAP        = 3;
    private static final int   CLIP_DOT_R     = 7;              // clip indicator circle radius
    private static final int   CLIP_DOT_GAP   = 4;

    // Reload bar
    private static final int   RELOAD_BAR_W   = 130;
    private static final int   RELOAD_BAR_H   = 8;
    private static final int   RELOAD_BAR_Y   = AMMO_ROW_Y + PIP_H + 10;

    // Fonts
    private static final Font  FONT_LABEL     = new Font("Monospaced", Font.BOLD, 13);
    private static final Font  FONT_SCORE     = new Font("Monospaced", Font.BOLD, 20);
    private static final Font  FONT_RELOAD    = new Font("Monospaced", Font.BOLD, 11);
    private static final Font  FONT_ENEMY     = new Font("Monospaced", Font.BOLD, 13);

    // Colours
    private static final Color COL_HP_FULL    = new Color(60,  200,  80);
    private static final Color COL_HP_MID     = new Color(220, 180,  40);
    private static final Color COL_HP_LOW     = new Color(210,  50,  50);
    private static final Color COL_BAR_BG     = new Color( 30,  30,  30, 160);
    private static final Color COL_BAR_BORDER = new Color(200, 200, 200,  90);
    private static final Color COL_AMMO_FULL  = new Color(220, 210,  70);   // bullet pip — loaded
    private static final Color COL_AMMO_EMPTY = new Color( 60,  60,  60, 160); // bullet pip — spent
    private static final Color COL_CLIP_AVAIL = new Color(180, 180, 220);
    private static final Color COL_CLIP_USED  = new Color( 50,  50,  50, 140);
    private static final Color COL_RELOAD_FG  = new Color( 80, 160, 255);
    private static final Color COL_RELOAD_BG  = new Color( 30,  30,  30, 180);
    private static final Color COL_RELOAD_TXT = new Color(200, 230, 255);
    private static final Color COL_SHADOW     = new Color(  0,   0,   0, 120);
    private static final Color COL_ENEMY_TXT  = new Color( 20,  20,  20);

    public void draw(Graphics2D g2, Player player, Level level, int vpW, int vpH) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawHealthBar(g2, player);
        drawWeaponHud(g2, player);
        drawEnemyCount(g2, level);
        drawScoreHud(g2, level, vpW);
    }

    

    private void drawHealthBar(Graphics2D g2, Player player) {
        float hp    = Math.max(0, player.getHealth());
        float ratio = hp / PLAYER_MAX_HP;

        // heart icon
        g2.setFont(FONT_LABEL);
        g2.setColor(COL_HP_LOW);
        g2.drawString("♥", MARGIN, HP_BAR_Y + HP_BAR_H - 1);

        int bx = HP_BAR_X;
        int by = HP_BAR_Y;

        // bar background
        g2.setColor(COL_BAR_BG);
        fillRoundBar(g2, bx, by, HP_BAR_W, HP_BAR_H);

        // bar fill — colour shifts green → yellow → red
        Color fillCol = ratio > 0.5f ? COL_HP_FULL
                      : ratio > 0.25f ? COL_HP_MID
                      : COL_HP_LOW;
        int fillW = Math.max(0, (int)(HP_BAR_W * ratio));
        g2.setColor(fillCol);
        fillRoundBar(g2, bx, by, fillW, HP_BAR_H);

        // border
        g2.setColor(COL_BAR_BORDER);
        g2.setStroke(new BasicStroke(1f));
        drawRoundBarOutline(g2, bx, by, HP_BAR_W, HP_BAR_H);

        // HP number label inside / beside bar
        String hpText = (int) hp + " / " + (int) PLAYER_MAX_HP;
        g2.setFont(FONT_LABEL);
        g2.setColor(COL_SHADOW);
        g2.drawString(hpText, bx + HP_BAR_W + 7 + 1, by + HP_BAR_H - 1);
        g2.setColor(Color.WHITE);
        g2.drawString(hpText, bx + HP_BAR_W + 7, by + HP_BAR_H - 2);
    }

    private void drawWeaponHud(Graphics2D g2, Player player) {
        int slot = player.get_activeslots();

        if (slot == 1) {
            drawGunHud(g2, player.getGun());
        } else {
            //short label for sword
            g2.setFont(FONT_LABEL);
            g2.setColor(new Color(200, 200, 200));
            g2.drawString("⚔  SWORD", MARGIN, AMMO_ROW_Y + PIP_H);
        }
    }

    private void drawGunHud(Graphics2D g2, Gun gun) {
        int   ammo      = gun.getAmmo();
        int   maxAmmo   = gun.getMaxAmmo();
        int   clips     = gun.getClips();
        int   maxClips  = gun.getMaxClips();
        boolean reloading = gun.isReloading();

        int cursorX = MARGIN;
        int pipY    = AMMO_ROW_Y;

        
        for (int i = 0; i < maxAmmo; i++) {
            boolean loaded = (i < ammo);
            g2.setColor(loaded ? COL_AMMO_FULL : COL_AMMO_EMPTY);
            g2.fillRoundRect(cursorX, pipY, PIP_W, PIP_H, 4, 4);

            // thin border
            g2.setColor(new Color(0, 0, 0, 80));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(cursorX, pipY, PIP_W, PIP_H, 4, 4);

            cursorX += PIP_W + PIP_GAP;
        }

        // small separator gap
        cursorX += 6;

        //dots are mags remaining
        int dotCenterY = pipY + PIP_H / 2;
        for (int i = 0; i < maxClips; i++) {
            boolean hasClip = (i < clips);
            // shadow
            g2.setColor(COL_SHADOW);
            g2.fillOval(cursorX + 1, dotCenterY - CLIP_DOT_R + 1, CLIP_DOT_R * 2, CLIP_DOT_R * 2);
            // fill
            g2.setColor(hasClip ? COL_CLIP_AVAIL : COL_CLIP_USED);
            g2.fillOval(cursorX, dotCenterY - CLIP_DOT_R, CLIP_DOT_R * 2, CLIP_DOT_R * 2);
            // rim
            g2.setColor(hasClip ? new Color(220, 220, 255) : new Color(80, 80, 80));
            g2.setStroke(new BasicStroke(1f));
            g2.drawOval(cursorX, dotCenterY - CLIP_DOT_R, CLIP_DOT_R * 2, CLIP_DOT_R * 2);

            cursorX += CLIP_DOT_R * 2 + CLIP_DOT_GAP;
        }

        g2.setFont(FONT_LABEL);
        String ammoText = ammo + " / " + maxAmmo;
        g2.setColor(COL_SHADOW);
        g2.drawString(ammoText, MARGIN + 1, AMMO_ROW_Y + PIP_H + 13 + 1);
        g2.setColor(reloading ? new Color(160, 160, 160) : Color.WHITE);
        g2.drawString(ammoText, MARGIN, AMMO_ROW_Y + PIP_H + 13);

        if (reloading) {
            drawReloadBar(g2, gun.getReloadProgress());
        } else if (ammo == 0 && clips > 0) {
            // prompt player to press R
            g2.setFont(FONT_RELOAD);
            g2.setColor(new Color(255, 80, 80));
            g2.drawString("[ R ] RELOAD", MARGIN, RELOAD_BAR_Y + RELOAD_BAR_H + 2);
        }
    }

    private void drawReloadBar(Graphics2D g2, float progress) {
        int bx = MARGIN;
        int by = RELOAD_BAR_Y;

        // background
        g2.setColor(COL_RELOAD_BG);
        fillRoundBar(g2, bx, by, RELOAD_BAR_W, RELOAD_BAR_H);

        // fill
        int fillW = (int)(RELOAD_BAR_W * progress);
        g2.setColor(COL_RELOAD_FG);
        if (fillW > 0) fillRoundBar(g2, bx, by, fillW, RELOAD_BAR_H);

        //shimmer — animated highlight stripe
        int shimmerX = bx + fillW - 6;
        if (shimmerX > bx && shimmerX < bx + RELOAD_BAR_W) {
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillRect(shimmerX, by, 4, RELOAD_BAR_H);
        }

        // border
        g2.setColor(COL_BAR_BORDER);
        g2.setStroke(new BasicStroke(1f));
        drawRoundBarOutline(g2, bx, by, RELOAD_BAR_W, RELOAD_BAR_H);

        
        g2.setFont(FONT_RELOAD);
        String txt = "RELOADING...";
        FontMetrics fm = g2.getFontMetrics();
        int txtX = bx + (RELOAD_BAR_W - fm.stringWidth(txt)) / 2;
        int txtY = by + RELOAD_BAR_H + 12;
        g2.setColor(COL_SHADOW);
        g2.drawString(txt, txtX + 1, txtY + 1);
        g2.setColor(COL_RELOAD_TXT);
        g2.drawString(txt, txtX, txtY);
    }

    private void drawEnemyCount(Graphics2D g2, Level level) {
        int y = RELOAD_BAR_Y + RELOAD_BAR_H + 28;
        g2.setFont(FONT_ENEMY);
        String txt = "Enemies alive: " + level.getEnemies().size();
        g2.setColor(COL_SHADOW);
        g2.drawString(txt, MARGIN + 1, y + 1);
        g2.setColor(COL_ENEMY_TXT);
        g2.drawString(txt, MARGIN, y);
    }
    //top right score
    private void drawScoreHud(Graphics2D g2, Level level, int vpW) {
        g2.setFont(FONT_SCORE);

        String scoreText = "SCORE: " + level.getScore();
        int scoreW = g2.getFontMetrics().stringWidth(scoreText);
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, vpW - scoreW - MARGIN, 36);

        int    minScore = level.getMinScore();
        String gateText = "EXIT MIN: " + minScore;
        int    gateW    = g2.getFontMetrics().stringWidth(gateText);
        g2.setColor(level.getScore() >= minScore
                    ? new Color(80, 220, 80)
                    : new Color(220, 80, 80));
        g2.drawString(gateText, vpW - gateW - MARGIN, 62);
    }
    //helpers reused
    private static void fillRoundBar(Graphics2D g2, int x, int y, int w, int h) {
        if (w <= 0) return;
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, h, h));
    }

    private static void drawRoundBarOutline(Graphics2D g2, int x, int y, int w, int h) {
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, h, h));
    }
}