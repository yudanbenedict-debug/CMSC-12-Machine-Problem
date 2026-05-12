
package Entities;

/*/----- CHANGE: Item.java fully rewritten -----/
 * PURPOSE: The old Item class extended Entity, required a sprite and
 * BufferedImage, had a complex EffectType enum (SPEED_BOOST, DOUBLE_JUMP,
 * INVINCIBILITY, etc.), and an applyEffect() method that was entirely
 * commented out — meaning none of it was functional. It was dead code.
 *
 * The new Item class is intentionally minimal:
 *   - No Entity inheritance (items don't move or animate yet)
 *   - No sprite dependency (drawn as a simple shape by the renderer)
 *   - Type enum kept to just what's real and working: COIN, with HEALTH
 *     stubbed in as a clearly-marked future placeholder
 *   - Holds a Rectangle bounds so collision detection in Level.update()
 *     works exactly as before — just typed instead of raw Rectangle
 *   - isCollected flag lets Level mark it collected and skip it in future
 *     collision checks without needing to remove it from the list mid-loop
 *
 * USAGE: LevelData holds List<Item> instead of List<Rectangle>. Level.update()
 * iterates items, checks isCollected, tests bounds intersection, marks
 * collected, and applies the score effect based on type.
 */
import java.awt.Rectangle;

public class Item {

    public enum Type {
        COIN,    // +1 score on pickup
        HEALTH   // placeholder for future health pack logic
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
/*/----- END CHANGE -----/*/