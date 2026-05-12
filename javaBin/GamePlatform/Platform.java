package GamePlatform;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Platform {
    protected float plat_width;
    protected float plat_height;
    protected float pos_x, pos_y;
    protected PlatformType plat_type;

    private int collisionOffsetX;
    private int collisionOffsetY;
    private int collisionWidthAdjust;
    private int collisionHeightAdjust;
    private final Rectangle cachedBounds = new Rectangle();
    private static BufferedImage metalImg;
    private static BufferedImage woodImg;
    private static BufferedImage sandImg;

    static {
        try {
            metalImg = javax.imageio.ImageIO.read(
                new java.io.File("Resources/Block-Assets/IronBlock_Assets.png")
            );
            woodImg = javax.imageio.ImageIO.read(
                new java.io.File("Resources/Block-Assets/GrassBlock_Assets.png")
            );
            sandImg = javax.imageio.ImageIO.read(
                new java.io.File("Resources/Block-Assets/SandBlock02_Assets.png")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Platform(float width, float height, float pos_x, float pos_y, PlatformType plat_type) {
        this.plat_width  = width;
        this.plat_height = height;
        this.pos_x       = pos_x;
        this.pos_y       = pos_y;
        this.plat_type   = plat_type;
        this.collisionOffsetX     = 0;
        this.collisionOffsetY     = 0;
        this.collisionWidthAdjust = 0;
        this.collisionHeightAdjust = 0;
    }

    // Kept for backwards compatibility — defaults to 32x32.
    public Platform(float pos_x, float pos_y, PlatformType plat_type) {
        this(32, 32, pos_x, pos_y, plat_type);
    }

    public Rectangle getBounds() {
        cachedBounds.setBounds(
            Math.round(pos_x)  + collisionOffsetX,
            Math.round(pos_y)  + collisionOffsetY,
            Math.max(1, Math.round(plat_width)  + collisionWidthAdjust),
            Math.max(1, Math.round(plat_height) + collisionHeightAdjust)
        );
        return cachedBounds;
    }

    public Rectangle getRenderBounds() {
        return new Rectangle(
            Math.round(pos_x), Math.round(pos_y),
            Math.round(plat_width), Math.round(plat_height)
        );
    }

    public Platform setCollisionBox(int offsetX, int offsetY, int widthAdjust, int heightAdjust) {
        this.collisionOffsetX      = offsetX;
        this.collisionOffsetY      = offsetY;
        this.collisionWidthAdjust  = widthAdjust;
        this.collisionHeightAdjust = heightAdjust;
        return this;
    }

    public PlatformType getType()  { return plat_type; }
    public float    getPosY()  { return pos_y; }
    public float        getPosX()  { return pos_x; }
    public float        getWidth() { return plat_width; }
    public float        getHeight(){ return plat_height; }
}