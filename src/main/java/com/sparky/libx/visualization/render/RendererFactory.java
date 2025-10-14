package com.sparky.libx.visualization.render;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Фабрика для создания рендереров регионов
 */
public class RendererFactory {
    
    /**
     * Создает рендерер на основе конфигурации
     */
    public static RegionRenderer createRenderer(ConfigurationSection config) {
        String type = config.getString("type", "particle").toLowerCase();
        
        switch (type) {
            case "block":
                return createBlockRenderer(config);
            case "particle":
            default:
                return createParticleRenderer(config);
        }
    }
    
    /**
     * Создает рендерер частиц
     */
    public static ParticleRenderer createParticleRenderer(ConfigurationSection config) {

        Particle particle = Particle.REDSTONE;
        Object particleData = new Particle.DustOptions(
            Color.fromRGB(config.getInt("color.red", 255), 
                         config.getInt("color.green", 0), 
                         config.getInt("color.blue", 0)),
            (float) config.getDouble("size", 1.0)
        );
        

        try {
            particle = Particle.valueOf(config.getString("particle", "REDSTONE").toUpperCase());
        } catch (IllegalArgumentException e) {

        }
        
        double density = config.getDouble("density", 0.2);
        int particlesPerTick = config.getInt("particles-per-tick", 10);

        
        return new ParticleRenderer(particle, particleData, density, particlesPerTick, duration);
    }
    
    /**
     * Создает блочный рендерер
     */
    public static BlockRenderer createBlockRenderer(ConfigurationSection config) {

        Material material = Material.matchMaterial(config.getString("material", "GLASS"));
        if (material == null || !material.isBlock()) {
            material = Material.GLASS;
        }
        

        boolean wireframe = config.getBoolean("wireframe", false);
        
        return new BlockRenderer(material, duration, wireframe);
    }
    
    /**
     * Создает стандартный рендерер для выделения
     */
    public static RegionRenderer createDefaultSelectionRenderer() {
        return new ParticleRenderer(
            Particle.VILLAGER_HAPPY,
            new Particle.DustOptions(Color.LIME, 1.5f),



        );
    }
    
    /**
     * Создает стандартный рендерер для отображения регионов
     */
    public static RegionRenderer createDefaultRegionRenderer() {
        return new ParticleRenderer(
            Particle.REDSTONE,
            new Particle.DustOptions(Color.RED, 1.0f),



        );
    }
}
