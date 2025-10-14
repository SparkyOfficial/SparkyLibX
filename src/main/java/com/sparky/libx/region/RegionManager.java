package com.sparky.libx.region;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import com.sparky.libx.event.RegionEvent;

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
        
        if (to == null || (to.getX() == from.getX() && to.getY() == from.getY() && to.getZ() == from.getZ())) {
            return;
        }
        
        Set<Region> currentRegions = getRegionsAt(to);
        Set<Region> previousRegions = getRegionsAt(from);
        
        for (Region region : previousRegions) {
            if (!currentRegions.contains(region)) {
                handleRegionLeave(player, region, to);
            }
        }
        
        for (Region region : currentRegions) {
            if (!previousRegions.contains(region)) {
                handleRegionEnter(player, region, to);
            }
        }
    }
    
    private void handleRegionEnter(Player player, Region region, Location location) {
        playerRegions.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>())
                    .add(region.getName().toLowerCase());
        
        RegionEvent event = new RegionEvent(player, location, region.getName(), 
                                          RegionEvent.RegionAction.ENTER);
        Bukkit.getPluginManager().callEvent(event);
    }
    
    private void handleRegionLeave(Player player, Region region, Location location) {
        playerRegions.computeIfPresent(player.getUniqueId(), (k, v) -> {
            v.remove(region.getName().toLowerCase());
            return v.isEmpty() ? null : v;
        });
        
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
    
    /**
     * Получает все зарегистрированные регионы
     */
    public Collection<Region> getRegions() {
        return regions.values();
    }
}