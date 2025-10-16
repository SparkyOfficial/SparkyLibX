package com.sparky.libx.physics;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.math.Matrix4x4;
import com.sparky.libx.math.Quaternion;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Computational Physics Framework for Minecraft Plugins
 * Provides capabilities for advanced physics simulations, rigid body dynamics, and complex physical systems
 * 
 * @author Андрій Будильников
 */
public class ComputationalPhysics {
    
    /**
     * Represents a rigid body in the physics simulation
     */
    public static class RigidBody {
        private int id;
        private Vector3D position;
        private Quaternion orientation;
        private Vector3D linearVelocity;
        private Vector3D angularVelocity;
        private Vector3D linearAcceleration;
        private Vector3D angularAcceleration;
        private double mass;
        private Matrix4x4 inertiaTensor;
        private Matrix4x4 inverseInertiaTensor;
        private Vector3D force;
        private Vector3D torque;
        private Shape shape;
        private Material material;
        private boolean isStatic;
        private boolean isEnabled;
        
        public RigidBody(int id, Shape shape, Material material, double mass) {
            this.id = id;
            this.position = new Vector3D(0, 0, 0);
            this.orientation = Quaternion.identity();
            this.linearVelocity = new Vector3D(0, 0, 0);
            this.angularVelocity = new Vector3D(0, 0, 0);
            this.linearAcceleration = new Vector3D(0, 0, 0);
            this.angularAcceleration = new Vector3D(0, 0, 0);
            this.mass = mass;
            this.shape = shape;
            this.material = material;
            this.isStatic = false;
            this.isEnabled = true;
            this.force = new Vector3D(0, 0, 0);
            this.torque = new Vector3D(0, 0, 0);
            
            // Calculate inertia tensor based on shape
            calculateInertiaTensor();
        }
        
