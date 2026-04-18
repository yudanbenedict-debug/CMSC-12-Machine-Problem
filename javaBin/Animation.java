import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Animation {
    private BufferedImage[] frames;
    private int frame_time;
    private int frame_counter;
    private boolean running;
    private int currentFrame;

    public Animation(BufferedImage[] frames, int frame_time, boolean running){
        this.frames = frames;
        this.frame_time = frame_time;
        this.running = running;
        frame_counter = 0;
        currentFrame = 0;
    }
    public void animate(){
        if(frames.length <= 1) return;
        //start frame counter here for how long the animation should loop.
        frame_counter++;
        if (frame_counter >= frame_time){
            frame_counter = 0;
            currentFrame = (currentFrame + 1) % frames.length;
        }
        if(!running && currentFrame == 0){
            currentFrame = frames.length - 1;
        }
    }
    //incase it is needed to be reset
    public void resetanimation(){
        currentFrame = 0;
        frame_counter = 0;
    }
    public boolean isFinished(){
        return !running && frame_counter >= frames.length - 1;
    }
    public BufferedImage getCurrentFrame(){
        return frames[currentFrame];
    }
}
