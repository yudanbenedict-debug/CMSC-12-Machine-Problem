package GamePlatform;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Platform {
    protected float plat_width;
    protected float plat_height;
    protected float pos_x, pos_y;
    protected PlatformType plat_type; //the plat_type can have added types eventually as we go through making the game.
    private int collisionOffsetX;
    private int collisionOffsetY;
    private int collisionWidthAdjust;
    private int collisionHeightAdjust;
    private static BufferedImage metalImg;
    private static BufferedImage woodImg;
    private static BufferedImage sandImg;

    static {
        try {
            metalImg = javax.imageio.ImageIO.read(
                Platform.class.getResource("/Resources/Block-Assets/IronBlock_Assets.png")
            );
            woodImg = javax.imageio.ImageIO.read(
                Platform.class.getResource("/Resources/Block-Assets/GrassBlock_Assets.png")
            );
            sandImg = javax.imageio.ImageIO.read(
                Platform.class.getResource("/Resources/Block-Assets/SandBlock02_Assets.png")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// Create an obj of this class  and the call the createPlatforms(); inside the GamePanel paintComponent(); and pass the graphics to createPlatforms();
    public Platform( float pos_x, float pos_y, PlatformType plat_type){
        this.plat_width = 32;
        this.plat_height = 32;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.plat_type = plat_type;
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
    public void createPlatforms(Graphics g, int quantity, int cameraX) {
        BufferedImage img = null;
        
        
        for(int i = 0; i < quantity; i++) {

            int drawX = (int) this.pos_x + (i * 32) - cameraX;
            int drawY = (int) this.pos_y;

            switch (plat_type) {
                case METAL -> img = metalImg;
                case WOOD  -> img = woodImg;
                case SAND  -> img = sandImg;
                default    -> img = null;
            }

            if (img != null) {
                g.drawImage(img, drawX, drawY, 32, 32, null);
            }
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
