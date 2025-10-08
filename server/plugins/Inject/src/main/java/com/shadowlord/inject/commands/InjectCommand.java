package com.shadowlord.inject.commands;

import com.shadowlord.inject.InjectPlugin;
import com.shadowlord.inject.behaviors.BehaviorRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.UUID;

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
    if (args.length < 2) {
      sender.sendMessage("§eUsage: /inject <entityId|@e:<uuid>|uuid> <behavior> [remove]");
      return true;
    }

    String target = args[0];
    String behaviorName = args[1];
    boolean remove = args.length >= 3 && args[2].equalsIgnoreCase("remove");

    Entity ent = resolveEntity(target);
    if (ent == null) {
      sender.sendMessage("§cEntity not found.");
      return true;
    }

    if (remove) {
      boolean ok = registry.removeBehaviorFrom(ent, behaviorName);
      sender.sendMessage(ok ? "§aBehavior removed." : "§cBehavior not present.");
    } else {
      boolean ok = registry.addBehaviorTo(ent, behaviorName);
      sender.sendMessage(ok ? "§aBehavior injected." : "§cBehavior not found.");
    }
    return true;
  }

  /**
   * Resolve an entity from a selector:
   * - @e:<uuid>  -> exact UUID
   * - numeric id -> scan loaded entities and match entity.getEntityId()
   * - plain UUID -> parse UUID
   */
  private Entity resolveEntity(String token) {
    // @e:UUID selector
    if (token.startsWith("@e:")) {
      String uuidPart = token.substring(3);
      try {
        UUID id = UUID.fromString(uuidPart);
        return Bukkit.getEntity(id);
      } catch (Throwable ignored) {
        return null;
      }
    }

    // Try numeric entity id (server-internal int id). Bukkit doesn't expose getEntity(int),
    // so scan loaded entities across worlds and match getEntityId().
    try {
      int numericId = Integer.parseInt(token);
      for (org.bukkit.World w : Bukkit.getWorlds()) {
        for (Entity e : w.getEntities()) {
          if (e.getEntityId() == numericId) return e;
        }
      }
      return null;
    } catch (NumberFormatException ignored) {
      // Not a number — fall through to UUID parse attempt.
    }

    // Try plain UUID string
    try {
      UUID uuid = UUID.fromString(token);
      return Bukkit.getEntity(uuid);
    } catch (Throwable ignored) {
      return null;
    }
  }
}
