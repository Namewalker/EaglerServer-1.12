package com.shadowlord.inject.behaviors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps registry of available behaviors and active behaviors on entities.
 */
public class BehaviorRegistry {
  private final Map<String, Behavior> available = new ConcurrentHashMap<>();
  private final Map<UUID, Map<String, Behavior>> active = new ConcurrentHashMap<>();
  private final Plugin plugin;
  private BukkitTask tickTask;

  public BehaviorRegistry(Plugin plugin) {
    this.plugin = plugin;
    startTicker();
  }

  public void register(String name, Behavior b) {
    available.put(name.toLowerCase(), b);
  }

  public boolean addBehaviorTo(Entity entity, String behaviorName) {
    Behavior b = available.get(behaviorName.toLowerCase());
    if (b == null) return false;
    active.computeIfAbsent(entity.getUniqueId(), k -> new ConcurrentHashMap<>())
          .computeIfAbsent(behaviorName.toLowerCase(), k -> {
            b.onAttach(entity);
            return b;
          });
    return true;
  }

  public boolean removeBehaviorFrom(Entity entity, String behaviorName) {
    Map<String, Behavior> map = active.get(entity.getUniqueId());
    if (map == null) return false;
    Behavior b = map.remove(behaviorName.toLowerCase());
    if (b == null) return false;
    b.onRemove(entity);
    if (map.isEmpty()) active.remove(entity.getUniqueId());
    return true;
  }

  public void disableAll() {
    // call onRemove for all active
    for (UUID id : new HashSet<>(active.keySet())) {
      Map<String, Behavior> map = active.remove(id);
      if (map == null) continue;
      for (Map.Entry<String, Behavior> e : map.entrySet()) {
        try {
          // best-effort: get entity and call onRemove
          org.bukkit.entity.Entity ent = Bukkit.getEntity(id);
          if (ent != null) e.getValue().onRemove(ent);
        } catch (Throwable ignored) {}
      }
    }
    if (tickTask != null) tickTask.cancel();
  }

  private void startTicker() {
    tickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      try {
        // iterate active behaviors and call tick
        for (Map.Entry<UUID, Map<String, Behavior>> entEntry : new HashSet<>(active.entrySet())) {
          org.bukkit.entity.Entity ent = Bukkit.getEntity(entEntry.getKey());
          if (ent == null || !ent.isValid()) {
            // cleanup
            active.remove(entEntry.getKey());
            continue;
          }
          for (Behavior b : new ArrayList<>(entEntry.getValue().values())) {
            try { b.tick(ent); } catch (Throwable t) { plugin.getLogger().warning("Behavior tick failed: " + t.getMessage()); }
          }
        }
      } catch (Throwable t) {
        plugin.getLogger().warning("BehaviorRegistry tick error: " + t.getMessage());
      }
    }, 1L, 2L); // tick every 2 server ticks for performance
  }
}