        /**
         * Calculates the inertia tensor for this rigid body based on its shape
         */
        private void calculateInertiaTensor() {
            if (shape instanceof Sphere) {
                Sphere sphere = (Sphere) shape;
                double i = 0.4 * mass * sphere.radius * sphere.radius;
                inertiaTensor = Matrix4x4.createScale(i, i, i);
            } else if (shape instanceof Box) {
                Box box = (Box) shape;
                double dx = box.size.getX() * box.size.getX();
                double dy = box.size.getY() * box.size.getY();
                double dz = box.size.getZ() * box.size.getZ();
                double ixx = mass * (dy + dz) / 12.0;
                double iyy = mass * (dx + dz) / 12.0;
                double izz = mass * (dx + dy) / 12.0;
                inertiaTensor = new Matrix4x4();
                inertiaTensor.set(0, 0, ixx);
                inertiaTensor.set(1, 1, iyy);
                inertiaTensor.set(2, 2, izz);
            } else {
                // Default to spherical approximation
                double i = 0.4 * mass * 1.0 * 1.0;
                inertiaTensor = Matrix4x4.createScale(i, i, i);
            }
            
            // Calculate inverse inertia tensor in world space
            Matrix4x4 rotationMatrix = quaternionToMatrix(orientation);
            Matrix4x4 transposedRotation = rotationMatrix.transpose();
            inverseInertiaTensor = rotationMatrix.multiply(inertiaTensor).multiply(transposedRotation);
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
         * Integrates the rigid body's motion using Verlet integration
         */
        public void integrate(double deltaTime) {
            if (isStatic || !isEnabled) {
                return;
            }
            
            // Update linear motion
            linearAcceleration = force.multiply(1.0 / mass);
            linearVelocity = linearVelocity.add(linearAcceleration.multiply(deltaTime));
            position = position.add(linearVelocity.multiply(deltaTime));
            
            // Update angular motion
            angularAcceleration = multiplyMatrixByVector3D(inverseInertiaTensor, torque);
            angularVelocity = angularVelocity.add(angularAcceleration.multiply(deltaTime));
            orientation = integrateQuaternion(orientation, angularVelocity, deltaTime);
            
            // Reset forces and torques
            force = new Vector3D(0, 0, 0);
            torque = new Vector3D(0, 0, 0);
            
            // Recalculate inertia tensor after orientation change
            calculateInertiaTensor();
        }
        
        /**
         * Integrates a quaternion using angular velocity
         */
        private Quaternion integrateQuaternion(Quaternion q, Vector3D angularVelocity, double deltaTime) {
            // Convert angular velocity to quaternion derivative
            Quaternion omega = new Quaternion(0, angularVelocity.getX(), angularVelocity.getY(), angularVelocity.getZ());
            Quaternion qDot = omega.multiply(q).multiply(0.5);
            
            // Integrate using Euler method
            Quaternion newQ = q.add(qDot.multiply(deltaTime));
            return newQ.normalize();
        }
        
        /**
         * Applies a force to the rigid body at its center of mass
         */
        public void applyForce(Vector3D force) {
            if (isStatic || !isEnabled) {
                return;
            }
            this.force = this.force.add(force);
        }
        
        /**
         * Applies a force to the rigid body at a specific point
         */
        public void applyForceAtPoint(Vector3D force, Vector3D point) {
            if (isStatic || !isEnabled) {
                return;
            }
            
            this.force = this.force.add(force);
            
            // Calculate torque: τ = r × F
            Vector3D r = point.subtract(position);
            Vector3D torque = r.cross(force);
            this.torque = this.torque.add(torque);
        }
        
        /**
         * Applies torque to the rigid body
         */
        public void applyTorque(Vector3D torque) {
            if (isStatic || !isEnabled) {
                return;
            }
            this.torque = this.torque.add(torque);
        }
        
        /**
         * Gets the transformation matrix for this rigid body
         */
        public Matrix4x4 getTransformMatrix() {
            Matrix4x4 translationMatrix = Matrix4x4.createTranslation(position.getX(), position.getY(), position.getZ());
            Matrix4x4 rotationMatrix = quaternionToMatrix(orientation);
            return translationMatrix.multiply(rotationMatrix);
        }
        
        public int getId() {
            return id;
        }
        
        public Vector3D getPosition() {
            return position;
        }
        
        public void setPosition(Vector3D position) {
            this.position = position;
        }
        
        public Quaternion getOrientation() {
            return orientation;
        }
        
        public void setOrientation(Quaternion orientation) {
            this.orientation = orientation;
            calculateInertiaTensor();
        }
        
        public Vector3D getLinearVelocity() {
            return linearVelocity;
        }
        
        public void setLinearVelocity(Vector3D linearVelocity) {
            this.linearVelocity = linearVelocity;
        }
        
        public Vector3D getAngularVelocity() {
            return angularVelocity;
        }
        
        public void setAngularVelocity(Vector3D angularVelocity) {
            this.angularVelocity = angularVelocity;
        }
        
        public double getMass() {
            return mass;
        }
        
        public void setMass(double mass) {
            this.mass = mass;
            calculateInertiaTensor();
        }
        
        public Shape getShape() {
            return shape;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public boolean isStatic() {
            return isStatic;
        }
        
        public void setStatic(boolean isStatic) {
            this.isStatic = isStatic;
        }
        
        public boolean isEnabled() {
            return isEnabled;
        }
        
        public void setEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }
    }
    
    /**
     * Represents a shape for collision detection
     */
    public abstract static class Shape {
        protected Vector3D center;
        
        public Shape(Vector3D center) {
            this.center = center;
        }
        
        public abstract boolean intersects(Shape other);
        public abstract Vector3D getSupport(Vector3D direction);
        public abstract double getVolume();
        
        public Vector3D getCenter() {
            return center;
        }
        
        public void setCenter(Vector3D center) {
            this.center = center;
        }
    }
    
    /**
     * Represents a sphere shape
     */
    public static class Sphere extends Shape {
        public double radius;
        
        public Sphere(Vector3D center, double radius) {
            super(center);
            this.radius = radius;
        }
        
