package com.sparky.libx.math;

/**
 * Расширенные тригонометрические функции
 * author: Андрій Будильников
 */
public final class Trigonometry {
    
    private Trigonometry() {}
    
    // ================ ГИПЕРБОЛИЧЕСКИЕ ФУНКЦИИ ================
    
    /**
     * Вычисляет гиперболический синус
     * @param x значение
     * @return sinh(x)
     */
    public static double sinh(double x) {
        return (Math.exp(x) - Math.exp(-x)) / 2.0;
    }
    
    /**
     * Вычисляет гиперболический косинус
     * @param x значение
     * @return cosh(x)
     */
    public static double cosh(double x) {
        return (Math.exp(x) + Math.exp(-x)) / 2.0;
    }
    
    /**
     * Вычисляет гиперболический тангенс
     * @param x значение
     * @return tanh(x)
     */
    public static double tanh(double x) {
        double exp2x = Math.exp(2 * x);
        return (exp2x - 1) / (exp2x + 1);
    }
    
    /**
     * Вычисляет гиперболический секанс
     * @param x значение
     * @return sech(x)
     */
    public static double sech(double x) {
        return 1.0 / cosh(x);
    }
    
    /**
     * Вычисляет гиперболический косеканс
     * @param x значение
     * @return csch(x)
     */
    public static double csch(double x) {
        return 1.0 / sinh(x);
    }
    
    /**
     * Вычисляет гиперболический котангенс
     * @param x значение
     * @return coth(x)
     */
    public static double coth(double x) {
        return 1.0 / tanh(x);
    }
    
    // ================ ОБРАТНЫЕ ГИПЕРБОЛИЧЕСКИЕ ФУНКЦИИ ================
    
    /**
     * Вычисляет обратный гиперболический синус
     * @param x значение
     * @return arsinh(x)
     */
    public static double arsinh(double x) {
        return Math.log(x + Math.sqrt(x * x + 1));
    }
    
    /**
     * Вычисляет обратный гиперболический косинус
     * @param x значение (должно быть >= 1)
     * @return arcosh(x)
     */
    public static double arcosh(double x) {
        if (x < 1) throw new IllegalArgumentException("Аргумент должен быть >= 1");
        return Math.log(x + Math.sqrt(x * x - 1));
    }
    
    /**
     * Вычисляет обратный гиперболический тангенс
     * @param x значение (должно быть в диапазоне (-1, 1))
     * @return artanh(x)
     */
    public static double artanh(double x) {
        if (x <= -1 || x >= 1) throw new IllegalArgumentException("Аргумент должен быть в диапазоне (-1, 1)");
        return 0.5 * Math.log((1 + x) / (1 - x));
    }
    
    /**
     * Вычисляет обратный гиперболический секанс
     * @param x значение (должно быть в диапазоне (0, 1])
     * @return arsech(x)
     */
    public static double arsech(double x) {
        if (x <= 0 || x > 1) throw new IllegalArgumentException("Аргумент должен быть в диапазоне (0, 1]");
        return Math.log((1 + Math.sqrt(1 - x * x)) / x);
    }
    
    /**
     * Вычисляет обратный гиперболический косеканс
     * @param x значение (не должно быть 0)
     * @return arcsch(x)
     */
    public static double arcsch(double x) {
        if (x == 0) throw new IllegalArgumentException("Аргумент не должен быть равен 0");
        return Math.log(1 / x + Math.sqrt(1 / (x * x) + 1));
    }
    
    /**
     * Вычисляет обратный гиперболический котангенс
     * @param x значение (должно быть |x| > 1)
     * @return arcoth(x)
     */
    public static double arcoth(double x) {
        if (Math.abs(x) <= 1) throw new IllegalArgumentException("Абсолютное значение аргумента должно быть > 1");
        return 0.5 * Math.log((x + 1) / (x - 1));
    }
    
    // ================ ДОПОЛНИТЕЛЬНЫЕ ТРИГОНОМЕТРИЧЕСКИЕ ФУНКЦИИ ================
    
    /**
     * Вычисляет котангенс
     * @param x угол в радианах
     * @return cot(x)
     */
    public static double cot(double x) {
        return 1.0 / Math.tan(x);
    }
    
    /**
     * Вычисляет секанс
     * @param x угол в радианах
     * @return sec(x)
     */
    public static double sec(double x) {
        return 1.0 / Math.cos(x);
    }
    
