package com.shadowlord.eaglerlogin;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SessionManager {
  private final Set<UUID> loggedIn = new HashSet<>();

  public boolean isLoggedIn(Player player) {
    return loggedIn.contains(player.getUniqueId());
  }

  public void markLoggedIn(Player player) {
    loggedIn.add(player.getUniqueId());
  }

  public void logout(Player player) {
    loggedIn.remove(player.getUniqueId());
  }
}
