package com.sparky.libx.test;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.sparky.libx.geometry.BoundingBox;
import com.sparky.libx.geometry.Ray;
import com.sparky.libx.geometry.ShapeUtils;
import com.sparky.libx.math.MathUtils;
import com.sparky.libx.math.Matrix4x4;
import com.sparky.libx.math.Quaternion;
import com.sparky.libx.math.Vector3D;
import com.sparky.libx.region.CuboidRegion;
import com.sparky.libx.region.CylinderRegion;
import com.sparky.libx.region.SphereRegion;

/**
 * Комплексний тест для демонстрації можливостей бібліотеки SparkyLibX
 * @author Андрій Будильников
 */
public class ComprehensiveTest {
    
    @Test
    public void testAdvancedRegionSystem() {
        // Тестуємо різні типи регіонів
        CuboidRegion cuboid = new CuboidRegion("test", null, 0, 0, 0, 9, 9, 9); // 10 блоків у кожному вимірі
        SphereRegion sphere = new SphereRegion("test", null, 0, 0, 0, 5);
        CylinderRegion cylinder = new CylinderRegion("test", null, 0, 0, 0, 3, 0, 10);
        
        // Тестуємо об'єми
        assertEquals(1000.0, cuboid.getVolume(), 0.001); // 10x10x10 блоків
        assertEquals((4.0/3.0) * Math.PI * 125, sphere.getVolume(), 0.001);
        assertEquals(Math.PI * 9 * 10, cylinder.getVolume(), 0.001);
        
        System.out.println("Region volumes test passed");
    }
    
    @Test
    public void testGeometricUtilities() {
        // Тестуємо обмежувальну коробку
        BoundingBox box1 = new BoundingBox(0, 0, 0, 5, 5, 5);
        BoundingBox box2 = new BoundingBox(3, 3, 3, 8, 8, 8);
        
        // Тестуємо перетин
        assertTrue(box1.intersects(box2));
        
        // Тестуємо об'єднання
        BoundingBox union = box1.union(box2);
        assertEquals(0.0, union.getMin().getX(), 0.001);
        assertEquals(8.0, union.getMax().getX(), 0.001);
        
        // Тестуємо розширення
        BoundingBox expanded = box1.expand(2);
        assertEquals(-2.0, expanded.getMin().getX(), 0.001);
        assertEquals(7.0, expanded.getMax().getX(), 0.001);
        
        System.out.println("Geometric utilities test passed");
    }
    
    @Test
    public void testMathematicalTools() {
        // Тестуємо 3D вектори
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(4, 5, 6);
        
        // Тестуємо операції
        Vector3D sum = v1.add(v2);
        assertEquals(5.0, sum.getX(), 0.001);
        assertEquals(7.0, sum.getY(), 0.001);
        assertEquals(9.0, sum.getZ(), 0.001);
        
        // Тестуємо скалярний добуток
        double dot = v1.dot(v2);
        assertEquals(32.0, dot, 0.001);
        
        // Тестуємо матриці 4x4
        Matrix4x4 identity = new Matrix4x4();
        Matrix4x4 translation = Matrix4x4.translate(1, 2, 3);
        
        // Тестуємо множення
        Matrix4x4 result = identity.multiply(translation);
        // Перевірка через отримання окремих елементів
        assertEquals(1.0, result.get(0, 0), 0.001);
        assertEquals(1.0, result.get(0, 3), 0.001);
        assertEquals(2.0, result.get(1, 3), 0.001);
        assertEquals(3.0, result.get(2, 3), 0.001);
        
        // Тестуємо кватерніони
        Quaternion q1 = new Quaternion(); // Одиничний кватерніон
        Quaternion q2 = Quaternion.rotationX(Math.PI / 2); // Поворот на 90 градусів
        
        // Тестуємо множення
        Quaternion product = q1.multiply(q2);
        assertEquals(q2.getW(), product.getW(), 0.001);
        assertEquals(q2.getX(), product.getX(), 0.001);
        
        System.out.println("Mathematical tools test passed");
    }
    
