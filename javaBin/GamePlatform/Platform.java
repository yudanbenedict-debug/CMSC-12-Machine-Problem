package GamePlatform;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;
import java.awt.Color;

public class Platform {
    protected float plat_width;
    protected float plat_height;
    protected float pos_x, pos_y;
    protected PlatformType plat_type; //the plat_type can have added types eventually as we go through making the game.
    protected int valueX;
    protected Random rand;
    protected int valueY;
    protected int[] choices = {230, 290, 350};
    private int collisionOffsetX;
    private int collisionOffsetY;
    private int collisionWidthAdjust;
    private int collisionHeightAdjust;


    public Platform(float plat_width, float plat_height, float pos_x, float pos_y, PlatformType plat_type){
        this.plat_width = plat_width;
        this.plat_height = plat_height;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.plat_type = plat_type;
        this.rand = new Random();
        valueX = rand.nextInt(17) * 30;
        valueY = choices[rand.nextInt(choices.length)];
        this.collisionOffsetX = 0;
        this.collisionOffsetY = 0;
        this.collisionWidthAdjust = 0;
        this.collisionHeightAdjust = 0;

        
        
    }
    //platform creation here. not necessary to create a getter/setter since this class will be final
    /**
     * @param pl_graphics
     * @param cameraX
     */
    public final void createPlatforms(Graphics pl_graphics, int cameraX /*camera position, so we can follow */){
        switch(plat_type){
            case METAL : 
                pl_graphics.setColor(new Color(90, 90, 90));
                pl_graphics.fillRect((int) pos_x - cameraX, (int) pos_y, (int) plat_width, (int) plat_height);
                break;
            case WOOD :
                pl_graphics.setColor(new Color(130, 84, 45));
                pl_graphics.fillRect((int) pos_x - cameraX, (int) pos_y, (int) plat_width, (int) plat_height);
                break;
            case SAND :
                pl_graphics.setColor(new Color(219, 183, 115));
                pl_graphics.fillRect((int) pos_x - cameraX, (int) pos_y, (int) plat_width, (int) plat_height);
                break;
            default:
                pl_graphics.setColor(Color.GRAY);
                pl_graphics.fillRect((int) pos_x - cameraX, (int) pos_y, (int) plat_width, (int) plat_height);
                break;
        }
    }

    public Rectangle getBounds() {
        int collisionX = Math.round(pos_x) + collisionOffsetX;
        int collisionY = Math.round(pos_y) + collisionOffsetY;
        int collisionWidth = Math.max(1, Math.round(plat_width) + collisionWidthAdjust);
        int collisionHeight = Math.max(1, Math.round(plat_height) + collisionHeightAdjust);
        return new Rectangle(collisionX, collisionY, collisionWidth, collisionHeight);
    }

    public Rectangle getRenderBounds() {
        return new Rectangle(Math.round(pos_x), Math.round(pos_y), Math.round(plat_width), Math.round(plat_height));
    }

    public Platform setCollisionBox(int offsetX, int offsetY, int widthAdjust, int heightAdjust) {
        this.collisionOffsetX = offsetX;
        this.collisionOffsetY = offsetY;
        this.collisionWidthAdjust = widthAdjust;
        this.collisionHeightAdjust = heightAdjust;
        return this;
    }

    public float getPosY() {
        return pos_y;
    }

}
