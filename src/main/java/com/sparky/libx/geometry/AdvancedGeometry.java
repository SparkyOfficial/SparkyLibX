package com.sparky.libx.geometry;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;

/**
 * Расширенные геометрические операции и генерация 3D форм
 * @author Андрій Будильников
 */
public class AdvancedGeometry {
    
    /**
     * Создает точки по спирали
     * @param center центр спирали
     * @param radius радиус спирали
     * @param height высота спирали
     * @param turns количество витков
     * @param points количество точек
     * @return список точек спирали
     */
    public static List<Vector> createSpiral(Vector center, double radius, double height, double turns, int points) {
        List<Vector> spiralPoints = new ArrayList<>();
        double angleStep = 2 * Math.PI * turns / points;
        double heightStep = height / points;
        
        for (int i = 0; i < points; i++) {
            double angle = i * angleStep;
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + i * heightStep;
            double z = center.getZ() + radius * Math.sin(angle);
            spiralPoints.add(new Vector(x, y, z));
        }
        
        return spiralPoints;
    }
    
    /**
     * Создает точки по тору
     * @param center центр тора
     * @param majorRadius большой радиус (расстояние от центра до центра трубки)
     * @param minorRadius малый радиус (радиус трубки)
     * @param majorPoints количество точек по большой окружности
     * @param minorPoints количество точек по малой окружности
     * @return список точек тора
     */
    public static List<Vector> createTorus(Vector center, double majorRadius, double minorRadius, 
                                          int majorPoints, int minorPoints) {
        List<Vector> torusPoints = new ArrayList<>();
        double majorAngleStep = 2 * Math.PI / majorPoints;
        double minorAngleStep = 2 * Math.PI / minorPoints;
        
        for (int i = 0; i < majorPoints; i++) {
            double majorAngle = i * majorAngleStep;
            double centerX = center.getX() + majorRadius * Math.cos(majorAngle);
            double centerZ = center.getZ() + majorRadius * Math.sin(majorAngle);
            
            for (int j = 0; j < minorPoints; j++) {
                double minorAngle = j * minorAngleStep;
                double x = centerX + minorRadius * Math.cos(majorAngle) * Math.cos(minorAngle);
                double y = center.getY() + minorRadius * Math.sin(minorAngle);
                double z = centerZ + minorRadius * Math.sin(majorAngle) * Math.cos(minorAngle);
                torusPoints.add(new Vector(x, y, z));
            }
        }
        
        return torusPoints;
    }
    
    /**
     * Создает точки по конусу
     * @param baseCenter центр основания конуса
     * @param baseRadius радиус основания
     * @param height высота конуса
     * @param points количество точек в основании
     * @return список точек конуса
     */
    public static List<Vector> createCone(Vector baseCenter, double baseRadius, double height, int points) {
        List<Vector> conePoints = new ArrayList<>();
        
        Vector apex = new Vector(baseCenter.getX(), baseCenter.getY() + height, baseCenter.getZ());
        conePoints.add(apex);
        
        double angleStep = 2 * Math.PI / points;
        for (int i = 0; i < points; i++) {
            double angle = i * angleStep;
            double x = baseCenter.getX() + baseRadius * Math.cos(angle);
            double z = baseCenter.getZ() + baseRadius * Math.sin(angle);
            conePoints.add(new Vector(x, baseCenter.getY(), z));
        }
        
        return conePoints;
    }
    
    /**
     * Создает точки по пирамиде
     * @param baseCenter центр основания пирамиды
     * @param baseSize размер основания (длина стороны квадрата)
     * @param height высота пирамиды
     * @return список точек пирамиды
     */
    public static List<Vector> createPyramid(Vector baseCenter, double baseSize, double height) {
        List<Vector> pyramidPoints = new ArrayList<>();
        
        Vector apex = new Vector(baseCenter.getX(), baseCenter.getY() + height, baseCenter.getZ());
        pyramidPoints.add(apex);
        
        double halfSize = baseSize / 2;
        pyramidPoints.add(new Vector(baseCenter.getX() - halfSize, baseCenter.getY(), baseCenter.getZ() - halfSize));
        pyramidPoints.add(new Vector(baseCenter.getX() + halfSize, baseCenter.getY(), baseCenter.getZ() - halfSize));
        pyramidPoints.add(new Vector(baseCenter.getX() + halfSize, baseCenter.getY(), baseCenter.getZ() + halfSize));
        pyramidPoints.add(new Vector(baseCenter.getX() - halfSize, baseCenter.getY(), baseCenter.getZ() + halfSize));
        
        return pyramidPoints;
    }
    
