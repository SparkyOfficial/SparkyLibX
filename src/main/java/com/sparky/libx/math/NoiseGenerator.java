package com.sparky.libx.math;

import java.util.Random;

/**
 * Генератор шума и математических паттернов для создания сложных структур в Minecraft
 * Поддерживает различные типы шума и математические функции
 */
public class NoiseGenerator {
    
    private static final int[] PERMUTATION = new int[512];
    private static final double PI2 = Math.PI * 2;
    
    static {
        // Инициализация перестановочного массива для Perlin шума
        int[] p = {151,160,137,91,90,15,131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
                   190,6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,88,237,149,56,87,174,20,125,136,171,168,68,175,
                   74,165,71,134,139,48,27,166,77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,102,143,54,65,25,
                   63,161,1,216,80,73,209,76,132,187,208,89,18,169,200,196,135,130,116,188,159,86,164,100,109,198,173,186,3,64,52,217,226,250,
                   124,123,5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,223,183,170,213,119,248,152,2,44,154,
                   163,70,221,153,101,155,167,43,172,9,129,22,39,253,19,98,108,110,79,113,224,232,178,185,112,104,218,246,97,228,251,34,242,
                   193,238,210,144,12,191,179,162,241,81,51,145,235,249,14,239,107,49,192,214,31,181,199,106,157,184,84,204,176,115,121,50,
                   45,127,4,150,254,138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180};
        
        for (int i = 0; i < 512; i++) {
            PERMUTATION[i] = p[i & 255];
        }
    }
    
    /**
     * Генерирует шум Перлина в заданной точке
     * @param x координата X
     * @param y координата Y
     * @param z координата Z
     * @return значение шума Перлина в диапазоне [-1, 1]
     */
    public static double perlinNoise(double x, double y, double z) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;
        int Z = (int)Math.floor(z) & 255;
        
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);
        
        double u = fade(x);
        double v = fade(y);
        double w = fade(z);
        
        int A = PERMUTATION[X] + Y;
        int AA = PERMUTATION[A] + Z;
        int AB = PERMUTATION[A + 1] + Z;
        int B = PERMUTATION[X + 1] + Y;
        int BA = PERMUTATION[B] + Z;
        int BB = PERMUTATION[B + 1] + Z;
        
        return lerp(w, lerp(v, lerp(u, grad(PERMUTATION[AA], x, y, z),
                grad(PERMUTATION[BA], x - 1, y, z)),
                lerp(u, grad(PERMUTATION[AB], x, y - 1, z),
                grad(PERMUTATION[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(PERMUTATION[AA + 1], x, y, z - 1),
                grad(PERMUTATION[BA + 1], x - 1, y, z - 1)),
                lerp(u, grad(PERMUTATION[AB + 1], x, y - 1, z - 1),
                grad(PERMUTATION[BB + 1], x - 1, y - 1, z - 1))));
    }
    
    /**
     * Генерирует фрактальный шум Перлина (FBM - Fractional Brownian Motion)
     * @param x координата X
     * @param y координата Y
     * @param z координата Z
     * @param octaves количество октав
     * @param persistence степень сохранения амплитуды между октавами
     * @param scale масштаб шума
     * @return значение фрактального шума
     */
    public static double fractalNoise(double x, double y, double z, int octaves, double persistence, double scale) {
        double total = 0;
        double frequency = scale;
        double amplitude = 1;
        double maxValue = 0;
        
        for (int i = 0; i < octaves; i++) {
            total += perlinNoise(x * frequency, y * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        
        return total / maxValue;
    }
    
    /**
     * Генерирует шум Вороного (Voronoi/Cellular noise)
     * @param x координата X
     * @param y координата Y
     * @param z координата Z
     * @param scale масштаб
     * @return значение шума Вороного
     */
    public static double voronoiNoise(double x, double y, double z, double scale) {
        x /= scale;
        y /= scale;
        z /= scale;
        
        int xi = (int)Math.floor(x);
        int yi = (int)Math.floor(y);
        int zi = (int)Math.floor(z);
        
        double distance = Double.MAX_VALUE;
        
        for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int nx = xi + dx;
                    int ny = yi + dy;
                    int nz = zi + dz;
                    
                    // Генерируем псевдослучайное значение на основе координат ячейки
                    Random random = new Random(nx * 37 + ny * 73 + nz * 137);
                    double px = nx + random.nextDouble();
                    double py = ny + random.nextDouble();
                    double pz = nz + random.nextDouble();
                    
                    double dx2 = px - x;
                    double dy2 = py - y;
                    double dz2 = pz - z;
                    double dist = Math.sqrt(dx2 * dx2 + dy2 * dy2 + dz2 * dz2);
                    
                    if (dist < distance) {
                        distance = dist;
                    }
                }
            }
        }
        
        return distance;
    }
    
    /**
     * Генерирует волновой шум (Wave noise)
     * @param x координата X
     * @param y координата Y
     * @param z координата Z
     * @param frequency частота волны
     * @param amplitude амплитуда волны
     * @return значение волнового шума
     */
    public static double waveNoise(double x, double y, double z, double frequency, double amplitude) {
        return amplitude * Math.sin(PI2 * frequency * x) * 
               Math.cos(PI2 * frequency * y) * 
               Math.sin(PI2 * frequency * z);
    }
    
    /**
     * Генерирует спиральный шум
     * @param x координата X
     * @param y координата Y
     * @param turns количество оборотов
     * @param frequency частота
     * @return значение спирального шума
     */
    public static double spiralNoise(double x, double y, double turns, double frequency) {
        double angle = Math.atan2(y, x);
        double radius = Math.sqrt(x * x + y * y);
        return Math.sin(angle * turns + radius * frequency);
    }
    
    /**
     * Генерирует шахматную доску
     * @param x координата X
     * @param y координата Y
     * @param size размер клетки
     * @return 0 или 1 в зависимости от положения на доске
     */
    public static int checkerboard(int x, int y, int size) {
        return ((x / size) + (y / size)) % 2;
    }
    
    /**
     * Генерирует случайный шум
     * @param x координата X
     * @param y координата Y
     * @param z координата Z
     * @param seed сид для генерации
     * @return случайное значение от 0 до 1
     */
    public static double randomNoise(int x, int y, int z, long seed) {
        Random random = new Random(x * 73856093 ^ y * 19349663 ^ z * 83492791 ^ seed);
        return random.nextDouble();
    }
    
    // Вспомогательные методы
    
    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }
    
    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}