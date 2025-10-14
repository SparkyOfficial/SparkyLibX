package com.sparky.libx.test;

import org.junit.jupiter.api.Test;

import com.sparky.libx.math.NoiseGenerator;
import com.sparky.libx.math.Vector3D;

/**
 * Тесты для проверки генерации паттернов и работы с блоками
 */
public class PatternGenerationTest {
    
    @Test
    public void testNoiseGenerator() {
        // Тестируем генератор шума Перлина
        double noise1 = NoiseGenerator.perlinNoise(0.5, 0.5, 0.5);
        double noise2 = NoiseGenerator.perlinNoise(1.0, 1.0, 1.0);
        
        // Проверяем, что значения шума находятся в правильном диапазоне
        assert noise1 >= -1.0 && noise1 <= 1.0;
        assert noise2 >= -1.0 && noise2 <= 1.0;
        
        System.out.println("Шум Перлина (0.5, 0.5, 0.5): " + noise1);
        System.out.println("Шум Перлина (1.0, 1.0, 1.0): " + noise2);
        
        // Тестируем фрактальный шум
        double fractalNoise = NoiseGenerator.fractalNoise(0.5, 0.5, 0.5, 3, 0.5, 0.01);
        assert fractalNoise >= -1.0 && fractalNoise <= 1.0;
        System.out.println("Фрактальный шум: " + fractalNoise);
        
        // Тестируем шум Вороного
        double voronoiNoise = NoiseGenerator.voronoiNoise(0.5, 0.5, 0.5, 1.0);
        assert voronoiNoise >= 0;
        System.out.println("Шум Вороного: " + voronoiNoise);
        
        // Тестируем волновой шум
        double waveNoise = NoiseGenerator.waveNoise(0.5, 0.5, 0.5, 2.0, 1.0);
        assert waveNoise >= -1.0 && waveNoise <= 1.0;
        System.out.println("Волновой шум: " + waveNoise);
        
        // Тестируем спиральный шум
        double spiralNoise = NoiseGenerator.spiralNoise(0.5, 0.5, 5.0, 2.0);
        assert spiralNoise >= -1.0 && spiralNoise <= 1.0;
        System.out.println("Спиральный шум: " + spiralNoise);
        
        // Тестируем шахматную доску
        int checkerboardValue = NoiseGenerator.checkerboard(5, 10, 3);
        assert checkerboardValue == 0 || checkerboardValue == 1;
        System.out.println("Шахматная доска (5, 10, 3): " + checkerboardValue);
    }
    
    @Test
    public void testVector3D() {
        // Тестируем класс Vector3D
        Vector3D vec1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D vec2 = new Vector3D(4.0, 5.0, 6.0);
        
        // Проверяем геттеры
        assert vec1.getX() == 1.0;
        assert vec1.getY() == 2.0;
        assert vec1.getZ() == 3.0;
        
        // Тестируем линейную интерполяцию
        Vector3D lerped = vec1.lerp(vec2, 0.5);
        assert lerped.getX() == 2.5;
        assert lerped.getY() == 3.5;
        assert lerped.getZ() == 4.5;
        
        // Тестируем расстояние
        double distance = vec1.distance(vec2);
        assert Math.abs(distance - Math.sqrt(27)) < 0.0001;
        
        System.out.println("Vector3D тесты пройдены успешно");
    }
}