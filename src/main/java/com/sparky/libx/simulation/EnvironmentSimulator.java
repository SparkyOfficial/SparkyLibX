package com.sparky.libx.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sparky.libx.math.NoiseGenerator;
import com.sparky.libx.math.Vector3D;

/**
 * Advanced Environment Simulation Engine for Minecraft Plugins
 * Provides capabilities for weather, climate, ecosystem, and environmental simulations
 */
public class EnvironmentSimulator {
    
    /**
     * Represents a particle in the environment simulation
     */
    public static class EnvironmentalParticle {
        private Vector3D position;
        private Vector3D velocity;
        private Vector3D acceleration;
        private double mass;
        private double size;
        private double lifeTime;
        private double maxLifeTime;
        private ParticleType type;
        private boolean alive;
        
        public enum ParticleType {
            WATER_VAPOR, RAIN_DROP, SNOW_FLAKE, DUST, LEAF, SEED
        }
        
        public EnvironmentalParticle(Vector3D position, Vector3D velocity, double mass, double size, 
                                   double lifeTime, ParticleType type) {
            this.position = position;
            this.velocity = velocity;
            this.acceleration = new Vector3D(0, 0, 0);
            this.mass = mass;
            this.size = size;
            this.lifeTime = lifeTime;
            this.maxLifeTime = lifeTime;
            this.type = type;
            this.alive = true;
        }
        
        public void update(double deltaTime) {
            if (alive) {
                lifeTime -= deltaTime;
                if (lifeTime <= 0) {
                    alive = false;
                    return;
                }
                
                // Update physics
                velocity = velocity.add(acceleration.multiply(deltaTime));
                position = position.add(velocity.multiply(deltaTime));
                
                // Reset acceleration
                acceleration = new Vector3D(0, 0, 0);
            }
        }
        
        public void applyForce(Vector3D force) {
            Vector3D forceAcceleration = force.divide(mass);
            acceleration = acceleration.add(forceAcceleration);
        }
        
        public boolean isAlive() {
            return alive;
        }
        
        public Vector3D getPosition() {
            return position;
        }
        
        public Vector3D getVelocity() {
            return velocity;
        }
        
        public ParticleType getType() {
            return type;
        }
        
        public double getLifeTimeRatio() {
            return lifeTime / maxLifeTime;
        }
    }
    
    /**
     * Represents weather conditions
     */
    public static class WeatherConditions {
        public enum WeatherType {
            CLEAR, CLOUDY, RAINY, STORMY, SNOWY, FOGGY
        }
        
        private WeatherType type;
        private double temperature;      // Celsius
        private double humidity;         // 0.0 to 1.0
        private double windSpeed;        // meters per second
        private Vector3D windDirection;  // normalized vector
        private double precipitation;     // 0.0 to 1.0
        private double visibility;       // 0.0 to 1.0
        private double pressure;         // hPa
        
        public WeatherConditions() {
            this.type = WeatherType.CLEAR;
            this.temperature = 20.0;
            this.humidity = 0.5;
            this.windSpeed = 2.0;
            this.windDirection = new Vector3D(1, 0, 0);
            this.precipitation = 0.0;
            this.visibility = 1.0;
            this.pressure = 1013.25;
        }
        
        public WeatherType getType() {
            return type;
        }
        
        public void setType(WeatherType type) {
            this.type = type;
        }
        
        public double getTemperature() {
            return temperature;
        }
        
        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
        
        public double getHumidity() {
            return humidity;
        }
        
        public void setHumidity(double humidity) {
            this.humidity = Math.max(0.0, Math.min(1.0, humidity));
        }
        
        public double getWindSpeed() {
            return windSpeed;
        }
        
        public void setWindSpeed(double windSpeed) {
            this.windSpeed = Math.max(0.0, windSpeed);
        }
        
        public Vector3D getWindDirection() {
            return windDirection;
        }
        
        public void setWindDirection(Vector3D windDirection) {
            this.windDirection = windDirection.normalize();
        }
        
