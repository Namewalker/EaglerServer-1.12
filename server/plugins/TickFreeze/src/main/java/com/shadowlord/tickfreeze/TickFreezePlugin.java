package com.shadowlord.tickfreeze;

import com.shadowlord.tickfreeze.listeners.FreezeListener;
import com.shadowlord.tickfreeze.commands.TickCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TickFreezePlugin extends JavaPlugin {
  private volatile boolean frozen = false;
  private final Set<UUID> frozenPlayers = Collections.synchronizedSet(new HashSet<>());
  public static final String BYPASS_PERMISSION = "tickfreeze.bypass";

  @Override
  public void onEnable() {
    if (getCommand("tick") != null) getCommand("tick").setExecutor(new TickCommand(this));
    FreezeListener listener = new FreezeListener(this);
    getServer().getPluginManager().registerEvents(listener, this);
    getLogger().info("TickFreeze enabled");
  }

  @Override
  public void onDisable() {
    if (frozen) unfreeze();
    getLogger().info("TickFreeze disabled");
  }

  public boolean isFrozen() {
    return frozen;
  }

  public synchronized void freeze() {
    if (frozen) return;
    frozen = true;

    // Stop daylight cycle and clear weather in all worlds
    for (World w : Bukkit.getWorlds()) {
      w.setGameRuleValue("doDaylightCycle", "false");
      w.setStorm(false);
      w.setThundering(false);
      w.setWeatherDuration(0);
      // best-effort spawn tick reductions (may be ignored by some implementations)
      try {
        w.setTicksPerAnimalSpawns(0);
        w.setTicksPerMonsterSpawns(0);
      } catch (Throwable ignored) {}
    }

    // Capture current online players as candidates to freeze; whitelist ops/bypass at runtime via shouldFreezePlayer
    Bukkit.getOnlinePlayers().forEach(p -> frozenPlayers.add(p.getUniqueId()));

    // Attempt to disable AI for loaded entities
    FreezeUtil.setNoAIForAllLoadedEntities(true, this);

    getLogger().info("Server simulation frozen (best-effort).");
  }

  public synchronized void unfreeze() {
    if (!frozen) return;
    frozen = false;

    for (World w : Bukkit.getWorlds()) {
      w.setGameRuleValue("doDaylightCycle", "true");
    }

    FreezeUtil.setNoAIForAllLoadedEntities(false, this);

    frozenPlayers.clear();
    getLogger().info("Server simulation unfrozen.");
  }

  // Return true only if plugin is frozen AND the player should be affected.
  // Excludes ops and anyone with tickfreeze.bypass permission.
  public boolean shouldFreezePlayer(UUID uuid) {
    if (!frozen) return false;
    // get player; if offline assume we shouldn't freeze
    org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
    if (p == null) return false;
    if (p.isOp()) return false;
    if (p.hasPermission(BYPASS_PERMISSION)) return false;
    // also only freeze those captured at freeze moment (keeps behavior predictable)
    return frozenPlayers.contains(uuid);
  }

  // helpers for selective freeze control if needed
  public void addFrozenPlayer(UUID uuid) { frozenPlayers.add(uuid); }
  public void removeFrozenPlayer(UUID uuid) { frozenPlayers.remove(uuid); }
  public Set<UUID> getFrozenPlayers() { return frozenPlayers; }
}
