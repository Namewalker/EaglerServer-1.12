package com.shadowlord.cursedaltar.commands;

import com.shadowlord.cursedaltar.CursedManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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

    if (sender instanceof ConsoleCommandSender) {
      sender.sendMessage("§cConsole must specify a player.");
      return true;
    }

    Player p = (Player) sender;
    applyVisionToAllCursed(p, seconds);
    p.sendMessage("§aActivated curse vision for " + seconds + " seconds.");
    return true;
  }

  private void applyVisionToAllCursed(Player viewer, int seconds) {
    final int ticks = Math.max(1, seconds) * 20;
    final String teamName = "curseVisionTemp_" + viewer.getName();

    Scoreboard board = viewer.getScoreboard();
    if (board == null) {
      board = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    Team team = board.getTeam(teamName);
    if (team == null) {
      team = board.registerNewTeam(teamName);
    }

    try {
      team.setPrefix(ChatColor.RED.toString());
      try {
        java.lang.reflect.Method setOptionMethod = Team.class.getMethod("setOption", Enum.class, Enum.class);
        Class<?> optionClass = Class.forName("org.bukkit.scoreboard.Team$Option");
        Class<?> statusClass = Class.forName("org.bukkit.scoreboard.Team$OptionStatus");
        Object glowingOption = Enum.valueOf((Class<Enum>) optionClass, "GLOWING");
        Object statusAlways = Enum.valueOf((Class<Enum>) statusClass, "ALWAYS");
        setOptionMethod.invoke(team, glowingOption, statusAlways);
      } catch (Exception ignored) {
      }
    } catch (Throwable ignored) {
    }

    for (Player online : Bukkit.getOnlinePlayers()) {
      if (online == null) continue;
      UUID u = online.getUniqueId();
      if (cursed.isCursed(u)) {
        try {
          team.addEntry(online.getName());
        } catch (Throwable ignored) {
        }

        try {
          online.addPotionEffect(new org.bukkit.potion.PotionEffect(
              org.bukkit.potion.PotionEffectType.GLOWING, ticks, 0, false, false));
        } catch (Throwable ignored) {
        }
      }
    }

    final Team teamFinal = team;
    final Plugin pluginFinal = Bukkit.getPluginManager().getPlugin("CursedAltar");
    if (pluginFinal == null) {
      return;
    }

    Bukkit.getScheduler().runTaskLater(pluginFinal, () -> {
      try {
        for (Player online : Bukkit.getOnlinePlayers()) {
          if (online == null) continue;
          try { teamFinal.removeEntry(online.getName()); } catch (Throwable ignored) {}
        }
        try { teamFinal.unregister(); } catch (Throwable ignored) {}
      } catch (Throwable ignored) {
      }
    }, ticks);
  }
}