        public double getPrecipitation() {
            return precipitation;
        }
        
        public void setPrecipitation(double precipitation) {
            this.precipitation = Math.max(0.0, Math.min(1.0, precipitation));
        }
        
        public double getVisibility() {
            return visibility;
        }
        
        public void setVisibility(double visibility) {
            this.visibility = Math.max(0.0, Math.min(1.0, visibility));
        }
        
        public double getPressure() {
            return pressure;
        }
        
        public void setPressure(double pressure) {
            this.pressure = pressure;
        }
    }
    
    /**
     * Weather system for simulating atmospheric conditions
     */
    public static class WeatherSystem {
        private WeatherConditions currentConditions;
        private WeatherConditions targetConditions;
        private double transitionTime;
        private double currentTime;
        private final Random random;
        private final List<EnvironmentalParticle> particles;
        private NoiseGenerator noiseGenerator;
        
        public WeatherSystem() {
            this.currentConditions = new WeatherConditions();
            this.targetConditions = new WeatherConditions();
            this.transitionTime = 300.0; // 5 minutes
            this.currentTime = 0.0;
            this.random = new Random();
            this.particles = new CopyOnWriteArrayList<>();
            this.noiseGenerator = new NoiseGenerator(12345);
        }
        
        public void update(double deltaTime) {
            // Update weather transition
            if (currentTime < transitionTime) {
                currentTime += deltaTime;
                double ratio = currentTime / transitionTime;
                
                // Interpolate weather conditions
                currentConditions.setTemperature(
                    lerp(currentConditions.getTemperature(), targetConditions.getTemperature(), ratio));
                currentConditions.setHumidity(
                    lerp(currentConditions.getHumidity(), targetConditions.getHumidity(), ratio));
                currentConditions.setWindSpeed(
                    lerp(currentConditions.getWindSpeed(), targetConditions.getWindSpeed(), ratio));
                currentConditions.setPrecipitation(
                    lerp(currentConditions.getPrecipitation(), targetConditions.getPrecipitation(), ratio));
                currentConditions.setVisibility(
                    lerp(currentConditions.getVisibility(), targetConditions.getVisibility(), ratio));
                currentConditions.setPressure(
                    lerp(currentConditions.getPressure(), targetConditions.getPressure(), ratio));
            }
            
            // Update particles
            Iterator<EnvironmentalParticle> iterator = particles.iterator();
            while (iterator.hasNext()) {
                EnvironmentalParticle particle = iterator.next();
                particle.update(deltaTime);
                
                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }
            
            // Generate new particles based on weather
            generateWeatherParticles(deltaTime);
        }
        
        private double lerp(double a, double b, double t) {
            return a + (b - a) * t;
        }
        
