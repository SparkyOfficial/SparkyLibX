package com.sparky.libx.math;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

/**
 * утиліти для оптимізації
 * включає методи мінімізації/максимізації функцій, лінійне програмування і генетичні алгоритми
 * @author Андрій Будильников
 */
public class Optimization {
    
    private static final double DEFAULT_TOLERANCE = 1e-6;
    private static final int DEFAULT_MAX_ITERATIONS = 1000;
    private static final Random random = new Random();
    
    /**
     * функціональний інтерфейс для функції оптимізації
     */
    @FunctionalInterface
    public interface OptimizationFunction {
        double apply(double[] variables);
    }
    
    /**
     * результат оптимізації
     */
    public static class OptimizationResult {
        private final double[] variables;
        private final double value;
        private final boolean converged;
        private final int iterations;
        
        public OptimizationResult(double[] variables, double value, boolean converged, int iterations) {
            this.variables = variables.clone();
            this.value = value;
            this.converged = converged;
            this.iterations = iterations;
        }
        
        public double[] getVariables() {
            return variables.clone();
        }
        
        public double getValue() {
            return value;
        }
        
        public boolean isConverged() {
            return converged;
        }
        
        public int getIterations() {
            return iterations;
        }
        
        @Override
        public String toString() {
            return String.format("OptimizationResult{variables=%s, value=%.6f, converged=%s, iterations=%d}",
                               Arrays.toString(variables), value, converged, iterations);
        }
    }
    
    /**
     * знайти мінімум функції однієї змінної методом золотого перетину
     * @param function функція для мінімізації
     * @param a ліва межа інтервалу
     * @param b права межа інтервалу
     * @param tolerance точність
     * @return результат оптимізації
     */
    public static OptimizationResult minimizeGoldenSection(
            Function<Double, Double> function, double a, double b, double tolerance) {
        
        if (a >= b) {
            throw new IllegalArgumentException("Ліва межа повинна бути меншою за праву");
        }
        
        final double phi = (1 + Math.sqrt(5)) / 2;
        final double resphi = 2 - phi;
        
        double x1 = a + resphi * (b - a);
        double x2 = b - resphi * (b - a);
        
        double f1 = function.apply(x1);
        double f2 = function.apply(x2);
        
        int iterations = 0;
        
        while (Math.abs(b - a) > tolerance && iterations < DEFAULT_MAX_ITERATIONS) {
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
            iterations++;
        }
        
        double xOpt = (a + b) / 2;
        double fOpt = function.apply(xOpt);
        
        return new OptimizationResult(new double[]{xOpt}, fOpt, Math.abs(b - a) <= tolerance, iterations);
    }
    
    /**
     * знайти мінімум функції однієї змінної методом золотого перетину з умовчальними параметрами
     * @param function функція для мінімізації
     * @param a ліва межа інтервалу
     * @param b права межа інтервалу
     * @return результат оптимізації
     */
    public static OptimizationResult minimizeGoldenSection(
            Function<Double, Double> function, double a, double b) {
        return minimizeGoldenSection(function, a, b, DEFAULT_TOLERANCE);
    }
    
    /**
     * знайти мінімум функції багатьох змінних методом градієнтного спуску
     * @param function функція для мінімізації
     * @param initialGuess початкове наближення
     * @param learningRate швидкість навчання
     * @param tolerance точність
     * @param maxIterations максимальна кількість ітерацій
     * @return результат оптимізації
     */
    public static OptimizationResult minimizeGradientDescent(
            OptimizationFunction function, double[] initialGuess, double learningRate,
            double tolerance, int maxIterations) {
        
        if (initialGuess == null || initialGuess.length == 0) {
            throw new IllegalArgumentException("Початкове наближення не може бути порожнім");
        }
        
        if (learningRate <= 0) {
            throw new IllegalArgumentException("Швидкість навчання повинна бути додатною");
        }
        
        double[] x = initialGuess.clone();
        double currentValue = function.apply(x);
        int iterations = 0;
        boolean converged = false;
        
        while (iterations < maxIterations) {
            // обчислити градієнт чисельно
            double[] gradient = numericalGradient(function, x);
            
            // перевірити збіжність
            double gradientMagnitude = 0;
            for (double g : gradient) {
                gradientMagnitude += g * g;
            }
            gradientMagnitude = Math.sqrt(gradientMagnitude);
            
            if (gradientMagnitude < tolerance) {
                converged = true;
                break;
            }
            
            // оновити змінні
            for (int i = 0; i < x.length; i++) {
                x[i] -= learningRate * gradient[i];
            }
            
            // обчислити нове значення функції
            double newValue = function.apply(x);
            
            // перевірити збіжність за значенням функції
            if (Math.abs(newValue - currentValue) < tolerance) {
                converged = true;
                break;
            }
            
            currentValue = newValue;
            iterations++;
        }
        
        return new OptimizationResult(x, currentValue, converged, iterations);
    }
    
