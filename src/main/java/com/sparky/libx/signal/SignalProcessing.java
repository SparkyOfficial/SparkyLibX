package com.sparky.libx.signal;

import com.sparky.libx.math.Vector3D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced Signal Processing Framework for Minecraft Plugins
 * Provides capabilities for digital signal processing, filtering, spectral analysis, and audio processing
 * 
 * @author Андрій Будильников
 */
public class SignalProcessing {
    
    /**
     * Represents a digital signal with time-domain samples
     */
    public static class Signal {
        private double[] samples;
        private double sampleRate;
        private double startTime;
        
        public Signal(double[] samples, double sampleRate) {
            this(samples, sampleRate, 0);
        }
        
        public Signal(double[] samples, double sampleRate, double startTime) {
            this.samples = Arrays.copyOf(samples, samples.length);
            this.sampleRate = sampleRate;
            this.startTime = startTime;
        }
        
        /**
         * Gets the number of samples in the signal
         */
        public int getLength() {
            return samples.length;
        }
        
        /**
         * Gets a sample at the specified index
         */
        public double getSample(int index) {
            if (index < 0 || index >= samples.length) {
                throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for length " + samples.length);
            }
            return samples[index];
        }
        
        /**
         * Sets a sample at the specified index
         */
        public void setSample(int index, double value) {
            if (index < 0 || index >= samples.length) {
                throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for length " + samples.length);
            }
            samples[index] = value;
        }
        
        /**
         * Gets all samples as an array
         */
        public double[] getSamples() {
            return Arrays.copyOf(samples, samples.length);
        }
        
        /**
         * Gets the sample rate
         */
        public double getSampleRate() {
            return sampleRate;
        }
        
        /**
         * Gets the start time
         */
        public double getStartTime() {
            return startTime;
        }
        
        /**
         * Gets the duration of the signal in seconds
         */
        public double getDuration() {
            return samples.length / sampleRate;
        }
        
        /**
         * Gets the time of a sample at the specified index
         */
        public double getTimeAtSample(int index) {
            return startTime + index / sampleRate;
        }
        
        /**
         * Gets the index of a sample at the specified time
         */
        public int getSampleAtTime(double time) {
            return (int) Math.round((time - startTime) * sampleRate);
        }
        
        /**
         * Creates a copy of this signal
         */
        public Signal copy() {
            return new Signal(samples, sampleRate, startTime);
        }
        
        /**
         * Adds another signal to this signal (element-wise)
         */
        public Signal add(Signal other) {
            if (this.sampleRate != other.sampleRate) {
                throw new IllegalArgumentException("Sample rates must match");
            }
            
            int maxLength = Math.max(this.samples.length, other.samples.length);
            double[] result = new double[maxLength];
            
            for (int i = 0; i < maxLength; i++) {
                double thisValue = (i < this.samples.length) ? this.samples[i] : 0;
                double otherValue = (i < other.samples.length) ? other.samples[i] : 0;
                result[i] = thisValue + otherValue;
            }
            
            return new Signal(result, sampleRate, Math.min(this.startTime, other.startTime));
        }
        
        /**
         * Multiplies this signal by another signal (element-wise)
         */
        public Signal multiply(Signal other) {
            if (this.sampleRate != other.sampleRate) {
                throw new IllegalArgumentException("Sample rates must match");
            }
            
            int minLength = Math.min(this.samples.length, other.samples.length);
            double[] result = new double[minLength];
            
            for (int i = 0; i < minLength; i++) {
                result[i] = this.samples[i] * other.samples[i];
            }
            
            return new Signal(result, sampleRate, Math.max(this.startTime, other.startTime));
        }
        
        /**
         * Scales this signal by a constant factor
         */
        public Signal scale(double factor) {
            double[] result = new double[samples.length];
            for (int i = 0; i < samples.length; i++) {
                result[i] = samples[i] * factor;
            }
            return new Signal(result, sampleRate, startTime);
        }
        
        /**
         * Shifts this signal in time
         */
        public Signal shift(double timeShift) {
            return new Signal(samples, sampleRate, startTime + timeShift);
        }
        
