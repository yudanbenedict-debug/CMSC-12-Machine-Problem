package Loaders;

import javax.sound.sampled.*;
import java.io.File;

/**
 * Simple singleton-style music player.
 * Only one track plays at a time — calling play() stops any current track first.
 */
public class MusicPlayer {

    private static Clip currentClip;

    /** Plays a WAV file on loop. Stops any currently playing music first. */
    public static void play(String filePath) {
        stop(); // stop previous track if any

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("[MusicPlayer] File not found: " + file.getAbsolutePath());
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);
            currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            currentClip.start();
            System.out.println("[MusicPlayer] Playing: " + filePath);

        } catch (Exception e) {
            System.err.println("[MusicPlayer] Failed to play " + filePath + ": " + e.getMessage());
        }
    }

    /** Stops and releases the current track. */
    public static void stop() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }

    /** Pauses the current track without releasing it. */
    public static void pause() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }
    }

    /** Resumes a paused track. */
    public static void resume() {
        if (currentClip != null && !currentClip.isRunning()) {
            currentClip.start();
        }
    }

    public static boolean isPlaying() {
        return currentClip != null && currentClip.isRunning();
    }

    private MusicPlayer() {}
}