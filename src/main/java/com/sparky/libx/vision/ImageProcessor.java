package com.sparky.libx.vision;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

/**
 * Advanced Image Processing and Computer Vision Utilities for Minecraft Plugins
 * Provides capabilities for image manipulation, feature detection, and pattern recognition
 */
public class ImageProcessor {
    
    /**
     * Represents a pixel with RGB values
     */
    public static class Pixel {
        public int r, g, b;
        
        public Pixel(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
        
        public Pixel(Color color) {
            this.r = color.getRed();
            this.g = color.getGreen();
            this.b = color.getBlue();
        }
        
        public Color toColor() {
            return new Color(r, g, b);
        }
        
        public int toRGB() {
            return (r << 16) | (g << 8) | b;
        }
        
        public double brightness() {
            return 0.299 * r + 0.587 * g + 0.114 * b;
        }
        
        public double distance(Pixel other) {
            int dr = this.r - other.r;
            int dg = this.g - other.g;
            int db = this.b - other.b;
            return Math.sqrt(dr * dr + dg * dg + db * db);
        }
        
        @Override
        public String toString() {
            return String.format("Pixel[r=%d, g=%d, b=%d]", r, g, b);
        }
    }
    
    /**
     * Convolution kernel for image filtering
     */
    public static class Kernel {
        private final double[][] matrix;
        private final int size;
        
        public Kernel(double[][] matrix) {
            this.matrix = matrix;
            this.size = matrix.length;
        }
        
        public static Kernel GAUSSIAN_3X3 = new Kernel(new double[][]{
            {1, 2, 1},
            {2, 4, 2},
            {1, 2, 1}
        });
        
        public static Kernel GAUSSIAN_5X5 = new Kernel(new double[][]{
            {1,  4,  6,  4, 1},
            {4, 16, 24, 16, 4},
            {6, 24, 36, 24, 6},
            {4, 16, 24, 16, 4},
            {1,  4,  6,  4, 1}
        });
        
        public static Kernel SOBEL_X = new Kernel(new double[][]{
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        });
        
        public static Kernel SOBEL_Y = new Kernel(new double[][]{
            {-1, -2, -1},
            { 0,  0,  0},
            { 1,  2,  1}
        });
        
        public static Kernel SHARPEN = new Kernel(new double[][]{
            { 0, -1,  0},
            {-1,  5, -1},
            { 0, -1,  0}
        });
        
        public static Kernel EDGE_DETECT = new Kernel(new double[][]{
            {-1, -1, -1},
            {-1,  8, -1},
            {-1, -1, -1}
        });
        
        public double[][] getMatrix() {
            return matrix;
        }
        
        public int getSize() {
            return size;
        }
    }
    
    /**
     * Apply convolution filter to an image
     */
    public static Pixel[][] convolve(Pixel[][] image, Kernel kernel) {
        int width = image.length;
        int height = image[0].length;
        int kernelSize = kernel.getSize();
        int halfKernel = kernelSize / 2;
        
        Pixel[][] result = new Pixel[width][height];
        
        // Normalize kernel
        double sum = 0;
        for (int i = 0; i < kernelSize; i++) {
            for (int j = 0; j < kernelSize; j++) {
                sum += kernel.getMatrix()[i][j];
            }
        }
        double divisor = sum == 0 ? 1 : sum;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double rSum = 0, gSum = 0, bSum = 0;
                
                for (int kx = 0; kx < kernelSize; kx++) {
                    for (int ky = 0; ky < kernelSize; ky++) {
                        int imgX = x + kx - halfKernel;
                        int imgY = y + ky - halfKernel;
                        
                        // Handle edge cases with mirroring
                        if (imgX < 0) imgX = -imgX;
                        if (imgY < 0) imgY = -imgY;
                        if (imgX >= width) imgX = 2 * width - imgX - 1;
                        if (imgY >= height) imgY = 2 * height - imgY - 1;
                        
                        Pixel pixel = image[imgX][imgY];
                        double weight = kernel.getMatrix()[kx][ky] / divisor;
                        
                        rSum += pixel.r * weight;
                        gSum += pixel.g * weight;
                        bSum += pixel.b * weight;
                    }
                }
                
                // Clamp values to valid range
                int r = Math.max(0, Math.min(255, (int) Math.round(rSum)));
                int g = Math.max(0, Math.min(255, (int) Math.round(gSum)));
                int b = Math.max(0, Math.min(255, (int) Math.round(bSum)));
                
                result[x][y] = new Pixel(r, g, b);
            }
        }
        
