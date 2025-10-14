package com.sparky.libx.visualization.render;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.sparky.libx.region.Region;

/**
 * Рендерер, отображающий регионы с помощью блоков
 */
public class BlockRenderer implements RegionRenderer {
    private final Map<UUID, RenderTask> activeRenders = new ConcurrentHashMap<>();
    private final BlockData blockData;
    private final int duration;
    private final boolean wireframe;
    
    public BlockRenderer(Material material, int duration, boolean wireframe) {
        this.blockData = Bukkit.createBlockData(material);
        this.duration = duration;
        this.wireframe = wireframe;
    }
    
    @Override
    public void render(Player player, Region region) {
        clear(player);

        RenderTask task = new RenderTask(player, region);
        task.runTaskTimer(Bukkit.getPluginManager().getPlugin("SparkyLibX"), 0, 20);
        activeRenders.put(player.getUniqueId(), task);

        if (duration > 0) {
            Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("SparkyLibX"),
                () -> clear(player),
                duration
            );
        }
    }
    
    @Override
    public void clear(Player player) {
        RenderTask task = activeRenders.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    
    /**
     * Останавливает все активные рендеры
     */
    public void clearAll() {
        for (RenderTask task : activeRenders.values()) {
            task.cancel();
        }
        activeRenders.clear();
    }
    
    /**
     * Задача для отрисовки блоков
     */
    private class RenderTask extends BukkitRunnable {
        private final Player player;
        private final Region region;
        private final Set<Location> renderedBlocks = new HashSet<>();
        private final Map<Location, BlockData> originalBlocks = new HashMap<>();
        
        public RenderTask(Player player, Region region) {
            this.player = player;
            this.region = region;

            if (wireframe) {
                collectWireframeBlocks();
            } else {
                collectSolidBlocks();
            }

            for (Location loc : renderedBlocks) {
                originalBlocks.put(loc, loc.getBlock().getBlockData().clone());
            }
        }
        
        @Override
        public void run() {
            if (!player.isOnline()) {
                restoreBlocks();
                cancel();
                return;
            }

            for (Location loc : renderedBlocks) {
                player.sendBlockChange(loc, blockData);
            }
        }
        
        @Override
        public synchronized void cancel() {
            restoreBlocks();
            super.cancel();
        }
        
        /**
         * Восстанавливает оригинальные блоки
         */
        private void restoreBlocks() {
            for (Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
                if (player.isOnline()) {
                    player.sendBlockChange(entry.getKey(), entry.getValue());
                }
            }
            originalBlocks.clear();
            renderedBlocks.clear();
        }
        
        /**
         * Собирает блоки для каркасного режима
         */
        private void collectWireframeBlocks() {
        }
        
        /**
         * Собирает блоки для сплошного режима
         */
        private void collectSolidBlocks() {
        }
    }
}