        @Override
        public boolean intersects(Shape other) {
            if (other instanceof Sphere) {
                Sphere otherSphere = (Sphere) other;
                double distance = center.distance(otherSphere.center);
                return distance <= (radius + otherSphere.radius);
            }
            return false;
        }
        
        @Override
        public Vector3D getSupport(Vector3D direction) {
            if (direction.magnitude() == 0) {
                return center.add(new Vector3D(radius, 0, 0));
            }
            return center.add(direction.normalize().multiply(radius));
        }
        
        @Override
        public double getVolume() {
            return (4.0 / 3.0) * Math.PI * radius * radius * radius;
        }
        
        public double getRadius() {
            return radius;
        }
        
        public void setRadius(double radius) {
            this.radius = radius;
        }
    }
    
    /**
     * Represents a box shape
     */
    public static class Box extends Shape {
        public Vector3D size;
        
        public Box(Vector3D center, Vector3D size) {
            super(center);
            this.size = size;
        }
        
        @Override
        public boolean intersects(Shape other) {
            if (other instanceof Box) {
                Box otherBox = (Box) other;
                Vector3D halfSize = size.multiply(0.5);
                Vector3D otherHalfSize = otherBox.size.multiply(0.5);
                
                return Math.abs(center.getX() - otherBox.center.getX()) < (halfSize.getX() + otherHalfSize.getX()) &&
                       Math.abs(center.getY() - otherBox.center.getY()) < (halfSize.getY() + otherHalfSize.getY()) &&
                       Math.abs(center.getZ() - otherBox.center.getZ()) < (halfSize.getZ() + otherHalfSize.getZ());
            } else if (other instanceof Sphere) {
                Sphere sphere = (Sphere) other;
                Vector3D halfSize = size.multiply(0.5);
                Vector3D closestPoint = new Vector3D(
                    Math.max(center.getX() - halfSize.getX(), Math.min(sphere.center.getX(), center.getX() + halfSize.getX())),
                    Math.max(center.getY() - halfSize.getY(), Math.min(sphere.center.getY(), center.getY() + halfSize.getY())),
                    Math.max(center.getZ() - halfSize.getZ(), Math.min(sphere.center.getZ(), center.getZ() + halfSize.getZ()))
                );
                return closestPoint.distance(sphere.center) <= sphere.radius;
            }
            return false;
        }
        
        @Override
        public Vector3D getSupport(Vector3D direction) {
            Vector3D halfSize = size.multiply(0.5);
            return center.add(new Vector3D(
                direction.getX() >= 0 ? halfSize.getX() : -halfSize.getX(),
                direction.getY() >= 0 ? halfSize.getY() : -halfSize.getY(),
                direction.getZ() >= 0 ? halfSize.getZ() : -halfSize.getZ()
            ));
        }
        
        @Override
        public double getVolume() {
            return size.getX() * size.getY() * size.getZ();
        }
        
        public Vector3D getSize() {
            return size;
        }
        
        public void setSize(Vector3D size) {
            this.size = size;
        }
    }
    
    /**
     * Represents material properties for physics simulation
     */
    public static class Material {
        private double density;
        private double restitution; // Bounciness (0.0 to 1.0)
        private double friction;    // Friction coefficient
        
        public Material(double density, double restitution, double friction) {
            this.density = density;
            this.restitution = restitution;
            this.friction = friction;
        }
        
        public double getDensity() {
            return density;
        }
        
        public void setDensity(double density) {
            this.density = density;
        }
        
        public double getRestitution() {
            return restitution;
        }
        
        public void setRestitution(double restitution) {
            this.restitution = restitution;
        }
        
        public double getFriction() {
            return friction;
        }
        
        public void setFriction(double friction) {
            this.friction = friction;
        }
    }
    
    /**
     * Represents a constraint between two rigid bodies
     */
    public abstract static class Constraint {
        protected RigidBody bodyA;
        protected RigidBody bodyB;
        protected double biasFactor;
        protected double relaxation;
        
