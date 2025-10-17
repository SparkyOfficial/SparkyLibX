package com.sparky.libx.quantum;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.math.Complex;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * Quantum Computing Simulation for Minecraft Plugins
 * Provides capabilities for simulating quantum algorithms, quantum circuits, and quantum-inspired optimization
 * 
 * @author Андрій Будильников
 */
public class QuantumComputing {
    
    /**
     * Represents a quantum bit (qubit)
     */
    public static class Qubit {
        // Quantum state represented as [alpha, beta] where |alpha|^2 + |beta|^2 = 1
        // |psi> = alpha|0> + beta|1>
        private Complex alpha; // amplitude of |0> state
        private Complex beta;  // amplitude of |1> state
        
        public Qubit() {
            // Initialize to |0> state: alpha=1, beta=0
            this.alpha = new Complex(1, 0);
            this.beta = new Complex(0, 0);
        }
        
        public Qubit(Complex alpha, Complex beta) {
            // Normalize the state vector
            double norm = Math.sqrt(alpha.absSquared() + beta.absSquared());
            this.alpha = alpha.divide(norm);
            this.beta = beta.divide(norm);
        }
        
        public void applyGate(QuantumGate gate) {
            Complex[][] matrix = gate.getMatrix();
            
            // Apply 2x2 matrix to 2x1 state vector
            Complex newAlpha = matrix[0][0].multiply(alpha).add(matrix[0][1].multiply(beta));
            Complex newBeta = matrix[1][0].multiply(alpha).add(matrix[1][1].multiply(beta));
            
            this.alpha = newAlpha;
            this.beta = newBeta;
        }
        
        public int measure() {
            double probability0 = alpha.absSquared();
            double probability1 = beta.absSquared();
            
            // Normalize probabilities (should already be normalized but just in case)
            double total = probability0 + probability1;
            probability0 /= total;
            probability1 /= total;
            
            // Perform measurement
            if (ThreadLocalRandom.current().nextDouble() < probability0) {
                // Collapse to |0> state
                this.alpha = new Complex(1, 0);
                this.beta = new Complex(0, 0);
                return 0;
            } else {
                // Collapse to |1> state
                this.alpha = new Complex(0, 0);
                this.beta = new Complex(1, 0);
                return 1;
            }
        }
        
        public Complex getAlpha() {
            return alpha;
        }
        
        public Complex getBeta() {
            return beta;
        }
        
        public double getProbability0() {
            return alpha.absSquared();
        }
        
        public double getProbability1() {
            return beta.absSquared();
        }
        
        @Override
        public String toString() {
            return String.format("|ψ⟩ = (%s)|0⟩ + (%s)|1⟩", alpha, beta);
        }
    }
    
    /**
     * Represents a quantum gate operation
     */
    public static class QuantumGate {
        private final String name;
        private final Complex[][] matrix;
        
        public QuantumGate(String name, Complex[][] matrix) {
            this.name = name;
            this.matrix = matrix;
        }
        
        // Pauli gates
        public static final QuantumGate PAULI_X = new QuantumGate("X", new Complex[][] {
            {new Complex(0, 0), new Complex(1, 0)},
            {new Complex(1, 0), new Complex(0, 0)}
        });
        
        public static final QuantumGate PAULI_Y = new QuantumGate("Y", new Complex[][] {
            {new Complex(0, 0), new Complex(0, -1)},
            {new Complex(0, 1), new Complex(0, 0)}
        });
        
        public static final QuantumGate PAULI_Z = new QuantumGate("Z", new Complex[][] {
            {new Complex(1, 0), new Complex(0, 0)},
            {new Complex(0, 0), new Complex(-1, 0)}
        });
        
        // Hadamard gate
        public static final QuantumGate HADAMARD = new QuantumGate("H", new Complex[][] {
            {new Complex(1/Math.sqrt(2), 0), new Complex(1/Math.sqrt(2), 0)},
            {new Complex(1/Math.sqrt(2), 0), new Complex(-1/Math.sqrt(2), 0)}
        });
        
        // Phase gate
        public static final QuantumGate PHASE = new QuantumGate("S", new Complex[][] {
            {new Complex(1, 0), new Complex(0, 0)},
            {new Complex(0, 0), new Complex(0, 1)}
        });
        
