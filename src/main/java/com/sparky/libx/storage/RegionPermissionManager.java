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
        // Спочатку перевіряємо кеш
        Map<UUID, Set<String>> regionPermissions = permissionCache.get(regionName.toLowerCase());
        if (regionPermissions != null) {
            Set<String> playerPermissions = regionPermissions.get(playerId);
            if (playerPermissions != null && playerPermissions.contains(permission)) {
                return CompletableFuture.completedFuture(true);
            }
        }
        
        // Якщо немає в кеші, перевіряємо в БД
        return databaseManager.hasPermission(playerId, regionName, permission);
    }
    
    /**
     * Встановлює право для гравця в регіоні
     */
    public CompletableFuture<Void> setPermission(UUID playerId, String regionName, String permission, boolean value) {
        // Оновлюємо кеш
        permissionCache.computeIfAbsent(regionName.toLowerCase(), k -> new ConcurrentHashMap<>())
                      .computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                      .add(permission);
        
        // Оновлюємо в БД
        return databaseManager.setPermission(playerId, regionName, permission, value);
    }
    
    /**
     * Видаляє право для гравця в регіоні
     */
    public CompletableFuture<Void> removePermission(UUID playerId, String regionName, String permission) {
        // Видаляємо з кешу
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
        
        // Видаляємо з БД
        return databaseManager.removePermission(playerId, regionName, permission);
    }
    
    /**
     * Отримує всі права гравця в регіоні
     */
    public CompletableFuture<Set<String>> getPlayerPermissions(UUID playerId, String regionName) {
        return databaseManager.getPlayerPermissions(playerId, regionName);
    }
    
    /**
     * Отримує всіх гравців з певним правом в регіоні
     */
    public CompletableFuture<Map<UUID, Boolean>> getPlayersWithPermission(String regionName, String permission) {
        return databaseManager.getPlayersWithPermission(regionName, permission);
    }
    
    /**
     * Кешує всі права для регіону
     */
    public CompletableFuture<Void> cacheRegionPermissions(String regionName) {
        return databaseManager.getAllRegionPermissions(regionName)
            .thenAccept(permissions -> {
                permissionCache.put(regionName.toLowerCase(), permissions);
            });
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