        private void generateWeatherParticles(double deltaTime) {
            double precipitation = currentConditions.getPrecipitation();
            WeatherConditions.WeatherType type = currentConditions.getType();
            
            if (precipitation > 0.1) {
                // Calculate particle generation rate based on precipitation
                double rate = precipitation * 100 * deltaTime;
                int particleCount = (int) rate;
                
                for (int i = 0; i < particleCount; i++) {
                    // Random position above the area
                    double x = random.nextGaussian() * 50;
                    double y = 50 + random.nextGaussian() * 10;
                    double z = random.nextGaussian() * 50;
                    Vector3D position = new Vector3D(x, y, z);
                    
                    // Velocity with wind influence
                    Vector3D windInfluence = currentConditions.getWindDirection()
                        .multiply(currentConditions.getWindSpeed() * 0.5);
                    double vx = windInfluence.getX() + random.nextGaussian() * 0.5;
                    double vy = type == WeatherConditions.WeatherType.SNOWY ? 
                        -random.nextDouble() * 2 - 1 : -random.nextDouble() * 10 - 5;
                    double vz = windInfluence.getZ() + random.nextGaussian() * 0.5;
                    Vector3D velocity = new Vector3D(vx, vy, vz);
                    
                    // Particle properties based on weather type
                    EnvironmentalParticle.ParticleType particleType;
                    double mass, size, lifeTime;
                    
                    if (type == WeatherConditions.WeatherType.SNOWY) {
                        particleType = EnvironmentalParticle.ParticleType.SNOW_FLAKE;
                        mass = 0.1;
                        size = 0.1 + random.nextDouble() * 0.2;
                        lifeTime = 20.0 + random.nextDouble() * 10.0;
                    } else if (type == WeatherConditions.WeatherType.RAINY || 
                               type == WeatherConditions.WeatherType.STORMY) {
                        particleType = EnvironmentalParticle.ParticleType.RAIN_DROP;
                        mass = 0.5;
                        size = 0.05 + random.nextDouble() * 0.1;
                        lifeTime = 5.0 + random.nextDouble() * 3.0;
                    } else {
                        particleType = EnvironmentalParticle.ParticleType.WATER_VAPOR;
                        mass = 0.01;
                        size = 0.02 + random.nextDouble() * 0.05;
                        lifeTime = 10.0 + random.nextDouble() * 5.0;
                    }
                    
                    EnvironmentalParticle particle = new EnvironmentalParticle(
                        position, velocity, mass, size, lifeTime, particleType);
                    particles.add(particle);
                }
            }
        }
        
        public void setWeather(WeatherConditions.WeatherType type) {
            targetConditions.setType(type);
            currentTime = 0.0;
            
            // Set target conditions based on weather type
            switch (type) {
                case CLEAR:
                    targetConditions.setTemperature(25.0);
                    targetConditions.setHumidity(0.3);
                    targetConditions.setWindSpeed(1.0);
                    targetConditions.setPrecipitation(0.0);
                    targetConditions.setVisibility(1.0);
                    targetConditions.setPressure(1015.0);
                    break;
                case CLOUDY:
                    targetConditions.setTemperature(20.0);
                    targetConditions.setHumidity(0.7);
                    targetConditions.setWindSpeed(3.0);
                    targetConditions.setPrecipitation(0.0);
                    targetConditions.setVisibility(0.8);
                    targetConditions.setPressure(1010.0);
                    break;
                case RAINY:
                    targetConditions.setTemperature(18.0);
                    targetConditions.setHumidity(0.9);
                    targetConditions.setWindSpeed(5.0);
                    targetConditions.setPrecipitation(0.7);
                    targetConditions.setVisibility(0.6);
                    targetConditions.setPressure(1005.0);
                    break;
                case STORMY:
                    targetConditions.setTemperature(16.0);
                    targetConditions.setHumidity(0.95);
                    targetConditions.setWindSpeed(15.0);
                    targetConditions.setPrecipitation(1.0);
                    targetConditions.setVisibility(0.3);
                    targetConditions.setPressure(995.0);
                    break;
                case SNOWY:
                    targetConditions.setTemperature(-2.0);
                    targetConditions.setHumidity(0.8);
                    targetConditions.setWindSpeed(4.0);
                    targetConditions.setPrecipitation(0.6);
                    targetConditions.setVisibility(0.5);
                    targetConditions.setPressure(1012.0);
                    break;
                case FOGGY:
                    targetConditions.setTemperature(15.0);
                    targetConditions.setHumidity(0.98);
                    targetConditions.setWindSpeed(0.5);
                    targetConditions.setPrecipitation(0.1);
                    targetConditions.setVisibility(0.2);
                    targetConditions.setPressure(1014.0);
                    break;
            }
        }
        
        public WeatherConditions getCurrentConditions() {
            return currentConditions;
        }
        
        public List<EnvironmentalParticle> getParticles() {
            return new ArrayList<>(particles);
        }
        
        public void setTransitionTime(double seconds) {
            this.transitionTime = seconds;
        }
    }
    
    /**
     * Ecosystem simulator for plant and animal behavior
     */
    public static class EcosystemSimulator {
        public static class Organism {
            protected Vector3D position;
            protected double health;
            protected double age;
            protected double energy;
            protected boolean alive;
            protected UUID id;
            
