package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.AudioClip;
import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;

/**
 * advanced audio file loader for loading various audio formats
 * supports wav, aiff, au, and other formats supported by java sound api
 * @author Андрій Будильников
 */
public class AudioFileLoader {
    
    /**
     * load audio clip from file path
     */
    public static AudioClip loadAudioClip(String filePath, String name) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("audio file not found: " + filePath);
        }
        
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        return createAudioClipFromStream(audioInputStream, name);
    }
    
    /**
     * load audio clip from url
     */
    public static AudioClip loadAudioClip(URL url, String name) throws Exception {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
        return createAudioClipFromStream(audioInputStream, name);
    }
    
    /**
     * load audio clip from resource
     */
    public static AudioClip loadAudioResource(String resourcePath, String name) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new FileNotFoundException("audio resource not found: " + resourcePath);
        }
        
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(resourceUrl);
        return createAudioClipFromStream(audioInputStream, name);
    }
    
    /**
     * load audio clip from input stream
     */
    public static AudioClip loadAudioStream(InputStream inputStream, String name) throws Exception {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
        return createAudioClipFromStream(audioInputStream, name);
    }
    
    /**
     * create audio clip from audio input stream
     */
    private static AudioClip createAudioClipFromStream(AudioInputStream audioInputStream, String name) throws Exception {
        try {
            AudioFormat format = audioInputStream.getFormat();
            AudioFormat targetFormat = AudioFormatConverter.getCompatibleFormat(format);
            
            // convert to target format if needed
            AudioInputStream convertedStream;
            if (!format.matches(targetFormat)) {
                convertedStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
            } else {
                convertedStream = audioInputStream;
            }
            
            // read all audio data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = convertedStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            byte[] audioData = baos.toByteArray();
            return new AudioClip(name, audioData, targetFormat);
        } finally {
            audioInputStream.close();
        }
    }
    
    /**
     * save audio clip to file (wav format)
     */
    public static void saveAudioClip(AudioClip clip, String filePath) throws Exception {
        File file = new File(filePath);
        AudioFormat format = clip.getFormat();
        byte[] audioData = clip.getData();
        
        // create audio input stream
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());
        
        // write to file
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);
    }
    
    /**
     * get supported audio file types
     */
    public static AudioFileFormat.Type[] getSupportedFileTypes() {
        return AudioSystem.getAudioFileTypes();
    }
    
    /**
     * check if a file format is supported
     */
    public static boolean isFileTypeSupported(AudioFileFormat.Type fileType) {
        return AudioSystem.isFileTypeSupported(fileType);
    }
    
    /**
     * get supported audio formats
     */
    public static AudioFormat[] getSupportedFormats() {
        // this is a simplified implementation
        // in a real application, you would query the system for supported formats
        return new AudioFormat[] {
            new AudioFormat(8000, 8, 1, false, false),   // 8kHz, 8-bit, mono, unsigned
            new AudioFormat(11025, 8, 1, false, false),  // 11kHz, 8-bit, mono, unsigned
            new AudioFormat(22050, 16, 1, true, false),  // 22kHz, 16-bit, mono, signed
            new AudioFormat(44100, 16, 2, true, false),  // 44kHz, 16-bit, stereo, signed
            new AudioFormat(48000, 16, 2, true, false)   // 48kHz, 16-bit, stereo, signed
        };
    }
}