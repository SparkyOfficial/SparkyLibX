package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.AudioClip;
import javax.sound.sampled.AudioFormat;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Advanced audio visualizer for creating sophisticated audio visualizations
 * including waveform, spectrum, and 3D visualizations
 */
public class AudioVisualizerAdvanced {
    
    /**
     * Parameters for advanced waveform visualization
     */
    public static class AdvancedWaveformParams {
        public int width = 800;
        public int height = 600;
        public Color backgroundColor = Color.BLACK;
        public Color waveformColor = Color.GREEN;
        public Color gridColor = Color.DARK_GRAY;
        public Color axisColor = Color.WHITE;
        public boolean showGrid = true;
        public boolean showAxis = true;
        public double zoom = 1.0;
        public int samplesPerPixel = 1;
        public VisualizationStyle style = VisualizationStyle.LINE;
        public boolean showSpectrum = false;
        public Color spectrumColor = Color.BLUE;
        
        public enum VisualizationStyle {
            LINE, BAR, DOT, AREA
        }
    }
    
    /**
     * Parameters for 3D audio visualization
     */
    public static class Audio3DParams {
        public int width = 800;
        public int height = 600;
        public Color backgroundColor = Color.BLACK;
        public Color sourceColor = Color.RED;
        public Color listenerColor = Color.BLUE;
        public Color waveColor = Color.GREEN;
        public double rotationX = 0;
        public double rotationY = 0;
        public double rotationZ = 0;
        public double scale = 1.0;
        public boolean showAxes = true;
    }
    
    /**
     * Parameters for spectrogram visualization
     */
    public static class SpectrogramParams {
        public int width = 800;
        public int height = 600;
        public Color backgroundColor = Color.BLACK;
        public Color lowColor = Color.BLACK;
        public Color highColor = Color.YELLOW;
        public boolean logarithmicScale = true;
        public double minFrequency = 0;
        public double maxFrequency = 0; // 0 means auto-detect
        public boolean showTimeAxis = true;
        public boolean showFrequencyAxis = true;
    }
    
