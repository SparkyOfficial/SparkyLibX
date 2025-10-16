package com.sparky.libx.complex;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.math.NoiseGenerator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Functional interface for cellular automaton update rules
 */
@FunctionalInterface
interface CellularAutomatonUpdateRule {
    boolean apply(int[][] current, int[][] next);
}

/**
 * Complex Systems Modeling for Minecraft Plugins
 * Provides capabilities for modeling complex adaptive systems, emergent behaviors, and network dynamics
 * 
 * @author Андрій Будильников
 */
public class ComplexSystems {
    
    /**
     * Represents an agent in a complex system
     */
    public static class Agent {
        private final UUID id;
        private Vector3D position;
        private Vector3D velocity;
        private double energy;
        private double health;
        private Map<String, Object> attributes;
        private List<Agent> neighbors;
        private boolean alive;
        
        public Agent(Vector3D position) {
            this.id = UUID.randomUUID();
            this.position = position;
            this.velocity = new Vector3D(0, 0, 0);
            this.energy = 100.0;
            this.health = 100.0;
            this.attributes = new ConcurrentHashMap<>();
            this.neighbors = new ArrayList<>();
            this.alive = true;
        }
        
        public void update(double deltaTime) {
            if (alive) {
                // Update position based on velocity
                position = position.add(velocity.multiply(deltaTime));
                
                // Consume energy over time
                energy -= deltaTime * 0.1;
                
                // Die if energy or health is depleted
                if (energy <= 0 || health <= 0) {
                    alive = false;
                }
            }
        }
        
        public void interact(Agent other, double deltaTime) {
            if (alive && other.isAlive()) {
                // Simple interaction - agents repel each other
                Vector3D direction = position.subtract(other.position);
                double distance = direction.magnitude();
                
                if (distance < 5.0 && distance > 0) {
                    Vector3D force = direction.normalize().multiply(10.0 / (distance * distance));
                    velocity = velocity.add(force.multiply(deltaTime));
                }
            }
        }
        
        public void addAttribute(String key, Object value) {
            attributes.put(key, value);
        }
        
        public <T> T getAttribute(String key, Class<T> type) {
            Object value = attributes.get(key);
            if (type.isInstance(value)) {
                return type.cast(value);
            }
            return null;
        }
        
        public void setNeighbors(List<Agent> neighbors) {
            this.neighbors = neighbors;
        }
        
        public List<Agent> getNeighbors() {
            return new ArrayList<>(neighbors);
        }
        
        public UUID getId() {
            return id;
        }
        
        public Vector3D getPosition() {
            return position;
        }
        
        public void setPosition(Vector3D position) {
            this.position = position;
        }
        
        public Vector3D getVelocity() {
            return velocity;
        }
        
        public void setVelocity(Vector3D velocity) {
            this.velocity = velocity;
        }
        
        public double getEnergy() {
            return energy;
        }
        
        public void setEnergy(double energy) {
            this.energy = Math.max(0, energy);
        }
        
        public double getHealth() {
            return health;
        }
        
        public void setHealth(double health) {
            this.health = Math.max(0, Math.min(100, health));
        }
        
        public boolean isAlive() {
            return alive;
        }
        
        public void setAlive(boolean alive) {
            this.alive = alive;
        }
    }
    
    /**
     * Represents a complex adaptive system with interacting agents
     */
    public static class AdaptiveSystem {
        private List<Agent> agents;
        private Function<Agent, Vector3D> environmentForce;
        private double time;
        private NoiseGenerator noiseGenerator;
        
        public AdaptiveSystem() {
            this.agents = new CopyOnWriteArrayList<>();
            this.environmentForce = agent -> new Vector3D(0, 0, 0);
            this.time = 0;
            this.noiseGenerator = new NoiseGenerator(System.currentTimeMillis());
        }
        
        public void addAgent(Agent agent) {
            agents.add(agent);
        }
        
        public void removeAgent(Agent agent) {
            agents.remove(agent);
        }
        
