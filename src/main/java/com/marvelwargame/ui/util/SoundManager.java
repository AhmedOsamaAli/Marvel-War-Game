package com.marvelwargame.ui.util;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Singleton sound manager. Plays OGG sounds via javax.sound.sampled + vorbisspi.
 * Works on Windows, Linux, and Mac. Each play call runs on a daemon thread.
 */
public final class SoundManager {

    private static final SoundManager INSTANCE = new SoundManager();

    private boolean muted  = false;
    private double  volume = 0.72;

    /** Maps event key → OGG resource paths (relative to classpath root). */
    private final Map<String, List<String>> soundPaths = new HashMap<>();
    private final Random rng = new Random();

    private SoundManager() {
        // UI interaction sounds
        register("click",    "audio/ui/click_003.ogg",   "audio/ui/click_004.ogg");
        register("select",   "audio/ui/select_001.ogg",  "audio/ui/select_002.ogg",
                             "audio/ui/select_003.ogg");
        register("confirm",  "audio/ui/confirmation_001.ogg", "audio/ui/confirmation_002.ogg");
        register("back",     "audio/ui/back_001.ogg",    "audio/ui/back_002.ogg");
        register("error",    "audio/ui/error_001.ogg",   "audio/ui/error_002.ogg");
        register("endturn",  "audio/ui/tick_001.ogg",    "audio/ui/tick_002.ogg");

        // Combat sounds
        register("move",     "audio/impact/footstep_concrete_000.ogg",
                             "audio/impact/footstep_concrete_001.ogg",
                             "audio/impact/footstep_concrete_002.ogg");
        register("attack",   "audio/impact/impactPunch_heavy_000.ogg",
                             "audio/impact/impactPunch_heavy_001.ogg",
                             "audio/impact/impactPunch_heavy_002.ogg");
        register("damage",   "audio/impact/impactMetal_heavy_000.ogg",
                             "audio/impact/impactMetal_heavy_001.ogg",
                             "audio/impact/impactPlate_heavy_000.ogg");
        register("ability",  "audio/ui/maximize_001.ogg", "audio/ui/maximize_003.ogg",
                             "audio/ui/maximize_005.ogg");
        register("knockout", "audio/ui/glitch_001.ogg",  "audio/ui/glitch_002.ogg",
                             "audio/ui/glitch_003.ogg");
        register("gameover", "audio/ui/glass_004.ogg",   "audio/ui/glass_005.ogg");
        register("cover",    "audio/impact/impactWood_heavy_000.ogg",
                             "audio/impact/impactWood_heavy_001.ogg");
        register("leader",   "audio/ui/maximize_008.ogg", "audio/ui/maximize_009.ogg");
        register("heal",     "audio/ui/confirmation_003.ogg", "audio/ui/confirmation_004.ogg");
    }

    private void register(String key, String... paths) {
        List<String> valid = new ArrayList<>();
        for (String p : paths) {
            if (getClass().getResource("/" + p) != null) valid.add(p);
            else System.err.println("[SoundManager] Missing: " + p);
        }
        if (!valid.isEmpty()) soundPaths.put(key, valid);
    }

    public static SoundManager getInstance() { return INSTANCE; }

    /** Play a random variant of the named sound on a background daemon thread. */
    public void play(String key) {
        if (muted) return;
        List<String> paths = soundPaths.get(key);
        if (paths == null || paths.isEmpty()) return;
        String path = "/" + paths.get(rng.nextInt(paths.size()));

        Thread t = new Thread(() -> {
            try (InputStream raw = getClass().getResourceAsStream(path)) {
                if (raw == null) return;
                AudioInputStream oggStream = AudioSystem.getAudioInputStream(
                        new BufferedInputStream(raw));
                AudioFormat srcFmt  = oggStream.getFormat();
                AudioFormat pcmFmt  = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        srcFmt.getSampleRate(), 16,
                        srcFmt.getChannels(),
                        srcFmt.getChannels() * 2,
                        srcFmt.getSampleRate(), false);
                AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFmt, oggStream);
                Clip clip = AudioSystem.getClip();
                clip.open(pcmStream);
                // Set volume via MASTER_GAIN control
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (volume <= 0.0) ? gain.getMinimum()
                             : (float) (20.0 * Math.log10(volume));
                    gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
                }
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                });
                clip.start();
            } catch (Exception e) {
                // non-critical — silently skip
            }
        });
        t.setDaemon(true);
        t.setName("sound-" + key);
        t.start();
    }

    public void setMuted(boolean muted) { this.muted = muted; }
    public boolean isMuted()             { return muted; }
    public void setVolume(double v)      { this.volume = Math.max(0.0, Math.min(1.0, v)); }
    public double getVolume()            { return volume; }
}