    /**
     * знайти мінімум функції багатьох змінних методом градієнтного спуску з умовчальними параметрами
     * @param function функція для мінімізації
     * @param initialGuess початкове наближення
     * @return результат оптимізації
     */
    public static OptimizationResult minimizeGradientDescent(
            OptimizationFunction function, double[] initialGuess) {
        return minimizeGradientDescent(function, initialGuess, 0.01, DEFAULT_TOLERANCE, DEFAULT_MAX_ITERATIONS);
    }
    
    /**
     * обчислити чисельний градієнт функції
     * @param function функція
     * @param x точка, в якій обчислюється градієнт
     * @param h крок
     * @return градієнт
     */
    public static double[] numericalGradient(OptimizationFunction function, double[] x, double h) {
        int n = x.length;
        double[] gradient = new double[n];
        double fx = function.apply(x);
        
        for (int i = 0; i < n; i++) {
            double[] xPlus = x.clone();
            xPlus[i] += h;
            gradient[i] = (function.apply(xPlus) - fx) / h;
        }
        
        return gradient;
    }
    
    /**
     * обчислити чисельний градієнт функції з умовчальним кроком
     * @param function функція
     * @param x точка, в якій обчислюється градієнт
     * @return градієнт
     */
    public static double[] numericalGradient(OptimizationFunction function, double[] x) {
        return numericalGradient(function, x, 1e-5);
    }
    
    /**
     * знайти мінімум функції багатьох змінних методом імітації відпалу
     * @param function функція для мінімізації
     * @param initialGuess початкове наближення
     * @param initialTemperature початкова температура
     * @param coolingRate швидкість охолодження
     * @param tolerance точність
     * @param maxIterations максимальна кількість ітерацій
     * @return результат оптимізації
     */
    public static OptimizationResult minimizeSimulatedAnnealing(
            OptimizationFunction function, double[] initialGuess, double initialTemperature,
            double coolingRate, double tolerance, int maxIterations) {
        
        if (initialGuess == null || initialGuess.length == 0) {
            throw new IllegalArgumentException("Початкове наближення не може бути порожнім");
        }
        
        if (initialTemperature <= 0) {
            throw new IllegalArgumentException("Початкова температура повинна бути додатною");
        }
        
        if (coolingRate <= 0 || coolingRate >= 1) {
            throw new IllegalArgumentException("Швидкість охолодження повинна бути між 0 і 1");
        }
        
        double[] currentSolution = initialGuess.clone();
        double currentValue = function.apply(currentSolution);
        double[] bestSolution = currentSolution.clone();
        double bestValue = currentValue;
        double temperature = initialTemperature;
        
        int iterations = 0;
        boolean converged = false;
        
        while (temperature > tolerance && iterations < maxIterations) {
            // згенерувати новий розв'язок поблизу поточного
            double[] newSolution = perturb(currentSolution, temperature / initialTemperature);
            double newValue = function.apply(newSolution);
            
            // прийняти чи відхилити новий розв'язок
            if (acceptanceProbability(currentValue, newValue, temperature) > random.nextDouble()) {
                currentSolution = newSolution;
                currentValue = newValue;
                
                // оновити найкращий розв'язок
                if (newValue < bestValue) {
                    bestSolution = newSolution.clone();
                    bestValue = newValue;
                }
            }
            
            // охолодити
            temperature *= coolingRate;
            iterations++;
        }
        
        // перевірити збіжність
        converged = temperature <= tolerance;
        
        return new OptimizationResult(bestSolution, bestValue, converged, iterations);
    }
    
    /**
     * знайти мінімум функції багатьох змінних методом імітації відпалу з умовчальними параметрами
     * @param function функція для мінімізації
     * @param initialGuess початкове наближення
     * @return результат оптимізації
     */
    public static OptimizationResult minimizeSimulatedAnnealing(
            OptimizationFunction function, double[] initialGuess) {
        return minimizeSimulatedAnnealing(function, initialGuess, 1000, 0.95, DEFAULT_TOLERANCE, DEFAULT_MAX_ITERATIONS);
    }
    
