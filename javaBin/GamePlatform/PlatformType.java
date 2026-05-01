package GamePlatform;

public enum PlatformType {
    GRASS(true),
    DIRT(true),
    METAL(true),
    SAND(true),
    WOOD(true);   

    private final boolean solid;

    PlatformType(boolean solid) {
        this.solid = solid;
    }

    public boolean isSolid() {
        return solid;
    }
}