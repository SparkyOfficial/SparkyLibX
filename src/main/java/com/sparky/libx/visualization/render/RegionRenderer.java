package com.sparky.libx.visualization.render;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.sparky.libx.region.Region;

/**
 * Базовый интерфейс для рендереров регионов
 * @author Андрій Будильников
 */
public interface RegionRenderer {
    /**
     * Отрисовывает регион для игрока
     */
    void render(Player player, Region region);
    
    /**
     * Очищает визуализацию для игрока
     */
    void clear(Player player);
    
    /**
     * Проверяет, виден ли регион из указанной позиции
     */
    default boolean isVisibleFrom(Region region, Location viewPoint) {
        // находятся ли регион и точка просмотра в одном мире
        if (!region.getWorld().equals(viewPoint.getWorld())) {
            return false;
        }
        
        // получаение центра региона
        Location center = region.getCenter();
        
        // ограничение в 128 блоков для производительности
        if (viewPoint.distanceSquared(center) > 128 * 128) {
            return false;
        }
        
        // rayTraceBlocks для проверки преград
        Vector direction = center.toVector().subtract(viewPoint.toVector()).normalize();
        return viewPoint.getWorld().rayTraceBlocks(viewPoint, direction, 128, 
            org.bukkit.FluidCollisionMode.NEVER, true) == null;
    }
    
    /**
     * Проверяет, находится ли точка в поле зрения игрока
     */
    default boolean isInView(Player player, Location point) {
        Location eyeLocation = player.getEyeLocation();
        return eyeLocation.getWorld().equals(point.getWorld()) &&
               eyeLocation.getDirection().dot(
                   point.toVector().subtract(eyeLocation.toVector()).normalize()) > 0;
    }
}