    /**
     * згенерувати збурений розв'язок
     * @param solution поточний розв'язок
     * @param perturbationScale масштаб збурення
     * @return збурений розв'язок
     */
    private static double[] perturb(double[] solution, double perturbationScale) {
        double[] perturbed = solution.clone();
        for (int i = 0; i < perturbed.length; i++) {
            perturbed[i] += (random.nextGaussian() * perturbationScale);
        }
        return perturbed;
    }
    
    /**
     * обчислити ймовірність прийняття нового розв'язку
     * @param currentValue поточне значення
     * @param newValue нове значення
     * @param temperature температура
     * @return ймовірність прийняття
     */
    private static double acceptanceProbability(double currentValue, double newValue, double temperature) {
        if (newValue < currentValue) {
            return 1.0;
        }
        return Math.exp((currentValue - newValue) / temperature);
    }
    
    /**
     * знайти мінімум функції багатьох змінних генетичним алгоритмом
     * @param function функція для мінімізації
     * @param dimensions кількість змінних
     * @param bounds межі для кожної змінної (масив пар [min, max])
     * @param populationSize розмір популяції
     * @param mutationRate швидкість мутації
     * @param crossoverRate швидкість схрещування
     * @param maxGenerations максимальна кількість поколінь
     * @return результат оптимізації
     */
    public static OptimizationResult minimizeGeneticAlgorithm(
            OptimizationFunction function, int dimensions, double[][] bounds,
            int populationSize, double mutationRate, double crossoverRate, int maxGenerations) {
        
        if (dimensions <= 0) {
            throw new IllegalArgumentException("Кількість змінних повинна бути додатною");
        }
        
        if (bounds == null || bounds.length != dimensions) {
            throw new IllegalArgumentException("Межі повинні бути вказані для кожної змінної");
        }
        
        if (populationSize <= 0) {
            throw new IllegalArgumentException("Розмір популяції повинен бути додатним");
        }
        
        if (mutationRate < 0 || mutationRate > 1) {
            throw new IllegalArgumentException("Швидкість мутації повинна бути між 0 і 1");
        }
        
        if (crossoverRate < 0 || crossoverRate > 1) {
            throw new IllegalArgumentException("Швидкість схрещування повинна бути між 0 і 1");
        }
        
        // ініціалізувати популяцію
        double[][] population = initializePopulation(dimensions, bounds, populationSize);
        double[] fitness = new double[populationSize];
        
        // обчислити придатність для початкової популяції
        for (int i = 0; i < populationSize; i++) {
            fitness[i] = function.apply(population[i]);
        }
        
        int generation = 0;
        double bestFitness = Arrays.stream(fitness).min().orElse(Double.POSITIVE_INFINITY);
        int bestIndex = 0;
        for (int i = 0; i < populationSize; i++) {
            if (fitness[i] < bestFitness) {
                bestFitness = fitness[i];
                bestIndex = i;
            }
        }
        double[] bestSolution = population[bestIndex].clone();
        
        // еволюція
        while (generation < maxGenerations) {
            // створити нову популяцію
            double[][] newPopulation = new double[populationSize][dimensions];
            
            // елітізм - зберегти найкращого
            newPopulation[0] = population[bestIndex].clone();
            
            // створити решту популяції
            for (int i = 1; i < populationSize; i++) {
                // вибрати батьків
                int parent1 = tournamentSelection(fitness, 3);
                int parent2 = tournamentSelection(fitness, 3);
                
                // схрещування
                double[] offspring;
                if (random.nextDouble() < crossoverRate) {
                    offspring = crossover(population[parent1], population[parent2]);
                } else {
                    offspring = population[parent1].clone();
                }
                
                // мутація
                if (random.nextDouble() < mutationRate) {
                    offspring = mutate(offspring, bounds);
                }
                
                // забезпечити межі
                enforceBounds(offspring, bounds);
                
                newPopulation[i] = offspring;
            }
            
            population = newPopulation;
            
            // обчислити придатність для нової популяції
            for (int i = 0; i < populationSize; i++) {
                fitness[i] = function.apply(population[i]);
            }
            
            // знайти найкращого в новій популяції
            double currentBestFitness = Arrays.stream(fitness).min().orElse(Double.POSITIVE_INFINITY);
            int currentBestIndex = 0;
            for (int i = 0; i < populationSize; i++) {
                if (fitness[i] < currentBestFitness) {
                    currentBestFitness = fitness[i];
                    currentBestIndex = i;
                }
            }
            
            // оновити глобального найкращого
            if (currentBestFitness < bestFitness) {
                bestFitness = currentBestFitness;
                bestSolution = population[currentBestIndex].clone();
            }
            
            generation++;
        }
        
        return new OptimizationResult(bestSolution, bestFitness, true, generation);
    }
    
