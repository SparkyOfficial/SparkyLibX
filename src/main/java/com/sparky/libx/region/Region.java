package com.sparky.libx.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Абстрактный класс, представляющий регион в мире
 */
public abstract class Region implements ConfigurationSerializable {
    
    protected final String name;
    protected final World world;
    
    protected Region(String name, World world) {
        this.name = name;
        this.world = world;
    }
    
    /**
     * Проверяет, содержит ли регион указанную точку
     */
    public abstract boolean contains(Location location);
    
    /**
     * Получает центр региона
     */
    public abstract Location getCenter();
    
    /**
     * Получает минимальные координаты региона
     */
    public abstract Location getMinPoint();
    
    /**
     * Получает максимальные координаты региона
     */
    public abstract Location getMaxPoint();
    
    /**
     * Получает объем региона в блоках
     */
    public abstract double getVolume();
    
    public String getName() {
        return name;
    }
    
    public World getWorld() {
        return world;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("world", world.getName());
        return result;
    }
    
    /**
     * Создает регион из конфигурации
     */
    public static Region deserialize(Map<String, Object> args) {
        String type = (String) args.get("type");
        

        switch (type.toLowerCase()) {
            case "cuboid":
                return CuboidRegion.deserialize(args);
            case "sphere":
                return SphereRegion.deserialize(args);
            case "polygon":
                return PolygonRegion.deserialize(args);
            default:
                throw new IllegalArgumentException("Unknown region type: " + type);
        }
    }
}
