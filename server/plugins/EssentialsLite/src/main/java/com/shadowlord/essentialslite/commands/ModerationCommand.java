package com.shadowlord.essentialslite.commands;

import com.shadowlord.essentialslite.EssentialsLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ModerationCommand implements CommandExecutor {
  private final EssentialsLite plugin;
  private final Set<UUID> muted = Collections.synchronizedSet(new HashSet<>());
  private boolean socialSpyEnabled = false;

  public ModerationCommand(EssentialsLite plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) { sender.sendMessage("Only players can use this."); return true; }
    Player p = (Player) sender;
    String cmd = label.toLowerCase();

    if (cmd.equals("mute")) {
      if (!p.hasPermission("es.mute")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /mute <player>"); return true; }
      Player t = Bukkit.getPlayer(args[0]);
      if (t == null) { p.sendMessage(ChatColor.RED + "Player not online."); return true; }
      muted.add(t.getUniqueId());
      p.sendMessage(ChatColor.GREEN + "Muted " + t.getName());
      t.sendMessage(ChatColor.RED + "You have been muted.");
      return true;
    }

    if (cmd.equals("unmute")) {
      if (!p.hasPermission("es.unmute")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /unmute <player>"); return true; }
      Player t = Bukkit.getPlayer(args[0]);
      if (t == null) { p.sendMessage(ChatColor.RED + "Player not online."); return true; }
      muted.remove(t.getUniqueId());
      p.sendMessage(ChatColor.GREEN + "Unmuted " + t.getName());
      t.sendMessage(ChatColor.GREEN + "You have been unmuted.");
      return true;
    }

    if (cmd.equals("socialspy")) {
      if (!p.hasPermission("es.socialspy")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
      socialSpyEnabled = !socialSpyEnabled;
      p.sendMessage(ChatColor.GREEN + "SocialSpy " + (socialSpyEnabled ? "enabled" : "disabled"));
      return true;
    }

    return true;
  }

  public boolean isMuted(UUID uuid) { return muted.contains(uuid); }
  public boolean isSocialSpyEnabled() { return socialSpyEnabled; }
}
