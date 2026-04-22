// package GamePlatform;

// import java.awt.*;
// import java.util.ArrayList;
// import javax.swing.*;

// public class PlatformTest extends JPanel {

//     ArrayList<Platform> platforms = new ArrayList<>();
//     int cameraX = 600;
//     Image background;

//     public PlatformTest() {
//         // Create test platforms
//         platforms.add(new Platform(800, 60, 5, 500, "ground_dirt"));
//         platforms.add(new Platform(120, 40, 400, 300, "brick"));
//     }

//     @Override
//     protected void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         background = new ImageIcon("C:\\Users\\Administrator\\Desktop\\HAHAH\\CMSC-12-Machine-Problem\\Resources\\Ocean.png").getImage();
//         // Background
//         g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        

//         // Draw all platforms
//         for (Platform p : platforms) {
//             p.createPlatforms(g, cameraX);
//         }
//     }

//     public static void main(String[] args) {
//         JFrame frame = new JFrame("Platform Test");
//         PlatformTest panel = new PlatformTest();

//         frame.add(panel);
//         frame.setSize(800, 600);
//         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         frame.setVisible(true);
//     }
// }