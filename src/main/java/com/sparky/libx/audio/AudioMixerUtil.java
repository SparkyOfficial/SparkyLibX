package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.AudioClip;
import javax.sound.sampled.AudioFormat;
import java.util.List;

/**
 * advanced audio mixer utility for combining multiple audio clips
 * provides capabilities for mixing, fading, and crossfading audio clips
 * @author Андрій Будильников
 */
public class AudioMixerUtil {
    
    /**
     * mix multiple audio clips together
     */
    public static AudioClip mixClips(List<AudioClip> clips, String name) throws Exception {
        if (clips == null || clips.isEmpty()) {
            throw new IllegalArgumentException("clips list cannot be null or empty");
        }
        
        // use the format of the first clip as the target format
        AudioFormat targetFormat = clips.get(0).getFormat();
        
        // find the maximum duration
        double maxDuration = 0;
        for (AudioClip clip : clips) {
            maxDuration = Math.max(maxDuration, clip.getDuration());
        }
        
        // calculate buffer size
        int bufferSize = (int) (maxDuration * targetFormat.getFrameRate() * targetFormat.getFrameSize());
        byte[] mixedData = new byte[bufferSize];
        
        // mix all clips
        for (AudioClip clip : clips) {
            byte[] clipData = clip.getData();
            AudioFormat clipFormat = clip.getFormat();
            
            // convert format if needed
            byte[] convertedData;
            if (!clipFormat.matches(targetFormat)) {
                convertedData = AudioFormatConverter.convertFormat(clipData, clipFormat, targetFormat);
            } else {
                convertedData = clipData;
            }
            
            // mix the data
            mixAudioData(mixedData, convertedData, targetFormat);
        }
        
        // normalize the mixed data to prevent clipping
        normalizeAudioData(mixedData, targetFormat);
        
        return new AudioClip(name, mixedData, targetFormat);
    }
    
    /**
     * mix two audio clips with specified volumes
     */
    public static AudioClip mixClips(AudioClip clip1, double volume1, AudioClip clip2, double volume2, String name) throws Exception {
        AudioFormat format1 = clip1.getFormat();
        AudioFormat format2 = clip2.getFormat();
        
        // use format1 as target format
        AudioFormat targetFormat = format1;
        
        // find the maximum duration
        double maxDuration = Math.max(clip1.getDuration(), clip2.getDuration());
        int bufferSize = (int) (maxDuration * targetFormat.getFrameRate() * targetFormat.getFrameSize());
        byte[] mixedData = new byte[bufferSize];
        
        // process first clip
        byte[] data1 = clip1.getData();
        byte[] convertedData1;
        if (!format1.matches(targetFormat)) {
            convertedData1 = AudioFormatConverter.convertFormat(data1, format1, targetFormat);
        } else {
            convertedData1 = data1;
        }
        applyVolume(convertedData1, targetFormat, volume1);
        System.arraycopy(convertedData1, 0, mixedData, 0, Math.min(convertedData1.length, mixedData.length));
        
        // process second clip
        byte[] data2 = clip2.getData();
        byte[] convertedData2;
        if (!format2.matches(targetFormat)) {
            convertedData2 = AudioFormatConverter.convertFormat(data2, format2, targetFormat);
        } else {
            convertedData2 = data2;
        }
        applyVolume(convertedData2, targetFormat, volume2);
        mixAudioData(mixedData, convertedData2, targetFormat);
        
        // normalize the mixed data to prevent clipping
        normalizeAudioData(mixedData, targetFormat);
        
        return new AudioClip(name, mixedData, targetFormat);
    }
    
