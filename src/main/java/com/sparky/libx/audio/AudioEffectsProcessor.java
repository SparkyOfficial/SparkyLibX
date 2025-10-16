package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.AudioClip;
import javax.sound.sampled.AudioFormat;
import java.util.Random;

/**
 * advanced audio effects processor for applying various audio effects
 * provides capabilities for reverb, echo, distortion, and other audio effects
 * @author Андрій Будильников
 */
public class AudioEffectsProcessor {
    
    /**
     * apply reverb effect to audio clip
     */
    public static AudioClip applyReverb(AudioClip clip, double roomSize, double damping, double width, double level, String name) {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        byte[] processedData = data.clone();
        
        // simplified reverb implementation
        int sampleRate = (int) format.getSampleRate();
        int delaySamples = (int) (roomSize * sampleRate * 0.1); // scale room size to delay
        int bufferSize = Math.min(delaySamples * 2, processedData.length / format.getFrameSize());
        
        // apply simple comb filter reverb
        applyCombFilterReverb(processedData, format, delaySamples, damping, level);
        
        // apply all-pass filter for diffusion
        applyAllPassFilter(processedData, format, delaySamples / 2, width);
        
        return new AudioClip(name, processedData, format);
    }
    
    /**
     * apply echo effect to audio clip
     */
    public static AudioClip applyEcho(AudioClip clip, double delay, double decay, double feedback, String name) {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        byte[] processedData = data.clone();
        
        int sampleRate = (int) format.getSampleRate();
        int delaySamples = (int) (delay * sampleRate);
        
        // apply echo effect
        for (int i = delaySamples; i < processedData.length / format.getFrameSize(); i++) {
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int frameSize = format.getFrameSize();
                int channelSize = frameSize / format.getChannels();
                int currentIndex = i * frameSize + channel * channelSize;
                int delayedIndex = (i - delaySamples) * frameSize + channel * channelSize;
                
                if (delayedIndex >= 0 && currentIndex < processedData.length && delayedIndex < processedData.length) {
                    if (format.getSampleSizeInBits() == 16) {
                        // 16-bit samples
                        short currentSample = (short) ((processedData[currentIndex + 1] << 8) | (processedData[currentIndex] & 0xFF));
                        short delayedSample = (short) ((processedData[delayedIndex + 1] << 8) | (processedData[delayedIndex] & 0xFF));
                        
                        short echoSample = (short) (currentSample + delayedSample * decay * feedback);
                        echoSample = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, echoSample));
                        
                        processedData[currentIndex] = (byte) (echoSample & 0xFF);
                        processedData[currentIndex + 1] = (byte) ((echoSample >> 8) & 0xFF);
                    } else if (format.getSampleSizeInBits() == 8) {
                        // 8-bit samples
                        byte currentSample = processedData[currentIndex];
                        byte delayedSample = processedData[delayedIndex];
                        
                        double echoValue = currentSample + delayedSample * decay * feedback;
                        byte echoSample = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, (int) echoValue));
                        
