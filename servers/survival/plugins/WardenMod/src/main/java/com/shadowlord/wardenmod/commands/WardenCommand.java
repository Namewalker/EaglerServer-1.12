package com.shadowlord.wardenmod.commands;

import com.shadowlord.wardenmod.entity.Warden;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

public class WardenCommand implements CommandExecutor {
    
    private final JavaPlugin plugin;
    
    public WardenCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("wardenmod.warden")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        // Spawn Warden (Iron Golem with custom properties)
        IronGolem ironGolem = player.getLocation().getWorld().spawn(
            player.getLocation().add(0, 1, 0), 
            IronGolem.class
        );
        
        Warden warden = new Warden(ironGolem, plugin);
        
        player.sendMessage(ChatColor.GREEN + "Warden summoned!");
        
        return true;
    }
}
