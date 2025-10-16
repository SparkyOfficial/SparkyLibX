package com.sparky.libx.vision;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.math.Matrix4x4;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Advanced Computer Vision Framework for Minecraft Plugins
 * Provides capabilities for image processing, object detection, feature extraction, and computer vision algorithms
 * 
 * @author Андрій Будильников
 */
public class ComputerVision {
    
    /**
     * Represents a 2D image with pixel data
     */
    public static class Image {
        private final int width;
        private final int height;
        private final int[][][] pixels; // [height][width][3] for RGB
        
        public Image(int width, int height) {
            this.width = width;
            this.height = height;
            this.pixels = new int[height][width][3];
        }
        
        public Image(BufferedImage bufferedImage) {
            this.width = bufferedImage.getWidth();
            this.height = bufferedImage.getHeight();
            this.pixels = new int[height][width][3];
            
            // Convert BufferedImage to our internal format
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    pixels[y][x][0] = (rgb >> 16) & 0xFF; // Red
                    pixels[y][x][1] = (rgb >> 8) & 0xFF;  // Green
                    pixels[y][x][2] = rgb & 0xFF;         // Blue
                }
            }
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public int getRed(int x, int y) {
            return pixels[y][x][0];
        }
        
        public int getGreen(int x, int y) {
            return pixels[y][x][1];
        }
        
        public int getBlue(int x, int y) {
            return pixels[y][x][2];
        }
        
        public int[] getRGB(int x, int y) {
            return new int[]{pixels[y][x][0], pixels[y][x][1], pixels[y][x][2]};
        }
        
        public void setRed(int x, int y, int value) {
            pixels[y][x][0] = clamp(value, 0, 255);
        }
        
        public void setGreen(int x, int y, int value) {
            pixels[y][x][1] = clamp(value, 0, 255);
        }
        
        public void setBlue(int x, int y, int value) {
            pixels[y][x][2] = clamp(value, 0, 255);
        }
        
        public void setRGB(int x, int y, int red, int green, int blue) {
            pixels[y][x][0] = clamp(red, 0, 255);
            pixels[y][x][1] = clamp(green, 0, 255);
            pixels[y][x][2] = clamp(blue, 0, 255);
        }
        
        public void setRGB(int x, int y, int[] rgb) {
            if (rgb.length != 3) {
                throw new IllegalArgumentException("RGB array must have 3 elements");
            }
            pixels[y][x][0] = clamp(rgb[0], 0, 255);
            pixels[y][x][1] = clamp(rgb[1], 0, 255);
            pixels[y][x][2] = clamp(rgb[2], 0, 255);
        }
        
