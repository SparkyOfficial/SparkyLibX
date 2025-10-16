package com.sparky.libx.game;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.math.Matrix4x4;
import com.sparky.libx.math.Quaternion;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

/**
 * Advanced Game Engine Framework for Minecraft Plugins
 * Provides capabilities for game development, entity systems, physics, and advanced rendering
 * 
 * @author Андрій Будильников
 */
public class AdvancedGameEngine {
    
    /**
     * Represents an entity in the game world
     */
    public static class Entity {
        private final long id;
        private final Map<Class<? extends Component>, Component> components;
        private final List<Entity> children;
        private Entity parent;
        private boolean active;
        
        public Entity(long id) {
            this.id = id;
            this.components = new HashMap<>();
            this.children = new ArrayList<>();
            this.parent = null;
            this.active = true;
        }
        
        /**
         * Adds a component to the entity
         */
        public <T extends Component> void addComponent(T component) {
            components.put(component.getClass(), component);
            component.setEntity(this);
        }
        
        /**
         * Removes a component from the entity
         */
        public <T extends Component> void removeComponent(Class<T> componentClass) {
            Component component = components.remove(componentClass);
            if (component != null) {
                component.setEntity(null);
            }
        }
        
        /**
         * Gets a component from the entity
         */
        @SuppressWarnings("unchecked")
        public <T extends Component> T getComponent(Class<T> componentClass) {
            return (T) components.get(componentClass);
        }
        
        /**
         * Checks if the entity has a component
         */
        public <T extends Component> boolean hasComponent(Class<T> componentClass) {
            return components.containsKey(componentClass);
        }
        
        /**
         * Adds a child entity
         */
        public void addChild(Entity child) {
            children.add(child);
            child.parent = this;
        }
        
        /**
         * Removes a child entity
         */
        public void removeChild(Entity child) {
            children.remove(child);
            child.parent = null;
        }
        
        /**
         * Gets the entity's ID
         */
        public long getId() {
            return id;
        }
        
        /**
         * Gets the entity's children
         */
        public List<Entity> getChildren() {
            return new ArrayList<>(children);
        }
        
        /**
         * Gets the entity's parent
         */
        public Entity getParent() {
            return parent;
        }
        
        /**
         * Checks if the entity is active
         */
        public boolean isActive() {
            return active;
        }
        
        /**
         * Sets the entity's active state
         */
        public void setActive(boolean active) {
            this.active = active;
        }
        
        /**
         * Gets all components
         */
        public Collection<Component> getComponents() {
            return new ArrayList<>(components.values());
        }
    }
    
    /**
     * Represents a component that can be attached to an entity
     */
    public abstract static class Component {
        private Entity entity;
        
        /**
         * Gets the entity this component is attached to
         */
        public Entity getEntity() {
            return entity;
        }
        
        /**
         * Sets the entity this component is attached to
         */
        void setEntity(Entity entity) {
            this.entity = entity;
        }
        
        /**
         * Updates the component
         */
        public abstract void update(double deltaTime);
    }
    
    /**
     * Represents a transform component
     */
    public static class TransformComponent extends Component {
        private Vector3D position;
        private Quaternion rotation;
        private Vector3D scale;
        
        public TransformComponent() {
            this.position = new Vector3D(0, 0, 0);
            this.rotation = Quaternion.identity();
            this.scale = new Vector3D(1, 1, 1);
        }
        
        public TransformComponent(Vector3D position, Quaternion rotation, Vector3D scale) {
            this.position = position;
            this.rotation = rotation;
            this.scale = scale;
        }
        
        @Override
        public void update(double deltaTime) {
            // Transform components don't need updating in this simple implementation
        }
        
        /**
         * Gets the position
         */
        public Vector3D getPosition() {
            return position;
        }
        
        /**
         * Sets the position
         */
        public void setPosition(Vector3D position) {
            this.position = position;
        }
        
        /**
         * Gets the rotation
         */
        public Quaternion getRotation() {
            return rotation;
        }
        
        /**
         * Sets the rotation
         */
        public void setRotation(Quaternion rotation) {
            this.rotation = rotation;
        }
        
