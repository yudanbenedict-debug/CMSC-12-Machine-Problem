package GameCreation;

import javax.imageio.ImageIO;

import java.io.File;
import java.util.ArrayList;
import Entities.Player;
import Entities.Enemies;
import GamePlatform.Platform;
import java.util.Scanner;
/*
    Make scanner for platforms
    Make scanner for enemies
    Make scanner for items

*/

public class Level {
    private ArrayList<Platform> platforms;
    private ArrayList<Enemies> enemies;
    private ArrayList<Item> items;

    private Player player;
    private static int levelNumber = 0;

    static{
        levelNumber++;
    }
    public Level(){

    }

    public void initialize(){
        //switch(levelNumber)
    }
    public void update(){

    }
    public int saveGame(){
        try{
            File saveFile = new File("javaBin/LevelFile/level.txt");
            Scanner lvl_scan = new Scanner(saveFile);

            String line = lvl_scan.nextLine();
            lvl_scan.close();

            return Integer.parseInt(line.split("=")[1]);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
