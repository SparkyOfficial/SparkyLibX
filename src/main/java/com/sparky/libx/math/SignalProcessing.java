package com.sparky.libx.math;

import java.util.Arrays;

/**
 * утиліти для обробки сигналів
 * включає перетворення фур'є, фільтрацію, кореляцію і спектральний аналіз
 * @author Андрій Будильников
 */
public class SignalProcessing {
    
    /**
     * комплексне число для представлення результатів перетворення фур'є
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
        
        public double magnitude() {
            return Math.sqrt(real * real + imaginary * imaginary);
        }
        
        public double phase() {
            return Math.atan2(imaginary, real);
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
        
        public Complex conjugate() {
            return new Complex(real, -imaginary);
        }
        
        @Override
        public String toString() {
            if (imaginary >= 0) {
                return String.format("%.3f+%.3fi", real, imaginary);
            } else {
                return String.format("%.3f%.3fi", real, imaginary);
            }
        }
    }
    
    /**
     * обчислити дискретне перетворення фур'є (DFT)
     * @param signal вхідний сигнал
     * @return масив комплексних чисел - результат DFT
     */
    public static Complex[] discreteFourierTransform(double[] signal) {
        if (signal == null || signal.length == 0) {
            throw new IllegalArgumentException("Сигнал не може бути порожнім");
        }
        
        int n = signal.length;
        Complex[] result = new Complex[n];
        
        for (int k = 0; k < n; k++) {
            double real = 0;
            double imaginary = 0;
            
            for (int t = 0; t < n; t++) {
                double angle = -2 * Math.PI * t * k / n;
                real += signal[t] * Math.cos(angle);
                imaginary += signal[t] * Math.sin(angle);
            }
            
            result[k] = new Complex(real, imaginary);
        }
        
        return result;
    }
    
    /**
     * обчислити обернене дискретне перетворення фур'є (IDFT)
     * @param spectrum спектр сигналу
     * @return масив дійсних чисел - відновлений сигнал
     */
    public static double[] inverseDiscreteFourierTransform(Complex[] spectrum) {
        if (spectrum == null || spectrum.length == 0) {
            throw new IllegalArgumentException("Спектр не може бути порожнім");
        }
        
        int n = spectrum.length;
        double[] result = new double[n];
        
        for (int t = 0; t < n; t++) {
            Complex sum = new Complex(0, 0);
            
            for (int k = 0; k < n; k++) {
                double angle = 2 * Math.PI * t * k / n;
                Complex exponential = new Complex(Math.cos(angle), Math.sin(angle));
                sum = sum.add(spectrum[k].multiply(exponential));
            }
            
            result[t] = sum.getReal() / n;
        }
        
        return result;
    }
    
    /**
     * обчислити швидке перетворення фур'є (FFT) методом Коулі-Тьюкі
     * @param signal вхідний сигнал (довжина повинна бути степенем 2)
     * @return масив комплексних чисел - результат FFT
     */
    public static Complex[] fastFourierTransform(double[] signal) {
        if (signal == null || signal.length == 0) {
            throw new IllegalArgumentException("Сигнал не може бути порожнім");
        }
        
        // перевірити чи довжина є степенем 2
        if (!isPowerOfTwo(signal.length)) {
            throw new IllegalArgumentException("Довжина сигналу повинна бути степенем 2");
        }
        
        int n = signal.length;
        
        // базовий випадок
        if (n == 1) {
            return new Complex[] { new Complex(signal[0], 0) };
        }
        
        // розділити на парні і непарні елементи
        double[] even = new double[n/2];
        double[] odd = new double[n/2];
        
        for (int i = 0; i < n/2; i++) {
            even[i] = signal[2*i];
            odd[i] = signal[2*i + 1];
        }
        
        // рекурсивно обчислити FFT для парних і непарних частин
        Complex[] evenFFT = fastFourierTransform(even);
        Complex[] oddFFT = fastFourierTransform(odd);
        
        // комбінувати результати
        Complex[] result = new Complex[n];
        
        for (int i = 0; i < n/2; i++) {
            double angle = -2 * Math.PI * i / n;
            Complex w = new Complex(Math.cos(angle), Math.sin(angle));
            Complex oddTerm = w.multiply(oddFFT[i]);
            
            result[i] = evenFFT[i].add(oddTerm);
            result[i + n/2] = evenFFT[i].subtract(oddTerm);
        }
        
        return result;
    }
    
