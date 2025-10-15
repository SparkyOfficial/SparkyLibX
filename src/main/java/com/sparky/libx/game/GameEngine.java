package com.sparky.libx.game;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.graphics.Renderer3D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Advanced Game Engine for Minecraft Plugins
 * Provides capabilities for game entity management, physics, and game logic
 */
public class GameEngine {
    
    /**
     * Represents a game entity with position, rotation, and components
     */
    public static class Entity {
        private final UUID id;
        private Vector3D position;
        private Vector3D rotation;
        private Vector3D scale;
        private final Map<String, Object> components;
        private boolean active;
        private Entity parent;
        private final List<Entity> children;
        
        public Entity() {
            this.id = UUID.randomUUID();
            this.position = new Vector3D(0, 0, 0);
            this.rotation = new Vector3D(0, 0, 0);
            this.scale = new Vector3D(1, 1, 1);
            this.components = new HashMap<>();
            this.active = true;
            this.children = new CopyOnWriteArrayList<>();
        }
        
        public Entity(Vector3D position) {
            this();
            this.position = position;
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
        
        public Vector3D getRotation() {
            return rotation;
        }
        
        public void setRotation(Vector3D rotation) {
            this.rotation = rotation;
        }
        
        public Vector3D getScale() {
            return scale;
        }
        
        public void setScale(Vector3D scale) {
            this.scale = scale;
        }
        
        public boolean isActive() {
            return active;
        }
        
        public void setActive(boolean active) {
            this.active = active;
        }
        
        public Entity getParent() {
            return parent;
        }
        
        public List<Entity> getChildren() {
            return new ArrayList<>(children);
        }
        
        public void setParent(Entity parent) {
            if (this.parent != null) {
                this.parent.children.remove(this);
            }
            this.parent = parent;
            if (parent != null) {
                parent.children.add(this);
            }
        }
        
        public void addChild(Entity child) {
            child.setParent(this);
        }
        
        public void removeChild(Entity child) {
            if (children.contains(child)) {
                child.setParent(null);
            }
        }
        
        public void addComponent(String name, Object component) {
            components.put(name, component);
        }
        
        public <T> T getComponent(String name, Class<T> type) {
            Object component = components.get(name);
            if (type.isInstance(component)) {
                return type.cast(component);
            }
            return null;
        }
        
        public void removeComponent(String name) {
            components.remove(name);
        }
        
        public Set<String> getComponentNames() {
            return new HashSet<>(components.keySet());
        }
        
        public Vector3D getWorldPosition() {
            if (parent != null) {
                return parent.getWorldPosition().add(position);
            }
            return position;
        }
        
        public Vector3D getWorldRotation() {
            if (parent != null) {
                return parent.getWorldRotation().add(rotation);
            }
            return rotation;
        }
        
        @Override
        public String toString() {
            return String.format("Entity{id=%s, pos=%s, rot=%s, components=%d}", 
                id, position, rotation, components.size());
        }
    }
    
    /**
     * Component interface for game entities
     */
    public interface Component {
        void update(double deltaTime);
        void render(Renderer3D.RenderContext context);
    }
    
    /**
     * Transform component for entity positioning
     */
    public static class TransformComponent implements Component {
        private Vector3D position;
        private Vector3D rotation;
        private Vector3D scale;
        
        public TransformComponent() {
            this.position = new Vector3D(0, 0, 0);
            this.rotation = new Vector3D(0, 0, 0);
            this.scale = new Vector3D(1, 1, 1);
        }
        
        public TransformComponent(Vector3D position, Vector3D rotation, Vector3D scale) {
            this.position = position;
            this.rotation = rotation;
            this.scale = scale;
        }
        
        public Vector3D getPosition() {
            return position;
        }
        
        public void setPosition(Vector3D position) {
            this.position = position;
        }
        
        public Vector3D getRotation() {
            return rotation;
        }
        
