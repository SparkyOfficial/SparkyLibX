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
        
        // Создаем ландшафт с использованием фрактального шума Перлина
        List<org.bukkit.block.Block> terrainBlocks = BlockUtils.createPerlinTerrain(
            corner1, corner2, 
            Material.STONE,  // Основной материал
            6,               // Количество октав
            0.5,             // Persistence (степень сохранения амплитуды)
            0.02,            // Масштаб шума
            20,              // Множитель высоты
            70               // Уровень моря
        );
        
        getLogger().info("Создан ландшафт из " + terrainBlocks.size() + " блоков");
    }
    
    /**
     * Создает сферу с шумом Перлина для создания органической формы
     * @param world мир Minecraft
     */
    public void createNoisySphere(World world) {
        Location center = new Location(world, 25, 80, 25);
        
        // Создаем сферу с шумом Перлина для создания органической формы
        List<org.bukkit.block.Block> sphereBlocks = BlockUtils.createNoisySphere(
            center, 
            10.0,            // Радиус сферы
            Material.GLASS,  // Материал
            0.1,             // Масштаб шума (меньше = более плавные переходы)
            0.0              // Пороговое значение (больше = меньше блоков)
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
        
        // Создаем паттерн Вороного
        List<org.bukkit.block.Block> voronoiBlocks = BlockUtils.createVoronoiPattern(
            corner1, corner2, 
            materials,  // Массив материалов для использования
            2.0         // Масштаб паттерна
        );
        
        getLogger().info("Создан паттерн Вороного из " + voronoiBlocks.size() + " блоков");
    }
    
    /**
     * Создает спиральный паттерн
     * @param world мир Minecraft
     */
    public void createSpiralPattern(World world) {
        Location center = new Location(world, 120, 70, 20);
        
        // Создаем спиральный паттерн
        List<org.bukkit.block.Block> spiralBlocks = BlockUtils.createSpiralPattern(
            center, 
            8.0,              // Радиус спирали
            20.0,             // Высота спирали
            5.0,              // Количество оборотов
            Material.GOLD_BLOCK // Материал
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
        
        // Создаем трехмерную шахматную доску
        List<org.bukkit.block.Block> checkerboardBlocks = BlockUtils.create3DCheckerboard(
            corner1, corner2,
            Material.BLACK_CONCRETE,  // Первый материал
            Material.WHITE_CONCRETE,  // Второй материал
            2                         // Размер клетки
        );
        
        getLogger().info("Создана 3D шахматная доска из " + checkerboardBlocks.size() + " блоков");
    }
    
    /**
     * Демонстрирует использование генератора шума напрямую
     */
    public void demonstrateNoiseGenerator() {
        getLogger().info("=== Демонстрация генератора шума ===");
        
        // Шум Перлина
        double perlin1 = NoiseGenerator.perlinNoise(0.5, 0.5, 0.5);
        double perlin2 = NoiseGenerator.perlinNoise(1.0, 1.0, 1.0);
        getLogger().info("Шум Перлина (0.5,0.5,0.5): " + perlin1);
        getLogger().info("Шум Перлина (1.0,1.0,1.0): " + perlin2);
        
        // Фрактальный шум (FBM)
        double fractal = NoiseGenerator.fractalNoise(0.5, 0.5, 0.5, 4, 0.5, 0.01);
        getLogger().info("Фрактальный шум: " + fractal);
        
        // Шум Вороного
        double voronoi = NoiseGenerator.voronoiNoise(0.5, 0.5, 0.5, 1.0);
        getLogger().info("Шум Вороного: " + voronoi);
        
        // Волновой шум
        double wave = NoiseGenerator.waveNoise(0.5, 0.5, 0.5, 2.0, 1.0);
        getLogger().info("Волновой шум: " + wave);
        
        // Спиральный шум
        double spiral = NoiseGenerator.spiralNoise(0.5, 0.5, 3.0, 2.0);
        getLogger().info("Спиральный шум: " + spiral);
        
        // Шахматная доска
        int checker = NoiseGenerator.checkerboard(5, 10, 3);
        getLogger().info("Шахматная доска (5,10,3): " + checker);
    }
    
    @Override
    public void onEnable() {
        getLogger().info("PatternExample plugin включен!");
        getLogger().info("Этот пример демонстрирует возможности библиотеки SparkyLibX");
        getLogger().info("для создания сложных паттернов и структур в Minecraft.");
        
        // Демонстрируем использование генератора шума
        demonstrateNoiseGenerator();
        
        getLogger().info("Для использования методов работы с блоками необходим запущенный мир Minecraft.");
        getLogger().info("Вызовите соответствующие методы из вашего плагина или команды.");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("PatternExample plugin выключен!");
    }
}