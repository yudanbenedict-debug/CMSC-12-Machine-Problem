package Entities;

import java.awt.Rectangle;

public class Item {

    public enum Type {
        COIN,    // +1 score on pickup
        HEALTH   // placeholder for future health pack logic (will not be added)
    }

    public final Type      type;
    public final Rectangle bounds;
    private      boolean   collected = false;

    public Item(Type type, int x, int y, int width, int height) {
        this.type   = type;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public boolean isCollected()          { return collected;       }
    public void    markCollected()        { collected = true;        }
    public int     getX()                 { return bounds.x;         }
    public int     getY()                 { return bounds.y;         }
    public int     getWidth()             { return bounds.width;     }
    public int     getHeight()            { return bounds.height;    }
}