package com.sparky.libx.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.sparky.libx.region.Region;

/**
 * Работа с базой данных в отдельном потоке
 * author: Андрій Будильников
 */
public class DatabaseManager {
    private final Plugin plugin;
    private final DataSource dataSource;
    private final Map<UUID, BukkitTask> pendingSaves = new ConcurrentHashMap<>();
    private final Set<UUID> modifiedRegions = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingDeletions = ConcurrentHashMap.newKeySet();
    

    private final Map<UUID, Region> regionCache = new ConcurrentHashMap<>();
    private final Map<String, UUID> nameToIdMap = new ConcurrentHashMap<>();
    

    private final ExecutorService dbExecutor;
    

    private static final int BATCH_SIZE = 50;
    

    private static final int DB_OPERATION_TIMEOUT = 30;
    
    public DatabaseManager(Plugin plugin, DataSource dataSource) {
        this.plugin = plugin;
        this.dataSource = dataSource;
        
        // пул потоков для работы с БД
        int poolSize = Math.max(4, Runtime.getRuntime().availableProcessors());
        this.dbExecutor = new ThreadPoolExecutor(
            2, // минимум потоков
            poolSize, // максимум потоков
            60L, TimeUnit.SECONDS, // время простоя перед завершением
            new LinkedBlockingQueue<>(1000), // очередь задач
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
            new ThreadPoolExecutor.CallerRunsPolicy() // если очередь полная, выполняем в текущем потоке
        );
        
        // создаем таблицы если их нет
        initializeDatabase();
        
        // авто-сохранение каждые 5 минут
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllModified();
            }
        }.runTaskTimerAsynchronously(plugin, 6000, 6000); // каждые 5 минут
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
        
        // создаем таблицы для прав доступа
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
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw new CompletionException("Ошибка удаления региона: " + e.getMessage(), e);
                }
                
            } catch (SQLException e) {
                throw new CompletionException("Ошибка БД", e);
            }
        }, dbExecutor);
    }
    
    /**
     * Удаляет несколько регионов сразу
     */
    public CompletableFuture<Integer> deleteRegions(Collection<UUID> regionIds) {
        if (regionIds.isEmpty()) {
            return CompletableFuture.completedFuture(0);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM regions WHERE id = ?")) {
                    
                    int deleted = 0;
                    
                    for (UUID regionId : regionIds) {
                        stmt.setString(1, regionId.toString());
                        stmt.addBatch();
                        

                        Region removed = regionCache.remove(regionId);
                        if (removed != null) {
                            nameToIdMap.remove(removed.getName().toLowerCase());
                        }
                        

                        if (++deleted % BATCH_SIZE == 0) {
                            stmt.executeBatch();
                        }
                    }
                    

                    int[] results = stmt.executeBatch();
                    conn.commit();
                    
                    return Arrays.stream(results).sum();
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw new CompletionException("Ошибка пакетного удаления: " + e.getMessage(), e);
                }
                
            } catch (SQLException e) {
                throw new CompletionException("Ошибка БД при пакетном удалении", e);
            }
        }, dbExecutor);
    }
    
    /**
     * Получает все ID регионов в мире
     */
    public CompletableFuture<List<UUID>> getRegionIdsInWorld(String worldName) {
        return CompletableFuture.supplyAsync(() -> {
            List<UUID> ids = new ArrayList<>();
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id FROM regions WHERE world = ?")) {
                
                stmt.setString(1, worldName);
                stmt.setQueryTimeout(DB_OPERATION_TIMEOUT);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ids.add(UUID.fromString(rs.getString("id")));
                    }
                }
                
                return ids;
                
            } catch (SQLException e) {
                throw new CompletionException("Ошибка загрузки ID регионов: " + e.getMessage(), e);
            }
        }, dbExecutor);
    }
    
    /**
     * Загружает все регионы в мире
     */
    public CompletableFuture<List<Region>> loadRegionsInWorld(String worldName) {
        return CompletableFuture.supplyAsync(() -> {
            List<Region> regions = new ArrayList<>();
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, data FROM regions WHERE world = ?")) {
                
                stmt.setString(1, worldName);
                stmt.setQueryTimeout(DB_OPERATION_TIMEOUT);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        try {
                            String data = rs.getString("data");
                            Region region = RegionSerializer.deserialize(data);
                            if (region != null) {
                                UUID regionId = UUID.fromString(rs.getString("id"));
                                

                                regionCache.put(regionId, region);
                                nameToIdMap.put(region.getName().toLowerCase(), regionId);
                                
                                regions.add(region);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Ошибка десериализации региона: " + e.getMessage());
                        }
                    }
                }
                
                return regions;
                
            } catch (SQLException e) {
                throw new CompletionException("Ошибка загрузки регионов: " + e.getMessage(), e);
            }
        }, dbExecutor);
    }
    
    /**
     * Сохраняет все измененные регионы
     */
    public CompletableFuture<Void> saveAllModified() {
        if (modifiedRegions.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        

        Set<UUID> toSave = new HashSet<>(modifiedRegions);
        modifiedRegions.removeAll(toSave);
        

        List<List<UUID>> batches = partitionList(new ArrayList<>(toSave), BATCH_SIZE);
        

        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = new CompletableFuture[batches.size()];
        

        for (int i = 0; i < batches.size(); i++) {
            List<UUID> batch = batches.get(i);
            futures[i] = saveBatch(batch).exceptionally(e -> {
                plugin.getLogger().warning("Ошибка сохранения пакета: " + e.getMessage());
                return null;
            });
        }
        

        return CompletableFuture.allOf(futures);
    }
    
    /**
     * Сохраняет пакет регионов
     */
    private CompletableFuture<Void> saveBatch(Collection<UUID> regionIds) {
        if (regionIds.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO regions (id, name, world, data, last_updated) VALUES (?, ?, ?, ?, ?)")) {
                    
                    stmt.setQueryTimeout(DB_OPERATION_TIMEOUT);
                    

                    for (UUID regionId : regionIds) {
                        Region region = regionCache.get(regionId);
                        if (region == null) continue;
                        
                        String regionData = RegionSerializer.serialize(region);
                        stmt.setString(1, regionId.toString());
                        stmt.setString(2, region.getName());
                        stmt.setString(3, region.getWorld().getName());
                        stmt.setString(4, regionData);
                        stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                        stmt.addBatch();
                    }
                    

                    stmt.executeBatch();
                    conn.commit();
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw new CompletionException("Ошибка сохранения пакета: " + e.getMessage(), e);
                }
                
            } catch (SQLException e) {
                throw new CompletionException("Ошибка БД при пакетном сохранении", e);
            }
        }, dbExecutor);
    }
    
    /**
     * Закрывает соединения с БД
     */
    public void close() {

        saveAllModified().join();
        

        dbExecutor.shutdown();
        try {
            if (!dbExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            dbExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        

        if (dataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) dataSource).close();
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка закрытия соединения БД: " + e.getMessage());
            }
        }
    }
    
    private UUID getOrCreateRegionId(Region region) {

        UUID existingId = nameToIdMap.get(region.getName().toLowerCase());
        if (existingId != null) {
            return existingId;
        }
        

        UUID newId = UUID.randomUUID();
        nameToIdMap.put(region.getName().toLowerCase(), newId);
        regionCache.put(newId, region);
        
        return newId;
    }
    
    private void scheduleDelayedSave(UUID regionId, long delay) {

        BukkitTask existingTask = pendingSaves.get(regionId);
        if (existingTask != null) {
            existingTask.cancel();
        }
        

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (modifiedRegions.remove(regionId)) {
                    Region region = regionCache.get(regionId);
                    if (region != null) {
                        saveRegionInternal(regionId, region);
                    }
                }
                pendingSaves.remove(regionId);
            }
        }.runTaskLaterAsynchronously(plugin, delay / 50); // мс в тики
        
        pendingSaves.put(regionId, task);
    }
    
    private CompletableFuture<Void> saveRegionInternal(UUID regionId, Region region) {
        return CompletableFuture.runAsync(() -> {
            String regionData = RegionSerializer.serialize(region);
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO regions (id, name, world, data, last_updated) VALUES (?, ?, ?, ?, ?)")) {
                
                stmt.setString(1, regionId.toString());
                stmt.setString(2, region.getName());
                stmt.setString(3, region.getWorld().getName());
                stmt.setString(4, regionData);
                stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                throw new CompletionException("Ошибка сохранения региона: " + e.getMessage(), e);
            }
        }, dbExecutor);
    }
    
    /**
     * Проверяет права игрока в регионе
     */
    public CompletableFuture<Boolean> hasPermission(UUID playerId, String regionName, String permission) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT value FROM region_permissions WHERE player_id = ? AND region_name = ? AND permission = ?")) {
                
                stmt.setString(1, playerId.toString());
                stmt.setString(2, regionName);
                stmt.setString(3, permission);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("value");
                    }
                }
                
                // по умолчанию прав нет
                return false;
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка проверки прав: " + e.getMessage());
                throw new CompletionException("Ошибка БД", e);
            }
        }, dbExecutor);
    }
    
    /**
     * Устанавливает права для игрока
     */
    public CompletableFuture<Void> setPermission(UUID playerId, String regionName, String permission, boolean value) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO region_permissions (player_id, region_name, permission, value) VALUES (?, ?, ?, ?)")) {
                
                stmt.setString(1, playerId.toString());
                stmt.setString(2, regionName);
                stmt.setString(3, permission);
                stmt.setBoolean(4, value);
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка установки прав: " + e.getMessage());
                throw new CompletionException("Ошибка БД", e);
            }
        }, dbExecutor);
    }
    
    /**
     * Удаляет права игрока
     */
    public CompletableFuture<Void> removePermission(UUID playerId, String regionName, String permission) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM region_permissions WHERE player_id = ? AND region_name = ? AND permission = ?")) {
                
                stmt.setString(1, playerId.toString());
                stmt.setString(2, regionName);
                stmt.setString(3, permission);
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка удаления прав: " + e.getMessage());
                throw new CompletionException("Ошибка БД", e);
            }
        }, dbExecutor);
    }
    
    /**
     * Получает все права игрока в регионе
     */
    public CompletableFuture<Set<String>> getPlayerPermissions(UUID playerId, String regionName) {
        return CompletableFuture.supplyAsync(() -> {
            Set<String> permissions = new HashSet<>();
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT permission FROM region_permissions WHERE player_id = ? AND region_name = ?")) {
                
                stmt.setString(1, playerId.toString());
                stmt.setString(2, regionName);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        permissions.add(rs.getString("permission"));
                    }
                }
                
                return permissions;
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка получения прав игрока: " + e.getMessage());
                throw new CompletionException("Ошибка БД", e);
            }
        }, dbExecutor);
    }
    
    /**
     * Получает всех игроков с определенным правом
     */
    public CompletableFuture<Map<UUID, Boolean>> getPlayersWithPermission(String regionName, String permission) {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Boolean> players = new ConcurrentHashMap<>();
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT player_id, value FROM region_permissions WHERE region_name = ? AND permission = ?")) {
                
                stmt.setString(1, regionName);
                stmt.setString(2, permission);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        players.put(UUID.fromString(rs.getString("player_id")), rs.getBoolean("value"));
                    }
                }
                
                return players;
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка получения игроков с правами: " + e.getMessage());
                throw new CompletionException("Ошибка БД", e);
            }
        }, dbExecutor);
    }
    
    /**
     * Получает все права для региона
     */
    public CompletableFuture<Map<UUID, Set<String>>> getAllRegionPermissions(String regionName) {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Set<String>> permissions = new ConcurrentHashMap<>();
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT player_id, permission FROM region_permissions WHERE region_name = ?")) {
                
                stmt.setString(1, regionName);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        UUID playerId = UUID.fromString(rs.getString("player_id"));
                        String permission = rs.getString("permission");
                        
                        permissions.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(permission);
                    }
                }
                
                return permissions;
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка получения всех прав региона: " + e.getMessage());
                throw new CompletionException("Ошибка БД", e);
            }
        }, dbExecutor);
    }
    
    /**
     * Создает таблицы для прав доступа
     */
    private void initializePermissionTables() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // таблица прав доступа к регионам
            stmt.execute("CREATE TABLE IF NOT EXISTS region_permissions (" +
                "player_id VARCHAR(36) NOT NULL, " +
                "region_name VARCHAR(64) NOT NULL, " +
                "permission VARCHAR(64) NOT NULL, " +
                "value BOOLEAN NOT NULL, " +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (player_id, region_name, permission)" +
            ")");
            
            // индексы для быстрого поиска
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_permissions_player ON region_permissions(player_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_permissions_region ON region_permissions(region_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_permissions_permission ON region_permissions(permission)");
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка создания таблиц прав: " + e.getMessage());
            throw new RuntimeException("Ошибка инициализации таблиц прав", e);
        }
    }
    
    /**
     * Разбивает список на части
     */
    private <T> List<List<T>> partitionList(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }
    
    /**
     * Возвращает исполнитель для асинхронных операций
     */
    private java.util.concurrent.Executor runAsync() {
        return dbExecutor;
    }
}