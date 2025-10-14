package com.sparky.libx.math;

/**
 * Утилитарный класс для математических операций
 */
public final class MathUtils {
    
    private MathUtils() {}
    
    /**
     * Ограничивает значение в заданных пределах
     * @param value значение
     * @param min минимальное значение
     * @param max максимальное значение
     * @return значение в пределах [min, max]
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Линейная интерполяция между двумя значениями
     * @param start начальное значение
     * @param end конечное значение
     * @param t параметр интерполяции [0, 1]
     * @return интерполированное значение
     */
    public static double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }
    
    /**
     * Округляет число до указанного количества знаков после запятой
     * @param value значение для округления
     * @param places количество знаков после запятой
     * @return округленное значение
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Количество знаков должно быть неотрицательным");
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
    
    /**
     * Проверяет, находится ли число в заданном диапазоне
     * @param value проверяемое значение
     * @param min нижняя граница (включительно)
     * @param max верхняя граница (включительно)
     * @return true, если значение входит в диапазон
     */
    public static boolean inRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
    
    /**
     * Преобразует градусы в радианы
     * @param deg угол в градусах
     * @return угол в радианах
     */
    public static double toRadians(double deg) {
        return deg * Math.PI / 180.0;
    }
    
    /**
     * Преобразует радианы в градусы
     * @param rad угол в радианах
     * @return угол в градусах
     */
    public static double toDegrees(double rad) {
        return rad * 180.0 / Math.PI;
    }
}
