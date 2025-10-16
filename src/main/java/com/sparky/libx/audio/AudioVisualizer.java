package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.AudioClip;
import com.sparky.libx.audio.AudioAnalyzer.AnalysisResult;
import javax.sound.sampled.AudioFormat;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * audio visualizer for creating visual representations of audio data
 * provides capabilities for waveform visualization, spectrum analysis, and audio graphs
 * @author Андрій Будильников
 */
public class AudioVisualizer {
    
    /**
     * waveform visualization parameters
     */
    public static class WaveformParams {
        public int width = 800;
        public int height = 200;
        public Color backgroundColor = Color.BLACK;
        public Color waveformColor = Color.GREEN;
        public boolean showAxis = true;
        public boolean showGrid = true;
        
        public WaveformParams() {}
        
        public WaveformParams(int width, int height, Color backgroundColor, Color waveformColor) {
            this.width = width;
            this.height = height;
            this.backgroundColor = backgroundColor;
            this.waveformColor = waveformColor;
        }
    }
    
    /**
     * spectrum visualization parameters
     */
    public static class SpectrumParams {
        public int width = 800;
        public int height = 200;
        public Color backgroundColor = Color.BLACK;
        public Color spectrumColor = Color.BLUE;
        public boolean logarithmicScale = true;
        public boolean showAxis = true;
        
        public SpectrumParams() {}
        
        public SpectrumParams(int width, int height, Color backgroundColor, Color spectrumColor) {
            this.width = width;
            this.height = height;
            this.backgroundColor = backgroundColor;
            this.spectrumColor = spectrumColor;
        }
    }
    
