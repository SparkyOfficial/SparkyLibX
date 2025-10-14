package com.sparky.libx.geometry;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;

/**
 * Утилиты для работы с геометрическими формами
 * @author Андрій Будильников
 */
public class ShapeUtils {
    
    /**
     * Создает линию из точек между двумя векторами
     * @param start начальная точка
     * @param end конечная точка
     * @param stepSize размер шага между точками
     * @return список точек линии
     */
    public static List<Vector> getLinePoints(Vector start, Vector end, double stepSize) {
        List<Vector> points = new ArrayList<>();
        
        double distance = start.distance(end);
        Vector direction = end.clone().subtract(start).normalize().multiply(stepSize);
        
        for (double d = 0; d <= distance; d += stepSize) {
            points.add(start.clone().add(direction.clone().multiply(d / stepSize)));
        }
        
        if (!points.contains(end)) {
            points.add(end.clone());
        }
        
        return points;
    }
    
    /**
     * Генерирует точки по окружности
     * @param center центр окружности
     * @param radius радиус
     * @param points сколько точек создать
     * @return список точек окружности
     */
    public static List<Vector> getCirclePoints(Vector center, double radius, int points) {
        List<Vector> circlePoints = new ArrayList<>();
        double angleStep = 2 * Math.PI / points;
        
        for (int i = 0; i < points; i++) {
            double angle = i * angleStep;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            circlePoints.add(new Vector(x, center.getY(), z));
        }
        
        return circlePoints;
    }
    
    /**
     * Создает точки на поверхности сферы
     * @param center центр сферы
     * @param radius радиус сферы
     * @param points количество точек
     * @return точки на сфере
     */
    public static List<Vector> getSpherePoints(Vector center, double radius, int points) {
        List<Vector> spherePoints = new ArrayList<>();
        
        double phi = Math.PI * (3.0 - Math.sqrt(5.0));

        
        for (int i = 0; i < points; i++) {
            double y = 1 - (i / (double) (points - 1)) * 2;
            double radiusAtY = Math.sqrt(1 - y * y);
            double theta = phi * i;
            double x = Math.cos(theta) * radiusAtY;
            double z = Math.sin(theta) * radiusAtY;

            Vector point = new Vector(
                center.getX() + x * radius,
                center.getY() + y * radius,
                center.getZ() + z * radius
            );
            
            spherePoints.add(point);
        }
        
        return spherePoints;
    }
    
    /**
     * Проверяет, находится ли точка внутри полигона
     * для простых форм типа квадратов работает отлично
     * @param point точка для проверки
     * @param polygon список вершин полигона
     * @return true если точка внутри
     */
    public static boolean isPointInPolygon(Vector point, List<Vector> polygon) {
        if (polygon.size() < 3) {
            return false;
        }
        
        double minX = polygon.get(0).getX();
        double maxX = minX;
        double minZ = polygon.get(0).getZ();
        double maxZ = minZ;
        
        for (int i = 1; i < polygon.size(); i++) {
            double x = polygon.get(i).getX();
            double z = polygon.get(i).getZ();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }
        
        double px = point.getX();
        double pz = point.getZ();
        
        return (px >= minX && px <= maxX && pz >= minZ && pz <= maxZ);
    }
    
    /**
     * Вычисляет площадь полигона по формуле шнуровки
     * @param polygon вершины полигона
     * @return площадь в блоках
     */
    public static double calculatePolygonArea(List<Vector> polygon) {
        if (polygon.size() < 3) {
            return 0;
        }
        
        double area = 0;
        int n = polygon.size();
        
        for (int i = 0; i < n; i++) {
            Vector current = polygon.get(i);
            Vector next = polygon.get((i + 1) % n);
            area += (current.getX() * next.getZ()) - (next.getX() * current.getZ());
        }
        
        return Math.abs(area) / 2.0;
    }
    
    /**
     * Проверяет пересечение двух отрезков
     * @param p1 начало первого отрезка
     * @param p2 конец первого отрезка
     * @param p3 начало второго отрезка
     * @param p4 конец второго отрезка
     * @return true если отрезки пересекаются
     */
    public static boolean doSegmentsIntersect(Vector p1, Vector p2, Vector p3, Vector p4) {
        int o1 = orientation(p1, p2, p3);
        int o2 = orientation(p1, p2, p4);
        int o3 = orientation(p3, p4, p1);
        int o4 = orientation(p3, p4, p2);

        if (o1 != o2 && o3 != o4) {
            return true;
        }

        if (o1 == 0 && onSegment(p1, p3, p2)) return true;
        if (o2 == 0 && onSegment(p1, p4, p2)) return true;
        if (o3 == 0 && onSegment(p3, p1, p4)) return true;
        if (o4 == 0 && onSegment(p3, p2, p4)) return true;
        
        return false;
    }

    private static int orientation(Vector p, Vector q, Vector r) {
        double val = (q.getZ() - p.getZ()) * (r.getX() - q.getX()) - 
                    (q.getX() - p.getX()) * (r.getZ() - q.getZ());
        
        if (val == 0) return 0;
        return (val > 0) ? 1 : 2;
    }

    private static boolean onSegment(Vector p, Vector q, Vector r) {
        return q.getX() <= Math.max(p.getX(), r.getX()) && 
               q.getX() >= Math.min(p.getX(), r.getX()) &&
               q.getZ() <= Math.max(p.getZ(), r.getZ()) && 
               q.getZ() >= Math.min(p.getZ(), r.getZ());
    }
}