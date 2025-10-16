package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.AudioClip;
import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * audio recorder for recording audio from microphone or other input devices
 * provides capabilities for recording, pausing, and saving audio recordings
 * @author Андрій Будильников
 */
public class AudioRecorder {
    
    private final ExecutorService executor;
    private volatile boolean recording;
    private volatile boolean paused;
    private volatile TargetDataLine dataLine;
    private final ByteArrayOutputStream byteArrayOutputStream;
    private final AudioFormat format;
    
    public AudioRecorder() {
        this.executor = Executors.newSingleThreadExecutor();
        this.recording = false;
        this.paused = false;
        this.dataLine = null;
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        // default format: 44.1kHz, 16-bit, stereo
        this.format = new AudioFormat(44100, 16, 2, true, false);
    }
    
    public AudioRecorder(AudioFormat format) {
        this.executor = Executors.newSingleThreadExecutor();
        this.recording = false;
        this.paused = false;
        this.dataLine = null;
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.format = format;
    }
    
    /**
     * start recording audio
     */
    public void start() throws LineUnavailableException {
        if (recording) {
            stop();
        }
        
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("audio line not supported: " + format);
        }
        
        dataLine = (TargetDataLine) AudioSystem.getLine(info);
        dataLine.open(format);
        dataLine.start();
        
        recording = true;
        paused = false;
        byteArrayOutputStream.reset();
        
        // record in background thread
        executor.submit(this::recordAudioData);
    }
    
    /**
     * record audio data
     */
    private void recordAudioData() {
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        
        try {
            while (recording) {
                // wait if paused
                while (paused && recording) {
                    Thread.sleep(10);
                }
                
                if (!recording) break;
                
                // read audio data
                int bytesRead = dataLine.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * pause recording
     */
    public void pause() {
        if (recording && !paused) {
            paused = true;
            if (dataLine != null) {
                dataLine.stop();
            }
        }
    }
    
    /**
     * resume recording
     */
    public void resume() {
        if (recording && paused) {
            paused = false;
            if (dataLine != null) {
                dataLine.start();
            }
        }
    }
    
    /**
     * stop recording
     */
    public void stop() {
        recording = false;
        paused = false;
        
        if (dataLine != null) {
            dataLine.stop();
            dataLine.close();
            dataLine = null;
        }
    }
    
    /**
     * get recorded audio as audio clip
     */
    public AudioClip getRecordedAudio(String name) {
        byte[] audioData = byteArrayOutputStream.toByteArray();
        return new AudioClip(name, audioData, format);
    }
    
    /**
     * save recorded audio to file (placeholder implementation)
     */
    public void saveToFile(String filePath) throws Exception {
        // in a real implementation, this would save the recorded audio to a file
        // for now, we'll just check if we have data
        if (byteArrayOutputStream.size() == 0) {
            throw new Exception("no audio data to save");
        }
    }
    
    /**
     * get recording duration in seconds
     */
    public double getDuration() {
        return (double) byteArrayOutputStream.size() / (format.getFrameSize() * format.getFrameRate());
    }
    
    /**
     * check if currently recording
     */
    public boolean isRecording() {
        return recording && !paused;
    }
    
    /**
     * check if currently paused
     */
    public boolean isPaused() {
        return paused;
    }
    
    /**
     * get audio format
     */
    public AudioFormat getFormat() {
        return format;
    }
    
    /**
     * close the audio recorder and release resources
     */
    public void close() {
        stop();
        executor.shutdown();
        try {
            byteArrayOutputStream.close();
        } catch (Exception e) {
            // ignore
        }
    }
}