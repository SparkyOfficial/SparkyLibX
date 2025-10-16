package com.sparky.libx.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * audio format converter for different audio formats
 * provides capabilities for converting between different sample rates, bit depths, and channel configurations
 * @author Андрій Будильников
 */
public class AudioFormatConverter {
    
    /**
     * convert audio data to a different format
     */
    public static byte[] convertFormat(byte[] audioData, AudioFormat sourceFormat, AudioFormat targetFormat) throws IOException {
        // Create audio input stream from source data
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream sourceStream = new AudioInputStream(bais, sourceFormat, audioData.length / sourceFormat.getFrameSize());
        
        // Convert to target format
        AudioInputStream targetStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);
        
        // Read converted data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = targetStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * resample audio to a different sample rate
     */
    public static byte[] resample(byte[] audioData, AudioFormat sourceFormat, float targetSampleRate) throws IOException {
        AudioFormat targetFormat = new AudioFormat(
            targetSampleRate,
            sourceFormat.getSampleSizeInBits(),
            sourceFormat.getChannels(),
            sourceFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED,
            sourceFormat.isBigEndian()
        );
        return convertFormat(audioData, sourceFormat, targetFormat);
    }
    
    /**
     * change bit depth of audio data
     */
    public static byte[] changeBitDepth(byte[] audioData, AudioFormat sourceFormat, int targetBitDepth) throws IOException {
        AudioFormat targetFormat = new AudioFormat(
            sourceFormat.getSampleRate(),
            targetBitDepth,
            sourceFormat.getChannels(),
            targetBitDepth > 8, // PCM signed for bit depths > 8
            sourceFormat.isBigEndian()
        );
        return convertFormat(audioData, sourceFormat, targetFormat);
    }
    
    /**
     * convert mono to stereo audio
     */
    public static byte[] monoToStereo(byte[] audioData, AudioFormat sourceFormat) throws IOException {
        if (sourceFormat.getChannels() != 1) {
            throw new IllegalArgumentException("source format must be mono");
        }
        
        AudioFormat targetFormat = new AudioFormat(
            sourceFormat.getSampleRate(),
            sourceFormat.getSampleSizeInBits(),
            2, // stereo
            sourceFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED,
            sourceFormat.isBigEndian()
        );
        return convertFormat(audioData, sourceFormat, targetFormat);
    }
    
    /**
     * convert stereo to mono audio
     */
    public static byte[] stereoToMono(byte[] audioData, AudioFormat sourceFormat) throws IOException {
        if (sourceFormat.getChannels() != 2) {
            throw new IllegalArgumentException("source format must be stereo");
        }
        
        AudioFormat targetFormat = new AudioFormat(
            sourceFormat.getSampleRate(),
            sourceFormat.getSampleSizeInBits(),
            1, // mono
            sourceFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED,
            sourceFormat.isBigEndian()
        );
        return convertFormat(audioData, sourceFormat, targetFormat);
    }
    
    /**
     * get compatible audio format for playback
     */
    public static AudioFormat getCompatibleFormat(AudioFormat sourceFormat) {
        return new AudioFormat(
            sourceFormat.getSampleRate(),
            sourceFormat.getSampleSizeInBits(),
            sourceFormat.getChannels(),
            true, // always use signed PCM for compatibility
            false // little endian for compatibility
        );
    }
}