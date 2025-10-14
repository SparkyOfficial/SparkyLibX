package com.sparky.libx.geometry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.sparky.libx.math.Vector3D;

/**
 * Клас для представлення обмежувальної коробки в 3D просторі
 * @author Андрій Будильников
 */
public class BoundingBox {
    private double minX, minY, minZ;
    private double maxX, maxY, maxZ;
    
    /**
     * Створює обмежувальну коробку з вказаних координат
     */
    public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }
    
    /**
     * Створює обмежувальну коробку з двох точок
     */
    public BoundingBox(Vector3D point1, Vector3D point2) {
        this(point1.getX(), point1.getY(), point1.getZ(), point2.getX(), point2.getY(), point2.getZ());
    }
    
    /**
     * Створює обмежувальну коробку з двох точок Bukkit
     */
    public BoundingBox(Location loc1, Location loc2) {
        this(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
    }
    
    /**
     * Створює обмежувальну коробку з вектора Bukkit
     */
    public BoundingBox(Vector vec1, Vector vec2) {
        this(vec1.getX(), vec1.getY(), vec1.getZ(), vec2.getX(), vec2.getY(), vec2.getZ());
    }
    
    /**
     * Отримує мінімальну точку коробки
     */
    public Vector3D getMin() {
        return new Vector3D(minX, minY, minZ);
    }
    
    /**
     * Отримує максимальну точку коробки
     */
    public Vector3D getMax() {
        return new Vector3D(maxX, maxY, maxZ);
    }
    
    /**
     * Отримує центр коробки
     */
    public Vector3D getCenter() {
        return new Vector3D(
            (minX + maxX) / 2.0,
            (minY + maxY) / 2.0,
            (minZ + maxZ) / 2.0
        );
    }
    
    /**
     * Отримує ширину коробки (по X)
     */
    public double getWidth() {
        return maxX - minX;
    }
    
    /**
     * Отримує висоту коробки (по Y)
     */
    public double getHeight() {
        return maxY - minY;
    }
    
    /**
     * Отримує глибину коробки (по Z)
     */
    public double getDepth() {
        return maxZ - minZ;
    }
    
    /**
     * Отримує об'єм коробки
     */
    public double getVolume() {
        return getWidth() * getHeight() * getDepth();
    }
    
    /**
     * Перевіряє, чи містить точка в коробці
     */
    public boolean contains(Vector3D point) {
        return point.getX() >= minX && point.getX() <= maxX &&
               point.getY() >= minY && point.getY() <= maxY &&
               point.getZ() >= minZ && point.getZ() <= maxZ;
    }
    
    /**
     * Перевіряє, чи містить точку Bukkit
     */
    public boolean contains(Location location) {
        return location.getX() >= minX && location.getX() <= maxX &&
               location.getY() >= minY && location.getY() <= maxY &&
               location.getZ() >= minZ && location.getZ() <= maxZ;
    }
    
    /**
     * Перевіряє, чи перетинається з іншою коробкою
     */
    public boolean intersects(BoundingBox other) {
        return this.minX <= other.maxX && this.maxX >= other.minX &&
               this.minY <= other.maxY && this.maxY >= other.minY &&
               this.minZ <= other.maxZ && this.maxZ >= other.minZ;
    }
    
    /**
     * Об'єднує з іншою коробкою
     */
    public BoundingBox union(BoundingBox other) {
        return new BoundingBox(
            Math.min(this.minX, other.minX),
            Math.min(this.minY, other.minY),
            Math.min(this.minZ, other.minZ),
            Math.max(this.maxX, other.maxX),
            Math.max(this.maxY, other.maxY),
            Math.max(this.maxZ, other.maxZ)
        );
    }
    
    /**
     * Створює копію коробки
     */
    public BoundingBox clone() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    /**
     * Розширює коробку на вказану відстань
     */
    public BoundingBox expand(double x, double y, double z) {
        return new BoundingBox(
            minX - x, minY - y, minZ - z,
            maxX + x, maxY + y, maxZ + z
        );
    }
    
    /**
     * Розширює коробку рівномірно в усіх напрямках
     */
    public BoundingBox expand(double distance) {
        return expand(distance, distance, distance);
    }
    
    /**
     * Стискає коробку на вказану відстань
     */
    public BoundingBox contract(double x, double y, double z) {
        return new BoundingBox(
            minX + x, minY + y, minZ + z,
            maxX - x, maxY - y, maxZ - z
        );
    }
    
    /**
     * Стискає коробку рівномірно в усіх напрямках
     */
    public BoundingBox contract(double distance) {
        return contract(distance, distance, distance);
    }
    
    /**
     * Переміщує коробку на вказаний вектор
     */
    public BoundingBox shift(Vector3D vector) {
        return new BoundingBox(
            minX + vector.getX(), minY + vector.getY(), minZ + vector.getZ(),
            maxX + vector.getX(), maxY + vector.getY(), maxZ + vector.getZ()
        );
    }
    
    /**
     * Переміщує коробку на вказані координати
     */
    public BoundingBox shift(double x, double y, double z) {
        return shift(new Vector3D(x, y, z));
    }
    
    @Override
    public String toString() {
        return String.format(
            "BoundingBox{min=[%.2f, %.2f, %.2f], max=[%.2f, %.2f, %.2f]}",
            minX, minY, minZ, maxX, maxY, maxZ
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BoundingBox that = (BoundingBox) obj;
        return Double.compare(that.minX, minX) == 0 &&
               Double.compare(that.minY, minY) == 0 &&
               Double.compare(that.minZ, minZ) == 0 &&
               Double.compare(that.maxX, maxX) == 0 &&
               Double.compare(that.maxY, maxY) == 0 &&
               Double.compare(that.maxZ, maxZ) == 0;
    }
    
    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(minX);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minZ);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxX);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxZ);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}