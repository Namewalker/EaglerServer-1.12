package com.shadowlord.cursedaltar.commands;

import com.shadowlord.cursedaltar.CursedManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BreakCurseCommand implements CommandExecutor {
  private final CursedManager cursed;

  public BreakCurseCommand(CursedManager cursed) {
    this.cursed = cursed;
  }

  @Override
  public boolean onCommand(CommandSender sender,
                           Command command,
                           String label,
                           String[] args) {
    // /breakcurse <player>
    if (args.length > 1) {
      sender.sendMessage("§cUsage: /breakcurse [player]");
      return true;
    }

    // Break another player’s curse
    if (args.length == 1) {
      if (!(sender instanceof Player) || !((Player) sender).isOp()) {
        sender.sendMessage("§cOnly server operators can break another player’s curse.");
        return true;
      }
      Player target = Bukkit.getPlayer(args[0]);
      if (target == null) {
        sender.sendMessage("§cPlayer not found or not online.");
        return true;
      }
      UUID uuid = target.getUniqueId();
      if (!cursed.isCursed(uuid)) {
        sender.sendMessage("§7That player is not cursed.");
      } else {
        cursed.removeCurse(uuid);
        sender.sendMessage("§aCurse removed from " + target.getName() + ".");
        target.sendMessage("§aAn operator has lifted your curse.");
      }
      return true;
    }

    // Break your own curse
    if (sender instanceof Player) {
      Player self = (Player) sender;
      UUID uuid = self.getUniqueId();
      if (!cursed.isCursed(uuid)) {
        self.sendMessage("§7You are not cursed.");
      } else {
        cursed.removeCurse(uuid);
        self.sendMessage("§aYour curse has been lifted.");
      }
    } else {
      sender.sendMessage("§cUsage: /breakcurse [player]");
    }
    return true;
  }
}
