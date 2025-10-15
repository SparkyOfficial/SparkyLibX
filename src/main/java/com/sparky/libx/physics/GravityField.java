package com.sparky.libx.physics;

import com.sparky.libx.math.Vector3D;

/**
 * поле гравітації, яке застосовує постійну силу тяжіння до фізичних сутностей
 * @author Андрій Будильников
 */
public class GravityField extends ForceField {
    
    private Vector3D direction;
    private double magnitude;
    
    /**
     * створити нове поле гравітації
     * @param position позиція поля
     * @param strength сила поля
     * @param radius радіус дії
     * @param direction напрямок гравітації
     * @param magnitude величина гравітації
     */
    public GravityField(Vector3D position, double strength, double radius, Vector3D direction, double magnitude) {
        super(position, strength, radius);
        this.direction = direction.normalize();
        this.magnitude = magnitude;
        this.setName("GravityField");
    }
    
    /**
     * обчислити силу гравітації, яку поле застосовує до сутності
     * @param entity фізична сутність
     * @return вектор сили гравітації
     */
    @Override
    public Vector3D calculateForce(PhysicsEntity entity) {
        if (!affects(entity)) {
            return new Vector3D(0, 0, 0);
        }
        
        // обчислити відстань до сутності
        double distance = getPosition().distance(entity.getPosition());
        
        // обчислити силу на основі відстані (зворотньо пропорційна квадрату відстані)
        double distanceFactor = 1.0;
        if (distance > 0) {
            distanceFactor = 1.0 / (distance * distance);
        }
        
        // обчислити силу гравітації
        double forceMagnitude = getStrength() * magnitude * entity.getMass() * distanceFactor;
        
        // застосувати напрямок
        return direction.multiply(-forceMagnitude); // негативний, бо гравітація притягує
    }
    
    /**
     * отримати напрямок гравітації
     */
    public Vector3D getDirection() {
        return direction;
    }
    
    /**
     * встановити напрямок гравітації
     */
    public void setDirection(Vector3D direction) {
        this.direction = direction.normalize();
    }
    
    /**
     * отримати величину гравітації
     */
    public double getMagnitude() {
        return magnitude;
    }
    
    /**
     * встановити величину гравітації
     */
    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }
    
    @Override
    public String toString() {
        return String.format("GravityField{position=%s, strength=%.2f, radius=%.2f, direction=%s, magnitude=%.2f}", 
                           getPosition(), getStrength(), getRadius(), direction, magnitude);
    }
}