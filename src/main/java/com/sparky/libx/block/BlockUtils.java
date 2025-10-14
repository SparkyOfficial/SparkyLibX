package com.sparky.libx.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.sparky.libx.math.NoiseGenerator;
import com.sparky.libx.math.Vector3D;

/**
 * Утилиты для работы с блоками в Minecraft
 * Предоставляет методы для создания сложных структур и паттернов
 */
public class BlockUtils {
    
    private static final Random random = new Random();
    
    private BlockUtils() {}
    
    /**
     * Получает все блоки в сфере
     * @param center центр сферы
     * @param radius радиус сферы
     * @return список блоков в сфере
     */
    public static List<Block> getBlocksInSphere(Location center, double radius) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        if (world == null) return blocks;
        
        int radiusCeil = (int) Math.ceil(radius);
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        for (int x = -radiusCeil; x <= radiusCeil; x++) {
            for (int y = -radiusCeil; y <= radiusCeil; y++) {
                for (int z = -radiusCeil; z <= radiusCeil; z++) {
                    double distanceSquared = x * x + y * y + z * z;
                    if (distanceSquared <= radius * radius) {
                        Block block = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
                        blocks.add(block);
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Проверяет, является ли блок твердым (не воздух и не проходимый)
     * @param block блок для проверки
     * @return true, если блок твердый
     */
    public static boolean isSolid(Block block) {
        return block != null && block.getType().isSolid();
    }
    
    /**
     * Находит ближайший твердый блок под указанным местоположением
     * @param location начальное местоположение
     * @param maxDistance максимальное расстояние для поиска вниз
     * @return ближайший твердый блок или null, если не найден
     */
    public static Block findSolidBlockBelow(Location location, int maxDistance) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = Math.min(world.getMaxHeight() - 1, location.getBlockY());
        int z = location.getBlockZ();
        
        for (int i = 0; i < maxDistance && y > 0; i++, y--) {
            Block block = world.getBlockAt(x, y, z);
            if (isSolid(block)) {
                return block;
            }
        }
        
        return null;
    }
    
    /**
     * Создает линию блоков между двумя точками
     * @param start начальная точка
     * @param end конечная точка
     * @param material тип блока
     * @param replaceAir заменять ли воздушные блоки
     * @return список измененных блоков
     */
    public static List<Block> createLine(Location start, Location end, Material material, boolean replaceAir) {
        List<Block> blocks = new ArrayList<>();
        if (start.getWorld() == null || end.getWorld() == null || !start.getWorld().equals(end.getWorld())) {
            return blocks;
        }
        
        Vector3D startVec = new Vector3D(start);
        Vector3D endVec = new Vector3D(end);
        double distance = startVec.distance(endVec);
        
        for (double i = 0; i <= 1.0; i += 1.0 / distance) {
            Vector3D point = startVec.lerp(endVec, i);
            Block block = start.getWorld().getBlockAt(
                (int) Math.round(point.getX()),
                (int) Math.round(point.getY()),
                (int) Math.round(point.getZ())
            );
            
            if ((replaceAir || block.getType() != Material.AIR) && block.getType() != material) {
                block.setType(material);
                blocks.add(block);
            }
        }
        
        return blocks;
    }
    
    /**
     * Получает все блоки, прилегающие к указанному блоку
     * @param block центральный блок
     * @param includeDiagonals включать ли диагональные блоки
     * @return список прилегающих блоков
     */
    public static List<Block> getAdjacentBlocks(Block block, boolean includeDiagonals) {
        List<Block> blocks = new ArrayList<>();
        
        blocks.add(block.getRelative(BlockFace.UP));
        blocks.add(block.getRelative(BlockFace.DOWN));
        blocks.add(block.getRelative(BlockFace.NORTH));
        blocks.add(block.getRelative(BlockFace.SOUTH));
        blocks.add(block.getRelative(BlockFace.WEST));
        blocks.add(block.getRelative(BlockFace.EAST));
        
        if (includeDiagonals) {
            blocks.add(block.getRelative(BlockFace.NORTH_EAST));
            blocks.add(block.getRelative(BlockFace.NORTH_WEST));
            blocks.add(block.getRelative(BlockFace.SOUTH_EAST));
            blocks.add(block.getRelative(BlockFace.SOUTH_WEST));
            
            Block up = block.getRelative(BlockFace.UP);
            blocks.add(up.getRelative(BlockFace.NORTH));
            blocks.add(up.getRelative(BlockFace.SOUTH));
            blocks.add(up.getRelative(BlockFace.EAST));
            blocks.add(up.getRelative(BlockFace.WEST));
            blocks.add(up.getRelative(BlockFace.NORTH_EAST));
            blocks.add(up.getRelative(BlockFace.NORTH_WEST));
            blocks.add(up.getRelative(BlockFace.SOUTH_EAST));
            blocks.add(up.getRelative(BlockFace.SOUTH_WEST));
            
            Block down = block.getRelative(BlockFace.DOWN);
            blocks.add(down.getRelative(BlockFace.NORTH));
            blocks.add(down.getRelative(BlockFace.SOUTH));
            blocks.add(down.getRelative(BlockFace.EAST));
            blocks.add(down.getRelative(BlockFace.WEST));
            blocks.add(down.getRelative(BlockFace.NORTH_EAST));
            blocks.add(down.getRelative(BlockFace.NORTH_WEST));
            blocks.add(down.getRelative(BlockFace.SOUTH_EAST));
            blocks.add(down.getRelative(BlockFace.SOUTH_WEST));
        }
        
        return blocks;
    }
    
    /**
     * Проверяет, может ли игрок видеть указанный блок
     * @param block блок для проверки
     * @param viewerLocation местоположение зрителя
     * @param maxDistance максимальная дистанция видимости
     * @return true, если блок виден
     */
    public static boolean isBlockVisible(Block block, Location viewerLocation, double maxDistance) {
        if (block == null || viewerLocation == null || block.getWorld() == null || 
            !block.getWorld().equals(viewerLocation.getWorld())) {
            return false;
        }
        
        Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
        if (viewerLocation.distance(blockCenter) > maxDistance) {
            return false;
        }
        
        return viewerLocation.getWorld().rayTraceBlocks(viewerLocation, 
            blockCenter.toVector().subtract(viewerLocation.toVector()).normalize(), 
            maxDistance) == null;
    }
    
    /**
     * Создает куб из блоков
     * @param corner1 первый угол куба
     * @param corner2 второй угол куба
     * @param material тип блока
     * @param hollow создавать ли пустотелый куб
     * @return список созданных блоков
     */
    public static List<Block> createCube(Location corner1, Location corner2, Material material, boolean hollow) {
        List<Block> blocks = new ArrayList<>();
        if (corner1.getWorld() == null || corner2.getWorld() == null || 
            !corner1.getWorld().equals(corner2.getWorld())) {
            return blocks;
        }
        
        World world = corner1.getWorld();
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!hollow || x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() != material) {
                            block.setType(material);
                            blocks.add(block);
                        }
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Создает сферу из блоков
     * @param center центр сферы
     * @param radius радиус сферы
     * @param material тип блока
     * @param hollow создавать ли пустотелую сферу
     * @return список созданных блоков
     */
    public static List<Block> createSphere(Location center, double radius, Material material, boolean hollow) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        if (world == null) return blocks;
        
        int radiusCeil = (int) Math.ceil(radius);
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        for (int x = -radiusCeil; x <= radiusCeil; x++) {
            for (int y = -radiusCeil; y <= radiusCeil; y++) {
                for (int z = -radiusCeil; z <= radiusCeil; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance <= radius) {
                        if (!hollow || distance >= radius - 1) {
                            Block block = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
                            if (block.getType() != material) {
                                block.setType(material);
                                blocks.add(block);
                            }
                        }
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Получает все блоки в заданном радиусе от точки
     * @param center центральная точка
     * @param radius радиус
     * @return список блоков
     */
    public static List<Block> getBlocksInRadius(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        if (world == null) return blocks;
        
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add(world.getBlockAt(centerX + x, centerY + y, centerZ + z));
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Заменяет все блоки одного типа на другой тип в заданной области
     * @param corner1 первый угол области
     * @param corner2 второй угол области
     * @param fromType тип блока для замены
     * @param toType новый тип блока
     * @return количество замененных блоков
     */
    public static int replaceBlocks(Location corner1, Location corner2, Material fromType, Material toType) {
        if (corner1.getWorld() == null || corner2.getWorld() == null || 
            !corner1.getWorld().equals(corner2.getWorld())) {
            return 0;
        }
        
        World world = corner1.getWorld();
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        int count = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == fromType) {
                        block.setType(toType);
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * Создает случайный блок из заданного списка материалов
     * @param location местоположение для создания блока
     * @param materials список возможных материалов
     * @return созданный блок или null если список пуст
     */
    public static Block createRandomBlock(Location location, Material... materials) {
        if (materials.length == 0 || location.getWorld() == null) {
            return null;
        }
        
        Material material = materials[random.nextInt(materials.length)];
        Block block = location.getBlock();
        block.setType(material);
        return block;
    }
    
    /**
     * Получает карту частот материалов в заданной области
     * @param corner1 первый угол области
     * @param corner2 второй угол области
     * @return карта частот материалов
     */
    public static Map<Material, Integer> getMaterialFrequencyMap(Location corner1, Location corner2) {
        Map<Material, Integer> frequencyMap = new HashMap<>();
        if (corner1.getWorld() == null || corner2.getWorld() == null || 
            !corner1.getWorld().equals(corner2.getWorld())) {
            return frequencyMap;
        }
        
        World world = corner1.getWorld();
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Material material = world.getBlockAt(x, y, z).getType();
                    frequencyMap.put(material, frequencyMap.getOrDefault(material, 0) + 1);
                }
            }
        }
        
        return frequencyMap;
    }
    
    /**
     * Проверяет, находится ли блок в воде
     * @param block блок для проверки
     * @return true если блок в воде
     */
    public static boolean isInWater(Block block) {
        return block != null && (block.getType() == Material.WATER || 
               block.getRelative(BlockFace.UP).getType() == Material.WATER);
    }
    
    /**
     * Проверяет, находится ли блок на небе (под открытым небом)
     * @param block блок для проверки
     * @return true если блок под открытым небом
     */
    public static boolean isUnderSky(Block block) {
        if (block == null || block.getWorld() == null) return false;
        
        int x = block.getX();
        int z = block.getZ();
        int maxY = block.getWorld().getMaxHeight();
        
        for (int y = block.getY() + 1; y < maxY; y++) {
            Block skyBlock = block.getWorld().getBlockAt(x, y, z);
            if (skyBlock.getType().isSolid()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Создает сферу из блоков с использованием шума Перлина
     * @param center центр сферы
     * @param radius радиус сферы
     * @param material тип блока
     * @param noiseScale масштаб шума (меньше значения = более плавные переходы)
     * @param threshold пороговое значение для определения, где размещать блоки
     * @return список созданных блоков
     */
    public static List<Block> createNoisySphere(Location center, double radius, Material material, double noiseScale, double threshold) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        int radiusInt = (int) Math.ceil(radius);
        
        NoiseGenerator noiseGen = new NoiseGenerator(System.currentTimeMillis());
        
        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int y = -radiusInt; y <= radiusInt; y++) {
                for (int z = -radiusInt; z <= radiusInt; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    
                    if (distance <= radius) {
                        double noise = noiseGen.perlinNoise(
                            (centerX + x) * noiseScale,
                            (centerY + y) * noiseScale,
                            (centerZ + z) * noiseScale
                        );
                        
                        if (noise > threshold) {
                            Block block = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
                            block.setType(material);
                            blocks.add(block);
                        }
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Создает ландшафт с использованием фрактального шума Перлина
     * @param corner1 первый угол области
     * @param corner2 второй угол области
     * @param baseMaterial основной материал поверхности
     * @param octaves количество октав для фрактального шума
     * @param persistence степень сохранения амплитуды между октавами
     * @param scale масштаб шума
     * @param heightMultiplier множитель высоты
     * @param seaLevel уровень моря
     * @return список созданных блоков
     */
    public static List<Block> createPerlinTerrain(Location corner1, Location corner2, Material baseMaterial, 
                                                  int octaves, double persistence, double scale, 
                                                  double heightMultiplier, int seaLevel) {
        List<Block> blocks = new ArrayList<>();
        World world = corner1.getWorld();
        
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        NoiseGenerator noiseGen = new NoiseGenerator(System.currentTimeMillis());
        
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double noise = noiseGen.fractalBrownianMotion(x * scale, z * scale, octaves, persistence, scale);
                int height = (int) (seaLevel + noise * heightMultiplier);
                
                for (int y = corner1.getBlockY(); y <= height && y <= corner2.getBlockY(); y++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(baseMaterial);
                    blocks.add(block);
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Создает паттерн Вороного (ячеистая структура)
     * @param corner1 первый угол области
     * @param corner2 второй угол области
     * @param materials массив материалов для использования в паттерне
     * @param scale масштаб паттерна
     * @return список созданных блоков
     */
    public static List<Block> createVoronoiPattern(Location corner1, Location corner2, Material[] materials, double scale) {
        List<Block> blocks = new ArrayList<>();
        World world = corner1.getWorld();
        
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        NoiseGenerator noiseGen = new NoiseGenerator(System.currentTimeMillis());
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double noise = noiseGen.voronoiNoise(x * scale, y * scale, 10);
                    
                    int materialIndex = (int) (noise * materials.length) % materials.length;
                    Material material = materials[materialIndex];
                    
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(material);
                    blocks.add(block);
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Создает волновой паттерн
     * @param corner1 первый угол области
     * @param corner2 второй угол области
     * @param material тип блока
     * @param frequency частота волны
     * @param amplitude амплитуда волны
     * @param direction направление волны (0=X, 1=Y, 2=Z)
     * @return список созданных блоков
     */
    public static List<Block> createWavePattern(Location corner1, Location corner2, Material material, 
                                               double frequency, double amplitude, int direction) {
        List<Block> blocks = new ArrayList<>();
        World world = corner1.getWorld();
        
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        NoiseGenerator noiseGen = new NoiseGenerator(System.currentTimeMillis());
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double value;
                    switch (direction) {
                        case 0:
                            value = noiseGen.waveNoise(x, y, frequency, amplitude);
                            if (value > 0.5) {
                                Block block = world.getBlockAt(x, y, z);
                                block.setType(material);
                                blocks.add(block);
                            }
                            break;
                        case 1:
                            value = noiseGen.waveNoise(x, y, frequency, amplitude);
                            if (value > 0.5) {
                                Block block = world.getBlockAt(x, y, z);
                                block.setType(material);
                                blocks.add(block);
                            }
                            break;
                        case 2:
                            value = noiseGen.waveNoise(x, y, frequency, amplitude);
                            if (value > 0.5) {
                                Block block = world.getBlockAt(x, y, z);
                                block.setType(material);
                                blocks.add(block);
                            }
                            break;
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Создает спиральный паттерн
     * @param center центр спирали
     * @param radius радиус спирали
     * @param height высота спирали
     * @param turns количество оборотов
     * @param material тип блока
     * @return список созданных блоков
     */
    public static List<Block> createSpiralPattern(Location center, double radius, double height, double turns, Material material) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        
        int points = (int) (turns * 50);
        
        for (int i = 0; i < points; i++) {
            double t = (double) i / points;
            double angle = t * Math.PI * 2 * turns;
            
            double x = center.getX() + Math.cos(angle) * radius * t;
            double z = center.getZ() + Math.sin(angle) * radius * t;
            double y = center.getY() + height * t;
            
            Block block = world.getBlockAt((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
            block.setType(material);
            blocks.add(block);
        }
        
        return blocks;
    }
    
    /**
     * Создает шахматную доску в трех измерениях
     * @param corner1 первый угол области
     * @param corner2 второй угол области
     * @param material1 первый материал
     * @param material2 второй материал
     * @param size размер клетки
     * @return список созданных блоков
     */
    public static List<Block> create3DCheckerboard(Location corner1, Location corner2, Material material1, Material material2, int size) {
        List<Block> blocks = new ArrayList<>();
        World world = corner1.getWorld();
        
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        NoiseGenerator noiseGen = new NoiseGenerator(System.currentTimeMillis());
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int value = (int) (noiseGen.checkerboard(x, y, size) + noiseGen.checkerboard(y, z, size) + 
                               noiseGen.checkerboard(x, z, size));
                    Material material = (value % 2 == 0) ? material1 : material2;
                    
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(material);
                    blocks.add(block);
                }
            }
        }
        
        return blocks;
    }
}