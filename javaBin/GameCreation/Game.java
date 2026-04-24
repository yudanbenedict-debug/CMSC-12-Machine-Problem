// package GameCreation;

// public class Game implements Runnable{
//     //game loop speed
//     private static final int MIL_PER_SEC = 1000;
//     private static final int NAN_PER_SEC = 1000000000;
//     private static final double TICKS = 60.0;

//     private boolean running;

//     private Thread thread;

//     public Game(){
//         initialize();
//     }

//     //initialize synchronization of threads

//     public synchronized void start(){
//         thread = new Thread(this);
//         thread.start();
//         running = true;
//     }

//     public synchronized void stop(){
//         try{
//             thread.join();
//             running = false;
//         }catch(InterruptedException e){
//             e.printStackTrace();
//         }
//     }
//     @Override 
//     public void run(){
//         //initialize the timings
//         long lastTime = System.nanoTime();
//         double am_ticks = TICKS;
//         double nan_sec = NAN_PER_SEC;
//         double delta = 0;
//         long timer = System.currentTimeMillis();
//         int frames = 0;
//         int updates = 0;

//         while (running){
//             long currentTime = System.nanoTime();
//             delta += (currentTime - lastTime) / nan_sec;
//             lastTime = currentTime;

//             while (delta >= 1){
//                 updates++;
//                 delta--;
//             }


//         }

//         stop();
//     }
//     public void initialize(){
//         start();
//     }

//     public static void main(String[] args) {
//         new Game();
//     }
// }
