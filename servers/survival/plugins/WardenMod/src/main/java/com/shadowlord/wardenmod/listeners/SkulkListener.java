package com.shadowlord.wardenmod.listeners;

import com.shadowlord.wardenmod.achievements.Achievements;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for Skulk block tracking and achievements
 */
public class SkulkListener implements Listener {
    
    private final JavaPlugin plugin;
    private SkulkSensorListener sensorListener;
    
    public void setSensorListener(SkulkSensorListener sensorListener) {
        this.sensorListener = sensorListener;
    }
    
    public SkulkListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // Check if it's Purple Wool (our Skulk placeholder)
        if (block.getType() == Material.WOOL && block.getData() == 10) {
            // Track number of Skulk blocks placed
            int skulkBlocksPlaced = player.hasMetadata("skulk_blocks_placed") ?
                player.getMetadata("skulk_blocks_placed").get(0).asInt() + 1 : 1;
            
            player.setMetadata("skulk_blocks_placed",
                new org.bukkit.metadata.FixedMetadataValue(plugin, skulkBlocksPlaced)
            );
            
            player.sendMessage(ChatColor.DARK_GRAY + "Skulk block placed! (" + skulkBlocksPlaced + ")");
            
            // Award Block Cultist achievement at 10 blocks
            if (skulkBlocksPlaced == 10) {
                Achievements.giveBlockCultist(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        
        // Check if player picked up a skull (Warden loot)
        if (item.getType() == Material.SKULL_ITEM) {
            Achievements.giveLegendaryCollector(player);
        }
        
        // Check if player has all three items (Shrieker, Skulk, Sensor)
        boolean hasShrieker = false;
        boolean hasSkulk = false;
        boolean hasSensor = false;
        
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null) {
                if (invItem.getType() == Material.PRISMARINE) {
                    hasShrieker = true;
                }
                if (invItem.getType() == Material.WOOL && invItem.getDurability() == 10) {
                    hasSkulk = true;
                }
                if (invItem.getType() == Material.PURPUR_PILLAR) {
                    hasSensor = true;
                }
            }
        }
        
        if (hasShrieker && hasSkulk && hasSensor) {
            Achievements.giveDeepDarkExplorer(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (sensorListener == null) return;
        // Notify nearby sensors when player moves
        Location loc = event.getTo();
        for (Block b : sensorListener.getNearbyBlocks(loc, 8)) {
            if (b.getType() == Material.PURPUR_PILLAR) {
                sensorListener.detectVibration(b.getLocation(), event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        if (sensorListener == null) return;
        if (event.getAction() != org.bukkit.event.block.Action.PHYSICAL && event.getClickedBlock() != null) {
            Block clicked = event.getClickedBlock();
            // Any interaction near sensor should trigger
            for (Block b : sensorListener.getNearbyBlocks(clicked.getLocation(), 6)) {
                if (b.getType() == Material.PURPUR_PILLAR) {
                    sensorListener.detectVibration(b.getLocation(), event.getPlayer());
                }
            }
        }
    }
}
