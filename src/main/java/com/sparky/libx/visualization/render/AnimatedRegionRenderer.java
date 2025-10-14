package com.sparky.libx.visualization.render;

import java.util.ArrayList;
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
import com.sparky.libx.visualization.RegionVisualizer;

/**
 * Анимированный рендерер регионов с эффектами
 * author: Андрій Будильников
 */
public class AnimatedRegionRenderer implements RegionRenderer {
    
    private final Map<UUID, AnimationTask> activeAnimations = new ConcurrentHashMap<>();
    private final RegionVisualizer visualizer;
    
    public AnimatedRegionRenderer(RegionVisualizer visualizer) {
        this.visualizer = visualizer;
    }
    
    /**
     * Отображает регион с анимацией
     */
    public void renderAnimated(Player player, Region region, AnimationStyle style) {
        clear(player);
        
        AnimationTask task = new AnimationTask(player, region, style);
        task.runTaskTimer(Bukkit.getPluginManager().getPlugin("SparkyLibX"), 0, style.getFrameDelay());
        activeAnimations.put(player.getUniqueId(), task);
        
        if (style.getDuration() > 0) {
            Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("SparkyLibX"),
                () -> clear(player),
                style.getDuration()
            );
        }
    }
    
    @Override
    public void render(Player player, Region region) {
        renderAnimated(player, region, AnimationStyle.PULSE);
    }
    
    @Override
    public void clear(Player player) {
        AnimationTask task = activeAnimations.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    
    /**
     * Останавливает все активные анимации
     */
    public void clearAll() {
        for (AnimationTask task : activeAnimations.values()) {
            task.cancel();
        }
        activeAnimations.clear();
    }
    
    /**
     * Задача для анимации отображения региона
     */
    private class AnimationTask extends BukkitRunnable {
        private final Player player;
        private final Region region;
        private final AnimationStyle style;
        private final List<Vector> points;
        private int frame = 0;
        private double animationProgress = 0.0;
        
        public AnimationTask(Player player, Region region, AnimationStyle style) {
            this.player = player;
            this.region = region;
            this.style = style;
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
            
            animationProgress += style.getSpeed();
            if (animationProgress >= 1.0) {
                animationProgress = 0.0;
                frame++;
            }
            
            renderFrame(world, playerLoc, frame, animationProgress);
        }
        
        /**
         * Отображает один кадр анимации
         */
        private void renderFrame(World world, Location playerLoc, int frame, double progress) {
            int particlesToShow = Math.min(
                style.getParticlesPerFrame(), 
                (int)(points.size() * style.getDensity())
            );
            
            int startIndex = (frame * style.getParticlesPerFrame()) % points.size();
            
            for (int i = 0; i < particlesToShow; i++) {
                int index = (startIndex + i) % points.size();
                Vector point = points.get(index);
                
                Location particleLoc = new Location(world, point.getX(), point.getY(), point.getZ());
                
                if (playerLoc.distanceSquared(particleLoc) < 1024 && 
                    player.getLocation().getDirection().dot(particleLoc.toVector().subtract(playerLoc.toVector())) > 0) {
                    
                    Location animatedLoc = applyAnimationEffects(particleLoc, progress);
                    Particle particle = getAnimatedParticle(progress);
                    Object particleData = getAnimatedParticleData(progress);
                    
                    player.spawnParticle(
                        particle,
                        animatedLoc,
                        1,
                        particleData
                    );
                }
            }
        }
        
        /**
         * Применяет эффекты анимации к позиции частицы
         */
        private Location applyAnimationEffects(Location original, double progress) {
            Location animated = original.clone();
            
            switch (style.getEffect()) {
                case PULSE:
                    double scale = 1.0 + 0.5 * Math.sin(progress * Math.PI * 2);
                    break;
                    
                case WAVE:
                    double waveOffset = Math.sin(progress * Math.PI * 4) * 0.5;
                    animated.add(0, waveOffset, 0);
                    break;
                    
                case SPIRAL:
                    double angle = progress * Math.PI * 4;
                    double radius = 0.3 * progress;
                    animated.add(
                        Math.cos(angle) * radius,
                        0,
                        Math.sin(angle) * radius
                    );
                    break;
            }
            
            return animated;
        }
        
        /**
         * Получает частицу для текущего кадра анимации
         */
        private Particle getAnimatedParticle(double progress) {
            switch (style.getParticleTransition()) {
                case COLOR_SHIFT:
                    return Particle.REDSTONE;
                    
                case TYPE_CYCLE:
                    Particle[] particles = {Particle.REDSTONE, Particle.VILLAGER_HAPPY, Particle.FLAME};
                    return particles[(int)(progress * particles.length) % particles.length];
                    
                default:
                    return style.getBaseParticle();
            }
        }
        
        /**
         * Получает данные частицы для текущего кадра анимации
         */
        private Object getAnimatedParticleData(double progress) {
            switch (style.getParticleTransition()) {
                case COLOR_SHIFT:
                    int red = (int)(255 * (1 - progress));
                    int blue = (int)(255 * progress);
                    return new Particle.DustOptions(Color.fromRGB(red, 0, blue), 1.0f);
                    
                default:
                    return style.getBaseParticleData();
            }
        }
        
        /**
         * Генерирует точки для отображения границ региона
         */
        private List<Vector> generatePoints(Region region) {
            List<Vector> points = new ArrayList<>();
            
            Location min = region.getMinPoint();
            Location max = region.getMaxPoint();
            
            generateLinePoints(points, min, new Location(min.getWorld(), max.getX(), min.getY(), min.getZ()));
            generateLinePoints(points, min, new Location(min.getWorld(), min.getX(), max.getY(), min.getZ()));
            generateLinePoints(points, min, new Location(min.getWorld(), min.getX(), min.getY(), max.getZ()));
            generateLinePoints(points, max, new Location(max.getWorld(), min.getX(), max.getY(), max.getZ()));
            generateLinePoints(points, max, new Location(max.getWorld(), max.getX(), min.getY(), max.getZ()));
            generateLinePoints(points, max, new Location(max.getWorld(), max.getX(), max.getY(), min.getZ()));
            
            generateLinePoints(points, new Location(min.getWorld(), min.getX(), min.getY(), max.getZ()), 
                              new Location(min.getWorld(), max.getX(), min.getY(), min.getZ()));
            generateLinePoints(points, new Location(min.getWorld(), min.getX(), max.getY(), min.getZ()), 
                              new Location(min.getWorld(), max.getX(), max.getY(), max.getZ()));
            
            return points;
        }
        
        /**
         * Генерирует точки вдоль линии
         */
        private void generateLinePoints(List<Vector> points, Location start, Location end) {
            Vector direction = end.toVector().subtract(start.toVector());
            double length = direction.length();
            direction.normalize();
            
            int pointCount = (int)(length / style.getPointSpacing()) + 1;
            
            for (int i = 0; i <= pointCount; i++) {
                Vector point = start.toVector().clone().add(direction.clone().multiply(i * style.getPointSpacing()));
                points.add(point);
            }
        }
    }
    
    /**
     * Стиль анимации
     */
    public static class AnimationStyle {
        public static final AnimationStyle PULSE = new AnimationStyle(
            Particle.REDSTONE,
            new Particle.DustOptions(Color.RED, 1.0f),
            AnimationEffect.PULSE,
            ParticleTransition.NONE,
            0.1,
            5,
            2,
            0.05,
            200,
            0.5
        );
        
        public static final AnimationStyle WAVE = new AnimationStyle(
            Particle.VILLAGER_HAPPY,
            null,
            AnimationEffect.WAVE,
            ParticleTransition.NONE,
            0.15,
            8,
            1,
            0.1,
            0,
            0.3
        );
        
        public static final AnimationStyle RAINBOW = new AnimationStyle(
            Particle.REDSTONE,
            new Particle.DustOptions(Color.RED, 1.0f),
            AnimationEffect.PULSE,
            ParticleTransition.COLOR_SHIFT,
            0.2,
            10,
            1,
            0.08,
            0,
            0.4
        );
        
        private final Particle baseParticle;
        private final Object baseParticleData;
        private final AnimationEffect effect;
        private final ParticleTransition particleTransition;
        private final double density;
        private final int particlesPerFrame;
        private final int frameDelay;
        private final double speed;
        private final int duration;
        private final double pointSpacing;
        
        public AnimationStyle(Particle baseParticle, Object baseParticleData, AnimationEffect effect,
                             ParticleTransition particleTransition, double density, int particlesPerFrame,
                             int frameDelay, double speed, int duration, double pointSpacing) {
            this.baseParticle = baseParticle;
            this.baseParticleData = baseParticleData;
            this.effect = effect;
            this.particleTransition = particleTransition;
            this.density = density;
            this.particlesPerFrame = particlesPerFrame;
            this.frameDelay = frameDelay;
            this.speed = speed;
            this.duration = duration;
            this.pointSpacing = pointSpacing;
        }
        
        public Particle getBaseParticle() { return baseParticle; }
        public Object getBaseParticleData() { return baseParticleData; }
        public AnimationEffect getEffect() { return effect; }
        public ParticleTransition getParticleTransition() { return particleTransition; }
        public double getDensity() { return density; }
        public int getParticlesPerFrame() { return particlesPerFrame; }
        public int getFrameDelay() { return frameDelay; }
        public double getSpeed() { return speed; }
        public int getDuration() { return duration; }
        public double getPointSpacing() { return pointSpacing; }
    }
    
    /**
     * Типы анимационных эффектов
     */
    public enum AnimationEffect {
        PULSE,
        WAVE,
        SPIRAL
    }
    
    /**
     * Типы переходов частиц
     */
    public enum ParticleTransition {
        NONE,
        COLOR_SHIFT,
        TYPE_CYCLE
    }
}