package com.shadowlord.cursedaltar;

import com.shadowlord.cursedaltar.commands.BreakCurseCommand;
import com.shadowlord.cursedaltar.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

public class CursedAltarPlugin extends JavaPlugin {
  private CursedManager cursed;

  @Override
  public void onEnable() {
    saveDefaultConfig();
    cursed = new CursedManager(this);

    getServer().getPluginManager().registerEvents(
      new AltarPlacementListener(cursed), this
    );
    getServer().getPluginManager().registerEvents(
      new AltarInteractListener(cursed), this
    );
    getServer().getPluginManager().registerEvents(
      new DisconnectListener(cursed), this
    );
    getServer().getPluginManager().registerEvents(
      new LoginListener(cursed), this
    );

    // Updated command registration
    getCommand("breakcurse")
      .setExecutor(new BreakCurseCommand(cursed));

    getLogger().info("CursedAltar enabled");
  }

  @Override
  public void onDisable() {
    cursed.saveAll();
    getLogger().info("CursedAltar disabled");
  }
}
