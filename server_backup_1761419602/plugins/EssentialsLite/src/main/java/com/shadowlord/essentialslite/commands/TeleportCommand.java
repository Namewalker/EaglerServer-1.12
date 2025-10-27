package com.shadowlord.essentialslite.commands;

import com.shadowlord.essentialslite.EssentialsLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportCommand implements CommandExecutor, Listener {
  private final EssentialsLite plugin;
  // requester -> target
  private final ConcurrentHashMap<UUID, UUID> requests = new ConcurrentHashMap<>();

  public TeleportCommand(EssentialsLite plugin) { this.plugin = plugin; }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) { sender.sendMessage("Only players."); return true; }
    Player p = (Player) sender;
    String cmd = label.toLowerCase();

    if (cmd.equals("tpa")) {
      if (!p.hasPermission("es.tpa")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /tpa <player>"); return true; }
      Player t = Bukkit.getPlayer(args[0]);
      if (t == null) { p.sendMessage(ChatColor.RED + "Player not online."); return true; }
      requests.put(p.getUniqueId(), t.getUniqueId());
      t.sendMessage(ChatColor.AQUA + p.getName() + " requests to teleport to you. Use /tpaccept or /tpdeny.");
      p.sendMessage(ChatColor.GREEN + "Teleport request sent.");
      return true;
    }

    if (cmd.equals("tpaccept")) {
      UUID requesterId = null;
      for (Map.Entry<UUID, UUID> e : requests.entrySet()) {
        if (e.getValue().equals(p.getUniqueId())) { requesterId = e.getKey(); break; }
      }
      if (requesterId == null) { p.sendMessage(ChatColor.RED + "No pending requests."); return true; }
      Player requester = Bukkit.getPlayer(requesterId);
      if (requester == null) { p.sendMessage(ChatColor.RED + "Requester not online."); requests.remove(requesterId); return true; }
      requester.teleport(p.getLocation());
      requester.sendMessage(ChatColor.GREEN + "Your teleport request was accepted.");
      p.sendMessage(ChatColor.GREEN + "Accepted teleport request.");
      requests.remove(requesterId);
      return true;
    }

    if (cmd.equals("tpdeny")) {
      UUID requesterId = null;
      for (Map.Entry<UUID, UUID> e : requests.entrySet()) {
        if (e.getValue().equals(p.getUniqueId())) { requesterId = e.getKey(); break; }
      }
      if (requesterId == null) { p.sendMessage(ChatColor.RED + "No pending requests."); return true; }
      Player requester = Bukkit.getPlayer(requesterId);
      if (requester != null) requester.sendMessage(ChatColor.RED + "Your teleport request was denied.");
      p.sendMessage(ChatColor.GREEN + "Denied teleport request.");
      requests.remove(requesterId);
      return true;
    }

    if (cmd.equals("tphere")) {
      if (!p.hasPermission("es.tp")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /tphere <player>"); return true; }
      Player t = Bukkit.getPlayer(args[0]);
      if (t == null) { p.sendMessage(ChatColor.RED + "Player not online."); return true; }
      t.teleport(p.getLocation());
      p.sendMessage(ChatColor.GREEN + "Teleported " + t.getName() + " to you.");
      return true;
    }

    return true;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    // clean up any requests involving the quitting player
    UUID id = e.getPlayer().getUniqueId();
    requests.remove(id);
    requests.entrySet().removeIf(entry -> entry.getValue().equals(id));
  }
}