                        processedData[currentIndex] = (byte) echoSample;
                    }
                }
            }
        }
        
        return new AudioClip(name, processedData, format);
    }
    
    /**
     * apply distortion effect to audio clip
     */
    public static AudioClip applyDistortion(AudioClip clip, double gain, double threshold, double mix, String name) {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        byte[] processedData = data.clone();
        
        // apply distortion effect
        for (int i = 0; i < processedData.length; i += format.getFrameSize()) {
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int channelSize = format.getFrameSize() / format.getChannels();
                int channelIndex = i + channel * channelSize;
                
                if (format.getSampleSizeInBits() == 16) {
                    // 16-bit samples
                    short sample = (short) ((processedData[channelIndex + 1] << 8) | (processedData[channelIndex] & 0xFF));
                    
                    // apply gain
                    double amplified = sample * gain;
                    
                    // apply clipping distortion
                    double distorted;
                    if (Math.abs(amplified) > threshold * Short.MAX_VALUE) {
                        distorted = Math.signum(amplified) * Short.MAX_VALUE;
                    } else {
                        distorted = amplified;
                    }
                    
                    // mix dry and wet signals
                    double mixed = sample * (1.0 - mix) + distorted * mix;
                    short result = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, (int) mixed));
                    
                    processedData[channelIndex] = (byte) (result & 0xFF);
                    processedData[channelIndex + 1] = (byte) ((result >> 8) & 0xFF);
                } else if (format.getSampleSizeInBits() == 8) {
                    // 8-bit samples
                    byte sample = processedData[channelIndex];
                    
                    // apply gain
                    double amplified = sample * gain;
                    
                    // apply clipping distortion
                    double distorted;
                    if (Math.abs(amplified) > threshold * Byte.MAX_VALUE) {
                        distorted = Math.signum(amplified) * Byte.MAX_VALUE;
                    } else {
                        distorted = amplified;
                    }
                    
                    // mix dry and wet signals
                    double mixed = sample * (1.0 - mix) + distorted * mix;
                    byte result = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, (int) mixed));
                    
                    processedData[channelIndex] = result;
                }
            }
        }
        
        return new AudioClip(name, processedData, format);
    }
    
    /**
     * apply chorus effect to audio clip
     */
    public static AudioClip applyChorus(AudioClip clip, double depth, double rate, double mix, String name) {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        byte[] processedData = data.clone();
        
        int sampleRate = (int) format.getSampleRate();
        Random random = new Random(42); // fixed seed for consistent results
        
        // apply chorus effect with slight variations
        for (int i = 0; i < processedData.length; i += format.getFrameSize()) {
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int channelSize = format.getFrameSize() / format.getChannels();
                int channelIndex = i + channel * channelSize;
                
                // calculate modulation
                double modulation = Math.sin(2 * Math.PI * rate * i / sampleRate) * depth * sampleRate;
                int delaySamples = (int) Math.abs(modulation);
                
                if (format.getSampleSizeInBits() == 16) {
                    // 16-bit samples
                    short sample = (short) ((processedData[channelIndex + 1] << 8) | (processedData[channelIndex] & 0xFF));
                    
                    // get delayed sample with linear interpolation
                    int delayedIndex = Math.max(0, i - delaySamples);
                    if (delayedIndex < processedData.length - format.getFrameSize()) {
                        short delayedSample = (short) ((processedData[delayedIndex + channelIndex + 1] << 8) | 
                                                     (processedData[delayedIndex + channelIndex] & 0xFF));
                        
                        // mix dry and wet signals
                        double mixed = sample * (1.0 - mix) + delayedSample * mix;
                        short result = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, (int) mixed));
                        
                        processedData[channelIndex] = (byte) (result & 0xFF);
                        processedData[channelIndex + 1] = (byte) ((result >> 8) & 0xFF);
                    }
                } else if (format.getSampleSizeInBits() == 8) {
                    // 8-bit samples
                    byte sample = processedData[channelIndex];
                    
                    // get delayed sample
                    int delayedIndex = Math.max(0, i - delaySamples);
                    if (delayedIndex < processedData.length - format.getFrameSize()) {
                        byte delayedSample = processedData[delayedIndex + channelIndex];
                        
                        // mix dry and wet signals
                        double mixed = sample * (1.0 - mix) + delayedSample * mix;
                        byte result = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, (int) mixed));
                        
                        processedData[channelIndex] = result;
                    }
                }
            }
        }
        
        return new AudioClip(name, processedData, format);
    }
    
    /**
     * apply flanger effect to audio clip
     */
    public static AudioClip applyFlanger(AudioClip clip, double depth, double rate, double feedback, String name) {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        byte[] processedData = data.clone();
        
        int sampleRate = (int) format.getSampleRate();
        
        // apply flanger effect
        for (int i = 0; i < processedData.length; i += format.getFrameSize()) {
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int channelSize = format.getFrameSize() / format.getChannels();
                int channelIndex = i + channel * channelSize;
                
                // calculate LFO (low frequency oscillator)
                double lfo = Math.sin(2 * Math.PI * rate * i / sampleRate) * depth * sampleRate * 0.01;
                int delaySamples = (int) Math.abs(lfo);
                
                if (format.getSampleSizeInBits() == 16) {
                    // 16-bit samples
                    short sample = (short) ((processedData[channelIndex + 1] << 8) | (processedData[channelIndex] & 0xFF));
                    
                    // get delayed sample
                    int delayedIndex = Math.max(0, i - delaySamples);
                    if (delayedIndex < processedData.length - format.getFrameSize()) {
                        short delayedSample = (short) ((processedData[delayedIndex + channelIndex + 1] << 8) | 
                                                     (processedData[delayedIndex + channelIndex] & 0xFF));
                        
                        // apply feedback
                        short flanged = (short) (sample + delayedSample * feedback);
                        flanged = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, flanged));
                        
                        processedData[channelIndex] = (byte) (flanged & 0xFF);
                        processedData[channelIndex + 1] = (byte) ((flanged >> 8) & 0xFF);
                    }
                } else if (format.getSampleSizeInBits() == 8) {
                    // 8-bit samples
                    byte sample = processedData[channelIndex];
                    
                    // get delayed sample
                    int delayedIndex = Math.max(0, i - delaySamples);
                    if (delayedIndex < processedData.length - format.getFrameSize()) {
                        byte delayedSample = processedData[delayedIndex + channelIndex];
                        
                        // apply feedback
                        double flangedValue = sample + delayedSample * feedback;
                        byte flanged = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, (int) flangedValue));
                        
                        processedData[channelIndex] = (byte) flanged;
                    }
                }
            }
        }
        
        return new AudioClip(name, processedData, format);
    }
    
    /**
     * apply comb filter reverb
     */
    private static void applyCombFilterReverb(byte[] data, AudioFormat format, int delaySamples, double damping, double level) {
        // simplified comb filter implementation
        for (int i = delaySamples; i < data.length / format.getFrameSize(); i++) {
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int frameSize = format.getFrameSize();
                int channelSize = frameSize / format.getChannels();
                int currentIndex = i * frameSize + channel * channelSize;
                int delayedIndex = (i - delaySamples) * frameSize + channel * channelSize;
                
                if (delayedIndex >= 0 && currentIndex < data.length && delayedIndex < data.length) {
                    if (format.getSampleSizeInBits() == 16) {
                        // 16-bit samples
                        short currentSample = (short) ((data[currentIndex + 1] << 8) | (data[currentIndex] & 0xFF));
                        short delayedSample = (short) ((data[delayedIndex + 1] << 8) | (data[delayedIndex] & 0xFF));
                        
                        short reverbSample = (short) (currentSample + delayedSample * level * damping);
                        reverbSample = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, reverbSample));
                        
                        data[currentIndex] = (byte) (reverbSample & 0xFF);
                        data[currentIndex + 1] = (byte) ((reverbSample >> 8) & 0xFF);
                    } else if (format.getSampleSizeInBits() == 8) {
                        // 8-bit samples
                        byte currentSample = data[currentIndex];
                        byte delayedSample = data[delayedIndex];
                        
                        double reverbValue = currentSample + delayedSample * level * damping;
                        byte reverbSample = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, (int) reverbValue));
                        
                        data[currentIndex] = (byte) reverbSample;
                    }
                }
            }
        }
    }
    
    /**
     * apply all-pass filter
     */
    private static void applyAllPassFilter(byte[] data, AudioFormat format, int delaySamples, double coefficient) {
        // simplified all-pass filter implementation
        for (int i = delaySamples; i < data.length / format.getFrameSize(); i++) {
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int frameSize = format.getFrameSize();
                int channelSize = frameSize / format.getChannels();
                int currentIndex = i * frameSize + channel * channelSize;
                int delayedIndex = (i - delaySamples) * frameSize + channel * channelSize;
                
                if (delayedIndex >= 0 && currentIndex < data.length && delayedIndex < data.length) {
                    if (format.getSampleSizeInBits() == 16) {
                        // 16-bit samples
                        short currentSample = (short) ((data[currentIndex + 1] << 8) | (data[currentIndex] & 0xFF));
                        short delayedSample = (short) ((data[delayedIndex + 1] << 8) | (data[delayedIndex] & 0xFF));
                        
                        // all-pass filter formula: y[n] = -g * x[n] + x[n-D] + g * y[n-D]
                        short filtered = (short) (-coefficient * currentSample + delayedSample + coefficient * delayedSample);
                        filtered = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, filtered));
                        
                        data[currentIndex] = (byte) (filtered & 0xFF);
                        data[currentIndex + 1] = (byte) ((filtered >> 8) & 0xFF);
                    } else if (format.getSampleSizeInBits() == 8) {
                        // 8-bit samples
                        byte currentSample = data[currentIndex];
                        byte delayedSample = data[delayedIndex];
                        
                        // all-pass filter formula
                        int filtered = (int) (-coefficient * currentSample + delayedSample + coefficient * delayedSample);
                        filtered = Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, filtered));
                        
                        data[currentIndex] = (byte) filtered;
                    }
                }
            }
        }
    }
    
    /**
     * apply low-pass filter
     */
    public static AudioClip applyLowPassFilter(AudioClip clip, double cutoffFrequency, String name) {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        byte[] processedData = data.clone();
        
        // simplified low-pass filter implementation
        double rc = 1.0 / (2 * Math.PI * cutoffFrequency);
        double dt = 1.0 / format.getSampleRate();
        double alpha = dt / (rc + dt);
        
        for (int i = format.getFrameSize(); i < processedData.length; i++) {
            processedData[i] = (byte) (alpha * processedData[i] + (1 - alpha) * processedData[i - format.getFrameSize()]);
        }
        
        return new AudioClip(name, processedData, format);
    }
    
    /**
     * apply high-pass filter
     */
    public static AudioClip applyHighPassFilter(AudioClip clip, double cutoffFrequency, String name) {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        byte[] processedData = data.clone();
        
        // simplified high-pass filter implementation
        double rc = 1.0 / (2 * Math.PI * cutoffFrequency);
        double dt = 1.0 / format.getSampleRate();
        double alpha = rc / (rc + dt);
        
        byte prevInput = 0;
        byte prevOutput = 0;
        
        for (int i = 0; i < processedData.length; i++) {
            byte input = processedData[i];
            byte output = (byte) (alpha * (prevOutput + input - prevInput));
            processedData[i] = output;
            prevInput = input;
            prevOutput = output;
        }
        
        return new AudioClip(name, processedData, format);
    }
}