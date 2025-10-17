package com.sparky.libx.game;

import com.sparky.libx.graphics.Renderer3D;
import com.sparky.libx.graphics.Renderer3D.RenderContext;
import com.sparky.libx.math.Vector3D;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Advanced game engine with entity-component system
 * @author Андрій Будильников
 */
public class AdvancedGameEngine {
    
    /**
     * Base component class
     */
    public static abstract class Component {
        protected Entity entity;
        
        public void setEntity(Entity entity) {
            this.entity = entity;
        }
        
        public Entity getEntity() {
            return entity;
        }
        
        public abstract void update(double deltaTime);
        public abstract void render(Renderer3D.RenderContext context);
    }
    
    /**
     * Entity class
     */
    public static class Entity {
        private final UUID id;
        private String name;
        private boolean active;
        private final Map<Class<? extends Component>, Component> components;
        private final List<Entity> children;
        private Entity parent;
        
        public Entity() {
            this.id = UUID.randomUUID();
            this.name = "Entity_" + id.toString().substring(0, 8);
            this.active = true;
            this.components = new ConcurrentHashMap<>();
            this.children = new CopyOnWriteArrayList<>();
            this.parent = null;
        }
        
        public Entity(String name) {
            this();
            this.name = name;
        }
        
        public <T extends Component> void addComponent(T component) {
            component.setEntity(this);
            components.put(component.getClass(), component);
        }
        
        public <T extends Component> T getComponent(Class<T> componentClass) {
            return componentClass.cast(components.get(componentClass));
        }
        
        public <T extends Component> void removeComponent(Class<T> componentClass) {
            components.remove(componentClass);
        }
        
        public boolean hasComponent(Class<? extends Component> componentClass) {
            return components.containsKey(componentClass);
        }
        
        public Set<Class<? extends Component>> getComponentTypes() {
            return new HashSet<>(components.keySet());
        }
        
        public void addChild(Entity child) {
            if (child.parent != null) {
                child.parent.removeChild(child);
            }
            children.add(child);
            child.parent = this;
        }
        
        public void removeChild(Entity child) {
            children.remove(child);
            child.parent = null;
        }
        
        public List<Entity> getChildren() {
            return new ArrayList<>(children);
        }
        
        public Entity getParent() {
            return parent;
        }
        
        /**
         * Gets the position of the entity
         */
        public Vector3D getPosition() {
            TransformComponent transform = getComponent(TransformComponent.class);
            return transform != null ? transform.getPosition() : new Vector3D(0, 0, 0);
        }
        
        /**
         * Sets the position of the entity
         */
        public void setPosition(Vector3D position) {
            TransformComponent transform = getComponent(TransformComponent.class);
            if (transform != null) {
                transform.setPosition(position);
            }
        }
        
        // Getters and setters
        public UUID getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
    
    /**
     * Entity manager
     */
    public static class EntityManager {
        private final Map<UUID, Entity> entities;
        private final Map<Class<? extends Component>, Set<Entity>> componentIndex;
        
        public EntityManager() {
            this.entities = new ConcurrentHashMap<>();
            this.componentIndex = new ConcurrentHashMap<>();
        }
        
        public void addEntity(Entity entity) {
            entities.put(entity.getId(), entity);
            
            // Update component index
            for (Class<? extends Component> componentType : entity.getComponentTypes()) {
                componentIndex.computeIfAbsent(componentType, k -> ConcurrentHashMap.newKeySet())
                             .add(entity);
            }
        }
        
        public void removeEntity(UUID id) {
            Entity entity = entities.remove(id);
            if (entity != null) {
                // Update component index
                for (Class<? extends Component> componentType : entity.getComponentTypes()) {
                    Set<Entity> entitySet = componentIndex.get(componentType);
                    if (entitySet != null) {
                        entitySet.remove(entity);
                    }
                }
            }
        }
        
        public Entity getEntity(UUID id) {
            return entities.get(id);
        }
        
        public List<Entity> getEntitiesWithComponent(Class<? extends Component> componentClass) {
            Set<Entity> entitySet = componentIndex.get(componentClass);
            return entitySet != null ? new ArrayList<>(entitySet) : new ArrayList<>();
        }
        
        public List<Entity> getAllEntities() {
            return new ArrayList<>(entities.values());
        }
    }
    