        public Constraint(RigidBody bodyA, RigidBody bodyB) {
            this.bodyA = bodyA;
            this.bodyB = bodyB;
            this.biasFactor = 0.2;
            this.relaxation = 0.8;
        }
        
        public abstract void solve(double deltaTime);
        
        public RigidBody getBodyA() {
            return bodyA;
        }
        
        public RigidBody getBodyB() {
            return bodyB;
        }
        
        public double getBiasFactor() {
            return biasFactor;
        }
        
        public void setBiasFactor(double biasFactor) {
            this.biasFactor = biasFactor;
        }
        
        public double getRelaxation() {
            return relaxation;
        }
        
        public void setRelaxation(double relaxation) {
            this.relaxation = relaxation;
        }
    }
    
    /**
     * Represents a distance constraint between two points on rigid bodies
     */
    public static class DistanceConstraint extends Constraint {
        private Vector3D anchorA;
        private Vector3D anchorB;
        private double distance;
        
        public DistanceConstraint(RigidBody bodyA, RigidBody bodyB, Vector3D anchorA, Vector3D anchorB) {
            super(bodyA, bodyB);
            this.anchorA = anchorA;
            this.anchorB = anchorB;
            // Calculate initial distance
            Vector3D worldA = bodyA.getPosition().add(anchorA);
            Vector3D worldB = bodyB.getPosition().add(anchorB);
            this.distance = worldA.distance(worldB);
        }
        
        @Override
        public void solve(double deltaTime) {
            Vector3D worldA = bodyA.getPosition().add(anchorA);
            Vector3D worldB = bodyB.getPosition().add(anchorB);
            Vector3D delta = worldB.subtract(worldA);
            double currentDistance = delta.magnitude();
            
            if (currentDistance == 0) {
                return;
            }
            
            double bias = (currentDistance - distance) * biasFactor;
            Vector3D normal = delta.normalize();
            double impulse = bias / (1.0 / bodyA.getMass() + 1.0 / bodyB.getMass());
            
            Vector3D impulseVector = normal.multiply(impulse * relaxation);
            bodyA.setLinearVelocity(bodyA.getLinearVelocity().add(impulseVector.multiply(1.0 / bodyA.getMass())));
            bodyB.setLinearVelocity(bodyB.getLinearVelocity().subtract(impulseVector.multiply(1.0 / bodyB.getMass())));
        }
        
        public Vector3D getAnchorA() {
            return anchorA;
        }
        
        public Vector3D getAnchorB() {
            return anchorB;
        }
        
        public double getDistance() {
            return distance;
        }
        
        public void setDistance(double distance) {
            this.distance = distance;
        }
    }
    
    /**
     * Represents a physics world containing all rigid bodies and constraints
     */
    public static class PhysicsWorld {
        private List<RigidBody> bodies;
        private List<Constraint> constraints;
        private Vector3D gravity;
        private double timeStep;
        private int solverIterations;
        
        public PhysicsWorld() {
            this.bodies = new ArrayList<>();
            this.constraints = new ArrayList<>();
            this.gravity = new Vector3D(0, -9.81, 0);
            this.timeStep = 1.0 / 60.0;
            this.solverIterations = 10;
        }
        
        /**
         * Updates the physics simulation
         */
        public void update(double deltaTime) {
            // Apply gravity to all non-static bodies
            for (RigidBody body : bodies) {
                if (!body.isStatic() && body.isEnabled()) {
                    body.applyForce(gravity.multiply(body.getMass()));
                }
            }
            
            // Integrate velocities and positions
            for (RigidBody body : bodies) {
                body.integrate(deltaTime);
            }
            
            // Detect and resolve collisions
            detectAndResolveCollisions();
            
            // Solve constraints
            for (int i = 0; i < solverIterations; i++) {
                for (Constraint constraint : constraints) {
                    constraint.solve(deltaTime);
                }
            }
        }
        