        /**
         * Reverses this signal in time
         */
        public Signal reverse() {
            double[] result = new double[samples.length];
            for (int i = 0; i < samples.length; i++) {
                result[i] = samples[samples.length - 1 - i];
            }
            return new Signal(result, sampleRate, startTime);
        }
        
        /**
         * Gets the energy of the signal
         */
        public double getEnergy() {
            double energy = 0;
            for (double sample : samples) {
                energy += sample * sample;
            }
            return energy;
        }
        
        /**
         * Gets the power of the signal
         */
        public double getPower() {
            return getEnergy() / samples.length;
        }
        
        /**
         * Gets the root mean square (RMS) of the signal
         */
        public double getRMS() {
            return Math.sqrt(getPower());
        }
        
        /**
         * Gets the maximum absolute value in the signal
         */
        public double getMaxAmplitude() {
            double max = 0;
            for (double sample : samples) {
                max = Math.max(max, Math.abs(sample));
            }
            return max;
        }
        
        /**
         * Normalizes the signal to have a maximum amplitude of 1
         */
        public Signal normalize() {
            double maxAmplitude = getMaxAmplitude();
            if (maxAmplitude == 0) {
                return copy();
            }
            return scale(1.0 / maxAmplitude);
        }
        
        /**
         * Applies a window function to the signal
         */
        public Signal applyWindow(WindowFunction window) {
            double[] result = new double[samples.length];
            for (int i = 0; i < samples.length; i++) {
                result[i] = samples[i] * window.getValue(i, samples.length);
            }
            return new Signal(result, sampleRate, startTime);
        }
        
        /**
         * Resamples the signal to a new sample rate
         */
        public Signal resample(double newSampleRate) {
            if (newSampleRate == sampleRate) {
                return copy();
            }
            
            int newLength = (int) Math.round(samples.length * newSampleRate / sampleRate);
            double[] result = new double[newLength];
            
            for (int i = 0; i < newLength; i++) {
                double time = startTime + i / newSampleRate;
                int oldIndex = getSampleAtTime(time);
                
                if (oldIndex >= 0 && oldIndex < samples.length) {
                    result[i] = samples[oldIndex];
                } else {
                    result[i] = 0;
                }
            }
            
            return new Signal(result, newSampleRate, startTime);
        }
        
        /**
         * Extracts a segment of the signal
         */
        public Signal segment(double startTime, double endTime) {
            int startSample = Math.max(0, getSampleAtTime(startTime));
            int endSample = Math.min(samples.length, getSampleAtTime(endTime));
            
            if (startSample >= endSample) {
                return new Signal(new double[0], sampleRate, startTime);
            }
            
            double[] segment = Arrays.copyOfRange(samples, startSample, endSample);
            return new Signal(segment, sampleRate, startTime);
        }
    }
    
    /**
     * Represents a window function for signal processing
     */
    public abstract static class WindowFunction {
        /**
         * Gets the value of the window function at the specified index
         */
        public abstract double getValue(int index, int length);
    }
    
    /**
     * Represents a rectangular window (no windowing)
     */
    public static class RectangularWindow extends WindowFunction {
        @Override
        public double getValue(int index, int length) {
            return 1.0;
        }
    }
    
    /**
     * Represents a Hamming window
     */
    public static class HammingWindow extends WindowFunction {
        @Override
        public double getValue(int index, int length) {
            return 0.54 - 0.46 * Math.cos(2 * Math.PI * index / (length - 1));
        }
    }
    
    /**
     * Represents a Hanning window
     */
    public static class HanningWindow extends WindowFunction {
        @Override
        public double getValue(int index, int length) {
            return 0.5 * (1 - Math.cos(2 * Math.PI * index / (length - 1)));
        }
    }
    
    /**
     * Represents a Blackman window
     */
    public static class BlackmanWindow extends WindowFunction {
        @Override
        public double getValue(int index, int length) {
            return 0.42 - 0.5 * Math.cos(2 * Math.PI * index / (length - 1)) + 
                   0.08 * Math.cos(4 * Math.PI * index / (length - 1));
        }
    }
    
    /**
     * Represents a Kaiser window
     */
    public static class KaiserWindow extends WindowFunction {
        private double beta;
        
        public KaiserWindow(double beta) {
            this.beta = beta;
        }
        
