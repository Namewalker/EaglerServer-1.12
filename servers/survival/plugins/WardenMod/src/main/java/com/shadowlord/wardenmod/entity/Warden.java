package com.shadowlord.wardenmod.entity;

import org.bukkit.Location;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Custom Warden entity - An iron golem with Warden-like properties
 */
public class Warden {
    
    private final IronGolem ironGolem;
    private final JavaPlugin plugin;
    private static final double WARDEN_MAX_HEALTH = 500.0; // Warden has 500 HP (250 hearts)
    private int lastAttackTick = -100;
    private static final int ATTACK_COOLDOWN = 8; // Warden attacks every 8 ticks
    
    public Warden(IronGolem ironGolem, JavaPlugin plugin) {
        this.ironGolem = ironGolem;
        this.plugin = plugin;
        this.setup();
    }
    
    private void setup() {
        // Set custom name
        ironGolem.setCustomName("Warden");
        ironGolem.setCustomNameVisible(true);
        
        // Set health to Warden maximum
        ironGolem.setMaxHealth(WARDEN_MAX_HEALTH);
        ironGolem.setHealth(WARDEN_MAX_HEALTH);
        
        // Make it aggressive and attack hostile mobs
        ironGolem.setPlayerCreated(false);
    }
    
    public IronGolem getEntity() {
        return ironGolem;
    }
    
    public void destroy() {
        if (ironGolem != null && !ironGolem.isDead()) {
            ironGolem.remove();
        }
    }
    
    public boolean isValid() {
        return ironGolem != null && !ironGolem.isDead();
    }
    
    public int getLastAttackTick() {
        return lastAttackTick;
    }
    
    public void setLastAttackTick(int tick) {
        this.lastAttackTick = tick;
    }
    
    public static int getAttackCooldown() {
        return ATTACK_COOLDOWN;
    }
    
    public static double getMaxHealth() {
        return WARDEN_MAX_HEALTH;
    }
}
