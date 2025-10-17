package com.sparky.libx.audio;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading and saving audio files
 * @author Андрій Будильников
 */
public class AudioFileLoader {
    
    /**
     * Load audio clip from file
     */
    public static AudioEngine.AudioClip loadAudioClip(String filePath, String name) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("audio file not found: " + filePath);
        }
        
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioStream.getFormat();
        
        // convert to PCM format if needed
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(),
                16,
                format.getChannels(),
                format.getChannels() * 2,
                format.getSampleRate(),
                false
            );
            audioStream = AudioSystem.getAudioInputStream(format, audioStream);
        }
        
        // read all audio data
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = audioStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        
        audioStream.close();
        byte[] audioData = buffer.toByteArray();
        
        return new AudioEngine.AudioClip(name, audioData, format);
    }
    
    /**
     * Load audio clip from resource
     */
    public static AudioEngine.AudioClip loadAudioResource(String resourcePath, String name) throws Exception {
        ClassLoader classLoader = AudioFileLoader.class.getClassLoader();
        URL resourceUrl = classLoader.getResource(resourcePath);
        
        if (resourceUrl == null) {
            throw new FileNotFoundException("audio resource not found: " + resourcePath);
        }
        
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(resourceUrl);
        AudioFormat format = audioStream.getFormat();
        
        // convert to PCM format if needed
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(),
                16,
                format.getChannels(),
                format.getChannels() * 2,
                format.getSampleRate(),
                false
            );
            audioStream = AudioSystem.getAudioInputStream(format, audioStream);
        }
        
        // read all audio data
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = audioStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        
        audioStream.close();
        byte[] audioData = buffer.toByteArray();
        
        return new AudioEngine.AudioClip(name, audioData, format);
    }
    
    /**
     * Save audio clip to file
     */
    public static void saveAudioClip(AudioEngine.AudioClip clip, String filePath) throws Exception {
        File file = new File(filePath);
        AudioFormat format = clip.getFormat();
        byte[] audioData = clip.getData();
        
        // create audio input stream from data
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, format, audioData.length / format.getFrameSize());
        
        // write to file
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);
        audioInputStream.close();
    }
    
    /**
     * get supported audio file types
     */
    public static AudioFileFormat.Type[] getSupportedFileTypes() {
        return AudioSystem.getAudioFileTypes();
    }
    
    /**
     * check if file type is supported
     */
    public static boolean isFileTypeSupported(AudioFileFormat.Type fileType) {
        return AudioSystem.isFileTypeSupported(fileType);
    }
    
    /**
     * get supported audio formats
     */
    public static AudioFormat[] getSupportedFormats() {
        // get supported formats from the system
        List<AudioFormat> formats = new ArrayList<>();
        
        // common formats
        formats.add(new AudioFormat(8000, 8, 1, false, false));   // 8kHz, 8-bit, mono, unsigned
        formats.add(new AudioFormat(11025, 8, 1, false, false));  // 11kHz, 8-bit, mono, unsigned
        formats.add(new AudioFormat(22050, 16, 1, true, false));  // 22kHz, 16-bit, mono, signed
        formats.add(new AudioFormat(44100, 16, 2, true, false));  // 44kHz, 16-bit, stereo, signed
        formats.add(new AudioFormat(48000, 16, 2, true, false));  // 48kHz, 16-bit, stereo, signed
        
        // query system for additional formats
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] sourceLines = mixer.getSourceLineInfo();
            for (Line.Info lineInfo : sourceLines) {
                if (lineInfo instanceof DataLine.Info) {
                    DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
                    AudioFormat[] lineFormats = dataLineInfo.getFormats();
                    for (AudioFormat format : lineFormats) {
                        if (!formats.contains(format)) {
                            formats.add(format);
                        }
                    }
                }
            }
        }
        
        return formats.toArray(new AudioFormat[0]);
    }
}