        @Override
        public double getValue(int index, int length) {
            double alpha = (length - 1) / 2.0;
            double x = (index - alpha) / alpha;
            return besselI0(beta * Math.sqrt(1 - x * x)) / besselI0(beta);
        }
        
        /**
         * Calculates the modified Bessel function of the first kind, order 0
         */
        private double besselI0(double x) {
            double ax = Math.abs(x);
            if (ax < 3.75) {
                double y = x / 3.75;
                y *= y;
                return 1.0 + y * (3.5156229 + y * (3.0899424 + y * (1.2067492 + 
                       y * (0.2659732 + y * (0.360768e-1 + y * 0.45813e-2)))));
            } else {
                double y = 3.75 / ax;
                return (Math.exp(ax) / Math.sqrt(ax)) * 
                       (0.39894228 + y * (0.1328592e-1 + y * (0.225319e-2 + 
                       y * (-0.157565e-2 + y * (0.916281e-2 + y * (-0.2057706e-1 + 
                       y * (0.2635537e-1 + y * (-0.1647633e-1 + y * 0.392377e-2))))))));
            }
        }
    }
    
    /**
     * Represents a digital filter
     */
    public abstract static class Filter {
        /**
         * Applies the filter to a signal
         */
        public abstract Signal apply(Signal input);
    }
    
    /**
     * Represents a finite impulse response (FIR) filter
     */
    public static class FIRFilter extends Filter {
        private double[] coefficients;
        
        public FIRFilter(double[] coefficients) {
            this.coefficients = Arrays.copyOf(coefficients, coefficients.length);
        }
        
        @Override
        public Signal apply(Signal input) {
            int inputLength = input.getLength();
            int filterLength = coefficients.length;
            int outputLength = inputLength + filterLength - 1;
            double[] output = new double[outputLength];
            
            // Apply convolution
            for (int n = 0; n < outputLength; n++) {
                for (int k = 0; k < filterLength; k++) {
                    int inputIndex = n - k;
                    if (inputIndex >= 0 && inputIndex < inputLength) {
                        output[n] += coefficients[k] * input.getSample(inputIndex);
                    }
                }
            }
            
            return new Signal(output, input.getSampleRate(), input.getStartTime());
        }
        
        /**
         * Gets the filter coefficients
         */
        public double[] getCoefficients() {
            return Arrays.copyOf(coefficients, coefficients.length);
        }
    }
    
    /**
     * Represents an infinite impulse response (IIR) filter
     */
    public static class IIRFilter extends Filter {
        private double[] feedforwardCoefficients;
        private double[] feedbackCoefficients;
        
        public IIRFilter(double[] feedforwardCoefficients, double[] feedbackCoefficients) {
            this.feedforwardCoefficients = Arrays.copyOf(feedforwardCoefficients, feedforwardCoefficients.length);
            this.feedbackCoefficients = Arrays.copyOf(feedbackCoefficients, feedbackCoefficients.length);
        }
        
        @Override
        public Signal apply(Signal input) {
            int inputLength = input.getLength();
            int feedforwardLength = feedforwardCoefficients.length;
            int feedbackLength = feedbackCoefficients.length;
            double[] output = new double[inputLength];
            
            // Apply difference equation
            for (int n = 0; n < inputLength; n++) {
                // Feedforward part
                for (int k = 0; k < feedforwardLength; k++) {
                    int inputIndex = n - k;
                    if (inputIndex >= 0) {
                        output[n] += feedforwardCoefficients[k] * input.getSample(inputIndex);
                    }
                }
                
                // Feedback part
                for (int k = 1; k < feedbackLength; k++) {
                    int outputIndex = n - k;
                    if (outputIndex >= 0) {
                        output[n] -= feedbackCoefficients[k] * output[outputIndex];
                    }
                }
                
                // Normalize by a0 coefficient (assuming a0 = 1)
                if (feedbackCoefficients.length > 0 && feedbackCoefficients[0] != 0) {
                    output[n] /= feedbackCoefficients[0];
                }
            }
            
            return new Signal(output, input.getSampleRate(), input.getStartTime());
        }
        
        /**
         * Gets the feedforward coefficients
         */
        public double[] getFeedforwardCoefficients() {
            return Arrays.copyOf(feedforwardCoefficients, feedforwardCoefficients.length);
        }
        