    /**
     * Base game system class
     */
    public static abstract class GameSystem {
        protected final EntityManager entityManager;
        
        public GameSystem(EntityManager entityManager) {
            this.entityManager = entityManager;
        }
        
        public abstract void update(double deltaTime);
    }
    
    /**
     * Render component
     */
    public static class RenderComponent extends Component {
        private Renderer3D.Mesh mesh;
        private boolean visible;
        
        public RenderComponent() {
            this.mesh = null;
            this.visible = true;
        }
        
        public RenderComponent(Renderer3D.Mesh mesh) {
            this.mesh = mesh;
            this.visible = true;
        }
        
        @Override
        public void update(double deltaTime) {
            // Render component updates happen through direct property changes
        }
        
        @Override
        public void render(Renderer3D.RenderContext context) {
            if (visible && mesh != null) {
                // render the mesh using the provided context
                // this would typically involve transforming the mesh based on the entity's position
                // and then drawing it to the screen
                System.out.println("rendering mesh for entity " + entity.getId());
            }
        }
        
        // Getters and setters
        public Renderer3D.Mesh getMesh() { return mesh; }
        public void setMesh(Renderer3D.Mesh mesh) { this.mesh = mesh; }
        public boolean isVisible() { return visible; }
        public void setVisible(boolean visible) { this.visible = visible; }
    }
    
    /**
     * Physics component
     */
    public static class PhysicsComponent extends Component {
        private Vector3D velocity;
        private Vector3D acceleration;
        private double mass;
        private boolean affectedByGravity;
        
        public PhysicsComponent() {
            this.velocity = new Vector3D(0, 0, 0);
            this.acceleration = new Vector3D(0, 0, 0);
            this.mass = 1.0;
            this.affectedByGravity = true;
        }
        
        public PhysicsComponent(double mass) {
            this();
            this.mass = mass;
        }
        
        @Override
        public void update(double deltaTime) {
            // Update velocity
            velocity = velocity.add(acceleration.multiply(deltaTime));
            
            // Update position
            if (entity != null) {
                Vector3D newPosition = entity.getPosition().add(velocity.multiply(deltaTime));
                entity.setPosition(newPosition);
            }
            
            // Reset acceleration
            acceleration = new Vector3D(0, 0, 0);
        }
        
        @Override
        public void render(Renderer3D.RenderContext context) {
            // Physics component doesn't render
        }
        
        public void applyForce(Vector3D force) {
            Vector3D forceAcceleration = force.divide(mass);
            acceleration = acceleration.add(forceAcceleration);
        }
        
        // Getters and setters
        public Vector3D getVelocity() { return velocity; }
        public void setVelocity(Vector3D velocity) { this.velocity = velocity; }
        public Vector3D getAcceleration() { return acceleration; }
        public void setAcceleration(Vector3D acceleration) { this.acceleration = acceleration; }
        public double getMass() { return mass; }
        public void setMass(double mass) { this.mass = mass; }
        public boolean isAffectedByGravity() { return affectedByGravity; }
        public void setAffectedByGravity(boolean affectedByGravity) { this.affectedByGravity = affectedByGravity; }
    }
    
    /**
     * Input component
     */
    public static class InputComponent extends Component {
        private final Map<Integer, Boolean> keyStates;
        private final Map<Integer, Boolean> previousKeyStates;
        
        public InputComponent() {
            this.keyStates = new ConcurrentHashMap<>();
            this.previousKeyStates = new ConcurrentHashMap<>();
        }
        
        @Override
        public void update(double deltaTime) {
            // Copy current states to previous states
            previousKeyStates.clear();
            previousKeyStates.putAll(keyStates);
        }
        
        @Override
        public void render(Renderer3D.RenderContext context) {
            // Input component doesn't render
        }
        
        public void setKeyState(int keyCode, boolean pressed) {
            keyStates.put(keyCode, pressed);
        }
        
        public boolean isKeyPressed(int keyCode) {
            return keyStates.getOrDefault(keyCode, false);
        }
        
        public boolean isKeyJustPressed(int keyCode) {
            return keyStates.getOrDefault(keyCode, false) && 
                   !previousKeyStates.getOrDefault(keyCode, false);
        }
        
        public boolean isKeyJustReleased(int keyCode) {
            return !keyStates.getOrDefault(keyCode, false) && 
                   previousKeyStates.getOrDefault(keyCode, false);
        }
    }
    