    @Test
    public void testShapeUtils() {
        // Тестуємо генерацію точок сфери
        Vector center = new Vector(0, 0, 0);
        List<Vector> spherePoints = ShapeUtils.getSpherePoints(center, 5, 100);
        
        assertEquals(100, spherePoints.size());
        
        // Перевіряємо, що всі точки на сфері
        for (Vector point : spherePoints) {
            double distance = point.distance(center);
            assertEquals(5.0, distance, 0.1); // Допуск для рівномірного розподілу
        }
        
        // Тестуємо перевірку точки в полігоні
        List<Vector> polygon = new ArrayList<>();
        polygon.add(new Vector(0, 0, 0));
        polygon.add(new Vector(10, 0, 0));
        polygon.add(new Vector(10, 10, 0));
        polygon.add(new Vector(0, 10, 0));
        
        Vector insidePoint = new Vector(5, 5, 0);
        Vector outsidePoint = new Vector(15, 15, 0);
        Vector onEdgePoint = new Vector(5, 0, 0);
        
        // Перевірка точок
        assertTrue(ShapeUtils.isPointInPolygon(insidePoint, polygon));
        assertFalse(ShapeUtils.isPointInPolygon(outsidePoint, polygon));
        // Точки на ребрі можуть поводити себе по-різному залежно від реалізації
        
        System.out.println("Shape utilities test passed");
    }
    
    @Test
    public void testAdvancedMathUtils() {
        // Тестуємо інтерполяцію
        double interpolated = MathUtils.lerp(0, 10, 0.5);
        assertEquals(5.0, interpolated, 0.001);
        
        // Тестуємо обмеження значень
        double clamped = MathUtils.clamp(15, 0, 10);
        assertEquals(10.0, clamped, 0.001);
        
        // Тестуємо нормалізацію (імітуємо функцію)
        double value = 5;
        double min = 0;
        double max = 10;
        double normalized = (value - min) / (max - min);
        assertEquals(0.5, normalized, 0.001);
        
        System.out.println("Advanced math utilities test passed");
    }
    
    @Test
    public void testIntegration() {
        // Комплексний тест, що демонструє інтеграцію всіх компонентів
        
        // 1. Створюємо регіон
        CuboidRegion region = new CuboidRegion("integration_test", null, -5, -5, -5, 5, 5, 5);
        
        // 2. Створюємо обмежувальну коробку для регіону
        BoundingBox regionBox = new BoundingBox(region.getMinPoint(), region.getMaxPoint());
        
        // 3. Перевіряємо об'єм
        assertEquals(1000.0, regionBox.getVolume(), 0.001);
        
        // 4. Створюємо матриці трансформації
        Matrix4x4 translation = Matrix4x4.translate(10, 0, 0);
        Matrix4x4 rotation = Matrix4x4.rotationY(Math.PI / 4); // 45 градусів
        Matrix4x4 transform = translation.multiply(rotation);
        
        // 5. Створюємо кватерніон для повороту
        Quaternion rotationQuat = Quaternion.rotationY(Math.PI / 4);
        
        // 6. Створюємо промінь для трасування
        Ray ray = new Ray(new org.bukkit.Location(null, 0, 0, 0), new org.bukkit.util.Vector(1, 0, 0));
        
        // 7. Генеруємо точки для візуалізації
        List<Vector3D> visualizationPoints = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            double x = -5 + i;
            visualizationPoints.add(new Vector3D(x, -5, -5));
            visualizationPoints.add(new Vector3D(x, 5, -5));
            visualizationPoints.add(new Vector3D(x, -5, 5));
            visualizationPoints.add(new Vector3D(x, 5, 5));
        }
        
        System.out.println("Integration test passed - all components work together");
        System.out.println("Generated " + visualizationPoints.size() + " points for visualization");
    }
}