        /**
         * Gets the feedback coefficients
         */
        public double[] getFeedbackCoefficients() {
            return Arrays.copyOf(feedbackCoefficients, feedbackCoefficients.length);
        }
    }
    
    /**
     * Represents a filter design utility
     */
    public static class FilterDesign {
        /**
         * Designs a low-pass FIR filter using the window method
         */
        public static FIRFilter designLowPassFIR(int filterLength, double cutoffFrequency, double sampleRate, WindowFunction window) {
            if (filterLength <= 0 || filterLength % 2 == 0) {
                throw new IllegalArgumentException("Filter length must be positive and odd");
            }
            
            double[] coefficients = new double[filterLength];
            int center = filterLength / 2;
            double normalizedCutoff = cutoffFrequency / sampleRate;
            
            // Generate sinc function
            for (int n = 0; n < filterLength; n++) {
                if (n == center) {
                    coefficients[n] = 2 * Math.PI * normalizedCutoff;
                } else {
                    double x = Math.PI * (n - center);
                    coefficients[n] = Math.sin(2 * Math.PI * normalizedCutoff * (n - center)) / x;
                }
            }
            
            // Apply window
            for (int n = 0; n < filterLength; n++) {
                coefficients[n] *= window.getValue(n, filterLength);
            }
            
            // Normalize
            double sum = 0;
            for (double coeff : coefficients) {
                sum += coeff;
            }
            for (int n = 0; n < filterLength; n++) {
                coefficients[n] /= sum;
            }
            
            return new FIRFilter(coefficients);
        }
        
        /**
         * Designs a high-pass FIR filter using the window method
         */
        public static FIRFilter designHighPassFIR(int filterLength, double cutoffFrequency, double sampleRate, WindowFunction window) {
            if (filterLength <= 0 || filterLength % 2 == 0) {
                throw new IllegalArgumentException("Filter length must be positive and odd");
            }
            
            double[] coefficients = new double[filterLength];
            int center = filterLength / 2;
            double normalizedCutoff = cutoffFrequency / sampleRate;
            
            // Generate sinc function for high-pass
            for (int n = 0; n < filterLength; n++) {
                if (n == center) {
                    coefficients[n] = 1 - 2 * Math.PI * normalizedCutoff;
                } else {
                    double x = Math.PI * (n - center);
                    coefficients[n] = -Math.sin(2 * Math.PI * normalizedCutoff * (n - center)) / x;
                }
            }
            
            // Apply window
            for (int n = 0; n < filterLength; n++) {
                coefficients[n] *= window.getValue(n, filterLength);
            }
            
            // Normalize
            double sum = 0;
            for (double coeff : coefficients) {
                sum += coeff;
            }
            for (int n = 0; n < filterLength; n++) {
                coefficients[n] /= sum;
            }
            
            return new FIRFilter(coefficients);
        }
        
        /**
         * Designs a band-pass FIR filter using the window method
         */
        public static FIRFilter designBandPassFIR(int filterLength, double lowCutoff, double highCutoff, double sampleRate, WindowFunction window) {
            if (filterLength <= 0 || filterLength % 2 == 0) {
                throw new IllegalArgumentException("Filter length must be positive and odd");
            }
            
            double[] coefficients = new double[filterLength];
            int center = filterLength / 2;
            double normalizedLowCutoff = lowCutoff / sampleRate;
            double normalizedHighCutoff = highCutoff / sampleRate;
            
            // Generate sinc function for band-pass
            for (int n = 0; n < filterLength; n++) {
                if (n == center) {
                    coefficients[n] = 2 * Math.PI * (normalizedHighCutoff - normalizedLowCutoff);
                } else {
                    double x = Math.PI * (n - center);
                    double lowSinc = Math.sin(2 * Math.PI * normalizedLowCutoff * (n - center)) / x;
                    double highSinc = Math.sin(2 * Math.PI * normalizedHighCutoff * (n - center)) / x;
                    coefficients[n] = highSinc - lowSinc;
                }
            }
            
            // Apply window
            for (int n = 0; n < filterLength; n++) {
                coefficients[n] *= window.getValue(n, filterLength);
            }
            
            // Normalize
            double sum = 0;
            for (double coeff : coefficients) {
                sum += coeff;
            }
            for (int n = 0; n < filterLength; n++) {
                coefficients[n] /= sum;
            }
            
            return new FIRFilter(coefficients);
        }
    }
    
