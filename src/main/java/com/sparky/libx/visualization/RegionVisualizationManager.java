package com.sparky.libx.visualization;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sparky.libx.region.Region;
import com.sparky.libx.visualization.render.RegionRenderer;
import com.sparky.libx.visualization.render.RendererFactory;

/**
 * Менеджер визуализации регионов
 */
public class RegionVisualizationManager {
    private final Plugin plugin;
    private final Map<String, RegionRenderer> renderers = new HashMap<>();
    private final Map<UUID, VisualizedRegion> visualizedRegions = new ConcurrentHashMap<>();
    private final RegionRenderer defaultRenderer;
    
    public RegionVisualizationManager(Plugin plugin) {
        this.plugin = plugin;

        this.defaultRenderer = RendererFactory.createDefaultRegionRenderer();

        registerRenderer("default", defaultRenderer);
        registerRenderer("selection", RendererFactory.createDefaultSelectionRenderer());
        registerRenderer("wireframe", RendererFactory.createBlockRenderer(
            org.bukkit.configuration.ConfigurationSection.class.cast(
                Map.of(
                    "type", "block",
                    "material", "GLASS",
                    "wireframe", true,
                    "duration", 200
                )
            )
        ));
    }
    
    /**
     * Регистрирует новый рендерер
     */
    public void registerRenderer(String name, RegionRenderer renderer) {
        renderers.put(name.toLowerCase(), renderer);
    }
    
    /**
     * Получает рендерер по имени
     */
    public RegionRenderer getRenderer(String name) {
        return renderers.getOrDefault(name.toLowerCase(), defaultRenderer);
    }
    
    /**
     * Визуализирует регион для игрока
     */
    public void visualize(Player player, Region region, String style) {
        visualize(player, region, getRenderer(style));
    }
    
    /**
     * Визуализирует регион для игрока с использованием указанного рендерера
     */
    public void visualize(Player player, Region region, RegionRenderer renderer) {
        stopVisualization(player);

        VisualizedRegion visualization = new VisualizedRegion(region, renderer);
        visualizedRegions.put(player.getUniqueId(), visualization);

        renderer.render(player, region);
    }
    
    /**
     * Останавливает визуализацию для игрока
     */
    public void stopVisualization(Player player) {
        VisualizedRegion visualization = visualizedRegions.remove(player.getUniqueId());
        if (visualization != null) {
            visualization.getRenderer().clear(player);
        }
    }
    
    /**
     * Останавливает все активные визуализации
     */
    public void stopAll() {
        for (Map.Entry<UUID, VisualizedRegion> entry : visualizedRegions.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                entry.getValue().getRenderer().clear(player);
            }
        }
        visualizedRegions.clear();
    }
    
    /**
     * Обрабатывает выход игрока с сервера
     */
    public void handlePlayerQuit(UUID playerId) {
        VisualizedRegion visualization = visualizedRegions.remove(playerId);
        if (visualization != null) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                visualization.getRenderer().clear(player);
            }
        }
    }
    
    /**
     * Внутренний класс для хранения информации о визуализации
     */
    private static class VisualizedRegion {
        private final Region region;
        private final RegionRenderer renderer;
        private final long createdAt;
        
        public VisualizedRegion(Region region, RegionRenderer renderer) {
            this.region = region;
            this.renderer = renderer;
            this.createdAt = System.currentTimeMillis();
        }
        
        public Region getRegion() {
            return region;
        }
        
        public RegionRenderer getRenderer() {
            return renderer;
        }
        
        public long getCreatedAt() {
            return createdAt;
        }
    }
}