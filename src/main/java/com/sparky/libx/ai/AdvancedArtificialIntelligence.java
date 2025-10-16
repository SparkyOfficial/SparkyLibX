package com.sparky.libx.ai;

// Removed Bukkit-dependent imports
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Advanced Artificial Intelligence Framework for Minecraft Plugins
 * Provides capabilities for neural networks, deep learning, reinforcement learning, and AI algorithms
 * 
 * @author Андрій Будильников
 */
public class AdvancedArtificialIntelligence {
    
    /**
     * Represents a neural network tensor
     */
    public static class Tensor {
        private final double[][][] data;
        private final int[] shape;
        
        public Tensor(int... shape) {
            if (shape.length != 3) {
                throw new IllegalArgumentException("Tensor must be 3-dimensional");
            }
            
            this.shape = Arrays.copyOf(shape, shape.length);
            this.data = new double[shape[0]][shape[1]][shape[2]];
        }
        
        public Tensor(double[][][] data) {
            this.data = data;
            this.shape = new int[]{data.length, data[0].length, data[0][0].length};
        }
        
        public double get(int i, int j, int k) {
            return data[i][j][k];
        }
        
        public void set(int i, int j, int k, double value) {
            data[i][j][k] = value;
        }
        
        public int[] getShape() {
            return Arrays.copyOf(shape, shape.length);
        }
        
        public int getRank() {
            return shape.length;
        }
        
