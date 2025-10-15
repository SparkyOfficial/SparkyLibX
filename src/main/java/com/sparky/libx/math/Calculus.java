package com.sparky.libx.math;

/**
 * утиліти для математичного аналізу
 * включає чисельне диференціювання, інтегрування, вирішення рівнянь і оптимізацію
 * @author Андрій Будильников
 */
public class Calculus {
    
    private static final double DEFAULT_EPSILON = 1e-3;
    
    /**
     * функціональний інтерфейс для математичної функції
     */
    @FunctionalInterface
    public interface MathFunction {
        double apply(double x);
    }
    
    /**
     * обчислити похідну функції в точці методом скінченних різниць
     * @param function функція
     * @param x точка, в якій обчислюється похідна
     * @param h крок
     * @return значення похідної
     */
    public static double derivative(MathFunction function, double x, double h) {
        return (function.apply(x + h) - function.apply(x - h)) / (2 * h);
    }
    
    /**
     * обчислити похідну функції в точці з умовчальним кроком
     * @param function функція
     * @param x точка, в якій обчислюється похідна
     * @return значення похідної
     */
    public static double derivative(MathFunction function, double x) {
        return derivative(function, x, DEFAULT_EPSILON);
    }
    
    /**
     * обчислити другу похідну функції в точці
     * @param function функція
     * @param x точка, в якій обчислюється друга похідна
     * @param h крок
     * @return значення другої похідної
     */
    public static double secondDerivative(MathFunction function, double x, double h) {
        return (function.apply(x + h) - 2 * function.apply(x) + function.apply(x - h)) / (h * h);
    }
    
    /**
     * обчислити другу похідну функції в точці з умовчальним кроком
     * @param function функція
     * @param x точка, в якій обчислюється друга похідна
     * @return значення другої похідної
     */
    public static double secondDerivative(MathFunction function, double x) {
        return secondDerivative(function, x, DEFAULT_EPSILON);
    }
    
    /**
     * обчислити визначений інтеграл методом трапецій
     * @param function функція
     * @param a нижня межа інтегрування
     * @param b верхня межа інтегрування
     * @param n кількість інтервалів
     * @return значення інтеграла
     */
    public static double integrateTrapezoid(MathFunction function, double a, double b, int n) {
        if (a >= b) {
            throw new IllegalArgumentException("Нижня межа повинна бути меншою за верхню");
        }
        
        if (n <= 0) {
            throw new IllegalArgumentException("Кількість інтервалів повинна бути додатною");
        }
        
        double h = (b - a) / n;
        double sum = 0.5 * (function.apply(a) + function.apply(b));
        
        for (int i = 1; i < n; i++) {
            sum += function.apply(a + i * h);
        }
        
        return sum * h;
    }
    
    /**
     * обчислити визначений інтеграл методом Сімпсона
     * @param function функція
     * @param a нижня межа інтегрування
     * @param b верхня межа інтегрування
     * @param n кількість інтервалів (повинно бути парним)
     * @return значення інтеграла
     */
    public static double integrateSimpson(MathFunction function, double a, double b, int n) {
        if (a >= b) {
            throw new IllegalArgumentException("Нижня межа повинна бути меншою за верхню");
        }
        
        if (n <= 0 || n % 2 != 0) {
            throw new IllegalArgumentException("Кількість інтервалів повинна бути додатною і парною");
        }
        
        double h = (b - a) / n;
        double sum = function.apply(a) + function.apply(b);
        
        // додати непарні терми
        for (int i = 1; i < n; i += 2) {
            sum += 4 * function.apply(a + i * h);
        }
        
        // додати парні терми
        for (int i = 2; i < n; i += 2) {
            sum += 2 * function.apply(a + i * h);
        }
        
        return sum * h / 3;
    }
    