    /**
     * Represents a Fourier transform utility
     */
    public static class FourierTransform {
        /**
         * Performs a discrete Fourier transform (DFT) on a signal
         */
        public static Complex[] dft(Signal signal) {
            int N = signal.getLength();
            Complex[] result = new Complex[N];
            
            for (int k = 0; k < N; k++) {
                Complex sum = new Complex(0, 0);
                for (int n = 0; n < N; n++) {
                    double angle = -2 * Math.PI * k * n / N;
                    Complex exponential = new Complex(Math.cos(angle), Math.sin(angle));
                    sum = sum.add(exponential.multiply(signal.getSample(n)));
                }
                result[k] = sum;
            }
            
            return result;
        }
        
        /**
         * Performs an inverse discrete Fourier transform (IDFT) on complex data
         */
        public static Signal idft(Complex[] spectrum, double sampleRate) {
            int N = spectrum.length;
            double[] result = new double[N];
            
            for (int n = 0; n < N; n++) {
                Complex sum = new Complex(0, 0);
                for (int k = 0; k < N; k++) {
                    double angle = 2 * Math.PI * k * n / N;
                    Complex exponential = new Complex(Math.cos(angle), Math.sin(angle));
                    sum = sum.add(spectrum[k].multiply(exponential));
                }
                result[n] = sum.getReal() / N;
            }
            
            return new Signal(result, sampleRate);
        }
        
        /**
         * Performs a fast Fourier transform (FFT) on a signal
         * Note: This implementation requires the signal length to be a power of 2
         */
        public static Complex[] fft(Signal signal) {
            int N = signal.getLength();
            
            // Check if N is a power of 2
            if ((N & (N - 1)) != 0) {
                throw new IllegalArgumentException("Signal length must be a power of 2 for FFT");
            }
            
            // Convert to complex array
            Complex[] x = new Complex[N];
            for (int i = 0; i < N; i++) {
                x[i] = new Complex(signal.getSample(i), 0);
            }
            
            // Bit-reversal permutation
            int j = 0;
            for (int i = 0; i < N; i++) {
                if (i < j) {
                    Complex temp = x[i];
                    x[i] = x[j];
                    x[j] = temp;
                }
                int k = N >> 1;
                while (k <= j) {
                    j -= k;
                    k >>= 1;
                }
                j += k;
            }
            
            // Cooley-Tukey FFT
            for (int len = 2; len <= N; len <<= 1) {
                double angle = -2 * Math.PI / len;
                Complex wlen = new Complex(Math.cos(angle), Math.sin(angle));
                
                for (int i = 0; i < N; i += len) {
                    Complex w = new Complex(1, 0);
                    for (int idx = 0; idx < len / 2; idx++) {
                        Complex u = x[i + idx];
                        Complex v = x[i + idx + len / 2].multiply(w);
                        x[i + idx] = u.add(v);
                        x[i + idx + len / 2] = u.subtract(v);
                        w = w.multiply(wlen);
                    }
                }
            }
            
            return x;
        }
        
        /**
         * Performs an inverse fast Fourier transform (IFFT) on complex data
         * Note: This implementation requires the data length to be a power of 2
         */
        public static Signal ifft(Complex[] spectrum, double sampleRate) {
            int N = spectrum.length;
            
            // Check if N is a power of 2
            if ((N & (N - 1)) != 0) {
                throw new IllegalArgumentException("Spectrum length must be a power of 2 for IFFT");
            }
            
            // Conjugate the spectrum
            Complex[] conjugated = new Complex[N];
            for (int i = 0; i < N; i++) {
                conjugated[i] = spectrum[i].conjugate();
            }
            
            // Apply FFT
            Complex[] result = fft(new Signal(new double[N], sampleRate), conjugated);
            
            // Conjugate and normalize
            double[] real = new double[N];
            for (int i = 0; i < N; i++) {
                real[i] = result[i].conjugate().getReal() / N;
            }
            
            return new Signal(real, sampleRate);
        }
        
