package com.shadowlord.eaglervanish.commands;

import com.shadowlord.eaglervanish.EaglerVanishPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class VanishCommand implements CommandExecutor {
  private final EaglerVanishPlugin plugin;
  private static final int INVIS_DURATION_TICKS = Integer.MAX_VALUE / 2;

  public VanishCommand(EaglerVanishPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Only players can use /vanish");
      return true;
    }
    Player p = (Player) sender;
    if (!p.hasPermission("eaglervanish.use")) {
      p.sendMessage("§cYou do not have permission to use /vanish");
      return true;
    }

    UUID id = p.getUniqueId();
    boolean nowVanished = !plugin.isVanished(id);
    plugin.setVanished(id, nowVanished);

    for (Player other : Bukkit.getOnlinePlayers()) {
      if (other.equals(p)) continue;
      if (nowVanished) other.hidePlayer(plugin, p);
      else other.showPlayer(plugin, p);
    }

    if (nowVanished) {
      p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, INVIS_DURATION_TICKS, 0, false, false));
      String msg = ChatColor.YELLOW + p.getName() + " has left the game";
      for (Player pl : Bukkit.getOnlinePlayers()) {
        if (pl.getUniqueId().equals(id)) continue;
        pl.sendMessage(msg);
      }
    } else {
      p.removePotionEffect(PotionEffectType.INVISIBILITY);
      String msg = ChatColor.YELLOW + p.getName() + " has joined the game";
      for (Player pl : Bukkit.getOnlinePlayers()) {
        if (pl.getUniqueId().equals(id)) continue;
        pl.sendMessage(msg);
      }
    }

    p.sendMessage(nowVanished ? "§7You are now vanished." : "§7You are now visible.");
    return true;
  }
}