        /**
         * Detects and resolves collisions between rigid bodies
         */
        private void detectAndResolveCollisions() {
            for (int i = 0; i < bodies.size(); i++) {
                for (int j = i + 1; j < bodies.size(); j++) {
                    RigidBody bodyA = bodies.get(i);
                    RigidBody bodyB = bodies.get(j);
                    
                    if (!bodyA.isEnabled() || !bodyB.isEnabled()) {
                        continue;
                    }
                    
                    if (bodyA.isStatic() && bodyB.isStatic()) {
                        continue;
                    }
                    
                    // Simple collision detection based on shapes
                    if (bodyA.getShape().intersects(bodyB.getShape())) {
                        resolveCollision(bodyA, bodyB);
                    }
                }
            }
        }
        
        /**
         * Resolves collision between two rigid bodies
         */
        private void resolveCollision(RigidBody bodyA, RigidBody bodyB) {
            // Calculate collision normal (simplified)
            Vector3D normal = bodyB.getPosition().subtract(bodyA.getPosition()).normalize();
            
            // Calculate relative velocity
            Vector3D relativeVelocity = bodyB.getLinearVelocity().subtract(bodyA.getLinearVelocity());
            double velocityAlongNormal = relativeVelocity.dot(normal);
            
            // Do not resolve if bodies are separating
            if (velocityAlongNormal > 0) {
                return;
            }
            
            // Calculate restitution
            double restitution = Math.min(bodyA.getMaterial().getRestitution(), bodyB.getMaterial().getRestitution());
            
            // Calculate impulse scalar
            double impulseScalar = -(1 + restitution) * velocityAlongNormal;
            impulseScalar /= (1.0 / bodyA.getMass() + 1.0 / bodyB.getMass());
            
            // Apply impulse
            Vector3D impulse = normal.multiply(impulseScalar);
            if (!bodyA.isStatic()) {
                bodyA.setLinearVelocity(bodyA.getLinearVelocity().subtract(impulse.multiply(1.0 / bodyA.getMass())));
            }
            if (!bodyB.isStatic()) {
                bodyB.setLinearVelocity(bodyB.getLinearVelocity().add(impulse.multiply(1.0 / bodyB.getMass())));
            }
        }
        
        /**
         * Adds a rigid body to the physics world
         */
        public void addBody(RigidBody body) {
            bodies.add(body);
        }
        
        /**
         * Removes a rigid body from the physics world
         */
        public void removeBody(RigidBody body) {
            bodies.remove(body);
        }
        
        /**
         * Adds a constraint to the physics world
         */
        public void addConstraint(Constraint constraint) {
            constraints.add(constraint);
        }
        
        /**
         * Removes a constraint from the physics world
         */
        public void removeConstraint(Constraint constraint) {
            constraints.remove(constraint);
        }
        
        /**
         * Gets all rigid bodies in the physics world
         */
        public List<RigidBody> getBodies() {
            return new ArrayList<>(bodies);
        }
        
        /**
         * Gets all constraints in the physics world
         */
        public List<Constraint> getConstraints() {
            return new ArrayList<>(constraints);
        }
        
        /**
         * Gets the gravity vector
         */
        public Vector3D getGravity() {
            return gravity;
        }
        
        /**
         * Sets the gravity vector
         */
        public void setGravity(Vector3D gravity) {
            this.gravity = gravity;
        }
        
        /**
         * Gets the time step
         */
        public double getTimeStep() {
            return timeStep;
        }
        
        /**
         * Sets the time step
         */
        public void setTimeStep(double timeStep) {
            this.timeStep = timeStep;
        }
        
        /**
         * Gets the number of solver iterations
         */
        public int getSolverIterations() {
            return solverIterations;
        }
        
        /**
         * Sets the number of solver iterations
         */
        public void setSolverIterations(int solverIterations) {
            this.solverIterations = solverIterations;
        }
    }
    
    /**
     * Represents a particle system for effects and fluids
     */
    public static class ParticleSystem {
        private List<Particle> particles;
        private Vector3D emitterPosition;
        private Vector3D emitterVelocity;
        private double emissionRate;
        private double particleLifetime;
        private double particleMass;
        private Random random;
        
