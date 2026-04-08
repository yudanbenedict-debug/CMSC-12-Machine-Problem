import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class Entity{
    //positions
    protected int x,y;
    protected int width, height;
    protected int velX, velY;
    protected static final int grav = 1;
    protected boolean isGrounded;
    protected BufferedImage charImage;
    protected BufferedImage sprite;

    public Entity(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        velX = 0;
        velY = 0;
        isGrounded = false;
    }
    //abstract class for player/enemies
    public abstract void update();
    public abstract void draw(Graphics g);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void applyGravity(){
        velY += grav;
        y += velY;
    }
    public void checkIfGrounded(int groundY){
        if(this.y >= groundY){
            this.y = groundY;
            isGrounded = true;
        }
        else{
            isGrounded = false;
        }

    }   

    //getters and setters
    // Position getters and setters
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    // Size getters and setters
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // Velocity getters and setters
    public int getVelX() {
        return velX;
    }

    public void setVelX(int velX) {
        this.velX = velX;
    }

    public int getVelY() {
        return velY;
    }

    public void setVelY(int velY) {
        this.velY = velY;
    }

    public static int getGrav() {
        return grav;
    }

    // Grounded getter and setter
    public boolean isGrounded() {
        return isGrounded;
    }

    public void setGrounded(boolean isGrounded) {
        this.isGrounded = isGrounded;
    }

    // Image getters and setters
    public BufferedImage getCharImage() {
        return charImage;
    }

    public void setCharImage(BufferedImage charImage) {
        this.charImage = charImage;
    }

    public BufferedImage getSprite() {
        return sprite;
    }

    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }

}