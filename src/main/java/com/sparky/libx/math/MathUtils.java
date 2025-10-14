package com.sparky.libx.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Утилиты для математических операций
 */
public class MathUtils {
    
    private static final Random random = new Random();
    
    /**
     * Вычисляет наибольший общий делитель двух чисел
     */
    public static long gcd(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    
    /**
     * Вычисляет наименьшее общее кратное двух чисел
     */
    public static long lcm(long a, long b) {
        return Math.abs(a * b) / gcd(a, b);
    }
    
    /**
     * Проверяет, является ли число простым
     */
    public static boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        
        for (long i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Генерирует список простых чисел до заданного предела (решето Эратосфена)
     */
    public static List<Long> sieveOfEratosthenes(long limit) {
        List<Long> primes = new ArrayList<>();
        if (limit < 2) return primes;
        
        boolean[] isPrime = new boolean[(int)limit + 1];
        for (int i = 2; i <= limit; i++) {
            isPrime[i] = true;
        }
        
        for (int i = 2; i * i <= limit; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j <= limit; j += i) {
                    isPrime[j] = false;
                }
            }
        }
        
        for (int i = 2; i <= limit; i++) {
            if (isPrime[i]) {
                primes.add((long)i);
            }
        }
        
        return primes;
    }
    
    /**
     * Вычисляет факториал числа
     */
    public static long factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("Факториал определен только для неотрицательных чисел");
        if (n > 20) throw new IllegalArgumentException("Результат слишком велик для long");
        
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
    
    /**
     * Вычисляет биномиальный коэффициент C(n, k)
     */
    public static long binomialCoefficient(int n, int k) {
        if (k > n || k < 0) return 0;
        if (k == 0 || k == n) return 1;
        
        k = Math.min(k, n - k);
        long result = 1;
        
        for (int i = 0; i < k; i++) {
            result = result * (n - i) / (i + 1);
        }
        
        return result;
    }
    
    /**
     * Округляет число до заданного количества знаков после запятой
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Количество знаков должно быть неотрицательным");
        
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    /**
     * Генерирует случайное число в диапазоне [min, max]
     */
    public static int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
    
    /**
     * Генерирует случайное число с плавающей точкой в диапазоне [min, max]
     */
    public static double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
    
    /**
     * Преобразует градусы в радианы
     */
    public static double toRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }
    
    /**
     * Преобразует радианы в градусы
     */
    public static double toDegrees(double radians) {
        return radians * 180.0 / Math.PI;
    }
    
    /**
     * Вычисляет расстояние между двумя точками в 2D пространстве
     */
    public static double distance2D(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Вычисляет расстояние между двумя точками в 3D пространстве
     */
    public static double distance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * Проверяет, находятся ли две точки в пределах заданного расстояния
     */
    public static boolean withinDistance(double x1, double y1, double x2, double y2, double distance) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy <= distance * distance;
    }
}