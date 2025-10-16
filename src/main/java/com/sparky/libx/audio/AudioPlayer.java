package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.AudioClip;
import com.sparky.libx.audio.AudioEngine.AudioSource;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * audio player for playing audio clips
 * provides capabilities for playing, pausing, stopping, and controlling audio playback
 * @author Андрій Будильников
 */
public class AudioPlayer {
    
    private final ExecutorService executor;
    private volatile boolean playing;
    private volatile boolean paused;
    private volatile SourceDataLine dataLine;
    private volatile int currentPosition;
    
    public AudioPlayer() {
        this.executor = Executors.newSingleThreadExecutor();
        this.playing = false;
        this.paused = false;
        this.dataLine = null;
        this.currentPosition = 0;
    }
    
    /**
     * play an audio clip
     */
    public void play(AudioClip clip) throws LineUnavailableException {
        if (playing) {
            stop();
        }
        
        AudioFormat format = clip.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("audio line not supported: " + format);
        }
        
        dataLine = (SourceDataLine) AudioSystem.getLine(info);
        dataLine.open(format);
        dataLine.start();
        
        playing = true;
        paused = false;
        currentPosition = 0;
        
        // play in background thread
        executor.submit(() -> playAudioData(clip.getData()));
    }
    
    /**
     * play audio data
     */
    private void playAudioData(byte[] audioData) {
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        
        try {
            while (playing && currentPosition < audioData.length) {
                // wait if paused
                while (paused && playing) {
                    Thread.sleep(10);
                }
                
                if (!playing) break;
                
                // calculate how much data to read
                int remaining = audioData.length - currentPosition;
                int toRead = Math.min(bufferSize, remaining);
                
                // copy data to buffer
                System.arraycopy(audioData, currentPosition, buffer, 0, toRead);
                
                // write to audio line
                if (dataLine != null) {
                    dataLine.write(buffer, 0, toRead);
                }
                
                currentPosition += toRead;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (playing) {
                stop();
            }
        }
    }
    
    /**
     * pause playback
     */
    public void pause() {
        if (playing && !paused) {
            paused = true;
            if (dataLine != null) {
                dataLine.stop();
            }
        }
    }
    
    /**
     * resume playback
     */
    public void resume() {
        if (playing && paused) {
            paused = false;
            if (dataLine != null) {
                dataLine.start();
            }
        }
    }
    
    /**
     * stop playback
     */
    public void stop() {
        playing = false;
        paused = false;
        
        if (dataLine != null) {
            dataLine.stop();
            dataLine.close();
            dataLine = null;
        }
        
        currentPosition = 0;
    }
    
    /**
     * set volume (0.0 to 1.0)
     */
    public void setVolume(double volume) {
        if (dataLine != null) {
            try {
                FloatControl volumeControl = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
                float min = volumeControl.getMinimum();
                float max = volumeControl.getMaximum();
                float scaledVolume = (float) (min + (max - min) * volume);
                volumeControl.setValue(scaledVolume);
            } catch (Exception e) {
                // volume control not supported
            }
        }
    }
    
    /**
     * get current playback position in seconds
     */
    public double getCurrentPosition(AudioClip clip) {
        if (clip == null) return 0.0;
        AudioFormat format = clip.getFormat();
        return (double) currentPosition / (format.getFrameSize() * format.getFrameRate());
    }
    
    /**
     * seek to position in seconds
     */
    public void seek(AudioClip clip, double position) {
        if (clip == null) return;
        
        AudioFormat format = clip.getFormat();
        int targetPosition = (int) (position * format.getFrameSize() * format.getFrameRate());
        this.currentPosition = Math.max(0, Math.min(targetPosition, clip.getData().length));
    }
    
    /**
     * check if currently playing
     */
    public boolean isPlaying() {
        return playing && !paused;
    }
    
    /**
     * check if currently paused
     */
    public boolean isPaused() {
        return paused;
    }
    
    /**
     * get playback progress (0.0 to 1.0)
     */
    public double getProgress(AudioClip clip) {
        if (clip == null || clip.getData().length == 0) return 0.0;
        return (double) currentPosition / clip.getData().length;
    }
    
    /**
     * close the audio player and release resources
     */
    public void close() {
        stop();
        executor.shutdown();
    }
}