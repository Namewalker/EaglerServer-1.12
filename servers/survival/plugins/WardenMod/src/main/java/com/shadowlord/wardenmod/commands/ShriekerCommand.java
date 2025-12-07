package com.shadowlord.wardenmod.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ShriekerCommand implements CommandExecutor {
    
    private final JavaPlugin plugin;
    
    public ShriekerCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("wardenmod.shrieker")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        // Give Shrieker block (using Prismarine as a placeholder in 1.12.2)
        ItemStack shrieker = new ItemStack(Material.PRISMARINE);
        shrieker.setAmount(1);
        
        player.getInventory().addItem(shrieker);
        player.sendMessage(ChatColor.GREEN + "Shrieker block given!");
        
        return true;
    }
}
