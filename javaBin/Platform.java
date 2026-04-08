import java.awt.Graphics;
import java.awt.Color;

public class Platform {
    protected int plat_width;
    protected int plat_height;
    protected int pos_x, pos_y;
    protected String plat_type; //the plat_type can have added types eventually as we go through making the game.

    public Platform(int plat_width, int plat_height, int pos_x, int pos_y, String plat_type){
        this.plat_width = plat_height;
        this.plat_height = plat_height;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.plat_type = plat_type;
    }
    //platform creation here. not necessary to create a getter/setter since this class will be final
    public final void createPlatforms(Graphics pl_graphics, int cameraX /*camera position, so we can follow */){
        

        //no logic for cameraX till we finalize the x pos of the character in a different class.
        switch(plat_type){
            case "ground_dirt" : 
            //note: cameraX is not the final pos for the platform.
                pl_graphics.setColor(new Color(139, 69, 19));
                pl_graphics.fillRect(cameraX, pos_y, plat_width, plat_width);
                break;
            case "brick" :
                pl_graphics.setColor(new Color(160, 82, 45));
                pl_graphics.fillRect(cameraX, pos_y, plat_width, plat_height);
                pl_graphics.setColor(new Color(120, 60, 30));
                // Brick pattern
                for (int i = 0; i < plat_width; i += 20) {
                    pl_graphics.drawLine(cameraX + i,  pos_y, cameraX + i,  pos_y + plat_height);
                }
                pl_graphics.drawLine(cameraX,  pos_y + plat_height/2, cameraX + plat_width,  pos_y + plat_height/2);
                break;

        }
    }


}
