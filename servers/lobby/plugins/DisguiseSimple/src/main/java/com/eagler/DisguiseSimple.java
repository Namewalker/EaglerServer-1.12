package com.eagler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import java.util.HashMap;
import java.util.Map;

public class DisguiseSimple extends JavaPlugin implements Listener {
    private Map<Player, String> disguises = new HashMap<>();
    private DisguiseManager disguiseManager;

    @Override
    public void onEnable() {
    Bukkit.getPluginManager().registerEvents(this, this);
    disguiseManager = new DisguiseManager(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("§cYou must be an operator (op) to use this command.");
            return true;
        }
        if (label.equalsIgnoreCase("disguise")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /disguise <mob|player> <name>");
                return true;
            }
            String type = args[0].toLowerCase();
            String target = args.length > 1 ? args[1] : "";
            if (type.equals("mob")) {
                disguises.put(player, target);
                try {
                    org.bukkit.entity.EntityType mobType = org.bukkit.entity.EntityType.valueOf(target.toUpperCase());
                    disguiseManager.disguiseAsMob(player, mobType);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cUnknown mob type.");
                }
            } else if (type.equals("player")) {
                disguises.put(player, target);
                disguiseManager.disguiseAsPlayer(player, target);
            } else {
                player.sendMessage("§cUnknown disguise type. Use mob or player.");
            }
            return true;
        }
        if (label.equalsIgnoreCase("undisguise")) {
            disguises.remove(player);
            disguiseManager.undisguise(player);
            return true;
        }
        if (label.equalsIgnoreCase("leash")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /leash <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage("§cPlayer not found or not online.");
                return true;
            }
            disguiseManager.leashPlayer(player, target);
            return true;
        }
        return false;
    }
}
