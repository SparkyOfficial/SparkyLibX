package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.AudioClip;
import javax.sound.sampled.AudioFormat;
import java.util.*;

/**
 * Advanced audio analyzer with spectral analysis, beat detection, and audio fingerprinting
 */
public class AudioAnalyzerAdvanced {
    
    /**
     * Audio analysis result containing detailed audio characteristics
     */
    public static class AdvancedAnalysisResult {
        public final double duration;
        public final double averageAmplitude;
        public final double maxAmplitude;
        public final double rms;
        public final double[] spectrum;
        public final double[] peaks;
        public final double[] zeroCrossings;
        public final double spectralCentroid;
        public final double spectralRolloff;
        public final double spectralFlux;
        public final double[] mfcc;
        public final BeatInfo[] beats;
        public final double tempo;
        public final double loudness;
        
        public AdvancedAnalysisResult(double duration, double averageAmplitude, double maxAmplitude,
                double rms, double[] spectrum, double[] peaks, double[] zeroCrossings,
                double spectralCentroid, double spectralRolloff, double spectralFlux,
                double[] mfcc, BeatInfo[] beats, double tempo, double loudness) {
            this.duration = duration;
            this.averageAmplitude = averageAmplitude;
            this.maxAmplitude = maxAmplitude;
            this.rms = rms;
            this.spectrum = spectrum;
            this.peaks = peaks;
            this.zeroCrossings = zeroCrossings;
            this.spectralCentroid = spectralCentroid;
            this.spectralRolloff = spectralRolloff;
            this.spectralFlux = spectralFlux;
            this.mfcc = mfcc;
            this.beats = beats;
            this.tempo = tempo;
            this.loudness = loudness;
        }
    }
    
    /**
     * Beat detection information
     */
    public static class BeatInfo {
        public final double timestamp;
        public final double strength;
        public final double frequency;
        
        public BeatInfo(double timestamp, double strength, double frequency) {
            this.timestamp = timestamp;
            this.strength = strength;
            this.frequency = frequency;
        }
    }
    
    /**
     * Audio fingerprint for audio identification
     */
    public static class AudioFingerprint {
        public final long[] hashValues;
        public final int[] timeIndices;
        public final double[] frequencies;
        
        public AudioFingerprint(long[] hashValues, int[] timeIndices, double[] frequencies) {
            this.hashValues = hashValues;
            this.timeIndices = timeIndices;
            this.frequencies = frequencies;
        }
    }
    
    /**
     * Perform advanced audio analysis
     */
    public static AdvancedAnalysisResult analyzeAdvanced(AudioClip clip) {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        
        // Calculate basic properties
        double duration = clip.getDuration();
        double[] amplitudeData = extractAmplitudeData(data, format);
        
        // Calculate amplitude statistics
        double averageAmplitude = calculateAverageAmplitude(amplitudeData);
        double maxAmplitude = calculateMaxAmplitude(amplitudeData);
        double rms = calculateRMS(amplitudeData);
        double loudness = calculateLoudness(amplitudeData);
        
        // Calculate spectrum (simplified)
        double[] spectrum = calculateSpectrum(amplitudeData, format.getSampleRate());
        
        // Detect peaks
        double[] peaks = detectPeaks(amplitudeData);
        
        // Calculate zero crossings
        double[] zeroCrossings = calculateZeroCrossings(amplitudeData);
        
        // Calculate spectral features
        double spectralCentroid = calculateSpectralCentroid(spectrum, format.getSampleRate());
        double spectralRolloff = calculateSpectralRolloff(spectrum, format.getSampleRate(), 0.85);
        double spectralFlux = calculateSpectralFlux(spectrum);
        
        // Calculate MFCC (simplified)
        double[] mfcc = calculateMFCC(spectrum, 13);
        
        // Detect beats
        BeatInfo[] beats = detectBeats(amplitudeData, format.getSampleRate());
        double tempo = calculateTempo(beats);
        
        return new AdvancedAnalysisResult(
            duration, averageAmplitude, maxAmplitude, rms, spectrum, peaks,
            zeroCrossings, spectralCentroid, spectralRolloff, spectralFlux,
            mfcc, beats, tempo, loudness
        );
    }
    