    /**
     * Создает точки по цилиндру
     * @param baseCenter центр основания цилиндра
     * @param radius радиус цилиндра
     * @param height высота цилиндра
     * @param points количество точек в основании
     * @return список точек цилиндра
     */
    public static List<Vector> createCylinder(Vector baseCenter, double radius, double height, int points) {
        List<Vector> cylinderPoints = new ArrayList<>();
        double angleStep = 2 * Math.PI / points;
        
        for (int i = 0; i < points; i++) {
            double angle = i * angleStep;
            double x = baseCenter.getX() + radius * Math.cos(angle);
            double z = baseCenter.getZ() + radius * Math.sin(angle);
            cylinderPoints.add(new Vector(x, baseCenter.getY(), z));
        }
        
        for (int i = 0; i < points; i++) {
            double angle = i * angleStep;
            double x = baseCenter.getX() + radius * Math.cos(angle);
            double z = baseCenter.getZ() + radius * Math.sin(angle);
            cylinderPoints.add(new Vector(x, baseCenter.getY() + height, z));
        }
        
        return cylinderPoints;
    }
    
    /**
     * Вычисляет центр тяжести набора точек
     * @param points список точек
     * @return центр тяжести
     */
    public static Vector calculateCentroid(List<Vector> points) {
        if (points.isEmpty()) {
            return new Vector(0, 0, 0);
        }
        
        double sumX = 0, sumY = 0, sumZ = 0;
        for (Vector point : points) {
            sumX += point.getX();
            sumY += point.getY();
            sumZ += point.getZ();
        }
        
        int count = points.size();
        return new Vector(sumX / count, sumY / count, sumZ / count);
    }
    
    /**
     * Поворачивает точку вокруг оси X
     * @param point точка для поворота
     * @param angle угол поворота в радианах
     * @return повернутая точка
     */
    public static Vector rotateAroundX(Vector point, double angle) {
        double y = point.getY() * Math.cos(angle) - point.getZ() * Math.sin(angle);
        double z = point.getY() * Math.sin(angle) + point.getZ() * Math.cos(angle);
        return new Vector(point.getX(), y, z);
    }
    
    /**
     * Поворачивает точку вокруг оси Y
     * @param point точка для поворота
     * @param angle угол поворота в радианах
     * @return повернутая точка
     */
    public static Vector rotateAroundY(Vector point, double angle) {
        double x = point.getX() * Math.cos(angle) + point.getZ() * Math.sin(angle);
        double z = -point.getX() * Math.sin(angle) + point.getZ() * Math.cos(angle);
        return new Vector(x, point.getY(), z);
    }
    
    /**
     * Поворачивает точку вокруг оси Z
     * @param point точка для поворота
     * @param angle угол поворота в радианах
     * @return повернутая точка
     */
    public static Vector rotateAroundZ(Vector point, double angle) {
        double x = point.getX() * Math.cos(angle) - point.getY() * Math.sin(angle);
        double y = point.getX() * Math.sin(angle) + point.getY() * Math.cos(angle);
        return new Vector(x, y, point.getZ());
    }
    
    /**
     * Масштабирует точку относительно центра
     * @param point точка для масштабирования
     * @param center центр масштабирования
     * @param scaleX масштаб по оси X
     * @param scaleY масштаб по оси Y
     * @param scaleZ масштаб по оси Z
     * @return масштабированная точка
     */
    public static Vector scale(Vector point, Vector center, double scaleX, double scaleY, double scaleZ) {
        double x = center.getX() + (point.getX() - center.getX()) * scaleX;
        double y = center.getY() + (point.getY() - center.getY()) * scaleY;
        double z = center.getZ() + (point.getZ() - center.getZ()) * scaleZ;
        return new Vector(x, y, z);
    }
    
    /**
     * Создает фрактал Мандельброта в 2D (проекция на плоскость XZ)
     * @param centerX центр по оси X
     * @param centerZ центр по оси Z
     * @param scale масштаб
     * @param width ширина области
     * @param height высота области
     * @param maxIterations максимальное количество итераций
     * @return список точек фрактала
     */
    public static List<Vector> createMandelbrotFractal(double centerX, double centerZ, double scale, 
                                                      int width, int height, int maxIterations) {
        List<Vector> fractalPoints = new ArrayList<>();
        double scaleX = scale / width;
        double scaleZ = scale / height;
        
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                double cx = centerX + (x - width / 2.0) * scaleX;
                double cz = centerZ + (z - height / 2.0) * scaleZ;
                
                double zx = 0;
                double zz = 0;
                int iterations = 0;
                
                while (zx * zx + zz * zz < 4 && iterations < maxIterations) {
                    double temp = zx * zx - zz * zz + cx;
                    zz = 2 * zx * zz + cz;
                    zx = temp;
                    iterations++;
                }
                
                if (iterations == maxIterations) {
                    fractalPoints.add(new Vector(cx, 0, cz));
                }
            }
        }
        
        return fractalPoints;
    }
}