        /**
         * Helper method for FFT with pre-converted complex array
         */
        private static Complex[] fft(Signal signal, Complex[] x) {
            int N = x.length;
            
            // Base case
            if (N == 1) {
                return new Complex[]{x[0]};
            }
            
            // Divide
            Complex[] even = new Complex[N / 2];
            Complex[] odd = new Complex[N / 2];
            for (int i = 0; i < N / 2; i++) {
                even[i] = x[2 * i];
                odd[i] = x[2 * i + 1];
            }
            
            // Conquer
            Complex[] evenFFT = fft(signal, even);
            Complex[] oddFFT = fft(signal, odd);
            
            // Combine
            Complex[] result = new Complex[N];
            for (int i = 0; i < N / 2; i++) {
                double angle = -2 * Math.PI * i / N;
                Complex w = new Complex(Math.cos(angle), Math.sin(angle));
                Complex wOdd = w.multiply(oddFFT[i]);
                result[i] = evenFFT[i].add(wOdd);
                result[i + N / 2] = evenFFT[i].subtract(wOdd);
            }
            
            return result;
        }
        
        /**
         * Computes the power spectral density of a signal
         */
        public static double[] powerSpectralDensity(Signal signal) {
            Complex[] spectrum = fft(signal);
            double[] psd = new double[spectrum.length];
            
            for (int i = 0; i < spectrum.length; i++) {
                psd[i] = spectrum[i].magnitudeSquared();
            }
            
            return psd;
        }
        
        /**
         * Computes the frequency bins for a spectrum
         */
        public static double[] frequencyBins(int spectrumLength, double sampleRate) {
            double[] bins = new double[spectrumLength];
            double frequencyStep = sampleRate / spectrumLength;
            
            for (int i = 0; i < spectrumLength; i++) {
                bins[i] = i * frequencyStep;
            }
            
            return bins;
        }
    }
    
    /**
     * Represents a complex number for signal processing
     */
    public static class Complex {
        private final double real;
        private final double imaginary;
        
        public Complex(double real, double imaginary) {
            this.real = real;
            this.imaginary = imaginary;
        }
        
        public double getReal() {
            return real;
        }
        
        public double getImaginary() {
            return imaginary;
        }
        
        public Complex add(Complex other) {
            return new Complex(real + other.real, imaginary + other.imaginary);
        }
        
        public Complex subtract(Complex other) {
            return new Complex(real - other.real, imaginary - other.imaginary);
        }
        
        public Complex multiply(Complex other) {
            return new Complex(
                real * other.real - imaginary * other.imaginary,
                real * other.imaginary + imaginary * other.real
            );
        }
        
        public Complex multiply(double scalar) {
            return new Complex(real * scalar, imaginary * scalar);
        }
        
        public Complex divide(Complex other) {
            double denominator = other.real * other.real + other.imaginary * other.imaginary;
            if (denominator == 0) {
                throw new ArithmeticException("Division by zero");
            }
            return new Complex(
                (real * other.real + imaginary * other.imaginary) / denominator,
                (imaginary * other.real - real * other.imaginary) / denominator
            );
        }
        
        public Complex conjugate() {
            return new Complex(real, -imaginary);
        }
        
        public double magnitude() {
            return Math.sqrt(real * real + imaginary * imaginary);
        }
        
        public double magnitudeSquared() {
            return real * real + imaginary * imaginary;
        }
        
        public double phase() {
            return Math.atan2(imaginary, real);
        }
        
        @Override
        public String toString() {
            if (imaginary >= 0) {
                return String.format("%.3f + %.3fi", real, imaginary);
            } else {
                return String.format("%.3f - %.3fi", real, -imaginary);
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            Complex complex = (Complex) obj;
            return Double.compare(complex.real, real) == 0 &&
                   Double.compare(complex.imaginary, imaginary) == 0;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(real, imaginary);
        }
    }
    
    /**
     * Represents a signal generator for creating test signals
     */
    public static class SignalGenerator {
        /**
         * Generates a sine wave signal
         */
        public static Signal generateSineWave(double frequency, double amplitude, double duration, double sampleRate) {
            int numSamples = (int) Math.round(duration * sampleRate);
            double[] samples = new double[numSamples];
            
            for (int i = 0; i < numSamples; i++) {
                double time = i / sampleRate;
                samples[i] = amplitude * Math.sin(2 * Math.PI * frequency * time);
            }
            
            return new Signal(samples, sampleRate);
        }
        
