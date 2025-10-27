package com.shadowlord.undeadsiege;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerTracker {
  private final Map<UUID, Location> originalLoc = new HashMap<>();
  private final Map<UUID, GameMode> originalMode = new HashMap<>();

  public void saveAndTeleport(Player p, Location dest) {
    originalLoc.put(p.getUniqueId(), p.getLocation());
    originalMode.put(p.getUniqueId(), p.getGameMode());
    p.teleport(dest);
  }

  public void setSpectator(Player p) {
    originalMode.putIfAbsent(p.getUniqueId(), p.getGameMode());
    p.setGameMode(GameMode.SPECTATOR);
  }

  public void restore(Player p) {
    if (originalLoc.containsKey(p.getUniqueId())) {
      p.teleport(originalLoc.remove(p.getUniqueId()));
    }
    if (originalMode.containsKey(p.getUniqueId())) {
      p.setGameMode(originalMode.remove(p.getUniqueId()));
    }
  }

  public void restoreAll(org.bukkit.Server server) {
    for (Player p : server.getOnlinePlayers()) {
      restore(p);
    }
  }
}