        public void setRotation(Vector3D rotation) {
            this.rotation = rotation;
        }
        
        public Vector3D getScale() {
            return scale;
        }
        
        public void setScale(Vector3D scale) {
            this.scale = scale;
        }
        
        @Override
        public void update(double deltaTime) {
            // Transform updates happen through direct property changes
        }
        
        @Override
        public void render(Renderer3D.RenderContext context) {
            // Transform affects rendering but doesn't render itself
        }
    }
    
    /**
     * Render component for visual representation
     */
    public static class RenderComponent implements Component {
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
        
        public Renderer3D.Mesh getMesh() {
            return mesh;
        }
        
        public void setMesh(Renderer3D.Mesh mesh) {
            this.mesh = mesh;
        }
        
        public boolean isVisible() {
            return visible;
        }
        
        public void setVisible(boolean visible) {
            this.visible = visible;
        }
        
        @Override
        public void update(double deltaTime) {
            // Render component updates happen through direct property changes
        }
        
        @Override
        public void render(Renderer3D.RenderContext context) {
            if (visible && mesh != null) {
                // In a real implementation, this would render the mesh
            }
        }
    }
    
    /**
     * Physics component for physical behavior
     */
    public static class PhysicsComponent implements Component {
        private Vector3D velocity;
        private Vector3D acceleration;
        private double mass;
        private boolean affectedByGravity;
        private boolean kinematic;
        
        public PhysicsComponent() {
            this.velocity = new Vector3D(0, 0, 0);
            this.acceleration = new Vector3D(0, 0, 0);
            this.mass = 1.0;
            this.affectedByGravity = true;
            this.kinematic = false;
        }
        
        public PhysicsComponent(double mass) {
            this();
            this.mass = mass;
        }
        
        public Vector3D getVelocity() {
            return velocity;
        }
        
        public void setVelocity(Vector3D velocity) {
            this.velocity = velocity;
        }
        
        public Vector3D getAcceleration() {
            return acceleration;
        }
        
        public void setAcceleration(Vector3D acceleration) {
            this.acceleration = acceleration;
        }
        
        public double getMass() {
            return mass;
        }
        
        public void setMass(double mass) {
            this.mass = mass;
        }
        
        public boolean isAffectedByGravity() {
            return affectedByGravity;
        }
        
        public void setAffectedByGravity(boolean affectedByGravity) {
            this.affectedByGravity = affectedByGravity;
        }
        
        public boolean isKinematic() {
            return kinematic;
        }
        
        public void setKinematic(boolean kinematic) {
            this.kinematic = kinematic;
        }
        
        public void applyForce(Vector3D force) {
            if (!kinematic) {
                Vector3D forceAcceleration = force.divide(mass);
                acceleration = acceleration.add(forceAcceleration);
            }
        }
        
        public void applyImpulse(Vector3D impulse) {
            if (!kinematic) {
                velocity = velocity.add(impulse.divide(mass));
            }
        }
        
        @Override
        public void update(double deltaTime) {
            if (!kinematic) {
                // Update velocity
                velocity = velocity.add(acceleration.multiply(deltaTime));
                
                // Reset acceleration
                acceleration = new Vector3D(0, 0, 0);
            }
        }
        
        @Override
        public void render(Renderer3D.RenderContext context) {
            // Physics component doesn't render
        }
    }
    
    /**
     * Collision component for collision detection
     */
    public static class CollisionComponent implements Component {
        public enum Shape {
            SPHERE, BOX, MESH
        }
        
        private Shape shape;
        private double radius;
        private Vector3D size;
        private Renderer3D.Mesh mesh;
        private boolean trigger;
        private final List<Collision> collisions;
        
        public CollisionComponent() {
            this.shape = Shape.SPHERE;
            this.radius = 1.0;
            this.size = new Vector3D(1, 1, 1);
            this.mesh = null;
            this.trigger = false;
            this.collisions = new ArrayList<>();
        }
        
