package com.shadowlord.wardenmod.listeners;

import com.shadowlord.wardenmod.achievements.Achievements;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

/**
 * Listener for Sneak 100 achievement
 * Awarded when player sneaks while holding Skulk or Skulk Sensor items
 */
public class SneakListener implements Listener {
    
    private final JavaPlugin plugin;
    
    public SneakListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        
        if (event.isSneaking()) {
            // Check if player is holding Skulk or Skulk Sensor items
            ItemStack mainHand = player.getItemInHand();
            
            if (mainHand != null && (
                mainHand.getType() == Material.WOOL && mainHand.getDurability() == 10 ||
                mainHand.getType() == Material.PURPUR_PILLAR ||
                mainHand.getType() == Material.PRISMARINE
            )) {
                // Award Sneak 100 achievement
                Achievements.giveSneak100(player);
            }
        }
    }
}
