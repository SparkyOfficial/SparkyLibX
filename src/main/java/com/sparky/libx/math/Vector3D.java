package com.sparky.libx.math;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Трехмерный вектор для математических операций
 * Предоставляет методы для работы с трехмерными координатами
 */
public class Vector3D {
    private final double x, y, z;
    
    /**
     * Создает новый вектор с указанными координатами
     * @param x координата X
     * @param y координата Y
     * @param z координата Z
     */
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Создает новый вектор из местоположения Bukkit
     * @param location местоположение Bukkit
     */
    public Vector3D(Location location) {
        this(location.getX(), location.getY(), location.getZ());
    }
    
    /**
     * Создает новый вектор из вектора Bukkit
     * @param vector вектор Bukkit
     */
    public Vector3D(Vector vector) {
        this(vector.getX(), vector.getY(), vector.getZ());
    }
    
    /**
     * Получает координату X
     * @return координата X
     */
    public double getX() {
        return x;
    }
    
    /**
     * Получает координату Y
     * @return координата Y
     */
    public double getY() {
        return y;
    }
    
    /**
     * Получает координату Z
     * @return координата Z
     */
    public double getZ() {
        return z;
    }
    
    /**
     * Вычисляет линейную интерполяцию между этим вектором и другим вектором
     * @param other другой вектор
     * @param t параметр интерполяции (0.0 = этот вектор, 1.0 = другой вектор)
     * @return интерполированный вектор
     */
    public Vector3D lerp(Vector3D other, double t) {
        return new Vector3D(
            x + (other.x - x) * t,
            y + (other.y - y) * t,
            z + (other.z - z) * t
        );
    }
    
    /**
     * Вычисляет расстояние до другого вектора
     * @param other другой вектор
     * @return расстояние до другого вектора
     */
    public double distance(Vector3D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * Складывает этот вектор с другим вектором
     * @param other другой вектор
     * @return результат сложения векторов
     */
    public Vector3D add(Vector3D other) {
        return new Vector3D(x + other.x, y + other.y, z + other.z);
    }
    
    /**
     * Вычисляет скалярное произведение этого вектора с другим вектором
     * @param other другой вектор
     * @return скалярное произведение
     */
    public double dot(Vector3D other) {
        return x * other.x + y * other.y + z * other.z;
    }
    
    /**
     * Преобразует этот вектор в вектор Bukkit
     * @return вектор Bukkit
     */
    public Vector toBukkitVector() {
        return new Vector(x, y, z);
    }
    
    @Override
    public String toString() {
        return "Vector3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}