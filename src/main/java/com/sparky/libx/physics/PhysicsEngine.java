package com.sparky.libx.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import com.sparky.libx.math.Vector3D;

/**
 * фізичний рушій для симуляції реалістичних фізичних взаємодій в майнкрафті
 * підтримує гравітацію, імпульси, колізії, рідини, вибухи і багато іншого
 * @author Андрій Будильников
 */
public class PhysicsEngine {
    
    private static PhysicsEngine instance;
    private final Map<UUID, PhysicsEntity> entities;
    private final List<ForceField> forceFields;
    private final Map<UUID, FluidVolume> fluidVolumes;
    private double gravity = 9.81;
    private double airResistance = 0.01;
    private long lastUpdateTime = System.currentTimeMillis();
    
    private PhysicsEngine() {
        this.entities = new HashMap<>();
        this.forceFields = new ArrayList<>();
        this.fluidVolumes = new HashMap<>();
    }
    
    /**
     * отримати екземпляр фізичного рушія (singleton)
     */
    public static PhysicsEngine getInstance() {
        if (instance == null) {
            instance = new PhysicsEngine();
        }
        return instance;
    }
    
    /**
     * оновити всі фізичні об'єкти
     */
    public void update() {
        long currentTime = System.currentTimeMillis();
        double deltaTime = (currentTime - lastUpdateTime) / 1000.0;
        lastUpdateTime = currentTime;
        
        // оновити всі сутності
        for (PhysicsEntity entity : entities.values()) {
            updateEntity(entity, deltaTime);
        }
        
        // оновити поля сил
        for (ForceField field : forceFields) {
            field.update(deltaTime);
        }
    }
    
    /**
     * оновити окрему фізичну сутність
     */
    private void updateEntity(PhysicsEntity entity, double deltaTime) {
        if (!entity.isActive()) return;
        
        // застосувати сили
        Vector3D totalForce = new Vector3D(0, 0, 0);
        
        // гравітація
        totalForce = totalForce.add(new Vector3D(0, -gravity * entity.getMass(), 0));
        
        // опір повітря
        Vector3D airResistanceForce = entity.getVelocity().multiply(-airResistance * entity.getDragCoefficient());
        totalForce = totalForce.add(airResistanceForce);
        
        // поля сил
        for (ForceField field : forceFields) {
            if (field.affects(entity)) {
                totalForce = totalForce.add(field.calculateForce(entity));
            }
        }
        
        // рідини
        for (FluidVolume fluid : fluidVolumes.values()) {
            if (fluid.contains(entity.getPosition())) {
                Vector3D buoyancy = calculateBuoyancy(entity, fluid);
                totalForce = totalForce.add(buoyancy);
                
                Vector3D drag = calculateFluidDrag(entity, fluid);
                totalForce = totalForce.add(drag);
            }
        }
        
        // обчислити прискорення
        Vector3D acceleration = totalForce.divide(entity.getMass());
        
        // оновити швидкість
        Vector3D newVelocity = entity.getVelocity().add(acceleration.multiply(deltaTime));
        entity.setVelocity(newVelocity);
        
        // оновити позицію
        Vector3D newPosition = entity.getPosition().add(newVelocity.multiply(deltaTime));
        entity.setPosition(newPosition);
        
        // обробити колізії
        handleCollisions(entity);
    }
    
    /**
     * обчислити силу плавучості
     */
    private Vector3D calculateBuoyancy(PhysicsEntity entity, FluidVolume fluid) {
        double volume = entity.getVolume();
        double fluidDensity = fluid.getDensity();
        double buoyantForce = fluidDensity * volume * gravity;
        return new Vector3D(0, buoyantForce, 0);
    }
    
    /**
     * обчислити силу опору в рідині
     */
    private Vector3D calculateFluidDrag(PhysicsEntity entity, FluidVolume fluid) {
        double dragCoefficient = fluid.getDragCoefficient();
        Vector3D velocity = entity.getVelocity();
        double speedSquared = velocity.magnitudeSquared();
        double dragMagnitude = 0.5 * fluid.getDensity() * speedSquared * dragCoefficient * entity.getDragArea();
        Vector3D dragDirection = velocity.normalize().multiply(-1);
        return dragDirection.multiply(dragMagnitude);
    }
    
    /**
     * обробити колізії
     */
    private void handleCollisions(PhysicsEntity entity) {
        // тут буде реалізація виявлення і обробки колізій
        // поки що проста реалізація для демонстрації
    }
    
    /**
     * додати фізичну сутність до рушія
     */
    public void addEntity(PhysicsEntity entity) {
        entities.put(entity.getId(), entity);
    }
    
    /**
     * видалити фізичну сутність з рушія
     */
    public void removeEntity(UUID entityId) {
        entities.remove(entityId);
    }
    
    /**
     * отримати фізичну сутність за ідентифікатором
     */
    public PhysicsEntity getEntity(UUID entityId) {
        return entities.get(entityId);
    }
    
    /**
     * додати поле сили
     */
    public void addForceField(ForceField field) {
        forceFields.add(field);
    }
    
    /**
     * видалити поле сили
     */
    public void removeForceField(ForceField field) {
        forceFields.remove(field);
    }
    
    /**
     * додати об'єм рідини
     */
    public void addFluidVolume(FluidVolume fluid) {
        fluidVolumes.put(fluid.getId(), fluid);
    }
    
    /**
     * видалити об'єм рідини
     */
    public void removeFluidVolume(UUID fluidId) {
        fluidVolumes.remove(fluidId);
    }
    
    /**
     * отримати об'єм рідини за ідентифікатором
     */
    public FluidVolume getFluidVolume(UUID fluidId) {
        return fluidVolumes.get(fluidId);
    }
    
    /**
     * встановити силу гравітації
     */
    public void setGravity(double gravity) {
        this.gravity = gravity;
    }
    
    /**
     * отримати силу гравітації
     */
    public double getGravity() {
        return gravity;
    }
    
    /**
     * встановити коефіцієнт опору повітря
     */
    public void setAirResistance(double airResistance) {
        this.airResistance = airResistance;
    }
    
    /**
     * отримати коефіцієнт опору повітря
     */
    public double getAirResistance() {
        return airResistance;
    }
    
    /**
     * створити вибух
     */
    public void createExplosion(Vector3D position, double radius, double force) {
        Explosion explosion = new Explosion(position, radius, force);
        explosion.apply(this);
    }
    
    /**
     * очистити всі фізичні об'єкти
     */
    public void clear() {
        entities.clear();
        forceFields.clear();
        fluidVolumes.clear();
    }
    
    /**
     * отримати кількість активних фізичних сутностей
     */
    public int getEntityCount() {
        return entities.size();
    }
    
    /**
     * отримати кількість полів сил
     */
    public int getForceFieldCount() {
        return forceFields.size();
    }
    
    /**
     * отримати кількість об'ємів рідин
     */
    public int getFluidVolumeCount() {
        return fluidVolumes.size();
    }
}