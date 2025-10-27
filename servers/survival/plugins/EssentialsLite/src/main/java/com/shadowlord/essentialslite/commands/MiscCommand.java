package com.shadowlord.essentialslite.commands;

import com.shadowlord.essentialslite.EssentialsLite;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class MiscCommand implements CommandExecutor {
  private final EssentialsLite plugin;
  public MiscCommand(EssentialsLite plugin) { this.plugin = plugin; }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) { sender.sendMessage("Only players."); return true; }
    Player p = (Player) sender;

    if (label.equalsIgnoreCase("back")) {
      String path = "back." + p.getUniqueId();
      if (!plugin.getStore().getConfig().isSet(path)) { p.sendMessage(ChatColor.RED + "No back location"); return true; }
      String world = plugin.getStore().getConfig().getString(path + ".world");
      double x = plugin.getStore().getConfig().getDouble(path + ".x");
      double y = plugin.getStore().getConfig().getDouble(path + ".y");
      double z = plugin.getStore().getConfig().getDouble(path + ".z");
      p.teleport(new Location(plugin.getServer().getWorld(world), x, y, z));
      p.sendMessage(ChatColor.GREEN + "Teleported back.");
      return true;
    }

    if (label.equalsIgnoreCase("spawn")) {
      if (!p.hasPermission("es.spawn")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      if (!plugin.getStore().getConfig().isSet("spawn")) { p.sendMessage(ChatColor.RED + "Spawn not set."); return true; }
      String w = plugin.getStore().getConfig().getString("spawn.world");
      double x = plugin.getStore().getConfig().getDouble("spawn.x");
      double y = plugin.getStore().getConfig().getDouble("spawn.y");
      double z = plugin.getStore().getConfig().getDouble("spawn.z");
      p.teleport(new Location(plugin.getServer().getWorld(w), x, y, z));
      p.sendMessage(ChatColor.GREEN + "Teleported to spawn.");
      return true;
    }

    if (label.equalsIgnoreCase("heal")) {
      if (!p.hasPermission("es.heal")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      p.setHealth(p.getMaxHealth());
      p.sendMessage(ChatColor.GREEN + "Healed.");
      return true;
    }

    if (label.equalsIgnoreCase("feed")) {
      if (!p.hasPermission("es.feed")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      p.setFoodLevel(20);
      p.sendMessage(ChatColor.GREEN + "Fed.");
      return true;
    }

    return true;
  }
}
