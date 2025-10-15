package com.sparky.libx.ml;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * просунуті алгоритми машинного навчання
 * включає регресію, класифікацію, кластеризацію та нейронні мережі
 * @author Андрій Будильников
 */
public class AdvancedML {
    
    /**
     * лінійна регресія
     */
    public static class LinearRegression {
        private double[] weights;
        private double bias;
        private double learningRate;
        private int epochs;
        
        public LinearRegression(double learningRate, int epochs) {
            this.learningRate = learningRate;
            this.epochs = epochs;
        }
        
        /**
         * навчити модель
         * @param X матриця ознак
         * @param y цільові значення
         */
        public void fit(double[][] X, double[] y) {
            int nSamples = X.length;
            int nFeatures = X[0].length;
            
            // ініціалізувати ваги
            weights = new double[nFeatures];
            bias = 0;
            
            // градієнтний спуск
            for (int epoch = 0; epoch < epochs; epoch++) {
                // передбачення
                double[] yPred = predict(X);
                
                // обчислити градієнти
                double[] dw = new double[nFeatures];
                double db = 0;
                
                for (int i = 0; i < nSamples; i++) {
                    double error = yPred[i] - y[i];
                    db += error;
                    for (int j = 0; j < nFeatures; j++) {
                        dw[j] += error * X[i][j];
                    }
                }
                
                // оновити ваги
                for (int j = 0; j < nFeatures; j++) {
                    weights[j] -= learningRate * dw[j] / nSamples;
                }
                bias -= learningRate * db / nSamples;
            }
        }
        
        /**
         * зробити передбачення
         * @param X матриця ознак
         * @return передбачені значення
         */
        public double[] predict(double[][] X) {
            int nSamples = X.length;
            double[] predictions = new double[nSamples];
            
            for (int i = 0; i < nSamples; i++) {
                predictions[i] = bias;
                for (int j = 0; j < weights.length; j++) {
                    predictions[i] += weights[j] * X[i][j];
                }
            }
            
            return predictions;
        }
        
        /**
         * обчислити середню квадратичну помилку
         * @param yTrue справжні значення
         * @param yPred передбачені значення
         * @return MSE
         */
        public double meanSquaredError(double[] yTrue, double[] yPred) {
            double sum = 0;
            for (int i = 0; i < yTrue.length; i++) {
                double error = yTrue[i] - yPred[i];
                sum += error * error;
            }
            return sum / yTrue.length;
        }
        
        /**
         * отримати ваги
         * @return ваги
         */
        public double[] getWeights() {
            return weights.clone();
        }
        
        /**
         * отримати зміщення
         * @return зміщення
         */
        public double getBias() {
            return bias;
        }
    }
    
    /**
     * логістична регресія
     */
    public static class LogisticRegression {
        private double[] weights;
        private double bias;
        private double learningRate;
        private int epochs;
        
        public LogisticRegression(double learningRate, int epochs) {
            this.learningRate = learningRate;
            this.epochs = epochs;
        }
        
        /**
         * навчити модель
         * @param X матриця ознак
         * @param y цільові значення (0 або 1)
         */
        public void fit(double[][] X, double[] y) {
            int nSamples = X.length;
            int nFeatures = X[0].length;
            
            // ініціалізувати ваги
            weights = new double[nFeatures];
            bias = 0;
            
            // градієнтний спуск
            for (int epoch = 0; epoch < epochs; epoch++) {
                // передбачення (сигмоїда)
                double[] linearPred = linearPredict(X);
                double[] yPred = sigmoid(linearPred);
                
                // обчислити градієнти
                double[] dw = new double[nFeatures];
                double db = 0;
                
                for (int i = 0; i < nSamples; i++) {
                    double error = yPred[i] - y[i];
                    db += error;
                    for (int j = 0; j < nFeatures; j++) {
                        dw[j] += error * X[i][j];
                    }
                }
                
                // оновити ваги
                for (int j = 0; j < nFeatures; j++) {
                    weights[j] -= learningRate * dw[j] / nSamples;
                }
                bias -= learningRate * db / nSamples;
            }
        }
        
