package com.sparky.libx.math;

/**
 * Кватернион для 3D вращений
 */
public class Quaternion {
    private final double w, x, y, z;
    
    /**
     * Создает новый кватернион
     */
    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Создает единичный кватернион
     */
    public static Quaternion identity() {
        return new Quaternion(1, 0, 0, 0);
    }
    
    /**
     * Создает кватернион из угла поворота и оси
     */
    public static Quaternion fromAxisAngle(Vector3D axis, double angle) {
        double halfAngle = angle / 2.0;
        double sinHalfAngle = Math.sin(halfAngle);
        double cosHalfAngle = Math.cos(halfAngle);
        
        return new Quaternion(
            cosHalfAngle,
            axis.getX() * sinHalfAngle,
            axis.getY() * sinHalfAngle,
            axis.getZ() * sinHalfAngle
        );
    }
    
    /**
     * Создает кватернион из углов Эйлера
     */
    public static Quaternion fromEulerAngles(double roll, double pitch, double yaw) {
        double cr = Math.cos(roll * 0.5);
        double sr = Math.sin(roll * 0.5);
        double cp = Math.cos(pitch * 0.5);
        double sp = Math.sin(pitch * 0.5);
        double cy = Math.cos(yaw * 0.5);
        double sy = Math.sin(yaw * 0.5);
        
        double w = cr * cp * cy + sr * sp * sy;
        double x = sr * cp * cy - cr * sp * sy;
        double y = cr * sp * cy + sr * cp * sy;
        double z = cr * cp * sy - sr * sp * cy;
        
        return new Quaternion(w, x, y, z);
    }
    
    /**
     * Складывает два кватерниона
     */
    public Quaternion add(Quaternion other) {
        return new Quaternion(
            this.w + other.w,
            this.x + other.x,
            this.y + other.y,
            this.z + other.z
        );
    }
    
    /**
     * Вычитает один кватернион из другого
     */
    public Quaternion subtract(Quaternion other) {
        return new Quaternion(
            this.w - other.w,
            this.x - other.x,
            this.y - other.y,
            this.z - other.z
        );
    }
    
    /**
     * Умножает два кватерниона
     */
    public Quaternion multiply(Quaternion other) {
        return new Quaternion(
            this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z,
            this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y,
            this.w * other.y - this.x * other.z + this.y * other.w + this.z * other.x,
            this.w * other.z + this.x * other.y - this.y * other.x + this.z * other.w
        );
    }
    
    /**
     * Умножает кватернион на скаляр
     */
    public Quaternion multiply(double scalar) {
        return new Quaternion(
            this.w * scalar,
            this.x * scalar,
            this.y * scalar,
            this.z * scalar
        );
    }
    
    /**
     * Вычисляет сопряженный кватернион
     */
    public Quaternion conjugate() {
        return new Quaternion(w, -x, -y, -z);
    }
    
    /**
     * Вычисляет норму кватерниона
     */
    public double norm() {
        return Math.sqrt(w * w + x * x + y * y + z * z);
    }
    
    /**
     * Нормализует кватернион
     */
    public Quaternion normalize() {
        double norm = norm();
        if (norm == 0) {
            return identity();
        }
        return multiply(1.0 / norm);
    }
    
    /**
     * Вычисляет обратный кватернион
     */
    public Quaternion inverse() {
        double normSquared = w * w + x * x + y * y + z * z;
        if (normSquared == 0) {
            throw new ArithmeticException("Невозможно вычислить обратный кватернион для нулевого кватерниона");
        }
        return conjugate().multiply(1.0 / normSquared);
    }
    
    /**
     * Интерполирует между двумя кватернионами (SLERP)
     */
    public Quaternion slerp(Quaternion other, double t) {
        double dot = this.w * other.w + this.x * other.x + this.y * other.y + this.z * other.z;
        
        if (dot < 0) {
            other = other.multiply(-1);
            dot = -dot;
        }
        
        if (dot > 0.9995) {
            return this.add(other.subtract(this).multiply(t)).normalize();
        }
        
        double theta0 = Math.acos(dot);
        double theta = theta0 * t;
        double sinTheta = Math.sin(theta);
        double sinTheta0 = Math.sin(theta0);
        
        double s0 = Math.cos(theta) - dot * sinTheta / sinTheta0;
        double s1 = sinTheta / sinTheta0;
        
        return this.multiply(s0).add(other.multiply(s1)).normalize();
    }
    
    /**
     * Преобразует кватернион в углы Эйлера
     */
    public double[] toEulerAngles() {
        double[] angles = new double[3];
        
        double sinr_cosp = 2 * (w * x + y * z);
        double cosr_cosp = 1 - 2 * (x * x + y * y);
        angles[0] = Math.atan2(sinr_cosp, cosr_cosp);
        
        double sinp = 2 * (w * y - z * x);
        if (Math.abs(sinp) >= 1) {
            angles[1] = Math.copySign(Math.PI / 2, sinp);
        } else {
            angles[1] = Math.asin(sinp);
        }
        
        double siny_cosp = 2 * (w * z + x * y);
        double cosy_cosp = 1 - 2 * (y * y + z * z);
        angles[2] = Math.atan2(siny_cosp, cosy_cosp);
        
        return angles;
    }
    
    public double getW() { return w; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    
    @Override
    public String toString() {
        return String.format("Quaternion(%.3f, %.3f, %.3f, %.3f)", w, x, y, z);
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