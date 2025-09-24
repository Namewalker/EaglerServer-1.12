package com.shadowlord.eaglerlogin.listeners;

import com.shadowlord.eaglerlogin.SessionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisconnectListener implements Listener {
  private final SessionManager sessions;

  public DisconnectListener(SessionManager sessions) {
    this.sessions = sessions;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    sessions.logout(event.getPlayer());
  }
}

mv ~/Projects/EaglerLogin/target/EaglerLogin-1.0-SNAPSHOT.jar ~/Servers/EaglerServer/plugins/
