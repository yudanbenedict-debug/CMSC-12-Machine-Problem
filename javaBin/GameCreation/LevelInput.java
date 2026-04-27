package GameCreation;

public class LevelInput {
    private final boolean moveLeft;
    private final boolean moveRight;
    private final boolean jumpPressed;

    public LevelInput(boolean moveLeft, boolean moveRight, boolean jumpPressed) {
        this.moveLeft = moveLeft;
        this.moveRight = moveRight;
        this.jumpPressed = jumpPressed;
    }

    public boolean isMoveLeft() {
        return moveLeft;
    }

    public boolean isMoveRight() {
        return moveRight;
    }

    public boolean isJumpPressed() {
        return jumpPressed;
    }
}
