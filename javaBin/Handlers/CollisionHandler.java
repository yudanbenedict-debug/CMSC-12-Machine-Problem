package Handlers;

import Entities.Player;
import GamePlatform.Platform;

import java.awt.Rectangle;
import java.util.List;

public class CollisionHandler {

    private static final float FLOOR_SNAP_TOLERANCE = 4.0f;

    // ─────────────────────────────────────────────────────────────────────────
    //  Player-platform collision
    // ─────────────────────────────────────────────────────────────────────────

    public void handlePlayerPlatformCollisions(Player player, List<Platform> platforms,
                                               float previousX, float previousY) {
        player.setGrounded(false);

        float previousBottom = previousY + player.getHeight();
        float previousTop    = previousY;
        float previousRight  = previousX + player.getWidth();
        float previousLeft   = previousX;

        for (Platform platform : platforms) {
            Rectangle pb = platform.getBounds();

            float platTop    = pb.y;
            float platBottom = pb.y + pb.height;
            float platLeft   = pb.x;
            float platRight  = pb.x + pb.width;

            float curBottom = player.getY() + player.getHeight();
            float curTop    = player.getY();
            float curLeft   = player.getX();
            float curRight  = player.getX() + player.getWidth();

            boolean overlapsH = curLeft < platRight && curRight > platLeft;
            boolean overlapsV = curTop  < platBottom && curBottom > platTop;

            if (!overlapsH || !overlapsV) continue;

            boolean fromTop    = previousBottom <= platTop    + FLOOR_SNAP_TOLERANCE;
            boolean fromBottom = previousTop    >= platBottom - FLOOR_SNAP_TOLERANCE;
            boolean fromLeft   = previousRight  <= platLeft   + FLOOR_SNAP_TOLERANCE;
            boolean fromRight  = previousLeft   >= platRight  - FLOOR_SNAP_TOLERANCE;

            if (fromTop && player.getVerticalVelocity() >= 0) {
                player.setY(platTop - player.getHeight());
                player.setVerticalVelocity(0);
                player.setGrounded(true);
            } else if (fromBottom && player.getVerticalVelocity() < 0) {
                player.setY(platBottom);
                player.setVerticalVelocity(0);
            } else if (fromLeft) {
                player.setX(platLeft - player.getWidth());
            } else if (fromRight) {
                player.setX(platRight);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Item collisions
    // ─────────────────────────────────────────────────────────────────────────

    public void handleItemCollisions(Player player, List<Rectangle> items) {
        Rectangle playerBounds = player.getBounds();
        for (Rectangle itemBounds : items) {
            if (playerBounds.intersects(itemBounds)) {
                // TODO: item collection logic
            }
        }
    }
}