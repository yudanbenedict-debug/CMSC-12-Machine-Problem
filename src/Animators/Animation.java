package Animators;
import java.awt.image.BufferedImage;

public class Animation {
    private BufferedImage[] frames;
    private int frame_time;
    private int frame_counter;
    private boolean running;
    private int currentFrame;
    private boolean hasPlayedThrough;

    public Animation(BufferedImage[] frames, int frame_time, boolean running){
        this.frames = frames;
        this.frame_time = frame_time;
        this.running = running;
        frame_counter = 0;
        currentFrame = 0;
        hasPlayedThrough = false;
    }

    public void animate(){
        if(frames.length <= 1) return;
        frame_counter++;
        if (frame_counter >= frame_time){
            frame_counter = 0;
            currentFrame = (currentFrame + 1) % frames.length;

            if (currentFrame == 0) hasPlayedThrough = true;
        }
        if(!running && currentFrame == 0){
            currentFrame = frames.length - 1;
        }
    }

    public void resetanimation(){
        currentFrame = 0;
        frame_counter = 0;

        hasPlayedThrough = false;

    }


    public boolean isFinished(){
        return hasPlayedThrough;
    }


    public BufferedImage getCurrentFrame(){
        return frames[currentFrame];
    }
}