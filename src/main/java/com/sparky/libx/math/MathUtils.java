package com.sparky.libx.math;

/**
 * Утилитарный класс для математических операций
 * author: Андрій Будильников
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
    
    // ================ ТРИГОНОМЕТРИЯ ================
    
    /**
     * Вычисляет синус угла в градусах
     * @param degrees угол в градусах
     * @return синус угла
     */
    public static double sinDegrees(double degrees) {
        return Math.sin(toRadians(degrees));
    }
    
    /**
     * Вычисляет косинус угла в градусах
     * @param degrees угол в градусах
     * @return косинус угла
     */
    public static double cosDegrees(double degrees) {
        return Math.cos(toRadians(degrees));
    }
    
    /**
     * Вычисляет тангенс угла в градусах
     * @param degrees угол в градусах
     * @return тангенс угла
     */
    public static double tanDegrees(double degrees) {
        return Math.tan(toRadians(degrees));
    }
    
    /**
     * Вычисляет арксинус и возвращает результат в градусах
     * @param value значение от -1 до 1
     * @return угол в градусах
     */
    public static double asinDegrees(double value) {
        return toDegrees(Math.asin(value));
    }
    
    /**
     * Вычисляет арккосинус и возвращает результат в градусах
     * @param value значение от -1 до 1
     * @return угол в градусах
     */
    public static double acosDegrees(double value) {
        return toDegrees(Math.acos(value));
    }
    
    /**
     * Вычисляет арктангенс и возвращает результат в градусах
     * @param value значение
     * @return угол в градусах
     */
    public static double atanDegrees(double value) {
        return toDegrees(Math.atan(value));
    }
    
    /**
     * Вычисляет арктангенс двух аргументов и возвращает результат в градусах
     * @param y координата y
     * @param x координата x
     * @return угол в градусах от -180 до 180
     */
    public static double atan2Degrees(double y, double x) {
        return toDegrees(Math.atan2(y, x));
    }
    
    // ================ АЛГЕБРА ================
    
    /**
     * Решает квадратное уравнение ax² + bx + c = 0
     * @param a коэффициент a
     * @param b коэффициент b
     * @param c коэффициент c
     * @return массив корней (0, 1 или 2 элемента)
     */
    public static double[] solveQuadratic(double a, double b, double c) {
        if (a == 0) {
            // Линейное уравнение bx + c = 0
            if (b == 0) {
                return new double[0]; // Нет решений
            }
            return new double[]{-c / b}; // Одно решение
        }
        
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return new double[0]; // Нет действительных решений
        } else if (discriminant == 0) {
            return new double[]{-b / (2 * a)}; // Один корень
        } else {
            double sqrtD = Math.sqrt(discriminant);
            return new double[]{
                (-b + sqrtD) / (2 * a),
                (-b - sqrtD) / (2 * a)
            }; // Два корня
        }
    }
    
    /**
     * Вычисляет факториал числа
     * @param n число (должно быть неотрицательным)
     * @return факториал n
     */
    public static long factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("Факториал определен только для неотрицательных чисел");
        if (n == 0 || n == 1) return 1;
        
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
    
    /**
     * Вычисляет биномиальный коэффициент C(n, k)
     * @param n общее количество элементов
     * @param k количество выбираемых элементов
     * @return биномиальный коэффициент
     */
    public static long binomialCoefficient(int n, int k) {
        if (k > n || k < 0) return 0;
        if (k == 0 || k == n) return 1;
        
        // Оптимизация: C(n, k) = C(n, n-k)
        if (k > n - k) k = n - k;
        
        long result = 1;
        for (int i = 0; i < k; i++) {
            result = result * (n - i) / (i + 1);
        }
        return result;
    }
    
    /**
     * Вычисляет наибольший общий делитель двух чисел
     * @param a первое число
     * @param b второе число
     * @return НОД(a, b)
     */
    public static int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    
    /**
     * Вычисляет наименьшее общее кратное двух чисел
     * @param a первое число
     * @param b второе число
     * @return НОК(a, b)
     */
    public static int lcm(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return Math.abs(a * b) / gcd(a, b);
    }
    
    // ================ ГЕОМЕТРИЯ ================
    
    /**
     * Вычисляет расстояние между двумя точками в 2D пространстве
     * @param x1 координата x первой точки
     * @param y1 координата y первой точки
     * @param x2 координата x второй точки
     * @param y2 координата y второй точки
     * @return расстояние между точками
     */
    public static double distance2D(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Вычисляет расстояние между двумя точками в 3D пространстве
     * @param x1 координата x первой точки
     * @param y1 координата y первой точки
     * @param z1 координата z первой точки
     * @param x2 координата x второй точки
     * @param y2 координата y второй точки
     * @param z2 координата z второй точки
     * @return расстояние между точками
     */
    public static double distance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * Вычисляет площадь треугольника по формуле Герона
     * @param a длина первой стороны
     * @param b длина второй стороны
     * @param c длина третьей стороны
     * @return площадь треугольника
     */
    public static double triangleArea(double a, double b, double c) {
        // Проверка существования треугольника
        if (a + b <= c || a + c <= b || b + c <= a) {
            return 0; // Невозможно создать треугольник
        }
        
        double s = (a + b + c) / 2; // Полупериметр
        return Math.sqrt(s * (s - a) * (s - b) * (s - c));
    }
    
    /**
     * Вычисляет площадь круга
     * @param radius радиус круга
     * @return площадь круга
     */
    public static double circleArea(double radius) {
        return Math.PI * radius * radius;
    }
    
    /**
     * Вычисляет объем сферы
     * @param radius радиус сферы
     * @return объем сферы
     */
    public static double sphereVolume(double radius) {
        return (4.0 / 3.0) * Math.PI * Math.pow(radius, 3);
    }
    
    /**
     * Вычисляет площадь поверхности сферы
     * @param radius радиус сферы
     * @return площадь поверхности сферы
     */
    public static double sphereSurfaceArea(double radius) {
        return 4 * Math.PI * radius * radius;
    }
}