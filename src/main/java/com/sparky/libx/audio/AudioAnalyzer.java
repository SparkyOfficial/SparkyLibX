package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.AudioClip;
import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * audio analyzer for analyzing audio properties and characteristics
 * provides capabilities for frequency analysis, amplitude detection, and audio visualization
 * @author Андрій Будильников
 */
public class AudioAnalyzer {
    
    /**
     * audio analysis results
     */
    public static class AnalysisResult {
        public final double duration;
        public final double averageAmplitude;
        public final double maxAmplitude;
        public final double rms;
        public final double[] spectrum;
        public final double[] peaks;
        
        public AnalysisResult(double duration, double averageAmplitude, double maxAmplitude, 
                            double rms, double[] spectrum, double[] peaks) {
            this.duration = duration;
            this.averageAmplitude = averageAmplitude;
            this.maxAmplitude = maxAmplitude;
            this.rms = rms;
            this.spectrum = spectrum;
            this.peaks = peaks;
        }
    }
    
    /**
     * analyze audio clip properties
     */
    public static AnalysisResult analyze(AudioClip clip) {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        
        // calculate basic properties
        double duration = clip.getDuration();
        double[] amplitudeData = extractAmplitudeData(data, format);
        
        // calculate amplitude statistics
        double averageAmplitude = calculateAverageAmplitude(amplitudeData);
        double maxAmplitude = calculateMaxAmplitude(amplitudeData);
        double rms = calculateRMS(amplitudeData);
        
        // calculate spectrum (simplified)
        double[] spectrum = calculateSpectrum(amplitudeData, format.getSampleRate());
        
        // detect peaks
        double[] peaks = detectPeaks(amplitudeData);
        
        return new AnalysisResult(duration, averageAmplitude, maxAmplitude, rms, spectrum, peaks);
    }
    
    /**
     * extract amplitude data from raw audio data
     */
    private static double[] extractAmplitudeData(byte[] data, AudioFormat format) {
        int frameSize = format.getFrameSize();
        int samples = data.length / frameSize;
        double[] amplitudeData = new double[samples];
        
        for (int i = 0; i < samples; i++) {
            int index = i * frameSize;
            double sampleSum = 0;
            
            // average all channels
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int channelIndex = index + (channel * (frameSize / format.getChannels()));
                
                if (format.getSampleSizeInBits() == 16) {
                    // 16-bit samples
                    short sample = (short) ((data[channelIndex + 1] << 8) | (data[channelIndex] & 0xFF));
                    sampleSum += Math.abs(sample) / (double) Short.MAX_VALUE;
                } else if (format.getSampleSizeInBits() == 8) {
                    // 8-bit samples
                    byte sample = data[channelIndex];
                    sampleSum += Math.abs(sample) / (double) Byte.MAX_VALUE;
                }
            }
            
            amplitudeData[i] = sampleSum / format.getChannels();
        }
        
