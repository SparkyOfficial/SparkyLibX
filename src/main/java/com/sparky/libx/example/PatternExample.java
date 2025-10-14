package com.sparky.libx.example;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import com.sparky.libx.block.BlockUtils;
import com.sparky.libx.math.NoiseGenerator;

/**
 * Пример использования библиотеки для создания паттернов и работы с блоками
 * Демонстрирует возможности генерации сложных структур с помощью шума Перлина и других алгоритмов
 */
public class PatternExample extends JavaPlugin {
    
    /**
     * Создает ландшафт с использованием фрактального шума Перлина
     * @param world мир Minecraft
     */
    public void createPerlinTerrain(World world) {
        Location corner1 = new Location(world, 0, 64, 0);
        Location corner2 = new Location(world, 50, 100, 50);
        
        List<org.bukkit.block.Block> terrainBlocks = BlockUtils.createPerlinTerrain(
            corner1, corner2, 
            Material.STONE,
            6,
            0.5,
            0.02,
            20,
            70
        );
        
        getLogger().info("Создан ландшафт из " + terrainBlocks.size() + " блоков");
    }
    
    /**
     * Создает сферу с шумом Перлина для создания органической формы
     * @param world мир Minecraft
     */
    public void createNoisySphere(World world) {
        Location center = new Location(world, 25, 80, 25);
        
        List<org.bukkit.block.Block> sphereBlocks = BlockUtils.createNoisySphere(
            center, 
            10.0,
            Material.GLASS,
            0.1,
            0.0
        );
        
        getLogger().info("Создана шумовая сфера из " + sphereBlocks.size() + " блоков");
    }
    
    /**
     * Создает паттерн Вороного (ячеистая структура)
     * @param world мир Minecraft
     */
    public void createVoronoiPattern(World world) {
        Location corner1 = new Location(world, 60, 64, 0);
        Location corner2 = new Location(world, 100, 80, 40);
        
        Material[] materials = {
            Material.STONE,
            Material.DIRT,
            Material.GRASS_BLOCK,
            Material.COBBLESTONE,
            Material.MOSSY_COBBLESTONE
        };
        
        List<org.bukkit.block.Block> voronoiBlocks = BlockUtils.createVoronoiPattern(
            corner1, corner2, 
            materials,
            2.0
        );
        
        getLogger().info("Создан паттерн Вороного из " + voronoiBlocks.size() + " блоков");
    }
    
    /**
     * Создает спиральный паттерн
     * @param world мир Minecraft
     */
    public void createSpiralPattern(World world) {
        Location center = new Location(world, 120, 70, 20);
        
        List<org.bukkit.block.Block> spiralBlocks = BlockUtils.createSpiralPattern(
            center, 
            8.0,
            20.0,
            5.0,
            Material.GOLD_BLOCK
        );
        
        getLogger().info("Создан спиральный паттерн из " + spiralBlocks.size() + " блоков");
    }
    
    /**
     * Создает трехмерную шахматную доску
     * @param world мир Minecraft
     */
    public void create3DCheckerboard(World world) {
        Location corner1 = new Location(world, 0, 90, 60);
        Location corner2 = new Location(world, 20, 110, 80);
        
        List<org.bukkit.block.Block> checkerboardBlocks = BlockUtils.create3DCheckerboard(
            corner1, corner2,
            Material.BLACK_CONCRETE,
            Material.WHITE_CONCRETE,
            2
        );
        
        getLogger().info("Создана 3D шахматная доска из " + checkerboardBlocks.size() + " блоков");
    }
    
    /**
     * Демонстрирует использование генератора шума напрямую
     */
    public void demonstrateNoiseGenerator() {
        getLogger().info("=== Демонстрация генератора шума ===");
        
        NoiseGenerator noiseGen = new NoiseGenerator(System.currentTimeMillis());
        
        double perlin1 = noiseGen.perlinNoise(0.5, 0.5, 0.5);
        double perlin2 = noiseGen.perlinNoise(1.0, 1.0, 1.0);
        getLogger().info("Шум Перлина (0.5,0.5,0.5): " + perlin1);
        getLogger().info("Шум Перлина (1.0,1.0,1.0): " + perlin2);
        
        double fractal = noiseGen.fractalBrownianMotion(0.5, 0.5, 4, 0.5, 0.01);
        getLogger().info("Фрактальный шум: " + fractal);
        
        double voronoi = noiseGen.voronoiNoise(0.5, 0.5, 10);
        getLogger().info("Шум Вороного: " + voronoi);
        
        double wave = noiseGen.waveNoise(0.5, 0.5, 2.0, 1.0);
        getLogger().info("Волновой шум: " + wave);
        
        double spiral = noiseGen.spiralNoise(0.5, 0.5, 3);
        getLogger().info("Спиральный шум: " + spiral);
        
        double checker = noiseGen.checkerboard(5, 10, 3);
        getLogger().info("Шахматная доска (5,10,3): " + checker);
    }
    
    @Override
    public void onEnable() {
        getLogger().info("PatternExample plugin включен!");
        getLogger().info("Этот пример демонстрирует возможности библиотеки SparkyLibX");
        getLogger().info("для создания сложных паттернов и структур в Minecraft.");
        
        demonstrateNoiseGenerator();
        
        getLogger().info("Для использования методов работы с блоками необходим запущенный мир Minecraft.");
        getLogger().info("Вызовите соответствующие методы из вашего плагина или команды.");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("PatternExample plugin выключен!");
    }
}