    /**
     * crossfade two audio clips
     */
    public static AudioClip crossfadeClips(AudioClip clip1, AudioClip clip2, double crossfadeDuration, String name) throws Exception {
        AudioFormat format1 = clip1.getFormat();
        AudioFormat format2 = clip2.getFormat();
        
        // use format1 as target format
        AudioFormat targetFormat = format1;
        
        // convert second clip if needed
        byte[] data2 = clip2.getData();
        byte[] convertedData2;
        if (!format2.matches(targetFormat)) {
            convertedData2 = AudioFormatConverter.convertFormat(data2, format2, targetFormat);
        } else {
            convertedData2 = data2;
        }
        
        // calculate crossfade samples
        int crossfadeSamples = (int) (crossfadeDuration * targetFormat.getFrameRate());
        
        // create result buffer
        int totalSamples1 = clip1.getData().length / targetFormat.getFrameSize();
        int totalSamples2 = convertedData2.length / targetFormat.getFrameSize();
        int resultSamples = totalSamples1 + totalSamples2 - crossfadeSamples;
        byte[] resultData = new byte[resultSamples * targetFormat.getFrameSize()];
        
        // copy first clip up to crossfade start
        int crossfadeStart = totalSamples1 - crossfadeSamples;
        System.arraycopy(clip1.getData(), 0, resultData, 0, crossfadeStart * targetFormat.getFrameSize());
        
        // apply crossfade
        for (int i = 0; i < crossfadeSamples; i++) {
            double ratio = (double) i / crossfadeSamples;
            
            // get samples from both clips
            int sampleIndex1 = crossfadeStart + i;
            int sampleIndex2 = i;
            
            if (sampleIndex1 < totalSamples1 && sampleIndex2 < totalSamples2) {
                // apply crossfade mixing
                mixCrossfadeSamples(resultData, clip1.getData(), convertedData2, sampleIndex1, sampleIndex2, ratio, targetFormat);
            }
        }
        
        // copy remaining of second clip
        int resultOffset = (crossfadeStart + crossfadeSamples) * targetFormat.getFrameSize();
        int secondClipOffset = crossfadeSamples * targetFormat.getFrameSize();
        int remainingLength = Math.min(
            resultData.length - resultOffset,
            convertedData2.length - secondClipOffset
        );
        if (remainingLength > 0) {
            System.arraycopy(convertedData2, secondClipOffset, resultData, resultOffset, remainingLength);
        }
        
        return new AudioClip(name, resultData, targetFormat);
    }
    
    /**
     * fade in an audio clip
     */
    public static AudioClip fadeIn(AudioClip clip, double fadeDuration, String name) throws Exception {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData().clone();
        
        int fadeSamples = (int) (fadeDuration * format.getFrameRate());
        int totalSamples = data.length / format.getFrameSize();
        
        // apply fade in
        for (int i = 0; i < Math.min(fadeSamples, totalSamples); i++) {
            double ratio = (double) i / fadeSamples;
            applyFadeToSample(data, i, ratio, format);
        }
        
        return new AudioClip(name, data, format);
    }
    
    /**
     * fade out an audio clip
     */
    public static AudioClip fadeOut(AudioClip clip, double fadeDuration, String name) throws Exception {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData().clone();
        
        int fadeSamples = (int) (fadeDuration * format.getFrameRate());
        int totalSamples = data.length / format.getFrameSize();
        
        // apply fade out
        for (int i = 0; i < Math.min(fadeSamples, totalSamples); i++) {
            double ratio = (double) i / fadeSamples;
            int sampleIndex = totalSamples - fadeSamples + i;
            applyFadeToSample(data, sampleIndex, 1.0 - ratio, format);
        }
        
        return new AudioClip(name, data, format);
    }
    