        /**
         * лінійне передбачення
         */
        private double[] linearPredict(double[][] X) {
            int nSamples = X.length;
            double[] predictions = new double[nSamples];
            
            for (int i = 0; i < nSamples; i++) {
                predictions[i] = bias;
                for (int j = 0; j < weights.length; j++) {
                    predictions[i] += weights[j] * X[i][j];
                }
            }
            
            return predictions;
        }
        
        /**
         * сигмоїдальна функція
         */
        private double[] sigmoid(double[] x) {
            double[] result = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                result[i] = 1 / (1 + Math.exp(-x[i]));
            }
            return result;
        }
        
        /**
         * зробити передбачення
         * @param X матриця ознак
         * @return передбачені значення (0 або 1)
         */
        public double[] predict(double[][] X) {
            double[] linearPred = linearPredict(X);
            double[] probabilities = sigmoid(linearPred);
            
            double[] predictions = new double[probabilities.length];
            for (int i = 0; i < probabilities.length; i++) {
                predictions[i] = probabilities[i] > 0.5 ? 1 : 0;
            }
            
            return predictions;
        }
        
        /**
         * зробити передбачення з ймовірностями
         * @param X матриця ознак
         * @return ймовірності
         */
        public double[] predict_proba(double[][] X) {
            double[] linearPred = linearPredict(X);
            return sigmoid(linearPred);
        }
        
        /**
         * обчислити точність
         * @param yTrue справжні значення
         * @param yPred передбачені значення
         * @return точність
         */
        public double accuracy(double[] yTrue, double[] yPred) {
            int correct = 0;
            for (int i = 0; i < yTrue.length; i++) {
                if (yTrue[i] == yPred[i]) {
                    correct++;
                }
            }
            return (double) correct / yTrue.length;
        }
        
        /**
         * отримати ваги
         * @return ваги
         */
        public double[] getWeights() {
            return weights.clone();
        }
        
