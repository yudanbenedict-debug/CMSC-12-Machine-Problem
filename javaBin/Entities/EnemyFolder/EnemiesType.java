package Entities.EnemyFolder;

public enum EnemiesType {

    //slime is for placeholder for now
    // int spriteWidth,  int spriteHeight,
    // int walkFrameCount, int chaseFrameCount, 
    // int alertFrameCount, int attackFrameCount)
    // deathFrameCount
    
    
    GUN_ENEMY (20f, 4f, 1.2f, 80, 140, 32, 48, 8, 8, 1, 5, 10),
    SWORD_ENEMY (30f, 4f, 1.2f, 80, 140, 32, 48, 8, 8, 1, 5, 10),
    PUNCH_ENEMY(30f, 4f, 1.2f, 80, 140, 32, 48, 8, 8,1,5, 10),
    KICK_ENEMY(30f, 4f, 1.2f, 70, 140, 32, 48, 8, 8, 1, 5, 10);

    public final float baseHealth;
    public final float baseDamage;
    public final float baseSpeed;

    public final int   patrolRange;
    public final int   aggroRange;

    public final int spriteWidth;
    public final int spriteHeight;
    public final int walkFrameCount;
    public final int attackFrameCount;
    public final int chaseFrameCount;
    public final int alertFrameCount;
    public final int deathFrameCount;

    EnemiesType(float baseHealth, float baseDamage, float baseSpeed,
        int patrolRange,  int aggroRange,
        int spriteWidth,  int spriteHeight,
        int walkFrameCount, int chaseFrameCount, 
        int alertFrameCount, int attackFrameCount, int deathFrameCount) {
        this.baseHealth       = baseHealth;
        this.baseDamage       = baseDamage;
        this.baseSpeed        = baseSpeed;
        this.patrolRange      = patrolRange;
        this.aggroRange       = aggroRange;
        this.spriteWidth      = spriteWidth;
        this.spriteHeight     = spriteHeight;
        this.walkFrameCount   = walkFrameCount;
        this.chaseFrameCount = chaseFrameCount;
        this.alertFrameCount = alertFrameCount;
        this.deathFrameCount = deathFrameCount;
        this.attackFrameCount = attackFrameCount;
     }
}