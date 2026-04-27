package Entities;

public enum EnemiesType {

    //slime is for placeholder for now
    SLIME   (20f,  4f,   1.2f,  80,  140),
    GOBLIN  (35f,  8f,   2.0f, 120,  200),
    ORC     (80f,  15f,  1.0f,  60,  180),
    SKELETON(50f,  10f,  1.6f, 100,  220);

    public final float baseHealth;
    public final float baseDamage;
    public final float baseSpeed;

    public final int   patrolRange;
    public final int   aggroRange;

    EnemiesType(float baseHealth, float baseDamage, float baseSpeed,
                int patrolRange, int aggroRange) {
        this.baseHealth  = baseHealth;
        this.baseDamage  = baseDamage;
        this.baseSpeed   = baseSpeed;
        this.patrolRange = patrolRange;
        this.aggroRange  = aggroRange;
    }
}