            public Organism(Vector3D position) {
                this.position = position;
                this.health = 1.0;
                this.age = 0.0;
                this.energy = 1.0;
                this.alive = true;
                this.id = UUID.randomUUID();
            }
            
            public void update(double deltaTime) {
                if (alive) {
                    age += deltaTime;
                    // Basic energy consumption
                    energy -= deltaTime * 0.01;
                    
                    if (energy <= 0 || health <= 0) {
                        alive = false;
                    }
                }
            }
            
            public Vector3D getPosition() {
                return position;
            }
            
            public double getHealth() {
                return health;
            }
            
            public double getAge() {
                return age;
            }
            
            public double getEnergy() {
                return energy;
            }
            
            public boolean isAlive() {
                return alive;
            }
            
            public UUID getId() {
                return id;
            }
        }
        
        public static class Plant extends Organism {
            private double growthRate;
            private double size;
            private double seedProduction;
            private List<Plant> nearbyPlants;
            
            public Plant(Vector3D position, double growthRate) {
                super(position);
                this.growthRate = growthRate;
                this.size = 0.1;
                this.seedProduction = 0.0;
                this.nearbyPlants = new ArrayList<>();
            }
            
            @Override
            public void update(double deltaTime) {
                super.update(deltaTime);
                
                if (alive) {
                    // Grow based on health and energy
                    size += growthRate * health * energy * deltaTime * 0.1;
                    
                    // Produce seeds based on size and health
                    seedProduction += size * health * deltaTime * 0.01;
                    
                    // Reproduce if enough seeds
                    if (seedProduction > 10.0 && nearbyPlants.size() < 5) {
                        reproduce();
                        seedProduction = 0.0;
                    }
                }
            }
            
            private void reproduce() {
                // Create a new plant nearby
                double angle = Math.random() * 2 * Math.PI;
                double distance = 2.0 + Math.random() * 3.0;
                double x = position.getX() + Math.cos(angle) * distance;
                double z = position.getZ() + Math.sin(angle) * distance;
                Vector3D newPosition = new Vector3D(x, position.getY(), z);
                
                Plant offspring = new Plant(newPosition, growthRate * (0.8 + Math.random() * 0.4));
                // Add the offspring to the ecosystem for real simulation
                // In a real implementation, this would:
                // - Validate the new plant's position in the environment
                // - Check for collisions with existing objects
                // - Add the plant to the spatial partitioning system
                // - Notify ecosystem observers of the new entity
                // - Handle resource allocation for the new plant
                // For simulation, we'll just log the reproduction event
                System.out.println("Plant produced offspring at " + newPosition);
            }
            
            public double getSize() {
                return size;
            }
            
            public void setNearbyPlants(List<Plant> plants) {
                this.nearbyPlants = plants;
            }
        }
        
        public static class Animal extends Organism {
            protected Vector3D velocity;
            protected Vector3D target;
            protected double speed;
            protected double hunger;
            protected double reproductionUrge;
            protected List<Organism> nearbyOrganisms;
            
            public Animal(Vector3D position, double speed) {
                super(position);
                this.velocity = new Vector3D(0, 0, 0);
                this.target = null;
                this.speed = speed;
                this.hunger = 0.0;
                this.reproductionUrge = 0.0;
                this.nearbyOrganisms = new ArrayList<>();
            }
            
            @Override
            public void update(double deltaTime) {
                super.update(deltaTime);
                
                if (alive) {
                    // Increase hunger and reproduction urge
                    hunger += deltaTime * 0.1;
                    reproductionUrge += deltaTime * 0.05;
                    
                    // Move towards target if exists
                    if (target != null) {
                        Vector3D direction = target.subtract(position).normalize();
                        velocity = direction.multiply(speed);
                        position = position.add(velocity.multiply(deltaTime));
                        
                        // Check if reached target
                        if (position.distance(target) < 1.0) {
                            target = null;
                        }
                    } else {
                        // Random movement
                        if (Math.random() < 0.01) {
                            double angle = Math.random() * 2 * Math.PI;
                            double distance = Math.random() * 10.0;
                            target = new Vector3D(
                                position.getX() + Math.cos(angle) * distance,
                                position.getY(),
                                position.getZ() + Math.sin(angle) * distance
                            );
                        }
                    }
                }
            }
            
