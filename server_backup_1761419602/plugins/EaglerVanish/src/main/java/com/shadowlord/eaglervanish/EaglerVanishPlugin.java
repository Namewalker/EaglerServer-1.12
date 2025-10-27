package com.shadowlord.eaglervanish;

import com.shadowlord.eaglervanish.commands.VanishCommand;
import com.shadowlord.eaglervanish.listeners.VanishListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EaglerVanishPlugin extends JavaPlugin {
  private final Set<UUID> vanished = Collections.synchronizedSet(new HashSet<>());

  @Override
  public void onEnable() {
    // register command and listener
    if (getCommand("vanish") != null) {
      getCommand("vanish").setExecutor(new VanishCommand(this));
    } else {
      getLogger().warning("Command 'vanish' missing from plugin.yml");
    }
    getServer().getPluginManager().registerEvents(new VanishListener(this), this);
    getLogger().info("EaglerVanish enabled");
  }

  @Override
  public void onDisable() {
    vanished.clear();
    getLogger().info("EaglerVanish disabled");
  }

  public boolean isVanished(UUID uuid) {
    return vanished.contains(uuid);
  }

  public void setVanished(UUID uuid, boolean state) {
    if (state) vanished.add(uuid);
    else vanished.remove(uuid);
    // update tab/list visibility for current online players
    getServer().getOnlinePlayers().forEach(other -> {
      if (other.getUniqueId().equals(uuid)) return;
      if (state) other.hidePlayer(this, getServer().getPlayer(uuid));
      else other.showPlayer(this, getServer().getPlayer(uuid));
    });
  }

  public Set<UUID> getVanishedPlayers() {
    return vanished;
  }
}
