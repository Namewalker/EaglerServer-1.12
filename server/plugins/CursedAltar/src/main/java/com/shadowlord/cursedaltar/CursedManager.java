package com.shadowlord.cursedaltar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class CursedManager {
  private final JavaPlugin plugin;
  private final Set<String> altarKeys = new HashSet<>();
  private final Map<UUID, Integer> levels   = new HashMap<>();

  public CursedManager(JavaPlugin plugin) {
    this.plugin = plugin;
    loadAll();
  }

  public boolean isAltar(Location loc) {
    return altarKeys.contains(makeKey(loc));
  }

  public void addAltar(Location loc) {
    altarKeys.add(makeKey(loc));
    saveAltars();
  }

  public boolean isCursed(UUID uuid) {
    return levels.containsKey(uuid);
  }

  public int getLevel(UUID uuid) {
    return levels.getOrDefault(uuid, 0);
  }

  public void registerCurse(UUID uuid) {
    if (!isCursed(uuid)) {
      levels.put(uuid, 1);
      saveCurses();
    }
  }

  public void escalateCurse(UUID uuid) {
    if (isCursed(uuid)) {
      levels.put(uuid, getLevel(uuid) + 1);
      saveCurses();
    }
  }

  public void saveAll() {
    saveAltars();
    saveCurses();
  }

  private void saveAltars() {
    FileConfiguration cfg = plugin.getConfig();
    cfg.set("altars", new ArrayList<>(altarKeys));
    plugin.saveConfig();
  }

  private void saveCurses() {
    FileConfiguration cfg = plugin.getConfig();
    cfg.set("cursed-players", null);
    for (Map.Entry<UUID, Integer> e : levels.entrySet()) {
      cfg.set("cursed-players." + e.getKey(), e.getValue());
    }
    plugin.saveConfig();
  }

  @SuppressWarnings("unchecked")
  private void loadAll() {
    FileConfiguration cfg = plugin.getConfig();
    List<String> list = cfg.getStringList("altars");
    if (list != null) altarKeys.addAll(list);

    if (cfg.isConfigurationSection("cursed-players")) {
      for (String key : cfg.getConfigurationSection("cursed-players").getKeys(false)) {
        levels.put(UUID.fromString(key), cfg.getInt("cursed-players." + key));
      }
    }
  }

  private String makeKey(Location loc) {
    return loc.getWorld().getName()
         + ":" + loc.getBlockX()
         + ":" + loc.getBlockY()
         + ":" + loc.getBlockZ();
  }
}