            public void setNearbyOrganisms(List<Organism> organisms) {
                this.nearbyOrganisms = organisms;
            }
            
            public Vector3D getVelocity() {
                return velocity;
            }
        }
        
        private List<Plant> plants;
        private List<Animal> animals;
        private double timeOfDay; // 0.0 to 24.0
        private double season;    // 0.0 to 4.0 (0=Spring, 1=Summer, 2=Fall, 3=Winter)
        private final Random random;
        
        public EcosystemSimulator() {
            this.plants = new CopyOnWriteArrayList<>();
            this.animals = new CopyOnWriteArrayList<>();
            this.timeOfDay = 12.0;
            this.season = 0.0;
            this.random = new Random();
        }
        
        public void update(double deltaTime) {
            // Update time
            timeOfDay += deltaTime / 3600.0; // Convert seconds to hours
            if (timeOfDay >= 24.0) {
                timeOfDay -= 24.0;
                season += 1.0 / 30.0; // Change season every 30 days
                if (season >= 4.0) {
                    season = 0.0;
                }
            }
            
            // Update all plants
            for (Plant plant : plants) {
                // Find nearby plants for reproduction
                List<Plant> nearby = findNearbyPlants(plant, 10.0);
                plant.setNearbyPlants(nearby);
                plant.update(deltaTime);
            }
            
            // Update all animals
            for (Animal animal : animals) {
                // Find nearby organisms
                List<Organism> nearby = findNearbyOrganisms(animal, 20.0);
                animal.setNearbyOrganisms(nearby);
                animal.update(deltaTime);
            }
            
            // Remove dead organisms
            plants.removeIf(plant -> !plant.isAlive());
            animals.removeIf(animal -> !animal.isAlive());
            
            // Spawn new organisms occasionally
            if (random.nextDouble() < 0.001) {
                spawnRandomOrganism();
            }
        }
        
        private List<Plant> findNearbyPlants(Plant plant, double radius) {
            List<Plant> nearby = new ArrayList<>();
            for (Plant other : plants) {
                if (other != plant && plant.getPosition().distance(other.getPosition()) <= radius) {
                    nearby.add(other);
                }
            }
            return nearby;
        }
        
        private List<Organism> findNearbyOrganisms(Organism organism, double radius) {
            List<Organism> nearby = new ArrayList<>();
            for (Plant plant : plants) {
                if (organism.getPosition().distance(plant.getPosition()) <= radius) {
                    nearby.add(plant);
                }
            }
            for (Animal animal : animals) {
                if (organism != animal && organism.getPosition().distance(animal.getPosition()) <= radius) {
                    nearby.add(animal);
                }
            }
            return nearby;
        }
        
        private void spawnRandomOrganism() {
            double x = random.nextGaussian() * 50;
            double z = random.nextGaussian() * 50;
            Vector3D position = new Vector3D(x, 0, z);
            
            if (random.nextDouble() < 0.7) {
                // Spawn plant
                double growthRate = 0.1 + random.nextDouble() * 0.2;
                plants.add(new Plant(position, growthRate));
            } else {
                // Spawn animal
                double speed = 1.0 + random.nextDouble() * 3.0;
                animals.add(new Animal(position, speed));
            }
        }
        
        public void addPlant(Plant plant) {
            plants.add(plant);
        }
        
        public void addAnimal(Animal animal) {
            animals.add(animal);
        }
        
        public List<Plant> getPlants() {
            return new ArrayList<>(plants);
        }
        
        public List<Animal> getAnimals() {
            return new ArrayList<>(animals);
        }
        
