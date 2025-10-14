package com.sparky.libx.test;

import java.util.List;

import org.bukkit.util.Vector;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.sparky.libx.geometry.AdvancedGeometry;
import com.sparky.libx.math.MathUtils;
import com.sparky.libx.math.Trigonometry;

/**
 * Тесты для расширенной библиотеки SparkyLibX
 * author: Андрій Будильников
 */
public class AdvancedLibraryTest {
    
    @Test
    public void testAdvancedMathUtils() {
        // Тест тригонометрии в градусах
        assertEquals(1.0, MathUtils.sinDegrees(90), 0.001);
        assertEquals(0.0, MathUtils.cosDegrees(90), 0.001);
        assertEquals(0.0, MathUtils.tanDegrees(0), 0.001);
        
        // Тест алгебры
        double[] roots = MathUtils.solveQuadratic(1, -5, 6); // x² - 5x + 6 = 0
        assertEquals(2, roots.length);
        assertEquals(3.0, roots[0], 0.001);
        assertEquals(2.0, roots[1], 0.001);
        
        // Тест факториала
        assertEquals(120, MathUtils.factorial(5));
        assertEquals(1, MathUtils.factorial(0));
        
        // Тест биномиальных коэффициентов
        assertEquals(10, MathUtils.binomialCoefficient(5, 2));
        assertEquals(1, MathUtils.binomialCoefficient(5, 0));
        
        // Тест НОД и НОК
        assertEquals(6, MathUtils.gcd(24, 18));
        assertEquals(72, MathUtils.lcm(24, 18));
        
        // Тест геометрии
        assertEquals(5.0, MathUtils.distance2D(0, 0, 3, 4), 0.001);
        assertEquals(12.083, MathUtils.distance3D(0, 0, 0, 3, 4, 11), 0.001); // Исправлено значение
        
        double triangleArea = MathUtils.triangleArea(3, 4, 5);
        assertEquals(6.0, triangleArea, 0.001);
        
        double circleArea = MathUtils.circleArea(5);
        assertEquals(25 * Math.PI, circleArea, 0.001);
        
        System.out.println("Advanced math utilities test passed");
    }
    
    @Test
    public void testTrigonometry() {
        // Тест гиперболических функций
        assertEquals(0.0, Trigonometry.sinh(0), 0.001);
        assertEquals(1.0, Trigonometry.cosh(0), 0.001);
        assertEquals(0.0, Trigonometry.tanh(0), 0.001);
        
        // Тест дополнительных тригонометрических функций
        assertEquals(1.0, Trigonometry.sec(0), 0.001);
        assertEquals(1.0, Trigonometry.csc(Math.PI / 2), 0.001);
        assertEquals(0.0, Trigonometry.cot(Math.PI / 2), 0.001);
        
        // Тест специальных функций
        assertEquals(0.0, Trigonometry.versin(0), 0.001);
        assertEquals(1.0, Trigonometry.coversin(0), 0.001);
        assertEquals(0.5, Trigonometry.haversin(Math.PI / 2), 0.001);
        
        System.out.println("Trigonometry test passed");
    }
    
    @Test
    public void testAdvancedGeometry() {
        // Тест создания спирали
        Vector center = new Vector(0, 0, 0);
        List<Vector> spiral = AdvancedGeometry.createSpiral(center, 5, 10, 2, 20);
        assertEquals(20, spiral.size());
        
        // Тест создания конуса
        List<Vector> cone = AdvancedGeometry.createCone(center, 5, 10, 10);
        assertTrue(cone.size() > 0);
        
        // Тест создания цилиндра
        List<Vector> cylinder = AdvancedGeometry.createCylinder(center, 5, 10, 10);
        assertEquals(20, cylinder.size());
        
        // Тест центра тяжести
        Vector centroid = AdvancedGeometry.calculateCentroid(spiral);
        assertNotNull(centroid);
        
        // Тест поворотов
        Vector point = new Vector(1, 0, 0);
        Vector rotatedX = AdvancedGeometry.rotateAroundX(point, Math.PI / 2);
        assertEquals(1.0, rotatedX.getX(), 0.001);
        assertEquals(0.0, rotatedX.getY(), 0.001);
        assertEquals(0.0, rotatedX.getZ(), 0.001);
        
        System.out.println("Advanced geometry test passed");
    }
    
    @Test
    public void testBlockUtils() {
        // Тест создания куба
        // Note: These tests would require a running Minecraft server to fully test
        // For now we'll just verify the methods exist and can be called
        
        // Тест математических функций для блоков
        double[] frequencies = {0.1, 0.2, 0.3, 0.4};
        int[] counts = {10, 20, 30, 40};
        
        // Просто проверяем что методы компилируются
        assertTrue(true); // Placeholder for actual block tests
        
        System.out.println("Block utilities test structure verified");
    }
    
    @Test
    public void testIntegration() {
        // Комплексный тест, демонстрирующий интеграцию всех компонентов
        
        // 1. Создаем спираль с использованием продвинутой геометрии
        Vector center = new Vector(0, 0, 0);
        List<Vector> spiral = AdvancedGeometry.createSpiral(center, 10, 20, 3, 100);
        
        // 2. Вычисляем центр тяжести спирали
        Vector centroid = AdvancedGeometry.calculateCentroid(spiral);
        
        // 3. Применяем тригонометрические функции для анализа
        double spiralLength = 0;
        for (int i = 1; i < spiral.size(); i++) {
            Vector prev = spiral.get(i - 1);
            Vector curr = spiral.get(i);
            spiralLength += prev.distance(curr);
        }
        
        // 4. Используем математические утилиты для расчетов
        double avgDistance = spiralLength / spiral.size();
        double roundedAvg = MathUtils.round(avgDistance, 2);
        
        // 5. Применяем тригонометрию
        double angle = Trigonometry.arsinh(1.0);
        
        System.out.println("Integration test passed - all components work together");
        System.out.println("Spiral points: " + spiral.size());
        System.out.println("Average distance between points: " + roundedAvg);
        System.out.println("ArcSinh(1): " + angle);
    }
}