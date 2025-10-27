package com.shadowlord.undeadsiege;

import com.shadowlord.undeadsiege.commands.SiegeCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class UndeadSiegePlugin extends JavaPlugin {
  private SiegeManager siegeManager;

  @Override
  public void onEnable() {
    // create plugin data folder and an empty config if none exists (avoids requiring config.yml in the JAR)
    if (!getDataFolder().exists()) getDataFolder().mkdirs();
    // ensure config file exists on disk (this writes an empty config if none present)
    saveConfig();

    siegeManager = new SiegeManager(this);
    SiegeCommand cmd = new SiegeCommand(this);
    if (getCommand("ssr") != null) getCommand("ssr").setExecutor(cmd);
    if (getCommand("siege") != null) getCommand("siege").setExecutor(cmd);
    getServer().getPluginManager().registerEvents(siegeManager, this);
    getLogger().info("UndeadSiege enabled.");
  }

  @Override
  public void onDisable() {
    if (siegeManager != null) siegeManager.stopSiege(true);
    getLogger().info("UndeadSiege disabled.");
  }

  public SiegeManager getSiegeManager() { return siegeManager; }
}
