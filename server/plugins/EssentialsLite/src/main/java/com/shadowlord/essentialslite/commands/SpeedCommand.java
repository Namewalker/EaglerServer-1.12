package com.shadowlord.essentialslite.commands;

import com.shadowlord.essentialslite.EssentialsLite;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SpeedCommand implements CommandExecutor {
  private final EssentialsLite plugin;
  public SpeedCommand(EssentialsLite plugin) { this.plugin = plugin; }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) { sender.sendMessage("Only players."); return true; }
    Player p = (Player) sender;
    if (!p.hasPermission("es.speed")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }

    if (args.length == 0) {
      p.sendMessage(ChatColor.YELLOW + "Usage: /speed <walk|fly|run> <0-10>");
      return true;
    }

    String mode = args[0].toLowerCase();
    float speed = 0.2f;
    if (args.length >= 2) {
      try { speed = Math.min(1.0f, Math.max(0f, Float.parseFloat(args[1]) / 10f)); }
      catch (NumberFormatException e) { p.sendMessage(ChatColor.RED + "Invalid speed."); return true; }
    }

    switch (mode) {
      case "walk":
        p.setWalkSpeed(speed);
        p.sendMessage(ChatColor.GREEN + "Walk speed set to " + (int)(speed*10));
        break;
      case "fly":
        p.setFlySpeed(speed);
        p.setAllowFlight(true);
        p.sendMessage(ChatColor.GREEN + "Fly speed set to " + (int)(speed*10));
        break;
      case "run":
        // run maps to sprint toggle; we emulate via giving sprinting if speed large enough
        p.setSprinting(speed > 0.5f);
        p.sendMessage(ChatColor.GREEN + "Run speed toggled based on speed value.");
        break;
      default:
        p.sendMessage(ChatColor.YELLOW + "Usage: /speed <walk|fly|run> <0-10>");
    }
    return true;
  }
}
