import java.awt.Graphics;
import java.awt.Image;
import java.util.Random;
import javax.swing.*;

public class Platform {
    protected int plat_width;
    protected int plat_height;
    protected int pos_x, pos_y;
    protected String plat_type; //the plat_type can have added types eventually as we go through making the game.
    protected Image brick;
    protected Image ground;
    protected int valueX;
    protected Random rand;
    protected int valueY;
    protected int[] choices = {230, 290, 350};


    public Platform(int plat_width, int plat_height, int pos_x, int pos_y, String plat_type){
        this.plat_width = plat_height;
        this.plat_height = plat_height;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.plat_type = plat_type;
        this.rand = new Random();
        valueX = rand.nextInt(17) * 30;
        valueY = choices[rand.nextInt(choices.length)];

        
        
    }
    //platform creation here. not necessary to create a getter/setter since this class will be final
    /**
     * @param pl_graphics
     * @param cameraX
     */
    public final void createPlatforms(Graphics pl_graphics, int cameraX /*camera position, so we can follow */){
        
        
        //no logic for cameraX till we finalize the x pos of the character in a different class.
        switch(plat_type){
            
            case "ground_dirt" : 
            //note: cameraX is not the final pos for the platform.
                ground = new ImageIcon("C:\\Users\\Administrator\\Desktop\\HAHAH\\CMSC-12-Machine-Problem\\Resources\\ground.jpg").getImage();
                pl_graphics.drawImage(ground, cameraX, pos_y, plat_width, plat_height, null);  
                    
                break;
            case "brick" :
                brick = new ImageIcon("C:\\Users\\Administrator\\Desktop\\HAHAH\\CMSC-12-Machine-Problem\\Resources\\brick.png").getImage();
                pl_graphics.drawImage(brick, cameraX, valueY, plat_width, plat_height, null);

                break;

        

        }

        
    }

 


}
