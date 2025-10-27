package com.shadowlord.graveyard;

import com.shadowlord.graveyard.commands.GraveyardCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class GraveyardPlugin extends JavaPlugin {
  private GraveyardManager manager;

  @Override
  public void onEnable() {
    if (!getDataFolder().exists()) getDataFolder().mkdirs();
    saveConfig();
    manager = new GraveyardManager(this);
    GraveyardCommand cmd = new GraveyardCommand(this);
    if (getCommand("graveyard") != null) getCommand("graveyard").setExecutor(cmd);
    if (getCommand("gcore") != null) getCommand("gcore").setExecutor(cmd);
    getServer().getPluginManager().registerEvents(new GraveyardListener(manager), this);
    getLogger().info("GraveyardBinding enabled.");
  }

  @Override
  public void onDisable() {
    if (manager != null) manager.saveAll();
    getLogger().info("GraveyardBinding disabled.");
  }

  public GraveyardManager getManager() { return manager; }
}
