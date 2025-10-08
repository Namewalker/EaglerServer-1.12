package com.shadowlord.inject.commands;

import com.shadowlord.inject.InjectPlugin;
import com.shadowlord.inject.behaviors.BehaviorRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class InjectCommand implements CommandExecutor {
  private final InjectPlugin plugin;
  private final BehaviorRegistry registry;

  public InjectCommand(InjectPlugin plugin, BehaviorRegistry registry) {
    this.plugin = plugin;
    this.registry = registry;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!sender.hasPermission("inject.use")) {
      sender.sendMessage("§cNo permission.");
      return true;
    }

    if (args.length < 3) {
      sender.sendMessage("§eUsage: /inject <mobType|@e:uuid|entityId> <radius|behavior> <behavior>");
      return true;
    }

    String target = args[0];
    String radiusOrBehavior = args[1];
    String behaviorName = args[2];

    List<Entity> targets = new ArrayList<>();

    // If sender is a player and target is a mob type + radius
    if (sender instanceof Player && isMobType(target) && isNumeric(radiusOrBehavior)) {
      EntityType type = EntityType.valueOf(target.toUpperCase());
      double radius = Double.parseDouble(radiusOrBehavior);
      Player p = (Player) sender;
      for (Entity e : p.getNearbyEntities(radius, radius, radius)) {
        if (e.getType() == type) targets.add(e);
      }
    } else {
      // Fallback: treat target as UUID or entity ID
      Entity ent = resolveEntity(target);
      if (ent != null) targets.add(ent);
    }

    if (targets.isEmpty()) {
      sender.sendMessage("§cNo matching entities found.");
      return true;
    }

    int success = 0;
    for (Entity e : targets) {
      if (registry.addBehaviorTo(e, behaviorName)) success++;
    }

    sender.sendMessage("§aInjected behavior '" + behaviorName + "' into " + success + " entities.");
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