        public ParticleSystem(Vector3D emitterPosition) {
            this.particles = new ArrayList<>();
            this.emitterPosition = emitterPosition;
            this.emitterVelocity = new Vector3D(0, 0, 0);
            this.emissionRate = 100; // particles per second
            this.particleLifetime = 5.0;
            this.particleMass = 1.0;
            this.random = new Random();
        }
        
        /**
         * Updates the particle system
         */
        public void update(double deltaTime) {
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
        }
        
        /**
         * Emits particles based on emission rate
         */
        private void emitParticles(double deltaTime) {
            double particlesToEmit = emissionRate * deltaTime;
            int particleCount = (int) Math.floor(particlesToEmit);
            
            // Add fractional particle with some probability
            if (random.nextDouble() < (particlesToEmit - particleCount)) {
                particleCount++;
            }
            
            for (int i = 0; i < particleCount; i++) {
                // Create particle with random properties
                Vector3D position = emitterPosition.add(new Vector3D(
                    (random.nextDouble() - 0.5) * 2,
                    (random.nextDouble() - 0.5) * 2,
                    (random.nextDouble() - 0.5) * 2
                ));
                
                Vector3D velocity = emitterVelocity.add(new Vector3D(
                    (random.nextDouble() - 0.5) * 10,
                    random.nextDouble() * 5,
                    (random.nextDouble() - 0.5) * 10
                ));
                
                Particle particle = new Particle(position, velocity, particleMass, particleLifetime);
                particles.add(particle);
            }
        }
        
        /**
         * Applies a force field to all particles
         */
        public void applyForceField(Vector3D force) {
            for (Particle particle : particles) {
                particle.applyForce(force);
            }
        }
        
        /**
         * Gets all particles in the system
         */
        public List<Particle> getParticles() {
            return new ArrayList<>(particles);
        }
        
        public Vector3D getEmitterPosition() {
            return emitterPosition;
        }
        
        public void setEmitterPosition(Vector3D emitterPosition) {
            this.emitterPosition = emitterPosition;
        }
        
        public Vector3D getEmitterVelocity() {
            return emitterVelocity;
        }
        
        public void setEmitterVelocity(Vector3D emitterVelocity) {
            this.emitterVelocity = emitterVelocity;
        }
        
        public double getEmissionRate() {
            return emissionRate;
        }
        
        public void setEmissionRate(double emissionRate) {
            this.emissionRate = emissionRate;
        }
        
        public double getParticleLifetime() {
            return particleLifetime;
        }
        
        public void setParticleLifetime(double particleLifetime) {
            this.particleLifetime = particleLifetime;
        }
        
        public double getParticleMass() {
            return particleMass;
        }
        
        public void setParticleMass(double particleMass) {
            this.particleMass = particleMass;
        }
    }
    
    /**
     * Represents a single particle in a particle system
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
        
        public Vector3D getPosition() {
            return position;
        }
        
        public Vector3D getVelocity() {
            return velocity;
        }
        
        public double getLifetime() {
            return lifetime;
        }
        
        public double getMaxLifetime() {
            return maxLifetime;
        }
        
        public double getMass() {
            return mass;
        }
    }
    
    /**
     * Multiplies a 4x4 matrix with a 3D vector (treating vector as 4D with w=0)
     */
    private static Vector3D multiplyMatrixByVector3D(Matrix4x4 matrix, Vector3D vector) {
        double x = matrix.get(0, 0) * vector.getX() + matrix.get(0, 1) * vector.getY() + matrix.get(0, 2) * vector.getZ();
        double y = matrix.get(1, 0) * vector.getX() + matrix.get(1, 1) * vector.getY() + matrix.get(1, 2) * vector.getZ();
        double z = matrix.get(2, 0) * vector.getX() + matrix.get(2, 1) * vector.getY() + matrix.get(2, 2) * vector.getZ();
        return new Vector3D(x, y, z);
    }
}
