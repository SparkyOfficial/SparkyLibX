package com.sparky.libx.region;

import com.sparky.libx.event.RegionEvent;
import com.sparky.libx.math.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер регионов для отслеживания входа/выхода игроков
 */
public class RegionManager implements Listener {
    
    private final Map<String, Region> regions = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> playerRegions = new ConcurrentHashMap<>();
    private final Plugin plugin;
    
    public RegionManager(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Регистрирует новый регион
     */
    public void registerRegion(Region region) {
        regions.put(region.getName().toLowerCase(), region);
    }
    
    /**
     * Удаляет регион
     */
    public void unregisterRegion(String name) {
        regions.remove(name.toLowerCase());
    }
    
    /**
     * Получает регион по имени
     */
    public Region getRegion(String name) {
        return regions.get(name.toLowerCase());
    }
    
    /**
     * Получает все регионы, содержащие точку
     */
    public Set<Region> getRegionsAt(Location location) {
        Set<Region> result = new HashSet<>();
        for (Region region : regions.values()) {
            if (region.contains(location)) {
                result.add(region);
            }
        }
        return result;
    }
    
    /**
     * Обработчик перемещения игрока
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        
        // Пропускаем, если координаты не изменились
        if (to == null || (to.getX() == from.getX() && to.getY() == from.getY() && to.getZ() == from.getZ())) {
            return;
        }
        
        // Получаем регионы, в которых находится игрок
        Set<Region> currentRegions = getRegionsAt(to);
        Set<Region> previousRegions = getRegionsAt(from);
        
        // Проверяем, вышел ли игрок из каких-то регионов
        for (Region region : previousRegions) {
            if (!currentRegions.contains(region)) {
                handleRegionLeave(player, region, to);
            }
        }
        
        // Проверяем, вошел ли игрок в какие-то регионы
        for (Region region : currentRegions) {
            if (!previousRegions.contains(region)) {
                handleRegionEnter(player, region, to);
            }
        }
    }
    
    private void handleRegionEnter(Player player, Region region, Location location) {
        // Добавляем регион в список регионов игрока
        playerRegions.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>())
                    .add(region.getName().toLowerCase());
        
        // Вызываем событие
        RegionEvent event = new RegionEvent(player, location, region.getName(), 
                                          RegionEvent.RegionAction.ENTER);
        Bukkit.getPluginManager().callEvent(event);
    }
    
    private void handleRegionLeave(Player player, Region region, Location location) {
        // Удаляем регион из списка регионов игрока
        playerRegions.computeIfPresent(player.getUniqueId(), (k, v) -> {
            v.remove(region.getName().toLowerCase());
            return v.isEmpty() ? null : v;
        });
        
        // Вызываем событие
        RegionEvent event = new RegionEvent(player, location, region.getName(), 
                                          RegionEvent.RegionAction.LEAVE);
        Bukkit.getPluginManager().callEvent(event);
    }
    
    /**
     * Получает все регионы, в которых находится игрок
     */
    public Set<Region> getPlayerRegions(Player player) {
        Set<String> regionNames = playerRegions.getOrDefault(player.getUniqueId(), Collections.emptySet());
        Set<Region> result = new HashSet<>();
        
        for (String name : regionNames) {
            Region region = regions.get(name);
            if (region != null) {
                result.add(region);
            }
        }
        
        return result;
    }
    
    /**
     * Проверяет, находится ли игрок в указанном регионе
     */
    public boolean isPlayerInRegion(Player player, String regionName) {
        Set<String> regions = playerRegions.get(player.getUniqueId());
        return regions != null && regions.contains(regionName.toLowerCase());
    }
}
