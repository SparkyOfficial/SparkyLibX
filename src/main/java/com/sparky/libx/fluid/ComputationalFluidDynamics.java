package com.sparky.libx.fluid;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.math.Matrix4x4;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Computational Fluid Dynamics Framework for Minecraft Plugins
 * Provides capabilities for simulating fluid dynamics, particle systems, and complex flow behaviors
 * 
 * @author Андрій Будильников
 */
public class ComputationalFluidDynamics {
    
    /**
     * Represents a 3D grid-based fluid simulation
     */
    public static class FluidGrid {
        private final int width, height, depth;
        private final double cellSize;
        private final FluidCell[][][] cells;
        private final List<FluidParticle> particles;
        private Vector3D gravity;
        private double viscosity;
        private double density;
        private double timeStep;
        
        public FluidGrid(int width, int height, int depth, double cellSize) {
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.cellSize = cellSize;
            this.cells = new FluidCell[width][height][depth];
            this.particles = new ArrayList<>();
            this.gravity = new Vector3D(0, -9.81, 0);
            this.viscosity = 0.01;
            this.density = 1.0;
            this.timeStep = 0.016; // 60 FPS
            
            // Initialize cells
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < depth; z++) {
                        cells[x][y][z] = new FluidCell();
                    }
                }
            }
        }
        
        /**
         * Updates the fluid simulation for one time step
         */
        public void update() {
            // Apply external forces
            applyGravity();
            
            // Advect velocity field
            advectVelocity();
            
            // Apply viscosity
            applyViscosity();
            
            // Project velocity field to make it incompressible
            projectVelocity();
            
            // Advect particles
            advectParticles();
            
            // Handle boundary conditions
            applyBoundaryConditions();
        }
        
        /**
         * Applies gravity force to all fluid cells
         */
        private void applyGravity() {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < depth; z++) {
                        cells[x][y][z].velocity = cells[x][y][z].velocity.add(gravity.multiply(timeStep));
                    }
                }
            }
        }
        
        /**
         * Advects the velocity field using semi-Lagrangian advection
         */
        private void advectVelocity() {
            FluidCell[][][] newCells = new FluidCell[width][height][depth];
            
            // Initialize new cells
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < depth; z++) {
                        newCells[x][y][z] = new FluidCell();
                    }
                }
            }
            
            // Advect each cell
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < depth; z++) {
                        // Trace particle back in time
                        Vector3D position = new Vector3D(x * cellSize, y * cellSize, z * cellSize);
                        Vector3D backtrack = position.subtract(cells[x][y][z].velocity.multiply(timeStep));
                        
                        // Interpolate velocity from surrounding cells
                        Vector3D interpolatedVelocity = interpolateVelocity(backtrack);
                        newCells[x][y][z].velocity = interpolatedVelocity;
                        newCells[x][y][z].pressure = cells[x][y][z].pressure;
                        newCells[x][y][z].density = cells[x][y][z].density;
                    }
                }
            }
            
            // Update cells
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < depth; z++) {
                        cells[x][y][z] = newCells[x][y][z];
                    }
                }
            }
        }
        
        /**
         * Applies viscosity to smooth the velocity field
         */
        private void applyViscosity() {
            // Simple diffusion using Jacobi iteration
            FluidCell[][][] newCells = new FluidCell[width][height][depth];
            
            // Initialize new cells
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < depth; z++) {
                        newCells[x][y][z] = new FluidCell();
                    }
                }
            }
            
            // Apply diffusion
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    for (int z = 1; z < depth - 1; z++) {
                        Vector3D sum = new Vector3D(0, 0, 0);
                        sum = sum.add(cells[x-1][y][z].velocity);
                        sum = sum.add(cells[x+1][y][z].velocity);
                        sum = sum.add(cells[x][y-1][z].velocity);
                        sum = sum.add(cells[x][y+1][z].velocity);
                        sum = sum.add(cells[x][y][z-1].velocity);
                        sum = sum.add(cells[x][y][z+1].velocity);
                        
                        double alpha = viscosity * timeStep / (cellSize * cellSize);
                        Vector3D diffused = cells[x][y][z].velocity.multiply(1 - 6 * alpha).add(sum.multiply(alpha));
                        newCells[x][y][z].velocity = diffused;
                        newCells[x][y][z].pressure = cells[x][y][z].pressure;
                        newCells[x][y][z].density = cells[x][y][z].density;
                    }
                }
            }
            
            // Update cells
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < depth; z++) {
                        cells[x][y][z] = newCells[x][y][z];
                    }
                }
            }
        }
        
        /**
         * Projects the velocity field to make it incompressible
         */
        private void projectVelocity() {
            // Solve Poisson equation for pressure using Jacobi iteration
            double[][][] pressure = new double[width][height][depth];
            double[][][] divergence = new double[width][height][depth];
            
            // Calculate divergence
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    for (int z = 1; z < depth - 1; z++) {
                        double dx = (cells[x+1][y][z].velocity.getX() - cells[x-1][y][z].velocity.getX()) / (2 * cellSize);
                        double dy = (cells[x][y+1][z].velocity.getY() - cells[x][y-1][z].velocity.getY()) / (2 * cellSize);
                        double dz = (cells[x][y][z+1].velocity.getZ() - cells[x][y][z-1].velocity.getZ()) / (2 * cellSize);
                        divergence[x][y][z] = dx + dy + dz;
                    }
                }
            }
            
            // Solve for pressure using Jacobi iteration
            for (int iter = 0; iter < 20; iter++) {
                double[][][] newPressure = new double[width][height][depth];
                
                for (int x = 1; x < width - 1; x++) {
                    for (int y = 1; y < height - 1; y++) {
                        for (int z = 1; z < depth - 1; z++) {
                            double sum = pressure[x-1][y][z] + pressure[x+1][y][z] + 
                                        pressure[x][y-1][z] + pressure[x][y+1][z] + 
                                        pressure[x][y][z-1] + pressure[x][y][z+1] - 
                                        cellSize * cellSize * divergence[x][y][z];
                            newPressure[x][y][z] = sum / 6.0;
                        }
                    }
                }
                
                pressure = newPressure;
            }
            
            // Subtract pressure gradient from velocity
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    for (int z = 1; z < depth - 1; z++) {
                        double dx = (pressure[x+1][y][z] - pressure[x-1][y][z]) / (2 * cellSize);
                        double dy = (pressure[x][y+1][z] - pressure[x][y-1][z]) / (2 * cellSize);
                        double dz = (pressure[x][y][z+1] - pressure[x][y][z-1]) / (2 * cellSize);
                        
                        Vector3D gradient = new Vector3D(dx, dy, dz);
                        cells[x][y][z].velocity = cells[x][y][z].velocity.subtract(gradient.multiply(timeStep));
                    }
                }
            }
        }
        
        /**
         * Advects fluid particles through the velocity field
         */
        private void advectParticles() {
            for (FluidParticle particle : particles) {
                // RK2 integration for better accuracy
                Vector3D k1 = interpolateVelocity(particle.position).multiply(timeStep);
                Vector3D k2 = interpolateVelocity(particle.position.add(k1.multiply(0.5))).multiply(timeStep);
                particle.position = particle.position.add(k2);
                
                // Update particle velocity
                particle.velocity = interpolateVelocity(particle.position);
                
                // Handle boundary collisions
                handleParticleBoundaryCollision(particle);
            }
        }
        
        /**
         * Applies boundary conditions to enforce solid walls
         */
        private void applyBoundaryConditions() {
            // Set normal velocity components to zero at boundaries
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    cells[0][y][z].velocity = new Vector3D(0, cells[0][y][z].velocity.getY(), cells[0][y][z].velocity.getZ());
                    cells[width-1][y][z].velocity = new Vector3D(0, cells[width-1][y][z].velocity.getY(), cells[width-1][y][z].velocity.getZ());
                }
            }
            
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    cells[x][0][z].velocity = new Vector3D(cells[x][0][z].velocity.getX(), 0, cells[x][0][z].velocity.getZ());
                    cells[x][height-1][z].velocity = new Vector3D(cells[x][height-1][z].velocity.getX(), 0, cells[x][height-1][z].velocity.getZ());
                }
            }
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    cells[x][y][0].velocity = new Vector3D(cells[x][y][0].velocity.getX(), cells[x][y][0].velocity.getY(), 0);
                    cells[x][y][depth-1].velocity = new Vector3D(cells[x][y][depth-1].velocity.getX(), cells[x][y][depth-1].velocity.getY(), 0);
                }
            }
        }
        
        /**
         * Handles particle collisions with boundaries
         */
        private void handleParticleBoundaryCollision(FluidParticle particle) {
            // Simple reflection at boundaries
            if (particle.position.getX() < 0) {
                particle.position = new Vector3D(0, particle.position.getY(), particle.position.getZ());
                particle.velocity = new Vector3D(-particle.velocity.getX() * 0.5, particle.velocity.getY(), particle.velocity.getZ());
            } else if (particle.position.getX() > width * cellSize) {
                particle.position = new Vector3D(width * cellSize, particle.position.getY(), particle.position.getZ());
                particle.velocity = new Vector3D(-particle.velocity.getX() * 0.5, particle.velocity.getY(), particle.velocity.getZ());
            }
            
            if (particle.position.getY() < 0) {
                particle.position = new Vector3D(particle.position.getX(), 0, particle.position.getZ());
                particle.velocity = new Vector3D(particle.velocity.getX(), -particle.velocity.getY() * 0.5, particle.velocity.getZ());
            } else if (particle.position.getY() > height * cellSize) {
                particle.position = new Vector3D(particle.position.getX(), height * cellSize, particle.position.getZ());
                particle.velocity = new Vector3D(particle.velocity.getX(), -particle.velocity.getY() * 0.5, particle.velocity.getZ());
            }
            
            if (particle.position.getZ() < 0) {
                particle.position = new Vector3D(particle.position.getX(), particle.position.getY(), 0);
                particle.velocity = new Vector3D(particle.velocity.getX(), particle.velocity.getY(), -particle.velocity.getZ() * 0.5);
            } else if (particle.position.getZ() > depth * cellSize) {
                particle.position = new Vector3D(particle.position.getX(), particle.position.getY(), depth * cellSize);
                particle.velocity = new Vector3D(particle.velocity.getX(), particle.velocity.getY(), -particle.velocity.getZ() * 0.5);
            }
        }
        
        /**
         * Interpolates velocity at a given position using trilinear interpolation
         */
        private Vector3D interpolateVelocity(Vector3D position) {
            double x = position.getX() / cellSize;
            double y = position.getY() / cellSize;
            double z = position.getZ() / cellSize;
            
            int x0 = (int) Math.floor(x);
            int y0 = (int) Math.floor(y);
            int z0 = (int) Math.floor(z);
            int x1 = x0 + 1;
            int y1 = y0 + 1;
            int z1 = z0 + 1;
            
            // Clamp indices to grid bounds
            x0 = Math.max(0, Math.min(width - 1, x0));
            y0 = Math.max(0, Math.min(height - 1, y0));
            z0 = Math.max(0, Math.min(depth - 1, z0));
            x1 = Math.max(0, Math.min(width - 1, x1));
            y1 = Math.max(0, Math.min(height - 1, y1));
            z1 = Math.max(0, Math.min(depth - 1, z1));
            
            // Calculate interpolation weights
            double fx = x - x0;
            double fy = y - y0;
            double fz = z - z0;
            
            // Trilinear interpolation
            Vector3D c000 = cells[x0][y0][z0].velocity;
            Vector3D c001 = cells[x0][y0][z1].velocity;
            Vector3D c010 = cells[x0][y1][z0].velocity;
            Vector3D c011 = cells[x0][y1][z1].velocity;
            Vector3D c100 = cells[x1][y0][z0].velocity;
            Vector3D c101 = cells[x1][y0][z1].velocity;
            Vector3D c110 = cells[x1][y1][z0].velocity;
            Vector3D c111 = cells[x1][y1][z1].velocity;
            
            Vector3D c00 = c000.multiply(1 - fx).add(c100.multiply(fx));
            Vector3D c01 = c001.multiply(1 - fx).add(c101.multiply(fx));
            Vector3D c10 = c010.multiply(1 - fx).add(c110.multiply(fx));
            Vector3D c11 = c011.multiply(1 - fx).add(c111.multiply(fx));
            
            Vector3D c0 = c00.multiply(1 - fy).add(c10.multiply(fy));
            Vector3D c1 = c01.multiply(1 - fy).add(c11.multiply(fy));
            
            return c0.multiply(1 - fz).add(c1.multiply(fz));
        }
        
        /**
         * Adds a fluid particle to the simulation
         */
        public void addParticle(FluidParticle particle) {
            particles.add(particle);
        }
        
        /**
         * Removes a fluid particle from the simulation
         */
        public void removeParticle(FluidParticle particle) {
            particles.remove(particle);
        }
        
        /**
         * Gets the list of fluid particles
         */
        public List<FluidParticle> getParticles() {
            return new ArrayList<>(particles);
        }
        
        /**
         * Gets a fluid cell at the specified grid coordinates
         */
        public FluidCell getCell(int x, int y, int z) {
            if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
                return cells[x][y][z];
            }
            return null;
        }
        
        /**
         * Gets the dimensions of the fluid grid
         */
        public int[] getDimensions() {
            return new int[]{width, height, depth};
        }
        
        /**
         * Gets the cell size
         */
        public double getCellSize() {
            return cellSize;
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
         * Gets the viscosity coefficient
         */
        public double getViscosity() {
            return viscosity;
        }
        
        /**
         * Sets the viscosity coefficient
         */
        public void setViscosity(double viscosity) {
            this.viscosity = viscosity;
        }
        
        /**
         * Gets the fluid density
         */
        public double getDensity() {
            return density;
        }
        
        /**
         * Sets the fluid density
         */
        public void setDensity(double density) {
            this.density = density;
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
    }
    
    /**
     * Represents a cell in the fluid grid
     */
    public static class FluidCell {
        private Vector3D velocity;
        private double pressure;
        private double density;
        
        public FluidCell() {
            this.velocity = new Vector3D(0, 0, 0);
            this.pressure = 0;
            this.density = 1.0;
        }
        
        public Vector3D getVelocity() {
            return velocity;
        }
        
        public void setVelocity(Vector3D velocity) {
            this.velocity = velocity;
        }
        
        public double getPressure() {
            return pressure;
        }
        
        public void setPressure(double pressure) {
            this.pressure = pressure;
        }
        
        public double getDensity() {
            return density;
        }
        
        public void setDensity(double density) {
            this.density = density;
        }
    }
    
    /**
     * Represents a fluid particle for particle-based fluid simulation
     */
    public static class FluidParticle {
        private Vector3D position;
        private Vector3D velocity;
        private Vector3D acceleration;
        private double mass;
        private double radius;
        private int id;
        
        public FluidParticle(int id, Vector3D position, double mass, double radius) {
            this.id = id;
            this.position = position;
            this.velocity = new Vector3D(0, 0, 0);
            this.acceleration = new Vector3D(0, 0, 0);
            this.mass = mass;
            this.radius = radius;
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
        
        public double getRadius() {
            return radius;
        }
        
        public void setRadius(double radius) {
            this.radius = radius;
        }
        
        public int getId() {
            return id;
        }
    }
    
    /**
     * Represents a fluid emitter for adding fluid to the simulation
     */
    public static class FluidEmitter {
        private Vector3D position;
        private Vector3D direction;
        private double velocity;
        private double rate;
        private double particleMass;
        private double particleRadius;
        private int particleIdCounter;
        private boolean enabled;
        
        public FluidEmitter(Vector3D position, Vector3D direction, double velocity, double rate) {
            this.position = position;
            this.direction = direction.normalize();
            this.velocity = velocity;
            this.rate = rate;
            this.particleMass = 1.0;
            this.particleRadius = 0.1;
            this.particleIdCounter = 0;
            this.enabled = true;
        }
        
        /**
         * Emits fluid particles into the simulation
         */
        public List<FluidParticle> emit(double deltaTime) {
            List<FluidParticle> newParticles = new ArrayList<>();
            
            if (!enabled) {
                return newParticles;
            }
            
            // Calculate number of particles to emit based on rate and time step
            double particlesToEmit = rate * deltaTime;
            int particleCount = (int) Math.floor(particlesToEmit);
            
            // Add fractional particle with some probability
            if (Math.random() < (particlesToEmit - particleCount)) {
                particleCount++;
            }
            
            // Create new particles
            for (int i = 0; i < particleCount; i++) {
                // Add some randomness to position and direction
                Vector3D randomOffset = new Vector3D(
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5
                );
                
                Vector3D randomDirection = direction.add(
                    new Vector3D(
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2
                    )
                ).normalize();
                
                Vector3D particlePosition = position.add(randomOffset);
                FluidParticle particle = new FluidParticle(
                    particleIdCounter++,
                    particlePosition,
                    particleMass,
                    particleRadius
                );
                
                particle.setVelocity(randomDirection.multiply(velocity));
                newParticles.add(particle);
            }
            
            return newParticles;
        }
        
        public Vector3D getPosition() {
            return position;
        }
        
        public void setPosition(Vector3D position) {
            this.position = position;
        }
        
        public Vector3D getDirection() {
            return direction;
        }
        
        public void setDirection(Vector3D direction) {
            this.direction = direction.normalize();
        }
        
        public double getVelocity() {
            return velocity;
        }
        
        public void setVelocity(double velocity) {
            this.velocity = velocity;
        }
        
        public double getRate() {
            return rate;
        }
        
        public void setRate(double rate) {
            this.rate = rate;
        }
        
        public double getParticleMass() {
            return particleMass;
        }
        
        public void setParticleMass(double particleMass) {
            this.particleMass = particleMass;
        }
        
        public double getParticleRadius() {
            return particleRadius;
        }
        
        public void setParticleRadius(double particleRadius) {
            this.particleRadius = particleRadius;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    /**
     * Represents a fluid obstacle that interacts with the fluid
     */
    public static class FluidObstacle {
        private Vector3D position;
        private Vector3D size;
        private ObstacleType type;
        private boolean enabled;
        
        public enum ObstacleType {
            CUBE, SPHERE, CYLINDER
        }
        
        public FluidObstacle(Vector3D position, Vector3D size, ObstacleType type) {
            this.position = position;
            this.size = size;
            this.type = type;
            this.enabled = true;
        }
        
        /**
         * Checks if a point is inside the obstacle
         */
        public boolean isPointInside(Vector3D point) {
            if (!enabled) {
                return false;
            }
            
            switch (type) {
                case CUBE:
                    return isPointInsideCube(point);
                case SPHERE:
                    return isPointInsideSphere(point);
                case CYLINDER:
                    return isPointInsideCylinder(point);
                default:
                    return false;
            }
        }
        
        /**
         * Checks if a point is inside a cube obstacle
         */
        private boolean isPointInsideCube(Vector3D point) {
            Vector3D min = position.subtract(size.multiply(0.5));
            Vector3D max = position.add(size.multiply(0.5));
            
            return point.getX() >= min.getX() && point.getX() <= max.getX() &&
                   point.getY() >= min.getY() && point.getY() <= max.getY() &&
                   point.getZ() >= min.getZ() && point.getZ() <= max.getZ();
        }
        
        /**
         * Checks if a point is inside a sphere obstacle
         */
        private boolean isPointInsideSphere(Vector3D point) {
            double distance = point.distance(position);
            return distance <= size.getX() * 0.5; // Assuming size.x is the diameter
        }
        
        /**
         * Checks if a point is inside a cylinder obstacle
         */
        private boolean isPointInsideCylinder(Vector3D point) {
            // Calculate distance from point to cylinder axis (assuming Y-axis aligned)
            double dx = point.getX() - position.getX();
            double dz = point.getZ() - position.getZ();
            double distanceToAxis = Math.sqrt(dx * dx + dz * dz);
            
            // Check if within radius and height
            double dy = Math.abs(point.getY() - position.getY());
            return distanceToAxis <= size.getX() * 0.5 && dy <= size.getY() * 0.5;
        }
        
        /**
         * Handles collision between a particle and this obstacle
         */
        public void handleParticleCollision(FluidParticle particle) {
            if (!enabled) {
                return;
            }
            
            // Simple reflection-based collision response
            Vector3D particlePosition = particle.getPosition();
            Vector3D particleVelocity = particle.getVelocity();
            
            switch (type) {
                case CUBE:
                    handleCubeCollision(particle, particlePosition, particleVelocity);
                    break;
                case SPHERE:
                    handleSphereCollision(particle, particlePosition, particleVelocity);
                    break;
                case CYLINDER:
                    handleCylinderCollision(particle, particlePosition, particleVelocity);
                    break;
            }
        }
        
        /**
         * Handles collision with a cube obstacle
         */
        private void handleCubeCollision(FluidParticle particle, Vector3D position, Vector3D velocity) {
            Vector3D min = this.position.subtract(size.multiply(0.5));
            Vector3D max = this.position.add(size.multiply(0.5));
            
            // Check collision with each face
            if (position.getX() - particle.getRadius() < min.getX() && velocity.getX() < 0) {
                particle.setPosition(new Vector3D(min.getX() + particle.getRadius(), position.getY(), position.getZ()));
                particle.setVelocity(new Vector3D(-velocity.getX() * 0.8, velocity.getY(), velocity.getZ()));
            } else if (position.getX() + particle.getRadius() > max.getX() && velocity.getX() > 0) {
                particle.setPosition(new Vector3D(max.getX() - particle.getRadius(), position.getY(), position.getZ()));
                particle.setVelocity(new Vector3D(-velocity.getX() * 0.8, velocity.getY(), velocity.getZ()));
            }
            
            if (position.getY() - particle.getRadius() < min.getY() && velocity.getY() < 0) {
                particle.setPosition(new Vector3D(position.getX(), min.getY() + particle.getRadius(), position.getZ()));
                particle.setVelocity(new Vector3D(velocity.getX(), -velocity.getY() * 0.8, velocity.getZ()));
            } else if (position.getY() + particle.getRadius() > max.getY() && velocity.getY() > 0) {
                particle.setPosition(new Vector3D(position.getX(), max.getY() - particle.getRadius(), position.getZ()));
                particle.setVelocity(new Vector3D(velocity.getX(), -velocity.getY() * 0.8, velocity.getZ()));
            }
            
            if (position.getZ() - particle.getRadius() < min.getZ() && velocity.getZ() < 0) {
                particle.setPosition(new Vector3D(position.getX(), position.getY(), min.getZ() + particle.getRadius()));
                particle.setVelocity(new Vector3D(velocity.getX(), velocity.getY(), -velocity.getZ() * 0.8));
            } else if (position.getZ() + particle.getRadius() > max.getZ() && velocity.getZ() > 0) {
                particle.setPosition(new Vector3D(position.getX(), position.getY(), max.getZ() - particle.getRadius()));
                particle.setVelocity(new Vector3D(velocity.getX(), velocity.getY(), -velocity.getZ() * 0.8));
            }
        }
        
        /**
         * Handles collision with a sphere obstacle
         */
        private void handleSphereCollision(FluidParticle particle, Vector3D position, Vector3D velocity) {
            Vector3D direction = position.subtract(this.position);
            double distance = direction.magnitude();
            double radius = size.getX() * 0.5 + particle.getRadius();
            
            if (distance < radius) {
                // Move particle outside the sphere
                Vector3D normalizedDirection = direction.normalize();
                Vector3D newPosition = this.position.add(normalizedDirection.multiply(radius));
                particle.setPosition(newPosition);
                
                // Reflect velocity
                double dotProduct = velocity.dot(normalizedDirection);
                Vector3D reflection = velocity.subtract(normalizedDirection.multiply(2 * dotProduct));
                particle.setVelocity(reflection.multiply(0.8));
            }
        }
        
        /**
         * Handles collision with a cylinder obstacle
         */
        private void handleCylinderCollision(FluidParticle particle, Vector3D position, Vector3D velocity) {
            // Calculate distance from particle to cylinder axis (Y-axis aligned)
            double dx = position.getX() - this.position.getX();
            double dz = position.getZ() - this.position.getZ();
            double distanceToAxis = Math.sqrt(dx * dx + dz * dz);
            
            double cylinderRadius = size.getX() * 0.5;
            double halfHeight = size.getY() * 0.5;
            
            // Check if particle is within cylinder height
            if (Math.abs(position.getY() - this.position.getY()) <= halfHeight) {
                // Handle radial collision
                if (distanceToAxis < cylinderRadius + particle.getRadius()) {
                    // Calculate normal vector pointing away from cylinder axis
                    Vector3D normal = new Vector3D(dx, 0, dz).normalize();
                    
                    // Move particle outside the cylinder
                    Vector3D newPosition = new Vector3D(
                        this.position.getX() + normal.getX() * (cylinderRadius + particle.getRadius()),
                        position.getY(),
                        this.position.getZ() + normal.getZ() * (cylinderRadius + particle.getRadius())
                    );
                    particle.setPosition(newPosition);
                    
                    // Reflect velocity
                    double dotProduct = velocity.dot(normal);
                    Vector3D reflection = velocity.subtract(normal.multiply(2 * dotProduct));
                    particle.setVelocity(reflection.multiply(0.8));
                }
            }
            
            // Handle top/bottom collision
            if (position.getY() - particle.getRadius() < this.position.getY() - halfHeight) {
                particle.setPosition(new Vector3D(position.getX(), this.position.getY() - halfHeight + particle.getRadius(), position.getZ()));
                particle.setVelocity(new Vector3D(velocity.getX(), -velocity.getY() * 0.8, velocity.getZ()));
            } else if (position.getY() + particle.getRadius() > this.position.getY() + halfHeight) {
                particle.setPosition(new Vector3D(position.getX(), this.position.getY() + halfHeight - particle.getRadius(), position.getZ()));
                particle.setVelocity(new Vector3D(velocity.getX(), -velocity.getY() * 0.8, velocity.getZ()));
            }
        }
        
        public Vector3D getPosition() {
            return position;
        }
        
        public void setPosition(Vector3D position) {
            this.position = position;
        }
        
        public Vector3D getSize() {
            return size;
        }
        
        public void setSize(Vector3D size) {
            this.size = size;
        }
        
        public ObstacleType getType() {
            return type;
        }
        
        public void setType(ObstacleType type) {
            this.type = type;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    /**
     * Represents a fluid simulation scene with all components
     */
    public static class FluidScene {
        private FluidGrid fluidGrid;
        private List<FluidEmitter> emitters;
        private List<FluidObstacle> obstacles;
        private double time;
        
        public FluidScene(FluidGrid fluidGrid) {
            this.fluidGrid = fluidGrid;
            this.emitters = new ArrayList<>();
            this.obstacles = new ArrayList<>();
            this.time = 0;
        }
        
        /**
         * Updates the entire fluid scene
         */
        public void update(double deltaTime) {
            time += deltaTime;
            
            // Emit new particles
            for (FluidEmitter emitter : emitters) {
                List<FluidParticle> newParticles = emitter.emit(deltaTime);
                for (FluidParticle particle : newParticles) {
                    fluidGrid.addParticle(particle);
                }
            }
            
            // Update fluid grid
            fluidGrid.update();
            
            // Handle particle-obstacle collisions
            List<FluidParticle> particles = fluidGrid.getParticles();
            for (FluidParticle particle : particles) {
                for (FluidObstacle obstacle : obstacles) {
                    if (obstacle.isPointInside(particle.getPosition())) {
                        obstacle.handleParticleCollision(particle);
                    }
                }
            }
        }
        
        /**
         * Adds an emitter to the scene
         */
        public void addEmitter(FluidEmitter emitter) {
            emitters.add(emitter);
        }
        
        /**
         * Removes an emitter from the scene
         */
        public void removeEmitter(FluidEmitter emitter) {
            emitters.remove(emitter);
        }
        
        /**
         * Adds an obstacle to the scene
         */
        public void addObstacle(FluidObstacle obstacle) {
            obstacles.add(obstacle);
        }
        
        /**
         * Removes an obstacle from the scene
         */
        public void removeObstacle(FluidObstacle obstacle) {
            obstacles.remove(obstacle);
        }
        
        /**
         * Gets the fluid grid
         */
        public FluidGrid getFluidGrid() {
            return fluidGrid;
        }
        
        /**
         * Gets the list of emitters
         */
        public List<FluidEmitter> getEmitters() {
            return new ArrayList<>(emitters);
        }
        
        /**
         * Gets the list of obstacles
         */
        public List<FluidObstacle> getObstacles() {
            return new ArrayList<>(obstacles);
        }
        
        /**
         * Gets the simulation time
         */
        public double getTime() {
            return time;
        }
    }
}