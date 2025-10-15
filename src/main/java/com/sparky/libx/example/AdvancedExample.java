package com.sparky.libx.example;

import java.util.*;

import org.bukkit.plugin.java.JavaPlugin;

import com.sparky.libx.geometry.ComputationalGeometry;
import com.sparky.libx.graph.GraphAlgorithms;
import com.sparky.libx.math.Calculus;
import com.sparky.libx.math.LinearAlgebra;
import com.sparky.libx.math.NoiseGenerator;
import com.sparky.libx.math.Optimization;
import com.sparky.libx.math.SignalProcessing;
import com.sparky.libx.math.Statistics;
import com.sparky.libx.ml.NeuralNetwork;
import com.sparky.libx.physics.PhysicsEngine;
import com.sparky.libx.physics.PhysicsEntity;

/**
 * приклад використання розширених можливостей бібліотеки
 * демонструє складні обчислення, фізику, машинне навчання і багато іншого
 * @author Андрій Будильников
 */
public class AdvancedExample extends JavaPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("AdvancedExample plugin включено!");
        
        // демонстрація різних можливостей бібліотеки
        demonstrateMathematics();
        demonstratePhysics();
        demonstrateMachineLearning();
        demonstrateGraphAlgorithms();
        demonstrateComputationalGeometry();
        demonstrateSignalProcessing();
        demonstrateOptimization();
        demonstrateStatistics();
        
        getLogger().info("Всі демонстрації завершено!");
    }
    
    /**
     * демонстрація математичних можливостей
     */
    private void demonstrateMathematics() {
        getLogger().info("=== Демонстрація математичних можливостей ===");
        
        // лінійна алгебра
        double[][] matrixA = {{1, 2}, {3, 4}};
        double[][] matrixB = {{2, 0}, {1, 2}};
        double[][] product = LinearAlgebra.multiply(matrixA, matrixB);
        getLogger().info("Множення матриць: " + Arrays.deepToString(product));
        
        // обчислити визначник
        double det = LinearAlgebra.determinant(matrixA);
        getLogger().info("Визначник матриці A: " + det);
        
        // чисельне диференціювання
        Calculus.MathFunction function = x -> x * x * x + 2 * x * x - 5 * x + 3;
        double derivative = Calculus.derivative(function, 2.0);
        getLogger().info("Похідна функції в точці x=2: " + derivative);
        
        // інтегрування
        double integral = Calculus.integrateTrapezoid(function, 0, 2, 1000);
        getLogger().info("Інтеграл функції від 0 до 2: " + integral);
    }
    
    /**
     * демонстрація фізичних можливостей
     */
    private void demonstratePhysics() {
        getLogger().info("=== Демонстрація фізичних можливостей ===");
        
        PhysicsEngine engine = PhysicsEngine.getInstance();
        
        // створити фізичну сутність
        PhysicsEntity entity = new PhysicsEntity(
            new com.sparky.libx.math.Vector3D(0, 10, 0), 
            5.0,  // маса
            1.0   // об'єм
        );
        entity.setName("Тестова куля");
        engine.addEntity(entity);
        
        getLogger().info("Створено фізичну сутність: " + entity);
        
        // оновити фізику
        engine.update();
        getLogger().info("Фізичний рушій оновлено, сутностей: " + engine.getEntityCount());
        
        // очистити
        engine.removeEntity(entity.getId());
    }
    
    /**
     * демонстрація можливостей машинного навчання
     */
    private void demonstrateMachineLearning() {
        getLogger().info("=== Демонстрація можливостей машинного навчання ===");
        
        // створити просту нейронну мережу
        NeuralNetwork nn = new NeuralNetwork(2, 3, 1, NeuralNetwork.SIGMOID);
        getLogger().info("Створено нейронну мережу: " + nn);
        
        // навчальні дані (XOR)
        double[][] inputs = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
        double[][] targets = {{0}, {1}, {1}, {0}};
        
        // навчити мережу (для демонстрації просто створюємо)
        getLogger().info("Нейронна мережа готова до навчання на даних XOR");
    }
    
    /**
     * демонстрація алгоритмів на графах
     */
    private void demonstrateGraphAlgorithms() {
        getLogger().info("=== Демонстрація алгоритмів на графах ===");
        
        // створити граф
        GraphAlgorithms.Graph graph = new GraphAlgorithms.Graph(false);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 5);
        graph.addEdge(2, 3, 8);
        graph.addEdge(2, 4, 10);
        graph.addEdge(3, 4, 2);
        
        getLogger().info("Створено граф з " + graph.getVertexCount() + " вершинами і " + graph.getEdgeCount() + " ребрами");
        
        // знайти найкоротший шлях
        GraphAlgorithms.PathResult result = GraphAlgorithms.dijkstra(graph, 0, 4);
        if (result.isFound()) {
            getLogger().info("Найкоротший шлях від 0 до 4: " + result.getPath() + " з відстанню " + result.getDistance());
        }
    }
    
    /**
     * демонстрація обчислювальної геометрії
     */
    private void demonstrateComputationalGeometry() {
        getLogger().info("=== Демонстрація обчислювальної геометрії ===");
        
        // створити точки
        List<ComputationalGeometry.Point2D> points = Arrays.asList(
            new ComputationalGeometry.Point2D(0, 0),
            new ComputationalGeometry.Point2D(1, 0),
            new ComputationalGeometry.Point2D(1, 1),
            new ComputationalGeometry.Point2D(0, 1)
        );
        
        // знайти опуклу оболонку
        List<ComputationalGeometry.Point2D> hull = ComputationalGeometry.convexHull(points);
        getLogger().info("Опукла оболонка: " + hull);
        
        // створити багатокутник
        ComputationalGeometry.Polygon polygon = new ComputationalGeometry.Polygon(points);
        getLogger().info("Площа багатокутника: " + polygon.area());
        getLogger().info("Периметр багатокутника: " + polygon.perimeter());
    }
    
    /**
     * демонстрація обробки сигналів
     */
    private void demonstrateSignalProcessing() {
        getLogger().info("=== Демонстрація обробки сигналів ===");
        
        // створити тестовий сигнал
        double[] signal = {1, 2, 3, 4, 3, 2, 1, 0, -1, -2, -3, -4, -3, -2, -1, 0};
        
        // обчислити DFT
        try {
            SignalProcessing.Complex[] spectrum = SignalProcessing.discreteFourierTransform(signal);
            getLogger().info("DFT обчислено для сигналу довжиною " + signal.length);
            
            // обчислити енергію сигналу
            double energy = SignalProcessing.signalEnergy(signal);
            getLogger().info("Енергія сигналу: " + energy);
        } catch (Exception e) {
            getLogger().info("Не вдалося обчислити DFT: " + e.getMessage());
        }
    }
    
    /**
     * демонстрація оптимізації
     */
    private void demonstrateOptimization() {
        getLogger().info("=== Демонстрація оптимізації ===");
        
        // мінімізувати квадратичну функцію
        Optimization.OptimizationFunction function = variables -> {
            double x = variables[0];
            double y = variables[1];
            return (x - 2) * (x - 2) + (y - 3) * (y - 3) + 1;
        };
        
        double[] initialGuess = {0, 0};
        Optimization.OptimizationResult result = Optimization.minimizeGradientDescent(function, initialGuess);
        
        getLogger().info("Мінімум функції знайдено в точці: " + 
                        Arrays.toString(result.getVariables()) + 
                        " зі значенням: " + result.getValue());
    }
    
    /**
     * демонстрація статистики
     */
    private void demonstrateStatistics() {
        getLogger().info("=== Демонстрація статистики ===");
        
        // створити тестові дані
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        
        // обчислити описову статистику
        double mean = Statistics.mean(data);
        double median = Statistics.median(data);
        double stdDev = Statistics.standardDeviation(data, true);
        
        getLogger().info("Середнє: " + mean);
        getLogger().info("Медіана: " + median);
        getLogger().info("Стандартне відхилення: " + stdDev);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("AdvancedExample plugin вимкнено!");
    }
}