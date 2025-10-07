package com.shadowlord.tickfreeze;

import com.shadowlord.tickfreeze.commands.TickCommand;
import com.shadowlord.tickfreeze.listeners.FreezeListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TickFreezePlugin extends JavaPlugin {
  private volatile boolean frozen = false;
  private final Set<UUID> frozenPlayers = Collections.synchronizedSet(new HashSet<>());
  public static final String BYPASS_PERMISSION = "tickfreeze.bypass";

  // suspended arrows -> original velocity
  private final Map<Arrow, Vector> suspendedArrows = Collections.synchronizedMap(new HashMap<>());
  private BukkitTask suspenderTask = null;

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

    for (World w : Bukkit.getWorlds()) {
      w.setGameRuleValue("doDaylightCycle", "false");
      w.setStorm(false);
      w.setThundering(false);
      w.setWeatherDuration(0);
      try {
        w.setTicksPerAnimalSpawns(0);
        w.setTicksPerMonsterSpawns(0);
      } catch (Throwable ignored) {}
    }

    Bukkit.getOnlinePlayers().forEach(p -> frozenPlayers.add(p.getUniqueId()));
    startSuspenderTask();
    getLogger().info("Server simulation frozen (best-effort).");
  }

  public synchronized void unfreeze() {
    if (!frozen) return;
    frozen = false;

    for (World w : Bukkit.getWorlds()) {
      w.setGameRuleValue("doDaylightCycle", "true");
    }

    releaseSuspendedArrows();
    stopSuspenderTask();
    frozenPlayers.clear();
    getLogger().info("Server simulation unfrozen.");
  }

  // Exclude ops and bypass-permission players from freeze effects
  public boolean shouldFreezePlayer(UUID uuid) {
    if (!frozen) return false;
    org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
    if (p == null) return false;
    if (p.isOp()) return false;
    if (p.hasPermission(BYPASS_PERMISSION)) return false;
    return frozenPlayers.contains(uuid);
  }

  public void addFrozenPlayer(UUID uuid) { frozenPlayers.add(uuid); }
  public void removeFrozenPlayer(UUID uuid) { frozenPlayers.remove(uuid); }
  public Set<UUID> getFrozenPlayers() { return frozenPlayers; }

  // Arrow suspension API

  public void suspendArrow(Arrow arrow, Vector originalVelocity) {
    if (arrow == null || !arrow.isValid()) return;
    suspendedArrows.put(arrow, originalVelocity == null ? new Vector(0,0,0) : originalVelocity.clone());
    startSuspenderTask();
  }

  public void releaseSuspendedArrows() {
    synchronized (suspendedArrows) {
      for (Map.Entry<Arrow, Vector> e : new HashSet<>(suspendedArrows.entrySet())) {
        Arrow arrow = e.getKey();
        Vector vel = e.getValue();
        try {
          if (arrow != null && arrow.isValid()) {
            arrow.setVelocity(vel.clone());
          }
        } catch (Throwable t) {
          getLogger().warning("Failed to release arrow: " + t.getMessage());
        }
        suspendedArrows.remove(arrow);
      }
    }
  }

  private void startSuspenderTask() {
    if (suspenderTask != null) return;
    suspenderTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
      if (!frozen) return;
      synchronized (suspendedArrows) {
        for (Arrow arrow : new HashSet<>(suspendedArrows.keySet())) {
          try {
            if (arrow == null || !arrow.isValid()) {
              suspendedArrows.remove(arrow);
              continue;
            }
            org.bukkit.Location loc = arrow.getLocation();
            arrow.teleport(loc);
            arrow.setVelocity(new Vector(0, 0, 0));
          } catch (Throwable t) {
            getLogger().warning("Failed to keep arrow suspended: " + t.getMessage());
          }
        }
      }
    }, 1L, 1L);
  }

  private void stopSuspenderTask() {
    if (suspenderTask != null) {
      try { suspenderTask.cancel(); } catch (Throwable ignored) {}
      suspenderTask = null;
    }
  }
}
