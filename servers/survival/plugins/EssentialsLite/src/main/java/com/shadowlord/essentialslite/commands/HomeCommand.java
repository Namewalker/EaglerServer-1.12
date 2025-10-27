package com.shadowlord.essentialslite.commands;

import com.shadowlord.essentialslite.EssentialsLite;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class HomeCommand implements CommandExecutor {
  private final EssentialsLite plugin;
  public HomeCommand(EssentialsLite plugin) { this.plugin = plugin; }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) { sender.sendMessage("Only players."); return true; }
    Player p = (Player) sender;
    FileConfiguration cfg = plugin.getStore().getConfig();

    if (label.equalsIgnoreCase("sethome")) {
      if (!p.hasPermission("es.sethome")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      String name = (args.length > 0) ? args[0] : "home";
      String path = "homes." + p.getUniqueId() + "." + name;
      Location l = p.getLocation();
      cfg.set(path + ".world", l.getWorld().getName());
      cfg.set(path + ".x", l.getX());
      cfg.set(path + ".y", l.getY());
      cfg.set(path + ".z", l.getZ());
      plugin.getStore().save();
      p.sendMessage(ChatColor.GREEN + "Home " + name + " set.");
      return true;
    }

    if (label.equalsIgnoreCase("delhome")) {
      String name = (args.length > 0) ? args[0] : "home";
      String path = "homes." + p.getUniqueId() + "." + name;
      cfg.set(path, null);
      plugin.getStore().save();
      p.sendMessage(ChatColor.GREEN + "Home " + name + " deleted.");
      return true;
    }

    // /home
    String name = (args.length > 0) ? args[0] : "home";
    String path = "homes." + p.getUniqueId() + "." + name;
    if (!cfg.isSet(path + ".world")) { p.sendMessage(ChatColor.RED + "Home not found."); return true; }
    String w = cfg.getString(path + ".world");
    double x = cfg.getDouble(path + ".x");
    double y = cfg.getDouble(path + ".y");
    double z = cfg.getDouble(path + ".z");
    Location loc = new Location(plugin.getServer().getWorld(w), x, y, z);
    p.teleport(loc);
    p.sendMessage(ChatColor.GREEN + "Teleported to home " + name + ".");
    return true;
  }
}
