package com.shadowlord.inject.commands;

import com.shadowlord.inject.PatrolManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class PatrolCommand implements CommandExecutor {
  private final org.bukkit.plugin.Plugin plugin;

  public PatrolCommand(org.bukkit.plugin.Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("inject.use")) {
      sender.sendMessage("§cNo permission.");
      return true;
    }

    if (args.length < 3) {
      sender.sendMessage("§eUsage: /patrol <mobType|@e:uuid|entityId> <radius|action> <addpoint|clear|list>");
      return true;
    }

    String target = args[0];
    String radiusOrAction = args[1];
    String action = args[2].toLowerCase();

    List<Entity> targets = new ArrayList<>();

    if (sender instanceof Player && isMobType(target) && isNumeric(radiusOrAction)) {
      EntityType type = EntityType.valueOf(target.toUpperCase());
      double radius = Double.parseDouble(radiusOrAction);
      Player p = (Player) sender;
      for (Entity e : p.getNearbyEntities(radius, radius, radius)) {
        if (e.getType() == type) targets.add(e);
      }
    } else {
      Entity ent = resolveEntity(target);
      if (ent != null) targets.add(ent);
    }

    if (targets.isEmpty()) {
      sender.sendMessage("§cNo matching entities found.");
      return true;
    }

    PatrolManager pm = PatrolManager.getInstance(plugin);
    int success = 0;

    for (Entity ent : targets) {
      UUID id = ent.getUniqueId();
      switch (action) {
        case "addpoint":
          if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can add patrol points.");
            return true;
          }
          Location loc = ((Player) sender).getLocation();
          pm.addPoint(id, loc);
          success++;
          break;
        case "clear":
          pm.clearPoints(id);
          success++;
          break;
        case "list":
          List<Location> pts = pm.getPointsFor(id);
          sender.sendMessage("§7Patrol points for " + id + ":");
          for (int i = 0; i < pts.size(); i++) {
            Location l = pts.get(i);
            sender.sendMessage("§8" + i + ": " + l.getWorld().getName() + " x=" + l.getBlockX() + " y=" + l.getBlockY() + " z=" + l.getBlockZ());
          }
          success++;
          break;
        default:
          sender.sendMessage("§eUnknown action: " + action);
          return true;
      }
    }

    sender.sendMessage("§aPatrol '" + action + "' applied to " + success + " entities.");
    return true;
  }

  private boolean isMobType(String s) {
    try {
      EntityType type = EntityType.valueOf(s.toUpperCase());
      return type.isAlive();
    } catch (IllegalArgumentException ignored) {
      return false;
    }
  }

  private boolean isNumeric(String s) {
    try {
      Double.parseDouble(s);
      return true;
    } catch (NumberFormatException ignored) {
      return false;
    }
  }

  private Entity resolveEntity(String token) {
    if (token.startsWith("@e:")) {
      try { return Bukkit.getEntity(UUID.fromString(token.substring(3))); } catch (Throwable ignored) {}
    }
    try {
      int id = Integer.parseInt(token);
      for (org.bukkit.World w : Bukkit.getWorlds()) for (Entity e : w.getEntities()) if (e.getEntityId() == id) return e;
    } catch (NumberFormatException ignored) {}
    try { return Bukkit.getEntity(UUID.fromString(token)); } catch (Throwable ignored) {}
    return null;
  }
}