    /**
     * Create an audio fingerprint for identification
     */
    public static AudioFingerprint createFingerprint(AudioClip clip) {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        double[] amplitudeData = extractAmplitudeData(data, format);
        
        // Calculate spectrogram
        double[][] spectrogram = calculateSpectrogram(amplitudeData, format.getSampleRate(), 1024, 512);
        
        // Extract key points for fingerprinting
        List<Long> hashValues = new ArrayList<>();
        List<Integer> timeIndices = new ArrayList<>();
        List<Double> frequencies = new ArrayList<>();
        
        // Simplified fingerprinting algorithm
        for (int t = 0; t < spectrogram.length - 10; t += 5) {
            for (int f = 0; f < spectrogram[t].length - 10; f += 5) {
                // Find local maximum in a 10x10 window
                double maxValue = spectrogram[t][f];
                int maxF = f, maxT = t;
                
                for (int dt = 0; dt < 10 && t + dt < spectrogram.length; dt++) {
                    for (int df = 0; df < 10 && f + df < spectrogram[t].length; df++) {
                        if (spectrogram[t + dt][f + df] > maxValue) {
                            maxValue = spectrogram[t + dt][f + df];
                            maxF = f + df;
                            maxT = t + dt;
                        }
                    }
                }
                
                // Create hash from frequency and time
                long hash = ((long) maxF << 32) | (maxT & 0xFFFFFFFFL);
                hashValues.add(hash);
                timeIndices.add(maxT);
                frequencies.add((double) maxF * format.getSampleRate() / 1024);
            }
        }
        
        return new AudioFingerprint(
            hashValues.stream().mapToLong(Long::longValue).toArray(),
            timeIndices.stream().mapToInt(Integer::intValue).toArray(),
            frequencies.stream().mapToDouble(Double::doubleValue).toArray()
        );
    }
    
    /**
     * Compare two audio fingerprints
     */
    public static double compareFingerprints(AudioFingerprint fp1, AudioFingerprint fp2) {
        if (fp1.hashValues.length == 0 || fp2.hashValues.length == 0) {
            return 0.0;
        }
        
        Set<Long> set1 = new HashSet<>();
        for (long hash : fp1.hashValues) {
            set1.add(hash);
        }
        
        int matches = 0;
        for (long hash : fp2.hashValues) {
            if (set1.contains(hash)) {
                matches++;
            }
        }
        
        return (2.0 * matches) / (fp1.hashValues.length + fp2.hashValues.length);
    }
    
    /**
     * Extract amplitude data from raw audio bytes
     */
    private static double[] extractAmplitudeData(byte[] data, AudioFormat format) {
        int bytesPerSample = format.getSampleSizeInBits() / 8;
        int numSamples = data.length / bytesPerSample;
        double[] amplitudeData = new double[numSamples];
        
        for (int i = 0; i < numSamples; i++) {
            int sample = 0;
            for (int b = 0; b < bytesPerSample; b++) {
                sample |= (data[i * bytesPerSample + b] & 0xFF) << (b * 8);
            }
            
            // Convert to signed value
            if (format.getSampleSizeInBits() == 16) {
                amplitudeData[i] = (short) sample / (double) Short.MAX_VALUE;
            } else {
                amplitudeData[i] = sample / (double) ((1 << format.getSampleSizeInBits()) - 1);
            }
        }
        
        return amplitudeData;
    }
    
    /**
     * Calculate average amplitude
     */
    private static double calculateAverageAmplitude(double[] data) {
        double sum = 0;
        for (double value : data) {
            sum += Math.abs(value);
        }
        return sum / data.length;
    }
    
    /**
     * Calculate maximum amplitude
     */
    private static double calculateMaxAmplitude(double[] data) {
        double max = 0;
        for (double value : data) {
            max = Math.max(max, Math.abs(value));
        }
        return max;
    }
    
    /**
     * Calculate RMS (Root Mean Square)
     */
    private static double calculateRMS(double[] data) {
        double sum = 0;
        for (double value : data) {
            sum += value * value;
        }
        return Math.sqrt(sum / data.length);
    }
    
    /**
     * Calculate perceived loudness
     */
    private static double calculateLoudness(double[] data) {
        // A-weighted loudness calculation (simplified)
        double sum = 0;
        for (double value : data) {
            sum += Math.pow(Math.abs(value), 0.67);
        }
        return Math.pow(sum / data.length, 1/0.67);
    }
    
    /**
     * Calculate spectrum using simplified FFT
     */
    private static double[] calculateSpectrum(double[] data, double sampleRate) {
        int fftSize = nextPowerOf2(data.length);
        double[] paddedData = Arrays.copyOf(data, fftSize);
        
        // Apply window function
        for (int i = 0; i < fftSize; i++) {
            double window = 0.5 * (1 - Math.cos(2 * Math.PI * i / (fftSize - 1)));
            paddedData[i] *= window;
        }
        
        // Calculate magnitude spectrum (simplified)
        double[] spectrum = new double[fftSize / 2];
        for (int i = 0; i < spectrum.length; i++) {
            spectrum[i] = Math.abs(paddedData[i]); // Simplified
        }
        
        return spectrum;
    }
    
