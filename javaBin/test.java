import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class test extends JFrame {
    
    public test() {
        GamePanel panel = new GamePanel();
        add(panel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        
        panel.startGame();
    }
    
    class GamePanel extends JPanel implements Runnable {
        // Player position
        int x = 100;
        int y = 500;
        int width = 40;
        int height = 40;
        
        // Velocity
        int velX = 0;
        int velY = 0;
        
        // Physics
        int gravity = 1;
        boolean isGrounded = false;
        
        // Input
        boolean leftPressed = false;
        boolean rightPressed = false;
        
        Thread gameThread;
        boolean running = true;
        
        public GamePanel() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.CYAN);
            setFocusable(true);
            setupInput();
        }
        
        private void setupInput() {
            InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = getActionMap();
            
            im.put(KeyStroke.getKeyStroke("pressed LEFT"), "left");
            im.put(KeyStroke.getKeyStroke("released LEFT"), "leftRelease");
            im.put(KeyStroke.getKeyStroke("pressed RIGHT"), "right");
            im.put(KeyStroke.getKeyStroke("released RIGHT"), "rightRelease");
            im.put(KeyStroke.getKeyStroke("pressed SPACE"), "jump");
            
            am.put("left", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    leftPressed = true;
                }
            });
            
            am.put("leftRelease", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    leftPressed = false;
                    if (!rightPressed) velX = 0;
                }
            });
            
            am.put("right", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    rightPressed = true;
                }
            });
            
            am.put("rightRelease", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    rightPressed = false;
                    if (!leftPressed) velX = 0;
                }
            });
            
            am.put("jump", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (isGrounded) {
                        velY = -15;
                        isGrounded = false;
                    }
                }
            });
        }
        
        public void startGame() {
            gameThread = new Thread(this);
            gameThread.start();
        }
        
        public void update() {
            // Movement
            if (leftPressed) velX = -5;
            if (rightPressed) velX = 5;
            
            // Gravity
            velY += gravity;
            y += velY;
            x += velX;
            
            // Ground collision
            if (y + height >= 550) {
                y = 550 - height;
                velY = 0;
                isGrounded = true;
            } else {
                isGrounded = false;
            }
            
            // Screen boundaries
            if (x < 0) x = 0;
            if (x + width > 800) x = 800 - width;
        }
        
        @Override
        public void run() {
            while (running) {
                update();
                repaint();
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Draw ground
            g.setColor(new Color(34, 139, 34));
            g.fillRect(0, 550, 800, 50);
            
            // Draw player (red rectangle)
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
            
            // Simple face so you know direction
        }
    }
    
    public static void main(String[] args) {
        new test();
    }
}