        public List<Agent> getAgents() {
            return new ArrayList<>(agents);
        }
        
        public void setEnvironmentForce(Function<Agent, Vector3D> forceFunction) {
            this.environmentForce = forceFunction;
        }
        
        public void update(double deltaTime) {
            time += deltaTime;
            
            // Update all agents
            for (Agent agent : agents) {
                // Apply environment forces
                Vector3D envForce = environmentForce.apply(agent);
                agent.setVelocity(agent.getVelocity().add(envForce.multiply(deltaTime)));
                
                // Add some random noise to movement
                double noiseX = noiseGenerator.perlinNoise(time * 0.1, agent.getId().hashCode()) * 0.1;
                double noiseY = noiseGenerator.perlinNoise(time * 0.1, agent.getId().hashCode() + 1) * 0.1;
                double noiseZ = noiseGenerator.perlinNoise(time * 0.1, agent.getId().hashCode() + 2) * 0.1;
                agent.setVelocity(agent.getVelocity().add(new Vector3D(noiseX, noiseY, noiseZ)));
                
                agent.update(deltaTime);
            }
            
            // Handle agent interactions
            for (int i = 0; i < agents.size(); i++) {
                Agent agent = agents.get(i);
                if (agent.isAlive()) {
                    // Find neighbors
                    List<Agent> neighbors = findNeighbors(agent, 10.0);
                    agent.setNeighbors(neighbors);
                    
                    // Interact with neighbors
                    for (Agent neighbor : neighbors) {
                        if (neighbor != agent) {
                            agent.interact(neighbor, deltaTime);
                        }
                    }
                }
            }
            
            // Remove dead agents
            agents.removeIf(agent -> !agent.isAlive());
        }
        
        private List<Agent> findNeighbors(Agent agent, double radius) {
            List<Agent> neighbors = new ArrayList<>();
            for (Agent other : agents) {
                if (other != agent && agent.getPosition().distance(other.getPosition()) <= radius) {
                    neighbors.add(other);
                }
            }
            return neighbors;
        }
        
        public double getTime() {
            return time;
        }
    }
    
    /**
     * Represents a cellular automaton for pattern formation
     */
    public static class CellularAutomaton {
        private int width;
        private int height;
        private int[][] currentState;
        private int[][] nextState;
        private CellularAutomatonUpdateRule updateRule;
        