    /**
     * Generate advanced waveform visualization
     */
    public static BufferedImage generateAdvancedWaveform(AudioClip clip, AdvancedWaveformParams params) {
        // Create image
        BufferedImage image = new BufferedImage(params.width, params.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Set background
        g2d.setColor(params.backgroundColor);
        g2d.fillRect(0, 0, params.width, params.height);
        
        // Draw grid if requested
        if (params.showGrid) {
            drawGrid(g2d, params);
        }
        
        // Draw axis if requested
        if (params.showAxis) {
            drawAxis(g2d, params);
        }
        
        // Extract amplitude data
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        double[] amplitudeData = extractAmplitudeData(data, format);
        
        // Draw waveform
        drawAdvancedWaveform(g2d, amplitudeData, params);
        
        // Draw spectrum if requested
        if (params.showSpectrum) {
            drawSpectrumOverlay(g2d, amplitudeData, format.getSampleRate(), params);
        }
        
        g2d.dispose();
        return image;
    }
    
    /**
     * Generate 3D audio visualization
     */
    public static BufferedImage generate3DVisualization(AudioClip clip, Audio3DParams params) {
        BufferedImage image = new BufferedImage(params.width, params.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Set background
        g2d.setColor(params.backgroundColor);
        g2d.fillRect(0, 0, params.width, params.height);
        
        // Draw 3D axes
        if (params.showAxes) {
            draw3DAxes(g2d, params);
        }
        
        // Draw audio wave in 3D space
        draw3DWave(g2d, clip, params);
        
        g2d.dispose();
        return image;
    }
    
    /**
     * Generate spectrogram visualization
     */
    public static BufferedImage generateSpectrogram(AudioClip clip, SpectrogramParams params) {
        BufferedImage image = new BufferedImage(params.width, params.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Set background
        g2d.setColor(params.backgroundColor);
        g2d.fillRect(0, 0, params.width, params.height);
        
        // Extract amplitude data
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        double[] amplitudeData = extractAmplitudeData(data, format);
        
        // Calculate spectrogram
        double[][] spectrogram = calculateSpectrogram(amplitudeData, format.getSampleRate(), 1024, 512);
        
        // Draw spectrogram
        drawSpectrogram(g2d, spectrogram, params);
        
        // Draw axes
        if (params.showTimeAxis || params.showFrequencyAxis) {
            drawSpectrogramAxes(g2d, spectrogram, format.getSampleRate(), params);
        }
        
        g2d.dispose();
        return image;
    }
    
    /**
     * Create animated visualization sequence
     */
    public static BufferedImage[] createAnimation(AudioClip clip, AdvancedWaveformParams params, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        double[] amplitudeData = extractAmplitudeData(data, format);
        
        int samplesPerFrame = amplitudeData.length / frameCount;
        
        for (int i = 0; i < frameCount; i++) {
            // Create image for this frame
            BufferedImage image = new BufferedImage(params.width, params.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // Set rendering hints
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Set background
            g2d.setColor(params.backgroundColor);
            g2d.fillRect(0, 0, params.width, params.height);
            
            // Draw grid if requested
            if (params.showGrid) {
                drawGrid(g2d, params);
            }
            
            // Draw axis if requested
            if (params.showAxis) {
                drawAxis(g2d, params);
            }
            
            // Extract amplitude data for this frame
            int startSample = i * samplesPerFrame;
            int endSample = Math.min(startSample + samplesPerFrame, amplitudeData.length);
            double[] frameData = Arrays.copyOfRange(amplitudeData, startSample, endSample);
            
            // Draw waveform for this frame
            drawAdvancedWaveform(g2d, frameData, params);
            
            g2d.dispose();
            frames[i] = image;
        }
        
        return frames;
    }
    
    /**
     * Draw grid on the visualization
     */
    private static void drawGrid(Graphics2D g2d, AdvancedWaveformParams params) {
        g2d.setColor(params.gridColor);
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        
        // Vertical grid lines
        int gridSpacingX = params.width / 10;
        for (int x = 0; x <= params.width; x += gridSpacingX) {
            g2d.drawLine(x, 0, x, params.height);
        }
        
        // Horizontal grid lines
        int gridSpacingY = params.height / 8;
        for (int y = 0; y <= params.height; y += gridSpacingY) {
            g2d.drawLine(0, y, params.width, y);
        }
    }
    
    /**
     * Draw axis on the visualization
     */
    private static void drawAxis(Graphics2D g2d, AdvancedWaveformParams params) {
        g2d.setColor(params.axisColor);
        g2d.setStroke(new BasicStroke(2f));
        
        // X axis (time)
        g2d.drawLine(0, params.height / 2, params.width, params.height / 2);
        
        // Y axis (amplitude)
        g2d.drawLine(params.width / 2, 0, params.width / 2, params.height);
    }
    
    /**
     * Draw advanced waveform based on parameters
     */
    private static void drawAdvancedWaveform(Graphics2D g2d, double[] amplitudeData, AdvancedWaveformParams params) {
        g2d.setColor(params.waveformColor);
        g2d.setStroke(new BasicStroke(2f));
        
        int samplesPerPixel = Math.max(1, params.samplesPerPixel);
        int pixels = Math.min(params.width, amplitudeData.length / samplesPerPixel);
        
        switch (params.style) {
            case LINE:
                drawLineWaveform(g2d, amplitudeData, params, samplesPerPixel, pixels);
                break;
            case BAR:
                drawBarWaveform(g2d, amplitudeData, params, samplesPerPixel, pixels);
                break;
            case DOT:
                drawDotWaveform(g2d, amplitudeData, params, samplesPerPixel, pixels);
                break;
            case AREA:
                drawAreaWaveform(g2d, amplitudeData, params, samplesPerPixel, pixels);
                break;
        }
    }
    
    /**
     * Draw line waveform
     */
    private static void drawLineWaveform(Graphics2D g2d, double[] amplitudeData, AdvancedWaveformParams params, 
                                       int samplesPerPixel, int pixels) {
        int centerY = params.height / 2;
        int lastX = 0, lastY = centerY;
        
        for (int i = 0; i < pixels; i++) {
            int startSample = i * samplesPerPixel;
            int endSample = Math.min(startSample + samplesPerPixel, amplitudeData.length);
            
            // Calculate average amplitude for this pixel
            double sum = 0;
            for (int j = startSample; j < endSample; j++) {
                sum += amplitudeData[j];
            }
            double avgAmplitude = sum / (endSample - startSample);
            
            int x = (int) ((double) i / pixels * params.width);
            int y = (int) (centerY - avgAmplitude * params.height / 2 * params.zoom);
            
            // Clamp Y to image bounds
            y = Math.max(0, Math.min(params.height, y));
            
            if (i > 0) {
                g2d.drawLine(lastX, lastY, x, y);
            }
            
            lastX = x;
            lastY = y;
        }
    }
    
    /**
     * Draw bar waveform
     */
    private static void drawBarWaveform(Graphics2D g2d, double[] amplitudeData, AdvancedWaveformParams params, 
                                      int samplesPerPixel, int pixels) {
        int centerY = params.height / 2;
        
        for (int i = 0; i < pixels; i++) {
            int startSample = i * samplesPerPixel;
            int endSample = Math.min(startSample + samplesPerPixel, amplitudeData.length);
            
            // Calculate average amplitude for this pixel
            double sum = 0;
            for (int j = startSample; j < endSample; j++) {
                sum += Math.abs(amplitudeData[j]);
            }
            double avgAmplitude = sum / (endSample - startSample);
            
            int x = (int) ((double) i / pixels * params.width);
            int barHeight = (int) (avgAmplitude * params.height / 2 * params.zoom);
            
            // Draw bar
            g2d.fillRect(x, centerY - barHeight / 2, Math.max(1, params.width / pixels), barHeight);
        }
    }
    
    /**
     * Draw dot waveform
     */
    private static void drawDotWaveform(Graphics2D g2d, double[] amplitudeData, AdvancedWaveformParams params, 
                                      int samplesPerPixel, int pixels) {
        int centerY = params.height / 2;
        
        for (int i = 0; i < pixels; i++) {
            int startSample = i * samplesPerPixel;
            int endSample = Math.min(startSample + samplesPerPixel, amplitudeData.length);
            
            // Calculate average amplitude for this pixel
            double sum = 0;
            for (int j = startSample; j < endSample; j++) {
                sum += amplitudeData[j];
            }
            double avgAmplitude = sum / (endSample - startSample);
            
            int x = (int) ((double) i / pixels * params.width);
            int y = (int) (centerY - avgAmplitude * params.height / 2 * params.zoom);
            
            // Clamp Y to image bounds
            y = Math.max(0, Math.min(params.height, y));
            
            // Draw dot
            g2d.fillOval(x - 2, y - 2, 4, 4);
        }
    }
    
    /**
     * Draw area waveform
     */
    private static void drawAreaWaveform(Graphics2D g2d, double[] amplitudeData, AdvancedWaveformParams params, 
                                       int samplesPerPixel, int pixels) {
        int centerY = params.height / 2;
        int[] xPoints = new int[pixels + 2];
        int[] yPoints = new int[pixels + 2];
        
        // Add points for the waveform
        for (int i = 0; i < pixels; i++) {
            int startSample = i * samplesPerPixel;
            int endSample = Math.min(startSample + samplesPerPixel, amplitudeData.length);
            
            // Calculate average amplitude for this pixel
            double sum = 0;
            for (int j = startSample; j < endSample; j++) {
                sum += amplitudeData[j];
            }
            double avgAmplitude = sum / (endSample - startSample);
            
            xPoints[i] = (int) ((double) i / pixels * params.width);
            yPoints[i] = (int) (centerY - avgAmplitude * params.height / 2 * params.zoom);
            
            // Clamp Y to image bounds
            yPoints[i] = Math.max(0, Math.min(params.height, yPoints[i]));
        }
        
        // Close the polygon by connecting to the center line
        xPoints[pixels] = params.width;
        yPoints[pixels] = centerY;
        xPoints[pixels + 1] = 0;
        yPoints[pixels + 1] = centerY;
        
        // Draw filled area
        g2d.fillPolygon(xPoints, yPoints, pixels + 2);
    }
    
    /**
     * Draw spectrum overlay
     */
    private static void drawSpectrumOverlay(Graphics2D g2d, double[] amplitudeData, double sampleRate, 
                                          AdvancedWaveformParams params) {
        g2d.setColor(params.spectrumColor);
        g2d.setStroke(new BasicStroke(1f));
        
        // Calculate spectrum (simplified)
        double[] spectrum = calculateSpectrum(amplitudeData, sampleRate);
        int centerY = params.height - 50; // Position at bottom of image
        
        int barWidth = Math.max(1, params.width / spectrum.length);
        for (int i = 0; i < Math.min(spectrum.length, params.width / barWidth); i++) {
            int barHeight = (int) (spectrum[i] * 30); // Scale for visibility
            int x = i * barWidth;
            g2d.fillRect(x, centerY - barHeight, barWidth, barHeight);
        }
    }
    
    /**
     * Draw 3D axes
     */
    private static void draw3DAxes(Graphics2D g2d, Audio3DParams params) {
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2f));
        
        int centerX = params.width / 2;
        int centerY = params.height / 2;
        int axisLength = 100;
        
        // X axis (red)
        g2d.setColor(Color.RED);
        g2d.drawLine(centerX, centerY, centerX + axisLength, centerY);
        g2d.drawString("X", centerX + axisLength + 5, centerY + 5);
        
        // Y axis (green)
        g2d.setColor(Color.GREEN);
        g2d.drawLine(centerX, centerY, centerX, centerY - axisLength);
        g2d.drawString("Y", centerX - 10, centerY - axisLength - 5);
        
        // Z axis (blue)
        g2d.setColor(Color.BLUE);
        int zEndX = centerX + (int) (axisLength * Math.cos(Math.PI / 6));
        int zEndY = centerY + (int) (axisLength * Math.sin(Math.PI / 6));
        g2d.drawLine(centerX, centerY, zEndX, zEndY);
        g2d.drawString("Z", zEndX + 5, zEndY + 5);
    }
    
    /**
     * Draw 3D wave visualization
     */
    private static void draw3DWave(Graphics2D g2d, AudioClip clip, Audio3DParams params) {
        g2d.setColor(params.waveColor);
        g2d.setStroke(new BasicStroke(1f));
        
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        double[] amplitudeData = extractAmplitudeData(data, format);
        
        int centerX = params.width / 2;
        int centerY = params.height / 2;
        int samples = Math.min(amplitudeData.length, 1000); // Limit for performance
        
        int lastX = 0, lastY = 0;
        for (int i = 0; i < samples; i++) {
            double amplitude = amplitudeData[i];
            double time = (double) i / samples * 2 * Math.PI;
            
            // 3D coordinates
            double x = amplitude * Math.cos(time) * 50;
            double y = amplitude * Math.sin(time) * 50;
            double z = time * 10;
            
            // Apply rotation
            double rotatedX = x * Math.cos(params.rotationY) - z * Math.sin(params.rotationY);
            double rotatedY = y;
            double rotatedZ = x * Math.sin(params.rotationY) + z * Math.cos(params.rotationY);
            
            // Project to 2D
            int screenX = centerX + (int) (rotatedX * params.scale);
            int screenY = centerY - (int) (rotatedY * params.scale);
            
            if (i > 0) {
                g2d.drawLine(lastX, lastY, screenX, screenY);
            }
            
            lastX = screenX;
            lastY = screenY;
        }
    }
    
    /**
     * Draw spectrogram
     */
    private static void drawSpectrogram(Graphics2D g2d, double[][] spectrogram, SpectrogramParams params) {
        int width = params.width;
        int height = params.height;
        
        for (int x = 0; x < width && x < spectrogram.length; x++) {
            int frameIndex = x * spectrogram.length / width;
            double[] frame = spectrogram[frameIndex];
            
            for (int y = 0; y < height && y < frame.length; y++) {
                int freqIndex = y * frame.length / height;
                double magnitude = frame[freqIndex];
                
                // Normalize magnitude
                magnitude = Math.min(1.0, magnitude);
                
                // Apply logarithmic scale if requested
                if (params.logarithmicScale) {
                    magnitude = Math.log10(1 + 9 * magnitude);
                }
                
                // Interpolate color
                Color color = interpolateColor(params.lowColor, params.highColor, magnitude);
                g2d.setColor(color);
                g2d.fillRect(x, height - y - 1, 1, 1);
            }
        }
    }
    
    /**
     * Draw spectrogram axes
     */
    private static void drawSpectrogramAxes(Graphics2D g2d, double[][] spectrogram, double sampleRate, 
                                          SpectrogramParams params) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Time axis
        if (params.showTimeAxis) {
            for (int i = 0; i <= 10; i++) {
                int x = i * params.width / 10;
                double time = i * spectrogram.length / (10.0 * sampleRate);
                g2d.drawLine(x, params.height - 1, x, params.height + 5);
                g2d.drawString(String.format("%.1fs", time), x - 15, params.height + 20);
            }
        }
        
        // Frequency axis
        if (params.showFrequencyAxis) {
            double maxFreq = params.maxFrequency > 0 ? params.maxFrequency : sampleRate / 2;
            for (int i = 0; i <= 8; i++) {
                int y = params.height - i * params.height / 8;
                double freq = i * maxFreq / 8;
                g2d.drawLine(0, y, -5, y);
                g2d.drawString(String.format("%.0fHz", freq), -40, y + 5);
            }
        }
    }
    
    /**
     * Interpolate between two colors
     */
    private static Color interpolateColor(Color color1, Color color2, double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        
        int red = (int) (color1.getRed() + ratio * (color2.getRed() - color1.getRed()));
        int green = (int) (color1.getGreen() + ratio * (color2.getGreen() - color1.getGreen()));
        int blue = (int) (color1.getBlue() + ratio * (color2.getBlue() - color1.getBlue()));
        
        return new Color(red, green, blue);
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
     * Calculate spectrum using simplified method
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