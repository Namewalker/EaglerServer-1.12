package com.shadowlord.wardenmod.listeners;

import com.shadowlord.wardenmod.achievements.Achievements;
import com.shadowlord.wardenmod.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.IronGolem;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Listener for Shrieker block events
 */
public class ShriekerListener implements Listener {
    
    private final JavaPlugin plugin;
        private final Map<Location, Long> shriekerCooldowns = new HashMap<>();
        private final Map<Location, Integer> shriekerTriggers = new HashMap<>();
        private final Random random = new Random();
        private WardenListener wardenListener;
    
        public void setWardenListener(WardenListener wardenListener) {
            this.wardenListener = wardenListener;
        }
    
    public ShriekerListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // Check if it's a Prismarine block (our Shrieker placeholder)
        if (block.getType() == Material.PRISMARINE) {
            // Spawn particle effect for Shrieker placement
            block.getWorld().spawnParticle(
                Particle.NOTE,
                block.getLocation().add(0.5, 0.5, 0.5),
                5,
                0.3,
                0.3,
                0.3
            );
            
            player.sendMessage(ChatColor.DARK_PURPLE + "Shrieker placed!");
            Achievements.giveShriekersWarning(player);
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        
        // Remove Shrieker tracking if destroyed
        if (block.getType() == Material.PRISMARINE) {
            shriekerCooldowns.remove(loc);
            shriekerTriggers.remove(loc);
        }
    }
    
    /**
     * Trigger a Shrieker to summon a Warden
     * Warden emerges from ground with particle effects
     */
    public void triggerShrieker(Location shriekerLoc) {
        // Check cooldown (Shriekers need time between triggers)
        if (shriekerCooldowns.containsKey(shriekerLoc)) {
            long timeSinceLastTrigger = System.currentTimeMillis() - shriekerCooldowns.get(shriekerLoc);
            if (timeSinceLastTrigger < 5000) { // 5 second cooldown
                return;
            }
        }
        
        shriekerCooldowns.put(shriekerLoc, System.currentTimeMillis());
        
        // Increment trigger count
        int triggers = shriekerTriggers.getOrDefault(shriekerLoc, 0) + 1;
        shriekerTriggers.put(shriekerLoc, triggers);
        
        // Spawn Warden on 3rd trigger (like real Warden behavior)
        if (triggers >= 3) {
            spawnWardenFromShrieker(shriekerLoc);
            shriekerTriggers.remove(shriekerLoc);
        }
    }
    
    private void spawnWardenFromShrieker(Location shriekerLoc) {
        if (wardenListener == null) return;
        
        // Spawn location is below the Shrieker
        Location spawnLoc = shriekerLoc.clone().subtract(0, 3, 0);
        spawnLoc.setY(Math.max(spawnLoc.getY(), 0)); // Ensure valid Y
        
        // Emergence animation - particles from ground
        for (int i = 0; i < 10; i++) {
            final int tick = i;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                plugin,
                new Runnable() {
                    @Override
                    public void run() {
                        // Spiral emergence effect
                        double angle = (tick * 36) * Math.PI / 180.0;
                        double x = Math.cos(angle) * 2;
                        double z = Math.sin(angle) * 2;
                        
                        Location particleLoc = spawnLoc.clone().add(x, tick * 0.5, z);
                        spawnLoc.getWorld().spawnParticle(
                            Particle.SPELL,
                            particleLoc,
                            3,
                            0.2,
                            0.2,
                            0.2
                        );
                        
                        // Add smoke effect
                        spawnLoc.getWorld().spawnParticle(
                            Particle.SMOKE_LARGE,
                            particleLoc,
                            2,
                            0.3,
                            0.3,
                            0.3
                        );
                    }
                },
                (long) tick
            );
        }
        
        // Actually spawn the Warden after emergence animation
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(
            plugin,
            new Runnable() {
                @Override
                public void run() {
                    IronGolem golem = spawnLoc.getWorld().spawn(spawnLoc, IronGolem.class);
                    Warden warden = new Warden(golem, plugin);
                    wardenListener.registerWarden(golem, warden);
                    
                    // Final emergence explosion
                    spawnLoc.getWorld().spawnParticle(
                        Particle.EXPLOSION_LARGE,
                        spawnLoc.add(0, 1.5, 0),
                        5,
                        1,
                        1,
                        1
                    );
                }
            },
            10L
        );
    }

}
