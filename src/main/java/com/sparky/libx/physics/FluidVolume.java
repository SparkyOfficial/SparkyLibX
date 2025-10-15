package com.sparky.libx.physics;

import java.util.UUID;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.geometry.BoundingBox;

/**
 * клас для представлення об'єму рідини в фізичному рушії
 * рідини мають густину, коефіцієнт опору і можуть впливати на фізичні сутності
 * @author Андрій Будильников
 */
public class FluidVolume {
    
    private final UUID id;
    private BoundingBox bounds;
    private double density;
    private double dragCoefficient;
    private String name;
    
    /**
     * створити новий об'єм рідини
     * @param bounds межі об'єму рідини
     * @param density густина рідини (кг/м³)
     * @param dragCoefficient коефіцієнт опору рідини
     */
    public FluidVolume(BoundingBox bounds, double density, double dragCoefficient) {
        this.id = UUID.randomUUID();
        this.bounds = bounds;
        this.density = density;
        this.dragCoefficient = dragCoefficient;
        this.name = "FluidVolume";
    }
    
    /**
     * отримати унікальний ідентифікатор об'єму рідини
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * перевірити чи позиція знаходиться в об'ємі рідини
     * @param position позиція для перевірки
     * @return true якщо позиція в об'ємі рідини
     */
    public boolean contains(Vector3D position) {
        return bounds.contains(position);
    }
    
    /**
     * отримати межі об'єму рідини
     */
    public BoundingBox getBounds() {
        return bounds;
    }
    
    /**
     * встановити межі об'єму рідини
     */
    public void setBounds(BoundingBox bounds) {
        this.bounds = bounds;
    }
    
    /**
     * отримати густину рідини
     */
    public double getDensity() {
        return density;
    }
    
    /**
     * встановити густину рідини
     */
    public void setDensity(double density) {
        if (density < 0) {
            throw new IllegalArgumentException("Густина не може бути від'ємною");
        }
        this.density = density;
    }
    
    /**
     * отримати коефіцієнт опору рідини
     */
    public double getDragCoefficient() {
        return dragCoefficient;
    }
    
    /**
     * встановити коефіцієнт опору рідини
     */
    public void setDragCoefficient(double dragCoefficient) {
        this.dragCoefficient = dragCoefficient;
    }
    
    /**
     * отримати ім'я об'єму рідини
     */
    public String getName() {
        return name;
    }
    
    /**
     * встановити ім'я об'єму рідини
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * обчислити об'єм рідини
     */
    public double getVolume() {
        return bounds.getVolume();
    }
    
    /**
     * отримати центр об'єму рідини
     */
    public Vector3D getCenter() {
        return bounds.getCenter();
    }
    
    /**
     * перевірити чи об'єм рідини перетинається з іншим об'ємом
     * @param other інший об'єм рідини
     * @return true якщо об'єми перетинаються
     */
    public boolean intersects(FluidVolume other) {
        return bounds.intersects(other.bounds);
    }
    
    @Override
    public String toString() {
        return String.format("FluidVolume{name='%s', bounds=%s, density=%.2f}", 
                           name, bounds, density);
    }
}