    /**
     * Detect peaks in amplitude data
     */
    private static double[] detectPeaks(double[] data) {
        List<Double> peaks = new ArrayList<>();
        double threshold = calculateAverageAmplitude(data) * 2.0;
        
        for (int i = 1; i < data.length - 1; i++) {
            if (Math.abs(data[i]) > threshold && 
                Math.abs(data[i]) > Math.abs(data[i-1]) && 
                Math.abs(data[i]) > Math.abs(data[i+1])) {
                peaks.add(data[i]);
            }
        }
        
        return peaks.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    /**
     * Calculate zero crossing rate
     */
    private static double[] calculateZeroCrossings(double[] data) {
        List<Double> crossings = new ArrayList<>();
        
        for (int i = 1; i < data.length; i++) {
            if ((data[i-1] >= 0 && data[i] < 0) || (data[i-1] < 0 && data[i] >= 0)) {
                crossings.add((double) i);
            }
        }
        
        return crossings.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    /**
     * Calculate spectral centroid
     */
    private static double calculateSpectralCentroid(double[] spectrum, double sampleRate) {
        double sum = 0, weightedSum = 0;
        for (int i = 0; i < spectrum.length; i++) {
            double frequency = i * sampleRate / (2 * spectrum.length);
            sum += spectrum[i];
            weightedSum += frequency * spectrum[i];
        }
        return sum > 0 ? weightedSum / sum : 0;
    }
    
    /**
     * Calculate spectral rolloff
     */
    private static double calculateSpectralRolloff(double[] spectrum, double sampleRate, double percentile) {
        double total = 0;
        for (double value : spectrum) {
            total += value;
        }
        
        double threshold = total * percentile;
        double cumulative = 0;
        
        for (int i = 0; i < spectrum.length; i++) {
            cumulative += spectrum[i];
            if (cumulative >= threshold) {
                return i * sampleRate / (2 * spectrum.length);
            }
        }
        
        return 0;
    }
    
    /**
     * Calculate spectral flux
     */
    private static double calculateSpectralFlux(double[] spectrum) {
        double flux = 0;
        for (int i = 1; i < spectrum.length; i++) {
            double diff = spectrum[i] - spectrum[i-1];
            flux += diff > 0 ? diff : 0; // Only positive changes
        }
        return flux;
    }
    
    /**
     * Calculate MFCC (simplified)
     */
    private static double[] calculateMFCC(double[] spectrum, int numCoefficients) {
        double[] mfcc = new double[numCoefficients];
        
        // Simplified DCT-like calculation
        for (int i = 0; i < numCoefficients; i++) {
            double sum = 0;
            for (int j = 0; j < Math.min(spectrum.length, 100); j++) {
                sum += spectrum[j] * Math.cos(Math.PI * i * (j + 0.5) / 100);
            }
            mfcc[i] = sum;
        }
        
        return mfcc;
    }
    
    /**
     * Detect beats in audio
     */
    private static BeatInfo[] detectBeats(double[] data, double sampleRate) {
        List<BeatInfo> beats = new ArrayList<>();
        double threshold = calculateAverageAmplitude(data) * 3.0;
        int minDistance = (int) (sampleRate * 0.1); // Minimum 100ms between beats
        
        for (int i = minDistance; i < data.length - minDistance; i++) {
            if (Math.abs(data[i]) > threshold) {
                // Check if this is a local maximum
                boolean isPeak = true;
                for (int j = Math.max(0, i - minDistance); j < Math.min(data.length, i + minDistance); j++) {
                    if (Math.abs(data[j]) > Math.abs(data[i])) {
                        isPeak = false;
                        break;
                    }
                }
                
                if (isPeak) {
                    double timestamp = i / sampleRate;
                    double strength = Math.abs(data[i]);
                    double frequency = 0; // Would need more complex analysis for frequency
                    beats.add(new BeatInfo(timestamp, strength, frequency));
                }
            }
        }
        
        return beats.toArray(new BeatInfo[0]);
    }
    
    /**
     * Calculate tempo from beat information
     */
    private static double calculateTempo(BeatInfo[] beats) {
        if (beats.length < 2) {
            return 0;
        }
        
        // Calculate average time between beats
        double sum = 0;
        for (int i = 1; i < beats.length; i++) {
            sum += beats[i].timestamp - beats[i-1].timestamp;
        }
        
        double avgInterval = sum / (beats.length - 1);
        return 60.0 / avgInterval; // Convert to BPM
    }
    
    /**
     * Calculate spectrogram
     */
    private static double[][] calculateSpectrogram(double[] data, double sampleRate, int windowSize, int hopSize) {
        int numFrames = (data.length - windowSize) / hopSize + 1;
        double[][] spectrogram = new double[numFrames][windowSize / 2];
        
        for (int frame = 0; frame < numFrames; frame++) {
            int start = frame * hopSize;
            double[] window = Arrays.copyOfRange(data, start, Math.min(start + windowSize, data.length));
            
            // Pad with zeros if necessary
            if (window.length < windowSize) {
                window = Arrays.copyOf(window, windowSize);
            }
            
            // Apply window function
            for (int i = 0; i < window.length; i++) {
                double hann = 0.5 * (1 - Math.cos(2 * Math.PI * i / (window.length - 1)));
                window[i] *= hann;
            }
            
            // Calculate magnitude spectrum (simplified)
            for (int i = 0; i < windowSize / 2; i++) {
                spectrogram[frame][i] = Math.abs(window[i]); // Simplified
            }
        }
        
        return spectrogram;
    }
    
    /**
     * Find next power of 2
     */
    private static int nextPowerOf2(int n) {
        if (n <= 0) return 1;
        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return n + 1;
    }
}