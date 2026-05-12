package Entities;
import java.awt.Graphics;

public abstract class LivingEntity extends Entity {
    protected float health;
    protected float damage;
    protected float walk_speed;
    protected float jump_height;

    public LivingEntity(float x, float y, int width, int height, float health, float damage, float walk_speed, float jump_height){
        super(x, y, width, height);
        this.health = health;
        this.damage = damage;
        this.walk_speed = walk_speed;
        this.jump_height = jump_height;
    }

    public void die(){
        onDeath();
    }

    public void takeDamage(float damage) {
        this.health = this.health - damage;

        if(health <= 0){
            die();
        }
    }
    @Override
    public abstract void update();
    @Override 
    public abstract void draw(Graphics g);

    public abstract void onDeath();
 
    
}
