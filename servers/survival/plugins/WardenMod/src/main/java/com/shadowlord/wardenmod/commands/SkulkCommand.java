package com.shadowlord.wardenmod.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class SkulkCommand implements CommandExecutor {
    
    private final JavaPlugin plugin;
    
    public SkulkCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("wardenmod.skulk")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        // Give Skulk block (using Purple Wool as a placeholder in 1.12.2)
        ItemStack skulk = new ItemStack(Material.WOOL);
        skulk.setDurability((short) 10); // Purple wool
        skulk.setAmount(1);
        
        player.getInventory().addItem(skulk);
        player.sendMessage(ChatColor.GREEN + "Skulk block given!");
        
        return true;
    }
}
