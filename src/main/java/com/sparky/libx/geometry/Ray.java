package com.sparky.libx.geometry;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

/**
 * Класс для работы с лучами в 3D пространстве
 * @author Андрій Будильников
 */
public class Ray {
    private final Vector origin;
    private final Vector direction;
    private final World world;
    
    /**
     * Создает луч из начальной точки в заданном направлении
     * @param origin начальная точка
     * @param direction направление (будет нормализовано)
     */
    public Ray(Location origin, Vector direction) {
        this.origin = origin.toVector();
        this.direction = direction.normalize();
        this.world = origin.getWorld();
    }
    
    /**
     * Создает луч между двумя точками
     * @param start начальная точка
     * @param end конечная точка
     */
    public Ray(Location start, Location end) {
        this.origin = start.toVector();
        this.direction = end.toVector().subtract(start.toVector()).normalize();
        this.world = start.getWorld();
    }
    
    /**
     * Выполняет трассировку луча до первого пересечения с блоком
     * @param maxDistance максимальная дистанция трассировки
     * @param ignoreTransparent игнорировать прозрачные блоки
     * @return информация о пересечении или null, если пересечения нет
     */
    public RayHit trace(double maxDistance, boolean ignoreTransparent) {
        if (world == null) return null;
        
        Vector currentPos = origin.clone();
        Vector step = direction.clone().multiply(0.1); // Малый шаг для точности

        
        Block lastBlock = null;
        double distance = 0;
        
        while (distance < maxDistance) {
            currentPos.add(step);
            distance += step.length();
            
            Block block = currentPos.toLocation(world).getBlock();
            

            if (block.equals(lastBlock)) continue;
            
            lastBlock = block;
            

            if (block.getType().isSolid() && (!ignoreTransparent || !block.isPassable())) {

                Vector hitPoint = getExactIntersectionPoint(block, currentPos);
                if (hitPoint != null) {
                    return new RayHit(
                        hitPoint.toLocation(world),
                        block,
                        getHitFace(hitPoint, block.getLocation().toVector()),
                        distance
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Получает все блоки, через которые проходит луч
     * @param maxDistance максимальная дистанция
     * @return список блоков на пути луча
     */
    public List<Block> getBlocks(double maxDistance) {
        List<Block> blocks = new ArrayList<>();
        if (world == null) return blocks;
        
        Vector currentPos = origin.clone();
        Vector step = direction.clone().multiply(0.5); // Больший шаг для производительности

        
        Block lastBlock = null;
        double distance = 0;
        
        while (distance < maxDistance) {
            currentPos.add(step);
            distance += step.length();
            
            Block block = currentPos.toLocation(world).getBlock();
            

            if (!block.equals(lastBlock)) {
                blocks.add(block);
                lastBlock = block;
            }
        }
        
        return blocks;
    }
    
    /**
     * Вычисляет точное местоположение пересечения с блоком
     */
    private Vector getExactIntersectionPoint(Block block, Vector approxPosition) {


        return approxPosition.clone();
    }
    
    /**
     * Определяет, какая грань блока была пересечена
     */
    private BlockFace getHitFace(Vector hitPoint, Vector blockCenter) {
        Vector relativeHit = hitPoint.clone().subtract(blockCenter);
        

        double max = Math.max(Math.max(
            Math.abs(relativeHit.getX()),
            Math.abs(relativeHit.getY())),
            Math.abs(relativeHit.getZ())
        );
        
        if (max == Math.abs(relativeHit.getX())) {
            return relativeHit.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else if (max == Math.abs(relativeHit.getY())) {
            return relativeHit.getY() > 0 ? BlockFace.UP : BlockFace.DOWN;
        } else {
            return relativeHit.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
    }
    
    /**
     * Класс для хранения информации о пересечении луча с блоком
     */
    public static class RayHit {
        private final Location hitPoint;
        private final Block hitBlock;
        private final BlockFace hitFace;
        private final double distance;
        
        public RayHit(Location hitPoint, Block hitBlock, BlockFace hitFace, double distance) {
            this.hitPoint = hitPoint;
            this.hitBlock = hitBlock;
            this.hitFace = hitFace;
            this.distance = distance;
        }
        
        public Location getHitPoint() {
            return hitPoint;
        }
        
        public Block getHitBlock() {
            return hitBlock;
        }
        
        public BlockFace getHitFace() {
            return hitFace;
        }
        
        public double getDistance() {
            return distance;
        }
    }
}