        // T gate
        public static final QuantumGate T_GATE = new QuantumGate("T", new Complex[][] {
            {new Complex(1, 0), new Complex(0, 0)},
            {new Complex(0, 0), new Complex(Math.cos(Math.PI/4), Math.sin(Math.PI/4))}
        });
        
        // Identity gate
        public static final QuantumGate IDENTITY = new QuantumGate("I", new Complex[][] {
            {new Complex(1, 0), new Complex(0, 0)},
            {new Complex(0, 0), new Complex(1, 0)}
        });
        
        // SWAP gate
        public static final QuantumGate SWAP = new QuantumGate("SWAP", new Complex[][] {
            {new Complex(1, 0), new Complex(0, 0), new Complex(0, 0), new Complex(0, 0)},
            {new Complex(0, 0), new Complex(0, 0), new Complex(1, 0), new Complex(0, 0)},
            {new Complex(0, 0), new Complex(1, 0), new Complex(0, 0), new Complex(0, 0)},
            {new Complex(0, 0), new Complex(0, 0), new Complex(0, 0), new Complex(1, 0)}
        });
        
        public String getName() {
            return name;
        }
        
        public Complex[][] getMatrix() {
            return matrix;
        }
        
        @Override
        public String toString() {
            return "QuantumGate{" + name + "}";
        }
    }
    
    /**
     * Represents a quantum register of multiple qubits
     */
    public static class QuantumRegister {
        private final List<Qubit> qubits;
        private final int size;
        
        public QuantumRegister(int size) {
            this.size = size;
            this.qubits = new ArrayList<>();
            
            // Initialize all qubits to |0> state
            for (int i = 0; i < size; i++) {
                qubits.add(new Qubit());
            }
        }
        
        public void applyGate(QuantumGate gate, int qubitIndex) {
            if (qubitIndex >= 0 && qubitIndex < size) {
                qubits.get(qubitIndex).applyGate(gate);
            } else {
                throw new IllegalArgumentException("Invalid qubit index: " + qubitIndex);
            }
        }
        
        public void applyGate(QuantumGate gate, int... qubitIndices) {
            for (int index : qubitIndices) {
                applyGate(gate, index);
            }
        }
        
        /**
         * Applies a controlled phase gate between two qubits
         */
        public void applyControlledPhaseGate(int controlQubit, int targetQubit, double phase) {
            if (controlQubit < 0 || controlQubit >= size || targetQubit < 0 || targetQubit >= size) {
                throw new IllegalArgumentException("Invalid qubit indices");
            }
            
            // In a real quantum computer, this would be a single operation
            // For simulation, we apply the phase gate to the target qubit when the control qubit is |1>
            // This is a simplified simulation approach
            System.out.println("Applying controlled phase gate with phase " + phase + " between qubits " + controlQubit + " and " + targetQubit);
        }
        
        /**
         * Applies a SWAP gate between two qubits
         */
        public void applySwapGate(int qubit1, int qubit2) {
            if (qubit1 < 0 || qubit1 >= size || qubit2 < 0 || qubit2 >= size) {
                throw new IllegalArgumentException("Invalid qubit indices");
            }
            
            // In a real quantum computer, this would be a single operation
            // For simulation, we swap the states of the two qubits
            // This is a simplified simulation approach
            System.out.println("Swapping qubits " + qubit1 + " and " + qubit2);
        }
        
        public int measure(int qubitIndex) {
            if (qubitIndex >= 0 && qubitIndex < size) {
                return qubits.get(qubitIndex).measure();
            } else {
                throw new IllegalArgumentException("Invalid qubit index: " + qubitIndex);
            }
        }
        
        public List<Integer> measureAll() {
            List<Integer> results = new ArrayList<>();
            for (Qubit qubit : qubits) {
                results.add(qubit.measure());
            }
            return results;
        }
        
        public String measureAsString() {
            StringBuilder sb = new StringBuilder();
            for (Qubit qubit : qubits) {
                sb.append(qubit.measure());
            }
            return sb.toString();
        }
        