        public CollisionComponent(Shape shape, double radius) {
            this();
            this.shape = shape;
            this.radius = radius;
        }
        
        public CollisionComponent(Shape shape, Vector3D size) {
            this();
            this.shape = shape;
            this.size = size;
        }
        
        public Shape getShape() {
            return shape;
        }
        
        public void setShape(Shape shape) {
            this.shape = shape;
        }
        
        public double getRadius() {
            return radius;
        }
        
        public void setRadius(double radius) {
            this.radius = radius;
        }
        
        public Vector3D getSize() {
            return size;
        }
        
        public void setSize(Vector3D size) {
            this.size = size;
        }
        
        public Renderer3D.Mesh getMesh() {
            return mesh;
        }
        
        public void setMesh(Renderer3D.Mesh mesh) {
            this.mesh = mesh;
        }
        
        public boolean isTrigger() {
            return trigger;
        }
        
        public void setTrigger(boolean trigger) {
            this.trigger = trigger;
        }
        
        public List<Collision> getCollisions() {
            return new ArrayList<>(collisions);
        }
        
        public void addCollision(Collision collision) {
            collisions.add(collision);
        }
        
        public void clearCollisions() {
            collisions.clear();
        }
        
        @Override
        public void update(double deltaTime) {
            // Collision detection would happen in the physics system
        }
        
        @Override
        public void render(Renderer3D.RenderContext context) {
            // Collision component doesn't render visually by default
        }
    }
    
    /**
     * Represents a collision between two entities
     */
    public static class Collision {
        private final Entity entityA;
        private final Entity entityB;
        private final Vector3D contactPoint;
        private final Vector3D normal;
        private final double penetration;
        
        public Collision(Entity entityA, Entity entityB, Vector3D contactPoint, Vector3D normal, double penetration) {
            this.entityA = entityA;
            this.entityB = entityB;
            this.contactPoint = contactPoint;
            this.normal = normal;
            this.penetration = penetration;
        }
        
        public Entity getEntityA() {
            return entityA;
        }
        
        public Entity getEntityB() {
            return entityB;
        }
        
        public Vector3D getContactPoint() {
            return contactPoint;
        }
        
        public Vector3D getNormal() {
            return normal;
        }
        
        public double getPenetration() {
            return penetration;
        }
    }
    
    /**
     * Input component for handling user input
     */
    public static class InputComponent implements Component {
        private final Map<String, Boolean> keys;
        private final Map<Integer, Boolean> mouseButtons;
        private Vector3D mousePosition;
        private Vector3D mouseDelta;
        
        public InputComponent() {
            this.keys = new ConcurrentHashMap<>();
            this.mouseButtons = new ConcurrentHashMap<>();
            this.mousePosition = new Vector3D(0, 0, 0);
            this.mouseDelta = new Vector3D(0, 0, 0);
        }
        
        public boolean isKeyDown(String key) {
            return keys.getOrDefault(key, false);
        }
        
        public void setKeyDown(String key, boolean down) {
            keys.put(key, down);
        }
        
        public boolean isMouseButtonDown(int button) {
            return mouseButtons.getOrDefault(button, false);
        }
        
        public void setMouseButtonDown(int button, boolean down) {
            mouseButtons.put(button, down);
        }
        
        public Vector3D getMousePosition() {
            return mousePosition;
        }
        
        public void setMousePosition(Vector3D mousePosition) {
            this.mousePosition = mousePosition;
        }
        
        public Vector3D getMouseDelta() {
            return mouseDelta;
        }
        
        public void setMouseDelta(Vector3D mouseDelta) {
            this.mouseDelta = mouseDelta;
        }
        
        @Override
        public void update(double deltaTime) {
            // Input updates happen through event handlers
        }
        
        @Override
        public void render(Renderer3D.RenderContext context) {
            // Input component doesn't render
        }
    }
    
    /**
     * Game state manager
     */
    public static class GameStateManager {
        private final Map<String, GameState> states;
        private GameState currentState;
        private GameState previousState;
        
