package com.sparky.libx.test;

import com.sparky.libx.math.MathUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса MathUtils
 */
class MathUtilsTest {

    @Test
    void testClamp() {
        // Тестируем граничные значения
        assertEquals(5.0, MathUtils.clamp(10.0, 1.0, 5.0));
        assertEquals(1.0, MathUtils.clamp(0.0, 1.0, 5.0));
        assertEquals(3.0, MathUtils.clamp(3.0, 1.0, 5.0));
        
        // Отрицательные значения
        assertEquals(-1.0, MathUtils.clamp(-5.0, -1.0, 5.0));
        assertEquals(5.0, MathUtils.clamp(10.0, -1.0, 5.0));
    }

    @Test
    void testLerp() {
        // Линейная интерполяция между 0 и 10
        assertEquals(0.0, MathUtils.lerp(0, 10, 0.0));
        assertEquals(5.0, MathUtils.lerp(0, 10, 0.5));
        assertEquals(10.0, MathUtils.lerp(0, 10, 1.0));
        
        // Отрицательные значения
        assertEquals(-10.0, MathUtils.lerp(-10, 10, 0.0));
        assertEquals(0.0, MathUtils.lerp(-10, 10, 0.5));
        assertEquals(10.0, MathUtils.lerp(-10, 10, 1.0));
    }

    @Test
    void testRound() {
        // Округление до целых
        assertEquals(5.0, MathUtils.round(5.12345, 0));
        
        // Округление до одного знака после запятой
        assertEquals(5.1, MathUtils.round(5.12345, 1));
        
        // Округление до трех знаков после запятой
        assertEquals(5.123, MathUtils.round(5.12345, 3));
        
        // Округление с отрицательным количеством знаков (вызывает исключение)
        assertThrows(IllegalArgumentException.class, () -> MathUtils.round(5.12345, -1));
    }

    @Test
    void testInRange() {
        // Проверка значений внутри диапазона
        assertTrue(MathUtils.inRange(5.0, 0.0, 10.0));
        assertTrue(MathUtils.inRange(0.0, 0.0, 10.0));
        assertTrue(MathUtils.inRange(10.0, 0.0, 10.0));
        
        // Проверка значений вне диапазона
        assertFalse(MathUtils.inRange(-1.0, 0.0, 10.0));
        assertFalse(MathUtils.inRange(11.0, 0.0, 10.0));
    }

    @Test
    void testToRadiansAndDegrees() {
        // Проверка преобразования градусов в радианы и обратно
        double angleInDegrees = 45.0;
        double angleInRadians = Math.toRadians(angleInDegrees);
        
        assertEquals(angleInRadians, MathUtils.toRadians(angleInDegrees), 1e-10);
        assertEquals(angleInDegrees, MathUtils.toDegrees(angleInRadians), 1e-10);
        
        // Проверка полного круга
        assertEquals(Math.PI * 2, MathUtils.toRadians(360), 1e-10);
        assertEquals(180.0, MathUtils.toDegrees(Math.PI), 1e-10);
    }
}
