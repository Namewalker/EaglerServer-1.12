package com.shadowlord.eaglervanish.listeners;

import com.shadowlord.eaglervanish.EaglerVanishPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;
import java.util.stream.Collectors;

public class VanishListener implements Listener {
  private final EaglerVanishPlugin plugin;

  public VanishListener(EaglerVanishPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player joined = e.getPlayer();
    // hide vanished players from the joiner
    for (UUID v : plugin.getVanishedPlayers()) {
      Player vp = Bukkit.getPlayer(v);
      if (vp != null && !vp.equals(joined)) {
        joined.hidePlayer(plugin, vp);
      }
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    // optional: keep vanish until manually toggled; here we clear to avoid stale state
    plugin.setVanished(e.getPlayer().getUniqueId(), false);
  }

  @EventHandler
  public void onChat(AsyncPlayerChatEvent e) {
    // prevent vanished players from broadcasting chat; allow spying via staff tools if desired
    if (plugin.isVanished(e.getPlayer().getUniqueId())) {
      e.setCancelled(true);
      e.getPlayer().sendMessage("§cYou cannot chat while vanished.");
    }
  }

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent e) {
    // intercept /list or other commands and show visible players only
    String msg = e.getMessage().toLowerCase();
    if (msg.equals("/list") || msg.startsWith("/list ")) {
      Player sender = e.getPlayer();
      String visible = Bukkit.getOnlinePlayers().stream()
        .filter(p -> !plugin.isVanished(p.getUniqueId()))
        .map(Player::getName)
        .collect(Collectors.joining(" "));
      sender.sendMessage("§7Online players: §f" + visible);
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onTarget(EntityTargetEvent e) {
    if (e.getTarget() instanceof Player) {
      Player p = (Player) e.getTarget();
      if (plugin.isVanished(p.getUniqueId())) e.setCancelled(true);
    }
  }

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player) {
      Player p = (Player) e.getDamager();
      if (plugin.isVanished(p.getUniqueId())) e.setCancelled(true);
    }
  }
}