        return result;
    }
    
    /**
     * Convert image to grayscale
     */
    public static Pixel[][] toGrayscale(Pixel[][] image) {
        int width = image.length;
        int height = image[0].length;
        Pixel[][] result = new Pixel[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Pixel p = image[x][y];
                int gray = (int) (0.299 * p.r + 0.587 * p.g + 0.114 * p.b);
                result[x][y] = new Pixel(gray, gray, gray);
            }
        }
        
        return result;
    }
    
    /**
     * Apply threshold to create binary image
     */
    public static Pixel[][] threshold(Pixel[][] image, int threshold) {
        int width = image.length;
        int height = image[0].length;
        Pixel[][] result = new Pixel[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Pixel p = image[x][y];
                int value = (int) p.brightness();
                int newValue = value > threshold ? 255 : 0;
                result[x][y] = new Pixel(newValue, newValue, newValue);
            }
        }
        
        return result;
    }
    
    /**
     * Detect edges using Sobel operator
     */
    public static Pixel[][] detectEdges(Pixel[][] image) {
        Pixel[][] gray = toGrayscale(image);
        Pixel[][] sobelX = convolve(gray, Kernel.SOBEL_X);
        Pixel[][] sobelY = convolve(gray, Kernel.SOBEL_Y);
        
        int width = image.length;
        int height = image[0].length;
        Pixel[][] result = new Pixel[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int gx = (int) sobelX[x][y].brightness();
                int gy = (int) sobelY[x][y].brightness();
                int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
                magnitude = Math.min(255, magnitude);
                result[x][y] = new Pixel(magnitude, magnitude, magnitude);
            }
        }
        
        return result;
    }
    
    /**
     * Apply Gaussian blur
     */
    public static Pixel[][] gaussianBlur(Pixel[][] image, int kernelSize) {
        Kernel kernel;
        if (kernelSize <= 3) {
            kernel = Kernel.GAUSSIAN_3X3;
        } else {
            kernel = Kernel.GAUSSIAN_5X5;
        }
        return convolve(image, kernel);
    }
    
    /**
     * Sharpen image
     */
    public static Pixel[][] sharpen(Pixel[][] image) {
        return convolve(image, Kernel.SHARPEN);
    }
    
    /**
     * Create histogram of image
     */
    public static int[] histogram(Pixel[][] image) {
        int[] hist = new int[256];
        int width = image.length;
        int height = image[0].length;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int brightness = (int) image[x][y].brightness();
                hist[brightness]++;
            }
        }
        
        return hist;
    }
    
    /**
     * Equalize histogram
     */
    public static Pixel[][] equalizeHistogram(Pixel[][] image) {
        int width = image.length;
        int height = image[0].length;
        Pixel[][] result = new Pixel[width][height];
        
        int[] hist = histogram(image);
        int totalPixels = width * height;
        
        // Calculate cumulative distribution function
        int[] cdf = new int[256];
        cdf[0] = hist[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + hist[i];
        }
        
        // Find minimum non-zero value in CDF
        int cdfMin = 0;
        for (int i = 0; i < 256; i++) {
            if (cdf[i] > 0) {
                cdfMin = cdf[i];
                break;
            }
        }
        
        // Create lookup table
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            lut[i] = (int) Math.round((double) (cdf[i] - cdfMin) / (totalPixels - cdfMin) * 255);
            lut[i] = Math.max(0, Math.min(255, lut[i]));
        }
        
        // Apply transformation
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Pixel p = image[x][y];
                int brightness = (int) p.brightness();
                int newBrightness = lut[brightness];
                result[x][y] = new Pixel(newBrightness, newBrightness, newBrightness);
            }
        }
        
        return result;
    }
    
    /**
     * Resize image using bilinear interpolation
     */
    public static Pixel[][] resize(Pixel[][] image, int newWidth, int newHeight) {
        int width = image.length;
        int height = image[0].length;
        Pixel[][] result = new Pixel[newWidth][newHeight];
        
        double xRatio = (double) width / newWidth;
        double yRatio = (double) height / newHeight;
        
        for (int x = 0; x < newWidth; x++) {
            for (int y = 0; y < newHeight; y++) {
                double px = x * xRatio;
                double py = y * yRatio;
                
                int x1 = (int) Math.floor(px);
                int y1 = (int) Math.floor(py);
                int x2 = Math.min(x1 + 1, width - 1);
                int y2 = Math.min(y1 + 1, height - 1);
                
                double fx = px - x1;
                double fy = py - y1;
                
                Pixel p1 = image[x1][y1];
                Pixel p2 = image[x2][y1];
                Pixel p3 = image[x1][y2];
                Pixel p4 = image[x2][y2];
                
                int r = (int) (
                    p1.r * (1 - fx) * (1 - fy) +
                    p2.r * fx * (1 - fy) +
                    p3.r * (1 - fx) * fy +
                    p4.r * fx * fy
                );
                
                int g = (int) (
                    p1.g * (1 - fx) * (1 - fy) +
                    p2.g * fx * (1 - fy) +
                    p3.g * (1 - fx) * fy +
                    p4.g * fx * fy
                );
                
                int b = (int) (
                    p1.b * (1 - fx) * (1 - fy) +
                    p2.b * fx * (1 - fy) +
                    p3.b * (1 - fx) * fy +
                    p4.b * fx * fy
                );
                
                result[x][y] = new Pixel(
                    Math.max(0, Math.min(255, r)),
                    Math.max(0, Math.min(255, g)),
                    Math.max(0, Math.min(255, b))
                );
            }
        }
        
        return result;
    }
    
    /**
     * Rotate image by angle in degrees
     */
    public static Pixel[][] rotate(Pixel[][] image, double angle) {
        int width = image.length;
        int height = image[0].length;
        
        double radians = Math.toRadians(angle);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        
        // Calculate new dimensions
        int newWidth = (int) (Math.abs(width * cos) + Math.abs(height * sin));
        int newHeight = (int) (Math.abs(width * sin) + Math.abs(height * cos));
        
        Pixel[][] result = new Pixel[newWidth][newHeight];
        
        // Center coordinates
        double cx = width / 2.0;
        double cy = height / 2.0;
        double ncx = newWidth / 2.0;
        double ncy = newHeight / 2.0;
        
        for (int x = 0; x < newWidth; x++) {
            for (int y = 0; y < newHeight; y++) {
                // Translate to origin
                double tx = x - ncx;
                double ty = y - ncy;
                
                // Rotate
                double rx = tx * cos + ty * sin;
                double ry = -tx * sin + ty * cos;
                
                // Translate back
                double px = rx + cx;
                double py = ry + cy;
                
                // Check bounds
                if (px >= 0 && px < width - 1 && py >= 0 && py < height - 1) {
                    int x1 = (int) Math.floor(px);
                    int y1 = (int) Math.floor(py);
                    int x2 = x1 + 1;
                    int y2 = y1 + 1;
                    
                    double fx = px - x1;
                    double fy = py - y1;
                    
                    Pixel p1 = image[x1][y1];
                    Pixel p2 = image[x2][y1];
                    Pixel p3 = image[x1][y2];
                    Pixel p4 = image[x2][y2];
                    
                    int r = (int) (
                        p1.r * (1 - fx) * (1 - fy) +
                        p2.r * fx * (1 - fy) +
                        p3.r * (1 - fx) * fy +
                        p4.r * fx * fy
                    );
                    
                    int g = (int) (
                        p1.g * (1 - fx) * (1 - fy) +
                        p2.g * fx * (1 - fy) +
                        p3.g * (1 - fx) * fy +
                        p4.g * fx * fy
                    );
                    
                    int b = (int) (
                        p1.b * (1 - fx) * (1 - fy) +
                        p2.b * fx * (1 - fy) +
                        p3.b * (1 - fx) * fy +
                        p4.b * fx * fy
                    );
                    
                    result[x][y] = new Pixel(
                        Math.max(0, Math.min(255, r)),
                        Math.max(0, Math.min(255, g)),
                        Math.max(0, Math.min(255, b))
                    );
                } else {
                    result[x][y] = new Pixel(0, 0, 0);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Create a checkerboard pattern
     */
    public static Pixel[][] createCheckerboard(int width, int height, int squareSize, Pixel color1, Pixel color2) {
        Pixel[][] image = new Pixel[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int squareX = x / squareSize;
                int squareY = y / squareSize;
                
                if ((squareX + squareY) % 2 == 0) {
                    image[x][y] = color1;
                } else {
                    image[x][y] = color2;
                }
            }
        }
        
        return image;
    }
    
    /**
     * Create gradient image
     */
    public static Pixel[][] createGradient(int width, int height, Pixel startColor, Pixel endColor) {
        Pixel[][] image = new Pixel[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double ratio = (double) x / (width - 1);
                
                int r = (int) (startColor.r * (1 - ratio) + endColor.r * ratio);
                int g = (int) (startColor.g * (1 - ratio) + endColor.g * ratio);
                int b = (int) (startColor.b * (1 - ratio) + endColor.b * ratio);
                
                image[x][y] = new Pixel(r, g, b);
            }
        }
        
        return image;
    }
    
    /**
     * Create random noise image
     */
    public static Pixel[][] createNoise(int width, int height) {
        Pixel[][] image = new Pixel[width][height];
        Random rand = new Random();
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int r = rand.nextInt(256);
                int g = rand.nextInt(256);
                int b = rand.nextInt(256);
                image[x][y] = new Pixel(r, g, b);
            }
        }
        
        return image;
    }
    
    /**
     * Find connected components in binary image
     */
    public static int[][] findConnectedComponents(Pixel[][] binaryImage) {
        int width = binaryImage.length;
        int height = binaryImage[0].length;
        int[][] labels = new int[width][height];
        int currentLabel = 1;
        
        // First pass: label components
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (binaryImage[x][y].r == 255 && labels[x][y] == 0) {
                    floodFill(binaryImage, labels, x, y, currentLabel++);
                }
            }
        }
        
        return labels;
    }
    
    /**
     * Helper method for connected component labeling
     */
    private static void floodFill(Pixel[][] image, int[][] labels, int x, int y, int label) {
        int width = image.length;
        int height = image[0].length;
        
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        if (image[x][y].r != 255 || labels[x][y] != 0) return;
        
        labels[x][y] = label;
        
        floodFill(image, labels, x + 1, y, label);
        floodFill(image, labels, x - 1, y, label);
        floodFill(image, labels, x, y + 1, label);
        floodFill(image, labels, x, y - 1, label);
    }
    
    /**
     * Convert BufferedImage to Pixel array
     */
    public static Pixel[][] fromBufferedImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        Pixel[][] pixels = new Pixel[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                pixels[x][y] = new Pixel(r, g, b);
            }
        }
        
        return pixels;
    }
    
    /**
     * Convert Pixel array to BufferedImage
     */
    public static BufferedImage toBufferedImage(Pixel[][] pixels) {
        int width = pixels.length;
        int height = pixels[0].length;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, pixels[x][y].toRGB());
            }
        }
        
        return img;
    }
    
    /**
     * Calculate structural similarity index between two images
     */
    public static double calculateSSIM(Pixel[][] img1, Pixel[][] img2) {
        if (img1.length != img2.length || img1[0].length != img2[0].length) {
            throw new IllegalArgumentException("Images must have the same dimensions");
        }
        
        int width = img1.length;
        int height = img1[0].length;
        
        // Constants for SSIM calculation
        final double K1 = 0.01;
        final double K2 = 0.03;
        final double L = 255;
        final double C1 = (K1 * L) * (K1 * L);
        final double C2 = (K2 * L) * (K2 * L);
        
        double mean1 = 0, mean2 = 0;
        double var1 = 0, var2 = 0, covar = 0;
        
        // Calculate means
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                mean1 += img1[x][y].brightness();
                mean2 += img2[x][y].brightness();
            }
        }
        mean1 /= (width * height);
        mean2 /= (width * height);
        
        // Calculate variances and covariance
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double b1 = img1[x][y].brightness();
                double b2 = img2[x][y].brightness();
                
                var1 += (b1 - mean1) * (b1 - mean1);
                var2 += (b2 - mean2) * (b2 - mean2);
                covar += (b1 - mean1) * (b2 - mean2);
            }
        }
        var1 /= (width * height - 1);
        var2 /= (width * height - 1);
        covar /= (width * height - 1);
        
        // Calculate SSIM
        double numerator = (2 * mean1 * mean2 + C1) * (2 * covar + C2);
        double denominator = (mean1 * mean1 + mean2 * mean2 + C1) * (var1 + var2 + C2);
        
        return numerator / denominator;
    }
}