        /**
         * отримати зміщення
         * @return зміщення
         */
        public double getBias() {
            return bias;
        }
    }
    
    /**
     * k-найближчих сусідів
     */
    public static class KNearestNeighbors {
        private final int k;
        private double[][] X_train;
        private double[] y_train;
        
        public KNearestNeighbors(int k) {
            this.k = k;
        }
        
        /**
         * навчити модель (просто зберегти навчальні дані)
         * @param X матриця ознак
         * @param y цільові значення
         */
        public void fit(double[][] X, double[] y) {
            this.X_train = X.clone();
            this.y_train = y.clone();
        }
        
        /**
         * зробити передбачення
         * @param X матриця ознак
         * @return передбачені значення
         */
        public double[] predict(double[][] X) {
            double[] predictions = new double[X.length];
            
            for (int i = 0; i < X.length; i++) {
                predictions[i] = predictSingle(X[i]);
            }
            
            return predictions;
        }
        
        /**
         * передбачити для одного зразка
         */
        private double predictSingle(double[] x) {
            // обчислити відстані до всіх навчальних зразків
            double[] distances = new double[X_train.length];
            for (int i = 0; i < X_train.length; i++) {
                distances[i] = euclideanDistance(x, X_train[i]);
            }
            
            // знайти k найближчих сусідів
            int[] indices = IntStream.range(0, distances.length)
                .boxed()
                .sorted((i, j) -> Double.compare(distances[i], distances[j]))
                .mapToInt(Integer::intValue)
                .limit(k)
                .toArray();
            
            // для регресії - середнє значення, для класифікації - мода
            double sum = 0;
            for (int index : indices) {
                sum += y_train[index];
            }
            
            return sum / k;
        }
        
        /**
         * евклідова відстань
         */
        private double euclideanDistance(double[] a, double[] b) {
            double sum = 0;
            for (int i = 0; i < a.length; i++) {
                double diff = a[i] - b[i];
                sum += diff * diff;
            }
            return Math.sqrt(sum);
        }
    }
    
    /**
     * k-середніх (k-means)
     */
    public static class KMeans {
        private final int k;
        private final int maxIterations;
        private final double tolerance;
        private double[][] centroids;
        private int[] labels;
        
        public KMeans(int k, int maxIterations, double tolerance) {
            this.k = k;
            this.maxIterations = maxIterations;
            this.tolerance = tolerance;
        }
        
        /**
         * навчити модель
         * @param X матриця ознак
         */
        public void fit(double[][] X) {
            int nSamples = X.length;
            int nFeatures = X[0].length;
            
            // ініціалізувати центроїди випадково
            centroids = new double[k][nFeatures];
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nFeatures; j++) {
                    centroids[i][j] = ThreadLocalRandom.current().nextDouble(-10, 10);
                }
            }
            
            // ітераційне оновлення
            for (int iteration = 0; iteration < maxIterations; iteration++) {
                // призначити кожен зразок до найближчого центроїда
                labels = new int[nSamples];
                for (int i = 0; i < nSamples; i++) {
                    labels[i] = getClosestCentroid(X[i]);
                }
                
                // оновити центроїди
                double[][] newCentroids = new double[k][nFeatures];
                int[] counts = new int[k];
                
                for (int i = 0; i < nSamples; i++) {
                    int cluster = labels[i];
                    counts[cluster]++;
                    for (int j = 0; j < nFeatures; j++) {
                        newCentroids[cluster][j] += X[i][j];
                    }
                }
                
                // обчислити нові центроїди
                boolean converged = true;
                for (int i = 0; i < k; i++) {
                    if (counts[i] > 0) {
                        for (int j = 0; j < nFeatures; j++) {
                            double newCentroid = newCentroids[i][j] / counts[i];
                            if (Math.abs(newCentroid - centroids[i][j]) > tolerance) {
                                converged = false;
                            }
                            centroids[i][j] = newCentroid;
                        }
                    }
                }
                
                if (converged) {
                    break;
                }
            }
        }
        
        /**
         * знайти найближчий центроїд
         */
        private int getClosestCentroid(double[] x) {
            double minDistance = Double.MAX_VALUE;
            int closestCentroid = 0;
            
            for (int i = 0; i < k; i++) {
                double distance = euclideanDistance(x, centroids[i]);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCentroid = i;
                }
            }
            
            return closestCentroid;
        }
        
        /**
         * евклідова відстань
         */
        private double euclideanDistance(double[] a, double[] b) {
            double sum = 0;
            for (int i = 0; i < a.length; i++) {
                double diff = a[i] - b[i];
                sum += diff * diff;
            }
            return Math.sqrt(sum);
        }
        
        /**
         * зробити передбачення
         * @param X матриця ознак
         * @return мітки кластерів
         */
        public int[] predict(double[][] X) {
            int[] predictions = new int[X.length];
            for (int i = 0; i < X.length; i++) {
                predictions[i] = getClosestCentroid(X[i]);
            }
            return predictions;
        }
        
        /**
         * отримати центроїди
         * @return центроїди
         */
        public double[][] getCentroids() {
            return centroids.clone();
        }
        
        /**
         * отримати мітки
         * @return мітки
         */
        public int[] getLabels() {
            return labels.clone();
        }
        
        /**
         * обчислити внутрішньокластерну суму квадратів
         * @param X матриця ознак
         * @return WCSS
         */
        public double calculateWCSS(double[][] X) {
            double wcss = 0;
            for (int i = 0; i < X.length; i++) {
                int cluster = labels[i];
                wcss += Math.pow(euclideanDistance(X[i], centroids[cluster]), 2);
            }
            return wcss;
        }
    }
    
    /**
     * метод опорних векторів (спрощена версія)
     */
    public static class SupportVectorMachine {
        private double[] weights;
        private double bias;
        private double learningRate;
        private int epochs;
        private double lambda; // параметр регуляризації
        
        public SupportVectorMachine(double learningRate, int epochs, double lambda) {
            this.learningRate = learningRate;
            this.epochs = epochs;
            this.lambda = lambda;
        }
        
        /**
         * навчити модель
         * @param X матриця ознак
         * @param y цільові значення (-1 або 1)
         */
        public void fit(double[][] X, double[] y) {
            int nSamples = X.length;
            int nFeatures = X[0].length;
            
            // ініціалізувати ваги
            weights = new double[nFeatures];
            bias = 0;
            
            // стохастичний градієнтний спуск
            for (int epoch = 0; epoch < epochs; epoch++) {
                for (int i = 0; i < nSamples; i++) {
                    double condition = y[i] * (dotProduct(X[i], weights) + bias);
                    
                    if (condition >= 1) {
                        // правильно класифіковано
                        for (int j = 0; j < nFeatures; j++) {
                            weights[j] -= learningRate * (2 * lambda * weights[j]);
                        }
                    } else {
                        // неправильно класифіковано
                        for (int j = 0; j < nFeatures; j++) {
                            weights[j] -= learningRate * (2 * lambda * weights[j] - y[i] * X[i][j]);
                        }
                        bias -= learningRate * y[i];
                    }
                }
            }
        }
        
        /**
         * скалярний добуток
         */
        private double dotProduct(double[] a, double[] b) {
            double sum = 0;
            for (int i = 0; i < a.length; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        }
        
        /**
         * зробити передбачення
         * @param X матриця ознак
         * @return передбачені значення (-1 або 1)
         */
        public double[] predict(double[][] X) {
            double[] predictions = new double[X.length];
            for (int i = 0; i < X.length; i++) {
                double linearOutput = dotProduct(X[i], weights) + bias;
                predictions[i] = linearOutput >= 0 ? 1 : -1;
            }
            return predictions;
        }
        
        /**
         * обчислити точність
         * @param yTrue справжні значення
         * @param yPred передбачені значення
         * @return точність
         */
        public double accuracy(double[] yTrue, double[] yPred) {
            int correct = 0;
            for (int i = 0; i < yTrue.length; i++) {
                if (yTrue[i] == yPred[i]) {
                    correct++;
                }
            }
            return (double) correct / yTrue.length;
        }
        
        /**
         * отримати ваги
         * @return ваги
         */
        public double[] getWeights() {
            return weights.clone();
        }
        
        /**
         * отримати зміщення
         * @return зміщення
         */
        public double getBias() {
            return bias;
        }
    }
    
    /**
     * дерево прийняття рішень
     */
    public static class DecisionTree {
        private Node root;
        private final int maxDepth;
        private final int minSamplesSplit;
        
        public DecisionTree(int maxDepth, int minSamplesSplit) {
            this.maxDepth = maxDepth;
            this.minSamplesSplit = minSamplesSplit;
        }
        
        /**
         * вузол дерева
         */
        private static class Node {
            // для внутрішніх вузлів
            int featureIndex;
            double threshold;
            Node left;
            Node right;
            
            // для листків
            boolean isLeaf;
            double value; // для регресії
            int classLabel; // для класифікації
            
            Node() {
                this.isLeaf = false;
            }
            
            Node(double value, int classLabel) {
                this.isLeaf = true;
                this.value = value;
                this.classLabel = classLabel;
            }
        }
        
        /**
         * навчити модель
         * @param X матриця ознак
         * @param y цільові значення
         */
        public void fit(double[][] X, double[] y) {
            root = buildTree(X, y, 0);
        }
        
        /**
         * побудувати дерево рекурсивно
         */
        private Node buildTree(double[][] X, double[] y, int depth) {
            int nSamples = X.length;
            int nFeatures = X[0].length;
            
            // умови зупинки
            if (depth >= maxDepth || nSamples < minSamplesSplit) {
                return createLeafNode(y);
            }
            
            // знайти найкраще розбиття
            SplitResult bestSplit = findBestSplit(X, y);
            
            if (bestSplit == null) {
                return createLeafNode(y);
            }
            
            // створити внутрішній вузол
            Node node = new Node();
            node.featureIndex = bestSplit.featureIndex;
            node.threshold = bestSplit.threshold;
            
            // розділити дані
            SplitData splitData = splitData(X, y, bestSplit.featureIndex, bestSplit.threshold);
            
            // рекурсивно побудувати піддерева
            node.left = buildTree(splitData.leftX, splitData.leftY, depth + 1);
            node.right = buildTree(splitData.rightX, splitData.rightY, depth + 1);
            
            return node;
        }
        
        /**
         * створити листковий вузол
         */
        private Node createLeafNode(double[] y) {
            // для спрощення використовуємо середнє значення (регресія)
            double mean = Arrays.stream(y).average().orElse(0);
            // для класифікації можна використовувати моду
            int majorityClass = (int) Math.round(mean);
            return new Node(mean, majorityClass);
        }
        
        /**
         * знайти найкраще розбиття
         */
        private SplitResult findBestSplit(double[][] X, double[] y) {
            int nSamples = X.length;
            int nFeatures = X[0].length;
            
            double bestGain = -1;
            SplitResult bestSplit = null;
            
            // для кожної ознаки
            for (int featureIndex = 0; featureIndex < nFeatures; featureIndex++) {
                // отримати унікальні значення ознаки
                Set<Double> uniqueValues = new HashSet<>();
                for (int i = 0; i < nSamples; i++) {
                    uniqueValues.add(X[i][featureIndex]);
                }
                
                // для кожного унікального значення
                for (double threshold : uniqueValues) {
                    // обчислити інформаційний приріст
                    double gain = calculateInformationGain(X, y, featureIndex, threshold);
                    
                    if (gain > bestGain) {
                        bestGain = gain;
                        bestSplit = new SplitResult(featureIndex, threshold);
                    }
                }
            }
            
            return bestSplit;
        }
        
        /**
         * обчислити інформаційний приріст
         */
        private double calculateInformationGain(double[][] X, double[] y, int featureIndex, double threshold) {
            // для спрощення використовуємо зменшення дисперсії
            double originalVariance = calculateVariance(y);
            
            SplitData splitData = splitData(X, y, featureIndex, threshold);
            
            if (splitData.leftY.length == 0 || splitData.rightY.length == 0) {
                return 0;
            }
            
            double leftVariance = calculateVariance(splitData.leftY);
            double rightVariance = calculateVariance(splitData.rightY);
            
            double weightedVariance = (splitData.leftY.length * leftVariance + 
                                     splitData.rightY.length * rightVariance) / y.length;
            
            return originalVariance - weightedVariance;
        }
        
        /**
         * обчислити дисперсію
         */
        private double calculateVariance(double[] y) {
            double mean = Arrays.stream(y).average().orElse(0);
            double variance = 0;
            for (double value : y) {
                variance += Math.pow(value - mean, 2);
            }
            return variance / y.length;
        }
        
        /**
         * розділити дані
         */
        private SplitData splitData(double[][] X, double[] y, int featureIndex, double threshold) {
            List<double[]> leftXList = new ArrayList<>();
            List<double[]> rightXList = new ArrayList<>();
            List<Double> leftYList = new ArrayList<>();
            List<Double> rightYList = new ArrayList<>();
            
            for (int i = 0; i < X.length; i++) {
                if (X[i][featureIndex] <= threshold) {
                    leftXList.add(X[i].clone());
                    leftYList.add(y[i]);
                } else {
                    rightXList.add(X[i].clone());
                    rightYList.add(y[i]);
                }
            }
            
            double[][] leftX = leftXList.toArray(new double[0][]);
            double[][] rightX = rightXList.toArray(new double[0][]);
            double[] leftY = leftYList.stream().mapToDouble(Double::doubleValue).toArray();
            double[] rightY = rightYList.stream().mapToDouble(Double::doubleValue).toArray();
            
            return new SplitData(leftX, rightX, leftY, rightY);
        }
        
        /**
         * зробити передбачення
         * @param X матриця ознак
         * @return передбачені значення
         */
        public double[] predict(double[][] X) {
            double[] predictions = new double[X.length];
            for (int i = 0; i < X.length; i++) {
                predictions[i] = predictSingle(X[i]);
            }
            return predictions;
        }
        
        /**
         * передбачити для одного зразка
         */
        private double predictSingle(double[] x) {
            Node node = root;
            while (!node.isLeaf) {
                if (x[node.featureIndex] <= node.threshold) {
                    node = node.left;
                } else {
                    node = node.right;
                }
            }
            return node.value;
        }
        
        /**
         * результат розбиття
         */
        private static class SplitResult {
            final int featureIndex;
            final double threshold;
            
            SplitResult(int featureIndex, double threshold) {
                this.featureIndex = featureIndex;
                this.threshold = threshold;
            }
        }
        
        /**
         * розділені дані
         */
        private static class SplitData {
            final double[][] leftX;
            final double[][] rightX;
            final double[] leftY;
            final double[] rightY;
            
            SplitData(double[][] leftX, double[][] rightX, double[] leftY, double[] rightY) {
                this.leftX = leftX;
                this.rightX = rightX;
                this.leftY = leftY;
                this.rightY = rightY;
            }
        }
    }
    
    /**
     * ансамбль випадкових лісів
     */
    public static class RandomForest {
        private final DecisionTree[] trees;
        private final int nEstimators;
        private final int maxDepth;
        private final int minSamplesSplit;
        
        public RandomForest(int nEstimators, int maxDepth, int minSamplesSplit) {
            this.nEstimators = nEstimators;
            this.maxDepth = maxDepth;
            this.minSamplesSplit = minSamplesSplit;
            this.trees = new DecisionTree[nEstimators];
        }
        
        /**
         * навчити модель
         * @param X матриця ознак
         * @param y цільові значення
         */
        public void fit(double[][] X, double[] y) {
            int nSamples = X.length;
            
            for (int i = 0; i < nEstimators; i++) {
                // створити бутстреп вибірку
                double[][] bootstrapX = new double[nSamples][];
                double[] bootstrapY = new double[nSamples];
                
                for (int j = 0; j < nSamples; j++) {
                    int randomIndex = ThreadLocalRandom.current().nextInt(nSamples);
                    bootstrapX[j] = X[randomIndex].clone();
                    bootstrapY[j] = y[randomIndex];
                }
                
                // створити і навчити дерево
                trees[i] = new DecisionTree(maxDepth, minSamplesSplit);
                trees[i].fit(bootstrapX, bootstrapY);
            }
        }
        
        /**
         * зробити передбачення
         * @param X матриця ознак
         * @return передбачені значення
         */
        public double[] predict(double[][] X) {
            double[] predictions = new double[X.length];
            
            // отримати передбачення від кожного дерева
            double[][] treePredictions = new double[nEstimators][X.length];
            for (int i = 0; i < nEstimators; i++) {
                treePredictions[i] = trees[i].predict(X);
            }
            
            // усереднити передбачення
            for (int i = 0; i < X.length; i++) {
                double sum = 0;
                for (int j = 0; j < nEstimators; j++) {
                    sum += treePredictions[j][i];
                }
                predictions[i] = sum / nEstimators;
            }
            
            return predictions;
        }
    }
    
    /**
     * градієнтний бустинг
     */
    public static class GradientBoosting {
        private final DecisionTree[] trees;
        private final int nEstimators;
        private final double learningRate;
        private final int maxDepth;
        private final int minSamplesSplit;
        private double initialValue;
        
        public GradientBoosting(int nEstimators, double learningRate, int maxDepth, int minSamplesSplit) {
            this.nEstimators = nEstimators;
            this.learningRate = learningRate;
            this.maxDepth = maxDepth;
            this.minSamplesSplit = minSamplesSplit;
            this.trees = new DecisionTree[nEstimators];
        }
        
        /**
         * навчити модель
         * @param X матриця ознак
         * @param y цільові значення
         */
        public void fit(double[][] X, double[] y) {
            int nSamples = X.length;
            
            // ініціалізувати передбачення середнім значенням
            initialValue = Arrays.stream(y).average().orElse(0);
            double[] predictions = new double[nSamples];
            Arrays.fill(predictions, initialValue);
            
            // послідовно додавати дерева
            for (int i = 0; i < nEstimators; i++) {
                // обчислити залишки (градієнти)
                double[] residuals = new double[nSamples];
                for (int j = 0; j < nSamples; j++) {
                    residuals[j] = y[j] - predictions[j];
                }
                
                // навчити дерево на залишках
                trees[i] = new DecisionTree(maxDepth, minSamplesSplit);
                trees[i].fit(X, residuals);
                
                // оновити передбачення
                double[] treePredictions = trees[i].predict(X);
                for (int j = 0; j < nSamples; j++) {
                    predictions[j] += learningRate * treePredictions[j];
                }
            }
        }
        
        /**
         * зробити передбачення
         * @param X матриця ознак
         * @return передбачені значення
         */
        public double[] predict(double[][] X) {
            double[] predictions = new double[X.length];
            Arrays.fill(predictions, initialValue);
            
            for (int i = 0; i < nEstimators; i++) {
                double[] treePredictions = trees[i].predict(X);
                for (int j = 0; j < X.length; j++) {
                    predictions[j] += learningRate * treePredictions[j];
                }
            }
            
            return predictions;
        }
    }
    
    /**
     * наївний байєсів класифікатор
     */
    public static class NaiveBayes {
        private Map<Integer, Double> classPriors;
        private Map<Integer, Map<Integer, double[]>> featureStats; // [mean, std]
        private Set<Integer> classes;
        
        public NaiveBayes() {
            classPriors = new HashMap<>();
            featureStats = new HashMap<>();
            classes = new HashSet<>();
        }
        
        /**
         * навчити модель
         * @param X матриця ознак
         * @param y цільові значення (класи)
         */
        public void fit(double[][] X, int[] y) {
            int nSamples = X.length;
            int nFeatures = X[0].length;
            
            // знайти унікальні класи
            classes = new HashSet<>();
            for (int label : y) {
                classes.add(label);
            }
            
            // обчислити апріорні ймовірності класів
            classPriors = new HashMap<>();
            Map<Integer, Integer> classCounts = new HashMap<>();
            for (int label : y) {
                classCounts.put(label, classCounts.getOrDefault(label, 0) + 1);
            }
            
            for (Map.Entry<Integer, Integer> entry : classCounts.entrySet()) {
                classPriors.put(entry.getKey(), (double) entry.getValue() / nSamples);
            }
            
            // обчислити статистики ознак для кожного класу
            featureStats = new HashMap<>();
            
            for (int classLabel : classes) {
                // знайти індекси зразків цього класу
                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < nSamples; i++) {
                    if (y[i] == classLabel) {
                        indices.add(i);
                    }
                }
                
                // обчислити статистики для кожної ознаки
                Map<Integer, double[]> classFeatureStats = new HashMap<>();
                for (int featureIndex = 0; featureIndex < nFeatures; featureIndex++) {
                    double[] featureValues = new double[indices.size()];
                    for (int i = 0; i < indices.size(); i++) {
                        featureValues[i] = X[indices.get(i)][featureIndex];
                    }
                    
                    double mean = Arrays.stream(featureValues).average().orElse(0);
                    double variance = 0;
                    for (double value : featureValues) {
                        variance += Math.pow(value - mean, 2);
                    }
                    variance /= featureValues.length;
                    double std = Math.sqrt(variance);
                    
                    classFeatureStats.put(featureIndex, new double[]{mean, std});
                }
                
                featureStats.put(classLabel, classFeatureStats);
            }
        }
        
        /**
         * зробити передбачення
         * @param X матриця ознак
         * @return передбачені класи
         */
        public int[] predict(double[][] X) {
            int[] predictions = new int[X.length];
            
            for (int i = 0; i < X.length; i++) {
                predictions[i] = predictSingle(X[i]);
            }
            
            return predictions;
        }
        
        /**
         * передбачити для одного зразка
         */
        private int predictSingle(double[] x) {
            double bestProbability = -1;
            int bestClass = -1;
            
            // для кожного класу обчислити ймовірність
            for (int classLabel : classes) {
                double probability = classPriors.get(classLabel);
                
                // помножити на ймовірності ознак (гауссів розподіл)
                Map<Integer, double[]> classStats = featureStats.get(classLabel);
                for (int featureIndex = 0; featureIndex < x.length; featureIndex++) {
                    double[] stats = classStats.get(featureIndex);
                    double mean = stats[0];
                    double std = stats[1];
                    
                    // гауссів розподіл
                    double featureProbability = (1 / (Math.sqrt(2 * Math.PI) * std)) *
                        Math.exp(-Math.pow(x[featureIndex] - mean, 2) / (2 * Math.pow(std, 2)));
                    
                    probability *= featureProbability;
                }
                
                if (probability > bestProbability) {
                    bestProbability = probability;
                    bestClass = classLabel;
                }
            }
            
            return bestClass;
        }
        
        /**
         * обчислити точність
         * @param yTrue справжні значення
         * @param yPred передбачені значення
         * @return точність
         */
        public double accuracy(int[] yTrue, int[] yPred) {
            int correct = 0;
            for (int i = 0; i < yTrue.length; i++) {
                if (yTrue[i] == yPred[i]) {
                    correct++;
                }
            }
            return (double) correct / yTrue.length;
        }
    }
}