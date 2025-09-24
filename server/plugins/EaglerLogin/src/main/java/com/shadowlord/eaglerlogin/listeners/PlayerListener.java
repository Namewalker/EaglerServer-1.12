package com.shadowlord.eaglerlogin.listeners;

import com.shadowlord.eaglerlogin.SessionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {
  private final SessionManager sessions;

  public PlayerListener(SessionManager sessions) {
    this.sessions = sessions;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    event.getPlayer().sendMessage("Please /register or /login to continue.");
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    if (!sessions.isLoggedIn(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onChat(AsyncPlayerChatEvent event) {
    if (!sessions.isLoggedIn(event.getPlayer())) {
      event.setCancelled(true);
      event.getPlayer().sendMessage("You must /login to chat.");
    }
  }
}
