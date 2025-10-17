package com.sparky.libx.example;

import com.sparky.libx.SparkyLibX;
import com.sparky.libx.region.CuboidRegion;
import com.sparky.libx.region.SphereRegion;
import com.sparky.libx.region.Region;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * демонстрація можливостей бібліотеки SparkyLibX
 * @author Андрій Будильников
 */
public class SparkyLibXDemo extends JavaPlugin {
    
    private SparkyLibX sparkyLib;
    
    @Override
    public void onEnable() {
        // отримуємо екземпляр бібліотеки
        sparkyLib = SparkyLibX.getInstance();
        
        if (sparkyLib != null) {
            getLogger().info("запуск демо режиму sparkylibx...");
            setupDemoRegions();
        } else {
            getLogger().severe("не вдалося отримати екземпляр sparkylibx!");
        }
    }
    
    /**
     * налаштовуємо демо регіони для показу можливостей
     */
    private void setupDemoRegions() {
        try {
            // отримуємо світ для створення регіонів
            World world = getServer().getWorlds().get(0);
            
            // створюємо кубоїдний регіон
            Location corner1 = new Location(world, 0, 64, 0);
            Location corner2 = new Location(world, 10, 74, 10);
            CuboidRegion cuboidRegion = new CuboidRegion("demo_cuboid", corner1, corner2);
            
            // створюємо сферичний регіон
            Location center = new Location(world, 20, 64, 20);
            SphereRegion sphereRegion = new SphereRegion("demo_sphere", center, 5.0);
            
            // реєструємо регіони в менеджері
            sparkyLib.getRegionManager().registerRegion(cuboidRegion);
            sparkyLib.getRegionManager().registerRegion(sphereRegion);
            
            getLogger().info("демо регіони створено успішно!");
            
        } catch (Exception e) {
            getLogger().severe("помилка при створенні демо регіонів: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable() {
        getLogger().info("демо режим вимкнено!");
    }
}