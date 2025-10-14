package com.sparky.libx.visualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.sparky.libx.region.Region;

/**
 * Система визуализации регионов с помощью частиц
 * @author Андрій Будильников
 */
public class RegionVisualizer {
    
    private final Map<UUID, VisualTask> activeVisualizations = new ConcurrentHashMap<>();
    private final Map<String, VisualizationStyle> styles = new HashMap<>();
    
    public RegionVisualizer() {

        registerStyle("default", new VisualizationStyle(
            Particle.REDSTONE,
            new Particle.DustOptions(Color.RED, 1.0f),
            0.5,
            5,
            0
        ));
        
        registerStyle("selection", new VisualizationStyle(
            Particle.VILLAGER_HAPPY,
            new Particle.DustOptions(Color.LIME, 1.5f),
            0.2,
            2,
            40
        ));
    }
    
    /**
     * Визуализирует регион для игрока
     */
    public void visualize(Player player, Region region, String styleName) {
        visualize(player, region, getStyle(styleName));
    }
    
    public void visualize(Player player, Region region, VisualizationStyle style) {

        stopVisualization(player);
        

        VisualTask task = new VisualTask(player, region, style);
        task.runTaskTimer(Bukkit.getPluginManager().getPlugin("SparkyLibX"), 0, 1);
        

        activeVisualizations.put(player.getUniqueId(), task);
        

        if (style.getDuration() > 0) {
            Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("SparkyLibX"),
                () -> stopVisualization(player),
                style.getDuration()
            );
        }
    }
    
    /**
     * Останавливает визуализацию для игрока
     */
    public void stopVisualization(Player player) {
        VisualTask task = activeVisualizations.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    
    /**
     * Останавливает все активные визуализации
     */
    public void stopAll() {
        for (VisualTask task : activeVisualizations.values()) {
            task.cancel();
        }
        activeVisualizations.clear();
    }
    
    /**
     * Регистрирует новый стиль визуализации
     */
    public void registerStyle(String name, VisualizationStyle style) {
        styles.put(name.toLowerCase(), style);
    }
    
    /**
     * Получает стиль визуализации по имени
     */
    public VisualizationStyle getStyle(String name) {
        return styles.getOrDefault(name.toLowerCase(), styles.get("default"));
    }
    
    /**
     * Класс, представляющий стиль визуализации
     */
    public static class VisualizationStyle {
        private final Particle particle;
        private final Object particleData;
        private final double density;
        private final int particlesPerTick;
        private final int duration;
        
        public VisualizationStyle(Particle particle, Object particleData, 
                                 double density, int particlesPerTick, int duration) {
            this.particle = particle;
            this.particleData = particleData;
            this.density = Math.max(0.05, density);
            this.particlesPerTick = Math.max(1, particlesPerTick);
            this.duration = duration;
        }
        
        public Particle getParticle() {
            return particle;
        }
        
        public Object getParticleData() {
            return particleData;
        }
        
        public double getDensity() {
            return density;
        }
        
        public int getParticlesPerTick() {
            return particlesPerTick;
        }
        
        public int getDuration() {
            return duration;
        }
    }
    
    /**
     * Задача для отрисовки частиц
     */
    private static class VisualTask extends BukkitRunnable {
        private final Player player;
        private final Region region;
        private final VisualizationStyle style;
        private final List<Vector> points;
        private int currentIndex = 0;
        
        public VisualTask(Player player, Region region, VisualizationStyle style) {
            this.player = player;
            this.region = region;
            this.style = style;
            this.points = generatePoints(region, style.getDensity());
        }
        
        @Override
        public void run() {
            if (!player.isOnline()) {
                cancel();
                return;
            }
            
            World world = player.getWorld();
            Location playerLoc = player.getLocation();
            

            for (int i = 0; i < style.getParticlesPerTick() && !points.isEmpty(); i++) {
                if (currentIndex >= points.size()) {
                    currentIndex = 0;
                }
                
                Vector point = points.get(currentIndex++);
                Location particleLoc = new Location(world, point.getX(), point.getY(), point.getZ());
                

                if (playerLoc.distanceSquared(particleLoc) < 1024 && 
                    player.getLocation().getDirection().dot(particleLoc.toVector().subtract(playerLoc.toVector())) > 0) {
                    player.spawnParticle(
                        style.getParticle(),
                        particleLoc,
                        1,
                        style.getParticleData()
                    );
                }
            }
        }
        
        /**
         * Генерирует точки для отрисовки границ региона
         */
        private List<Vector> generatePoints(Region region, double density) {
            List<Vector> points = new ArrayList<>();
            

            if (region instanceof com.sparky.libx.region.CuboidRegion) {
                points.addAll(generateCuboidPoints((com.sparky.libx.region.CuboidRegion) region, density));
            } else if (region instanceof com.sparky.libx.region.SphereRegion) {
                points.addAll(generateSpherePoints((com.sparky.libx.region.SphereRegion) region, density));
            } else if (region instanceof com.sparky.libx.region.PolygonRegion) {
                points.addAll(generatePolygonPoints((com.sparky.libx.region.PolygonRegion) region, density));
            }
            
            return points;
        }
        
        private Collection<? extends Vector> generateCuboidPoints(
                com.sparky.libx.region.CuboidRegion region, double density) {
            
            List<Vector> points = new ArrayList<>();
            Location min = region.getMinPoint();
            Location max = region.getMaxPoint();
            




            



            
            return points;
        }
        
        private Collection<? extends Vector> generateSpherePoints(
                com.sparky.libx.region.SphereRegion region, double density) {
            
            List<Vector> points = new ArrayList<>();
            Location center = region.getCenter();
            double radius = ((com.sparky.libx.region.SphereRegion) region).getRadius();
            




            
            return points;
        }
        
        private Collection<? extends Vector> generatePolygonPoints(
                com.sparky.libx.region.PolygonRegion region, double density) {
            
            List<Vector> points = new ArrayList<>();
            double minY = region.getMinY();
            double maxY = region.getMaxY();
            

            List<com.sparky.libx.region.PolygonRegion.Vector2D> vertices = region.getPoints();
            for (int i = 0; i < vertices.size(); i++) {
                com.sparky.libx.region.PolygonRegion.Vector2D current = vertices.get(i);
                com.sparky.libx.region.PolygonRegion.Vector2D next = vertices.get((i + 1) % vertices.size());
                

                generateLine(points, 
                    new Location(region.getWorld(), current.getX(), minY, current.getZ()),
                    new Location(region.getWorld(), next.getX(), minY, next.getZ()),
                    density
                );
                

                generateLine(points, 
                    new Location(region.getWorld(), current.getX(), minY, current.getZ()),
                    new Location(region.getWorld(), current.getX(), maxY, current.getZ()),
                    density
                );
                

                if (i > 0) {
                    generateLine(points, 
                        new Location(region.getWorld(), current.getX(), maxY, current.getZ()),
                        new Location(region.getWorld(), next.getX(), maxY, next.getZ()),
                        density
                    );
                }
            }
            
            return points;
        }
        
        private void generateLine(List<Vector> points, Location from, Location to, double density) {
            double distance = from.distance(to);
            Vector direction = to.toVector().subtract(from.toVector()).normalize().multiply(density);
            
            for (double d = 0; d < distance; d += density) {
                points.add(from.toVector().add(direction.clone().multiply(d)));
            }
            

            points.add(to.toVector());
        }
        
        private void generateLine(List<Vector> points, Location start, double dx, double dy, double dz, double density) {
            Vector direction = new Vector(dx - start.getX(), dy - start.getY(), dz - start.getZ());
            double length = direction.length();
            direction.normalize().multiply(Math.min(density, length));
            
            for (double d = 0; d < length; d += density) {
                points.add(start.toVector().add(direction.clone().multiply(d)));
            }
        }
        
        private void generateCircle(List<Vector> points, Location center, double radius, int axis, double density) {
            int steps = (int) (2 * Math.PI * radius / density);

            
            for (int i = 0; i < steps; i++) {
                double angle = 2 * Math.PI * i / steps;
                double x = 0, y = 0, z = 0;
                
                switch (axis) {
                    case 0: // XY plane
                        x = Math.cos(angle) * radius;
                        y = Math.sin(angle) * radius;
                        z = 0;
                        break;
                    case 1: // XZ plane
                        x = Math.cos(angle) * radius;
                        z = Math.sin(angle) * radius;
                        y = 0;
                        break;
                    case 2: // YZ plane
                        y = Math.cos(angle) * radius;
                        z = Math.sin(angle) * radius;
                        x = 0;
                        break;
                }
                
                points.add(center.toVector().add(new Vector(x, y, z)));
            }
        }
    }
}
