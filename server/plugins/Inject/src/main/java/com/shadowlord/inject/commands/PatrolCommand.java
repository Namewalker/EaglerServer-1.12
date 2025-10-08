package com.shadowlord.inject.commands;

import com.shadowlord.inject.PatrolManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Usage:
 *  /patrol <entityId|@e:<uuid>|uuid> addpoint
 *  /patrol <entityId|@e:<uuid>|uuid> clear
 *  /patrol <entityId|@e:<uuid>|uuid> list
 *
 * When adding a point, the command must be run by a player and uses the player's location.
 */
public class PatrolCommand implements CommandExecutor {
  private final org.bukkit.plugin.Plugin plugin;

  public PatrolCommand(org.bukkit.plugin.Plugin plugin) { this.plugin = plugin; }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("inject.use")) {
      sender.sendMessage("§cNo permission.");
      return true;
    }
    if (args.length < 2) {
      sender.sendMessage("§eUsage: /patrol <entity> <addpoint|clear|list>");
      return true;
    }
    String target = args[0];
    String action = args[1].toLowerCase();

    Entity ent = resolveEntity(target);
    if (ent == null) {
      sender.sendMessage("§cEntity not found.");
      return true;
    }
    UUID id = ent.getUniqueId();
    PatrolManager pm = PatrolManager.getInstance(plugin);

    switch (action) {
      case "addpoint":
        if (!(sender instanceof Player)) {
          sender.sendMessage("§cOnly players can add patrol points using their location.");
          return true;
        }
        Location loc = ((Player) sender).getLocation();
        pm.addPoint(id, loc);
        sender.sendMessage("§aPatrol point added for entity " + id.toString());
        break;
      case "clear":
        pm.clearPoints(id);
        sender.sendMessage("§aCleared patrol points for entity " + id.toString());
        break;
      case "list":
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Location l : pm.listPoints(id)) {
          sb.append("\n").append(i++).append(": ").append(l.getWorld().getName())
            .append(" x=").append(l.getBlockX()).append(" y=").append(l.getBlockY()).append(" z=").append(l.getBlockZ());
        }
        sender.sendMessage("§7Patrol points for " + id + ":" + (sb.length() == 0 ? " none" : sb.toString()));
        break;
      default:
        sender.sendMessage("§eUnknown action. Use addpoint, clear, or list.");
    }
    return true;
  }

  private Entity resolveEntity(String token) {
    // reuse same resolution as InjectCommand: @e:UUID, numeric id scan, plain UUID
    if (token.startsWith("@e:")) {
      try { return Bukkit.getEntity(UUID.fromString(token.substring(3))); } catch (Throwable ignored) { return null; }
    }
    try {
      int numericId = Integer.parseInt(token);
      for (org.bukkit.World w : Bukkit.getWorlds()) for (Entity e : w.getEntities()) if (e.getEntityId() == numericId) return e;
    } catch (NumberFormatException ignored) {}
    try { return Bukkit.getEntity(UUID.fromString(token)); } catch (Throwable ignored) { return null; }
  }
}
