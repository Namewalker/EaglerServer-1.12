package com.shadowlord.essentialslite.commands;

import com.shadowlord.essentialslite.EssentialsLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class JailCommand implements CommandExecutor {
  private final EssentialsLite plugin;
  public JailCommand(EssentialsLite plugin) { this.plugin = plugin; }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) { sender.sendMessage("Only players."); return true; }
    Player p = (Player) sender;
    FileConfiguration cfg = plugin.getStore().getConfig();

    if (label.equalsIgnoreCase("setjail")) {
      if (!p.hasPermission("es.setjail")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /setjail <name>"); return true; }
      String name = args[0];
      Location l = p.getLocation();
      cfg.set("jails." + name + ".world", l.getWorld().getName());
      cfg.set("jails." + name + ".x", l.getX());
      cfg.set("jails." + name + ".y", l.getY());
      cfg.set("jails." + name + ".z", l.getZ());
      plugin.getStore().save();
      p.sendMessage(ChatColor.GREEN + "Jail " + name + " set.");
      return true;
    }

    if (label.equalsIgnoreCase("jail")) {
      if (!p.hasPermission("es.jail")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /jail <player> [jail]"); return true; }
      Player t = Bukkit.getPlayer(args[0]);
      if (t == null) { p.sendMessage(ChatColor.RED + "Player not online."); return true; }
      String jail = (args.length > 1) ? args[1] : "default";
      if (!cfg.isSet("jails." + jail + ".world")) { p.sendMessage(ChatColor.RED + "Jail not found."); return true; }
      String w = cfg.getString("jails." + jail + ".world");
      double x = cfg.getDouble("jails." + jail + ".x");
      double y = cfg.getDouble("jails." + jail + ".y");
      double z = cfg.getDouble("jails." + jail + ".z");
      Location loc = new Location(plugin.getServer().getWorld(w), x, y, z);
      t.teleport(loc);
      cfg.set("jailed." + t.getUniqueId().toString(), jail);
      plugin.getStore().save();
      p.sendMessage(ChatColor.GREEN + "Jailed " + t.getName() + " to " + jail);
      return true;
    }

    if (label.equalsIgnoreCase("unjail")) {
      if (!p.hasPermission("es.unjail")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /unjail <player>"); return true; }
      Player t = Bukkit.getPlayer(args[0]);
      if (t == null) { p.sendMessage(ChatColor.RED + "Player not online."); return true; }
      cfg.set("jailed." + t.getUniqueId().toString(), null);
      plugin.getStore().save();
      p.sendMessage(ChatColor.GREEN + "Unjailed " + t.getName());
      return true;
    }

    return true;
  }
}