    /**
     * знайти корінь функції методом бісекції
     * @param function функція
     * @param a ліва межа інтервалу
     * @param b права межа інтервалу
     * @param epsilon точність
     * @return корінь функції
     */
    public static double findRootBisection(MathFunction function, double a, double b, double epsilon) {
        if (function.apply(a) * function.apply(b) >= 0) {
            throw new IllegalArgumentException("Функція повинна мати різні знаки на кінцях інтервалу");
        }
        
        double c = a;
        while ((b - a) >= epsilon) {
            // знайти середину
            c = (a + b) / 2;
            
            // перевірити чи середина є коренем
            if (function.apply(c) == 0.0) {
                break;
            }
            
            // вирішити яку частину інтервалу взяти
            if (function.apply(c) * function.apply(a) < 0) {
                b = c;
            } else {
                a = c;
            }
        }
        
        return c;
    }
    
    /**
     * знайти корінь функції методом Ньютона-Рафсона
     * @param function функція
     * @param derivative похідна функції
     * @param x0 початкове наближення
     * @param epsilon точність
     * @param maxIterations максимальна кількість ітерацій
     * @return корінь функції
     */
    public static double findRootNewtonRaphson(MathFunction function, MathFunction derivative, 
                                             double x0, double epsilon, int maxIterations) {
        double x = x0;
        
        for (int i = 0; i < maxIterations; i++) {
            double fx = function.apply(x);
            double dfx = derivative.apply(x);
            
            if (Math.abs(dfx) < epsilon) {
                throw new RuntimeException("Похідна близька до нуля, метод не збігається");
            }
            
            double xNew = x - fx / dfx;
            
            if (Math.abs(xNew - x) < epsilon) {
                return xNew;
            }
            
            x = xNew;
        }
        
        throw new RuntimeException("Метод не збігся за " + maxIterations + " ітерацій");
    }
    
    /**
     * знайти мінімум функції методом золотого перетину
     * @param function функція
     * @param a ліва межа інтервалу
     * @param b права межа інтервалу
     * @param epsilon точність
     * @return точка мінімуму
     */
    public static double findMinimumGoldenSection(MathFunction function, double a, double b, double epsilon) {
        final double phi = (1 + Math.sqrt(5)) / 2;
        final double resphi = 2 - phi;
        
        double x1 = a + resphi * (b - a);
        double x2 = b - resphi * (b - a);
        
        double f1 = function.apply(x1);
        double f2 = function.apply(x2);
        
        while (Math.abs(b - a) > epsilon) {
            if (f1 < f2) {
                b = x2;
                x2 = x1;
                f2 = f1;
                x1 = a + resphi * (b - a);
                f1 = function.apply(x1);
            } else {
                a = x1;
                x1 = x2;
                f1 = f2;
                x2 = b - resphi * (b - a);
                f2 = function.apply(x2);
            }
        }
        
        return (a + b) / 2;
    }
    
    /**
     * обчислити градієнт функції двох змінних
     * @param function функція двох змінних
     * @param x перша змінна
     * @param y друга змінна
     * @param h крок
     * @return градієнт як вектор
     */
    public static Vector3D gradient(BiMathFunction function, double x, double y, double h) {
        // часткова похідна по x
        double dfdx = (function.apply(x + h, y) - function.apply(x - h, y)) / (2 * h);
        
        // часткова похідна по y
        double dfdy = (function.apply(x, y + h) - function.apply(x, y - h)) / (2 * h);
        
        return new Vector3D(dfdx, dfdy, 0);
    }
    
    /**
     * обчислити градієнт функції двох змінних з умовчальним кроком
     * @param function функція двох змінних
     * @param x перша змінна
     * @param y друга змінна
     * @return градієнт як вектор
     */
    public static Vector3D gradient(BiMathFunction function, double x, double y) {
        return gradient(function, x, y, DEFAULT_EPSILON);
    }
    
    /**
     * функціональний інтерфейс для функції двох змінних
     */
    @FunctionalInterface
    public interface BiMathFunction {
        double apply(double x, double y);
    }
}