        public GameStateManager() {
            this.states = new HashMap<>();
            this.currentState = null;
            this.previousState = null;
        }
        
        public void addState(String name, GameState state) {
            states.put(name, state);
        }
        
        public GameState getState(String name) {
            return states.get(name);
        }
        
        public void setState(String name) {
            GameState newState = states.get(name);
            if (newState != null) {
                if (currentState != null) {
                    currentState.onExit();
                }
                previousState = currentState;
                currentState = newState;
                currentState.onEnter();
            }
        }
        
        public void pushState(String name) {
            GameState newState = states.get(name);
            if (newState != null) {
                if (currentState != null) {
                    currentState.onPause();
                }
                previousState = currentState;
                currentState = newState;
                currentState.onEnter();
            }
        }
        
        public void popState() {
            if (currentState != null) {
                currentState.onExit();
            }
            currentState = previousState;
            previousState = null;
            if (currentState != null) {
                currentState.onResume();
            }
        }
        
        public GameState getCurrentState() {
            return currentState;
        }
        
        public void update(double deltaTime) {
            if (currentState != null) {
                currentState.onUpdate(deltaTime);
            }
        }
        
        public void render(Renderer3D.RenderContext context) {
            if (currentState != null) {
                currentState.onRender(context);
            }
        }
    }
    
    /**
     * Game state interface
     */
    public interface GameState {
        void onEnter();
        void onExit();
        void onPause();
        void onResume();
        void onUpdate(double deltaTime);
        void onRender(Renderer3D.RenderContext context);
    }
    
    /**
     * Scene graph for organizing game entities
     */
    public static class SceneGraph {
        private final Entity root;
        private final Map<UUID, Entity> entityMap;
        
        public SceneGraph() {
            this.root = new Entity(new Vector3D(0, 0, 0));
            this.entityMap = new ConcurrentHashMap<>();
            this.entityMap.put(root.getId(), root);
        }
        
        public Entity getRoot() {
            return root;
        }
        
        public void addEntity(Entity entity) {
            root.addChild(entity);
            entityMap.put(entity.getId(), entity);
        }
        
        public void addEntity(Entity parent, Entity entity) {
            parent.addChild(entity);
            entityMap.put(entity.getId(), entity);
        }
        
        public void removeEntity(Entity entity) {
            if (entity.getParent() != null) {
                entity.getParent().removeChild(entity);
            }
            entityMap.remove(entity.getId());
        }
        
        public Entity findEntity(UUID id) {
            return entityMap.get(id);
        }
        
        public List<Entity> findEntitiesByName(String name) {
            List<Entity> result = new ArrayList<>();
            // In a real implementation, entities would have names
            // This is a placeholder for the search functionality
            return result;
        }
        
        public void update(double deltaTime) {
            updateEntity(root, deltaTime);
        }
        
        private void updateEntity(Entity entity, double deltaTime) {
            if (entity.isActive()) {
                // Update all components
                for (String componentName : entity.getComponentNames()) {
                    Object component = entity.getComponent(componentName, Object.class);
                    if (component instanceof Component) {
                        ((Component) component).update(deltaTime);
                    }
                }
                
                // Update children
                for (Entity child : entity.getChildren()) {
                    updateEntity(child, deltaTime);
                }
            }
        }
        
        public void render(Renderer3D.RenderContext context) {
            renderEntity(root, context);
        }
        
        private void renderEntity(Entity entity, Renderer3D.RenderContext context) {
            if (entity.isActive()) {
                // Render all components
                for (String componentName : entity.getComponentNames()) {
                    Object component = entity.getComponent(componentName, Object.class);
                    if (component instanceof Component) {
                        ((Component) component).render(context);
                    }
                }
                
                // Render children
                for (Entity child : entity.getChildren()) {
                    renderEntity(child, context);
                }
            }
        }
    }
    
