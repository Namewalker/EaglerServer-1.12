package com.shadowlord.cursedaltar.commands;

import com.shadowlord.cursedaltar.CursedManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CurseLevelCommand implements CommandExecutor {
  private final CursedManager cursed;

  public CurseLevelCommand(CursedManager cursed) {
    this.cursed = cursed;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length > 1) {
      sender.sendMessage("§cUsage: /curselevel [player]");
      return true;
    }

    if (args.length == 1) {
      if (!(sender instanceof Player) || !((Player) sender).isOp()) {
        sender.sendMessage("§cOnly operators can check other players’ curse levels.");
        return true;
      }
      Player target = Bukkit.getPlayer(args[0]);
      if (target == null) {
        sender.sendMessage("§cPlayer not found or not online.");
        return true;
      }
      int level = cursed.getLevel(target.getUniqueId());
      if (level <= 0) {
        sender.sendMessage("§7" + target.getName() + " is not cursed.");
      } else {
        sender.sendMessage("§7" + target.getName() + " is cursed at level §c" + level + "§7.");
      }
      return true;
    }

    if (sender instanceof Player) {
      Player self = (Player) sender;
      UUID uuid = self.getUniqueId();
      int level = cursed.getLevel(uuid);
      if (level <= 0) {
        self.sendMessage("§aYou are not cursed.");
      } else {
        self.sendMessage("§7Your curse level is §c" + level + "§7.");
      }
    } else {
      sender.sendMessage("§cConsole must specify a player.");
    }

    return true;
  }
}
