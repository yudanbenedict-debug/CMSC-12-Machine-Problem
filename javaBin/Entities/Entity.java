package Entities;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;

public abstract class Entity {

    protected float x, y;
    protected float width, height;

    protected BufferedImage sprite;  

    public Entity(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        // sprite can stay null until set in subclasses
    }

    public abstract void update();
    public abstract void draw(Graphics g);

    //collision method
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, (int)width, (int)height);
    }

    //setters/getters just incase we change the variable to private
    // public float getX() { return x; }
    // public void setX(float x) { this.x = x; }

    // public float getY() { return y; }
    // public void setY(float y) { this.y = y; }

    // public float getWidth() { return width; }
    // public float getHeight() { return height; }

    // public BufferedImage getSprite() { return sprite; }
    // public void setSprite(BufferedImage sprite) { 
    //     this.sprite = sprite; 
    // }
}