        /**
         * Gets the scale
         */
        public Vector3D getScale() {
            return scale;
        }
        
        /**
         * Sets the scale
         */
        public void setScale(Vector3D scale) {
            this.scale = scale;
        }
        
        /**
         * Gets the transformation matrix
         */
        public Matrix4x4 getTransformMatrix() {
            Matrix4x4 translationMatrix = Matrix4x4.createTranslation(position.getX(), position.getY(), position.getZ());
            Matrix4x4 rotationMatrix = quaternionToMatrix(rotation);
            Matrix4x4 scaleMatrix = Matrix4x4.createScale(scale.getX(), scale.getY(), scale.getZ());
            
            return translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix);
        }
        
        /**
         * Converts a quaternion to a rotation matrix
         */
        private Matrix4x4 quaternionToMatrix(Quaternion q) {
            double w = q.getW();
            double x = q.getX();
            double y = q.getY();
            double z = q.getZ();
            
            Matrix4x4 matrix = new Matrix4x4();
            matrix.set(0, 0, 1 - 2 * (y * y + z * z));
            matrix.set(0, 1, 2 * (x * y - w * z));
            matrix.set(0, 2, 2 * (x * z + w * y));
            matrix.set(1, 0, 2 * (x * y + w * z));
            matrix.set(1, 1, 1 - 2 * (x * x + z * z));
            matrix.set(1, 2, 2 * (y * z - w * x));
            matrix.set(2, 0, 2 * (x * z - w * y));
            matrix.set(2, 1, 2 * (y * z + w * x));
            matrix.set(2, 2, 1 - 2 * (x * x + y * y));
            matrix.set(3, 3, 1);
            
            return matrix;
        }
    }
    
    /**
     * Represents a render component
     */
    public static class RenderComponent extends Component {
        private String meshName;
        private String materialName;
        private boolean visible;
        
        public RenderComponent(String meshName, String materialName) {
            this.meshName = meshName;
            this.materialName = materialName;
            this.visible = true;
        }
        
        @Override
        public void update(double deltaTime) {
            // Render components don't need updating in this simple implementation
        }
        
        /**
         * Gets the mesh name
         */
        public String getMeshName() {
            return meshName;
        }
        
        /**
         * Sets the mesh name
         */
        public void setMeshName(String meshName) {
            this.meshName = meshName;
        }
        
        /**
         * Gets the material name
         */
        public String getMaterialName() {
            return materialName;
        }
        
        /**
         * Sets the material name
         */
        public void setMaterialName(String materialName) {
            this.materialName = materialName;
        }
        
        /**
         * Checks if the component is visible
         */
        public boolean isVisible() {
            return visible;
        }
        
        /**
         * Sets the visibility
         */
        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }
    
    /**
     * Represents a physics component
     */
    public static class PhysicsComponent extends Component {
        private Vector3D velocity;
        private Vector3D acceleration;
        private Vector3D force;
        private double mass;
        private boolean affectedByGravity;
        
        public PhysicsComponent(double mass) {
            this.velocity = new Vector3D(0, 0, 0);
            this.acceleration = new Vector3D(0, 0, 0);
            this.force = new Vector3D(0, 0, 0);
            this.mass = mass;
            this.affectedByGravity = true;
        }
        
        @Override
        public void update(double deltaTime) {
            // Apply forces
            acceleration = force.multiply(1.0 / mass);
            
            // Apply gravity if enabled
            if (affectedByGravity) {
                acceleration = acceleration.add(new Vector3D(0, -9.81, 0));
            }
            
            // Update velocity
            velocity = velocity.add(acceleration.multiply(deltaTime));
            
            // Update position through transform component
            TransformComponent transform = getEntity().getComponent(TransformComponent.class);
            if (transform != null) {
                Vector3D newPosition = transform.getPosition().add(velocity.multiply(deltaTime));
                transform.setPosition(newPosition);
            }
            
            // Reset forces
            force = new Vector3D(0, 0, 0);
        }
        
        /**
         * Applies a force to the entity
         */
        public void applyForce(Vector3D force) {
            this.force = this.force.add(force);
        }
        
        /**
         * Gets the velocity
         */
        public Vector3D getVelocity() {
            return velocity;
        }
        
        /**
         * Sets the velocity
         */
        public void setVelocity(Vector3D velocity) {
            this.velocity = velocity;
        }
        
        /**
         * Gets the acceleration
         */
        public Vector3D getAcceleration() {
            return acceleration;
        }
        
        /**
         * Gets the mass
         */
        public double getMass() {
            return mass;
        }
        
        /**
         * Sets the mass
         */
        public void setMass(double mass) {
            this.mass = mass;
        }
        
        /**
         * Checks if affected by gravity
         */
        public boolean isAffectedByGravity() {
            return affectedByGravity;
        }
        
        /**
         * Sets whether affected by gravity
         */
        public void setAffectedByGravity(boolean affectedByGravity) {
            this.affectedByGravity = affectedByGravity;
        }
    }
    
    /**
     * Represents an entity system manager
     */
    public static class EntityManager {
        private final Map<Long, Entity> entities;
        private final Map<Class<? extends Component>, List<Entity>> componentEntities;
        private final AtomicLong entityIdCounter;
        private final Lock entitiesLock;
        
        public EntityManager() {
            this.entities = new ConcurrentHashMap<>();
            this.componentEntities = new ConcurrentHashMap<>();
            this.entityIdCounter = new AtomicLong(0);
            this.entitiesLock = new ReentrantLock();
        }
        
        /**
         * Creates a new entity
         */
        public Entity createEntity() {
            long id = entityIdCounter.incrementAndGet();
            Entity entity = new Entity(id);
            entities.put(id, entity);
            return entity;
        }
        
        /**
         * Destroys an entity
         */
        public void destroyEntity(Entity entity) {
            entitiesLock.lock();
            try {
                // Remove from component mappings
                for (Component component : entity.getComponents()) {
                    Class<? extends Component> componentClass = component.getClass();
                    List<Entity> entityList = componentEntities.get(componentClass);
                    if (entityList != null) {
                        entityList.remove(entity);
                    }
                }
                
                // Remove from entities map
                entities.remove(entity.getId());
                
                // Destroy children
                for (Entity child : entity.getChildren()) {
                    destroyEntity(child);
                }
            } finally {
                entitiesLock.unlock();
            }
        }
        
        /**
         * Gets an entity by ID
         */
        public Entity getEntity(long id) {
            return entities.get(id);
        }
        
        /**
         * Gets all entities with a specific component
         */
        public List<Entity> getEntitiesWithComponent(Class<? extends Component> componentClass) {
            return new ArrayList<>(componentEntities.getOrDefault(componentClass, new ArrayList<>()));
        }
        
        /**
         * Registers a component with an entity
         */
        public void registerComponent(Entity entity, Component component) {
            Class<? extends Component> componentClass = component.getClass();
            componentEntities.computeIfAbsent(componentClass, k -> new CopyOnWriteArrayList<>()).add(entity);
        }
        
        /**
         * Unregisters a component from an entity
         */
        public void unregisterComponent(Entity entity, Class<? extends Component> componentClass) {
            List<Entity> entityList = componentEntities.get(componentClass);
            if (entityList != null) {
                entityList.remove(entity);
            }
        }
        
        /**
         * Gets all entities
         */
        public Collection<Entity> getAllEntities() {
            return new ArrayList<>(entities.values());
        }
    }
    
    /**
     * Represents a game system
     */
    public abstract static class GameSystem {
        protected EntityManager entityManager;
        
        public GameSystem(EntityManager entityManager) {
            this.entityManager = entityManager;
        }
        
        /**
         * Updates the system
         */
        public abstract void update(double deltaTime);
    }
    
    /**
     * Represents a rendering system
     */
    public static class RenderingSystem extends GameSystem {
        public RenderingSystem(EntityManager entityManager) {
            super(entityManager);
        }
        
        @Override
        public void update(double deltaTime) {
            // In a real implementation, this would render all visible entities
            List<Entity> renderEntities = entityManager.getEntitiesWithComponent(RenderComponent.class);
            
            for (Entity entity : renderEntities) {
                if (entity.isActive()) {
                    RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
                    if (renderComponent != null && renderComponent.isVisible()) {
                        // Render the entity
                        renderEntity(entity, deltaTime);
                    }
                }
            }
        }
        
        /**
         * Renders an entity
         */
        private void renderEntity(Entity entity, double deltaTime) {
            // In a real implementation, this would perform actual rendering
            System.out.println("Rendering entity " + entity.getId());
        }
    }
    
    /**
     * Represents a physics system
     */
    public static class PhysicsSystem extends GameSystem {
        public PhysicsSystem(EntityManager entityManager) {
            super(entityManager);
        }
        
        @Override
        public void update(double deltaTime) {
            List<Entity> physicsEntities = entityManager.getEntitiesWithComponent(PhysicsComponent.class);
            
            for (Entity entity : physicsEntities) {
                if (entity.isActive()) {
                    PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
                    if (physicsComponent != null) {
                        physicsComponent.update(deltaTime);
                    }
                }
            }
        }
    }
    
    /**
     * Represents a game engine
     */
    public static class GameEngine {
        private final EntityManager entityManager;
        private final List<GameSystem> systems;
        private final ScheduledExecutorService gameLoop;
        private volatile boolean running;
        private volatile double targetFPS;
        
        public GameEngine() {
            this.entityManager = new EntityManager();
            this.systems = new ArrayList<>();
            this.gameLoop = Executors.newSingleThreadScheduledExecutor();
            this.running = false;
            this.targetFPS = 60.0;
        }
        
        /**
         * Adds a system to the engine
         */
        public void addSystem(GameSystem system) {
            systems.add(system);
        }
        
        /**
         * Removes a system from the engine
         */
        public void removeSystem(GameSystem system) {
            systems.remove(system);
        }
        
        /**
         * Starts the game engine
         */
        public void start() {
            if (running) {
                throw new IllegalStateException("Game engine is already running");
            }
            
            running = true;
            
            // Start game loop
            long frameDelay = (long) (1000.0 / targetFPS);
            gameLoop.scheduleAtFixedRate(this::gameLoop, 0, frameDelay, TimeUnit.MILLISECONDS);
            
            System.out.println("Game engine started");
        }
        
        /**
         * Stops the game engine
         */
        public void stop() {
            if (!running) {
                return;
            }
            
            running = false;
            gameLoop.shutdown();
            
            try {
                if (!gameLoop.awaitTermination(5, TimeUnit.SECONDS)) {
                    gameLoop.shutdownNow();
                }
            } catch (InterruptedException e) {
                gameLoop.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            System.out.println("Game engine stopped");
        }
        
        /**
         * The main game loop
         */
        private void gameLoop() {
            if (!running) {
                return;
            }
            
            long startTime = System.nanoTime();
            double deltaTime = 1.0 / targetFPS;
            
            // Update all systems
            for (GameSystem system : systems) {
                try {
                    system.update(deltaTime);
                } catch (Exception e) {
                    System.err.println("Error updating system: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            long endTime = System.nanoTime();
            long frameTime = endTime - startTime;
            long frameTimeMs = frameTime / 1_000_000;
            
            System.out.println("Frame time: " + frameTimeMs + "ms");
        }
        
        /**
         * Gets the entity manager
         */
        public EntityManager getEntityManager() {
            return entityManager;
        }
        
        /**
         * Sets the target FPS
         */
        public void setTargetFPS(double targetFPS) {
            this.targetFPS = targetFPS;
        }
        
        /**
         * Gets the target FPS
         */
        public double getTargetFPS() {
            return targetFPS;
        }
    }
    
    /**
     * Represents a game state manager
     */
    public static class GameStateManager {
        private final Stack<GameState> stateStack;
        private final Lock stackLock;
        
        public GameStateManager() {
            this.stateStack = new Stack<>();
            this.stackLock = new ReentrantLock();
        }
        
        /**
         * Pushes a new game state onto the stack
         */
        public void pushState(GameState state) {
            stackLock.lock();
            try {
                if (!stateStack.isEmpty()) {
                    stateStack.peek().pause();
                }
                stateStack.push(state);
                state.enter();
            } finally {
                stackLock.unlock();
            }
        }
        
        /**
         * Pops the current game state from the stack
         */
        public void popState() {
            stackLock.lock();
            try {
                if (!stateStack.isEmpty()) {
                    GameState state = stateStack.pop();
                    state.exit();
                    
                    if (!stateStack.isEmpty()) {
                        stateStack.peek().resume();
                    }
                }
            } finally {
                stackLock.unlock();
            }
        }
        
        /**
         * Replaces the current game state
         */
        public void changeState(GameState state) {
            stackLock.lock();
            try {
                if (!stateStack.isEmpty()) {
                    GameState oldState = stateStack.pop();
                    oldState.exit();
                }
                stateStack.push(state);
                state.enter();
            } finally {
                stackLock.unlock();
            }
        }
        
        /**
         * Updates the current game state
         */
        public void update(double deltaTime) {
            stackLock.lock();
            try {
                if (!stateStack.isEmpty()) {
                    stateStack.peek().update(deltaTime);
                }
            } finally {
                stackLock.unlock();
            }
        }
        
        /**
         * Renders the current game state
         */
        public void render() {
            stackLock.lock();
            try {
                if (!stateStack.isEmpty()) {
                    stateStack.peek().render();
                }
            } finally {
                stackLock.unlock();
            }
        }
        
        /**
         * Gets the current game state
         */
        public GameState getCurrentState() {
            stackLock.lock();
            try {
                return stateStack.isEmpty() ? null : stateStack.peek();
            } finally {
                stackLock.unlock();
            }
        }
    }
    
    /**
     * Represents a game state
     */
    public abstract static class GameState {
        /**
         * Called when entering the state
         */
        public abstract void enter();
        
        /**
         * Called when exiting the state
         */
        public abstract void exit();
        
        /**
         * Called when pausing the state
         */
        public abstract void pause();
        
        /**
         * Called when resuming the state
         */
        public abstract void resume();
        
        /**
         * Updates the state
         */
        public abstract void update(double deltaTime);
        
        /**
         * Renders the state
         */
        public abstract void render();
    }
    
    /**
     * Represents an input manager
     */
    public static class InputManager {
        private final Map<Integer, Boolean> keyStates;
        private final Map<Integer, Boolean> previousKeyStates;
        private final Lock inputLock;
        
        public InputManager() {
            this.keyStates = new ConcurrentHashMap<>();
            this.previousKeyStates = new ConcurrentHashMap<>();
            this.inputLock = new ReentrantLock();
        }
        
        /**
         * Updates the input manager
         */
        public void update() {
            inputLock.lock();
            try {
                // Copy current states to previous states
                previousKeyStates.clear();
                previousKeyStates.putAll(keyStates);
            } finally {
                inputLock.unlock();
            }
        }
        
        /**
         * Sets the state of a key
         */
        public void setKeyState(int keyCode, boolean pressed) {
            keyStates.put(keyCode, pressed);
        }
        
        /**
         * Checks if a key is currently pressed
         */
        public boolean isKeyPressed(int keyCode) {
            return keyStates.getOrDefault(keyCode, false);
        }
        
        /**
         * Checks if a key was just pressed
         */
        public boolean isKeyJustPressed(int keyCode) {
            boolean current = keyStates.getOrDefault(keyCode, false);
            boolean previous = previousKeyStates.getOrDefault(keyCode, false);
            return current && !previous;
        }
        
        /**
         * Checks if a key was just released
         */
        public boolean isKeyJustReleased(int keyCode) {
            boolean current = keyStates.getOrDefault(keyCode, false);
            boolean previous = previousKeyStates.getOrDefault(keyCode, false);
            return !current && previous;
        }
    }
    
    /**
     * Represents a resource manager
     */
    public static class ResourceManager {
        private final Map<String, Object> resources;
        private final Lock resourcesLock;
        
        public ResourceManager() {
            this.resources = new ConcurrentHashMap<>();
            this.resourcesLock = new ReentrantLock();
        }
        
        /**
         * Loads a resource
         */
        public <T> void loadResource(String name, T resource) {
            resourcesLock.lock();
            try {
                resources.put(name, resource);
            } finally {
                resourcesLock.unlock();
            }
        }
        
        /**
         * Gets a resource
         */
        @SuppressWarnings("unchecked")
        public <T> T getResource(String name, Class<T> type) {
            Object resource = resources.get(name);
            if (resource != null && type.isInstance(resource)) {
                return (T) resource;
            }
            return null;
        }
        
        /**
         * Unloads a resource
         */
        public void unloadResource(String name) {
            resourcesLock.lock();
            try {
                resources.remove(name);
            } finally {
                resourcesLock.unlock();
            }
        }
        
        /**
         * Checks if a resource is loaded
         */
        public boolean isResourceLoaded(String name) {
            return resources.containsKey(name);
        }
        
        /**
         * Gets all resource names
         */
        public Set<String> getResourceNames() {
            return new HashSet<>(resources.keySet());
        }
    }
    
    /**
     * Represents a particle system
     */
    public static class ParticleSystem {
        private final List<Particle> particles;
        private final Vector3D emitterPosition;
        private final Vector3D emitterVelocity;
        private final double emissionRate;
        private final Lock particlesLock;
        
        public ParticleSystem(Vector3D emitterPosition, double emissionRate) {
            this.particles = new ArrayList<>();
            this.emitterPosition = emitterPosition;
            this.emitterVelocity = new Vector3D(0, 0, 0);
            this.emissionRate = emissionRate;
            this.particlesLock = new ReentrantLock();
        }
        
        /**
         * Updates the particle system
         */
        public void update(double deltaTime) {
            particlesLock.lock();
            try {
                // Update existing particles
                Iterator<Particle> iterator = particles.iterator();
                while (iterator.hasNext()) {
                    Particle particle = iterator.next();
                    particle.update(deltaTime);
                    
                    // Remove dead particles
                    if (particle.getLifetime() <= 0) {
                        iterator.remove();
                    }
                }
                
                // Emit new particles
                emitParticles(deltaTime);
            } finally {
                particlesLock.unlock();
            }
        }
        
        /**
         * Emits particles based on emission rate
         */
        private void emitParticles(double deltaTime) {
            double particlesToEmit = emissionRate * deltaTime;
            int particleCount = (int) Math.floor(particlesToEmit);
            
            // Add fractional particle with some probability
            if (Math.random() < (particlesToEmit - particleCount)) {
                particleCount++;
            }
            
            for (int i = 0; i < particleCount; i++) {
                // Create particle with random properties
                Vector3D position = emitterPosition.add(new Vector3D(
                    (Math.random() - 0.5) * 2,
                    (Math.random() - 0.5) * 2,
                    (Math.random() - 0.5) * 2
                ));
                
                Vector3D velocity = emitterVelocity.add(new Vector3D(
                    (Math.random() - 0.5) * 10,
                    Math.random() * 5,
                    (Math.random() - 0.5) * 10
                ));
                
                Particle particle = new Particle(position, velocity, 1.0, 5.0);
                particles.add(particle);
            }
        }
        
        /**
         * Gets all particles
         */
        public List<Particle> getParticles() {
            particlesLock.lock();
            try {
                return new ArrayList<>(particles);
            } finally {
                particlesLock.unlock();
            }
        }
        
        /**
         * Gets the emitter position
         */
        public Vector3D getEmitterPosition() {
            return emitterPosition;
        }
        
        /**
         * Gets the emission rate
         */
        public double getEmissionRate() {
            return emissionRate;
        }
    }
    
    /**
     * Represents a particle
     */
    public static class Particle {
        private Vector3D position;
        private Vector3D velocity;
        private Vector3D acceleration;
        private double mass;
        private double lifetime;
        private double maxLifetime;
        
        public Particle(Vector3D position, Vector3D velocity, double mass, double lifetime) {
            this.position = position;
            this.velocity = velocity;
            this.acceleration = new Vector3D(0, 0, 0);
            this.mass = mass;
            this.lifetime = lifetime;
            this.maxLifetime = lifetime;
        }
        
        /**
         * Updates the particle's motion
         */
        public void update(double deltaTime) {
            // Update velocity and position
            velocity = velocity.add(acceleration.multiply(deltaTime));
            position = position.add(velocity.multiply(deltaTime));
            
            // Update lifetime
            lifetime -= deltaTime;
            
            // Reset acceleration
            acceleration = new Vector3D(0, 0, 0);
        }
        
        /**
         * Applies a force to the particle
         */
        public void applyForce(Vector3D force) {
            acceleration = acceleration.add(force.multiply(1.0 / mass));
        }
        
        /**
         * Gets the position
         */
        public Vector3D getPosition() {
            return position;
        }
        
        /**
         * Gets the velocity
         */
        public Vector3D getVelocity() {
            return velocity;
        }
        
        /**
         * Gets the lifetime
         */
        public double getLifetime() {
            return lifetime;
        }
        
        /**
         * Gets the maximum lifetime
         */
        public double getMaxLifetime() {
            return maxLifetime;
        }
        
        /**
         * Gets the mass
         */
        public double getMass() {
            return mass;
        }
    }
    
    /**
     * Represents a camera
     */
    public static class Camera {
        private Vector3D position;
        private Quaternion rotation;
        private double fov;
        private double nearPlane;
        private double farPlane;
        private double aspectRatio;
        
        public Camera() {
            this.position = new Vector3D(0, 0, 0);
            this.rotation = Quaternion.identity();
            this.fov = Math.toRadians(60);
            this.nearPlane = 0.1;
            this.farPlane = 1000.0;
            this.aspectRatio = 16.0 / 9.0;
        }
        
        /**
         * Gets the view matrix
         */
        public Matrix4x4 getViewMatrix() {
            // Convert quaternion to rotation matrix
            Matrix4x4 rotationMatrix = quaternionToMatrix(rotation);
            
            // Create translation matrix (negative position)
            Matrix4x4 translationMatrix = Matrix4x4.createTranslation(-position.getX(), -position.getY(), -position.getZ());
            
            // View matrix = rotation * translation
            return rotationMatrix.multiply(translationMatrix);
        }
        
        /**
         * Gets the projection matrix
         */
        public Matrix4x4 getProjectionMatrix() {
            double f = 1.0 / Math.tan(fov / 2.0);
            double rangeInv = 1.0 / (nearPlane - farPlane);
            
            Matrix4x4 matrix = new Matrix4x4();
            matrix.set(0, 0, f / aspectRatio);
            matrix.set(1, 1, f);
            matrix.set(2, 2, (nearPlane + farPlane) * rangeInv);
            matrix.set(2, 3, -1);
            matrix.set(3, 2, 2.0 * nearPlane * farPlane * rangeInv);
            matrix.set(3, 3, 0);
            
            return matrix;
        }
        
        /**
         * Converts a quaternion to a rotation matrix
         */
        private Matrix4x4 quaternionToMatrix(Quaternion q) {
            double w = q.getW();
            double x = q.getX();
            double y = q.getY();
            double z = q.getZ();
            
            Matrix4x4 matrix = new Matrix4x4();
            matrix.set(0, 0, 1 - 2 * (y * y + z * z));
            matrix.set(0, 1, 2 * (x * y - w * z));
            matrix.set(0, 2, 2 * (x * z + w * y));
            matrix.set(1, 0, 2 * (x * y + w * z));
            matrix.set(1, 1, 1 - 2 * (x * x + z * z));
            matrix.set(1, 2, 2 * (y * z - w * x));
            matrix.set(2, 0, 2 * (x * z - w * y));
            matrix.set(2, 1, 2 * (y * z + w * x));
            matrix.set(2, 2, 1 - 2 * (x * x + y * y));
            matrix.set(3, 3, 1);
            
            return matrix;
        }
        
        /**
         * Gets the position
         */
        public Vector3D getPosition() {
            return position;
        }
        
        /**
         * Sets the position
         */
        public void setPosition(Vector3D position) {
            this.position = position;
        }
        
        /**
         * Gets the rotation
         */
        public Quaternion getRotation() {
            return rotation;
        }
        
        /**
         * Sets the rotation
         */
        public void setRotation(Quaternion rotation) {
            this.rotation = rotation;
        }
        
        /**
         * Gets the field of view
         */
        public double getFov() {
            return fov;
        }
        
        /**
         * Sets the field of view
         */
        public void setFov(double fov) {
            this.fov = fov;
        }
        
        /**
         * Gets the near plane distance
         */
        public double getNearPlane() {
            return nearPlane;
        }
        
        /**
         * Sets the near plane distance
         */
        public void setNearPlane(double nearPlane) {
            this.nearPlane = nearPlane;
        }
        
        /**
         * Gets the far plane distance
         */
        public double getFarPlane() {
            return farPlane;
        }
        
        /**
         * Sets the far plane distance
         */
        public void setFarPlane(double farPlane) {
            this.farPlane = farPlane;
        }
        
        /**
         * Gets the aspect ratio
         */
        public double getAspectRatio() {
            return aspectRatio;
        }
        
        /**
         * Sets the aspect ratio
         */
        public void setAspectRatio(double aspectRatio) {
            this.aspectRatio = aspectRatio;
        }
    }
    
    /**
     * Represents a scene manager
     */
    public static class SceneManager {
        private final Map<String, Scene> scenes;
        private final Lock scenesLock;
        private Scene currentScene;
        
        public SceneManager() {
            this.scenes = new ConcurrentHashMap<>();
            this.scenesLock = new ReentrantLock();
            this.currentScene = null;
        }
        
        /**
         * Adds a scene
         */
        public void addScene(String name, Scene scene) {
            scenesLock.lock();
            try {
                scenes.put(name, scene);
            } finally {
                scenesLock.unlock();
            }
        }
        
        /**
         * Removes a scene
         */
        public void removeScene(String name) {
            scenesLock.lock();
            try {
                Scene scene = scenes.remove(name);
                if (scene == currentScene) {
                    currentScene = null;
                }
            } finally {
                scenesLock.unlock();
            }
        }
        
        /**
         * Sets the current scene
         */
        public void setCurrentScene(String name) {
            scenesLock.lock();
            try {
                Scene scene = scenes.get(name);
                if (scene != null) {
                    if (currentScene != null) {
                        currentScene.exit();
                    }
                    currentScene = scene;
                    currentScene.enter();
                }
            } finally {
                scenesLock.unlock();
            }
        }
        
        /**
         * Updates the current scene
         */
        public void update(double deltaTime) {
            scenesLock.lock();
            try {
                if (currentScene != null) {
                    currentScene.update(deltaTime);
                }
            } finally {
                scenesLock.unlock();
            }
        }
        
        /**
         * Renders the current scene
         */
        public void render() {
            scenesLock.lock();
            try {
                if (currentScene != null) {
                    currentScene.render();
                }
            } finally {
                scenesLock.unlock();
            }
        }
        
        /**
         * Gets the current scene
         */
        public Scene getCurrentScene() {
            scenesLock.lock();
            try {
                return currentScene;
            } finally {
                scenesLock.unlock();
            }
        }
        
        /**
         * Gets a scene by name
         */
        public Scene getScene(String name) {
            return scenes.get(name);
        }
    }
    
    /**
     * Represents a scene
     */
    public abstract static class Scene {
        protected EntityManager entityManager;
        protected List<GameSystem> systems;
        
        public Scene() {
            this.entityManager = new EntityManager();
            this.systems = new ArrayList<>();
        }
        
        /**
         * Called when entering the scene
         */
        public abstract void enter();
        
        /**
         * Called when exiting the scene
         */
        public abstract void exit();
        
        /**
         * Updates the scene
         */
        public void update(double deltaTime) {
            // Update entity manager
            // Update all systems
            for (GameSystem system : systems) {
                system.update(deltaTime);
            }
        }
        
        /**
         * Renders the scene
         */
        public abstract void render();
        
        /**
         * Gets the entity manager
         */
        public EntityManager getEntityManager() {
            return entityManager;
        }
        
        /**
         * Adds a system to the scene
         */
        public void addSystem(GameSystem system) {
            systems.add(system);
        }
        
        /**
         * Removes a system from the scene
         */
        public void removeSystem(GameSystem system) {
            systems.remove(system);
        }
    }
}