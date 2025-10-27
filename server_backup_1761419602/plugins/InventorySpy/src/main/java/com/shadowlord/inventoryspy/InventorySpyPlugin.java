package com.shadowlord.inventoryspy;

import com.shadowlord.inventoryspy.commands.InventoryCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class InventorySpyPlugin extends JavaPlugin {
  @Override
  public void onEnable() {
    if (getCommand("inventory") != null) {
      getCommand("inventory").setExecutor(new InventoryCommand(this));
    }
    getLogger().info("InventorySpy enabled.");
  }

  @Override
  public void onDisable() {
    getLogger().info("InventorySpy disabled.");
  }
}
