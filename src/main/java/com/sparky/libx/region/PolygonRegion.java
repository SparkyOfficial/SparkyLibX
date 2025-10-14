package com.sparky.libx.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;

/**
 * Реализация региона в форме полигона (многоугольника)
 */
@SerializableAs("PolygonRegion")
public class PolygonRegion extends Region {
    
    private final double minY, maxY;
    private final List<Vector2D> points;
    private final BoundingBox2D bounds;
    
    public PolygonRegion(String name, World world, double minY, double maxY, List<Vector2D> points) {
        super(name, world);
        
        if (points.size() < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 points");
        }
        
        this.minY = minY;
        this.maxY = maxY;
        this.points = new ArrayList<>(points);
        this.bounds = new BoundingBox2D(points);
    }
    
    public PolygonRegion(String name, World world, double minY, double maxY, double[] x, double[] z) {
        this(name, world, minY, maxY, createPoints(x, z));
    }
    
    private static List<Vector2D> createPoints(double[] x, double[] z) {
        if (x.length != z.length) {
            throw new IllegalArgumentException("x and z arrays must have the same length");
        }
        
        List<Vector2D> points = new ArrayList<>();
        for (int i = 0; i < x.length; i++) {
            points.add(new Vector2D(x[i], z[i]));
        }
        return points;
    }
    
    @Override
    public boolean contains(Location location) {
        if (!location.getWorld().equals(world)) {
            return false;
        }
        

        double y = location.getY();
        if (y < minY || y > maxY) {
            return false;
        }
        

        Vector2D point = new Vector2D(location.getX(), location.getZ());
        if (!bounds.contains(point)) {
            return false;
        }
        

        return isPointInPolygon(point);
    }
    
    private boolean isPointInPolygon(Vector2D point) {
        boolean inside = false;
        int n = points.size();
        
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Vector2D p1 = points.get(i);
            Vector2D p2 = points.get(j);
            
            if (((p1.getZ() > point.getZ()) != (p2.getZ() > point.getZ())) &&
                (point.getX() < (p2.getX() - p1.getX()) * (point.getZ() - p1.getZ()) / 
                (p2.getZ() - p1.getZ()) + p1.getX())) {
                inside = !inside;
            }
        }
        
        return inside;
    }
    
    @Override
    public Location getCenter() {
        double centerX = bounds.getMinX() + bounds.getWidth() / 2;
        double centerZ = bounds.getMinZ() + bounds.getDepth() / 2;
        double centerY = (minY + maxY) / 2;
        
        return new Location(world, centerX, centerY, centerZ);
    }
    
    @Override
    public Location getMinPoint() {
        return new Location(world, bounds.getMinX(), minY, bounds.getMinZ());
    }
    
    @Override
    public Location getMaxPoint() {
        return new Location(world, bounds.getMaxX(), maxY, bounds.getMaxZ());
    }
    
    @Override
    public double getVolume() {

        return bounds.getArea() * (maxY - minY);
    }
    
    public List<Vector2D> getPoints() {
        return new ArrayList<>(points);
    }
    
    public double getMinY() {
        return minY;
    }
    
    public double getMaxY() {
        return maxY;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("type", "polygon");
        result.put("minY", minY);
        result.put("maxY", maxY);
        
        List<Map<String, Double>> pointsList = new ArrayList<>();
        for (Vector2D point : points) {
            Map<String, Double> pointMap = new HashMap<>();
            pointMap.put("x", point.getX());
            pointMap.put("z", point.getZ());
            pointsList.add(pointMap);
        }
        
        result.put("points", pointsList);
        return result;
    }
    
    public static PolygonRegion deserialize(Map<String, Object> args) {
        String name = (String) args.get("name");
        World world = Bukkit.getWorld((String) args.get("world"));
        
        double minY = ((Number) args.get("minY")).doubleValue();
        double maxY = ((Number) args.get("maxY")).doubleValue();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Double>> pointsList = (List<Map<String, Double>>) args.get("points");
        List<Vector2D> points = new ArrayList<>();
        
        for (Map<String, Double> pointMap : pointsList) {
            points.add(new Vector2D(
                pointMap.get("x"),
                pointMap.get("z")
            ));
        }
        
        return new PolygonRegion(name, world, minY, maxY, points);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PolygonRegion other = (PolygonRegion) obj;
        return Double.compare(other.minY, minY) == 0 &&
               Double.compare(other.maxY, maxY) == 0 &&
               points.equals(other.points) &&
               world.equals(other.world);
    }
    
    @Override
    public int hashCode() {
        int result = world.hashCode();
        long temp = Double.doubleToLongBits(minY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + points.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return String.format(
            "PolygonRegion{name='%s', world=%s, points=%d, minY=%.2f, maxY=%.2f}",
            name, world.getName(), points.size(), minY, maxY
        );
    }
    
    /**
     * 2D вектор для представления точек полигона
     */
    public static class Vector2D {
        private final double x, z;
        
        public Vector2D(double x, double z) {
            this.x = x;
            this.z = z;
        }
        
        public double getX() {
            return x;
        }
        
        public double getZ() {
            return z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            Vector2D other = (Vector2D) obj;
            return Double.compare(other.x, x) == 0 &&
                   Double.compare(other.z, z) == 0;
        }
        
        @Override
        public int hashCode() {
            long temp = Double.doubleToLongBits(x);
            int result = (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(z);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }
    
    /**
     * Ограничивающий прямоугольник для оптимизации проверок
     */
    private static class BoundingBox2D {
        private double minX, maxX, minZ, maxZ;
        
        public BoundingBox2D(List<Vector2D> points) {
            if (points.isEmpty()) {
                throw new IllegalArgumentException("Points list cannot be empty");
            }
            
            Vector2D first = points.get(0);
            minX = maxX = first.getX();
            minZ = maxZ = first.getZ();
            
            for (int i = 1; i < points.size(); i++) {
                Vector2D point = points.get(i);
                minX = Math.min(minX, point.getX());
                maxX = Math.max(maxX, point.getX());
                minZ = Math.min(minZ, point.getZ());
                maxZ = Math.max(maxZ, point.getZ());
            }
        }
        
        public boolean contains(Vector2D point) {
            return point.getX() >= minX && point.getX() <= maxX &&
                   point.getZ() >= minZ && point.getZ() <= maxZ;
        }
        
        public double getMinX() {
            return minX;
        }
        
        public double getMaxX() {
            return maxX;
        }
        
        public double getMinZ() {
            return minZ;
        }
        
        public double getMaxZ() {
            return maxZ;
        }
        
        public double getWidth() {
            return maxX - minX;
        }
        
        public double getDepth() {
            return maxZ - minZ;
        }
        
        public double getArea() {
            return getWidth() * getDepth();
        }
    }
}
