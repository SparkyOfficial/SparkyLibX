package com.sparky.libx.procedural;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.sparky.libx.math.NoiseGenerator;

/**
 * Генератор сложных структур и построек
 * @author Андрій Будильников
 */
public class StructureGenerator {
    
    /**
     * Создает дерево процедурно
     * @param location местоположение основания дерева
     * @param trunkHeight высота ствола
     * @param trunkMaterial материал ствола
     * @param leavesMaterial материал листвы
     * @return список созданных блоков
     */
    public static List<Block> generateTree(Location location, int trunkHeight, Material trunkMaterial, Material leavesMaterial) {
        List<Block> blocks = new ArrayList<>();
        World world = location.getWorld();
        if (world == null) return blocks;
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        for (int i = 0; i < trunkHeight; i++) {
            Block block = world.getBlockAt(x, y + i, z);
            block.setType(trunkMaterial);
            blocks.add(block);
        }
        
        int leafHeight = y + trunkHeight;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distance <= 2.5) {
                        if (Math.random() > 0.3) {
                            Block block = world.getBlockAt(x + dx, leafHeight + dy, z + dz);
                            if (block.getType() != trunkMaterial) {
                                block.setType(leavesMaterial);
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
     * Создает кристальную пещеру
     * @param center центр пещеры
     * @param radius радиус пещеры
     * @param crystalMaterial материал кристаллов
     * @param baseMaterial основной материал пещеры
     * @return список созданных блоков
     */
    public static List<Block> generateCrystalCave(Location center, double radius, Material crystalMaterial, Material baseMaterial) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        if (world == null) return blocks;
        
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        int radiusCeil = (int) Math.ceil(radius);
        
        for (int x = -radiusCeil; x <= radiusCeil; x++) {
            for (int y = -radiusCeil; y <= radiusCeil; y++) {
                for (int z = -radiusCeil; z <= radiusCeil; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance <= radius) {
                        Block block = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
                        if (block.getType() != Material.AIR) {
                            block.setType(Material.AIR);
                            blocks.add(block);
                        }
                    }
                }
            }
        }
        
        NoiseGenerator noiseGen = new NoiseGenerator(System.currentTimeMillis());
        
        for (int x = -radiusCeil; x <= radiusCeil; x++) {
            for (int y = -radiusCeil; y <= radiusCeil; y++) {
                for (int z = -radiusCeil; z <= radiusCeil; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance >= radius - 1 && distance <= radius) {
                        double noise = noiseGen.perlinNoise(
                            (centerX + x) * 0.2,
                            (centerY + y) * 0.2,
                            (centerZ + z) * 0.2
                        );
                        
                        if (noise > 0.4) {
                            Block block = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
                            if (block.getType() == Material.AIR) {
                                block.setType(crystalMaterial);
                                blocks.add(block);
                            }
                        }
                    }
                }
            }
        }
        
        for (int x = -radiusCeil; x <= radiusCeil; x++) {
            for (int z = -radiusCeil; z <= radiusCeil; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= radius) {
                    Block block = world.getBlockAt(centerX + x, centerY - (int)radius, centerZ + z);
                    if (block.getType() == Material.AIR) {
                        block.setType(baseMaterial);
                        blocks.add(block);
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Создает лабиринт
     * @param corner1 первый угол
     * @param corner2 второй угол
     * @param wallMaterial материал стен
     * @param pathWidth ширина проходов
     * @return список созданных блоков
     */
    public static List<Block> generateMaze(Location corner1, Location corner2, Material wallMaterial, int pathWidth) {
        List<Block> blocks = new ArrayList<>();
        World world = corner1.getWorld();
        if (world == null || !world.equals(corner2.getWorld())) return blocks;
        
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        int width = (maxX - minX) / pathWidth;
        int depth = (maxZ - minZ) / pathWidth;
        
        boolean[][] maze = new boolean[width][depth];
        
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                maze[x][z] = true;
            }
        }
        
        generateMazeRecursive(maze, 1, 1);
        
        int startY = corner1.getBlockY();
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                if (maze[x][z]) {
                    for (int y = 0; y < 3; y++) {
                        for (int dx = 0; dx < pathWidth; dx++) {
                            for (int dz = 0; dz < pathWidth; dz++) {
                                Block block = world.getBlockAt(
                                    minX + x * pathWidth + dx,
                                    startY + y,
                                    minZ + z * pathWidth + dz
                                );
                                block.setType(wallMaterial);
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
     * Рекурсивный алгоритм генерации лабиринта
     */
    private static void generateMazeRecursive(boolean[][] maze, int x, int z) {
        maze[x][z] = false;
        
        int[] directions = {0, 1, 2, 3};
        shuffleArray(directions);
        
        for (int i = 0; i < directions.length; i++) {
            int dx = 0, dz = 0;
            
            switch (directions[i]) {
                case 0: dx = 0; dz = -2; break;
                case 1: dx = 2; dz = 0; break;
                case 2: dx = 0; dz = 2; break;
                case 3: dx = -2; dz = 0; break;
            }
            
            int nx = x + dx;
            int nz = z + dz;
            
            if (nx > 0 && nx < maze.length - 1 && nz > 0 && nz < maze[0].length - 1) {
                if (maze[nx][nz]) {
                    maze[x + dx/2][z + dz/2] = false;
                    generateMazeRecursive(maze, nx, nz);
                }
            }
        }
    }
    
    /**
     * Перемешивает массив
     */
    private static void shuffleArray(int[] array) {
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
    
    /**
     * Создает спиральную башню
     * @param center центр башни
     * @param height высота башни
     * @param radius радиус башни
     * @param material материал башни
     * @param turns количество оборотов
     * @return список созданных блоков
     */
    public static List<Block> generateSpiralTower(Location center, int height, double radius, Material material, double turns) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        if (world == null) return blocks;
        
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        for (int x = -((int)radius); x <= (int)radius; x++) {
            for (int z = -((int)radius); z <= (int)radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= radius) {
                    Block block = world.getBlockAt(centerX + x, centerY, centerZ + z);
                    block.setType(material);
                    blocks.add(block);
                }
            }
        }
        
        for (int y = 1; y < height; y++) {
            double angle = (double) y / height * Math.PI * 2 * turns;
            int x = (int) (Math.cos(angle) * radius);
            int z = (int) (Math.sin(angle) * radius);
            
            Block block = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
            block.setType(material);
            blocks.add(block);
            
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= 1) {
                        Block surrounding = world.getBlockAt(centerX + x + dx, centerY + y, centerZ + z + dz);
                        if (surrounding.getType() == Material.AIR) {
                            surrounding.setType(material);
                            blocks.add(surrounding);
                        }
                    }
                }
            }
        }
        
        return blocks;
    }
}