    /**
     * Timer for game events and cooldowns
     */
    public static class Timer {
        private double currentTime;
        private final Map<String, TimerData> timers;
        
        public Timer() {
            this.currentTime = 0;
            this.timers = new HashMap<>();
        }
        
        public void update(double deltaTime) {
            currentTime += deltaTime;
            
            // Update all timers
            Iterator<Map.Entry<String, TimerData>> iterator = timers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, TimerData> entry = iterator.next();
                TimerData timerData = entry.getValue();
                
                if (timerData.isExpired(currentTime)) {
                    if (timerData.callback != null) {
                        timerData.callback.run();
                    }
                    
                    if (timerData.repeat) {
                        timerData.reset(currentTime);
                    } else {
                        iterator.remove();
                    }
                }
            }
        }
        
        public void setTimer(String name, double duration, boolean repeat, Runnable callback) {
            timers.put(name, new TimerData(duration, repeat, callback, currentTime));
        }
        
        public void cancelTimer(String name) {
            timers.remove(name);
        }
        
        public boolean isTimerActive(String name) {
            return timers.containsKey(name);
        }
        
        public double getTimerRemaining(String name) {
            TimerData timer = timers.get(name);
            if (timer != null) {
                return timer.getRemainingTime(currentTime);
            }
            return 0;
        }
        
        private static class TimerData {
            private final double duration;
            private final boolean repeat;
            private final Runnable callback;
            private double endTime;
            
            public TimerData(double duration, boolean repeat, Runnable callback, double currentTime) {
                this.duration = duration;
                this.repeat = repeat;
                this.callback = callback;
                this.endTime = currentTime + duration;
            }
            
            public boolean isExpired(double currentTime) {
                return currentTime >= endTime;
            }
            
            public double getRemainingTime(double currentTime) {
                return Math.max(0, endTime - currentTime);
            }
            
            public void reset(double currentTime) {
                endTime = currentTime + duration;
            }
        }
    }
    
    /**
     * Particle system for special effects
     */
    public static class ParticleSystem {
        private final List<Particle> particles;
        private final ParticleEmitter emitter;
        private boolean active;
        
        public ParticleSystem(ParticleEmitter emitter) {
            this.particles = new CopyOnWriteArrayList<>();
            this.emitter = emitter;
            this.active = true;
        }
        
        public void update(double deltaTime) {
            if (active) {
                // Update existing particles
                Iterator<Particle> iterator = particles.iterator();
                while (iterator.hasNext()) {
                    Particle particle = iterator.next();
                    particle.update(deltaTime);
                    
                    if (particle.isDead()) {
                        iterator.remove();
                    }
                }
                
                // Emit new particles
                if (emitter != null) {
                    List<Particle> newParticles = emitter.emit(deltaTime);
                    particles.addAll(newParticles);
                }
            }
        }
        
        public void render(Renderer3D.RenderContext context) {
            for (Particle particle : particles) {
                particle.render(context);
            }
        }
        
        public void setActive(boolean active) {
            this.active = active;
        }
        
        public boolean isActive() {
            return active;
        }
        
        public int getParticleCount() {
            return particles.size();
        }
    }
    
    /**
     * Particle for particle systems
     */
    public static class Particle {
        private Vector3D position;
        private Vector3D velocity;
        private Vector3D acceleration;
        private Vector3D color;
        private double size;
        private double lifeTime;
        private double maxLifeTime;
        private boolean alive;
        
        public Particle(Vector3D position, Vector3D velocity, Vector3D color, double size, double lifeTime) {
            this.position = position;
            this.velocity = velocity;
            this.acceleration = new Vector3D(0, 0, 0);
            this.color = color;
            this.size = size;
            this.lifeTime = lifeTime;
            this.maxLifeTime = lifeTime;
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
            }
        }
        
        public void render(Renderer3D.RenderContext context) {
            if (alive) {
                // In a real implementation, this would render the particle
            }
        }
        
