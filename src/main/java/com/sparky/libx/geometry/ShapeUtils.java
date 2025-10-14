package com.sparky.libx.geometry;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;

/**
 * Утилиты для работы с геометрическими фигурами
 */
public final class ShapeUtils {
    
    private ShapeUtils() {}
    
    /**
     * Генерирует точки на линии между двумя точками
     * @param start начальная точка
     * @param end конечная точка
     * @param stepSize шаг между точками
     * @return список точек на линии
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
     * Генерирует точки на окружности
     * @param center центр окружности
     * @param radius радиус окружности
     * @param points количество точек
     * @return список точек на окружности
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
     * Генерирует точки на сфере
     * @param center центр сферы
     * @param radius радиус сферы
     * @param points количество точек
     * @return список точек на сфере
     */
    public static List<Vector> getSpherePoints(Vector center, double radius, int points) {
        List<Vector> spherePoints = new ArrayList<>();
        
        double phi = Math.PI * (3.0 - Math.sqrt(5.0)); // Золотое сечение для равномерного распределения

        
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
     * @param point проверяемая точка
     * @param polygon вершины полигона (в порядке обхода по или против часовой стрелки)
     * @return true, если точка внутри полигона
     */
    public static boolean isPointInPolygon(Vector point, List<Vector> polygon) {
        if (polygon.size() < 3) {
            return false;
        }
        
        boolean result = false;
        int n = polygon.size();
        
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Vector vi = polygon.get(i);
            Vector vj = polygon.get(j);
            
            if (((vi.getZ() > point.getZ()) != (vj.getZ() > point.getZ())) &&
                (point.getX() < (vj.getX() - vi.getX()) * (point.getZ() - vi.getZ()) / 
                (vj.getZ() - vi.getZ()) + vi.getX())) {
                result = !result;
            }
        }
        
        return result;
    }
    
    /**
     * Вычисляет площадь полигона по формуле шнуровки Гаусса
     * @param polygon вершины полигона
     * @return площадь полигона
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
     * Проверяет, пересекаются ли два отрезка
     * @param p1 начальная точка первого отрезка
     * @param p2 конечная точка первого отрезка
     * @param p3 начальная точка второго отрезка
     * @param p4 конечная точка второго отрезка
     * @return true, если отрезки пересекаются
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
