package com.shadowlord.cursedaltar.commands;

import com.shadowlord.cursedaltar.CursedManager;
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
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Only players can break curses.");
      return true;
    }

    Player player = (Player) sender;
    UUID uuid = player.getUniqueId();
    if (cursed.isCursed(uuid)) {
      cursed.removeCurse(uuid);
      player.sendMessage("§aThe curse has been lifted. You feel light again.");
    } else {
      player.sendMessage("§7You are not cursed.");
    }
    return true;
  }
}
