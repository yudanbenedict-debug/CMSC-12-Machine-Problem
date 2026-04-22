package GamePlatform;

public enum PlatformType {
    METAL(true),
    WOOD(true),

    SAND(true);
    private boolean Solid;

   PlatformType(boolean Solid){
    this.Solid = Solid;
    }
    
    public boolean isSolid(){
        return Solid;
    }
}