        public Tensor add(Tensor other) {
            if (!Arrays.equals(this.shape, other.shape)) {
                throw new IllegalArgumentException("Tensor shapes must match");
            }
            
            Tensor result = new Tensor(shape);
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    for (int k = 0; k < shape[2]; k++) {
                        result.set(i, j, k, this.get(i, j, k) + other.get(i, j, k));
                    }
                }
            }
            return result;
        }
        
        public Tensor multiply(double scalar) {
            Tensor result = new Tensor(shape);
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    for (int k = 0; k < shape[2]; k++) {
                        result.set(i, j, k, this.get(i, j, k) * scalar);
                    }
                }
            }
            return result;
        }
        
        public Tensor multiply(Tensor other) {
            if (!Arrays.equals(this.shape, other.shape)) {
                throw new IllegalArgumentException("Tensor shapes must match");
            }
            
            Tensor result = new Tensor(shape);
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    for (int k = 0; k < shape[2]; k++) {
                        result.set(i, j, k, this.get(i, j, k) * other.get(i, j, k));
                    }
                }
            }
            return result;
        }
        
        public Tensor transpose() {
            // Simple transpose for 3D tensor (swap first two dimensions)
            Tensor result = new Tensor(shape[1], shape[0], shape[2]);
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    for (int k = 0; k < shape[2]; k++) {
                        result.set(j, i, k, this.get(i, j, k));
                    }
                }
            }
            return result;
        }
        
        public Tensor reshape(int... newShape) {
            int totalElements = 1;
            for (int dim : shape) {
                totalElements *= dim;
            }
            
            int newTotalElements = 1;
            for (int dim : newShape) {
                newTotalElements *= dim;
            }
            
            if (totalElements != newTotalElements) {
                throw new IllegalArgumentException("Cannot reshape tensor to different number of elements");
            }
            
            // Flatten tensor to 1D array
            double[] flat = new double[totalElements];
            int index = 0;
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    for (int k = 0; k < shape[2]; k++) {
                        flat[index++] = this.get(i, j, k);
                    }
                }
            }
            
            // Reshape to new dimensions
            if (newShape.length == 3) {
                double[][][] newData = new double[newShape[0]][newShape[1]][newShape[2]];
                index = 0;
                for (int i = 0; i < newShape[0]; i++) {
                    for (int j = 0; j < newShape[1]; j++) {
                        for (int k = 0; k < newShape[2]; k++) {
                            newData[i][j][k] = flat[index++];
                        }
                    }
                }
                return new Tensor(newData);
            } else {
                throw new IllegalArgumentException("Only 3D reshape supported");
            }
        }
        
        public double sum() {
            double sum = 0;
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    for (int k = 0; k < shape[2]; k++) {
                        sum += this.get(i, j, k);
                    }
                }
            }
            return sum;
        }
        
        public double mean() {
            return sum() / (shape[0] * shape[1] * shape[2]);
        }
        
        public Tensor copy() {
            double[][][] newData = new double[shape[0]][shape[1]][shape[2]];
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    System.arraycopy(this.data[i][j], 0, newData[i][j], 0, shape[2]);
                }
            }
            return new Tensor(newData);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Tensor(shape=").append(Arrays.toString(shape)).append(")\n");
            for (int i = 0; i < Math.min(shape[0], 3); i++) {
                sb.append("Layer ").append(i).append(":\n");
                for (int j = 0; j < Math.min(shape[1], 5); j++) {
                    for (int k = 0; k < Math.min(shape[2], 5); k++) {
                        sb.append(String.format("%8.3f ", this.get(i, j, k)));
                    }
                    sb.append("\n");
                }
                if (shape[1] > 5) sb.append("...\n");
            }
            if (shape[0] > 3) sb.append("...\n");
            return sb.toString();
        }
    }
    
    /**
     * Represents a neural network layer
     */
    public abstract static class Layer {
        protected Tensor weights;
        protected Tensor biases;
        protected Tensor input;
        protected Tensor output;
        protected Tensor gradientWeights;
        protected Tensor gradientBiases;
        
        public abstract Tensor forward(Tensor input);
        public abstract Tensor backward(Tensor gradient);
        
        public Tensor getWeights() {
            return weights;
        }
        
        public Tensor getBiases() {
            return biases;
        }
        
        public Tensor getOutput() {
            return output;
        }
        
        public Tensor getGradientWeights() {
            return gradientWeights;
        }
        
        public Tensor getGradientBiases() {
            return gradientBiases;
        }
    }
    
    /**
     * Represents a dense (fully connected) layer
     */
    public static class DenseLayer extends Layer {
        private final int inputSize;
        private final int outputSize;
        private final ActivationFunction activation;
        
        public DenseLayer(int inputSize, int outputSize, ActivationFunction activation) {
            this.inputSize = inputSize;
            this.outputSize = outputSize;
            this.activation = activation;
            
            // Initialize weights and biases
            this.weights = new Tensor(inputSize, outputSize, 1);
            this.biases = new Tensor(1, outputSize, 1);
            this.gradientWeights = new Tensor(inputSize, outputSize, 1);
            this.gradientBiases = new Tensor(1, outputSize, 1);
            
            // Xavier initialization
            double weightScale = Math.sqrt(2.0 / (inputSize + outputSize));
            for (int i = 0; i < inputSize; i++) {
                for (int j = 0; j < outputSize; j++) {
                    weights.set(i, j, 0, ThreadLocalRandom.current().nextGaussian() * weightScale);
                }
            }
            
            // Initialize biases to zero
            for (int j = 0; j < outputSize; j++) {
                biases.set(0, j, 0, 0);
            }
        }
        
        @Override
        public Tensor forward(Tensor input) {
            this.input = input;
            
            // Perform matrix multiplication: output = input * weights + biases
            Tensor output = new Tensor(1, outputSize, 1);
            
            for (int j = 0; j < outputSize; j++) {
                double sum = biases.get(0, j, 0);
                for (int i = 0; i < inputSize; i++) {
                    sum += input.get(0, i, 0) * weights.get(i, j, 0);
                }
                output.set(0, j, 0, sum);
            }
            
            // Apply activation function
            if (activation != null) {
                for (int j = 0; j < outputSize; j++) {
                    output.set(0, j, 0, activation.apply(output.get(0, j, 0)));
                }
            }
            
            this.output = output;
            return output;
        }
        
        @Override
        public Tensor backward(Tensor gradient) {
            // Calculate gradient with respect to activation
            Tensor activationGradient = gradient.copy();
            if (activation != null) {
                for (int j = 0; j < outputSize; j++) {
                    double outputValue = output.get(0, j, 0);
                    activationGradient.set(0, j, 0, 
                        gradient.get(0, j, 0) * activation.derivative(outputValue));
                }
            }
            
            // Calculate gradient with respect to weights
            for (int i = 0; i < inputSize; i++) {
                for (int j = 0; j < outputSize; j++) {
                    double grad = input.get(0, i, 0) * activationGradient.get(0, j, 0);
                    gradientWeights.set(i, j, 0, gradientWeights.get(i, j, 0) + grad);
                }
            }
            
            // Calculate gradient with respect to biases
            for (int j = 0; j < outputSize; j++) {
                double grad = activationGradient.get(0, j, 0);
                gradientBiases.set(0, j, 0, gradientBiases.get(0, j, 0) + grad);
            }
            
            // Calculate gradient with respect to input
            Tensor inputGradient = new Tensor(1, inputSize, 1);
            for (int i = 0; i < inputSize; i++) {
                double sum = 0;
                for (int j = 0; j < outputSize; j++) {
                    sum += weights.get(i, j, 0) * activationGradient.get(0, j, 0);
                }
                inputGradient.set(0, i, 0, sum);
            }
            
            return inputGradient;
        }
    }
    
    /**
     * Represents a convolutional layer
     */
    public static class ConvolutionalLayer extends Layer {
        private final int inputHeight;
        private final int inputWidth;
        private final int inputDepth;
        private final int filterSize;
        private final int numFilters;
        private final int stride;
        private final int padding;
        private final ActivationFunction activation;
        
        private final int outputHeight;
        private final int outputWidth;
        
        public ConvolutionalLayer(int inputHeight, int inputWidth, int inputDepth,
                                int filterSize, int numFilters, int stride, int padding,
                                ActivationFunction activation) {
            this.inputHeight = inputHeight;
            this.inputWidth = inputWidth;
            this.inputDepth = inputDepth;
            this.filterSize = filterSize;
            this.numFilters = numFilters;
            this.stride = stride;
            this.padding = padding;
            this.activation = activation;
            
            this.outputHeight = (inputHeight - filterSize + 2 * padding) / stride + 1;
            this.outputWidth = (inputWidth - filterSize + 2 * padding) / stride + 1;
            
            // Initialize filters and biases
            this.weights = new Tensor(filterSize, filterSize, inputDepth * numFilters);
            this.biases = new Tensor(1, 1, numFilters);
            this.gradientWeights = new Tensor(filterSize, filterSize, inputDepth * numFilters);
            this.gradientBiases = new Tensor(1, 1, numFilters);
            
            // Xavier initialization for filters
            double weightScale = Math.sqrt(2.0 / (filterSize * filterSize * inputDepth));
            for (int f = 0; f < numFilters; f++) {
                for (int i = 0; i < filterSize; i++) {
                    for (int j = 0; j < filterSize; j++) {
                        for (int d = 0; d < inputDepth; d++) {
                            int index = f * inputDepth + d;
                            weights.set(i, j, index, ThreadLocalRandom.current().nextGaussian() * weightScale);
                        }
                    }
                }
            }
            
            // Initialize biases to zero
            for (int f = 0; f < numFilters; f++) {
                biases.set(0, 0, f, 0);
            }
        }
        
        @Override
        public Tensor forward(Tensor input) {
            this.input = input;
            Tensor output = new Tensor(outputHeight, outputWidth, numFilters);
            
            // Apply convolution
            for (int f = 0; f < numFilters; f++) {
                for (int i = 0; i < outputHeight; i++) {
                    for (int j = 0; j < outputWidth; j++) {
                        double sum = biases.get(0, 0, f);
                        
                        // Apply filter at position (i, j)
                        for (int fi = 0; fi < filterSize; fi++) {
                            for (int fj = 0; fj < filterSize; fj++) {
                                int inputI = i * stride + fi - padding;
                                int inputJ = j * stride + fj - padding;
                                
                                if (inputI >= 0 && inputI < inputHeight && 
                                    inputJ >= 0 && inputJ < inputWidth) {
                                    for (int d = 0; d < inputDepth; d++) {
                                        int weightIndex = f * inputDepth + d;
                                        sum += input.get(inputI, inputJ, d) * 
                                               weights.get(fi, fj, weightIndex);
                                    }
                                }
                            }
                        }
                        
                        output.set(i, j, f, sum);
                    }
                }
            }
            
            // Apply activation function
            if (activation != null) {
                for (int i = 0; i < outputHeight; i++) {
                    for (int j = 0; j < outputWidth; j++) {
                        for (int f = 0; f < numFilters; f++) {
                            double value = output.get(i, j, f);
                            output.set(i, j, f, activation.apply(value));
                        }
                    }
                }
            }
            
            this.output = output;
            return output;
        }
        
        @Override
        public Tensor backward(Tensor gradient) {
            // Calculate gradient with respect to activation
            Tensor activationGradient = gradient.copy();
            if (activation != null) {
                for (int i = 0; i < outputHeight; i++) {
                    for (int j = 0; j < outputWidth; j++) {
                        for (int f = 0; f < numFilters; f++) {
                            double outputValue = output.get(i, j, f);
                            activationGradient.set(i, j, f, 
                                gradient.get(i, j, f) * activation.derivative(outputValue));
                        }
                    }
                }
            }
            
            // Calculate gradient with respect to weights
            for (int f = 0; f < numFilters; f++) {
                for (int i = 0; i < outputHeight; i++) {
                    for (int j = 0; j < outputWidth; j++) {
                        double grad = activationGradient.get(i, j, f);
                        
                        for (int fi = 0; fi < filterSize; fi++) {
                            for (int fj = 0; fj < filterSize; fj++) {
                                int inputI = i * stride + fi - padding;
                                int inputJ = j * stride + fj - padding;
                                
                                if (inputI >= 0 && inputI < inputHeight && 
                                    inputJ >= 0 && inputJ < inputWidth) {
                                    for (int d = 0; d < inputDepth; d++) {
                                        int weightIndex = f * inputDepth + d;
                                        double weightGrad = input.get(inputI, inputJ, d) * grad;
                                        gradientWeights.set(fi, fj, weightIndex, 
                                            gradientWeights.get(fi, fj, weightIndex) + weightGrad);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Calculate gradient with respect to biases
            for (int f = 0; f < numFilters; f++) {
                double sum = 0;
                for (int i = 0; i < outputHeight; i++) {
                    for (int j = 0; j < outputWidth; j++) {
                        sum += activationGradient.get(i, j, f);
                    }
                }
                gradientBiases.set(0, 0, f, gradientBiases.get(0, 0, f) + sum);
            }
            
            // Calculate gradient with respect to input
            Tensor inputGradient = new Tensor(inputHeight, inputWidth, inputDepth);
            for (int i = 0; i < inputHeight; i++) {
                for (int j = 0; j < inputWidth; j++) {
                    for (int d = 0; d < inputDepth; d++) {
                        double sum = 0;
                        
                        // Accumulate gradients from all positions where this input pixel was used
                        for (int f = 0; f < numFilters; f++) {
                            int weightIndex = f * inputDepth + d;
                            
                            for (int oi = 0; oi < outputHeight; oi++) {
                                for (int oj = 0; oj < outputWidth; oj++) {
                                    int filterI = i - (oi * stride - padding);
                                    int filterJ = j - (oj * stride - padding);
                                    
                                    if (filterI >= 0 && filterI < filterSize && 
                                        filterJ >= 0 && filterJ < filterSize) {
                                        sum += weights.get(filterI, filterJ, weightIndex) * 
                                               activationGradient.get(oi, oj, f);
                                    }
                                }
                            }
                        }
                        
                        inputGradient.set(i, j, d, sum);
                    }
                }
            }
            
            return inputGradient;
        }
    }
    
    /**
     * Represents a pooling layer
     */
    public static class PoolingLayer extends Layer {
        private final int inputHeight;
        private final int inputWidth;
        private final int inputDepth;
        private final int poolSize;
        private final int stride;
        private final PoolingType poolingType;
        
        private final int outputHeight;
        private final int outputWidth;
        private int[][][] maxIndices; // For max pooling backward pass
        
        public enum PoolingType {
            MAX, AVERAGE
        }
        
        public PoolingLayer(int inputHeight, int inputWidth, int inputDepth,
                          int poolSize, int stride, PoolingType poolingType) {
            this.inputHeight = inputHeight;
            this.inputWidth = inputWidth;
            this.inputDepth = inputDepth;
            this.poolSize = poolSize;
            this.stride = stride;
            this.poolingType = poolingType;
            
            this.outputHeight = (inputHeight - poolSize) / stride + 1;
            this.outputWidth = (inputWidth - poolSize) / stride + 1;
            
            if (poolingType == PoolingType.MAX) {
                this.maxIndices = new int[outputHeight][outputWidth][inputDepth];
            }
        }
        
        @Override
        public Tensor forward(Tensor input) {
            this.input = input;
            Tensor output = new Tensor(outputHeight, outputWidth, inputDepth);
            
            for (int d = 0; d < inputDepth; d++) {
                for (int i = 0; i < outputHeight; i++) {
                    for (int j = 0; j < outputWidth; j++) {
                        double value;
                        
                        if (poolingType == PoolingType.MAX) {
                            value = Double.NEGATIVE_INFINITY;
                            int maxI = 0, maxJ = 0;
                            
                            // Find maximum in pooling window
                            for (int pi = 0; pi < poolSize; pi++) {
                                for (int pj = 0; pj < poolSize; pj++) {
                                    int inputI = i * stride + pi;
                                    int inputJ = j * stride + pj;
                                    
                                    if (inputI < inputHeight && inputJ < inputWidth) {
                                        double inputValue = input.get(inputI, inputJ, d);
                                        if (inputValue > value) {
                                            value = inputValue;
                                            maxI = inputI;
                                            maxJ = inputJ;
                                        }
                                    }
                                }
                            }
                            
                            maxIndices[i][j][d] = maxI * inputWidth + maxJ;
                        } else { // AVERAGE pooling
                            value = 0;
                            int count = 0;
                            
                            // Calculate average in pooling window
                            for (int pi = 0; pi < poolSize; pi++) {
                                for (int pj = 0; pj < poolSize; pj++) {
                                    int inputI = i * stride + pi;
                                    int inputJ = j * stride + pj;
                                    
                                    if (inputI < inputHeight && inputJ < inputWidth) {
                                        value += input.get(inputI, inputJ, d);
                                        count++;
                                    }
                                }
                            }
                            
                            if (count > 0) {
                                value /= count;
                            }
                        }
                        
                        output.set(i, j, d, value);
                    }
                }
            }
            
            this.output = output;
            return output;
        }
        
        @Override
        public Tensor backward(Tensor gradient) {
            Tensor inputGradient = new Tensor(inputHeight, inputWidth, inputDepth);
            
            for (int d = 0; d < inputDepth; d++) {
                for (int i = 0; i < outputHeight; i++) {
                    for (int j = 0; j < outputWidth; j++) {
                        double grad = gradient.get(i, j, d);
                        
                        if (poolingType == PoolingType.MAX) {
                            // Distribute gradient only to the maximum element
                            int index = maxIndices[i][j][d];
                            int maxI = index / inputWidth;
                            int maxJ = index % inputWidth;
                            inputGradient.set(maxI, maxJ, d, 
                                inputGradient.get(maxI, maxJ, d) + grad);
                        } else { // AVERAGE pooling
                            // Distribute gradient equally to all elements in the pooling window
                            double avgGrad = grad / (poolSize * poolSize);
                            
                            for (int pi = 0; pi < poolSize; pi++) {
                                for (int pj = 0; pj < poolSize; pj++) {
                                    int inputI = i * stride + pi;
                                    int inputJ = j * stride + pj;
                                    
                                    if (inputI < inputHeight && inputJ < inputWidth) {
                                        inputGradient.set(inputI, inputJ, d, 
                                            inputGradient.get(inputI, inputJ, d) + avgGrad);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            return inputGradient;
        }
    }
    
    /**
     * Represents an activation function
     */
    public interface ActivationFunction {
        double apply(double x);
        double derivative(double x);
    }
    
    /**
     * Represents the ReLU activation function
     */
    public static class ReLU implements ActivationFunction {
        @Override
        public double apply(double x) {
            return Math.max(0, x);
        }
        
        @Override
        public double derivative(double x) {
            return x > 0 ? 1 : 0;
        }
    }
    
    /**
     * Represents the Sigmoid activation function
     */
    public static class Sigmoid implements ActivationFunction {
        @Override
        public double apply(double x) {
            return 1.0 / (1.0 + Math.exp(-x));
        }
        
        @Override
        public double derivative(double x) {
            double sigmoid = apply(x);
            return sigmoid * (1 - sigmoid);
        }
    }
    
    /**
     * Represents the Tanh activation function
     */
    public static class Tanh implements ActivationFunction {
        @Override
        public double apply(double x) {
            return Math.tanh(x);
        }
        
        @Override
        public double derivative(double x) {
            double tanh = Math.tanh(x);
            return 1 - tanh * tanh;
        }
    }
    
    /**
     * Represents the Softmax activation function
     */
    public static class Softmax implements ActivationFunction {
        private final double[] outputs;
        
        public Softmax(int size) {
            this.outputs = new double[size];
        }
        
        @Override
        public double apply(double x) {
            // Softmax is applied to the entire vector, not individual elements
            throw new UnsupportedOperationException("Softmax must be applied to entire vector");
        }
        
        public double[] apply(double[] x) {
            // Find max for numerical stability
            double max = Arrays.stream(x).max().orElse(0);
            
            // Calculate exponentials
            double[] exps = new double[x.length];
            double sum = 0;
            for (int i = 0; i < x.length; i++) {
                exps[i] = Math.exp(x[i] - max);
                sum += exps[i];
            }
            
            // Calculate softmax
            for (int i = 0; i < x.length; i++) {
                outputs[i] = exps[i] / sum;
            }
            
            return outputs.clone();
        }
        
        @Override
        public double derivative(double x) {
            // Softmax derivative is more complex and depends on the entire vector
            throw new UnsupportedOperationException("Softmax derivative must be calculated for entire vector");
        }
        
        public double[][] jacobian(double[] x) {
            double[] softmaxOutputs = apply(x);
            int n = x.length;
            double[][] jacobian = new double[n][n];
            
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == j) {
                        jacobian[i][j] = softmaxOutputs[i] * (1 - softmaxOutputs[i]);
                    } else {
                        jacobian[i][j] = -softmaxOutputs[i] * softmaxOutputs[j];
                    }
                }
            }
            
            return jacobian;
        }
    }
    
    /**
     * Represents a loss function
     */
    public interface LossFunction {
        double compute(double[] predicted, double[] actual);
        double[] gradient(double[] predicted, double[] actual);
    }
    
    /**
     * Represents the Mean Squared Error loss function
     */
    public static class MeanSquaredError implements LossFunction {
        @Override
        public double compute(double[] predicted, double[] actual) {
            if (predicted.length != actual.length) {
                throw new IllegalArgumentException("Predicted and actual arrays must have same length");
            }
            
            double sum = 0;
            for (int i = 0; i < predicted.length; i++) {
                double diff = predicted[i] - actual[i];
                sum += diff * diff;
            }
            return sum / predicted.length;
        }
        
        @Override
        public double[] gradient(double[] predicted, double[] actual) {
            if (predicted.length != actual.length) {
                throw new IllegalArgumentException("Predicted and actual arrays must have same length");
            }
            
            double[] gradient = new double[predicted.length];
            for (int i = 0; i < predicted.length; i++) {
                gradient[i] = 2 * (predicted[i] - actual[i]) / predicted.length;
            }
            return gradient;
        }
    }
    
    /**
     * Represents the Cross-Entropy loss function
     */
    public static class CrossEntropy implements LossFunction {
        @Override
        public double compute(double[] predicted, double[] actual) {
            if (predicted.length != actual.length) {
                throw new IllegalArgumentException("Predicted and actual arrays must have same length");
            }
            
            double sum = 0;
            for (int i = 0; i < predicted.length; i++) {
                // Add small epsilon to prevent log(0)
                double epsilon = 1e-15;
                double clipped = Math.max(epsilon, Math.min(1 - epsilon, predicted[i]));
                sum -= actual[i] * Math.log(clipped);
            }
            return sum;
        }
        
        @Override
        public double[] gradient(double[] predicted, double[] actual) {
            if (predicted.length != actual.length) {
                throw new IllegalArgumentException("Predicted and actual arrays must have same length");
            }
            
            double[] gradient = new double[predicted.length];
            for (int i = 0; i < predicted.length; i++) {
                // Add small epsilon to prevent division by zero
                double epsilon = 1e-15;
                double clipped = Math.max(epsilon, Math.min(1 - epsilon, predicted[i]));
                gradient[i] = (clipped - actual[i]) / (clipped * (1 - clipped)) / predicted.length;
            }
            return gradient;
        }
    }
    
    /**
     * Represents an optimizer for training neural networks
     */
    public abstract static class Optimizer {
        protected double learningRate;
        
        public Optimizer(double learningRate) {
            this.learningRate = learningRate;
        }
        
        public abstract void updateWeights(Layer layer);
        
        public double getLearningRate() {
            return learningRate;
        }
        
        public void setLearningRate(double learningRate) {
            this.learningRate = learningRate;
        }
    }
    
    /**
     * Represents the Stochastic Gradient Descent optimizer
     */
    public static class SGD extends Optimizer {
        public SGD(double learningRate) {
            super(learningRate);
        }
        
        @Override
        public void updateWeights(Layer layer) {
            Tensor weights = layer.getWeights();
            Tensor biases = layer.getBiases();
            Tensor gradWeights = layer.getGradientWeights();
            Tensor gradBiases = layer.getGradientBiases();
            
            int[] weightShape = weights.getShape();
            int[] biasShape = biases.getShape();
            
            // Update weights
            for (int i = 0; i < weightShape[0]; i++) {
                for (int j = 0; j < weightShape[1]; j++) {
                    for (int k = 0; k < weightShape[2]; k++) {
                        double newWeight = weights.get(i, j, k) - 
                                          learningRate * gradWeights.get(i, j, k);
                        weights.set(i, j, k, newWeight);
                        gradWeights.set(i, j, k, 0); // Reset gradient
                    }
                }
            }
            
            // Update biases
            for (int i = 0; i < biasShape[0]; i++) {
                for (int j = 0; j < biasShape[1]; j++) {
                    for (int k = 0; k < biasShape[2]; k++) {
                        double newBias = biases.get(i, j, k) - 
                                        learningRate * gradBiases.get(i, j, k);
                        biases.set(i, j, k, newBias);
                        gradBiases.set(i, j, k, 0); // Reset gradient
                    }
                }
            }
        }
    }
    
    /**
     * Represents the Adam optimizer
     */
    public static class Adam extends Optimizer {
        private final double beta1;
        private final double beta2;
        private final double epsilon;
        private final Map<Layer, Tensor> mWeights; // First moment vector for weights
        private final Map<Layer, Tensor> vWeights; // Second moment vector for weights
        private final Map<Layer, Tensor> mBiases;  // First moment vector for biases
        private final Map<Layer, Tensor> vBiases;  // Second moment vector for biases
        private final Map<Layer, Integer> t;       // Time step
        private final double decayRate;
        
        public Adam(double learningRate) {
            this(learningRate, 0.9, 0.999, 1e-8, 0.0);
        }
        
        public Adam(double learningRate, double beta1, double beta2, double epsilon, double decayRate) {
            super(learningRate);
            this.beta1 = beta1;
            this.beta2 = beta2;
            this.epsilon = epsilon;
            this.decayRate = decayRate;
            this.mWeights = new HashMap<>();
            this.vWeights = new HashMap<>();
            this.mBiases = new HashMap<>();
            this.vBiases = new HashMap<>();
            this.t = new HashMap<>();
        }
        
        @Override
        public void updateWeights(Layer layer) {
            // Initialize moment vectors if not already done
            if (!mWeights.containsKey(layer)) {
                int[] weightShape = layer.getWeights().getShape();
                int[] biasShape = layer.getBiases().getShape();
                
                mWeights.put(layer, new Tensor(weightShape));
                vWeights.put(layer, new Tensor(weightShape));
                mBiases.put(layer, new Tensor(biasShape));
                vBiases.put(layer, new Tensor(biasShape));
                t.put(layer, 0);
            }
            
            // Increment time step
            t.put(layer, t.get(layer) + 1);
            int timeStep = t.get(layer);
            
            Tensor weights = layer.getWeights();
            Tensor biases = layer.getBiases();
            Tensor gradWeights = layer.getGradientWeights();
            Tensor gradBiases = layer.getGradientBiases();
            
            Tensor mW = mWeights.get(layer);
            Tensor vW = vWeights.get(layer);
            Tensor mB = mBiases.get(layer);
            Tensor vB = vBiases.get(layer);
            
            int[] weightShape = weights.getShape();
            int[] biasShape = biases.getShape();
            
            // Update learning rate with decay
            double currentLearningRate = learningRate * Math.pow(1.0 - decayRate, timeStep);
            
            // Update weights
            for (int i = 0; i < weightShape[0]; i++) {
                for (int j = 0; j < weightShape[1]; j++) {
                    for (int k = 0; k < weightShape[2]; k++) {
                        double grad = gradWeights.get(i, j, k);
                        
                        // Update biased first moment estimate
                        double mWVal = beta1 * mW.get(i, j, k) + (1 - beta1) * grad;
                        mW.set(i, j, k, mWVal);
                        
                        // Update biased second raw moment estimate
                        double vWVal = beta2 * vW.get(i, j, k) + (1 - beta2) * grad * grad;
                        vW.set(i, j, k, vWVal);
                        
                        // Compute bias-corrected first moment estimate
                        double mWCorrected = mWVal / (1 - Math.pow(beta1, timeStep));
                        
                        // Compute bias-corrected second raw moment estimate
                        double vWCorrected = vWVal / (1 - Math.pow(beta2, timeStep));
                        
                        // Update weights
                        double newWeight = weights.get(i, j, k) - 
                                          currentLearningRate * mWCorrected / 
                                          (Math.sqrt(vWCorrected) + epsilon);
                        weights.set(i, j, k, newWeight);
                        
                        gradWeights.set(i, j, k, 0); // Reset gradient
                    }
                }
            }
            
            // Update biases
            for (int i = 0; i < biasShape[0]; i++) {
                for (int j = 0; j < biasShape[1]; j++) {
                    for (int k = 0; k < biasShape[2]; k++) {
                        double grad = gradBiases.get(i, j, k);
                        
                        // Update biased first moment estimate
                        double mBVal = beta1 * mB.get(i, j, k) + (1 - beta1) * grad;
                        mB.set(i, j, k, mBVal);
                        
                        // Update biased second raw moment estimate
                        double vBVal = beta2 * vB.get(i, j, k) + (1 - beta2) * grad * grad;
                        vB.set(i, j, k, vBVal);
                        
                        // Compute bias-corrected first moment estimate
                        double mBCorrected = mBVal / (1 - Math.pow(beta1, timeStep));
                        
                        // Compute bias-corrected second raw moment estimate
                        double vBCorrected = vBVal / (1 - Math.pow(beta2, timeStep));
                        
                        // Update biases
                        double newBias = biases.get(i, j, k) - 
                                        currentLearningRate * mBCorrected / 
                                        (Math.sqrt(vBCorrected) + epsilon);
                        biases.set(i, j, k, newBias);
                        
                        gradBiases.set(i, j, k, 0); // Reset gradient
                    }
                }
            }
        }
    }
    
    /**
     * Represents a neural network model
     */
    public static class NeuralNetwork {
        private final List<Layer> layers;
        private final LossFunction lossFunction;
        private final Optimizer optimizer;
        private final List<Double> trainingLossHistory;
        
        public NeuralNetwork(LossFunction lossFunction, Optimizer optimizer) {
            this.layers = new ArrayList<>();
            this.lossFunction = lossFunction;
            this.optimizer = optimizer;
            this.trainingLossHistory = new ArrayList<>();
        }
        
        /**
         * Adds a layer to the network
         */
        public void addLayer(Layer layer) {
            layers.add(layer);
        }
        
        /**
         * Performs forward propagation through the network
         */
        public Tensor forward(Tensor input) {
            Tensor current = input;
            for (Layer layer : layers) {
                current = layer.forward(current);
            }
            return current;
        }
        
        /**
         * Performs backward propagation through the network
         */
        public void backward(Tensor gradient) {
            Tensor currentGradient = gradient;
            // Propagate backwards through layers
            for (int i = layers.size() - 1; i >= 0; i--) {
                currentGradient = layers.get(i).backward(currentGradient);
            }
        }
        
        /**
         * Trains the network on a batch of data
         */
        public double trainBatch(Tensor[] inputs, Tensor[] targets) {
            if (inputs.length != targets.length) {
                throw new IllegalArgumentException("Input and target arrays must have same length");
            }
            
            double totalLoss = 0;
            
            // Forward pass
            Tensor[] predictions = new Tensor[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                predictions[i] = forward(inputs[i]);
            }
            
            // Calculate loss and gradients
            for (int i = 0; i < inputs.length; i++) {
                // Convert tensors to arrays for loss calculation
                double[] predictedArray = tensorToArray(predictions[i]);
                double[] targetArray = tensorToArray(targets[i]);
                
                double loss = lossFunction.compute(predictedArray, targetArray);
                totalLoss += loss;
                
                double[] lossGradient = lossFunction.gradient(predictedArray, targetArray);
                Tensor gradientTensor = arrayToTensor(lossGradient, predictions[i].getShape());
                
                // Backward pass
                backward(gradientTensor);
            }
            
            // Update weights
            for (Layer layer : layers) {
                optimizer.updateWeights(layer);
            }
            
            double averageLoss = totalLoss / inputs.length;
            trainingLossHistory.add(averageLoss);
            
            return averageLoss;
        }
        
        /**
         * Trains the network for multiple epochs
         */
        public void train(Tensor[] inputs, Tensor[] targets, int epochs, int batchSize) {
            for (int epoch = 0; epoch < epochs; epoch++) {
                // Shuffle data
                int[] indices = IntStream.range(0, inputs.length).toArray();
                shuffleArray(indices);
                
                double totalLoss = 0;
                int batchCount = 0;
                
                // Process batches
                for (int i = 0; i < inputs.length; i += batchSize) {
                    int end = Math.min(i + batchSize, inputs.length);
                    Tensor[] batchInputs = new Tensor[end - i];
                    Tensor[] batchTargets = new Tensor[end - i];
                    
                    for (int j = 0; j < end - i; j++) {
                        batchInputs[j] = inputs[indices[i + j]];
                        batchTargets[j] = targets[indices[i + j]];
                    }
                    
                    double batchLoss = trainBatch(batchInputs, batchTargets);
                    totalLoss += batchLoss;
                    batchCount++;
                }
                
                double averageLoss = totalLoss / batchCount;
                if (epoch % 10 == 0) {
                    System.out.println("Epoch " + epoch + ", Loss: " + averageLoss);
                }
            }
        }
        
        /**
         * Makes predictions on new data
         */
        public Tensor[] predict(Tensor[] inputs) {
            Tensor[] predictions = new Tensor[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                predictions[i] = forward(inputs[i]);
            }
            return predictions;
        }
        
        /**
         * Gets the training loss history
         */
        public List<Double> getTrainingLossHistory() {
            return new ArrayList<>(trainingLossHistory);
        }
        
        /**
         * Converts a tensor to a 1D array
         */
        private double[] tensorToArray(Tensor tensor) {
            int[] shape = tensor.getShape();
            double[] array = new double[shape[0] * shape[1] * shape[2]];
            int index = 0;
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    for (int k = 0; k < shape[2]; k++) {
                        array[index++] = tensor.get(i, j, k);
                    }
                }
            }
            return array;
        }
        
        /**
         * Converts a 1D array to a tensor
         */
        private Tensor arrayToTensor(double[] array, int[] shape) {
            if (array.length != shape[0] * shape[1] * shape[2]) {
                throw new IllegalArgumentException("Array length must match tensor shape");
            }
            
            Tensor tensor = new Tensor(shape);
            int index = 0;
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    for (int k = 0; k < shape[2]; k++) {
                        tensor.set(i, j, k, array[index++]);
                    }
                }
            }
            return tensor;
        }
        
        /**
         * Shuffles an array
         */
        private void shuffleArray(int[] array) {
            Random random = new Random();
            for (int i = array.length - 1; i > 0; i--) {
                int index = random.nextInt(i + 1);
                int temp = array[index];
                array[index] = array[i];
                array[i] = temp;
            }
        }
    }
    
    /**
     * Represents a reinforcement learning agent
     */
    public static class ReinforcementLearningAgent {
        private final int stateSize;
        private final int actionSize;
        private final NeuralNetwork policyNetwork;
        private final NeuralNetwork valueNetwork;
        private final double gamma; // Discount factor
        private final double epsilon; // Exploration rate
        private final List<Experience> experienceBuffer;
        private final int bufferSize;
        private final Random random;
        
        /**
         * Represents an experience tuple (state, action, reward, next_state, done)
         */
        public static class Experience {
            private final Tensor state;
            private final int action;
            private final double reward;
            private final Tensor nextState;
            private final boolean done;
            
            public Experience(Tensor state, int action, double reward, Tensor nextState, boolean done) {
                this.state = state;
                this.action = action;
                this.reward = reward;
                this.nextState = nextState;
                this.done = done;
            }
            
            public Tensor getState() {
                return state;
            }
            
            public int getAction() {
                return action;
            }
            
            public double getReward() {
                return reward;
            }
            
            public Tensor getNextState() {
                return nextState;
            }
            
            public boolean isDone() {
                return done;
            }
        }
        
        public ReinforcementLearningAgent(int stateSize, int actionSize, double gamma, double epsilon, int bufferSize) {
            this.stateSize = stateSize;
            this.actionSize = actionSize;
            this.gamma = gamma;
            this.epsilon = epsilon;
            this.bufferSize = bufferSize;
            this.experienceBuffer = new ArrayList<>();
            this.random = new Random();
            
            // Create policy network (actor)
            this.policyNetwork = new NeuralNetwork(new CrossEntropy(), new Adam(0.001));
            this.policyNetwork.addLayer(new DenseLayer(stateSize, 64, new ReLU()));
            this.policyNetwork.addLayer(new DenseLayer(64, 64, new ReLU()));
            this.policyNetwork.addLayer(new DenseLayer(64, actionSize, null)); // No activation for logits
            
            // Create value network (critic)
            this.valueNetwork = new NeuralNetwork(new MeanSquaredError(), new Adam(0.001));
            this.valueNetwork.addLayer(new DenseLayer(stateSize, 64, new ReLU()));
            this.valueNetwork.addLayer(new DenseLayer(64, 64, new ReLU()));
            this.valueNetwork.addLayer(new DenseLayer(64, 1, null));
        }
        
        /**
         * Selects an action based on the current policy
         */
        public int selectAction(Tensor state) {
            // Epsilon-greedy exploration
            if (random.nextDouble() < epsilon) {
                return random.nextInt(actionSize);
            }
            
            // Use policy network to select action
            Tensor actionProbabilities = policyNetwork.forward(state);
            double[] probs = softmax(actionProbabilities);
            
            // Sample action from probability distribution
            double rand = random.nextDouble();
            double cumulative = 0;
            for (int i = 0; i < actionSize; i++) {
                cumulative += probs[i];
                if (rand < cumulative) {
                    return i;
                }
            }
            
            return actionSize - 1; // Fallback
        }
        
        /**
         * Stores an experience in the replay buffer
         */
        public void storeExperience(Tensor state, int action, double reward, Tensor nextState, boolean done) {
            Experience experience = new Experience(state, action, reward, nextState, done);
            experienceBuffer.add(experience);
            
            // Remove oldest experience if buffer is full
            if (experienceBuffer.size() > bufferSize) {
                experienceBuffer.remove(0);
            }
        }
        
        /**
         * Trains the agent using experiences from the replay buffer
         */
        public void train(int batchSize) {
            if (experienceBuffer.size() < batchSize) {
                return;
            }
            
            // Sample random batch from replay buffer
            Collections.shuffle(experienceBuffer);
            List<Experience> batch = experienceBuffer.subList(0, batchSize);
            
            // Prepare training data
            Tensor[] states = new Tensor[batchSize];
            Tensor[] actions = new Tensor[batchSize];
            double[] rewards = new double[batchSize];
            Tensor[] nextStates = new Tensor[batchSize];
            boolean[] dones = new boolean[batchSize];
            
            for (int i = 0; i < batchSize; i++) {
                Experience exp = batch.get(i);
                states[i] = exp.getState();
                actions[i] = new Tensor(1, actionSize, 1);
                actions[i].set(0, exp.getAction(), 0, 1.0);
                rewards[i] = exp.getReward();
                nextStates[i] = exp.getNextState();
                dones[i] = exp.isDone();
            }
            
            // Train value network (critic)
            trainValueNetwork(states, rewards, nextStates, dones);
            
            // Train policy network (actor)
            trainPolicyNetwork(states, actions);
        }
        
        /**
         * Trains the value network using TD learning
         */
        private void trainValueNetwork(Tensor[] states, double[] rewards, Tensor[] nextStates, boolean[] dones) {
            Tensor[] targets = new Tensor[states.length];
            
            for (int i = 0; i < states.length; i++) {
                double target;
                if (dones[i]) {
                    target = rewards[i];
                } else {
                    Tensor nextStateValue = valueNetwork.forward(nextStates[i]);
                    target = rewards[i] + gamma * nextStateValue.get(0, 0, 0);
                }
                
                targets[i] = new Tensor(1, 1, 1);
                targets[i].set(0, 0, 0, target);
            }
            
            // Train value network
            valueNetwork.train(states, targets, 1, states.length);
        }
        
        /**
         * Trains the policy network using policy gradient
         */
        private void trainPolicyNetwork(Tensor[] states, Tensor[] actions) {
            // This is a simplified implementation
            // In practice, you would use more sophisticated policy gradient methods
            policyNetwork.train(states, actions, 1, states.length);
        }
        
        /**
         * Applies softmax to a tensor
         */
        private double[] softmax(Tensor tensor) {
            int[] shape = tensor.getShape();
            double[] array = new double[shape[1]]; // Assuming shape[0] = 1 and shape[2] = 1
            
            for (int i = 0; i < shape[1]; i++) {
                array[i] = tensor.get(0, i, 0);
            }
            
            // Find max for numerical stability
            double max = Arrays.stream(array).max().orElse(0);
            
            // Calculate exponentials
            double[] exps = new double[array.length];
            double sum = 0;
            for (int i = 0; i < array.length; i++) {
                exps[i] = Math.exp(array[i] - max);
                sum += exps[i];
            }
            
            // Calculate softmax
            for (int i = 0; i < array.length; i++) {
                array[i] = exps[i] / sum;
            }
            
            return array;
        }
        
        public double getGamma() {
            return gamma;
        }
        
        public double getEpsilon() {
            return epsilon;
        }
        
        public int getBufferSize() {
            return bufferSize;
        }
    }
    
    /**
     * Represents a genetic algorithm
     */
    public static class GeneticAlgorithm<T> {
        private final int populationSize;
        private final double mutationRate;
        private final double crossoverRate;
        private final int elitismCount;
        private final FitnessFunction<T> fitnessFunction;
        private final CrossoverFunction<T> crossoverFunction;
        private final MutationFunction<T> mutationFunction;
        private final SelectionFunction<T> selectionFunction;
        private final Random random;
        
        public interface FitnessFunction<T> {
            double evaluate(T individual);
        }
        
        public interface CrossoverFunction<T> {
            T crossover(T parent1, T parent2);
        }
        
        public interface MutationFunction<T> {
            T mutate(T individual);
        }
        
        public interface SelectionFunction<T> {
            T select(List<T> population, List<Double> fitnesses);
        }
        
        public GeneticAlgorithm(int populationSize, double mutationRate, double crossoverRate, int elitismCount,
                              FitnessFunction<T> fitnessFunction, CrossoverFunction<T> crossoverFunction,
                              MutationFunction<T> mutationFunction, SelectionFunction<T> selectionFunction) {
            this.populationSize = populationSize;
            this.mutationRate = mutationRate;
            this.crossoverRate = crossoverRate;
            this.elitismCount = elitismCount;
            this.fitnessFunction = fitnessFunction;
            this.crossoverFunction = crossoverFunction;
            this.mutationFunction = mutationFunction;
            this.selectionFunction = selectionFunction;
            this.random = new Random();
        }
        
        /**
         * Evolves a population for a number of generations
         */
        public List<T> evolve(List<T> initialPopulation, int generations) {
            List<T> population = new ArrayList<>(initialPopulation);
            
            for (int generation = 0; generation < generations; generation++) {
                // Evaluate fitness
                List<Double> fitnesses = new ArrayList<>();
                for (T individual : population) {
                    fitnesses.add(fitnessFunction.evaluate(individual));
                }
                
                // Create new population
                List<T> newPopulation = new ArrayList<>();
                
                // Elitism: carry over best individuals
                List<Integer> sortedIndices = getSortedIndices(fitnesses);
                for (int i = 0; i < elitismCount && i < population.size(); i++) {
                    newPopulation.add(population.get(sortedIndices.get(i)));
                }
                
                // Generate offspring
                while (newPopulation.size() < populationSize) {
                    // Selection
                    T parent1 = selectionFunction.select(population, fitnesses);
                    T parent2 = selectionFunction.select(population, fitnesses);
                    
                    // Crossover
                    T offspring = parent1;
                    if (random.nextDouble() < crossoverRate) {
                        offspring = crossoverFunction.crossover(parent1, parent2);
                    }
                    
                    // Mutation
                    if (random.nextDouble() < mutationRate) {
                        offspring = mutationFunction.mutate(offspring);
                    }
                    
                    newPopulation.add(offspring);
                }
                
                population = newPopulation;
                
                // Print generation statistics
                if (generation % 10 == 0) {
                    double bestFitness = fitnesses.get(sortedIndices.get(0));
                    double averageFitness = fitnesses.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    System.out.println("Generation " + generation + ": Best=" + bestFitness + ", Average=" + averageFitness);
                }
            }
            
            return population;
        }
        
        /**
         * Gets indices sorted by fitness (descending order)
         */
        private List<Integer> getSortedIndices(List<Double> fitnesses) {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < fitnesses.size(); i++) {
                indices.add(i);
            }
            
            indices.sort((i, j) -> Double.compare(fitnesses.get(j), fitnesses.get(i)));
            return indices;
        }
    }
    
    /**
     * Represents a decision tree
     */
    public static class DecisionTree<T> {
        private Node<T> root;
        private final int maxDepth;
        private final int minSamplesSplit;
        private final Random random;
        
        private static class Node<T> {
            // For internal nodes
            int featureIndex;
            double threshold;
            Node<T> left;
            Node<T> right;
            
            // For leaf nodes
            boolean isLeaf;
            T prediction;
            double confidence;
            
            Node() {
                this.isLeaf = false;
                this.featureIndex = -1;
                this.threshold = 0;
                this.left = null;
                this.right = null;
                this.prediction = null;
                this.confidence = 0;
            }
            
            Node(T prediction, double confidence) {
                this.isLeaf = true;
                this.featureIndex = -1;
                this.threshold = 0;
                this.left = null;
                this.right = null;
                this.prediction = prediction;
                this.confidence = confidence;
            }
        }
        
        public DecisionTree(int maxDepth, int minSamplesSplit) {
            this.maxDepth = maxDepth;
            this.minSamplesSplit = minSamplesSplit;
            this.random = new Random();
            this.root = null;
        }
        
        /**
         * Trains the decision tree
         */
        public void train(double[][] features, T[] labels) {
            if (features.length != labels.length) {
                throw new IllegalArgumentException("Features and labels must have same length");
            }
            
            root = buildTree(features, labels, 0);
        }
        
        /**
         * Builds a decision tree recursively
         */
        private Node<T> buildTree(double[][] features, T[] labels, int depth) {
            int nSamples = features.length;
            int nFeatures = features[0].length;
            
            // Stopping criteria
            if (depth >= maxDepth || nSamples < minSamplesSplit || isPure(labels)) {
                return createLeafNode(labels);
            }
            
            // Find best split
            BestSplit bestSplit = findBestSplit(features, labels);
            
            if (bestSplit == null) {
                return createLeafNode(labels);
            }
            
            // Split data
            List<Integer> leftIndices = new ArrayList<>();
            List<Integer> rightIndices = new ArrayList<>();
            
            for (int i = 0; i < nSamples; i++) {
                if (features[i][bestSplit.featureIndex] <= bestSplit.threshold) {
                    leftIndices.add(i);
                } else {
                    rightIndices.add(i);
                }
            }
            
            // Create node
            Node<T> node = new Node<T>();
            node.featureIndex = bestSplit.featureIndex;
            node.threshold = bestSplit.threshold;
            
            // Build subtrees
            double[][] leftFeatures = new double[leftIndices.size()][nFeatures];
            T[] leftLabels = (T[]) new Object[leftIndices.size()];
            for (int i = 0; i < leftIndices.size(); i++) {
                int idx = leftIndices.get(i);
                leftFeatures[i] = features[idx];
                leftLabels[i] = labels[idx];
            }
            
            double[][] rightFeatures = new double[rightIndices.size()][nFeatures];
            T[] rightLabels = (T[]) new Object[rightIndices.size()];
            for (int i = 0; i < rightIndices.size(); i++) {
                int idx = rightIndices.get(i);
                rightFeatures[i] = features[idx];
                rightLabels[i] = labels[idx];
            }
            
            node.left = buildTree(leftFeatures, leftLabels, depth + 1);
            node.right = buildTree(rightFeatures, rightLabels, depth + 1);
            
            return node;
        }
        
        /**
         * Checks if all labels are the same
         */
        private boolean isPure(T[] labels) {
            if (labels.length == 0) return true;
            T first = labels[0];
            for (T label : labels) {
                if (!label.equals(first)) {
                    return false;
                }
            }
            return true;
        }
        
        /**
         * Creates a leaf node
         */
        private Node<T> createLeafNode(T[] labels) {
            // Find most common label
            Map<T, Integer> labelCounts = new HashMap<>();
            for (T label : labels) {
                labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
            }
            
            T bestLabel = null;
            int maxCount = 0;
            for (Map.Entry<T, Integer> entry : labelCounts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    bestLabel = entry.getKey();
                }
            }
            
            double confidence = (double) maxCount / labels.length;
            return new Node<T>(bestLabel, confidence);
        }
        
        /**
         * Finds the best split for the given features and labels
         */
        private BestSplit findBestSplit(double[][] features, T[] labels) {
            int nSamples = features.length;
            int nFeatures = features[0].length;
            
            if (nSamples <= 1) {
                return null;
            }
            
            // Calculate initial Gini impurity
            double bestGini = calculateGini(labels);
            BestSplit bestSplit = null;
            
            // Try all features and all possible thresholds
            for (int featureIndex = 0; featureIndex < nFeatures; featureIndex++) {
                // Get all unique values for this feature
                Set<Double> uniqueValues = new HashSet<>();
                for (int i = 0; i < nSamples; i++) {
                    uniqueValues.add(features[i][featureIndex]);
                }
                
                // Try each unique value as a threshold
                for (double threshold : uniqueValues) {
                    // Split samples based on threshold
                    List<Integer> leftIndices = new ArrayList<>();
                    List<Integer> rightIndices = new ArrayList<>();
                    
                    for (int i = 0; i < nSamples; i++) {
                        if (features[i][featureIndex] <= threshold) {
                            leftIndices.add(i);
                        } else {
                            rightIndices.add(i);
                        }
                    }
                    
                    // Skip if one side is empty
                    if (leftIndices.isEmpty() || rightIndices.isEmpty()) {
                        continue;
                    }
                    
                    // Calculate weighted Gini impurity for this split
                    T[] leftLabels = (T[]) new Object[leftIndices.size()];
                    T[] rightLabels = (T[]) new Object[rightIndices.size()];
                    
                    for (int i = 0; i < leftIndices.size(); i++) {
                        leftLabels[i] = labels[leftIndices.get(i)];
                    }
                    
                    for (int i = 0; i < rightIndices.size(); i++) {
                        rightLabels[i] = labels[rightIndices.get(i)];
                    }
                    
                    double leftGini = calculateGini(leftLabels);
                    double rightGini = calculateGini(rightLabels);
                    
                    double weightedGini = ((double) leftIndices.size() / nSamples) * leftGini +
                                        ((double) rightIndices.size() / nSamples) * rightGini;
                    
                    // Update best split if this is better
                    if (weightedGini < bestGini) {
                        bestGini = weightedGini;
                        bestSplit = new BestSplit(featureIndex, threshold, bestGini);
                    }
                }
            }
            
            return bestSplit;
        }
        
        /**
         * Calculates the Gini impurity for a set of labels
         */
        private double calculateGini(T[] labels) {
            if (labels.length == 0) {
                return 0;
            }
            
            // Count occurrences of each label
            Map<T, Integer> labelCounts = new HashMap<>();
            for (T label : labels) {
                labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
            }
            
            // Calculate Gini impurity
            double gini = 1.0;
            for (int count : labelCounts.values()) {
                double probability = (double) count / labels.length;
                gini -= probability * probability;
            }
            
            return gini;
        }
        
        /**
         * Makes a prediction for a single sample
         */
        public PredictionResult<T> predict(double[] features) {
            if (root == null) {
                throw new IllegalStateException("Model not trained yet");
            }
            
            Node<T> node = root;
            while (!node.isLeaf) {
                if (features[node.featureIndex] <= node.threshold) {
                    node = node.left;
                } else {
                    node = node.right;
                }
            }
            
            return new PredictionResult<T>(node.prediction, node.confidence);
        }
        
        /**
         * Makes predictions for multiple samples
         */
        public List<PredictionResult<T>> predict(double[][] features) {
            List<PredictionResult<T>> predictions = new ArrayList<>();
            for (double[] feature : features) {
                predictions.add(predict(feature));
            }
            return predictions;
        }
        
        /**
         * Represents the best split found
         */
        private static class BestSplit {
            final int featureIndex;
            final double threshold;
            final double gini;
            
            BestSplit(int featureIndex, double threshold, double gini) {
                this.featureIndex = featureIndex;
                this.threshold = threshold;
                this.gini = gini;
            }
        }
        
        /**
         * Represents a prediction result
         */
        public static class PredictionResult<T> {
            private final T prediction;
            private final double confidence;
            
            PredictionResult(T prediction, double confidence) {
                this.prediction = prediction;
                this.confidence = confidence;
            }
            
            public T getPrediction() {
                return prediction;
            }
            
            public double getConfidence() {
                return confidence;
            }
        }
    }
    

    
    /**
     * Represents a support vector machine
     */
    public static class SupportVectorMachine {
        private double[] weights;
        private double bias;
        private final double learningRate;
        private final int epochs;
        private final double regularization;
        
        public SupportVectorMachine(double learningRate, int epochs, double regularization) {
            this.learningRate = learningRate;
            this.epochs = epochs;
            this.regularization = regularization;
            this.weights = null;
            this.bias = 0;
        }
        
        /**
         * Trains the SVM
         */
        public void train(double[][] features, int[] labels) {
            if (features.length != labels.length) {
                throw new IllegalArgumentException("Features and labels must have same length");
            }
            
            int nSamples = features.length;
            int nFeatures = features[0].length;
            
            // Initialize weights
            weights = new double[nFeatures];
            bias = 0;
            
            // Training loop
            for (int epoch = 0; epoch < epochs; epoch++) {
                for (int i = 0; i < nSamples; i++) {
                    double[] x = features[i];
                    int y = labels[i];
                    
                    // Check if update is needed
                    double condition = y * (dotProduct(weights, x) + bias);
                    
                    if (condition >= 1) {
                        // Correct classification, update weights with regularization only
                        for (int j = 0; j < nFeatures; j++) {
                            weights[j] -= learningRate * (2 * regularization * weights[j]);
                        }
                    } else {
                        // Incorrect classification, update weights and bias
                        for (int j = 0; j < nFeatures; j++) {
                            weights[j] -= learningRate * (2 * regularization * weights[j] - (double) y * x[j]);
                        }
                        bias -= learningRate * y;
                    }
                }
            }
        }
        
        /**
         * Makes a prediction for a single sample
         */
        public int predict(double[] features) {
            if (weights == null) {
                throw new IllegalStateException("Model not trained yet");
            }
            
            double linearOutput = dotProduct(weights, features) + bias;
            return linearOutput >= 0 ? 1 : -1;
        }
        
        /**
         * Makes predictions for multiple samples
         */
        public int[] predict(double[][] features) {
            int[] predictions = new int[features.length];
            for (int i = 0; i < features.length; i++) {
                predictions[i] = predict(features[i]);
            }
            return predictions;
        }
        
        /**
         * Calculates the dot product of two vectors
         */
        private double dotProduct(double[] a, double[] b) {
            double sum = 0;
            for (int i = 0; i < a.length; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        }
        
        /**
         * Gets the weights
         */
        public double[] getWeights() {
            return weights != null ? weights.clone() : null;
        }
        
        /**
         * Gets the bias
         */
        public double getBias() {
            return bias;
        }
    }
    
    /**
     * Represents a clustering algorithm (K-means)
     */
    public static class KMeans {
        private final int k;
        private final int maxIterations;
        private final Random random;
        private double[][] centroids;
        private int[] assignments;
        
        public KMeans(int k, int maxIterations) {
            this.k = k;
            this.maxIterations = maxIterations;
            this.random = new Random();
            this.centroids = null;
            this.assignments = null;
        }
        
        /**
         * Fits the K-means model to the data
         */
        public void fit(double[][] data) {
            int nSamples = data.length;
            int nFeatures = data[0].length;
            
            // Initialize centroids randomly
            centroids = new double[k][nFeatures];
            for (int i = 0; i < k; i++) {
                int randomIndex = random.nextInt(nSamples);
                System.arraycopy(data[randomIndex], 0, centroids[i], 0, nFeatures);
            }
            
            assignments = new int[nSamples];
            
            // Iterative optimization
            for (int iteration = 0; iteration < maxIterations; iteration++) {
                // Assign points to closest centroids
                boolean changed = false;
                for (int i = 0; i < nSamples; i++) {
                    int closestCentroid = findClosestCentroid(data[i]);
                    if (assignments[i] != closestCentroid) {
                        assignments[i] = closestCentroid;
                        changed = true;
                    }
                }
                
                // Update centroids
                updateCentroids(data);
                
                // Stop if no assignments changed
                if (!changed) {
                    break;
                }
            }
        }
        
        /**
         * Finds the closest centroid to a point
         */
        private int findClosestCentroid(double[] point) {
            double minDistance = Double.MAX_VALUE;
            int closestCentroid = 0;
            
            for (int i = 0; i < k; i++) {
                double distance = euclideanDistance(point, centroids[i]);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCentroid = i;
                }
            }
            
            return closestCentroid;
        }
        
        /**
         * Updates centroids based on current assignments
         */
        private void updateCentroids(double[][] data) {
            int nFeatures = data[0].length;
            
            // Initialize new centroids
            double[][] newCentroids = new double[k][nFeatures];
            int[] counts = new int[k];
            
            // Sum points for each cluster
            for (int i = 0; i < data.length; i++) {
                int cluster = assignments[i];
                for (int j = 0; j < nFeatures; j++) {
                    newCentroids[cluster][j] += data[i][j];
                }
                counts[cluster]++;
            }
            
            // Calculate means
            for (int i = 0; i < k; i++) {
                if (counts[i] > 0) {
                    for (int j = 0; j < nFeatures; j++) {
                        centroids[i][j] = newCentroids[i][j] / counts[i];
                    }
                }
            }
        }
        
        /**
         * Calculates Euclidean distance between two points
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
         * Predicts cluster assignments for new data
         */
        public int[] predict(double[][] data) {
            if (centroids == null) {
                throw new IllegalStateException("Model not trained yet");
            }
            
            int[] predictions = new int[data.length];
            for (int i = 0; i < data.length; i++) {
                predictions[i] = findClosestCentroid(data[i]);
            }
            return predictions;
        }
        
        /**
         * Gets the centroids
         */
        public double[][] getCentroids() {
            return centroids != null ? centroids.clone() : null;
        }
        
        /**
         * Gets the cluster assignments for training data
         */
        public int[] getAssignments() {
            return assignments != null ? assignments.clone() : null;
        }
    }
}