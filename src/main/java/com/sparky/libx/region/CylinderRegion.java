package com.sparky.libx.region;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.SerializableAs;

/**
 * Реалізація регіону у формі циліндра
 * @author Андрій Будильников
 */
@SerializableAs("CylinderRegion")
public class CylinderRegion extends Region {
    
    private final double centerX, centerY, centerZ;
    private final double radius;
    private final double minY, maxY;
    private final double radiusSquared;
    
    public CylinderRegion(String name, World world, double centerX, double centerY, double centerZ, 
                         double radius, double minY, double maxY) {
        super(name, world);
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.minY = minY;
        this.maxY = maxY;
        this.radiusSquared = radius * radius;
    }
    
    public CylinderRegion(String name, Location center, double radius, double height) {
        this(
            name,
            center.getWorld(),
            center.getX(),
            center.getY(),
            center.getZ(),
            radius,
            center.getY() - height / 2,
            center.getY() + height / 2
        );
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
        
        double dx = location.getX() - centerX;
        double dz = location.getZ() - centerZ;
        double distanceSquared = dx * dx + dz * dz;
        
        return distanceSquared <= radiusSquared;
    }
    
    @Override
    public Location getCenter() {
        return new Location(world, centerX, (minY + maxY) / 2, centerZ);
    }
    
    @Override
    public Location getMinPoint() {
        return new Location(world, centerX - radius, minY, centerZ - radius);
    }
    
    @Override
    public Location getMaxPoint() {
        return new Location(world, centerX + radius, maxY, centerZ + radius);
    }
    
    @Override
    public double getVolume() {
        return Math.PI * radius * radius * (maxY - minY);
    }
    
    public double getRadius() {
        return radius;
    }
    
    public double getMinY() {
        return minY;
    }
    
    public double getMaxY() {
        return maxY;
    }
    
    public double getHeight() {
        return maxY - minY;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("type", "cylinder");
        result.put("centerX", centerX);
        result.put("centerY", centerY);
        result.put("centerZ", centerZ);
        result.put("radius", radius);
        result.put("minY", minY);
        result.put("maxY", maxY);
        return result;
    }
    
    public static CylinderRegion deserialize(Map<String, Object> args) {
        String name = (String) args.get("name");
        World world = Bukkit.getWorld((String) args.get("world"));
        
        double centerX = ((Number) args.get("centerX")).doubleValue();
        double centerY = ((Number) args.get("centerY")).doubleValue();
        double centerZ = ((Number) args.get("centerZ")).doubleValue();
        double radius = ((Number) args.get("radius")).doubleValue();
        double minY = ((Number) args.get("minY")).doubleValue();
        double maxY = ((Number) args.get("maxY")).doubleValue();
        
        return new CylinderRegion(name, world, centerX, centerY, centerZ, radius, minY, maxY);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CylinderRegion other = (CylinderRegion) obj;
        return Double.compare(other.centerX, centerX) == 0 &&
               Double.compare(other.centerY, centerY) == 0 &&
               Double.compare(other.centerZ, centerZ) == 0 &&
               Double.compare(other.radius, radius) == 0 &&
               Double.compare(other.minY, minY) == 0 &&
               Double.compare(other.maxY, maxY) == 0 &&
               world.equals(other.world);
    }
    
    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(centerX);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(centerY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(centerZ);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(radius);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + world.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return String.format(
            "CylinderRegion{name='%s', world=%s, center=[%.2f, %.2f, %.2f], radius=%.2f, height=%.2f}",
            name, world.getName(), centerX, centerY, centerZ, radius, maxY - minY
        );
    }
}