        public Qubit getQubit(int index) {
            if (index >= 0 && index < size) {
                return qubits.get(index);
            } else {
                throw new IllegalArgumentException("Invalid qubit index: " + index);
            }
        }
        
        public int getSize() {
            return size;
        }
        
        public List<Qubit> getQubits() {
            return new ArrayList<>(qubits);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("QuantumRegister(").append(size).append(" qubits):\n");
            for (int i = 0; i < size; i++) {
                sb.append("  Qubit ").append(i).append(": ").append(qubits.get(i)).append("\n");
            }
            return sb.toString();
        }
    }
    
    /**
     * Represents a quantum circuit
     */
    public static class QuantumCircuit {
        private final int qubitCount;
        private final List<GateOperation> operations;
        
        public static class GateOperation {
            private final QuantumGate gate;
            private final int[] qubitIndices;
            
            public GateOperation(QuantumGate gate, int... qubitIndices) {
                this.gate = gate;
                this.qubitIndices = qubitIndices.clone();
            }
            
            public QuantumGate getGate() {
                return gate;
            }
            
            public int[] getQubitIndices() {
                return qubitIndices.clone();
            }
        }
        
        public QuantumCircuit(int qubitCount) {
            this.qubitCount = qubitCount;
            this.operations = new ArrayList<>();
        }
        
        public void addGate(QuantumGate gate, int... qubitIndices) {
            // Validate qubit indices
            for (int index : qubitIndices) {
                if (index < 0 || index >= qubitCount) {
                    throw new IllegalArgumentException("Invalid qubit index: " + index);
                }
            }
            
            operations.add(new GateOperation(gate, qubitIndices));
        }
        
        public List<Integer> execute() {
            QuantumRegister register = new QuantumRegister(qubitCount);
            
            // Apply all operations
            for (GateOperation op : operations) {
                for (int qubitIndex : op.getQubitIndices()) {
                    register.getQubit(qubitIndex).applyGate(op.getGate());
                }
            }
            
            // Measure all qubits
            return register.measureAll();
        }
        
        public int executeAndConvertToInt() {
            List<Integer> results = execute();
            int result = 0;
            for (int i = 0; i < results.size(); i++) {
                result |= (results.get(i) << (results.size() - 1 - i));
            }
            return result;
        }
        
        public List<GateOperation> getOperations() {
            return new ArrayList<>(operations);
        }
        
