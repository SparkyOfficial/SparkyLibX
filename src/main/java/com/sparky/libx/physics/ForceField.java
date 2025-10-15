package com.sparky.libx.physics;

import com.sparky.libx.math.Vector3D;

/**
 * абстрактний клас для представлення поля сили в фізичному рушії
 * поля сили можуть впливати на фізичні сутності з різною інтенсивністю
 * @author Андрій Будильников
 */
public abstract class ForceField {
    
    protected Vector3D position;
    protected double strength;
    protected double radius;
    protected String name;
    
    /**
     * створити нове поле сили
     * @param position позиція поля сили
     * @param strength сила поля
     * @param radius радіус дії поля
     */
    public ForceField(Vector3D position, double strength, double radius) {
        this.position = position;
        this.strength = strength;
        this.radius = radius;
        this.name = "ForceField";
    }
    
    /**
     * обчислити силу, яку поле застосовує до сутності
     * @param entity фізична сутність
     * @return вектор сили
     */
    public abstract Vector3D calculateForce(PhysicsEntity entity);
    
    /**
     * перевірити чи поле впливає на сутність
     * @param entity фізична сутність
     * @return true якщо поле впливає на сутність
     */
    public boolean affects(PhysicsEntity entity) {
        double distance = position.distance(entity.getPosition());
        return distance <= radius;
    }
    
    /**
     * отримати позицію поля сили
     */
    public Vector3D getPosition() {
        return position;
    }
    
    /**
     * встановити позицію поля сили
     */
    public void setPosition(Vector3D position) {
        this.position = position;
    }
    
    /**
     * отримати силу поля
     */
    public double getStrength() {
        return strength;
    }
    
    /**
     * встановити силу поля
     */
    public void setStrength(double strength) {
        this.strength = strength;
    }
    
    /**
     * отримати радіус дії поля
     */
    public double getRadius() {
        return radius;
    }
    
    /**
     * встановити радіус дії поля
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }
    
    /**
     * отримати ім'я поля сили
     */
    public String getName() {
        return name;
    }
    
    /**
     * встановити ім'я поля сили
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * оновити поле сили
     * @param deltaTime час, що минув з останнього оновлення
     */
    public void update(double deltaTime) {
        // базова реалізація - нічого не робити
        // підкласи можуть перевизначити цей метод
    }
}