    /**
     * обчислити обернене швидке перетворення фур'є (IFFT)
     * @param spectrum спектр сигналу
     * @return масив дійсних чисел - відновлений сигнал
     */
    public static double[] inverseFastFourierTransform(Complex[] spectrum) {
        if (spectrum == null || spectrum.length == 0) {
            throw new IllegalArgumentException("Спектр не може бути порожнім");
        }
        
        // перевірити чи довжина є степенем 2
        if (!isPowerOfTwo(spectrum.length)) {
            throw new IllegalArgumentException("Довжина спектру повинна бути степенем 2");
        }
        
        int n = spectrum.length;
        
        // взяти комплексне спряження
        Complex[] conjugate = new Complex[n];
        for (int i = 0; i < n; i++) {
            conjugate[i] = spectrum[i].conjugate();
        }
        
        // обчислити FFT від спряженого
        Complex[] fftResult = fastFourierTransform(Arrays.stream(conjugate)
            .mapToDouble(c -> c.getReal())
            .toArray());
        
        // взяти спряження результату і поділити на n
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = fftResult[i].conjugate().getReal() / n;
        }
        
        return result;
    }
    
    /**
     * перевірити чи число є степенем 2
     * @param n число для перевірки
     * @return true якщо число є степенем 2
     */
    private static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    
    /**
     * обчислити згортку двох сигналів
     * @param signal1 перший сигнал
     * @param signal2 другий сигнал
     * @return згортка сигналів
     */
    public static double[] convolution(double[] signal1, double[] signal2) {
        if (signal1 == null || signal2 == null || signal1.length == 0 || signal2.length == 0) {
            throw new IllegalArgumentException("Сигнали не можуть бути порожніми");
        }
        
        int n = signal1.length;
        int m = signal2.length;
        int resultLength = n + m - 1;
        double[] result = new double[resultLength];
        
        for (int i = 0; i < resultLength; i++) {
            for (int j = Math.max(0, i - m + 1); j < Math.min(n, i + 1); j++) {
                result[i] += signal1[j] * signal2[i - j];
            }
        }
        
        return result;
    }
    
    /**
     * обчислити кореляцію двох сигналів
     * @param signal1 перший сигнал
     * @param signal2 другий сигнал
     * @return кореляція сигналів
     */
    public static double[] correlation(double[] signal1, double[] signal2) {
        if (signal1 == null || signal2 == null || signal1.length == 0 || signal2.length == 0) {
            throw new IllegalArgumentException("Сигнали не можуть бути порожніми");
        }
        
        // інвертувати другий сигнал для кореляції
        double[] reversedSignal2 = new double[signal2.length];
        for (int i = 0; i < signal2.length; i++) {
            reversedSignal2[i] = signal2[signal2.length - 1 - i];
        }
        
        return convolution(signal1, reversedSignal2);
    }
    
    /**
     * застосувати низькочастотний фільтр до сигналу
     * @param signal вхідний сигнал
     * @param cutoffFrequency частота зрізу
     * @param sampleRate частота дискретизації
     * @return відфільтрований сигнал
     */
    public static double[] lowPassFilter(double[] signal, double cutoffFrequency, double sampleRate) {
        if (signal == null || signal.length == 0) {
            throw new IllegalArgumentException("Сигнал не може бути порожнім");
        }
        
        if (cutoffFrequency <= 0 || cutoffFrequency >= sampleRate / 2) {
            throw new IllegalArgumentException("Частота зрізу повинна бути між 0 і частотою Найквіста");
        }
        
        // обчислити FFT сигналу
        Complex[] spectrum = fastFourierTransform(padToPowerOfTwo(signal));
        
        // обчислити частоти
        int n = spectrum.length;
        double[] frequencies = new double[n];
        for (int i = 0; i < n; i++) {
            frequencies[i] = (double) i * sampleRate / n;
            if (frequencies[i] > sampleRate / 2) {
                frequencies[i] -= sampleRate;
            }
        }
        
        // застосувати фільтр
        for (int i = 0; i < n; i++) {
            if (Math.abs(frequencies[i]) > cutoffFrequency) {
                spectrum[i] = new Complex(0, 0);
            }
        }
        
        // обчислити IFFT
        double[] filteredSignal = inverseFastFourierTransform(spectrum);
        
        // повернути оригінальну довжину
        return Arrays.copyOf(filteredSignal, signal.length);
    }
    
    /**
     * застосувати високочастотний фільтр до сигналу
     * @param signal вхідний сигнал
     * @param cutoffFrequency частота зрізу
     * @param sampleRate частота дискретизації
     * @return відфільтрований сигнал
     */
    public static double[] highPassFilter(double[] signal, double cutoffFrequency, double sampleRate) {
        if (signal == null || signal.length == 0) {
            throw new IllegalArgumentException("Сигнал не може бути порожнім");
        }
        
        if (cutoffFrequency <= 0 || cutoffFrequency >= sampleRate / 2) {
            throw new IllegalArgumentException("Частота зрізу повинна бути між 0 і частотою Найквіста");
        }
        
        // обчислити FFT сигналу
        Complex[] spectrum = fastFourierTransform(padToPowerOfTwo(signal));
        
        // обчислити частоти
        int n = spectrum.length;
        double[] frequencies = new double[n];
        for (int i = 0; i < n; i++) {
            frequencies[i] = (double) i * sampleRate / n;
            if (frequencies[i] > sampleRate / 2) {
                frequencies[i] -= sampleRate;
            }
        }
        
        // застосувати фільтр
        for (int i = 0; i < n; i++) {
            if (Math.abs(frequencies[i]) < cutoffFrequency) {
                spectrum[i] = new Complex(0, 0);
            }
        }
        
        // обчислити IFFT
        double[] filteredSignal = inverseFastFourierTransform(spectrum);
        
        // повернути оригінальну довжину
        return Arrays.copyOf(filteredSignal, signal.length);
    }
    
    /**
     * доповнити сигнал нулями до найближчої довжини, що є степенем 2
     * @param signal вхідний сигнал
     * @return доповнений сигнал
     */
    private static double[] padToPowerOfTwo(double[] signal) {
        int n = signal.length;
        int powerOfTwo = 1;
        while (powerOfTwo < n) {
            powerOfTwo <<= 1;
        }
        
        if (powerOfTwo == n) {
            return signal;
        }
        
        return Arrays.copyOf(signal, powerOfTwo);
    }
    
    /**
     * обчислити спектральну густину потужності
     * @param signal вхідний сигнал
     * @return масив значень спектральної густини потужності
     */
    public static double[] powerSpectralDensity(double[] signal) {
        if (signal == null || signal.length == 0) {
            throw new IllegalArgumentException("Сигнал не може бути порожнім");
        }
        
        // обчислити FFT
        Complex[] spectrum = fastFourierTransform(padToPowerOfTwo(signal));
        
        // обчислити магнітуду в квадраті
        double[] psd = new double[spectrum.length];
        for (int i = 0; i < spectrum.length; i++) {
            psd[i] = spectrum[i].magnitude() * spectrum[i].magnitude();
        }
        
        return psd;
    }
    
    /**
     * обчислити перетворення вейвлетів (дискретне)
     * @param signal вхідний сигнал
     * @param wavelet вейвлет-функція
     * @param scales масив масштабів
     * @return коефіцієнти вейвлет-перетворення
     */
    public static double[][] discreteWaveletTransform(double[] signal, WaveletFunction wavelet, double[] scales) {
        if (signal == null || signal.length == 0) {
            throw new IllegalArgumentException("Сигнал не може бути порожнім");
        }
        
        if (scales == null || scales.length == 0) {
            throw new IllegalArgumentException("Масив масштабів не може бути порожнім");
        }
        
        int n = signal.length;
        int s = scales.length;
        double[][] coefficients = new double[s][n];
        
        for (int i = 0; i < s; i++) {
            double scale = scales[i];
            for (int t = 0; t < n; t++) {
                coefficients[i][t] = waveletTransformAtPoint(signal, wavelet, scale, t);
            }
        }
        
        return coefficients;
    }
    
    /**
     * обчислити вейвлет-перетворення в конкретній точці
     * @param signal вхідний сигнал
     * @param wavelet вейвлет-функція
     * @param scale масштаб
     * @param translation зсув
     * @return коефіцієнт вейвлет-перетворення
     */
    private static double waveletTransformAtPoint(double[] signal, WaveletFunction wavelet, double scale, int translation) {
        double sum = 0;
        int n = signal.length;
        
        for (int i = 0; i < n; i++) {
            double t = (i - translation) / scale;
            sum += signal[i] * wavelet.apply(t) / Math.sqrt(scale);
        }
        
        return sum;
    }
    
    /**
     * функціональний інтерфейс для вейвлет-функцій
     */
    @FunctionalInterface
    public interface WaveletFunction {
        double apply(double t);
    }
    
    /**
     * вейвлет Хаара
     */
    public static final WaveletFunction HAAR_WAVELET = t -> {
        if (t >= 0 && t < 0.5) {
            return 1;
        } else if (t >= 0.5 && t < 1) {
            return -1;
        } else {
            return 0;
        }
    };
    
    /**
     * обчислити автокореляцію сигналу
     * @param signal вхідний сигнал
     * @return автокореляція
     */
    public static double[] autocorrelation(double[] signal) {
        return correlation(signal, signal);
    }
    
    /**
     * обчислити енергію сигналу
     * @param signal вхідний сигнал
     * @return енергія сигналу
     */
    public static double signalEnergy(double[] signal) {
        if (signal == null || signal.length == 0) {
            throw new IllegalArgumentException("Сигнал не може бути порожнім");
        }
        
        double energy = 0;
        for (double value : signal) {
            energy += value * value;
        }
        
        return energy;
    }
    
    /**
     * обчислити середню потужність сигналу
     * @param signal вхідний сигнал
     * @return середня потужність
     */
    public static double meanPower(double[] signal) {
        if (signal == null || signal.length == 0) {
            throw new IllegalArgumentException("Сигнал не може бути порожнім");
        }
        
        return signalEnergy(signal) / signal.length;
    }
}