        /**
         * Generates a cosine wave signal
         */
        public static Signal generateCosineWave(double frequency, double amplitude, double duration, double sampleRate) {
            int numSamples = (int) Math.round(duration * sampleRate);
            double[] samples = new double[numSamples];
            
            for (int i = 0; i < numSamples; i++) {
                double time = i / sampleRate;
                samples[i] = amplitude * Math.cos(2 * Math.PI * frequency * time);
            }
            
            return new Signal(samples, sampleRate);
        }
        
        /**
         * Generates a square wave signal
         */
        public static Signal generateSquareWave(double frequency, double amplitude, double duration, double sampleRate) {
            int numSamples = (int) Math.round(duration * sampleRate);
            double[] samples = new double[numSamples];
            
            for (int i = 0; i < numSamples; i++) {
                double time = i / sampleRate;
                double period = 1.0 / frequency;
                double phase = time % period;
                samples[i] = (phase < period / 2) ? amplitude : -amplitude;
            }
            
            return new Signal(samples, sampleRate);
        }
        
        /**
         * Generates a sawtooth wave signal
         */
        public static Signal generateSawtoothWave(double frequency, double amplitude, double duration, double sampleRate) {
            int numSamples = (int) Math.round(duration * sampleRate);
            double[] samples = new double[numSamples];
            
            for (int i = 0; i < numSamples; i++) {
                double time = i / sampleRate;
                double period = 1.0 / frequency;
                double phase = time % period;
                samples[i] = 2 * amplitude * (phase / period) - amplitude;
            }
            
            return new Signal(samples, sampleRate);
        }
        
        /**
         * Generates a triangle wave signal
         */
        public static Signal generateTriangleWave(double frequency, double amplitude, double duration, double sampleRate) {
            int numSamples = (int) Math.round(duration * sampleRate);
            double[] samples = new double[numSamples];
            
            for (int i = 0; i < numSamples; i++) {
                double time = i / sampleRate;
                double period = 1.0 / frequency;
                double phase = time % period;
                if (phase < period / 2) {
                    samples[i] = 4 * amplitude * (phase / period) - amplitude;
                } else {
                    samples[i] = 3 * amplitude - 4 * amplitude * (phase / period);
                }
            }
            
            return new Signal(samples, sampleRate);
        }
        
        /**
         * Generates white noise signal
         */
        public static Signal generateWhiteNoise(double amplitude, double duration, double sampleRate) {
            int numSamples = (int) Math.round(duration * sampleRate);
            double[] samples = new double[numSamples];
            Random random = new Random();
            
            for (int i = 0; i < numSamples; i++) {
                samples[i] = amplitude * (2 * random.nextDouble() - 1);
            }
            
            return new Signal(samples, sampleRate);
        }
        
        /**
         * Generates a chirp signal (frequency sweeps linearly)
         */
        public static Signal generateChirp(double startFrequency, double endFrequency, double amplitude, double duration, double sampleRate) {
            int numSamples = (int) Math.round(duration * sampleRate);
            double[] samples = new double[numSamples];
            
            for (int i = 0; i < numSamples; i++) {
                double time = i / sampleRate;
                double frequency = startFrequency + (endFrequency - startFrequency) * time / duration;
                samples[i] = amplitude * Math.sin(2 * Math.PI * frequency * time);
            }
            
            return new Signal(samples, sampleRate);
        }
        
        /**
         * Generates an impulse signal
         */
        public static Signal generateImpulse(double amplitude, int delaySamples, int totalSamples, double sampleRate) {
            double[] samples = new double[totalSamples];
            if (delaySamples >= 0 && delaySamples < totalSamples) {
                samples[delaySamples] = amplitude;
            }
            return new Signal(samples, sampleRate);
        }
        
        /**
         * Generates a step signal
         */
        public static Signal generateStep(double amplitude, int delaySamples, int totalSamples, double sampleRate) {
            double[] samples = new double[totalSamples];
            for (int i = delaySamples; i < totalSamples; i++) {
                samples[i] = amplitude;
            }
            return new Signal(samples, sampleRate);
        }
    }
}