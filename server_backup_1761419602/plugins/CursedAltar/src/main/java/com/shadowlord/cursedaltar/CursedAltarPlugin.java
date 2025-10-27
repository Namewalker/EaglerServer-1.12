package com.shadowlord.cursedaltar;

import com.shadowlord.cursedaltar.commands.BreakCurseCommand;
import com.shadowlord.cursedaltar.commands.CurseLevelCommand;
import com.shadowlord.cursedaltar.commands.CurseVisionCommand;
import com.shadowlord.cursedaltar.listeners.AltarPlacementListener;
import com.shadowlord.cursedaltar.listeners.AltarInteractListener;
import com.shadowlord.cursedaltar.listeners.DisconnectListener;
import com.shadowlord.cursedaltar.listeners.LoginListener;
import com.shadowlord.cursedaltar.listeners.MirrorListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CursedAltarPlugin extends JavaPlugin {
  private CursedManager cursed;

  @Override
  public void onEnable() {
    saveDefaultConfig();
    cursed = new CursedManager(this);

    getServer().getPluginManager().registerEvents(new AltarPlacementListener(cursed), this);
    getServer().getPluginManager().registerEvents(new AltarInteractListener(cursed), this);
    getServer().getPluginManager().registerEvents(new DisconnectListener(cursed), this);
    getServer().getPluginManager().registerEvents(new LoginListener(cursed), this);
    getServer().getPluginManager().registerEvents(new MirrorListener(cursed), this);

    getCommand("breakcurse").setExecutor(new BreakCurseCommand(cursed));
    getCommand("curselevel").setExecutor(new CurseLevelCommand(cursed));
    getCommand("cursevision").setExecutor(new CurseVisionCommand(cursed));

    getLogger().info("CursedAltar enabled");
  }

  @Override
  public void onDisable() {
    if (cursed != null) {
      cursed.saveAll();
    }
    getLogger().info("CursedAltar disabled");
  }
}
