package Entities.EnemyFolder;

import GamePlatform.Platform;
import Handlers.EnemyManager;
import Weapons.Rocket;
import java.util.List;

public class Boss extends Enemies {

    private static final int ROCKET_COOLDOWN_MAX = 90;
    private static final int EXPLOSION_RADIUS = 50;
    private static final float ROCKET_DAMAGE = 25f;
    private static final float DESIRED_DISTANCE = 180f;

    private int rocketCooldown = 0;
    private EnemyManager enemyManager;

    public Boss(EnemiesType type, float x, float y) {
        super(type, x, y);
        this.walk_speed = 0.8f;
    }

    public void setEnemyManager(EnemyManager manager) {
        this.enemyManager = manager;
    }

    @Override
    public void update(List<Platform> platforms, float playerX, float playerY) {
        if (!isAlive()) {
            super.update(platforms, playerX, playerY);
            return;
        }

        if (rocketCooldown > 0) rocketCooldown--;

        facingRight = playerX > getX();

        float dx = playerX - (getX() + getWidth() / 2);
        float dist = Math.abs(dx);

        //maintain optimal distance from player — not too close, not too far
        if (dist < DESIRED_DISTANCE - 40) {
            velX = facingRight ? -walk_speed : walk_speed;
        } else if (dist > DESIRED_DISTANCE + 40) {
            velX = facingRight ? walk_speed : -walk_speed;
        } else {
            velX = 0;
        }

        if (rocketCooldown <= 0 && dist <= 350) {
            fireRocket(playerX, playerY);
            rocketCooldown = ROCKET_COOLDOWN_MAX;
        }

        applyPhysics();
        resolvePlatforms(platforms);
    }

    private void fireRocket(float targetX, float targetY) {
        if (enemyManager == null) return;

        float dx = targetX - (getX() + getWidth() / 2);
        float dy = targetY - (getY() + getHeight() / 2);
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.01f) return;
        dx /= length;
        dy /= length;

        float startX = facingRight ? getX() + getWidth() : getX();
        float startY = getY() + getHeight() / 2;

        Rocket rocket = new Rocket(startX, startY, dx * 6f, dy * 6f, ROCKET_DAMAGE, EXPLOSION_RADIUS);
        enemyManager.addRocket(rocket);
    }
}