    /**
     * Transform component
     */
    public static class TransformComponent extends Component {
        private Vector3D position;
        private Vector3D rotation;
        private Vector3D scale;
        
        public TransformComponent() {
            this.position = new Vector3D(0, 0, 0);
            this.rotation = new Vector3D(0, 0, 0);
            this.scale = new Vector3D(1, 1, 1);
        }
        
        @Override
        public void update(double deltaTime) {
            // Transform component updates happen through direct property changes
        }
        
        @Override
        public void render(Renderer3D.RenderContext context) {
            // Transform component doesn't render
        }
        
        // Getters and setters
        public Vector3D getPosition() { return position; }
        public void setPosition(Vector3D position) { this.position = position; }
        public Vector3D getRotation() { return rotation; }
        public void setRotation(Vector3D rotation) { this.rotation = rotation; }
        public Vector3D getScale() { return scale; }
        public void setScale(Vector3D scale) { this.scale = scale; }
    }
    
    /**
     * Render context for passing rendering information
     */
    
    /**
     * Render system
     */
    public static class RenderingSystem extends GameSystem {
        public RenderingSystem(EntityManager entityManager) {
            super(entityManager);
        }
        
        @Override
        public void update(double deltaTime) {
            // render all visible entities
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
            // perform actual rendering by calling the component's render method
            RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
            if (renderComponent != null) {
                // Create a proper render context with entity position and camera
                TransformComponent transformComponent = entity.getComponent(TransformComponent.class);
                Vector3D entityPosition = transformComponent != null ? transformComponent.getPosition() : new Vector3D(0, 0, 0);
                
                // Create a simple render context
                // For now, just call render with null context since we don't have a proper rendering setup
                renderComponent.render(null);
            }
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
         * Replaces the current game state with a new one
         */
        public void changeState(GameState state) {
            stackLock.lock();
            try {
                if (!stateStack.isEmpty()) {
                    GameState currentState = stateStack.pop();
                    currentState.exit();
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
        public void render(Renderer3D.RenderContext context) {
            stackLock.lock();
            try {
                if (!stateStack.isEmpty()) {
                    stateStack.peek().render(context);
                }
            } finally {
                stackLock.unlock();
            }
        }
        
        /**
         * Handles input for the current game state
         */
        public void handleInput(int keyCode, boolean pressed) {
            stackLock.lock();
            try {
                if (!stateStack.isEmpty()) {
                    stateStack.peek().handleInput(keyCode, pressed);
                }
            } finally {
                stackLock.unlock();
            }
        }
    }
    
    /**
     * Represents a game state
     */
    public static abstract class GameState {
        public abstract void enter();
        public abstract void exit();
        public abstract void update(double deltaTime);
        public abstract void render(Renderer3D.RenderContext context);
        public abstract void handleInput(int keyCode, boolean pressed);
        public abstract void pause();
        public abstract void resume();
    }
    
    /**
     * Represents a resource manager
     */
    public static class ResourceManager {
        private final Map<String, Object> resources;
        private final Lock resourceLock;
        
        public ResourceManager() {
            this.resources = new ConcurrentHashMap<>();
            this.resourceLock = new ReentrantLock();
        }
        
        /**
         * Loads a resource
         */
        public <T> void loadResource(String name, T resource) {
            resourceLock.lock();
            try {
                resources.put(name, resource);
            } finally {
                resourceLock.unlock();
            }
        }
        
        /**
         * Gets a resource
         */
        @SuppressWarnings("unchecked")
        public <T> T getResource(String name, Class<T> type) {
            resourceLock.lock();
            try {
                Object resource = resources.get(name);
                if (type.isInstance(resource)) {
                    return (T) resource;
                }
                return null;
            } finally {
                resourceLock.unlock();
            }
        }
        
        /**
         * Unloads a resource
         */
        public void unloadResource(String name) {
            resourceLock.lock();
            try {
                resources.remove(name);
            } finally {
                resourceLock.unlock();
            }
        }
        
        /**
         * Checks if a resource is loaded
         */
        public boolean isResourceLoaded(String name) {
            resourceLock.lock();
            try {
                return resources.containsKey(name);
            } finally {
                resourceLock.unlock();
            }
        }
    }
}