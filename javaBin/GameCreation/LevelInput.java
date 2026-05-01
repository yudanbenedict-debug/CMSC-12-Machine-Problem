package GameCreation;

public class LevelInput {
    private final boolean moveLeft;
    private final boolean moveRight;
    private final boolean jumpPressed;
    private final boolean sprintPressed;
    private final boolean attackPressed;
    private final int     weaponSlot;
    private final boolean reloadPressed;

    public LevelInput(boolean moveLeft, boolean moveRight, boolean jumpPressed, boolean sprintPressed, boolean attackPressed, int weaponSlot, boolean reloadPressed) {
        this.sprintPressed = sprintPressed;
        this.moveLeft = moveLeft;
        this.moveRight = moveRight;
        this.jumpPressed = jumpPressed;
        this.attackPressed = attackPressed;
        this.weaponSlot = weaponSlot;
        this.reloadPressed = reloadPressed;
    }

    public boolean isMoveLeft() {
        return moveLeft;
    }

    public boolean isRunning(){
        return sprintPressed;
    }
    public boolean isMoveRight() {
        return moveRight;
    }

    public boolean isJumpPressed() {
        return jumpPressed;
    }
    public boolean isAttackPressed(){
        return attackPressed;
    }
    public int getWeaponSlot(){
        return weaponSlot;
    }
    public boolean isReloadPressed(){
        return reloadPressed;
    }
}