        public CellularAutomaton(int width, int height) {
            this.width = width;
            this.height = height;
            this.currentState = new int[width][height];
            this.nextState = new int[width][height];
            this.updateRule = this::defaultUpdateRule;
            
            // Initialize with random state
            Random random = new Random();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    currentState[x][y] = random.nextInt(2);
                }
            }
        }
        
        public void setUpdateRule(CellularAutomatonUpdateRule rule) {
            this.updateRule = rule;
        }
        
        public void update() {
            // Apply update rule to compute next state
            boolean changed = updateRule.apply(currentState, nextState);
            
            // Swap states if changed
            if (changed) {
                int[][] temp = currentState;
                currentState = nextState;
                nextState = temp;
            }
        }
        
        private boolean defaultUpdateRule(int[][] current, int[][] next) {
            boolean changed = false;
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int neighbors = countLivingNeighbors(current, x, y);
                    int currentState = current[x][y];
                    int nextState = currentState;
                    
                    // Conway's Game of Life rules
                    if (currentState == 1) {
                        // Living cell
                        if (neighbors < 2 || neighbors > 3) {
                            nextState = 0; // Dies
                        }
                    } else {
                        // Dead cell
                        if (neighbors == 3) {
                            nextState = 1; // Becomes alive
                        }
                    }
                    
                    next[x][y] = nextState;
                    if (nextState != currentState) {
                        changed = true;
                    }
                }
            }
            
            return changed;
        }
        
        private int countLivingNeighbors(int[][] grid, int x, int y) {
            int count = 0;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    
                    int nx = (x + dx + width) % width; // Toroidal boundary conditions
                    int ny = (y + dy + height) % height;
                    
                    count += grid[nx][ny];
                }
            }
            return count;
        }
        
        public int getState(int x, int y) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                return currentState[x][y];
            }
            return 0;
        }
        
        public void setState(int x, int y, int state) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                currentState[x][y] = state;
            }
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public int[][] getCurrentState() {
            return currentState;
        }
    }
    
    /**
     * Represents a network of connected nodes for complex network analysis
     */
    public static class ComplexNetwork {
        public static class Node {
            private final UUID id;
            private Map<String, Object> attributes;
            private List<Edge> edges;
            private Vector3D position;
            
            public Node() {
                this.id = UUID.randomUUID();
                this.attributes = new ConcurrentHashMap<>();
                this.edges = new CopyOnWriteArrayList<>();
                this.position = new Vector3D(0, 0, 0);
            }
            
            public Node(Vector3D position) {
                this();
                this.position = position;
            }
            
            public void addAttribute(String key, Object value) {
                attributes.put(key, value);
            }
            
            public <T> T getAttribute(String key, Class<T> type) {
                Object value = attributes.get(key);
                if (type.isInstance(value)) {
                    return type.cast(value);
                }
                return null;
            }
            
            public void addEdge(Edge edge) {
                if (!edges.contains(edge)) {
                    edges.add(edge);
                }
            }
            
            public void removeEdge(Edge edge) {
                edges.remove(edge);
            }
            
            public List<Edge> getEdges() {
                return new ArrayList<>(edges);
            }
            
            public int getDegree() {
                return edges.size();
            }
            
            public UUID getId() {
                return id;
            }
            
            public Vector3D getPosition() {
                return position;
            }
            
            public void setPosition(Vector3D position) {
                this.position = position;
            }
        }
        
        public static class Edge {
            private final UUID id;
            private Node source;
            private Node target;
            private double weight;
            private boolean directed;
            private Map<String, Object> attributes;
            
            public Edge(Node source, Node target, double weight, boolean directed) {
                this.id = UUID.randomUUID();
                this.source = source;
                this.target = target;
                this.weight = weight;
                this.directed = directed;
                this.attributes = new ConcurrentHashMap<>();
                
                // Add this edge to the nodes
                source.addEdge(this);
                if (!directed) {
                    target.addEdge(this);
                }
            }
            
            public UUID getId() {
                return id;
            }
            
            public Node getSource() {
                return source;
            }
            
            public Node getTarget() {
                return target;
            }
            
            public double getWeight() {
                return weight;
            }
            
            public void setWeight(double weight) {
                this.weight = weight;
            }
            
            public boolean isDirected() {
                return directed;
            }
            
            public void addAttribute(String key, Object value) {
                attributes.put(key, value);
            }
            
            public <T> T getAttribute(String key, Class<T> type) {
                Object value = attributes.get(key);
                if (type.isInstance(value)) {
                    return type.cast(value);
                }
                return null;
            }
            
            public boolean isBetween(Node node1, Node node2) {
                if (directed) {
                    return source == node1 && target == node2;
                } else {
                    return (source == node1 && target == node2) || (source == node2 && target == node1);
                }
            }
        }
        
        private List<Node> nodes;
        private List<Edge> edges;
        private boolean directed;
        
        public ComplexNetwork(boolean directed) {
            this.nodes = new CopyOnWriteArrayList<>();
            this.edges = new CopyOnWriteArrayList<>();
            this.directed = directed;
        }
        
        public Node createNode() {
            Node node = new Node();
            nodes.add(node);
            return node;
        }
        
        public Node createNode(Vector3D position) {
            Node node = new Node(position);
            nodes.add(node);
            return node;
        }
        
        public Edge createEdge(Node source, Node target, double weight) {
            Edge edge = new Edge(source, target, weight, directed);
            edges.add(edge);
            return edge;
        }
        
        public Edge createEdge(Node source, Node target) {
            return createEdge(source, target, 1.0);
        }
        
        public void removeNode(Node node) {
            // Remove all edges connected to this node
            List<Edge> edgesToRemove = new ArrayList<>();
            for (Edge edge : edges) {
                if (edge.getSource() == node || edge.getTarget() == node) {
                    edgesToRemove.add(edge);
                }
            }
            
            for (Edge edge : edgesToRemove) {
                removeEdge(edge);
            }
            
            nodes.remove(node);
        }
        
        public void removeEdge(Edge edge) {
            edge.getSource().removeEdge(edge);
            if (!edge.isDirected()) {
                edge.getTarget().removeEdge(edge);
            }
            edges.remove(edge);
        }
        
        public List<Node> getNodes() {
            return new ArrayList<>(nodes);
        }
        
        public List<Edge> getEdges() {
            return new ArrayList<>(edges);
        }
        
        public int getNodeCount() {
            return nodes.size();
        }
        
        public int getEdgeCount() {
            return edges.size();
        }
        
        public boolean isDirected() {
            return directed;
        }
        
        /**
         * Calculate the shortest path between two nodes using Dijkstra's algorithm
         */
        public List<Node> shortestPath(Node start, Node end) {
            if (start == end) {
                return Arrays.asList(start);
            }
            
            Map<Node, Double> distances = new HashMap<>();
            Map<Node, Node> previous = new HashMap<>();
            Set<Node> unvisited = new HashSet<>();
            
            // Initialize
            for (Node node : nodes) {
                distances.put(node, Double.POSITIVE_INFINITY);
                previous.put(node, null);
                unvisited.add(node);
            }
            distances.put(start, 0.0);
            
            while (!unvisited.isEmpty()) {
                // Find node with minimum distance
                Node current = unvisited.stream()
                    .min(Comparator.comparingDouble(distances::get))
                    .orElse(null);
                
                if (current == null || distances.get(current) == Double.POSITIVE_INFINITY) {
                    break;
                }
                
                unvisited.remove(current);
                
                if (current == end) {
                    break;
                }
                
                // Update distances to neighbors
                for (Edge edge : current.getEdges()) {
                    Node neighbor = edge.getSource() == current ? edge.getTarget() : edge.getSource();
                    
                    if (unvisited.contains(neighbor)) {
                        double alt = distances.get(current) + edge.getWeight();
                        if (alt < distances.get(neighbor)) {
                            distances.put(neighbor, alt);
                            previous.put(neighbor, current);
                        }
                    }
                }
            }
            
            // Reconstruct path
            List<Node> path = new ArrayList<>();
            Node current = end;
            
            while (current != null) {
                path.add(0, current);
                current = previous.get(current);
            }
            
            // Check if path exists
            if (path.get(0) != start) {
                return new ArrayList<>(); // No path found
            }
            
            return path;
        }
        
        /**
         * Calculate the clustering coefficient of a node
         */
        public double clusteringCoefficient(Node node) {
            List<Node> neighbors = new ArrayList<>();
            for (Edge edge : node.getEdges()) {
                Node neighbor = edge.getSource() == node ? edge.getTarget() : edge.getSource();
                neighbors.add(neighbor);
            }
            
            if (neighbors.size() < 2) {
                return 0.0;
            }
            
            int connections = 0;
            for (int i = 0; i < neighbors.size(); i++) {
                for (int j = i + 1; j < neighbors.size(); j++) {
                    if (areConnected(neighbors.get(i), neighbors.get(j))) {
                        connections++;
                    }
                }
            }
            
            // Maximum possible connections between neighbors
            int maxConnections = neighbors.size() * (neighbors.size() - 1) / 2;
            
            return (double) connections / maxConnections;
        }
        
        private boolean areConnected(Node node1, Node node2) {
            for (Edge edge : edges) {
                if (edge.isBetween(node1, node2)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Calculate the betweenness centrality of a node
         */
        public double betweennessCentrality(Node targetNode) {
            double centrality = 0.0;
            
            for (Node source : nodes) {
                if (source == targetNode) continue;
                
                for (Node target : nodes) {
                    if (target == targetNode || target == source) continue;
                    
                    List<Node> shortestPaths = findAllShortestPaths(source, target);
                    if (shortestPaths.isEmpty()) continue;
                    
                    int totalPaths = shortestPaths.size();
                    int pathsThroughTarget = 0;
                    
                    for (Node node : shortestPaths) {
                        if (node == targetNode) {
                            pathsThroughTarget++;
                        }
                    }
                    
                    centrality += (double) pathsThroughTarget / totalPaths;
                }
            }
            
            return centrality;
        }
        
        private List<Node> findAllShortestPaths(Node start, Node end) {
            // Simplified implementation - in practice this would be more complex
            List<Node> path = shortestPath(start, end);
            return path.isEmpty() ? new ArrayList<>() : Arrays.asList(path.toArray(new Node[0]));
        }
        
        /**
         * Calculate the degree distribution of the network
         */
        public Map<Integer, Integer> degreeDistribution() {
            Map<Integer, Integer> distribution = new HashMap<>();
            
            for (Node node : nodes) {
                int degree = node.getDegree();
                distribution.put(degree, distribution.getOrDefault(degree, 0) + 1);
            }
            
            return distribution;
        }
    }
    
    /**
     * Represents a swarm intelligence system
     */
    public static class SwarmIntelligence {
        public static class Particle {
            private Vector3D position;
            private Vector3D velocity;
            private Vector3D bestPosition;
            private double bestFitness;
            private Function<Vector3D, Double> fitnessFunction;
            
            public Particle(Vector3D position, Function<Vector3D, Double> fitnessFunction) {
                this.position = position;
                this.velocity = new Vector3D(
                    (Math.random() - 0.5) * 2,
                    (Math.random() - 0.5) * 2,
                    (Math.random() - 0.5) * 2
                );
                this.bestPosition = position;
                this.bestFitness = Double.POSITIVE_INFINITY;
                this.fitnessFunction = fitnessFunction;
            }
            
            public void update(double inertia, double cognitive, double social, Vector3D globalBest) {
                // Update velocity
                Vector3D cognitiveComponent = bestPosition.subtract(position).multiply(cognitive * Math.random());
                Vector3D socialComponent = globalBest.subtract(position).multiply(social * Math.random());
                velocity = velocity.multiply(inertia).add(cognitiveComponent).add(socialComponent);
                
                // Update position
                position = position.add(velocity);
                
                // Update best position if needed
                double fitness = fitnessFunction.apply(position);
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestPosition = position;
                }
            }
            
            public Vector3D getPosition() {
                return position;
            }
            
            public Vector3D getBestPosition() {
                return bestPosition;
            }
            
            public double getBestFitness() {
                return bestFitness;
            }
        }
        
        private List<Particle> particles;
        private Vector3D globalBestPosition;
        private double globalBestFitness;
        private Function<Vector3D, Double> fitnessFunction;
        private double inertia;
        private double cognitive;
        private double social;
        
        public SwarmIntelligence(int particleCount, Function<Vector3D, Double> fitnessFunction,
                               double inertia, double cognitive, double social) {
            this.particles = new ArrayList<>();
            this.fitnessFunction = fitnessFunction;
            this.inertia = inertia;
            this.cognitive = cognitive;
            this.social = social;
            this.globalBestFitness = Double.POSITIVE_INFINITY;
            
            // Initialize particles
            Random random = new Random();
            for (int i = 0; i < particleCount; i++) {
                Vector3D position = new Vector3D(
                    random.nextGaussian() * 10,
                    random.nextGaussian() * 10,
                    random.nextGaussian() * 10
                );
                particles.add(new Particle(position, fitnessFunction));
            }
            
            // Initialize global best
            updateGlobalBest();
        }
        
        public void update() {
            // Update all particles
            for (Particle particle : particles) {
                particle.update(inertia, cognitive, social, globalBestPosition);
            }
            
            // Update global best
            updateGlobalBest();
        }
        
        private void updateGlobalBest() {
            for (Particle particle : particles) {
                if (particle.getBestFitness() < globalBestFitness) {
                    globalBestFitness = particle.getBestFitness();
                    globalBestPosition = particle.getBestPosition();
                }
            }
        }
        
        public Vector3D getGlobalBestPosition() {
            return globalBestPosition;
        }
        
        public double getGlobalBestFitness() {
            return globalBestFitness;
        }
        
        public List<Particle> getParticles() {
            return new ArrayList<>(particles);
        }
    }
    
    /**
     * Represents a genetic algorithm system
     */
    public static class GeneticAlgorithm<T> {
        public static class Individual {
            private Object[] genes;
            private double fitness;
            
            public Individual(Object[] genes) {
                this.genes = genes.clone();
                this.fitness = 0.0;
            }
            
            public Object[] getGenes() {
                return genes.clone();
            }
            
            public Object getGene(int index) {
                return genes[index];
            }
            
            public void setGene(int index, Object gene) {
                genes[index] = gene;
            }
            
            public double getFitness() {
                return fitness;
            }
            
            public void setFitness(double fitness) {
                this.fitness = fitness;
            }
        }
        
        private List<Individual> population;
        private Function<Object[], Double> fitnessFunction;
        private Function<Individual, Object[]> crossoverFunction;
        private Function<Individual, Object[]> mutationFunction;
        private int generation;
        private Random random;
        
        public GeneticAlgorithm(int populationSize, int chromosomeLength,
                              Function<Object[], Double> fitnessFunction,
                              Function<Individual, Object[]> crossoverFunction,
                              Function<Individual, Object[]> mutationFunction) {
            this.population = new ArrayList<>();
            this.fitnessFunction = fitnessFunction;
            this.crossoverFunction = crossoverFunction;
            this.mutationFunction = mutationFunction;
            this.generation = 0;
            this.random = new Random();
            
            // Initialize population
            for (int i = 0; i < populationSize; i++) {
                Object[] genes = new Object[chromosomeLength];
                for (int j = 0; j < chromosomeLength; j++) {
                    genes[j] = random.nextDouble(); // Simple initialization
                }
                population.add(new Individual(genes));
            }
        }
        
        public void evolve(int generations) {
            for (int i = 0; i < generations; i++) {
                evaluateFitness();
                selection();
                crossover();
                mutation();
                generation++;
            }
        }
        
        private void evaluateFitness() {
            for (Individual individual : population) {
                double fitness = fitnessFunction.apply(individual.getGenes());
                individual.setFitness(fitness);
            }
        }
        
        private void selection() {
            // Tournament selection
            population.sort(Comparator.comparingDouble(Individual::getFitness).reversed());
            int keep = population.size() / 2;
            population = population.subList(0, keep);
        }
        
        private void crossover() {
            List<Individual> newIndividuals = new ArrayList<>();
            Random random = new Random();
            
            while (newIndividuals.size() + population.size() < population.size() * 2) {
                int parent1Index = random.nextInt(population.size());
                int parent2Index = random.nextInt(population.size());
                
                Individual parent1 = population.get(parent1Index);
                Individual parent2 = population.get(parent2Index);
                
                Object[] childGenes = crossoverFunction.apply(parent1);
                Individual child = new Individual(childGenes);
                newIndividuals.add(child);
            }
            
            population.addAll(newIndividuals);
        }
        
        private void mutation() {
            for (Individual individual : population) {
                Object[] mutatedGenes = mutationFunction.apply(individual);
                // Replace genes with mutated ones
                for (int i = 0; i < mutatedGenes.length; i++) {
                    individual.setGene(i, mutatedGenes[i]);
                }
            }
        }
        
        public Individual getBestIndividual() {
            return population.stream()
                .max(Comparator.comparingDouble(Individual::getFitness))
                .orElse(null);
        }
        
        public List<Individual> getPopulation() {
            return new ArrayList<>(population);
        }
        
        public int getGeneration() {
            return generation;
        }
    }
}