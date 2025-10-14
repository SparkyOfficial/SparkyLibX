package com.sparky.libx.visualization.render;

import com.sparky.libx.region.Region;
import com.sparky.libx.region.CuboidRegion;
import com.sparky.libx.region.SphereRegion;
import com.sparky.libx.region.PolygonRegion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Рендерер, отображающий границы регионов с помощью частиц
 * @author Андрій Будильников
 */
public class ParticleRenderer implements RegionRenderer {
    private final Map<UUID, RenderTask> activeRenders = new ConcurrentHashMap<>();
    private final Particle particle;
    private final Object particleData;
    private final double density;
    private final int particlesPerTick;
    private final int duration;
    
    public ParticleRenderer(Particle particle, Object particleData, 
                           double density, int particlesPerTick, int duration) {
        this.particle = particle;
        this.particleData = particleData;
        this.density = Math.max(0.05, density);
        this.particlesPerTick = Math.max(1, particlesPerTick);
        this.duration = duration;
    }
    
    @Override
    public void render(Player player, Region region) {

        clear(player);
        

        RenderTask task = new RenderTask(player, region);
        task.runTaskTimer(Bukkit.getPluginManager().getPlugin("SparkyLibX"), 0, 1);
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
     * Задача для отрисовки частиц
     */
    private class RenderTask extends BukkitRunnable {
        private final Player player;
        private final Region region;
        private final List<Vector> points;
        private int currentIndex = 0;
        
        public RenderTask(Player player, Region region) {
            this.player = player;
            this.region = region;
            this.points = generatePoints(region);
        }
        
        @Override
        public void run() {
            if (!player.isOnline()) {
                cancel();
                return;
            }
            
            World world = player.getWorld();
            Location playerLoc = player.getLocation();
            

            if (!isVisibleFrom(region, playerLoc)) {
                return;
            }
            

            for (int i = 0; i < particlesPerTick && !points.isEmpty(); i++) {
                if (currentIndex >= points.size()) {
                    currentIndex = 0;
                }
                
                Vector point = points.get(currentIndex++);
                Location particleLoc = new Location(world, point.getX(), point.getY(), point.getZ());
                

                if (isInView(player, particleLoc)) {
                    player.spawnParticle(
                        particle,
                        particleLoc,
                        1,
                        particleData
                    );
                }
            }
        }
        
        /**
         * Генерирует точки для отрисовки границ региона
         */
        private List<Vector> generatePoints(Region region) {
            if (region instanceof CuboidRegion) {
                return generateCuboidPoints((CuboidRegion) region);
            } else if (region instanceof SphereRegion) {
                return generateSpherePoints((SphereRegion) region);
            } else if (region instanceof PolygonRegion) {
                return generatePolygonPoints((PolygonRegion) region);
            }
            return Collections.emptyList();
        }
        
        private List<Vector> generateCuboidPoints(CuboidRegion region) {
            List<Vector> points = new ArrayList<>();
            Location minLoc = region.getMinPoint();
            Location maxLoc = region.getMaxPoint();
            Vector min = minLoc.toVector();
            Vector max = maxLoc.toVector();
            





            

            generateLine(points, min, new Vector(min.getX(), max.getY(), min.getZ()));
            generateLine(points, new Vector(max.getX(), min.getY(), min.getZ()), 
                        new Vector(max.getX(), max.getY(), min.getZ()));
            generateLine(points, new Vector(min.getX(), min.getY(), max.getZ()),
                        new Vector(min.getX(), max.getY(), max.getZ()));
            generateLine(points, max, new Vector(max.getX(), max.getY(), min.getZ()));
            
            return points;
        }
        
        private List<Vector> generateSpherePoints(SphereRegion region) {
            List<Vector> points = new ArrayList<>();
            double radius = region.getRadius();
            Location center = region.getCenter();
            

            int pointsPerCircle = (int) (2 * Math.PI * radius / density);

            

            for (double y = -radius; y <= radius; y += density) {
                double currentRadius = Math.sqrt(radius * radius - y * y);
                for (int i = 0; i < pointsPerCircle; i++) {
                    double angle = 2 * Math.PI * i / pointsPerCircle;
                    double x = center.getX() + currentRadius * Math.cos(angle);
                    double z = center.getZ() + currentRadius * Math.sin(angle);
                    points.add(new Vector(x, center.getY() + y, z));
                }
            }
            
            return points;
        }
        
        private List<Vector> generatePolygonPoints(PolygonRegion region) {
            List<Vector> points = new ArrayList<>();

            return points;
        }
        
        private void generateLine(List<Vector> points, Vector from, Vector to) {
            double distance = from.distance(to);
            int pointsCount = (int) (distance / density) + 1;
            
            for (int i = 0; i <= pointsCount; i++) {
                double t = (double) i / pointsCount;
                points.add(from.clone().multiply(1 - t).add(to.clone().multiply(t)));
            }
        }
    }
}
