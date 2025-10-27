package com.shadowlord.essentialslite.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class YamlStore {
  private final Plugin plugin;
  private final File file;
  private FileConfiguration cfg;

  public YamlStore(Plugin plugin, String filename) {
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(), filename);
    this.cfg = new YamlConfiguration();
    if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
  }

  public void load() {
    try {
      if (file.exists()) cfg.load(file);
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void save() {
    try { cfg.save(file); } catch (Exception e) { e.printStackTrace(); }
  }

  public FileConfiguration getConfig() { return cfg; }
}
