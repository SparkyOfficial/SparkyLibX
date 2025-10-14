package com.sparky.libx.block;

import com.sparky.libx.math.Vector3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилиты для работы с блоками в Minecraft
 */
public final class BlockUtils {
    
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
        
        // Основные направления (север, юг, запад, восток, верх, низ)
        blocks.add(block.getRelative(BlockFace.UP));
        blocks.add(block.getRelative(BlockFace.DOWN));
        blocks.add(block.getRelative(BlockFace.NORTH));
        blocks.add(block.getRelative(BlockFace.SOUTH));
        blocks.add(block.getRelative(BlockFace.WEST));
        blocks.add(block.getRelative(BlockFace.EAST));
        
        if (includeDiagonals) {
            // Угловые блоки
            blocks.add(block.getRelative(BlockFace.NORTH_EAST));
            blocks.add(block.getRelative(BlockFace.NORTH_WEST));
            blocks.add(block.getRelative(BlockFace.SOUTH_EAST));
            blocks.add(block.getRelative(BlockFace.SOUTH_WEST));
            
            // Верхние угловые блоки
            Block up = block.getRelative(BlockFace.UP);
            blocks.add(up.getRelative(BlockFace.NORTH));
            blocks.add(up.getRelative(BlockFace.SOUTH));
            blocks.add(up.getRelative(BlockFace.EAST));
            blocks.add(up.getRelative(BlockFace.WEST));
            blocks.add(up.getRelative(BlockFace.NORTH_EAST));
            blocks.add(up.getRelative(BlockFace.NORTH_WEST));
            blocks.add(up.getRelative(BlockFace.SOUTH_EAST));
            blocks.add(up.getRelative(BlockFace.SOUTH_WEST));
            
            // Нижние угловые блоки
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
     * Проверяет, может ли игорь видеть указанный блок
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
        
        // Проверяем расстояние
        Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
        if (viewerLocation.distance(blockCenter) > maxDistance) {
            return false;
        }
        
        // Проверяем, не закрыт ли блок другими блоками
        return viewerLocation.getWorld().rayTraceBlocks(viewerLocation, 
            blockCenter.toVector().subtract(viewerLocation.toVector()).normalize(), 
            maxDistance) == null;
    }
}
