package main;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Utility class for playing sound effects in the shell.
 * Uses a pool of clips to handle rapid key presses.
 */
public class SoundPlayer
{
    private static final int POOL_SIZE = 30;
    private static Clip[] clipPool = new Clip[POOL_SIZE];
    private static int currentClip = 0;
    private static boolean soundEnabled = true;
    private static final String SOUND_FILE = "sounds/keypress.wav";
    private static File loadedSoundFile;

    /**
     * Initialize the sound player by preloading multiple clips.
     */
    public static void init()
    {
        try
        {
            loadedSoundFile = findSoundFile();
            
            if (loadedSoundFile != null && loadedSoundFile.exists())
            {
                // Pre-load multiple clips for rapid playback
                for (int i = 0; i < POOL_SIZE; i++)
                {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(loadedSoundFile);
                    clipPool[i] = AudioSystem.getClip();
                    clipPool[i].open(audioIn);
                }
                System.out.println("Sound effects loaded: " + loadedSoundFile.getAbsolutePath());
            }
            else
            {
                System.out.println("Sound file not found. Looked in:");
                System.out.println("  - sounds/keypress.wav (relative to working dir)");
                System.out.println("  - sounds/keypress.wav (relative to class location)");
                soundEnabled = false;
            }
        }
        catch (UnsupportedAudioFileException e)
        {
            System.out.println("Unsupported audio format. Please use a standard WAV file (PCM).");
            soundEnabled = false;
        }
        catch (Exception e)
        {
            System.out.println("Could not load sound: " + e.getMessage());
            soundEnabled = false;
        }
    }

    /**
     * Find the sound file by checking multiple possible locations.
     */
    private static File findSoundFile()
    {
        // 1. Try relative to current working directory
        File f1 = new File(SOUND_FILE);
        if (f1.exists()) return f1;

        // 2. Try relative to where the class file is located
        try
        {
            String classPath = SoundPlayer.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI().getPath();
            File classDir = new File(classPath).getParentFile();
            File f2 = new File(classDir, SOUND_FILE);
            if (f2.exists()) return f2;
            
            // 3. If classPath is a classes/bin folder, go up one level
            File f3 = new File(classDir.getParentFile(), SOUND_FILE);
            if (f3.exists()) return f3;
        }
        catch (Exception e)
        {
            // Ignore URI parsing errors
        }

        // 4. Try user.dir as fallback
        File f4 = new File(System.getProperty("user.dir"), SOUND_FILE);
        if (f4.exists()) return f4;

        return null;
    }

    /**
     * Play the key press sound effect.
     * Uses rotating clip pool To handle rapid key presses.
     */
    public static void playKeyPress()
    {
        if (!soundEnabled || clipPool[0] == null) return;
        
        // Use the next clip in the pool (round-robin)
        Clip clip = clipPool[currentClip];
        currentClip = (currentClip + 1) % POOL_SIZE;
        
        // Stop if running, rewind, and play
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    /**
     * Enable or disable sound effects.
     */
    public static void setSoundEnabled(boolean enabled)
    {
        soundEnabled = enabled;
    }

    /**
     * Check if sound effects are enabled.
     */
    public static boolean isSoundEnabled()
    {
        return soundEnabled;
    }

    /**
     * Cleanup resources when shutting down.
     */
    public static void close()
    {
        for (Clip clip : clipPool)
        {
            if (clip != null) clip.close();
        }
    }
}

