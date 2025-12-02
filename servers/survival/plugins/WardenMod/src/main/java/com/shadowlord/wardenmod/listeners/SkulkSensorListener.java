package com.shadowlord.wardenmod.listeners;

import com.shadowlord.wardenmod.achievements.Achievements;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for Skulk Sensor block events
 * Detects vibrations and triggers effects
 */
public class SkulkSensorListener implements Listener {
    
    private final JavaPlugin plugin;
    private static final double DETECTION_RANGE = 16.0;
        private final Map<Location, Long> sensorActivations = new HashMap<>();
        private final Map<Location, Integer> redstoneOutputs = new HashMap<>();
        private ShriekerListener shriekerListener;
    
        public void setShriekerListener(ShriekerListener shriekerListener) {
            this.shriekerListener = shriekerListener;
        }
    
    public SkulkSensorListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // Check if it's a Purpur Pillar (our Skulk Sensor placeholder)
        if (block.getType() == Material.PURPUR_PILLAR) {
            // Spawn particle effect for Skulk Sensor placement
            block.getWorld().spawnParticle(
                Particle.REDSTONE,
                block.getLocation().add(0.5, 0.5, 0.5),
                5,
                0.3,
                0.3,
                0.3
            );
            
            player.sendMessage(ChatColor.DARK_AQUA + "Skulk Sensor placed!");
            Achievements.giveVibrationDetector(player);
        }
    }
    
    private void spawnSensorEffect(org.bukkit.Location location) {
        // Redstone particles to show activation
        location.getWorld().spawnParticle(
            Particle.REDSTONE,
            location.add(0.5, 0.5, 0.5),
            8,
            0.3,
            0.3,
            0.3
        );
        
        // Sonic particles
        location.getWorld().spawnParticle(
            Particle.SPELL,
            location,
            4,
            0.2,
            0.2,
            0.2
        );
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.PURPUR_PILLAR) {
            sensorActivations.remove(block.getLocation());
            redstoneOutputs.remove(block.getLocation());
        }
    }
    
    /**
     * Detect vibrations near Skulk Sensor
     * Simulates sensor detecting movements and sounds within range
     */
    public void detectVibration(Location sensorLoc, Entity source) {
        double distance = sensorLoc.distance(source.getLocation());
        
        // Sensor detects within 16 block range
        if (distance <= DETECTION_RANGE) {
            activateSensor(sensorLoc);
        }
    }
    
    private void activateSensor(Location sensorLoc) {
        // Check cooldown
        if (sensorActivations.containsKey(sensorLoc)) {
            long timeSinceActivation = System.currentTimeMillis() - sensorActivations.get(sensorLoc);
            if (timeSinceActivation < 1000) { // 1 second cooldown
                return;
            }
        }
        
        sensorActivations.put(sensorLoc, System.currentTimeMillis());
        
        // Increment redstone output (up to 15 like real comparators)
        int output = redstoneOutputs.getOrDefault(sensorLoc, 0) + 1;
        if (output > 15) output = 15;
        redstoneOutputs.put(sensorLoc, output);
        
        // Emit redstone particles to show signal strength
        emitRedstoneSignal(sensorLoc, output);
        
        // Find and trigger nearby Shriekers
        if (shriekerListener != null) {
            for (Block nearby : getNearbyBlocks(sensorLoc, 10)) {
                if (nearby.getType() == Material.PRISMARINE) {
                    shriekerListener.triggerShrieker(nearby.getLocation());
                }
            }
        }
        
        // Reset output after 2 seconds
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(
            plugin,
            new Runnable() {
                @Override
                public void run() {
                    redstoneOutputs.remove(sensorLoc);
                }
            },
            40L  // 2 seconds
        );
    }
    
    private void emitRedstoneSignal(Location sensorLoc, int strength) {
        // Emit redstone particles based on signal strength
        sensorLoc.getWorld().spawnParticle(
            Particle.REDSTONE,
            sensorLoc.add(0.5, 0.5, 0.5),
            strength * 2,
            0.3,
            0.3,
            0.3,
            strength / 15.0  // Intensity based on signal
        );
    }
    
    public java.util.List<Block> getNearbyBlocks(Location center, int radius) {
        java.util.List<Block> blocks = new java.util.ArrayList<>();
        for (int x = center.getBlockX() - radius; x <= center.getBlockX() + radius; x++) {
            for (int z = center.getBlockZ() - radius; z <= center.getBlockZ() + radius; z++) {
                for (int y = center.getBlockY() - radius; y <= center.getBlockY() + radius; y++) {
                    Block block = center.getWorld().getBlockAt(x, y, z);
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

}