    /**
     * ініціалізувати популяцію випадковими розв'язками
     * @param dimensions кількість змінних
     * @param bounds межі для кожної змінної
     * @param populationSize розмір популяції
     * @return початкова популяція
     */
    private static double[][] initializePopulation(int dimensions, double[][] bounds, int populationSize) {
        double[][] population = new double[populationSize][dimensions];
        
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < dimensions; j++) {
                double min = bounds[j][0];
                double max = bounds[j][1];
                population[i][j] = min + random.nextDouble() * (max - min);
            }
        }
        
        return population;
    }
    
    /**
     * вибір батька методом турніру
     * @param fitness масив придатностей
     * @param tournamentSize розмір турніру
     * @return індекс вибраного батька
     */
    private static int tournamentSelection(double[] fitness, int tournamentSize) {
        int bestIndex = random.nextInt(fitness.length);
        double bestFitness = fitness[bestIndex];
        
        for (int i = 1; i < tournamentSize; i++) {
            int candidateIndex = random.nextInt(fitness.length);
            if (fitness[candidateIndex] < bestFitness) {
                bestIndex = candidateIndex;
                bestFitness = fitness[candidateIndex];
            }
        }
        
        return bestIndex;
    }
    
    /**
     * схрещування двох батьків
     * @param parent1 перший батько
     * @param parent2 другий батько
     * @return нащадок
     */
    private static double[] crossover(double[] parent1, double[] parent2) {
        double[] offspring = new double[parent1.length];
        
        for (int i = 0; i < parent1.length; i++) {
            if (random.nextBoolean()) {
                offspring[i] = parent1[i];
            } else {
                offspring[i] = parent2[i];
            }
        }
        
        return offspring;
    }
    
    /**
     * мутація розв'язку
     * @param solution розв'язок
     * @param bounds межі для кожної змінної
     * @return змутований розв'язок
     */
    private static double[] mutate(double[] solution, double[][] bounds) {
        double[] mutated = solution.clone();
        int geneIndex = random.nextInt(mutated.length);
        
        double min = bounds[geneIndex][0];
        double max = bounds[geneIndex][1];
        mutated[geneIndex] = min + random.nextDouble() * (max - min);
        
        return mutated;
    }
    
    /**
     * забезпечити межі для розв'язку
     * @param solution розв'язок
     * @param bounds межі для кожної змінної
     */
    private static void enforceBounds(double[] solution, double[][] bounds) {
        for (int i = 0; i < solution.length; i++) {
            double min = bounds[i][0];
            double max = bounds[i][1];
            solution[i] = Math.max(min, Math.min(max, solution[i]));
        }
    }
    
    /**
     * знайти мінімум функції багатьох змінних генетичним алгоритмом з умовчальними параметрами
     * @param function функція для мінімізації
     * @param dimensions кількість змінних
     * @param bounds межі для кожної змінної (масив пар [min, max])
     * @return результат оптимізації
     */
    public static OptimizationResult minimizeGeneticAlgorithm(
            OptimizationFunction function, int dimensions, double[][] bounds) {
        return minimizeGeneticAlgorithm(function, dimensions, bounds, 50, 0.1, 0.8, 100);
    }
    
    /**
     * максимізувати функцію шляхом мінімізації її негативу
     * @param function функція для максимізації
     * @param initialGuess початкове наближення
     * @return результат оптимізації
     */
    public static OptimizationResult maximize(OptimizationFunction function, double[] initialGuess) {
        OptimizationFunction negativeFunction = variables -> -function.apply(variables);
        OptimizationResult result = minimizeGradientDescent(negativeFunction, initialGuess);
        return new OptimizationResult(result.getVariables(), -result.getValue(), result.isConverged(), result.getIterations());
    }
}