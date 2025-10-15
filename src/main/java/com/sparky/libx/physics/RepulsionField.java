package com.sparky.libx.physics;

import com.sparky.libx.math.Vector3D;

/**
 * поле відштовхування, яке відштовхує фізичні сутності від центру
 * @author Андрій Будильников
 */
public class RepulsionField extends ForceField {
    
    private double falloffRate;
    
    /**
     * створити нове поле відштовхування
     * @param position позиція поля
     * @param strength сила поля
     * @param radius радіус дії
     * @param falloffRate швидкість зменшення сили з відстанню
     */
    public RepulsionField(Vector3D position, double strength, double radius, double falloffRate) {
        super(position, strength, radius);
        this.falloffRate = falloffRate;
        this.setName("RepulsionField");
    }
    
    /**
     * обчислити силу відштовхування, яку поле застосовує до сутності
     * @param entity фізична сутність
     * @return вектор сили відштовхування
     */
    @Override
    public Vector3D calculateForce(PhysicsEntity entity) {
        if (!affects(entity)) {
            return new Vector3D(0, 0, 0);
        }
        
        // обчислити напрямок від центру поля до сутності
        Vector3D direction = entity.getPosition().subtract(getPosition());
        double distance = direction.magnitude();
        
        if (distance <= 0) {
            return new Vector3D(0, 0, 0);
        }
        
        // нормалізувати напрямок
        Vector3D normalizedDirection = direction.normalize();
        
        // обчислити силу на основі відстані (зворотньо пропорційна з урахуванням falloffRate)
        double distanceFactor = Math.exp(-distance * falloffRate);
        double forceMagnitude = getStrength() * distanceFactor;
        
        // застосувати силу в напрямку від центру
        return normalizedDirection.multiply(forceMagnitude);
    }
    
    /**
     * отримати швидкість зменшення сили з відстанню
     */
    public double getFalloffRate() {
        return falloffRate;
    }
    
    /**
     * встановити швидкість зменшення сили з відстанню
     */
    public void setFalloffRate(double falloffRate) {
        this.falloffRate = falloffRate;
    }
    
    @Override
    public String toString() {
        return String.format("RepulsionField{position=%s, strength=%.2f, radius=%.2f, falloffRate=%.2f}", 
                           getPosition(), getStrength(), getRadius(), falloffRate);
    }
}