package com.sparky.libx.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация региона в форме кубоида
 */
@SerializableAs("CuboidRegion")
public class CuboidRegion extends Region {
    
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;
    
    public CuboidRegion(String name, World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        super(name, world);
        
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }
    
    public CuboidRegion(String name, Location corner1, Location corner2) {
        this(
            name,
            corner1.getWorld(),
            corner1.getBlockX(),
            corner1.getBlockY(),
            corner1.getBlockZ(),
            corner2.getBlockX(),
            corner2.getBlockY(),
            corner2.getBlockZ()
        );
        
        if (!corner1.getWorld().equals(corner2.getWorld())) {
            throw new IllegalArgumentException("Corners must be in the same world");
        }
    }
    
    @Override
    public boolean contains(Location location) {
        if (!location.getWorld().equals(world)) {
            return false;
        }
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
    
    @Override
    public Location getCenter() {
        return new Location(
            world,
            (minX + maxX) / 2.0,
            (minY + maxY) / 2.0,
            (minZ + maxZ) / 2.0
        );
    }
    
    @Override
    public Location getMinPoint() {
        return new Location(world, minX, minY, minZ);
    }
    
    @Override
    public Location getMaxPoint() {
        return new Location(world, maxX, maxY, maxZ);
    }
    
    @Override
    public double getVolume() {
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }
    
    public int getWidth() {
        return maxX - minX + 1;
    }
    
    public int getHeight() {
        return maxY - minY + 1;
    }
    
    public int getLength() {
        return maxZ - minZ + 1;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("type", "cuboid");
        result.put("minX", minX);
        result.put("minY", minY);
        result.put("minZ", minZ);
        result.put("maxX", maxX);
        result.put("maxY", maxY);
        result.put("maxZ", maxZ);
        return result;
    }
    
    public static CuboidRegion deserialize(Map<String, Object> args) {
        String name = (String) args.get("name");
        World world = Bukkit.getWorld((String) args.get("world"));
        
        int minX = ((Number) args.get("minX")).intValue();
        int minY = ((Number) args.get("minY")).intValue();
        int minZ = ((Number) args.get("minZ")).intValue();
        int maxX = ((Number) args.get("maxX")).intValue();
        int maxY = ((Number) args.get("maxY")).intValue();
        int maxZ = ((Number) args.get("maxZ")).intValue();
        
        return new CuboidRegion(name, world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CuboidRegion other = (CuboidRegion) obj;
        return minX == other.minX && minY == other.minY && minZ == other.minZ &&
               maxX == other.maxX && maxY == other.maxY && maxZ == other.maxZ &&
               world.equals(other.world);
    }
    
    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + minX;
        result = 31 * result + minY;
        result = 31 * result + minZ;
        result = 31 * result + maxX;
        result = 31 * result + maxY;
        result = 31 * result + maxZ;
        return result;
    }
    
    @Override
    public String toString() {
        return String.format(
            "CuboidRegion{name='%s', world=%s, min=[%d, %d, %d], max=[%d, %d, %d]}",
            name, world.getName(), minX, minY, minZ, maxX, maxY, maxZ
        );
    }
}
