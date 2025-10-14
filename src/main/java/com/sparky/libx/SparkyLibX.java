package com.sparky.libx;

import com.sparky.libx.region.RegionManager;
import com.sparky.libx.storage.DatabaseManager;
import com.sparky.libx.visualization.RegionVisualizationManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;

/**
 * Главный класс плагина SparkyLibX
 * @author Андрій Будильников
 */
public class SparkyLibX extends JavaPlugin {
    private static SparkyLibX instance;
    private DatabaseManager databaseManager;
    private RegionManager regionManager;
    private RegionVisualizationManager visualizationManager;
    
    @Override
    public void onEnable() {
        instance = this;
        

        initializeDatabase();
        

        this.regionManager = new RegionManager(this);
        

        this.visualizationManager = new RegionVisualizationManager(this);
        

        registerCommands();
        registerListeners();
        
        getLogger().info("SparkyLibX успешно загружен!");
    }
    
    @Override
    public void onDisable() {

        if (visualizationManager != null) {
            visualizationManager.stopAll();
        }
        

        if (databaseManager != null) {
            databaseManager.close();
        }
        
        getLogger().info("SparkyLibX успешно выключен!");
    }
    
    private void initializeDatabase() {
        try {


            this.databaseManager = new DatabaseManager(this, dataSource);
        } catch (Exception e) {
            getLogger().severe("Не удалось инициализировать базу данных: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    private void registerCommands() {

    }
    
    private void registerListeners() {

    }
    

    
    public static SparkyLibX getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public RegionManager getRegionManager() {
        return regionManager;
    }
    
    public RegionVisualizationManager getVisualizationManager() {
        return visualizationManager;
    }
}
