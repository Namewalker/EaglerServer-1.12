package com.shadowlord.cursedaltar.listeners;

import com.shadowlord.cursedaltar.CursedManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.UUID;

public class DisconnectListener implements Listener {
  private final CursedManager cursed;

  public DisconnectListener(CursedManager cursed) {
    this.cursed = cursed;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    if (cursed.isCursed(uuid)) {
      cursed.escalateCurse(uuid);
    }
  }
}
