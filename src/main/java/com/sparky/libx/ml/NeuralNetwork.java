package com.sparky.libx.ml;

import java.util.Arrays;
import java.util.Random;

import com.sparky.libx.math.LinearAlgebra;

/**
 * проста нейронна мережа з одним прихованим шаром
 * @author Андрій Будильников
 */
public class NeuralNetwork {
    
    private final int inputSize;
    private final int hiddenSize;
    private final int outputSize;
    private final double[][] weightsInputHidden;
    private final double[][] weightsHiddenOutput;
    private final double[] biasHidden;
    private final double[] biasOutput;
    private final ActivationFunction activationFunction;
    private final Random random;
    
    /**
     * функціональний інтерфейс для функцій активації
     */
    @FunctionalInterface
    public interface ActivationFunction {
        double apply(double x);
        
        default double derivative(double x) {
            // чисельна похідна за замовчуванням
            double h = 1e-5;
            return (apply(x + h) - apply(x - h)) / (2 * h);
        }
    }
    
    /**
     * сигмоїдальна функція активації
     */
    public static final ActivationFunction SIGMOID = new ActivationFunction() {
        @Override
        public double apply(double x) {
            return 1.0 / (1.0 + Math.exp(-x));
        }
        
        @Override
        public double derivative(double x) {
            double sigmoid = apply(x);
            return sigmoid * (1 - sigmoid);
        }
    };
    
    /**
     * гіперболічний тангенс
     */
    public static final ActivationFunction TANH = new ActivationFunction() {
        @Override
        public double apply(double x) {
            return Math.tanh(x);
        }
        
        @Override
        public double derivative(double x) {
            double tanh = Math.tanh(x);
            return 1 - tanh * tanh;
        }
    };
    
    /**
     * лінійна функція активації (ReLU)
     */
    public static final ActivationFunction RELU = new ActivationFunction() {
        @Override
        public double apply(double x) {
            return Math.max(0, x);
        }
        
        @Override
        public double derivative(double x) {
            return x > 0 ? 1 : 0;
        }
    };
    