        public double getTimeOfDay() {
            return timeOfDay;
        }
        
        public double getSeason() {
            return season;
        }
    }
    
    /**
     * Geological simulation for terrain and landscape changes
     */
    public static class GeologicalSimulator {
        public static class TerrainChunk {
            private Vector3D position;
            private double[][] heightMap;
            private double[][] temperatureMap;
            private double[][] moistureMap;
            private int width, depth;
            private long lastUpdate;
            
            public TerrainChunk(Vector3D position, int width, int depth) {
                this.position = position;
                this.width = width;
                this.depth = depth;
                this.heightMap = new double[width][depth];
                this.temperatureMap = new double[width][depth];
                this.moistureMap = new double[width][depth];
                this.lastUpdate = System.currentTimeMillis();
                
                // Initialize with some basic terrain
                initializeTerrain();
            }
            
            private void initializeTerrain() {
                NoiseGenerator noise = new NoiseGenerator(System.currentTimeMillis());
                
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        double worldX = position.getX() + x;
                        double worldZ = position.getZ() + z;
                        
                        // Generate height using fractal noise
                        double height = noise.fractalBrownianMotion(worldX * 0.01, worldZ * 0.01, 4, 0.5, 1.0) * 50;
                        heightMap[x][z] = height;
                        
                        // Generate temperature (decreases with height and latitude)
                        double temperature = 20.0 - height * 0.1 - Math.abs(worldZ) * 0.01;
                        temperatureMap[x][z] = temperature;
                        
                        // Generate moisture (random with some coherence)
                        double moisture = noise.perlinNoise(worldX * 0.005, worldZ * 0.005) * 0.5 + 0.5;
                        moistureMap[x][z] = moisture;
                    }
                }
            }
            
            public void update(double deltaTime, WeatherConditions weather) {
                long currentTime = System.currentTimeMillis();
                double timeElapsed = (currentTime - lastUpdate) / 1000.0; // Convert to seconds
                lastUpdate = currentTime;
                
                // Apply weathering effects
                applyWeathering(weather, timeElapsed);
                
                // Apply erosion
                applyErosion(timeElapsed);
            }
            
