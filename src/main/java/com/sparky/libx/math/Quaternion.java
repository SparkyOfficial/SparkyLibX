package com.sparky.libx.math;

/**
 * Клас для представлення кватерніонів для 3D обертань
 * @author Андрій Будильников
 */
public class Quaternion {
    private double w, x, y, z;
    
    /**
     * Створює кватерніон з вказаних компонентів
     */
    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Створює одиничний кватерніон
     */
    public Quaternion() {
        this(1, 0, 0, 0);
    }
    
    /**
     * Створює кватерніон з вектора обертання
     */
    public Quaternion(Vector3D axis, double angle) {
        double halfAngle = angle / 2.0;
        double sin = Math.sin(halfAngle);
        double cos = Math.cos(halfAngle);
        
        this.w = cos;
        this.x = axis.getX() * sin;
        this.y = axis.getY() * sin;
        this.z = axis.getZ() * sin;
    }
    
    /**
     * Отримує компонент W
     */
    public double getW() {
        return w;
    }
    
    /**
     * Отримує компонент X
     */
    public double getX() {
        return x;
    }
    
    /**
     * Отримує компонент Y
     */
    public double getY() {
        return y;
    }
    
    /**
     * Отримує компонент Z
     */
    public double getZ() {
        return z;
    }
    
    /**
     * Обчислює довжину кватерніона
     */
    public double length() {
        return Math.sqrt(w * w + x * x + y * y + z * z);
    }
    
    /**
     * Нормалізує кватерніон
     */
    public Quaternion normalize() {
        double length = length();
        if (length == 0) return new Quaternion();
        return new Quaternion(w / length, x / length, y / length, z / length);
    }
    
    /**
     * Обчислює спряжений кватерніон
     */
    public Quaternion conjugate() {
        return new Quaternion(w, -x, -y, -z);
    }
    
    /**
     * Обчислює обернений кватерніон
     */
    public Quaternion inverse() {
        double lengthSquared = w * w + x * x + y * y + z * z;
        if (lengthSquared == 0) return new Quaternion();
        return conjugate().multiply(1.0 / lengthSquared);
    }
    
    /**
     * Множить кватерніон на скаляр
     */
    public Quaternion multiply(double scalar) {
        return new Quaternion(w * scalar, x * scalar, y * scalar, z * scalar);
    }
    
    /**
     * Множить кватерніон на інший кватерніон
     */
    public Quaternion multiply(Quaternion other) {
        return new Quaternion(
            w * other.w - x * other.x - y * other.y - z * other.z,
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w
        );
    }
    
    /**
     * Додає кватерніон до іншого кватерніона
     */
    public Quaternion add(Quaternion other) {
        return new Quaternion(w + other.w, x + other.x, y + other.y, z + other.z);
    }
    
    /**
     * Віднімає інший кватерніон від цього
     */
    public Quaternion subtract(Quaternion other) {
        return new Quaternion(w - other.w, x - other.x, y - other.y, z - other.z);
    }
    
    /**
     * Інтерполює між двома кватерніонами (SLERP)
     */
    public Quaternion slerp(Quaternion other, double t) {
        double dot = dot(other);
        
        // Якщо кватерніони протилежні, обертаємо один з них
        if (dot < 0.0) {
            other = other.multiply(-1);
            dot = -dot;
        }
        
        // Якщо кватерніони дуже близькі, використовуємо лінійну інтерполяцію
        if (dot > 0.9995) {
            return nlerp(other, t);
        }
        
        double theta = Math.acos(dot);
        double sinTheta = Math.sin(theta);
        
        double scale0 = Math.sin((1.0 - t) * theta) / sinTheta;
        double scale1 = Math.sin(t * theta) / sinTheta;
        
        return new Quaternion(
            scale0 * w + scale1 * other.w,
            scale0 * x + scale1 * other.x,
            scale0 * y + scale1 * other.y,
            scale0 * z + scale1 * other.z
        );
    }
    
    /**
     * Нормалізована лінійна інтерполяція між двома кватерніонами
     */
    public Quaternion nlerp(Quaternion other, double t) {
        double dot = dot(other);
        
        double scale0 = 1.0 - t;
        double scale1 = (dot >= 0) ? t : -t;
        
        Quaternion result = new Quaternion(
            scale0 * w + scale1 * other.w,
            scale0 * x + scale1 * other.x,
            scale0 * y + scale1 * other.y,
            scale0 * z + scale1 * other.z
        );
        
        return result.normalize();
    }
    
    /**
     * Обчислює скалярний добуток з іншим кватерніоном
     */
    public double dot(Quaternion other) {
        return w * other.w + x * other.x + y * other.y + z * other.z;
    }
    
    /**
     * Поворотує вектор за допомогою кватерніона
     */
    public Vector3D rotate(Vector3D vector) {
        Quaternion vectorQuat = new Quaternion(0, vector.getX(), vector.getY(), vector.getZ());
        Quaternion result = multiply(vectorQuat).multiply(inverse());
        return new Vector3D(result.x, result.y, result.z);
    }
    
    /**
     * Створює кватерніон обертання навколо осі X
     */
    public static Quaternion rotationX(double angle) {
        double halfAngle = angle / 2.0;
        return new Quaternion(Math.cos(halfAngle), Math.sin(halfAngle), 0, 0);
    }
    
    /**
     * Створює кватерніон обертання навколо осі Y
     */
    public static Quaternion rotationY(double angle) {
        double halfAngle = angle / 2.0;
        return new Quaternion(Math.cos(halfAngle), 0, Math.sin(halfAngle), 0);
    }
    
    /**
     * Створює кватерніон обертання навколо осі Z
     */
    public static Quaternion rotationZ(double angle) {
        double halfAngle = angle / 2.0;
        return new Quaternion(Math.cos(halfAngle), 0, 0, Math.sin(halfAngle));
    }
    
    /**
     * Створює кватерніон з кутів Ейлера (в радіанах)
     */
    public static Quaternion fromEulerAngles(double yaw, double pitch, double roll) {
        double cy = Math.cos(yaw * 0.5);
        double sy = Math.sin(yaw * 0.5);
        double cp = Math.cos(pitch * 0.5);
        double sp = Math.sin(pitch * 0.5);
        double cr = Math.cos(roll * 0.5);
        double sr = Math.sin(roll * 0.5);
        
        return new Quaternion(
            cr * cp * cy + sr * sp * sy,
            sr * cp * cy - cr * sp * sy,
            cr * sp * cy + sr * cp * sy,
            cr * cp * sy - sr * sp * cy
        );
    }
    
    @Override
    public String toString() {
        return String.format("Quaternion{w=%.3f, x=%.3f, y=%.3f, z=%.3f}", w, x, y, z);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Quaternion that = (Quaternion) obj;
        return Double.compare(that.w, w) == 0 &&
               Double.compare(that.x, x) == 0 &&
               Double.compare(that.y, y) == 0 &&
               Double.compare(that.z, z) == 0;
    }
    
    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(w);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(x);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}