        /**
         * Converts this image to a BufferedImage
         */
        public BufferedImage toBufferedImage() {
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = (pixels[y][x][0] << 16) | (pixels[y][x][1] << 8) | pixels[y][x][2];
                    bufferedImage.setRGB(x, y, rgb);
                }
            }
            return bufferedImage;
        }
        
        /**
         * Creates a copy of this image
         */
        public Image copy() {
            Image copy = new Image(width, height);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    copy.pixels[y][x][0] = this.pixels[y][x][0];
                    copy.pixels[y][x][1] = this.pixels[y][x][1];
                    copy.pixels[y][x][2] = this.pixels[y][x][2];
                }
            }
            return copy;
        }
        
        /**
         * Gets a sub-image from this image
         */
        public Image getSubImage(int x, int y, int subWidth, int subHeight) {
            if (x < 0 || y < 0 || x + subWidth > width || y + subHeight > height) {
                throw new IllegalArgumentException("Sub-image bounds are outside the image");
            }
            
            Image subImage = new Image(subWidth, subHeight);
            for (int sy = 0; sy < subHeight; sy++) {
                for (int sx = 0; sx < subWidth; sx++) {
                    subImage.pixels[sy][sx][0] = this.pixels[y + sy][x + sx][0];
                    subImage.pixels[sy][sx][1] = this.pixels[y + sy][x + sx][1];
                    subImage.pixels[sy][sx][2] = this.pixels[y + sy][x + sx][2];
                }
            }
            return subImage;
        }
        
        /**
         * Clamps a value between min and max
         */
        private int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }
    }
    
    /**
     * Represents a grayscale image with pixel data
     */
    public static class GrayscaleImage {
        private final int width;
        private final int height;
        private final int[][] pixels; // [height][width]
        
        public GrayscaleImage(int width, int height) {
            this.width = width;
            this.height = height;
            this.pixels = new int[height][width];
        }
        
        public GrayscaleImage(Image colorImage) {
            this.width = colorImage.getWidth();
            this.height = colorImage.getHeight();
            this.pixels = new int[height][width];
            
            // Convert color image to grayscale using luminance formula
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int[] rgb = colorImage.getRGB(x, y);
                    int gray = (int) (0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);
                    pixels[y][x] = gray;
                }
            }
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public int getPixel(int x, int y) {
            return pixels[y][x];
        }
        
        public void setPixel(int x, int y, int value) {
            pixels[y][x] = clamp(value, 0, 255);
        }
        
        /**
         * Converts this grayscale image to a color image
         */
        public Image toColorImage() {
            Image colorImage = new Image(width, height);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int gray = pixels[y][x];
                    colorImage.setRGB(x, y, gray, gray, gray);
                }
            }
            return colorImage;
        }
        
        /**
         * Creates a copy of this grayscale image
         */
        public GrayscaleImage copy() {
            GrayscaleImage copy = new GrayscaleImage(width, height);
            for (int y = 0; y < height; y++) {
                System.arraycopy(this.pixels[y], 0, copy.pixels[y], 0, width);
            }
            return copy;
        }
        
        /**
         * Clamps a value between min and max
         */
        private int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }
    }
    
    /**
     * Represents a computer vision filter
     */
    public abstract static class Filter {
        /**
         * Applies the filter to an image
         */
        public abstract Image apply(Image image);
    }
    
    /**
     * Represents a convolution filter
     */
    public static class ConvolutionFilter extends Filter {
        private final double[][] kernel;
        private final boolean normalize;
        
        public ConvolutionFilter(double[][] kernel) {
            this(kernel, true);
        }
        
        public ConvolutionFilter(double[][] kernel, boolean normalize) {
            this.kernel = kernel;
            this.normalize = normalize;
        }
        
        @Override
        public Image apply(Image image) {
            int width = image.getWidth();
            int height = image.getHeight();
            Image result = image.copy();
            
            int kernelHeight = kernel.length;
            int kernelWidth = kernel[0].length;
            int kernelCenterY = kernelHeight / 2;
            int kernelCenterX = kernelWidth / 2;
            
            // Normalize kernel if requested
            double kernelSum = 0;
            if (normalize) {
                for (int i = 0; i < kernelHeight; i++) {
                    for (int j = 0; j < kernelWidth; j++) {
                        kernelSum += kernel[i][j];
                    }
                }
                if (kernelSum == 0) kernelSum = 1; // Avoid division by zero
            } else {
                kernelSum = 1;
            }
            
            // Apply convolution
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double redSum = 0, greenSum = 0, blueSum = 0;
                    
                    // Apply kernel
                    for (int ky = 0; ky < kernelHeight; ky++) {
                        for (int kx = 0; kx < kernelWidth; kx++) {
                            int imageY = y + ky - kernelCenterY;
                            int imageX = x + kx - kernelCenterX;
                            
                            // Handle boundary conditions (mirror padding)
                            if (imageY < 0) imageY = -imageY;
                            if (imageY >= height) imageY = 2 * height - imageY - 2;
                            if (imageX < 0) imageX = -imageX;
                            if (imageX >= width) imageX = 2 * width - imageX - 2;
                            
                            double kernelValue = kernel[ky][kx] / kernelSum;
                            int[] rgb = image.getRGB(imageX, imageY);
                            redSum += rgb[0] * kernelValue;
                            greenSum += rgb[1] * kernelValue;
                            blueSum += rgb[2] * kernelValue;
                        }
                    }
                    
                    // Set result pixel
                    result.setRGB(x, y, (int) redSum, (int) greenSum, (int) blueSum);
                }
            }
            
            return result;
        }
    }
    
    /**
     * Represents a Gaussian blur filter
     */
    public static class GaussianBlurFilter extends Filter {
        private final double sigma;
        private final int radius;
        
        public GaussianBlurFilter(double sigma) {
            this.sigma = sigma;
            this.radius = (int) Math.ceil(sigma * 3);
        }
        
        @Override
        public Image apply(Image image) {
            // Create Gaussian kernel
            int size = 2 * radius + 1;
            double[][] kernel = new double[size][size];
            double sum = 0;
            
            for (int y = -radius; y <= radius; y++) {
                for (int x = -radius; x <= radius; x++) {
                    double value = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                    kernel[y + radius][x + radius] = value;
                    sum += value;
                }
            }
            
            // Normalize kernel
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    kernel[y][x] /= sum;
                }
            }
            
            // Apply convolution
            ConvolutionFilter convolutionFilter = new ConvolutionFilter(kernel);
            return convolutionFilter.apply(image);
        }
    }
    
    /**
     * Represents an edge detection filter
     */
    public static class EdgeDetectionFilter extends Filter {
        private final EdgeDetectionType type;
        
        public enum EdgeDetectionType {
            SOBEL, PREWITT, LAPLACIAN
        }
        
        public EdgeDetectionFilter(EdgeDetectionType type) {
            this.type = type;
        }
        
        @Override
        public Image apply(Image image) {
            double[][] kernelX, kernelY;
            
            switch (type) {
                case SOBEL:
                    kernelX = new double[][]{
                        {-1, 0, 1},
                        {-2, 0, 2},
                        {-1, 0, 1}
                    };
                    kernelY = new double[][]{
                        {-1, -2, -1},
                        { 0,  0,  0},
                        { 1,  2,  1}
                    };
                    break;
                    
                case PREWITT:
                    kernelX = new double[][]{
                        {-1, 0, 1},
                        {-1, 0, 1},
                        {-1, 0, 1}
                    };
                    kernelY = new double[][]{
                        {-1, -1, -1},
                        { 0,  0,  0},
                        { 1,  1,  1}
                    };
                    break;
                    
                case LAPLACIAN:
                    kernelX = new double[][]{
                        { 0, -1,  0},
                        {-1,  4, -1},
                        { 0, -1,  0}
                    };
                    kernelY = new double[][]{
                        { 0,  0,  0},
                        { 0,  0,  0},
                        { 0,  0,  0}
                    };
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unknown edge detection type");
            }
            
            // Convert to grayscale for edge detection
            GrayscaleImage grayImage = new GrayscaleImage(image);
            int width = grayImage.getWidth();
            int height = grayImage.getHeight();
            
            // Apply kernels
            GrayscaleImage result = grayImage.copy();
            
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    double gx = 0, gy = 0;
                    
                    // Apply kernels
                    for (int ky = -1; ky <= 1; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            int pixel = grayImage.getPixel(x + kx, y + ky);
                            gx += pixel * kernelX[ky + 1][kx + 1];
                            gy += pixel * kernelY[ky + 1][kx + 1];
                        }
                    }
                    
                    // Calculate gradient magnitude
                    int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
                    result.setPixel(x, y, magnitude);
                }
            }
            
            return result.toColorImage();
        }
    }
    
    /**
     * Represents a morphological operation filter
     */
    public static class MorphologicalFilter extends Filter {
        private final MorphologicalOperation operation;
        private final int[][] structuringElement;
        
        public enum MorphologicalOperation {
            ERODE, DILATE, OPEN, CLOSE
        }
        
        public MorphologicalFilter(MorphologicalOperation operation) {
            this(operation, createDefaultStructuringElement());
        }
        
        public MorphologicalFilter(MorphologicalOperation operation, int[][] structuringElement) {
            this.operation = operation;
            this.structuringElement = structuringElement;
        }
        
        private static int[][] createDefaultStructuringElement() {
            return new int[][]{
                {1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
            };
        }
        
        @Override
        public Image apply(Image image) {
            // Convert to grayscale
            GrayscaleImage grayImage = new GrayscaleImage(image);
            
            switch (operation) {
                case ERODE:
                    return erode(grayImage).toColorImage();
                case DILATE:
                    return dilate(grayImage).toColorImage();
                case OPEN:
                    return dilate(erode(grayImage)).toColorImage();
                case CLOSE:
                    return erode(dilate(grayImage)).toColorImage();
                default:
                    throw new IllegalArgumentException("Unknown morphological operation");
            }
        }
        
        private GrayscaleImage erode(GrayscaleImage image) {
            int width = image.getWidth();
            int height = image.getHeight();
            GrayscaleImage result = image.copy();
            
            int seHeight = structuringElement.length;
            int seWidth = structuringElement[0].length;
            int seCenterY = seHeight / 2;
            int seCenterX = seWidth / 2;
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int min = 255;
                    
                    // Apply structuring element
                    for (int seY = 0; seY < seHeight; seY++) {
                        for (int seX = 0; seX < seWidth; seX++) {
                            if (structuringElement[seY][seX] != 0) {
                                int imageY = y + seY - seCenterY;
                                int imageX = x + seX - seCenterX;
                                
                                // Handle boundary conditions
                                if (imageY >= 0 && imageY < height && imageX >= 0 && imageX < width) {
                                    int pixel = image.getPixel(imageX, imageY);
                                    min = Math.min(min, pixel);
                                }
                            }
                        }
                    }
                    
                    result.setPixel(x, y, min);
                }
            }
            
            return result;
        }
        
        private GrayscaleImage dilate(GrayscaleImage image) {
            int width = image.getWidth();
            int height = image.getHeight();
            GrayscaleImage result = image.copy();
            
            int seHeight = structuringElement.length;
            int seWidth = structuringElement[0].length;
            int seCenterY = seHeight / 2;
            int seCenterX = seWidth / 2;
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int max = 0;
                    
                    // Apply structuring element
                    for (int seY = 0; seY < seHeight; seY++) {
                        for (int seX = 0; seX < seWidth; seX++) {
                            if (structuringElement[seY][seX] != 0) {
                                int imageY = y + seY - seCenterY;
                                int imageX = x + seX - seCenterX;
                                
                                // Handle boundary conditions
                                if (imageY >= 0 && imageY < height && imageX >= 0 && imageX < width) {
                                    int pixel = image.getPixel(imageX, imageY);
                                    max = Math.max(max, pixel);
                                }
                            }
                        }
                    }
                    
                    result.setPixel(x, y, max);
                }
            }
            
            return result;
        }
    }
    
    /**
     * Represents a feature detector
     */
    public abstract static class FeatureDetector {
        /**
         * Detects features in an image
         */
        public abstract List<Feature> detect(Image image);
    }
    
    /**
     * Represents a corner detector using Harris corner detection
     */
    public static class HarrisCornerDetector extends FeatureDetector {
        private final double threshold;
        private final int blockSize;
        private final double k;
        
        public HarrisCornerDetector() {
            this(0.01, 2, 0.04);
        }
        
        public HarrisCornerDetector(double threshold, int blockSize, double k) {
            this.threshold = threshold;
            this.blockSize = blockSize;
            this.k = k;
        }
        
        @Override
        public List<Feature> detect(Image image) {
            // Convert to grayscale
            GrayscaleImage grayImage = new GrayscaleImage(image);
            int width = grayImage.getWidth();
            int height = grayImage.getHeight();
            List<Feature> corners = new ArrayList<>();
            
            // Calculate gradients
            double[][] ix = new double[height][width];
            double[][] iy = new double[height][width];
            
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    ix[y][x] = grayImage.getPixel(x + 1, y) - grayImage.getPixel(x - 1, y);
                    iy[y][x] = grayImage.getPixel(x, y + 1) - grayImage.getPixel(x, y - 1);
                }
            }
            
            // Calculate products of derivatives
            double[][] ixx = new double[height][width];
            double[][] iyy = new double[height][width];
            double[][] ixy = new double[height][width];
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    ixx[y][x] = ix[y][x] * ix[y][x];
                    iyy[y][x] = iy[y][x] * iy[y][x];
                    ixy[y][x] = ix[y][x] * iy[y][x];
                }
            }
            
            // Apply Gaussian smoothing
            double sigma = 1.0;
            int radius = (int) Math.ceil(sigma * 3);
            double[][] gaussianKernel = createGaussianKernel(sigma, radius);
            
            ixx = applyGaussianFilter(ixx, gaussianKernel);
            iyy = applyGaussianFilter(iyy, gaussianKernel);
            ixy = applyGaussianFilter(ixy, gaussianKernel);
            
            // Calculate Harris corner response
            double[][] response = new double[height][width];
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double det = ixx[y][x] * iyy[y][x] - ixy[y][x] * ixy[y][x];
                    double trace = ixx[y][x] + iyy[y][x];
                    response[y][x] = det - k * trace * trace;
                }
            }
            
            // Find local maxima above threshold
            double maxResponse = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    maxResponse = Math.max(maxResponse, response[y][x]);
                }
            }
            
            double thresholdValue = threshold * maxResponse;
            
            // Non-maximum suppression
            for (int y = blockSize; y < height - blockSize; y++) {
                for (int x = blockSize; x < width - blockSize; x++) {
                    if (response[y][x] > thresholdValue) {
                        boolean isMaximum = true;
                        
                        // Check if it's a local maximum
                        for (int dy = -blockSize; dy <= blockSize; dy++) {
                            for (int dx = -blockSize; dx <= blockSize; dx++) {
                                if (dy == 0 && dx == 0) continue;
                                if (response[y + dy][x + dx] > response[y][x]) {
                                    isMaximum = false;
                                    break;
                                }
                            }
                            if (!isMaximum) break;
                        }
                        
                        if (isMaximum) {
                            corners.add(new Feature(x, y, response[y][x]));
                        }
                    }
                }
            }
            
            return corners;
        }
        
        private double[][] createGaussianKernel(double sigma, int radius) {
            int size = 2 * radius + 1;
            double[][] kernel = new double[size][size];
            double sum = 0;
            
            for (int y = -radius; y <= radius; y++) {
                for (int x = -radius; x <= radius; x++) {
                    double value = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                    kernel[y + radius][x + radius] = value;
                    sum += value;
                }
            }
            
            // Normalize
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    kernel[y][x] /= sum;
                }
            }
            
            return kernel;
        }
        
        private double[][] applyGaussianFilter(double[][] image, double[][] kernel) {
            int height = image.length;
            int width = image[0].length;
            double[][] result = new double[height][width];
            
            int kernelHeight = kernel.length;
            int kernelWidth = kernel[0].length;
            int kernelCenterY = kernelHeight / 2;
            int kernelCenterX = kernelWidth / 2;
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double sum = 0;
                    
                    for (int ky = 0; ky < kernelHeight; ky++) {
                        for (int kx = 0; kx < kernelWidth; kx++) {
                            int imageY = y + ky - kernelCenterY;
                            int imageX = x + kx - kernelCenterX;
                            
                            // Handle boundary conditions
                            if (imageY >= 0 && imageY < height && imageX >= 0 && imageX < width) {
                                sum += image[imageY][imageX] * kernel[ky][kx];
                            }
                        }
                    }
                    
                    result[y][x] = sum;
                }
            }
            
            return result;
        }
    }
    
    /**
     * Represents a feature detected in an image
     */
    public static class Feature {
        private final int x;
        private final int y;
        private final double response;
        
        public Feature(int x, int y, double response) {
            this.x = x;
            this.y = y;
            this.response = response;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public double getResponse() {
            return response;
        }
        
        @Override
        public String toString() {
            return "Feature{x=" + x + ", y=" + y + ", response=" + response + "}";
        }
    }
    
    /**
     * Represents an object detector
     */
    public abstract static class ObjectDetector {
        /**
         * Detects objects in an image
         */
        public abstract List<DetectedObject> detect(Image image);
    }
    
    /**
     * Represents a simple template matching object detector
     */
    public static class TemplateMatchingDetector extends ObjectDetector {
        private final Image template;
        private final double threshold;
        
        public TemplateMatchingDetector(Image template) {
            this(template, 0.8);
        }
        
        public TemplateMatchingDetector(Image template, double threshold) {
            this.template = template;
            this.threshold = threshold;
        }
        
        @Override
        public List<DetectedObject> detect(Image image) {
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int templateWidth = template.getWidth();
            int templateHeight = template.getHeight();
            List<DetectedObject> objects = new ArrayList<>();
            
            // Convert to grayscale for matching
            GrayscaleImage grayImage = new GrayscaleImage(image);
            GrayscaleImage grayTemplate = new GrayscaleImage(template);
            
            // Normalize template
            double templateMean = 0;
            for (int y = 0; y < templateHeight; y++) {
                for (int x = 0; x < templateWidth; x++) {
                    templateMean += grayTemplate.getPixel(x, y);
                }
            }
            templateMean /= (templateWidth * templateHeight);
            
            // Template matching using normalized cross-correlation
            for (int y = 0; y <= imageHeight - templateHeight; y++) {
                for (int x = 0; x <= imageWidth - templateWidth; x++) {
                    double correlation = calculateNormalizedCrossCorrelation(
                        grayImage, grayTemplate, x, y, templateMean);
                    
                    if (correlation > threshold) {
                        objects.add(new DetectedObject(x, y, templateWidth, templateHeight, correlation));
                    }
                }
            }
            
            return objects;
        }
        
        private double calculateNormalizedCrossCorrelation(
                GrayscaleImage image, GrayscaleImage template, int offsetX, int offsetY, double templateMean) {
            int templateWidth = template.getWidth();
            int templateHeight = template.getHeight();
            
            // Calculate image patch mean
            double imageMean = 0;
            for (int y = 0; y < templateHeight; y++) {
                for (int x = 0; x < templateWidth; x++) {
                    imageMean += image.getPixel(offsetX + x, offsetY + y);
                }
            }
            imageMean /= (templateWidth * templateHeight);
            
            // Calculate numerator and denominators
            double numerator = 0;
            double imageDenominator = 0;
            double templateDenominator = 0;
            
            for (int y = 0; y < templateHeight; y++) {
                for (int x = 0; x < templateWidth; x++) {
                    int imagePixel = image.getPixel(offsetX + x, offsetY + y);
                    int templatePixel = template.getPixel(x, y);
                    
                    double imageDiff = imagePixel - imageMean;
                    double templateDiff = templatePixel - templateMean;
                    
                    numerator += imageDiff * templateDiff;
                    imageDenominator += imageDiff * imageDiff;
                    templateDenominator += templateDiff * templateDiff;
                }
            }
            
            // Avoid division by zero
            if (imageDenominator == 0 || templateDenominator == 0) {
                return 0;
            }
            
            return numerator / Math.sqrt(imageDenominator * templateDenominator);
        }
    }
    
    /**
     * Represents a detected object in an image
     */
    public static class DetectedObject {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final double confidence;
        
        public DetectedObject(int x, int y, int width, int height, double confidence) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.confidence = confidence;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        @Override
        public String toString() {
            return "DetectedObject{x=" + x + ", y=" + y + ", width=" + width + 
                   ", height=" + height + ", confidence=" + confidence + "}";
        }
    }
    
    /**
     * Represents an image descriptor for feature matching
     */
    public abstract static class ImageDescriptor {
        /**
         * Computes the descriptor for a feature
         */
        public abstract double[] compute(Image image, Feature feature);
    }
    
    /**
     * Represents a simple histogram of oriented gradients (HOG) descriptor
     */
    public static class HOGDescriptor extends ImageDescriptor {
        private final int cellSize;
        private final int blockSize;
        private final int numBins;
        
        public HOGDescriptor() {
            this(8, 2, 9);
        }
        
        public HOGDescriptor(int cellSize, int blockSize, int numBins) {
            this.cellSize = cellSize;
            this.blockSize = blockSize;
            this.numBins = numBins;
        }
        
        @Override
        public double[] compute(Image image, Feature feature) {
            // Convert to grayscale
            GrayscaleImage grayImage = new GrayscaleImage(image);
            
            // Calculate gradients
            int width = grayImage.getWidth();
            int height = grayImage.getHeight();
            double[][] gradientMagnitude = new double[height][width];
            double[][] gradientAngle = new double[height][width];
            
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    int gx = grayImage.getPixel(x + 1, y) - grayImage.getPixel(x - 1, y);
                    int gy = grayImage.getPixel(x, y + 1) - grayImage.getPixel(x, y - 1);
                    
                    gradientMagnitude[y][x] = Math.sqrt(gx * gx + gy * gy);
                    gradientAngle[y][x] = Math.atan2(gy, gx);
                }
            }
            
            // Calculate histogram for each cell
            int cellsX = width / cellSize;
            int cellsY = height / cellSize;
            double[][][] histograms = new double[cellsY][cellsX][numBins];
            
            for (int cy = 0; cy < cellsY; cy++) {
                for (int cx = 0; cx < cellsX; cx++) {
                    double[] histogram = new double[numBins];
                    
                    // Calculate histogram for this cell
                    for (int y = cy * cellSize; y < (cy + 1) * cellSize && y < height; y++) {
                        for (int x = cx * cellSize; x < (cx + 1) * cellSize && x < width; x++) {
                            double magnitude = gradientMagnitude[y][x];
                            double angle = gradientAngle[y][x];
                            
                            // Convert angle to bin index (0 to 180 degrees)
                            int bin = (int) ((angle + Math.PI) / (Math.PI / numBins)) % numBins;
                            histogram[bin] += magnitude;
                        }
                    }
                    
                    histograms[cy][cx] = histogram;
                }
            }
            
            // Normalize histograms in blocks
            int blocksX = (cellsX - blockSize + 1);
            int blocksY = (cellsY - blockSize + 1);
            double[] descriptor = new double[blocksY * blocksX * blockSize * blockSize * numBins];
            int descriptorIndex = 0;
            
            for (int by = 0; by < blocksY; by++) {
                for (int bx = 0; bx < blocksX; bx++) {
                    // Collect histograms for this block
                    double[] blockHistogram = new double[blockSize * blockSize * numBins];
                    int blockIndex = 0;
                    
                    for (int dy = 0; dy < blockSize; dy++) {
                        for (int dx = 0; dx < blockSize; dx++) {
                            int cy = by + dy;
                            int cx = bx + dx;
                            
                            System.arraycopy(histograms[cy][cx], 0, blockHistogram, blockIndex, numBins);
                            blockIndex += numBins;
                        }
                    }
                    
                    // Normalize block histogram
                    double norm = 0;
                    for (double value : blockHistogram) {
                        norm += value * value;
                    }
                    norm = Math.sqrt(norm);
                    
                    if (norm > 0) {
                        for (int i = 0; i < blockHistogram.length; i++) {
                            blockHistogram[i] /= norm;
                        }
                    }
                    
                    // Add to descriptor
                    System.arraycopy(blockHistogram, 0, descriptor, descriptorIndex, blockHistogram.length);
                    descriptorIndex += blockHistogram.length;
                }
            }
            
            return descriptor;
        }
    }
    
    /**
     * Represents a feature matcher for matching descriptors
     */
    public static class FeatureMatcher {
        private final double threshold;
        
        public FeatureMatcher() {
            this(0.8);
        }
        
        public FeatureMatcher(double threshold) {
            this.threshold = threshold;
        }
        
        /**
         * Matches features between two images using their descriptors
         */
        public List<Match> match(Image image1, Image image2, 
                                FeatureDetector detector, ImageDescriptor descriptor) {
            // Detect features in both images
            List<Feature> features1 = detector.detect(image1);
            List<Feature> features2 = detector.detect(image2);
            
            // Compute descriptors for all features
            List<double[]> descriptors1 = new ArrayList<>();
            List<double[]> descriptors2 = new ArrayList<>();
            
            for (Feature feature : features1) {
                descriptors1.add(descriptor.compute(image1, feature));
            }
            
            for (Feature feature : features2) {
                descriptors2.add(descriptor.compute(image2, feature));
            }
            
            // Match features using nearest neighbor matching
            List<Match> matches = new ArrayList<>();
            
            for (int i = 0; i < features1.size(); i++) {
                double[] desc1 = descriptors1.get(i);
                int bestIndex = -1;
                double bestDistance = Double.MAX_VALUE;
                double secondBestDistance = Double.MAX_VALUE;
                
                // Find two nearest neighbors
                for (int j = 0; j < features2.size(); j++) {
                    double[] desc2 = descriptors2.get(j);
                    double distance = calculateEuclideanDistance(desc1, desc2);
                    
                    if (distance < bestDistance) {
                        secondBestDistance = bestDistance;
                        bestDistance = distance;
                        bestIndex = j;
                    } else if (distance < secondBestDistance) {
                        secondBestDistance = distance;
                    }
                }
                
                // Apply ratio test
                if (bestIndex != -1 && bestDistance < threshold * secondBestDistance) {
                    matches.add(new Match(features1.get(i), features2.get(bestIndex), bestDistance));
                }
            }
            
            return matches;
        }
        
        private double calculateEuclideanDistance(double[] a, double[] b) {
            if (a.length != b.length) {
                throw new IllegalArgumentException("Descriptor arrays must have same length");
            }
            
            double sum = 0;
            for (int i = 0; i < a.length; i++) {
                double diff = a[i] - b[i];
                sum += diff * diff;
            }
            return Math.sqrt(sum);
        }
    }
    
    /**
     * Represents a matched feature pair
     */
    public static class Match {
        private final Feature feature1;
        private final Feature feature2;
        private final double distance;
        
        public Match(Feature feature1, Feature feature2, double distance) {
            this.feature1 = feature1;
            this.feature2 = feature2;
            this.distance = distance;
        }
        
        public Feature getFeature1() {
            return feature1;
        }
        
        public Feature getFeature2() {
            return feature2;
        }
        
        public double getDistance() {
            return distance;
        }
        
        @Override
        public String toString() {
            return "Match{feature1=" + feature1 + ", feature2=" + feature2 + ", distance=" + distance + "}";
        }
    }
    
    /**
     * Represents an image segmentation algorithm
     */
    public abstract static class ImageSegmentation {
        /**
         * Segments an image into regions
         */
        public abstract List<Segment> segment(Image image);
    }
    
    /**
     * Represents a simple k-means clustering segmentation
     */
    public static class KMeansSegmentation extends ImageSegmentation {
        private final int k;
        private final int maxIterations;
        
        public KMeansSegmentation(int k) {
            this(k, 100);
        }
        
        public KMeansSegmentation(int k, int maxIterations) {
            this.k = k;
            this.maxIterations = maxIterations;
        }
        
        @Override
        public List<Segment> segment(Image image) {
            int width = image.getWidth();
            int height = image.getHeight();
            int numPixels = width * height;
            
            // Convert image to feature vectors (RGB values)
            double[][] pixels = new double[numPixels][3];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int[] rgb = image.getRGB(x, y);
                    pixels[y * width + x][0] = rgb[0];
                    pixels[y * width + x][1] = rgb[1];
                    pixels[y * width + x][2] = rgb[2];
                }
            }
            
            // Initialize centroids randomly
            double[][] centroids = new double[k][3];
            Random random = new Random();
            for (int i = 0; i < k; i++) {
                centroids[i][0] = random.nextInt(256);
                centroids[i][1] = random.nextInt(256);
                centroids[i][2] = random.nextInt(256);
            }
            
            // Assign pixels to clusters
            int[] assignments = new int[numPixels];
            
            // K-means iterations
            for (int iteration = 0; iteration < maxIterations; iteration++) {
                // Assign each pixel to the nearest centroid
                for (int i = 0; i < numPixels; i++) {
                    double minDistance = Double.MAX_VALUE;
                    int bestCluster = 0;
                    
                    for (int j = 0; j < k; j++) {
                        double distance = calculateEuclideanDistance(pixels[i], centroids[j]);
                        if (distance < minDistance) {
                            minDistance = distance;
                            bestCluster = j;
                        }
                    }
                    
                    assignments[i] = bestCluster;
                }
                
                // Update centroids
                double[][] newCentroids = new double[k][3];
                int[] counts = new int[k];
                
                for (int i = 0; i < numPixels; i++) {
                    int cluster = assignments[i];
                    newCentroids[cluster][0] += pixels[i][0];
                    newCentroids[cluster][1] += pixels[i][1];
                    newCentroids[cluster][2] += pixels[i][2];
                    counts[cluster]++;
                }
                
                boolean converged = true;
                for (int j = 0; j < k; j++) {
                    if (counts[j] > 0) {
                        double[] oldCentroid = centroids[j].clone();
                        centroids[j][0] = newCentroids[j][0] / counts[j];
                        centroids[j][1] = newCentroids[j][1] / counts[j];
                        centroids[j][2] = newCentroids[j][2] / counts[j];
                        
                        // Check for convergence
                        if (calculateEuclideanDistance(oldCentroid, centroids[j]) > 1e-6) {
                            converged = false;
                        }
                    }
                }
                
                if (converged) {
                    break;
                }
            }
            
            // Create segments
            List<Segment> segments = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                segments.add(new Segment(i, new ArrayList<>(), 
                    (int) centroids[i][0], (int) centroids[i][1], (int) centroids[i][2]));
            }
            
            // Assign pixels to segments
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixelIndex = y * width + x;
                    int cluster = assignments[pixelIndex];
                    segments.get(cluster).addPixel(new Point(x, y));
                }
            }
            
            return segments;
        }
        
        private double calculateEuclideanDistance(double[] a, double[] b) {
            double sum = 0;
            for (int i = 0; i < a.length; i++) {
                double diff = a[i] - b[i];
                sum += diff * diff;
            }
            return Math.sqrt(sum);
        }
    }
    
    /**
     * Represents a segmented region of an image
     */
    public static class Segment {
        private final int id;
        private final List<Point> pixels;
        private final int averageRed;
        private final int averageGreen;
        private final int averageBlue;
        
        public Segment(int id, List<Point> pixels, int averageRed, int averageGreen, int averageBlue) {
            this.id = id;
            this.pixels = new ArrayList<>(pixels);
            this.averageRed = averageRed;
            this.averageGreen = averageGreen;
            this.averageBlue = averageBlue;
        }
        
        public int getId() {
            return id;
        }
        
        public List<Point> getPixels() {
            return new ArrayList<>(pixels);
        }
        
        public void addPixel(Point pixel) {
            pixels.add(pixel);
        }
        
        public int getAverageRed() {
            return averageRed;
        }
        
        public int getAverageGreen() {
            return averageGreen;
        }
        
        public int getAverageBlue() {
            return averageBlue;
        }
        
        public int getSize() {
            return pixels.size();
        }
        
        @Override
        public String toString() {
            return "Segment{id=" + id + ", size=" + pixels.size() + ", color=(" + 
                   averageRed + "," + averageGreen + "," + averageBlue + ")}";
        }
    }
}