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
     * Вычитает другой вектор из этого вектора
     * @param other другой вектор
     * @return результат вычитания векторов
     */
    public Vector3D subtract(Vector3D other) {
        return new Vector3D(x - other.x, y - other.y, z - other.z);
    }
    
    /**
     * Умножает этот вектор на скаляр
     * @param scalar скаляр
     * @return результат умножения вектора на скаляр
     */
    public Vector3D multiply(double scalar) {
        return new Vector3D(x * scalar, y * scalar, z * scalar);
    }
    
    /**
     * Делит этот вектор на скаляр
     * @param scalar скаляр
     * @return результат деления вектора на скаляр
     */
    public Vector3D divide(double scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Невозможно разделить на ноль");
        }
        return new Vector3D(x / scalar, y / scalar, z / scalar);
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
     * Вычисляет векторное произведение этого вектора с другим вектором
     * @param other другой вектор
     * @return векторное произведение
     */
    public Vector3D cross(Vector3D other) {
        return new Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }
    
    /**
     * Вычисляет квадрат магнитуды (длины) вектора
     * @return квадрат магнитуды вектора
     */
    public double magnitudeSquared() {
        return x * x + y * y + z * z;
    }
    
    /**
     * Вычисляет магнитуду (длину) вектора
     * @return магнитуда вектора
     */
    public double magnitude() {
        return Math.sqrt(magnitudeSquared());
    }
    
    /**
     * Нормализует этот вектор (делает его единичной длины)
     * @return нормализованный вектор
     */
    public Vector3D normalize() {
        double mag = magnitude();
        if (mag == 0) {
            return new Vector3D(0, 0, 0);
        }
        return divide(mag);
    }
    
    /**
     * Преобразует этот вектор в вектор Bukkit
     * @return вектор Bukkit
     */
    public Vector toBukkitVector() {
        return new Vector(x, y, z);
    }
    
    /**
     * Создает копию этого вектора
     * @return копия вектора
     */
    public Vector3D copy() {
        return new Vector3D(x, y, z);
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