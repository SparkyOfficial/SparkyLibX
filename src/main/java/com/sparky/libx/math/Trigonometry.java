package com.sparky.libx.math;

/**
 * Расширенные тригонометрические функции
 */
public class Trigonometry {
    
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
     * Вычисляет обратный гиперболический синус
     * @param x значение
     * @return asinh(x)
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(x * x + 1));
    }
    
    /**
     * Вычисляет обратный гиперболический косинус
     * @param x значение
     * @return acosh(x)
     */
    public static double acosh(double x) {
        if (x < 1) {
            throw new IllegalArgumentException("Аргумент должен быть >= 1");
        }
        return Math.log(x + Math.sqrt(x * x - 1));
    }
    
    /**
     * Вычисляет обратный гиперболический тангенс
     * @param x значение
     * @return atanh(x)
     */
    public static double atanh(double x) {
        if (Math.abs(x) >= 1) {
            throw new IllegalArgumentException("Аргумент должен быть в диапазоне (-1, 1)");
        }
        return 0.5 * Math.log((1 + x) / (1 - x));
    }
    
    /**
     * Вычисляет секанс (1/cos)
     * @param x угол в радианах
     * @return sec(x)
     */
    public static double sec(double x) {
        return 1.0 / Math.cos(x);
    }
    
    /**
     * Вычисляет косеканс (1/sin)
     * @param x угол в радианах
     * @return csc(x)
     */
    public static double csc(double x) {
        return 1.0 / Math.sin(x);
    }
    
    /**
     * Вычисляет котангенс (cos/sin)
     * @param x угол в радианах
     * @return cot(x)
     */
    public static double cot(double x) {
        return Math.cos(x) / Math.sin(x);
    }
    
    /**
     * Вычисляет обратный секанс
     * @param x значение
     * @return asec(x)
     */
    public static double asec(double x) {
        if (Math.abs(x) < 1) {
            throw new IllegalArgumentException("Абсолютное значение аргумента должно быть >= 1");
        }
        return Math.acos(1.0 / x);
    }
    
    /**
     * Вычисляет обратный косеканс
     * @param x значение
     * @return acsc(x)
     */
    public static double acsc(double x) {
        if (Math.abs(x) < 1) {
            throw new IllegalArgumentException("Абсолютное значение аргумента должно быть >= 1");
        }
        return Math.asin(1.0 / x);
    }
    
    /**
     * Вычисляет обратный котангенс
     * @param x значение
     * @return acot(x)
     */
    public static double acot(double x) {
        return Math.atan(1.0 / x);
    }
    
    /**
     * Вычисляет версинус (1 - cos)
     * @param x угол в радианах
     * @return versin(x)
     */
    public static double versin(double x) {
        return 1.0 - Math.cos(x);
    }
    
    /**
     * Вычисляет коверсинус (1 + cos)
     * @param x угол в радианах
     * @return coversin(x)
     */
    public static double coversin(double x) {
        return 1.0 + Math.cos(x);
    }
    
    /**
     * Вычисляет гаверсинус (версинус/2)
     * @param x угол в радианах
     * @return haversin(x)
     */
    public static double haversin(double x) {
        return versin(x) / 2.0;
    }
    
    /**
     * Вычисляет экссеканс (sec - 1)
     * @param x угол в радианах
     * @return exsec(x)
     */
    public static double exsec(double x) {
        return sec(x) - 1.0;
    }
    
    /**
     * Вычисляет экскосеканс (csc - 1)
     * @param x угол в радианах
     * @return excsc(x)
     */
    public static double excsc(double x) {
        return csc(x) - 1.0;
    }
}