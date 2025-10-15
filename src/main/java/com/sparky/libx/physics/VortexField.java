package com.sparky.libx.physics;

import com.sparky.libx.math.Vector3D;

/**
 * вихрове поле, яке створює обертову силу навколо центральної осі
 * @author Андрій Будильников
 */
public class VortexField extends ForceField {
    
    private Vector3D axis;
    private double angularVelocity;
    private double inwardForce;
    
    /**
     * створити нове вихрове поле
     * @param position позиція поля
     * @param strength сила поля
     * @param radius радіус дії
     * @param axis вісь обертання
     * @param angularVelocity кутова швидкість
     * @param inwardForce сила, що притягує до центру
     */
    public VortexField(Vector3D position, double strength, double radius, Vector3D axis, 
                      double angularVelocity, double inwardForce) {
        super(position, strength, radius);
        this.axis = axis.normalize();
        this.angularVelocity = angularVelocity;
        this.inwardForce = inwardForce;
        this.setName("VortexField");
    }
    
    /**
     * обчислити вихрову силу, яку поле застосовує до сутності
     * @param entity фізична сутність
     * @return вектор вихрової сили
     */
    @Override
    public Vector3D calculateForce(PhysicsEntity entity) {
        if (!affects(entity)) {
            return new Vector3D(0, 0, 0);
        }
        
        // обчислити вектор від центру поля до сутності
        Vector3D offset = entity.getPosition().subtract(getPosition());
        
        // проєкція на вісь обертання
        double projection = offset.dot(axis);
        Vector3D axisComponent = axis.multiply(projection);
        
        // радіальна компонента (перпендикулярна до осі)
        Vector3D radialComponent = offset.subtract(axisComponent);
        
        // обчислити тангенціальну компоненту (перпендикулярну до радіальної і осі)
        Vector3D tangentialComponent = axis.cross(radialComponent);
        
        if (tangentialComponent.magnitude() <= 0) {
            return new Vector3D(0, 0, 0);
        }
        
        // нормалізувати тангенціальну компоненту
        tangentialComponent = tangentialComponent.normalize();
        
        // обчислити радіальну відстань
        double radialDistance = radialComponent.magnitude();
        
        // обчислити силу на основі відстані
        double distanceFactor = 1.0;
        if (getRadius() > 0) {
            distanceFactor = 1.0 - Math.min(1.0, radialDistance / getRadius());
        }
        
        // тангенціальна сила (обертання)
        double tangentialForce = getStrength() * angularVelocity * radialDistance * distanceFactor;
        
        // радіальна сила (притягання до центру)
        double radialForce = getStrength() * inwardForce * distanceFactor;
        
        // комбінувати сили
        Vector3D tangentialForceVector = tangentialComponent.multiply(tangentialForce);
        Vector3D radialForceVector = radialComponent.normalize().multiply(-radialForce);
        
        return tangentialForceVector.add(radialForceVector);
    }
    
    /**
     * отримати вісь обертання
     */
    public Vector3D getAxis() {
        return axis;
    }
    
    /**
     * встановити вісь обертання
     */
    public void setAxis(Vector3D axis) {
        this.axis = axis.normalize();
    }
    
    /**
     * отримати кутову швидкість
     */
    public double getAngularVelocity() {
        return angularVelocity;
    }
    
    /**
     * встановити кутову швидкість
     */
    public void setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }
    
    /**
     * отримати силу, що притягує до центру
     */
    public double getInwardForce() {
        return inwardForce;
    }
    
    /**
     * встановити силу, що притягує до центру
     */
    public void setInwardForce(double inwardForce) {
        this.inwardForce = inwardForce;
    }
    
    @Override
    public String toString() {
        return String.format("VortexField{position=%s, strength=%.2f, radius=%.2f, axis=%s, angularVelocity=%.2f, inwardForce=%.2f}", 
                           getPosition(), getStrength(), getRadius(), axis, angularVelocity, inwardForce);
    }
}