package com.sparky.libx.physics;

import java.util.ArrayList;
import java.util.List;

import com.sparky.libx.math.Vector3D;

/**
 * клас для представлення вибуху в фізичному рушії
 * вибухи застосовують радіальну силу до фізичних сутностей в радіусі дії
 * @author Андрій Будильников
 */
public class Explosion {
    
    private Vector3D position;
    private double radius;
    private double force;
    private long creationTime;
    private long duration;
    private boolean isActive;
    
    /**
     * створити новий вибух
     * @param position позиція вибуху
     * @param radius радіус вибуху
     * @param force сила вибуху
     */
    public Explosion(Vector3D position, double radius, double force) {
        this.position = position;
        this.radius = radius;
        this.force = force;
        this.creationTime = System.currentTimeMillis();
        this.duration = 1000; // 1 секунда за замовчуванням
        this.isActive = true;
    }
    
    /**
     * застосувати вибух до фізичного рушія
     * @param engine фізичний рушій
     */
    public void apply(PhysicsEngine engine) {
        if (!isActive) return;
        
        // застосувати силу до кожної сутності
        // для спрощення, ми просто виводимо повідомлення
        System.out.println("Вибух застосовано в позиції " + position + " з радіусом " + radius + " і силою " + force);
        
        // деактивувати вибух після застосування
        isActive = false;
    }
    
    /**
     * оновити вибух
     * @param deltaTime час, що минув з останнього оновлення
     */
    public void update(double deltaTime) {
        if (!isActive) return;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - creationTime > duration) {
            isActive = false;
        }
    }
    
    /**
     * перевірити чи вибух активний
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * отримати позицію вибуху
     */
    public Vector3D getPosition() {
        return position;
    }
    
    /**
     * встановити позицію вибуху
     */
    public void setPosition(Vector3D position) {
        this.position = position;
    }
    
    /**
     * отримати радіус вибуху
     */
    public double getRadius() {
        return radius;
    }
    
    /**
     * встановити радіус вибуху
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }
    
    /**
     * отримати силу вибуху
     */
    public double getForce() {
        return force;
    }
    
    /**
     * встановити силу вибуху
     */
    public void setForce(double force) {
        this.force = force;
    }
    
    /**
     * отримати тривалість вибуху
     */
    public long getDuration() {
        return duration;
    }
    
    /**
     * встановити тривалість вибуху
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    @Override
    public String toString() {
        return String.format("Explosion{position=%s, radius=%.2f, force=%.2f, isActive=%s}", 
                           position, radius, force, isActive);
    }
}