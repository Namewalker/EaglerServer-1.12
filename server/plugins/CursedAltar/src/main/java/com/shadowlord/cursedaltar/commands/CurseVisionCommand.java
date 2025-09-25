package com.shadowlord.cursedaltar.commands;

import com.shadowlord.cursedaltar.CursedManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class CurseVisionCommand implements CommandExecutor {
  private final CursedManager cursed;

  public CurseVisionCommand(CursedManager cursed) {
    this.cursed = cursed;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.isOp()) {
      sender.sendMessage("§cOnly server operators can use /cursevision.");
      return true;
    }

    if (args.length < 1 || args.length > 2) {
      sender.sendMessage("§cUsage: /cursevision <seconds> [player]");
      return true;
    }

    int seconds;
    try {
      seconds = Integer.parseInt(args[0]);
      if (seconds <= 0) throw new NumberFormatException();
    } catch (NumberFormatException ex) {
      sender.sendMessage("§cSeconds must be a positive integer.");
      return true;
    }

    if (args.length == 2) {
      Player target = Bukkit.getPlayer(args[1]);
      if (target == null) {
        sender.sendMessage("§cPlayer not found or not online.");
        return true;
      }
      applyVisionToAllCursed(target, seconds);
      sender.sendMessage("§aActivated curse vision for " + seconds + "s for " + target.getName() + ".");
      target.sendMessage("§aYou feel the Curse Mirror reveal the cursed for " + seconds + " seconds.");
      return true;
    }

    // No player specified: apply to command sender if it's a player
    if (sender instanceof Player) {
      Player p = (Player) sender;
      applyVisionToAllCursed(p, seconds);
      p.sendMessage("§aActivated curse vision for " + seconds + " seconds.");
    } else {
      sender.sendMessage("§cConsole must specify a player.");
    }

    return true;
  }

  private void applyVisionToAllCursed(Player viewer, int seconds) {
    int ticks = seconds * 20;
    for (Player online : Bukkit.getOnlinePlayers()) {
      UUID u = online.getUniqueId();
      if (cursed.isCursed(u) && online.isOnline()) {
        // Apply glowing effect for duration (attempt; may require server version support)
        online.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, ticks, 0));
      }
    }
    // Also ensure the viewer sees them: in older server versions glowing is server-side; this should suffice.
  }
}