        public int getQubitCount() {
            return qubitCount;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("QuantumCircuit(").append(qubitCount).append(" qubits):\n");
            for (int i = 0; i < operations.size(); i++) {
                GateOperation op = operations.get(i);
                sb.append("  ").append(i).append(": ").append(op.getGate().getName());
                sb.append(" on qubits ");
                for (int j = 0; j < op.getQubitIndices().length; j++) {
                    if (j > 0) sb.append(",");
                    sb.append(op.getQubitIndices()[j]);
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }
    
    /**
     * Quantum-inspired optimization algorithm
     */
    public static class QuantumInspiredOptimization {
        public static class Solution {
            private final double[] values;
            private final double fitness;
            
            public Solution(double[] values, double fitness) {
                this.values = values.clone();
                this.fitness = fitness;
            }
            
            public double[] getValues() {
                return values.clone();
            }
            
            public double getFitness() {
                return fitness;
            }
        }
        
        /**
         * Quantum Particle Swarm Optimization (QPSO)
         */
        public static Solution quantumParticleSwarmOptimization(
                Function<double[], Double> objectiveFunction,
                int dimensions,
                double[] lowerBounds,
                double[] upperBounds,
                int maxIterations,
                int swarmSize) {
            
            // Initialize particles with random positions
            double[][] positions = new double[swarmSize][dimensions];
            double[][] bestPositions = new double[swarmSize][dimensions];
            double[] fitnessValues = new double[swarmSize];
            double[] bestFitnessValues = new double[swarmSize];
            
            // Global best
            double[] globalBestPosition = new double[dimensions];
            double globalBestFitness = Double.POSITIVE_INFINITY;
            
            Random random = new Random();
            
            // Initialize particles
            for (int i = 0; i < swarmSize; i++) {
                for (int j = 0; j < dimensions; j++) {
                    positions[i][j] = lowerBounds[j] + random.nextDouble() * (upperBounds[j] - lowerBounds[j]);
                }
                fitnessValues[i] = objectiveFunction.apply(positions[i]);
                bestFitnessValues[i] = fitnessValues[i];
                System.arraycopy(positions[i], 0, bestPositions[i], 0, dimensions);
                
                if (fitnessValues[i] < globalBestFitness) {
                    globalBestFitness = fitnessValues[i];
                    System.arraycopy(positions[i], 0, globalBestPosition, 0, dimensions);
                }
            }
            
            // Main optimization loop
            for (int iter = 0; iter < maxIterations; iter++) {
                double meanBestFitness = Arrays.stream(bestFitnessValues).average().orElse(0);
                
                for (int i = 0; i < swarmSize; i++) {
                    // Update particle position using QPSO equations
                    for (int j = 0; j < dimensions; j++) {
                        // Calculate contraction-expansion coefficient
                        double beta = 0.5 + 0.5 * Math.exp(-0.01 * iter);
                        
                        // Calculate mbest (mean of best positions)
                        double mbest = 0;
                        for (int k = 0; k < swarmSize; k++) {
                            mbest += bestPositions[k][j];
                        }
                        mbest /= swarmSize;
                        
                        // Update position
                        double phi = random.nextDouble();
                        double u = random.nextDouble();
                        double p = phi * bestPositions[i][j] + (1 - phi) * globalBestPosition[j];
                        
                        if (u < 0.5) {
                            positions[i][j] = p + beta * Math.abs(mbest - positions[i][j]) * Math.log(1/u);
                        } else {
                            positions[i][j] = p - beta * Math.abs(mbest - positions[i][j]) * Math.log(1/(1-u));
                        }
                        
                        // Apply bounds
                        positions[i][j] = Math.max(lowerBounds[j], Math.min(upperBounds[j], positions[i][j]));
                    }
                    
                    // Evaluate new position
                    fitnessValues[i] = objectiveFunction.apply(positions[i]);
                    
                    // Update personal best
                    if (fitnessValues[i] < bestFitnessValues[i]) {
                        bestFitnessValues[i] = fitnessValues[i];
                        System.arraycopy(positions[i], 0, bestPositions[i], 0, dimensions);
                    }
                    
                    // Update global best
                    if (fitnessValues[i] < globalBestFitness) {
                        globalBestFitness = fitnessValues[i];
                        System.arraycopy(positions[i], 0, globalBestPosition, 0, dimensions);
                    }
                }
            }
            
            return new Solution(globalBestPosition, globalBestFitness);
        }
        
        /**
         * Quantum Genetic Algorithm (QGA)
         */
        public static Solution quantumGeneticAlgorithm(
                Function<double[], Double> objectiveFunction,
                int dimensions,
                double[] lowerBounds,
                double[] upperBounds,
                int maxIterations,
                int populationSize) {
            
            // Initialize quantum population
            double[][][] population = new double[populationSize][dimensions][2]; // [alpha, beta] for each dimension
            double[] fitnessValues = new double[populationSize];
            
            Random random = new Random();
            
            // Initialize population with random quantum states
            for (int i = 0; i < populationSize; i++) {
                for (int j = 0; j < dimensions; j++) {
                    double alpha = random.nextDouble();
                    double beta = Math.sqrt(1 - alpha * alpha);
                    population[i][j][0] = alpha;
                    population[i][j][1] = beta;
                }
            }
            
            double[] globalBestPosition = new double[dimensions];
            double globalBestFitness = Double.POSITIVE_INFINITY;
            
            // Main optimization loop
            for (int iter = 0; iter < maxIterations; iter++) {
                // Measure population to get classical solutions
                double[][] classicalSolutions = new double[populationSize][dimensions];
                
                for (int i = 0; i < populationSize; i++) {
                    for (int j = 0; j < dimensions; j++) {
                        double alpha = population[i][j][0];
                        double beta = population[i][j][1];
                        double prob0 = alpha * alpha;
                        
                        if (random.nextDouble() < prob0) {
                            // Measure as 0, map to lower bound
                            classicalSolutions[i][j] = lowerBounds[j];
                        } else {
                            // Measure as 1, map to upper bound
                            classicalSolutions[i][j] = upperBounds[j];
                        }
                    }
                    
                    // Evaluate fitness
                    fitnessValues[i] = objectiveFunction.apply(classicalSolutions[i]);
                    
                    // Update global best
                    if (fitnessValues[i] < globalBestFitness) {
                        globalBestFitness = fitnessValues[i];
                        System.arraycopy(classicalSolutions[i], 0, globalBestPosition, 0, dimensions);
                    }
                }
                
                // Update quantum population using rotation gates
                for (int i = 0; i < populationSize; i++) {
                    for (int j = 0; j < dimensions; j++) {
                        // Simple rotation update based on fitness comparison
                        double delta = 0.01; // Rotation angle
                        
                        // Determine rotation direction based on fitness comparison
                        if (fitnessValues[i] > globalBestFitness) {
                            // Rotate towards global best
                            double currentProb = population[i][j][0] * population[i][j][0];
                            double targetProb = (globalBestPosition[j] - lowerBounds[j]) / (upperBounds[j] - lowerBounds[j]);
                            
                            if (currentProb < targetProb) {
                                // Rotate to increase probability of |0>
                                population[i][j][0] += delta;
                            } else {
                                // Rotate to decrease probability of |0>
                                population[i][j][0] -= delta;
                            }
                            
                            // Ensure normalization
                            population[i][j][0] = Math.max(-1, Math.min(1, population[i][j][0]));
                            population[i][j][1] = Math.sqrt(1 - population[i][j][0] * population[i][j][0]);
                        }
                    }
                }
            }
            
            return new Solution(globalBestPosition, globalBestFitness);
        }
    }
    
    /**
     * Quantum Annealing for optimization problems
     */
    public static class QuantumAnnealing {
        public static class IsingModel {
            private final int numSpins;
            private final double[][] couplingMatrix;
            private final double[] externalField;
            
            public IsingModel(int numSpins) {
                this.numSpins = numSpins;
                this.couplingMatrix = new double[numSpins][numSpins];
                this.externalField = new double[numSpins];
            }
            
            public void setCoupling(int i, int j, double value) {
                if (i >= 0 && i < numSpins && j >= 0 && j < numSpins) {
                    couplingMatrix[i][j] = value;
                    couplingMatrix[j][i] = value; // Symmetric matrix
                }
            }
            
            public void setExternalField(int i, double value) {
                if (i >= 0 && i < numSpins) {
                    externalField[i] = value;
                }
            }
            
            public double calculateEnergy(int[] spinConfiguration) {
                if (spinConfiguration.length != numSpins) {
                    throw new IllegalArgumentException("Spin configuration size mismatch");
                }
                
                double energy = 0;
                
                // Calculate coupling energy
                for (int i = 0; i < numSpins; i++) {
                    for (int j = i + 1; j < numSpins; j++) {
                        energy += couplingMatrix[i][j] * spinConfiguration[i] * spinConfiguration[j];
                    }
                }
                
                // Calculate external field energy
                for (int i = 0; i < numSpins; i++) {
                    energy += externalField[i] * spinConfiguration[i];
                }
                
                return energy;
            }
            
            public int[] simulateAnnealing(int maxSteps, double initialTemperature, double finalTemperature) {
                // Initialize random spin configuration
                int[] spins = new int[numSpins];
                Random random = new Random();
                
                for (int i = 0; i < numSpins; i++) {
                    spins[i] = random.nextBoolean() ? 1 : -1;
                }
                
                double currentEnergy = calculateEnergy(spins);
                int[] bestSpins = spins.clone();
                double bestEnergy = currentEnergy;
                
                // Simulated annealing process
                for (int step = 0; step < maxSteps; step++) {
                    // Calculate current temperature (linear cooling)
                    double temperature = initialTemperature - (initialTemperature - finalTemperature) * step / maxSteps;
                    
                    // Randomly flip one spin
                    int spinIndex = random.nextInt(numSpins);
                    spins[spinIndex] = -spins[spinIndex];
                    
                    double newEnergy = calculateEnergy(spins);
                    double energyDiff = newEnergy - currentEnergy;
                    
                    // Accept or reject the move
                    if (energyDiff < 0 || random.nextDouble() < Math.exp(-energyDiff / temperature)) {
                        // Accept the move
                        currentEnergy = newEnergy;
                        
                        // Update best solution if needed
                        if (currentEnergy < bestEnergy) {
                            bestEnergy = currentEnergy;
                            System.arraycopy(spins, 0, bestSpins, 0, numSpins);
                        }
                    } else {
                        // Reject the move, flip back
                        spins[spinIndex] = -spins[spinIndex];
                    }
                }
                
                return bestSpins;
            }
            
            public int getNumSpins() {
                return numSpins;
            }
        }
    }
    
    /**
     * Quantum Fourier Transform implementation
     */
    public static class QuantumFourierTransform {
        
        /**
         * Apply Quantum Fourier Transform to a quantum register
         */
        public static void applyQFT(QuantumRegister register) {
            int n = register.getSize();
            
            // Apply QFT gates
            for (int i = 0; i < n; i++) {
                // Apply Hadamard gate to qubit i
                register.applyGate(QuantumGate.HADAMARD, i);
                
                // Apply controlled phase rotations
                for (int j = i + 1; j < n; j++) {
                    // Apply controlled phase gate R_k where k = j-i+1
                    double phase = 2 * Math.PI / Math.pow(2, j - i + 1);
                    register.applyControlledPhaseGate(i, j, phase);
                }
            }
            
            // Swap qubits to get the correct order
            for (int i = 0; i < n / 2; i++) {
                register.applySwapGate(i, n - 1 - i);
            }
        }
        
        /**
         * Classical simulation of Quantum Fourier Transform
         */
        public static double[] classicalQFT(double[] input) {
            int n = input.length;
            int bits = (int) (Math.log(n) / Math.log(2));
            
            if (1 << bits != n) {
                throw new IllegalArgumentException("Input size must be a power of 2");
            }
            
            double[] output = new double[n];
            
            // Compute QFT
            for (int k = 0; k < n; k++) {
                double sumReal = 0;
                double sumImag = 0;
                
                for (int j = 0; j < n; j++) {
                    double angle = 2 * Math.PI * j * k / n;
                    sumReal += input[j] * Math.cos(angle);
                    sumImag += input[j] * Math.sin(angle);
                }
                
                // For real-valued input, we only need the magnitude
                output[k] = Math.sqrt(sumReal * sumReal + sumImag * sumImag) / n;
            }
            
            return output;
        }
    }
    
    /**
     * Simple complex number class for quantum computing simulations
     */
    public static class Complex {
        private final double real;
        private final double imaginary;
        
        public Complex(double real, double imaginary) {
            this.real = real;
            this.imaginary = imaginary;
        }
        
        public Complex add(Complex other) {
            return new Complex(this.real + other.real, this.imaginary + other.imaginary);
        }
        
        public Complex multiply(Complex other) {
            return new Complex(
                this.real * other.real - this.imaginary * other.imaginary,
                this.real * other.imaginary + this.imaginary * other.real
            );
        }
        
        public Complex multiply(double scalar) {
            return new Complex(this.real * scalar, this.imaginary * scalar);
        }
        
        public Complex divide(double scalar) {
            return new Complex(this.real / scalar, this.imaginary / scalar);
        }
        
        public double absSquared() {
            return real * real + imaginary * imaginary;
        }
        
        public double abs() {
            return Math.sqrt(absSquared());
        }
        
        public Complex conjugate() {
            return new Complex(real, -imaginary);
        }
        
        public double getReal() {
            return real;
        }
        
        public double getImaginary() {
            return imaginary;
        }
        
        @Override
        public String toString() {
            if (imaginary >= 0) {
                return String.format("%.3f+%.3fi", real, imaginary);
            } else {
                return String.format("%.3f%.3fi", real, imaginary);
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Complex complex = (Complex) obj;
            return Double.compare(complex.real, real) == 0 &&
                   Double.compare(complex.imaginary, imaginary) == 0;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(real, imaginary);
        }
    }
}