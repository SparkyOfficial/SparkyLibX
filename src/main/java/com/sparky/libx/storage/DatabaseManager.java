package com.sparky.libx.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sparky.libx.region.Region;

/**
 * Менеджер базы данных для хранения регионов и прав доступа
 * @author Андрій Будильников
 */
public class DatabaseManager {
    private final Plugin plugin;
    private final DataSource dataSource;
    private final ExecutorService dbExecutor;
    
    private final Map<UUID, Region> regionCache = new ConcurrentHashMap<>();
    private final Map<String, UUID> nameToIdMap = new ConcurrentHashMap<>();
    private final Set<UUID> modifiedRegions = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingDeletions = ConcurrentHashMap.newKeySet();
    
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    
    private static final int DB_OPERATION_TIMEOUT = 30;
    
    public DatabaseManager(Plugin plugin, DataSource dataSource) {
        this.plugin = plugin;
        this.dataSource = dataSource;
        
        int poolSize = Math.max(4, Runtime.getRuntime().availableProcessors());
        this.dbExecutor = new ThreadPoolExecutor(
            2,
            poolSize,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);
                
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "SparkyLibX-DB-Worker-" + counter.incrementAndGet());
                    t.setDaemon(true);
                    t.setPriority(Thread.MIN_PRIORITY + 1);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        initializeDatabase();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllModified();
            }
        }.runTaskTimerAsynchronously(plugin, 6000, 6000);
    }
    
    /**
     * Создает таблицы в БД если их еще нет
     */
    private void initializeDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA journal_mode=WAL;");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS regions (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "name VARCHAR(64) NOT NULL, " +
                "world VARCHAR(64) NOT NULL, " +
                "data TEXT NOT NULL, " +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(name)" +
            ")");
            
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_regions_world ON regions(world)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_regions_name ON regions(LOWER(name))");
            
            stmt.execute("ANALYZE;");
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось инициализировать БД: " + e.getMessage());
            throw new RuntimeException("Ошибка инициализации БД", e);
        }
        
        initializePermissionTables();
    }
    
    /**
     * Загружает регион по ID из БД
     */
    public CompletableFuture<Region> loadRegion(UUID regionId) {
        Region cached = regionCache.get(regionId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT data FROM regions WHERE id = ?")) {
                
                stmt.setString(1, regionId.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String data = rs.getString("data");
                        Region region = RegionSerializer.deserialize(data);
                        
                        if (region != null) {
                            regionCache.put(regionId, region);
                            nameToIdMap.put(region.getName().toLowerCase(), regionId);
                        }
                        
                        return region;
                    }
                }
                
                return null;
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка загрузки региона: " + e.getMessage());
                throw new RuntimeException("Ошибка БД", e);
            }
        }, runAsync());
    }
    
    /**
     * Загружает регион по имени
     */
    public CompletableFuture<Region> loadRegion(String name) {
        UUID cachedId = nameToIdMap.get(name.toLowerCase());
        if (cachedId != null) {
            return loadRegion(cachedId);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id FROM regions WHERE LOWER(name) = LOWER(?)")) {
                
                stmt.setString(1, name);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        UUID regionId = UUID.fromString(rs.getString("id"));
                        return loadRegion(regionId).join();
                    }
                }
                
                return null;
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка загрузки региона по имени: " + e.getMessage());
                throw new RuntimeException("Ошибка БД", e);
            }
        }, runAsync());
    }
    
    /**
     * Сохраняет регион (асинхронно)
     */
    public CompletableFuture<Void> saveRegion(Region region) {
        UUID regionId = getOrCreateRegionId(region);
        modifiedRegions.add(regionId);
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Немедленно сохраняет регион
     */
    public CompletableFuture<Void> saveRegionNow(Region region) {
        UUID regionId = getOrCreateRegionId(region);
        return saveRegionInternal(regionId, region);
    }
    
    /**
     * Удаляет регион из БД
     */
    public CompletableFuture<Boolean> deleteRegion(UUID regionId) {
        pendingDeletions.add(regionId);
        modifiedRegions.remove(regionId);
        
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM regions WHERE id = ?")) {
                    
                    stmt.setString(1, regionId.toString());
                    int affected = stmt.executeUpdate();
                    
                    if (affected > 0) {
                        Region removed = regionCache.remove(regionId);
                        if (removed != null) {
                            nameToIdMap.remove(removed.getName().toLowerCase());
                        }
                        conn.commit();
                        return true;
                    }
                    
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка удаления региона: " + e.getMessage());
                throw new RuntimeException("Ошибка БД", e);
            }
        }, runAsync());
    }
    
    /**
     * Сохраняет все измененные регионы
     */
    public void saveAllModified() {
        if (modifiedRegions.isEmpty() && pendingDeletions.isEmpty()) {
            return;
        }
        
        Set<UUID> regionsToSave = new HashSet<>(modifiedRegions);
        Set<UUID> regionsToDelete = new HashSet<>(pendingDeletions);
        
        modifiedRegions.removeAll(regionsToSave);
        pendingDeletions.removeAll(regionsToDelete);
        
        CompletableFuture.runAsync(() -> {
            for (UUID regionId : regionsToSave) {
                Region region = regionCache.get(regionId);
                if (region != null) {
                    saveRegionInternal(regionId, region).join();
                }
            }
            
            for (UUID regionId : regionsToDelete) {
                deleteRegion(regionId).join();
            }
        }, runAsync());
    }
    
    /**
     * Внутренний метод сохранения региона
     */
    private CompletableFuture<Void> saveRegionInternal(UUID regionId, Region region) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO regions (id, name, world, data) VALUES (?, ?, ?, ?)")) {
                    
                    stmt.setString(1, regionId.toString());
                    stmt.setString(2, region.getName());
                    stmt.setString(3, region.getWorld().getName());
                    stmt.setString(4, RegionSerializer.serialize(region));
                    
                    stmt.executeUpdate();
                    conn.commit();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка сохранения региона: " + e.getMessage());
                throw new RuntimeException("Ошибка БД", e);
            }
        }, runAsync());
    }
    
    /**
     * Получает или создает ID для региона
     */
    private UUID getOrCreateRegionId(Region region) {
        UUID existingId = nameToIdMap.get(region.getName().toLowerCase());
        if (existingId != null) {
            return existingId;
        }
        
        UUID newId = UUID.randomUUID();
        nameToIdMap.put(region.getName().toLowerCase(), newId);
        return newId;
    }
    
    /**
     * Закрывает соединения с БД
     */
    public void close() {
        saveAllModified();
        
        if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
            ((com.zaxxer.hikari.HikariDataSource) dataSource).close();
        }
        
        dbExecutor.shutdown();
        try {
            if (!dbExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            dbExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Получает исполнитель для асинхронных операций
     */
    private Executor runAsync() {
        return dbExecutor;
    }
    
    /**
     * Инициализирует таблицы прав доступа
     */
    private void initializePermissionTables() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS region_permissions (" +
                "region_id VARCHAR(36) NOT NULL, " +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "permission_type VARCHAR(32) NOT NULL, " +
                "allowed BOOLEAN NOT NULL DEFAULT FALSE, " +
                "PRIMARY KEY (region_id, player_uuid, permission_type), " +
                "FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE" +
            ")");
            
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_permissions_region ON region_permissions(region_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_permissions_player ON region_permissions(player_uuid)");
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось инициализировать таблицы прав доступа: " + e.getMessage());
            throw new RuntimeException("Ошибка инициализации БД", e);
        }
    }
    
    /**
     * Устанавливает право доступа для игрока к региону
     */
    public CompletableFuture<Void> setPermission(UUID regionId, UUID playerUuid, String permissionType, boolean allowed) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO region_permissions (region_id, player_uuid, permission_type, allowed) VALUES (?, ?, ?, ?)")) {
                    
                    stmt.setString(1, regionId.toString());
                    stmt.setString(2, playerUuid.toString());
                    stmt.setString(3, permissionType);
                    stmt.setBoolean(4, allowed);
                    
                    stmt.executeUpdate();
                    conn.commit();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка установки прав доступа: " + e.getMessage());
                throw new RuntimeException("Ошибка БД", e);
            }
        }, runAsync());
    }
    
    /**
     * Проверяет, имеет ли игрок право доступа к региону
     */
    public CompletableFuture<Boolean> hasPermission(UUID regionId, UUID playerUuid, String permissionType) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT allowed FROM region_permissions WHERE region_id = ? AND player_uuid = ? AND permission_type = ?")) {
                
                stmt.setString(1, regionId.toString());
                stmt.setString(2, playerUuid.toString());
                stmt.setString(3, permissionType);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("allowed");
                    }
                    
                    return false;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка проверки прав доступа: " + e.getMessage());
                throw new RuntimeException("Ошибка БД", e);
            }
        }, runAsync());
    }
    
    /**
     * Удаляет все права доступа для игрока
     */
    public CompletableFuture<Void> removePlayerPermissions(UUID playerUuid) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM region_permissions WHERE player_uuid = ?")) {
                
                stmt.setString(1, playerUuid.toString());
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка удаления прав доступа игрока: " + e.getMessage());
                throw new RuntimeException("Ошибка БД", e);
            }
        }, runAsync());
    }
    
    /**
     * Удаляет все права доступа для региона
     */
    public CompletableFuture<Void> removeRegionPermissions(UUID regionId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM region_permissions WHERE region_id = ?")) {
                
                stmt.setString(1, regionId.toString());
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка удаления прав доступа региона: " + e.getMessage());
                throw new RuntimeException("Ошибка БД", e);
            }
        }, runAsync());
    }
    
    /**
     * Получает все права доступа для региона
     */
    public CompletableFuture<Map<UUID, Map<String, Boolean>>> getRegionPermissions(UUID regionId) {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Map<String, Boolean>> permissions = new HashMap<>();
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT player_uuid, permission_type, allowed FROM region_permissions WHERE region_id = ?")) {
                
                stmt.setString(1, regionId.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));
                        String permissionType = rs.getString("permission_type");
                        boolean allowed = rs.getBoolean("allowed");
                        
                        permissions.computeIfAbsent(playerUuid, k -> new HashMap<>())
                                  .put(permissionType, allowed);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка получения прав доступа региона: " + e.getMessage());
                throw new RuntimeException("Ошибка БД", e);
            }
            
            return permissions;
        }, runAsync());
    }
    
    /**
     * Получает все права доступа для игрока
     */
    public CompletableFuture<Map<UUID, Map<String, Boolean>>> getPlayerPermissions(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Map<String, Boolean>> permissions = new HashMap<>();
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT region_id, permission_type, allowed FROM region_permissions WHERE player_uuid = ?")) {
                
                stmt.setString(1, playerUuid.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        UUID regionId = UUID.fromString(rs.getString("region_id"));
                        String permissionType = rs.getString("permission_type");
                        boolean allowed = rs.getBoolean("allowed");
                        
                        permissions.computeIfAbsent(regionId, k -> new HashMap<>())
                                  .put(permissionType, allowed);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка получения прав доступа игрока: " + e.getMessage());
                throw new RuntimeException("Ошибка БД", e);
            }
            
            return permissions;
        }, runAsync());
    }
}