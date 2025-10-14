package com.sparky.libx.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.bukkit.plugin.Plugin;

import com.sparky.libx.region.Region;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Система хранения регионов в базе данных
 * @author Андрій Будильников
 */
public class RegionStorage {
    
    private final Plugin plugin;
    private final DataSource dataSource;
    private final Logger logger;
    
    public RegionStorage(Plugin plugin, String jdbcUrl, String username, String password) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("SparkyLibX-RegionPool");
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setLeakDetectionThreshold(60000);
        
        this.dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS regions (
                    id UUID PRIMARY KEY,
                    name VARCHAR(64) NOT NULL,
                    world VARCHAR(64) NOT NULL,
                    type VARCHAR(32) NOT NULL,
                    data TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (name, world)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS region_permissions (
                    region_id UUID NOT NULL,
                    player_id UUID NOT NULL,
                    permission VARCHAR(64) NOT NULL,
                    value BOOLEAN NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (region_id, player_id, permission),
                    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
                )
            """);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_regions_world ON regions(world)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_region_permissions_region ON region_permissions(region_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_region_permissions_player ON region_permissions(player_id)");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    /**
     * Создает хранилище с H2 в файле плагина
     */
    public static RegionStorage createFileStorage(Plugin plugin) {
        String dbPath = new File(plugin.getDataFolder(), "regions").getAbsolutePath();
        String jdbcUrl = "jdbc:h2:" + dbPath + ";MODE=MySQL;AUTO_SERVER=TRUE";
        return new RegionStorage(plugin, jdbcUrl, "sa", "");
    }
    
    /**
     * Загружает все регионы для указанного мира
     */
    public CompletableFuture<List<Region>> loadRegions(String worldName) {
        return CompletableFuture.supplyAsync(() -> {
            List<Region> regions = new ArrayList<>();
            
            String sql = "SELECT id, name, type, data FROM regions WHERE world = ?";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, worldName);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        try {
                            Region region = Region.deserialize(
                                Map.of(
                                    "id", UUID.fromString(rs.getString("id")),
                                    "name", rs.getString("name"),
                                    "world", worldName,
                                    "type", rs.getString("type"),
                                    "data", rs.getString("data")
                                )
                            );
                            regions.add(region);
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Failed to load region: " + rs.getString("name"), e);
                        }
                    }
                }
                
                return regions;
                
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load regions", e);
            }
        }, runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }
    
    /**
     * Сохраняет регион в базу данных
     */
    public CompletableFuture<Void> saveRegion(Region region) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO regions (id, name, world, type, data, updated_at)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    type = VALUES(type),
                    data = VALUES(data),
                    updated_at = CURRENT_TIMESTAMP
            """;
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, UUID.randomUUID().toString());
                stmt.setString(2, region.getName());
                stmt.setString(3, region.getWorld().getName());
                stmt.setString(4, getRegionType(region));
                stmt.setString(5, RegionSerializer.serialize(region));
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save region: " + region.getName(), e);
            }
        }, runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }
    
    /**
     * Удаляет регион из базы данных
     */
    public CompletableFuture<Boolean> deleteRegion(String name, String world) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM regions WHERE name = ? AND world = ?";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, name);
                stmt.setString(2, world);
                
                int affected = stmt.executeUpdate();
                return affected > 0;
                
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete region: " + name, e);
            }
        }, runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }
    
    /**
     * Проверяет наличие прав у игрока в регионе
     */
    public CompletableFuture<Boolean> hasPermission(UUID playerId, String regionName, String world, String permission) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                SELECT rp.value 
                FROM region_permissions rp
                JOIN regions r ON rp.region_id = r.id
                WHERE r.name = ? AND r.world = ? AND rp.player_id = ? AND rp.permission = ?
            """;
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, regionName);
                stmt.setString(2, world);
                stmt.setString(3, playerId.toString());
                stmt.setString(4, permission);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("value");
                    }
                }

                return false;
                
            } catch (SQLException e) {
                throw new RuntimeException("Failed to check permission", e);
            }
        }, runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }
    
    /**
     * Устанавливает право для игрока в регионе
     */
    public CompletableFuture<Void> setPermission(UUID playerId, String regionName, String world, 
                                               String permission, boolean value) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO region_permissions (region_id, player_id, permission, value)
                SELECT r.id, ?, ?, ?
                FROM regions r
                WHERE r.name = ? AND r.world = ?
                ON DUPLICATE KEY UPDATE value = VALUES(value)
            """;
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, playerId.toString());
                stmt.setString(2, permission);
                stmt.setBoolean(3, value);
                stmt.setString(4, regionName);
                stmt.setString(5, world);
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                throw new RuntimeException("Failed to set permission", e);
            }
        }, runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }
    
    /**
     * Закрывает соединения с базой данных
     */
    public void close() {
        if (dataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) dataSource).close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to close database connection", e);
            }
        }
    }
    
    private String getRegionType(Region region) {
        String className = region.getClass().getSimpleName();
        return className.replace("Region", "").toLowerCase();
    }
    
    private boolean getDefaultPermission(String permission) {
        return false;
    }
}