package com.sparky.libx.storage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.plugin.Plugin;

/**
 * Менеджер прав доступу до регіонів
 * @author Андрій Будильников
 */
public class RegionPermissionManager {
    
    private final Plugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<String, Map<UUID, Set<String>>> permissionCache = new ConcurrentHashMap<>();
    
    public RegionPermissionManager(Plugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }
    
    /**
     * Перевіряє, чи має гравець певне право в регіоні
     */
    public CompletableFuture<Boolean> hasPermission(UUID playerId, String regionName, String permission) {
        Map<UUID, Set<String>> regionPermissions = permissionCache.get(regionName.toLowerCase());
        if (regionPermissions != null) {
            Set<String> playerPermissions = regionPermissions.get(playerId);
            if (playerPermissions != null && playerPermissions.contains(permission)) {
                return CompletableFuture.completedFuture(true);
            }
        }
        
        return CompletableFuture.completedFuture(false);
    }
    
    /**
     * Встановлює право для гравця в регіоні
     */
    public CompletableFuture<Void> setPermission(UUID playerId, String regionName, String permission, boolean value) {
        permissionCache.computeIfAbsent(regionName.toLowerCase(), k -> new ConcurrentHashMap<>())
                      .computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                      .add(permission);
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Видаляє право для гравця в регіоні
     */
    public CompletableFuture<Void> removePermission(UUID playerId, String regionName, String permission) {
        Map<UUID, Set<String>> regionPermissions = permissionCache.get(regionName.toLowerCase());
        if (regionPermissions != null) {
            Set<String> playerPermissions = regionPermissions.get(playerId);
            if (playerPermissions != null) {
                playerPermissions.remove(permission);
                if (playerPermissions.isEmpty()) {
                    regionPermissions.remove(playerId);
                }
            }
            if (regionPermissions.isEmpty()) {
                permissionCache.remove(regionName.toLowerCase());
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Отримує всі права гравця в регіоні
     */
    public CompletableFuture<Set<String>> getPlayerPermissions(UUID playerId, String regionName) {
        Map<UUID, Set<String>> regionPermissions = permissionCache.get(regionName.toLowerCase());
        if (regionPermissions != null) {
            Set<String> playerPermissions = regionPermissions.get(playerId);
            if (playerPermissions != null) {
                return CompletableFuture.completedFuture(playerPermissions);
            }
        }
        return CompletableFuture.completedFuture(ConcurrentHashMap.newKeySet());
    }
    
    /**
     * Отримує всіх гравців з певним правом в регіоні
     */
    public CompletableFuture<Map<UUID, Boolean>> getPlayersWithPermission(String regionName, String permission) {
        Map<UUID, Boolean> result = new ConcurrentHashMap<>();
        Map<UUID, Set<String>> regionPermissions = permissionCache.get(regionName.toLowerCase());
        if (regionPermissions != null) {
            for (Map.Entry<UUID, Set<String>> entry : regionPermissions.entrySet()) {
                if (entry.getValue().contains(permission)) {
                    result.put(entry.getKey(), true);
                }
            }
        }
        return CompletableFuture.completedFuture(result);
    }
    
    /**
     * Кешує всі права для регіону
     */
    public CompletableFuture<Void> cacheRegionPermissions(String regionName) {
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Очищує кеш для регіону
     */
    public void clearRegionCache(String regionName) {
        permissionCache.remove(regionName.toLowerCase());
    }
    
    /**
     * Очищує весь кеш
     */
    public void clearCache() {
        permissionCache.clear();
    }
    
    /**
     * Інвалідує кеш для гравця
     */
    public void invalidatePlayerCache(UUID playerId) {
        for (Map<UUID, Set<String>> regionPermissions : permissionCache.values()) {
            regionPermissions.remove(playerId);
        }
    }
}