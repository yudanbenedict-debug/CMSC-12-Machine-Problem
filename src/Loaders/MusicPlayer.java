package Loaders;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;

public class MusicPlayer {

    private static Clip currentClip;

    public static void play(String filePath) {
        stop();

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

    public static void stop() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }

    public static void pause() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }
    }

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