        public boolean isDead() {
            return !alive || lifeTime <= 0;
        }
        
        public Vector3D getPosition() {
            return position;
        }
        
        public Vector3D getColor() {
            return color;
        }
        
        public double getSize() {
            return size;
        }
        
        public double getLifeTime() {
            return lifeTime;
        }
        
        public double getLifeTimeRatio() {
            return lifeTime / maxLifeTime;
        }
    }
    
    /**
     * Particle emitter interface
     */
    public interface ParticleEmitter {
        List<Particle> emit(double deltaTime);
    }
    
    /**
     * Box particle emitter
     */
    public static class BoxEmitter implements ParticleEmitter {
        private Vector3D position;
        private Vector3D size;
        private Vector3D velocityMin;
        private Vector3D velocityMax;
        private Vector3D color;
        private double sizeMin;
        private double sizeMax;
        private double lifeTimeMin;
        private double lifeTimeMax;
        private double emissionRate;
        private double accumulatedTime;
        
        public BoxEmitter() {
            this.position = new Vector3D(0, 0, 0);
            this.size = new Vector3D(1, 1, 1);
            this.velocityMin = new Vector3D(-1, -1, -1);
            this.velocityMax = new Vector3D(1, 1, 1);
            this.color = new Vector3D(1, 1, 1);
            this.sizeMin = 0.1;
            this.sizeMax = 0.5;
            this.lifeTimeMin = 1.0;
            this.lifeTimeMax = 3.0;
            this.emissionRate = 10.0; // particles per second
            this.accumulatedTime = 0;
        }
        
        @Override
        public List<Particle> emit(double deltaTime) {
            List<Particle> newParticles = new ArrayList<>();
            
            accumulatedTime += deltaTime;
            double particlesToEmit = accumulatedTime * emissionRate;
            int particleCount = (int) particlesToEmit;
            accumulatedTime -= particleCount / emissionRate;
            
            Random random = new Random();
            
            for (int i = 0; i < particleCount; i++) {
                // Random position within box
                double x = position.getX() + (random.nextDouble() - 0.5) * size.getX();
                double y = position.getY() + (random.nextDouble() - 0.5) * size.getY();
                double z = position.getZ() + (random.nextDouble() - 0.5) * size.getZ();
                Vector3D particlePosition = new Vector3D(x, y, z);
                
                // Random velocity
                double vx = velocityMin.getX() + random.nextDouble() * (velocityMax.getX() - velocityMin.getX());
                double vy = velocityMin.getY() + random.nextDouble() * (velocityMax.getY() - velocityMin.getY());
                double vz = velocityMin.getZ() + random.nextDouble() * (velocityMax.getZ() - velocityMin.getZ());
                Vector3D particleVelocity = new Vector3D(vx, vy, vz);
                
                // Random size
                double particleSize = sizeMin + random.nextDouble() * (sizeMax - sizeMin);
                
                // Random lifetime
                double particleLifeTime = lifeTimeMin + random.nextDouble() * (lifeTimeMax - lifeTimeMin);
                
                Particle particle = new Particle(particlePosition, particleVelocity, color, particleSize, particleLifeTime);
                newParticles.add(particle);
            }
            
            return newParticles;
        }
        
        public void setPosition(Vector3D position) {
            this.position = position;
        }
        
        public void setSize(Vector3D size) {
            this.size = size;
        }
        
        public void setVelocityRange(Vector3D min, Vector3D max) {
            this.velocityMin = min;
            this.velocityMax = max;
        }
        
        public void setColor(Vector3D color) {
            this.color = color;
        }
        
        public void setSizeRange(double min, double max) {
            this.sizeMin = min;
            this.sizeMax = max;
        }
        
        public void setLifeTimeRange(double min, double max) {
            this.lifeTimeMin = min;
            this.lifeTimeMax = max;
        }
        
        public void setEmissionRate(double rate) {
            this.emissionRate = rate;
        }
    }
}