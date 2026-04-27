import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Entities.Entity;
import Entities.Player;

public class Item extends Entity {

    public enum EffectType {
    SPEED_BOOST,
    DOUBLE_JUMP,
    INVINCIBILITY,
    DEBUFF_SLOWNESS,
    DEBUFF_REVERSE_CONTROLS //  "DEBUFF_REDUCDED_JUMP" to be added
}

    private EffectType buffType;
    private int duration = 5;      // in seconds (0 = permanent)
    private boolean isActive;  // not needed for item itself, but for effect the on player

    public Item(int x, int y, BufferedImage sprite, int width, int height, EffectType buffType, int duration) {
        super(x, y, width, height);
        this.buffType = buffType;
        this.sprite = sprite;
        this.duration = duration;
        this.isActive = true;
    }

    @Override
    public void update() {
        // Maybe adding idle animation such as float effect here...
    }

    @Override
    public void draw(Graphics g) {
        if (isActive && sprite != null) {
            g.drawImage(sprite, (int) x, (int) y, (int) width, (int) height, null);
        }
    }

     public void applyEffect(Player player) {
        if (!isActive) return;  
        
        
        if (buffType.name().startsWith("DEBUFF")) {
            player.applyDebuff(buffType.name(), duration);
        } else {
            player.applyBuff(buffType.name(), duration);
        }
        
        isActive = false;  
    }

    public EffectType getBuffType() {
        return buffType;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}
