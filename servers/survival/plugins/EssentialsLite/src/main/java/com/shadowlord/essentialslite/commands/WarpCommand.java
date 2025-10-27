package com.shadowlord.essentialslite.commands;

import com.shadowlord.essentialslite.EssentialsLite;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class WarpCommand implements CommandExecutor {
  private final EssentialsLite plugin;
  public WarpCommand(EssentialsLite plugin) { this.plugin = plugin; }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) { sender.sendMessage("Only players."); return true; }
    Player p = (Player) sender;
    FileConfiguration cfg = plugin.getStore().getConfig();

    if (label.equalsIgnoreCase("setwarp")) {
      if (!p.hasPermission("es.setwarp")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /setwarp <name>"); return true; }
      String name = args[0];
      Location l = p.getLocation();
      cfg.set("warps." + name + ".world", l.getWorld().getName());
      cfg.set("warps." + name + ".x", l.getX());
      cfg.set("warps." + name + ".y", l.getY());
      cfg.set("warps." + name + ".z", l.getZ());
      plugin.getStore().save();
      p.sendMessage(ChatColor.GREEN + "Warp " + name + " set.");
      return true;
    }

    if (label.equalsIgnoreCase("delwarp")) {
      if (!p.hasPermission("es.setwarp")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /delwarp <name>"); return true; }
      cfg.set("warps." + args[0], null);
      plugin.getStore().save();
      p.sendMessage(ChatColor.GREEN + "Warp deleted.");
      return true;
    }

    // /warp <name>
    if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /warp <name>"); return true; }
    String name = args[0];
    if (!cfg.isSet("warps." + name + ".world")) { p.sendMessage(ChatColor.RED + "Warp not found."); return true; }
    String w = cfg.getString("warps." + name + ".world");
    double x = cfg.getDouble("warps." + name + ".x");
    double y = cfg.getDouble("warps." + name + ".y");
    double z = cfg.getDouble("warps." + name + ".z");
    Location loc = new Location(plugin.getServer().getWorld(w), x, y, z);
    p.teleport(loc);
    p.sendMessage(ChatColor.GREEN + "Teleported to warp " + name + ".");
    return true;
  }
}