    /**
     * створити нову нейронну мережу
     * @param inputSize кількість вхідних нейронів
     * @param hiddenSize кількість прихованих нейронів
     * @param outputSize кількість вихідних нейронів
     * @param activationFunction функція активації
     */
    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize, ActivationFunction activationFunction) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        this.activationFunction = activationFunction;
        this.random = new Random();
        
        // ініціалізувати ваги випадковими значеннями
        this.weightsInputHidden = new double[inputSize][hiddenSize];
        this.weightsHiddenOutput = new double[hiddenSize][outputSize];
        this.biasHidden = new double[hiddenSize];
        this.biasOutput = new double[outputSize];
        
        initializeWeights();
    }
    
    /**
     * ініціалізувати ваги випадковими значеннями
     */
    private void initializeWeights() {
        // ініціалізація Ксав'є
        double inputHiddenStd = Math.sqrt(2.0 / inputSize);
        double hiddenOutputStd = Math.sqrt(2.0 / hiddenSize);
        
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weightsInputHidden[i][j] = random.nextGaussian() * inputHiddenStd;
            }
        }
        
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weightsHiddenOutput[i][j] = random.nextGaussian() * hiddenOutputStd;
            }
        }
        
        // ініціалізувати зміщення нулями
        Arrays.fill(biasHidden, 0);
        Arrays.fill(biasOutput, 0);
    }
    
    /**
     * прямий прохід через мережу
     * @param inputs вхідні дані
     * @return вихідні дані
     */
    public double[] forward(double[] inputs) {
        if (inputs.length != inputSize) {
            throw new IllegalArgumentException("Розмір вхідних даних повинен бути " + inputSize);
        }
        
        // обчислити вихід прихованого шару
        double[] hidden = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = biasHidden[j];
            for (int i = 0; i < inputSize; i++) {
                sum += inputs[i] * weightsInputHidden[i][j];
            }
            hidden[j] = activationFunction.apply(sum);
        }
        
        // обчислити вихід вихідного шару
        double[] output = new double[outputSize];
        for (int k = 0; k < outputSize; k++) {
            double sum = biasOutput[k];
            for (int j = 0; j < hiddenSize; j++) {
                sum += hidden[j] * weightsHiddenOutput[j][k];
            }
            output[k] = sum; // лінійна активація для вихідного шару
        }
        
        return output;
    }
    
    /**
     * навчити мережу методом зворотного поширення помилки
     * @param inputs вхідні дані
     * @param targets цільові значення
     * @param learningRate швидкість навчання
     */
    public void train(double[] inputs, double[] targets, double learningRate) {
        if (inputs.length != inputSize) {
            throw new IllegalArgumentException("Розмір вхідних даних повинен бути " + inputSize);
        }
        
        if (targets.length != outputSize) {
            throw new IllegalArgumentException("Розмір цільових даних повинен бути " + outputSize);
        }
        
        // прямий прохід
        // обчислити вихід прихованого шару
        double[] hidden = new double[hiddenSize];
        double[] hiddenActivated = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = biasHidden[j];
            for (int i = 0; i < inputSize; i++) {
                sum += inputs[i] * weightsInputHidden[i][j];
            }
            hidden[j] = sum;
            hiddenActivated[j] = activationFunction.apply(sum);
        }
        
        // обчислити вихід вихідного шару
        double[] output = new double[outputSize];
        for (int k = 0; k < outputSize; k++) {
            double sum = biasOutput[k];
            for (int j = 0; j < hiddenSize; j++) {
                sum += hiddenActivated[j] * weightsHiddenOutput[j][k];
            }
            output[k] = sum;
        }
        
        // зворотний прохід
        // обчислити помилку вихідного шару
        double[] outputError = new double[outputSize];
        for (int k = 0; k < outputSize; k++) {
            outputError[k] = targets[k] - output[k];
        }
        
        // обчислити дельти для вихідного шару
        double[] outputDelta = outputError; // для лінійної активації похідна = 1
        
        // обчислити помилку прихованого шару
        double[] hiddenError = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = 0;
            for (int k = 0; k < outputSize; k++) {
                sum += outputDelta[k] * weightsHiddenOutput[j][k];
            }
            hiddenError[j] = sum;
        }
        
        // обчислити дельти для прихованого шару
        double[] hiddenDelta = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            hiddenDelta[j] = hiddenError[j] * activationFunction.derivative(hidden[j]);
        }
        
        // оновити ваги між прихованим і вихідним шарами
        for (int j = 0; j < hiddenSize; j++) {
            for (int k = 0; k < outputSize; k++) {
                weightsHiddenOutput[j][k] += learningRate * outputDelta[k] * hiddenActivated[j];
            }
        }
        
        // оновити зміщення вихідного шару
        for (int k = 0; k < outputSize; k++) {
            biasOutput[k] += learningRate * outputDelta[k];
        }
        
        // оновити ваги між вхідним і прихованим шарами
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weightsInputHidden[i][j] += learningRate * hiddenDelta[j] * inputs[i];
            }
        }
        
        // оновити зміщення прихованого шару
        for (int j = 0; j < hiddenSize; j++) {
            biasHidden[j] += learningRate * hiddenDelta[j];
        }
    }
    
    /**
     * навчити мережу на наборі даних
     * @param inputs масив вхідних даних
     * @param targets масив цільових даних
     * @param epochs кількість епох
     * @param learningRate швидкість навчання
     */
    public void trainBatch(double[][] inputs, double[][] targets, int epochs, double learningRate) {
        if (inputs.length != targets.length) {
            throw new IllegalArgumentException("Кількість вхідних і цільових даних повинна бути однаковою");
        }
        
        for (int epoch = 0; epoch < epochs; epoch++) {
            for (int i = 0; i < inputs.length; i++) {
                train(inputs[i], targets[i], learningRate);
            }
        }
    }
    
    /**
     * обчислити середню квадратичну помилку
     * @param inputs масив вхідних даних
     * @param targets масив цільових даних
     * @return середня квадратична помилка
     */
    public double meanSquaredError(double[][] inputs, double[][] targets) {
        if (inputs.length != targets.length) {
            throw new IllegalArgumentException("Кількість вхідних і цільових даних повинна бути однаковою");
        }
        
        double totalError = 0;
        for (int i = 0; i < inputs.length; i++) {
            double[] output = forward(inputs[i]);
            for (int j = 0; j < outputSize; j++) {
                double error = targets[i][j] - output[j];
                totalError += error * error;
            }
        }
        
        return totalError / (inputs.length * outputSize);
    }
    
    /**
     * отримати ваги між вхідним і прихованим шарами
     * @return ваги
     */
    public double[][] getWeightsInputHidden() {
        return weightsInputHidden.clone();
    }
    
    /**
     * отримати ваги між прихованим і вихідним шарами
     * @return ваги
     */
    public double[][] getWeightsHiddenOutput() {
        return weightsHiddenOutput.clone();
    }
    
    /**
     * отримати зміщення прихованого шару
     * @return зміщення
     */
    public double[] getBiasHidden() {
        return biasHidden.clone();
    }
    
    /**
     * отримати зміщення вихідного шару
     * @return зміщення
     */
    public double[] getBiasOutput() {
        return biasOutput.clone();
    }
    
    /**
     * отримати кількість вхідних нейронів
     * @return кількість вхідних нейронів
     */
    public int getInputSize() {
        return inputSize;
    }
    
    /**
     * отримати кількість прихованих нейронів
     * @return кількість прихованих нейронів
     */
    public int getHiddenSize() {
        return hiddenSize;
    }
    
    /**
     * отримати кількість вихідних нейронів
     * @return кількість вихідних нейронів
     */
    public int getOutputSize() {
        return outputSize;
    }
    
    @Override
    public String toString() {
        return String.format("NeuralNetwork{inputSize=%d, hiddenSize=%d, outputSize=%d}", 
                           inputSize, hiddenSize, outputSize);
    }
}