            private void applyWeathering(WeatherConditions weather, double timeElapsed) {
                double precipitation = weather.getPrecipitation();
                double temperature = weather.getTemperature();
                
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        // Freeze-thaw weathering
                        if (temperature < 0 && heightMap[x][z] > 0) {
                            heightMap[x][z] -= timeElapsed * 0.0001 * precipitation;
                        }
                        
                        // Chemical weathering (more effective in warm, wet conditions)
                        double weatheringRate = 0.00005 * precipitation * Math.max(0, temperature) * timeElapsed;
                        heightMap[x][z] -= weatheringRate;
                    }
                }
            }
            
            private void applyErosion(double timeElapsed) {
                // Simple water erosion model
                for (int x = 1; x < width - 1; x++) {
                    for (int z = 1; z < depth - 1; z++) {
                        // Calculate slope in each direction
                        double slopeX = heightMap[x + 1][z] - heightMap[x - 1][z];
                        double slopeZ = heightMap[x][z + 1] - heightMap[x][z - 1];
                        
                        // Erode based on slope
                        double erosionRate = Math.sqrt(slopeX * slopeX + slopeZ * slopeZ) * 0.0001 * timeElapsed;
                        heightMap[x][z] -= erosionRate;
                    }
                }
            }
            
            public double getHeight(int x, int z) {
                if (x >= 0 && x < width && z >= 0 && z < depth) {
                    return heightMap[x][z];
                }
                return 0;
            }
            
            public double getTemperature(int x, int z) {
                if (x >= 0 && x < width && z >= 0 && z < depth) {
                    return temperatureMap[x][z];
                }
                return 0;
            }
            
            public double getMoisture(int x, int z) {
                if (x >= 0 && x < width && z >= 0 && z < depth) {
                    return moistureMap[x][z];
                }
                return 0;
            }
            
            public Vector3D getPosition() {
                return position;
            }
        }
        
        private Map<String, TerrainChunk> chunks;
        private final int chunkSize;
        private NoiseGenerator noiseGenerator;
        
        public GeologicalSimulator(int chunkSize) {
            this.chunks = new ConcurrentHashMap<>();
            this.chunkSize = chunkSize;
            this.noiseGenerator = new NoiseGenerator(System.currentTimeMillis());
        }
        
        public void update(double deltaTime, WeatherConditions weather) {
            // Update all chunks
            for (TerrainChunk chunk : chunks.values()) {
                chunk.update(deltaTime, weather);
            }
        }
        
        public TerrainChunk getOrCreateChunk(int chunkX, int chunkZ) {
            String key = chunkX + "," + chunkZ;
            TerrainChunk chunk = chunks.get(key);
            
            if (chunk == null) {
                Vector3D position = new Vector3D(chunkX * chunkSize, 0, chunkZ * chunkSize);
                chunk = new TerrainChunk(position, chunkSize, chunkSize);
                chunks.put(key, chunk);
            }
            
            return chunk;
        }
        
        public double getHeightAt(double x, double z) {
            int chunkX = (int) Math.floor(x / chunkSize);
            int chunkZ = (int) Math.floor(z / chunkSize);
            TerrainChunk chunk = getOrCreateChunk(chunkX, chunkZ);
            
            int localX = (int) (x - chunkX * chunkSize);
            int localZ = (int) (z - chunkZ * chunkSize);
            
            return chunk.getHeight(localX, localZ);
        }
        
        public double getTemperatureAt(double x, double z) {
            int chunkX = (int) Math.floor(x / chunkSize);
            int chunkZ = (int) Math.floor(z / chunkSize);
            TerrainChunk chunk = getOrCreateChunk(chunkX, chunkZ);
            
            int localX = (int) (x - chunkX * chunkSize);
            int localZ = (int) (z - chunkZ * chunkSize);
            
            return chunk.getTemperature(localX, localZ);
        }
        
        public double getMoistureAt(double x, double z) {
            int chunkX = (int) Math.floor(x / chunkSize);
            int chunkZ = (int) Math.floor(z / chunkSize);
            TerrainChunk chunk = getOrCreateChunk(chunkX, chunkZ);
            
            int localX = (int) (x - chunkX * chunkSize);
            int localZ = (int) (z - chunkZ * chunkSize);
            
            return chunk.getMoisture(localX, localZ);
        }
        
        public Collection<TerrainChunk> getAllChunks() {
            return chunks.values();
        }
    }
    
    /**
     * Main environment simulation system
     */
    public static class Simulator {
        private WeatherSystem weatherSystem;
        private EcosystemSimulator ecosystem;
        private GeologicalSimulator geology;
        private boolean running;
        
        public Simulator() {
            this.weatherSystem = new WeatherSystem();
            this.ecosystem = new EcosystemSimulator();
            this.geology = new GeologicalSimulator(64); // 64x64 chunks
            this.running = false;
        }
        
        public void start() {
            running = true;
        }
        
        public void stop() {
            running = false;
        }
        
        public void update(double deltaTime) {
            if (running) {
                weatherSystem.update(deltaTime);
                ecosystem.update(deltaTime);
                geology.update(deltaTime, weatherSystem.getCurrentConditions());
            }
        }
        
        public WeatherSystem getWeatherSystem() {
            return weatherSystem;
        }
        
        public EcosystemSimulator getEcosystem() {
            return ecosystem;
        }
        
        public GeologicalSimulator getGeology() {
            return geology;
        }
        
        public void setWeather(WeatherConditions.WeatherType type) {
            weatherSystem.setWeather(type);
        }
        
        public void setTimeOfDay(double hour) {
            // This would need to be implemented in the ecosystem
        }
        
        public void setSeason(double season) {
            // This would need to be implemented in the ecosystem
        }
    }
}