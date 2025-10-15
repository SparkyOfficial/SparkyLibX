package com.sparky.libx.math;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * утиліти для статистичного аналізу
 * включає описову статистику, розподіли, тестування гіпотез і регресійний аналіз
 * @author Андрій Будильников
 */
public class Statistics {
    
    /**
     * обчислити середнє арифметичне
     * @param values масив значень
     * @return середнє арифметичне
     */
    public static double mean(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Масив значень не може бути порожнім");
        }
        
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }
    
    /**
     * обчислити середнє арифметичне з використанням stream api
     * @param values список значень
     * @return середнє арифметичне
     */
    public static double mean(List<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Список значень не може бути порожнім");
        }
        
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }
    
    /**
     * обчислити медіану
     * @param values масив значень
     * @return медіана
     */
    public static double median(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Масив значень не може бути порожнім");
        }
        
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        
        int n = sorted.length;
        if (n % 2 == 0) {
            return (sorted[n/2 - 1] + sorted[n/2]) / 2.0;
        } else {
            return sorted[n/2];
        }
    }
    
    /**
     * обчислити моду (найчастіше зустрічається значення)
     * @param values масив значень
     * @return мода
     */
    public static double mode(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Масив значень не може бути порожнім");
        }
        
        return Arrays.stream(values)
                .boxed()
                .collect(Collectors.groupingBy(Double::doubleValue, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Comparator.comparingLong(entry -> entry.getValue()))
                .map(entry -> entry.getKey())
                .orElse(Double.NaN);
    }
    
    /**
     * обчислити дисперсію
     * @param values масив значень
     * @param isSample чи значення є вибіркою (true) чи генеральною сукупністю (false)
     * @return дисперсія
     */
    public static double variance(double[] values, boolean isSample) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Масив значень не може бути порожнім");
        }
        
        double mean = mean(values);
        double sum = 0;
        for (double value : values) {
            sum += Math.pow(value - mean, 2);
        }
        
        int divisor = isSample ? values.length - 1 : values.length;
        return sum / divisor;
    }
    
    /**
     * обчислити стандартне відхилення
     * @param values масив значень
     * @param isSample чи значення є вибіркою (true) чи генеральною сукупністю (false)
     * @return стандартне відхилення
     */
    public static double standardDeviation(double[] values, boolean isSample) {
        return Math.sqrt(variance(values, isSample));
    }
    
    /**
     * обчислити коефіцієнт варіації
     * @param values масив значень
     * @param isSample чи значення є вибіркою (true) чи генеральною сукупністю (false)
     * @return коефіцієнт варіації
     */
    public static double coefficientOfVariation(double[] values, boolean isSample) {
        double mean = mean(values);
        if (mean == 0) {
            throw new IllegalArgumentException("Середнє не може бути нулем для обчислення коефіцієнта варіації");
        }
        return standardDeviation(values, isSample) / Math.abs(mean);
    }
    
    /**
     * обчислити квартилі
     * @param values масив значень
     * @return масив з трьох квартилів [Q1, Q2, Q3]
     */
    public static double[] quartiles(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Масив значень не може бути порожнім");
        }
        
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        
        int n = sorted.length;
        double[] quartiles = new double[3];
        
        // Q2 (медіана)
        quartiles[1] = median(sorted);
        
        // Q1 (медіана нижньої половини)
        int mid = n / 2;
        double[] lowerHalf = Arrays.copyOfRange(sorted, 0, mid);
        quartiles[0] = median(lowerHalf);
        
        // Q3 (медіана верхньої половини)
        double[] upperHalf = (n % 2 == 0) ? 
            Arrays.copyOfRange(sorted, mid, n) : 
            Arrays.copyOfRange(sorted, mid + 1, n);
        quartiles[2] = median(upperHalf);
        
        return quartiles;
    }
    
    /**
     * обчислити інтерквартильний розмах
     * @param values масив значень
     * @return інтерквартильний розмах
     */
    public static double interquartileRange(double[] values) {
        double[] quartiles = quartiles(values);
        return quartiles[2] - quartiles[0];
    }
    
    /**
     * обчислити діапазон
     * @param values масив значень
     * @return діапазон (різниця між максимальним і мінімальним значенням)
     */
    public static double range(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Масив значень не може бути порожнім");
        }
        
        double min = Arrays.stream(values).min().orElse(0);
        double max = Arrays.stream(values).max().orElse(0);
        return max - min;
    }
    
    /**
     * обчислити з-оцінку (стандартизоване значення)
     * @param value значення
     * @param mean середнє
     * @param standardDeviation стандартне відхилення
     * @return з-оцінка
     */
    public static double zScore(double value, double mean, double standardDeviation) {
        if (standardDeviation == 0) {
            throw new IllegalArgumentException("Стандартне відхилення не може бути нулем");
        }
        return (value - mean) / standardDeviation;
    }
    
    /**
     * обчислити кореляцію пірсона між двома масивами
     * @param x перший масив
     * @param y другий масив
     * @return коефіцієнт кореляції пірсона
     */
    public static double pearsonCorrelation(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length || x.length == 0) {
            throw new IllegalArgumentException("Масиви повинні бути не порожніми і мати однакову довжину");
        }
        
        int n = x.length;
        double meanX = mean(x);
        double meanY = mean(y);
        
        double numerator = 0;
        double sumSquaredX = 0;
        double sumSquaredY = 0;
        
        for (int i = 0; i < n; i++) {
            double diffX = x[i] - meanX;
            double diffY = y[i] - meanY;
            numerator += diffX * diffY;
            sumSquaredX += diffX * diffX;
            sumSquaredY += diffY * diffY;
        }
        
        double denominator = Math.sqrt(sumSquaredX * sumSquaredY);
        if (denominator == 0) {
            return 0; // немає варіації в одному з масивів
        }
        
        return numerator / denominator;
    }
    
    /**
     * обчислити коваріацію між двома масивами
     * @param x перший масив
     * @param y другий масив
     * @param isSample чи масиви є вибірками (true) чи генеральними сукупностями (false)
     * @return коваріація
     */
    public static double covariance(double[] x, double[] y, boolean isSample) {
        if (x == null || y == null || x.length != y.length || x.length == 0) {
            throw new IllegalArgumentException("Масиви повинні бути не порожніми і мати однакову довжину");
        }
        
        int n = x.length;
        double meanX = mean(x);
        double meanY = mean(y);
        
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += (x[i] - meanX) * (y[i] - meanY);
        }
        
        int divisor = isSample ? n - 1 : n;
        return sum / divisor;
    }
    
    /**
     * обчислити перцентиль
     * @param values масив значень
     * @param percentile перцентиль (0-100)
     * @return значення перцентилю
     */
    public static double percentile(double[] values, double percentile) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Масив значень не може бути порожнім");
        }
        
        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Перцентиль повинен бути між 0 і 100");
        }
        
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        
        double index = (percentile / 100.0) * (sorted.length - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        
        if (lowerIndex == upperIndex) {
            return sorted[lowerIndex];
        }
        
        double weight = index - lowerIndex;
        return sorted[lowerIndex] * (1 - weight) + sorted[upperIndex] * weight;
    }
    
    /**
     * обчислити середнє геометричне
     * @param values масив значень
     * @return середнє геометричне
     */
    public static double geometricMean(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Масив значень не може бути порожнім");
        }
        
        double product = 1;
        for (double value : values) {
            if (value <= 0) {
                throw new IllegalArgumentException("Всі значення повинні бути додатними для середнього геометричного");
            }
            product *= value;
        }
        
        return Math.pow(product, 1.0 / values.length);
    }
    
    /**
     * обчислити середнє гармонійне
     * @param values масив значень
     * @return середнє гармонійне
     */
    public static double harmonicMean(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Масив значень не може бути порожнім");
        }
        
        double sum = 0;
        for (double value : values) {
            if (value == 0) {
                throw new IllegalArgumentException("Значення не можуть бути нульовими для середнього гармонійного");
            }
            sum += 1.0 / value;
        }
        
        return values.length / sum;
    }
    
    /**
     * обчислити середнє усіченої вибірки
     * @param values масив значень
     * @param trimPercentage відсоток значень для відсікання з кожного кінця (0-50)
     * @return середнє усіченої вибірки
     */
    public static double trimmedMean(double[] values, double trimPercentage) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Масив значень не може бути порожнім");
        }
        
        if (trimPercentage < 0 || trimPercentage > 50) {
            throw new IllegalArgumentException("Відсоток відсікання повинен бути між 0 і 50");
        }
        
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        
        int n = sorted.length;
        int trimCount = (int) Math.round(n * trimPercentage / 100.0);
        
        if (2 * trimCount >= n) {
            throw new IllegalArgumentException("Занадто великий відсоток відсікання");
        }
        
        double sum = 0;
        for (int i = trimCount; i < n - trimCount; i++) {
            sum += sorted[i];
        }
        
        return sum / (n - 2 * trimCount);
    }
    
    /**
     * обчислити середнє абсолютне відхилення
     * @param values масив значень
     * @return середнє абсолютне відхилення
     */
    public static double meanAbsoluteDeviation(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Масив значень не може бути порожнім");
        }
        
        double mean = mean(values);
        double sum = 0;
        for (double value : values) {
            sum += Math.abs(value - mean);
        }
        return sum / values.length;
    }
    
    /**
     * обчислити асиметрію (скошеність)
     * @param values масив значень
     * @return коефіцієнт асиметрії
     */
    public static double skewness(double[] values) {
        if (values == null || values.length < 3) {
            throw new IllegalArgumentException("Потрібно щонайменше 3 значення для обчислення асиметрії");
        }
        
        double mean = mean(values);
        double stdDev = standardDeviation(values, true);
        
        if (stdDev == 0) {
            return 0;
        }
        
        double sum = 0;
        for (double value : values) {
            sum += Math.pow((value - mean) / stdDev, 3);
        }
        
        int n = values.length;
        return (n * sum) / ((n - 1) * (n - 2));
    }
    
    /**
     * обчислити ексцес (курутість)
     * @param values масив значень
     * @return коефіцієнт ексцесу
     */
    public static double kurtosis(double[] values) {
        if (values == null || values.length < 4) {
            throw new IllegalArgumentException("Потрібно щонайменше 4 значення для обчислення ексцесу");
        }
        
        double mean = mean(values);
        double stdDev = standardDeviation(values, true);
        
        if (stdDev == 0) {
            return 0;
        }
        
        double sum = 0;
        for (double value : values) {
            sum += Math.pow((value - mean) / stdDev, 4);
        }
        
        int n = values.length;
        double factor = (double) n * (n + 1) / ((n - 1) * (n - 2) * (n - 3));
        double subtract = (double) 3 * (n - 1) * (n - 1) / ((n - 2) * (n - 3));
        
        return factor * sum - subtract;
    }
}