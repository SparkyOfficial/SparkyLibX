package com.sparky.libx.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Событие, вызываемое при взаимодействии с регионами
 */
public class RegionEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    
    private final Player player;
    private final Location location;
    private final String regionName;
    private final RegionAction action;
    
    public RegionEvent(Player player, Location location, String regionName, RegionAction action) {
        this.player = player;
        this.location = location;
        this.regionName = regionName;
        this.action = action;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public String getRegionName() {
        return regionName;
    }
    
    public RegionAction getAction() {
        return action;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    /**
     * Тип действия с регионом
     */
    public enum RegionAction {
        ENTER,   // Игрок вошел в регион
        LEAVE,   // Игрок вышел из региона
        INTERACT // Игрок взаимодействует с регионом
    }
}