    /**
     * Вычисляет косеканс
     * @param x угол в радианах
     * @return csc(x)
     */
    public static double csc(double x) {
        return 1.0 / Math.sin(x);
    }
    
    /**
     * Вычисляет версинус (versine)
     * @param x угол в радианах
     * @return versin(x) = 1 - cos(x)
     */
    public static double versin(double x) {
        return 1.0 - Math.cos(x);
    }
    
    /**
     * Вычисляет коверсинус (coversine)
     * @param x угол в радианах
     * @return coversin(x) = 1 - sin(x)
     */
    public static double coversin(double x) {
        return 1.0 - Math.sin(x);
    }
    
    /**
     * Вычисляет гаверсинус (haversine)
     * @param x угол в радианах
     * @return haversin(x) = (1 - cos(x)) / 2
     */
    public static double haversin(double x) {
        return (1.0 - Math.cos(x)) / 2.0;
    }
    
    /**
     * Вычисляет гаверкосинус (hacoversine)
     * @param x угол в радианах
     * @return hacoversin(x) = (1 - sin(x)) / 2
     */
    public static double hacoversin(double x) {
        return (1.0 - Math.sin(x)) / 2.0;
    }
    
    /**
     * Вычисляет экссеканс (exsecant)
     * @param x угол в радианах
     * @return exsec(x) = sec(x) - 1
     */
    public static double exsec(double x) {
        return sec(x) - 1.0;
    }
    
    /**
     * Вычисляет экскосеканс (excosecant)
     * @param x угол в радианах
     * @return excsc(x) = csc(x) - 1
     */
    public static double excsc(double x) {
        return csc(x) - 1.0;
    }
    
    // ================ ФУНКЦИИ ДЛЯ РАБОТЫ С ТРЕУГОЛЬНИКАМИ ================
    
    /**
     * Решает треугольник по трем сторонам (по теореме косинусов)
     * @param a сторона a
     * @param b сторона b
     * @param c сторона c
     * @return массив углов в радианах [A, B, C]
     */
    public static double[] solveTriangleBySides(double a, double b, double c) {
        // Проверка существования треугольника
        if (a + b <= c || a + c <= b || b + c <= a) {
            throw new IllegalArgumentException("Невозможно создать треугольник с такими сторонами");
        }
        
        // Вычисление углов по теореме косинусов
        double angleA = Math.acos((b * b + c * c - a * a) / (2 * b * c));
        double angleB = Math.acos((a * a + c * c - b * b) / (2 * a * c));
        double angleC = Math.PI - angleA - angleB;
        
        return new double[]{angleA, angleB, angleC};
    }
    
    /**
     * Решает треугольник по двум сторонам и углу между ними
     * @param a сторона a
     * @param b сторона b
     * @param angleC угол между сторонами a и b в радианах
     * @return массив [сторона c, угол A, угол B] в радианах
     */
    public static double[] solveTriangleByTwoSidesAndAngle(double a, double b, double angleC) {
        // Вычисление третьей стороны по теореме косинусов
        double c = Math.sqrt(a * a + b * b - 2 * a * b * Math.cos(angleC));
        
        // Вычисление остальных углов
        double angleA = Math.asin(a * Math.sin(angleC) / c);
        double angleB = Math.PI - angleA - angleC;
        
        return new double[]{c, angleA, angleB};
    }
    
    /**
     * Вычисляет площадь треугольника по двум сторонам и углу между ними
     * @param a сторона a
     * @param b сторона b
     * @param angleC угол между сторонами в радианах
     * @return площадь треугольника
     */
    public static double triangleAreaByTwoSidesAndAngle(double a, double b, double angleC) {
        return 0.5 * a * b * Math.sin(angleC);
    }
    
    /**
     * Вычисляет длину медианы треугольника
     * @param a сторона a
     * @param b сторона b
     * @param c сторона c
     * @param side сторона, к которой проведена медиана (a, b или c)
     * @return длина медианы
     */
    public static double medianLength(double a, double b, double c, char side) {
        switch (side) {
            case 'a':
                return 0.5 * Math.sqrt(2 * b * b + 2 * c * c - a * a);
            case 'b':
                return 0.5 * Math.sqrt(2 * a * a + 2 * c * c - b * b);
            case 'c':
                return 0.5 * Math.sqrt(2 * a * a + 2 * b * b - c * c);
            default:
                throw new IllegalArgumentException("Сторона должна быть 'a', 'b' или 'c'");
        }
    }
}