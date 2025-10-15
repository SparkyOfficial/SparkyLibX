package com.sparky.libx.physics;

import java.util.UUID;

import com.sparky.libx.math.Vector3D;

/**
 * фізична сутність для симуляції в фізичному рушії
 * представляє об'єкт з масою, позицією, швидкістю і іншими фізичними властивостями
 * @author Андрій Будильников
 */
public class PhysicsEntity {
    
    private final UUID id;
    private Vector3D position;
    private Vector3D velocity;
    private Vector3D acceleration;
    private double mass;
    private double volume;
    private double dragCoefficient;
    private double dragArea;
    private boolean active;
    private String name;
    
    /**
     * створити нову фізичну сутність
     */
    public PhysicsEntity(Vector3D position, double mass, double volume) {
        this.id = UUID.randomUUID();
        this.position = position;
        this.velocity = new Vector3D(0, 0, 0);
        this.acceleration = new Vector3D(0, 0, 0);
        this.mass = mass;
        this.volume = volume;
        this.dragCoefficient = 0.47; // коефіцієнт опору для сфери
        this.dragArea = 1.0;
        this.active = true;
        this.name = "PhysicsEntity";
    }
    
    /**
     * отримати унікальний ідентифікатор сутності
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * отримати позицію сутності
     */
    public Vector3D getPosition() {
        return position;
    }
    
    /**
     * встановити позицію сутності
     */
    public void setPosition(Vector3D position) {
        this.position = position;
    }
    
    /**
     * отримати швидкість сутності
     */
    public Vector3D getVelocity() {
        return velocity;
    }
    
    /**
     * встановити швидкість сутності
     */
    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }
    
    /**
     * отримати прискорення сутності
     */
    public Vector3D getAcceleration() {
        return acceleration;
    }
    
    /**
     * встановити прискорення сутності
     */
    public void setAcceleration(Vector3D acceleration) {
        this.acceleration = acceleration;
    }
    
    /**
     * отримати масу сутності
     */
    public double getMass() {
        return mass;
    }
    
    /**
     * встановити масу сутності
     */
    public void setMass(double mass) {
        if (mass <= 0) {
            throw new IllegalArgumentException("Маса повинна бути більше нуля");
        }
        this.mass = mass;
    }
    
    /**
     * отримати об'єм сутності
     */
    public double getVolume() {
        return volume;
    }
    
    /**
     * встановити об'єм сутності
     */
    public void setVolume(double volume) {
        if (volume <= 0) {
            throw new IllegalArgumentException("Об'єм повинен бути більше нуля");
        }
        this.volume = volume;
    }
    
    /**
     * отримати коефіцієнт опору
     */
    public double getDragCoefficient() {
        return dragCoefficient;
    }
    
    /**
     * встановити коефіцієнт опору
     */
    public void setDragCoefficient(double dragCoefficient) {
        this.dragCoefficient = dragCoefficient;
    }
    
    /**
     * отримати площу опору
     */
    public double getDragArea() {
        return dragArea;
    }
    
    /**
     * встановити площу опору
     */
    public void setDragArea(double dragArea) {
        this.dragArea = dragArea;
    }
    
    /**
     * перевірити чи сутність активна
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * встановити активність сутності
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * отримати ім'я сутності
     */
    public String getName() {
        return name;
    }
    
    /**
     * встановити ім'я сутності
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * застосувати силу до сутності
     */
    public void applyForce(Vector3D force) {
        Vector3D acceleration = force.divide(mass);
        this.acceleration = this.acceleration.add(acceleration);
    }
    
    /**
     * застосувати імпульс до сутності
     */
    public void applyImpulse(Vector3D impulse) {
        Vector3D velocityChange = impulse.divide(mass);
        this.velocity = this.velocity.add(velocityChange);
    }
    
    /**
     * обчислити кінетичну енергію
     */
    public double getKineticEnergy() {
        double speedSquared = velocity.magnitudeSquared();
        return 0.5 * mass * speedSquared;
    }
    
    /**
     * обчислити імпульс
     */
    public Vector3D getMomentum() {
        return velocity.multiply(mass);
    }
    
    /**
     * перевірити чи сутність в певній позиції
     */
    public boolean isAtPosition(Vector3D position, double tolerance) {
        return this.position.distance(position) <= tolerance;
    }
    
    @Override
    public String toString() {
        return String.format("PhysicsEntity{name='%s', position=%s, velocity=%s, mass=%.2f}", 
                           name, position, velocity, mass);
    }
}