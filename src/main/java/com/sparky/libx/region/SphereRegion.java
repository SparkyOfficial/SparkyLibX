package com.sparky.libx.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

/**
 * Реализация региона в форме сферы
 */
@SerializableAs("SphereRegion")
public class SphereRegion extends Region {
    
    private final double centerX, centerY, centerZ;
    private final double radius;
    private final double radiusSquared;
    
    public SphereRegion(String name, World world, double centerX, double centerY, double centerZ, double radius) {
        super(name, world);
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }
    
    public SphereRegion(String name, Location center, double radius) {
        this(
            name,
            center.getWorld(),
            center.getX(),
            center.getY(),
            center.getZ(),
            radius
        );
    }
    
    @Override
    public boolean contains(Location location) {
        if (!location.getWorld().equals(world)) {
            return false;
        }
        
        double dx = location.getX() - centerX;
        double dy = location.getY() - centerY;
        double dz = location.getZ() - centerZ;
        
        return dx * dx + dy * dy + dz * dz <= radiusSquared;
    }
    
    @Override
    public Location getCenter() {
        return new Location(world, centerX, centerY, centerZ);
    }
    
    @Override
    public Location getMinPoint() {
        return new Location(
            world,
            centerX - radius,
            centerY - radius,
            centerZ - radius
        );
    }
    
    @Override
    public Location getMaxPoint() {
        return new Location(
            world,
            centerX + radius,
            centerY + radius,
            centerZ + radius
        );
    }
    
    @Override
    public double getVolume() {
        return (4.0 / 3.0) * Math.PI * radius * radius * radius;
    }
    
    public double getRadius() {
        return radius;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("type", "sphere");
        result.put("centerX", centerX);
        result.put("centerY", centerY);
        result.put("centerZ", centerZ);
        result.put("radius", radius);
        return result;
    }
    
    public static SphereRegion deserialize(Map<String, Object> args) {
        String name = (String) args.get("name");
        World world = Bukkit.getWorld((String) args.get("world"));
        
        double centerX = ((Number) args.get("centerX")).doubleValue();
        double centerY = ((Number) args.get("centerY")).doubleValue();
        double centerZ = ((Number) args.get("centerZ")).doubleValue();
        double radius = ((Number) args.get("radius")).doubleValue();
        
        return new SphereRegion(name, world, centerX, centerY, centerZ, radius);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SphereRegion other = (SphereRegion) obj;
        return Double.compare(other.centerX, centerX) == 0 &&
               Double.compare(other.centerY, centerY) == 0 &&
               Double.compare(other.centerZ, centerZ) == 0 &&
               Double.compare(other.radius, radius) == 0 &&
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
        result = 31 * result + world.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return String.format(
            "SphereRegion{name='%s', world=%s, center=[%.2f, %.2f, %.2f], radius=%.2f}",
            name, world.getName(), centerX, centerY, centerZ, radius
        );
    }
}