    /**
     * generate waveform image from audio clip
     */
    public static BufferedImage generateWaveform(AudioClip clip, WaveformParams params) {
        // create image
        BufferedImage image = new BufferedImage(params.width, params.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // set background
        g2d.setColor(params.backgroundColor);
        g2d.fillRect(0, 0, params.width, params.height);
        
        // draw grid if requested
        if (params.showGrid) {
            drawGrid(g2d, params);
        }
        
        // draw axis if requested
        if (params.showAxis) {
            drawAxis(g2d, params);
        }
        
        // extract amplitude data
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        double[] amplitudeData = extractAmplitudeData(data, format);
        
        // draw waveform
        drawWaveform(g2d, amplitudeData, params);
        
        g2d.dispose();
        return image;
    }
    
    /**
     * generate spectrum image from audio clip
     */
    public static BufferedImage generateSpectrum(AudioClip clip, SpectrumParams params) {
        // analyze audio
        AnalysisResult analysis = AudioAnalyzer.analyze(clip);
        
        // create image
        BufferedImage image = new BufferedImage(params.width, params.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // set background
        g2d.setColor(params.backgroundColor);
        g2d.fillRect(0, 0, params.width, params.height);
        
        // draw axis if requested
        if (params.showAxis) {
            drawSpectrumAxis(g2d, params);
        }
        
        // draw spectrum
        drawSpectrum(g2d, analysis.spectrum, params);
        
        g2d.dispose();
        return image;
    }
    
    /**
     * generate combined visualization (waveform and spectrum)
     */
    public static BufferedImage generateCombinedView(AudioClip clip, WaveformParams waveformParams, SpectrumParams spectrumParams) {
        // create combined image
        int combinedWidth = Math.max(waveformParams.width, spectrumParams.width);
        int combinedHeight = waveformParams.height + spectrumParams.height + 10; // 10px gap
        
        BufferedImage image = new BufferedImage(combinedWidth, combinedHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // set background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, combinedWidth, combinedHeight);
        
        // generate and draw waveform
        BufferedImage waveformImage = generateWaveform(clip, waveformParams);
        g2d.drawImage(waveformImage, 0, 0, null);
        
        // generate and draw spectrum
        BufferedImage spectrumImage = generateSpectrum(clip, spectrumParams);
        g2d.drawImage(spectrumImage, 0, waveformParams.height + 10, null);
        
        g2d.dispose();
        return image;
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
     * draw grid on waveform
     */
    private static void drawGrid(Graphics2D g2d, WaveformParams params) {
        g2d.setColor(new Color(50, 50, 50));
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        
        // vertical grid lines
        int gridSpacing = params.width / 10;
        for (int x = 0; x <= params.width; x += gridSpacing) {
            g2d.drawLine(x, 0, x, params.height);
        }
        
        // horizontal grid lines
        int centerY = params.height / 2;
        g2d.drawLine(0, centerY, params.width, centerY);
        g2d.drawLine(0, centerY / 2, params.width, centerY / 2);
        g2d.drawLine(0, centerY + centerY / 2, params.width, centerY + centerY / 2);
        
        g2d.setStroke(new BasicStroke());
    }
    
    /**
     * draw axis on waveform
     */
    private static void drawAxis(Graphics2D g2d, WaveformParams params) {
        g2d.setColor(Color.GRAY);
        
        // horizontal axis (center line)
        int centerY = params.height / 2;
        g2d.drawLine(0, centerY, params.width, centerY);
        
        // vertical axis
        g2d.drawLine(0, 0, 0, params.height);
        
        // draw axis labels
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("0", 5, centerY - 5);
        g2d.drawString("+1", 5, 15);
        g2d.drawString("-1", 5, params.height - 5);
    }
    
    /**
     * draw waveform
     */
    private static void drawWaveform(Graphics2D g2d, double[] amplitudeData, WaveformParams params) {
        g2d.setColor(params.waveformColor);
        g2d.setStroke(new BasicStroke(2f));
        
        int centerY = params.height / 2;
        int samples = amplitudeData.length;
        int pointsToDraw = Math.min(samples, params.width);
        
        int lastX = 0;
        int lastY = centerY;
        
        for (int i = 0; i < pointsToDraw; i++) {
            int sampleIndex = (int) ((double) i / pointsToDraw * samples);
            double amplitude = amplitudeData[sampleIndex];
            
            int x = i;
            int y = centerY - (int) (amplitude * (centerY - 10)); // leave 10px margin
            
            if (i > 0) {
                g2d.drawLine(lastX, lastY, x, y);
            }
            
            lastX = x;
            lastY = y;
        }
    }
    
    /**
     * draw axis on spectrum
     */
    private static void drawSpectrumAxis(Graphics2D g2d, SpectrumParams params) {
        g2d.setColor(Color.GRAY);
        
        // horizontal axis
        g2d.drawLine(0, params.height - 1, params.width, params.height - 1);
        
        // vertical axis
        g2d.drawLine(0, 0, 0, params.height);
        
        // draw axis labels
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("0Hz", 5, params.height - 5);
        g2d.drawString("20kHz", params.width - 40, params.height - 5);
    }
    
    /**
     * draw spectrum
     */
    private static void drawSpectrum(Graphics2D g2d, double[] spectrum, SpectrumParams params) {
        g2d.setColor(params.spectrumColor);
        g2d.setStroke(new BasicStroke(2f));
        
        int bands = spectrum.length;
        int barWidth = Math.max(1, params.width / bands);
        
        for (int i = 0; i < bands; i++) {
            double amplitude = spectrum[i];
            
            // apply logarithmic scale if requested
            if (params.logarithmicScale) {
                amplitude = Math.log10(1 + 9 * amplitude); // scale to 0-1 range
            }
            
            int x = i * barWidth;
            int barHeight = (int) (amplitude * (params.height - 20)); // leave 20px margin
            int y = params.height - barHeight - 10;
            
            g2d.fillRect(x, y, barWidth - 1, barHeight);
        }
    }
    
    /**
     * generate ascii art representation of waveform
     */
    public static String generateAsciiWaveform(AudioClip clip, int width, int height) {
        AudioFormat format = clip.getFormat();
        byte[] data = clip.getData();
        double[] amplitudeData = extractAmplitudeData(data, format);
        
        StringBuilder ascii = new StringBuilder();
        
        // create simple ascii visualization
        int samplesPerRow = Math.max(1, amplitudeData.length / width);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sampleIndex = x * samplesPerRow;
                if (sampleIndex < amplitudeData.length) {
                    double amplitude = amplitudeData[sampleIndex];
                    int scaledY = (int) ((amplitude + 1.0) * height / 2);
                    
                    if (y == scaledY) {
                        ascii.append("*");
                    } else if (y == height / 2) {
                        ascii.append("-");
                    } else {
                        ascii.append(" ");
                    }
                } else {
                    ascii.append(" ");
                }
            }
            ascii.append("\n");
        }
        
        return ascii.toString();
    }
    
    /**
     * generate text-based spectrum analysis
     */
    public static String generateTextSpectrum(AudioClip clip, int bands) {
        AnalysisResult analysis = AudioAnalyzer.analyze(clip);
        double[] spectrum = analysis.spectrum;
        
        StringBuilder textSpectrum = new StringBuilder();
        textSpectrum.append("Frequency Spectrum Analysis:\n");
        textSpectrum.append("============================\n");
        
        int displayBands = Math.min(bands, spectrum.length);
        double maxAmplitude = 0;
        
        // find maximum amplitude for scaling
        for (double amplitude : spectrum) {
            maxAmplitude = Math.max(maxAmplitude, amplitude);
        }
        
        // display each band
        for (int i = 0; i < displayBands; i++) {
            double amplitude = spectrum[i];
            double normalized = maxAmplitude > 0 ? amplitude / maxAmplitude : 0;
            
            // create bar visualization
            int barLength = (int) (normalized * 50);
            StringBuilder bar = new StringBuilder();
            for (int j = 0; j < barLength; j++) {
                bar.append("█");
            }
            
            textSpectrum.append(String.format("Band %2d: %6.3f |%s\n", i, amplitude, bar.toString()));
        }
        
        return textSpectrum.toString();
    }
}