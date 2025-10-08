package com.shadowlord.inject;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central store for patrol points per-entity. Designed as a singleton per-plugin instance.
 * Points are not persisted across restarts in this minimal version.
 */
public class PatrolManager {
  private static final Map<org.bukkit.plugin.Plugin, PatrolManager> INSTANCES = new ConcurrentHashMap<>();
  private final Map<UUID, List<Location>> points = new ConcurrentHashMap<>();
  private final org.bukkit.plugin.Plugin plugin;

  private PatrolManager(org.bukkit.plugin.Plugin plugin) { this.plugin = plugin; }

  public static PatrolManager getInstance(org.bukkit.plugin.Plugin plugin) {
    return INSTANCES.computeIfAbsent(plugin, p -> new PatrolManager(plugin));
  }

  public void addPoint(UUID entityId, Location loc) {
    points.computeIfAbsent(entityId, k -> Collections.synchronizedList(new ArrayList<>()))
          .add(loc.clone());
  }

  public void clearPoints(UUID entityId) {
    points.remove(entityId);
  }

  public List<Location> getPointsFor(UUID entityId) {
    List<Location> list = points.get(entityId);
    if (list == null) return Collections.emptyList();
    return Collections.unmodifiableList(list);
  }

  public List<Location> listPoints(UUID entityId) {
    return getPointsFor(entityId);
  }
}
