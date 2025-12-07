package com.shadowlord.wardenmod.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class SkulkSensorCommand implements CommandExecutor {
    
    private final JavaPlugin plugin;
    
    public SkulkSensorCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("wardenmod.skulksensor")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        // Give Skulk Sensor block (using Purpur Pillar as a placeholder in 1.12.2)
        ItemStack sensor = new ItemStack(Material.PURPUR_PILLAR);
        sensor.setAmount(1);
        
        player.getInventory().addItem(sensor);
        player.sendMessage(ChatColor.GREEN + "Skulk Sensor block given!");
        
        return true;
    }
}
