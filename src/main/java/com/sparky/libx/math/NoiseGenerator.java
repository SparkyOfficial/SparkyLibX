package com.sparky.libx.math;

import java.util.Random;

/**
 * Генератор шума для создания процедурных текстур и ландшафтов
 */
public class NoiseGenerator {
    private final long seed;
    private final Random random;
    
    public NoiseGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }
    
    /**
     * Генерирует перлин шум в точке
     */
    public double perlinNoise(double x, double y) {
        return perlinNoise(x, y, 0);
    }
    
    /**
     * Генерирует 3D перлин шум в точке
     */
    public double perlinNoise(double x, double y, double z) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;
        int Z = (int)Math.floor(z) & 255;
        
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);
        
        double u = fade(x);
        double v = fade(y);
        double w = fade(z);
        
        int A = p[X] + Y;
        int AA = p[A] + Z;
        int AB = p[A + 1] + Z;
        int B = p[X + 1] + Y;
        int BA = p[B] + Z;
        int BB = p[B + 1] + Z;
        
        return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z),
                grad(p[BA], x - 1, y, z)),
                lerp(u, grad(p[AB], x, y - 1, z),
                grad(p[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(p[AA + 1], x, y, z - 1),
                grad(p[BA + 1], x - 1, y, z - 1)),
                lerp(u, grad(p[AB + 1], x, y - 1, z - 1),
                grad(p[BB + 1], x - 1, y - 1, z - 1))));
    }
    
    /**
     * Генерирует фрактальный броуновский шум (FBM)
     */
    public double fractalBrownianMotion(double x, double y, int octaves, double persistence, double scale) {
        double total = 0;
        double frequency = scale;
        double amplitude = 1;
        double maxValue = 0;
        
        for (int i = 0; i < octaves; i++) {
            total += perlinNoise(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        
        return total / maxValue;
    }
    
    /**
     * Генерирует вороной шум
     */
    public double voronoiNoise(double x, double y, int points) {
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < points; i++) {
            double px = (x + i * 137.5) % 1.0;
            double py = (y + i * 79.3) % 1.0;
            
            double dx = x - px;
            double dy = y - py;
            double distance = dx * dx + dy * dy;
            
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        
        return Math.sqrt(minDistance);
    }
    
    /**
     * Генерирует волновой шум
     */
    public double waveNoise(double x, double y, double frequency, double amplitude) {
        return amplitude * Math.sin(frequency * x) * Math.cos(frequency * y);
    }
    
    /**
     * Генерирует спиральный шум
     */
    public double spiralNoise(double x, double y, int turns) {
        double angle = Math.atan2(y, x);
        double radius = Math.sqrt(x * x + y * y);
        
        return Math.sin(turns * angle + radius);
    }
    
    /**
     * Генерирует шахматную доску
     */
    public double checkerboard(double x, double y, double size) {
        int xi = (int) Math.floor(x / size);
        int yi = (int) Math.floor(y / size);
        return (xi + yi) % 2 == 0 ? 1.0 : -1.0;
    }
    
    private static final int[] p = new int[512];
    
    static {
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        
        for (int i = 0; i < 256; i++) {
            int j = (int) (Math.random() * 256);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }
        
        for (int i = 0; i < 256; i++) {
            p[256 + i] = p[i];
        }
    }
    
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }
    
    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}