        return amplitudeData;
    }
    
    /**
     * calculate average amplitude
     */
    private static double calculateAverageAmplitude(double[] amplitudeData) {
        double sum = 0;
        for (double amplitude : amplitudeData) {
            sum += amplitude;
        }
        return sum / amplitudeData.length;
    }
    
    /**
     * calculate maximum amplitude
     */
    private static double calculateMaxAmplitude(double[] amplitudeData) {
        double max = 0;
        for (double amplitude : amplitudeData) {
            max = Math.max(max, amplitude);
        }
        return max;
    }
    
    /**
     * calculate root mean square (RMS)
     */
    private static double calculateRMS(double[] amplitudeData) {
        double sum = 0;
        for (double amplitude : amplitudeData) {
            sum += amplitude * amplitude;
        }
        return Math.sqrt(sum / amplitudeData.length);
    }
    
    /**
     * calculate frequency spectrum (simplified implementation)
     */
    private static double[] calculateSpectrum(double[] amplitudeData, float sampleRate) {
        int spectrumSize = Math.min(1024, amplitudeData.length / 2);
        double[] spectrum = new double[spectrumSize];
        
        // simple frequency bands analysis
        int samplesPerBand = amplitudeData.length / spectrumSize;
        
        for (int band = 0; band < spectrumSize; band++) {
            double bandSum = 0;
            int startSample = band * samplesPerBand;
            int endSample = Math.min(startSample + samplesPerBand, amplitudeData.length);
            
            for (int i = startSample; i < endSample; i++) {
                bandSum += amplitudeData[i];
            }
            
            spectrum[band] = bandSum / (endSample - startSample);
        }
        
        return spectrum;
    }
    
    /**
     * detect peaks in amplitude data
     */
    private static double[] detectPeaks(double[] amplitudeData) {
        List<Double> peaks = new ArrayList<>();
        double threshold = calculateAverageAmplitude(amplitudeData) * 2.0; // 2x average as threshold
        
        for (int i = 1; i < amplitudeData.length - 1; i++) {
            // check if current sample is a peak
            if (amplitudeData[i] > amplitudeData[i - 1] && 
                amplitudeData[i] > amplitudeData[i + 1] && 
                amplitudeData[i] > threshold) {
                peaks.add((double) i / amplitudeData.length); // normalized position
            }
        }
        
        // convert to array
        return peaks.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    /**
     * detect beats in audio data
     */
    public static double[] detectBeats(AudioClip clip) {
        AnalysisResult analysis = analyze(clip);
        List<Double> beats = new ArrayList<>();
        
        // simple beat detection based on peaks and amplitude changes
        double[] peaks = analysis.peaks;
        double averageAmplitude = analysis.averageAmplitude;
        
        // group peaks that are close together as beats
        double beatThreshold = 0.05; // 5% of audio length
        
        for (int i = 0; i < peaks.length; i++) {
            // check if this peak is significantly louder than average
            if (peaks[i] > averageAmplitude * 3) { // 3x average as beat threshold
                // check if this is not too close to previous beat
                if (beats.isEmpty() || (peaks[i] - beats.get(beats.size() - 1)) > beatThreshold) {
                    beats.add(peaks[i]);
                }
            }
        }
        
        // convert to array
        return beats.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    /**
     * calculate audio tempo (beats per minute)
     */
    public static double calculateTempo(AudioClip clip) {
        double[] beats = detectBeats(clip);
        
        if (beats.length < 2) {
            return 0; // not enough beats to calculate tempo
        }
        
        // calculate average time between beats
        double totalTime = 0;
        for (int i = 1; i < beats.length; i++) {
            totalTime += (beats[i] - beats[i - 1]);
        }
        
        double averageBeatInterval = totalTime / (beats.length - 1);
        double duration = clip.getDuration();
        
        // convert to beats per minute
        return 60.0 / (averageBeatInterval * duration);
    }
    
    /**
     * get dominant frequency in audio clip
     */
    public static double getDominantFrequency(AudioClip clip) {
        AnalysisResult analysis = analyze(clip);
        
        // find the frequency band with maximum energy
        double maxEnergy = 0;
        int dominantBand = 0;
        
        for (int i = 0; i < analysis.spectrum.length; i++) {
            if (analysis.spectrum[i] > maxEnergy) {
                maxEnergy = analysis.spectrum[i];
                dominantBand = i;
            }
        }
        
        // convert band index to frequency
        AudioFormat format = clip.getFormat();
        double frequencyRange = format.getSampleRate() / 2.0; // nyquist frequency
        return (dominantBand * frequencyRange) / analysis.spectrum.length;
    }
    
    /**
     * check if audio is stereo
     */
    public static boolean isStereo(AudioClip clip) {
        return clip.getFormat().getChannels() == 2;
    }
    
    /**
     * check if audio is mono
     */
    public static boolean isMono(AudioClip clip) {
        return clip.getFormat().getChannels() == 1;
    }
    
    /**
     * get audio quality score (0.0 to 1.0)
     */
    public static double getQualityScore(AudioClip clip) {
        AnalysisResult analysis = analyze(clip);
        AudioFormat format = clip.getFormat();
        
        // factors affecting quality score:
        // 1. Sample rate (higher is better, up to 48kHz)
        double sampleRateScore = Math.min(format.getSampleRate() / 48000.0, 1.0);
        
        // 2. Bit depth (higher is better)
        double bitDepthScore = format.getSampleSizeInBits() / 24.0;
        
        // 3. Dynamic range (difference between max and average amplitude)
        double dynamicRange = analysis.maxAmplitude - analysis.averageAmplitude;
        double dynamicScore = Math.min(dynamicRange / 0.5, 1.0);
        
        // 4. Clipping detection (penalize if max amplitude is too high)
        double clippingPenalty = analysis.maxAmplitude > 0.95 ? 0.5 : 1.0;
        
        // combine scores
        return (sampleRateScore * 0.3 + bitDepthScore * 0.3 + dynamicScore * 0.3 + clippingPenalty * 0.1);
    }
}