    /**
     * mix audio data arrays
     */
    private static void mixAudioData(byte[] target, byte[] source, AudioFormat format) {
        int frameSize = format.getFrameSize();
        int samples = Math.min(target.length, source.length) / frameSize;
        
        for (int i = 0; i < samples; i++) {
            int targetIndex = i * frameSize;
            int sourceIndex = i * frameSize;
            
            // mix each channel
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int channelIndex = targetIndex + (channel * (frameSize / format.getChannels()));
                
                if (format.getSampleSizeInBits() == 16) {
                    // 16-bit samples
                    short targetSample = (short) ((target[channelIndex + 1] << 8) | (target[channelIndex] & 0xFF));
                    short sourceSample = (short) ((source[sourceIndex + 1] << 8) | (source[sourceIndex] & 0xFF));
                    
                    // simple mixing (this can cause clipping, so we should normalize later)
                    int mixed = targetSample + sourceSample;
                    mixed = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, mixed));
                    
                    target[channelIndex] = (byte) (mixed & 0xFF);
                    target[channelIndex + 1] = (byte) ((mixed >> 8) & 0xFF);
                } else if (format.getSampleSizeInBits() == 8) {
                    // 8-bit samples
                    byte targetSample = target[channelIndex];
                    byte sourceSample = source[sourceIndex];
                    
                    // simple mixing
                    int mixed = targetSample + sourceSample;
                    mixed = Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, mixed));
                    
                    target[channelIndex] = (byte) mixed;
                }
            }
        }
    }
    
    /**
     * apply volume to audio data
     */
    private static void applyVolume(byte[] data, AudioFormat format, double volume) {
        int frameSize = format.getFrameSize();
        int samples = data.length / frameSize;
        
        for (int i = 0; i < samples; i++) {
            int index = i * frameSize;
            
            // apply to each channel
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int channelIndex = index + (channel * (frameSize / format.getChannels()));
                
                if (format.getSampleSizeInBits() == 16) {
                    // 16-bit samples
                    short sample = (short) ((data[channelIndex + 1] << 8) | (data[channelIndex] & 0xFF));
                    sample = (short) (sample * volume);
                    data[channelIndex] = (byte) (sample & 0xFF);
                    data[channelIndex + 1] = (byte) ((sample >> 8) & 0xFF);
                } else if (format.getSampleSizeInBits() == 8) {
                    // 8-bit samples
                    byte sample = data[channelIndex];
                    sample = (byte) (sample * volume);
                    data[channelIndex] = sample;
                }
            }
        }
    }
    
    /**
     * normalize audio data to prevent clipping
     */
    private static void normalizeAudioData(byte[] data, AudioFormat format) {
        int frameSize = format.getFrameSize();
        int samples = data.length / frameSize;
        
        // find maximum amplitude
        int maxAmplitude = 0;
        for (int i = 0; i < samples; i++) {
            int index = i * frameSize;
            
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int channelIndex = index + (channel * (frameSize / format.getChannels()));
                
                if (format.getSampleSizeInBits() == 16) {
                    // 16-bit samples
                    short sample = (short) ((data[channelIndex + 1] << 8) | (data[channelIndex] & 0xFF));
                    maxAmplitude = Math.max(maxAmplitude, Math.abs(sample));
                } else if (format.getSampleSizeInBits() == 8) {
                    // 8-bit samples
                    byte sample = data[channelIndex];
                    maxAmplitude = Math.max(maxAmplitude, Math.abs(sample));
                }
            }
        }
        
        // apply normalization if needed
        if (format.getSampleSizeInBits() == 16 && maxAmplitude > Short.MAX_VALUE * 0.9) {
            double scale = (double) (Short.MAX_VALUE * 0.9) / maxAmplitude;
            applyVolume(data, format, scale);
        } else if (format.getSampleSizeInBits() == 8 && maxAmplitude > Byte.MAX_VALUE * 0.9) {
            double scale = (double) (Byte.MAX_VALUE * 0.9) / maxAmplitude;
            applyVolume(data, format, scale);
        }
    }
    
    /**
     * mix crossfade samples
     */
    private static void mixCrossfadeSamples(byte[] result, byte[] data1, byte[] data2, 
                                          int sampleIndex1, int sampleIndex2, double ratio, AudioFormat format) {
        int frameSize = format.getFrameSize();
        int resultIndex = sampleIndex1 * frameSize;
        int index1 = sampleIndex1 * frameSize;
        int index2 = sampleIndex2 * frameSize;
        
        // mix each channel with crossfade ratio
        for (int channel = 0; channel < format.getChannels(); channel++) {
            int channelSize = frameSize / format.getChannels();
            int channelIndex = channel * channelSize;
            
            if (format.getSampleSizeInBits() == 16) {
                // 16-bit samples
                short sample1 = (short) ((data1[index1 + channelIndex + 1] << 8) | (data1[index1 + channelIndex] & 0xFF));
                short sample2 = (short) ((data2[index2 + channelIndex + 1] << 8) | (data2[index2 + channelIndex] & 0xFF));
                
                // apply crossfade
                double mixed = sample1 * (1.0 - ratio) + sample2 * ratio;
                short resultSample = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, (int) mixed));
                
                result[resultIndex + channelIndex] = (byte) (resultSample & 0xFF);
                result[resultIndex + channelIndex + 1] = (byte) ((resultSample >> 8) & 0xFF);
            } else if (format.getSampleSizeInBits() == 8) {
                // 8-bit samples
                byte sample1 = data1[index1 + channelIndex];
                byte sample2 = data2[index2 + channelIndex];
                
                // apply crossfade
                double mixed = sample1 * (1.0 - ratio) + sample2 * ratio;
                byte resultSample = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, (int) mixed));
                
                result[resultIndex + channelIndex] = resultSample;
            }
        }
    }
    
    /**
     * apply fade to a specific sample
     */
    private static void applyFadeToSample(byte[] data, int sampleIndex, double ratio, AudioFormat format) {
        int frameSize = format.getFrameSize();
        int index = sampleIndex * frameSize;
        
        // apply fade to each channel
        for (int channel = 0; channel < format.getChannels(); channel++) {
            int channelIndex = index + (channel * (frameSize / format.getChannels()));
            
            if (format.getSampleSizeInBits() == 16) {
                // 16-bit samples
                short sample = (short) ((data[channelIndex + 1] << 8) | (data[channelIndex] & 0xFF));
                sample = (short) (sample * ratio);
                data[channelIndex] = (byte) (sample & 0xFF);
                data[channelIndex + 1] = (byte) ((sample >> 8) & 0xFF);
            } else if (format.getSampleSizeInBits() == 8) {
                // 8-bit samples
                byte sample = data[channelIndex];
                sample = (byte) (sample * ratio);
                data[channelIndex] = sample;
            }
        }
    }
}