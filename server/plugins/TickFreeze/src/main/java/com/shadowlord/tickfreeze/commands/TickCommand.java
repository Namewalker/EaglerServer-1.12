package com.shadowlord.tickfreeze.commands;

import com.shadowlord.tickfreeze.TickFreezePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TickCommand implements CommandExecutor {
  private final TickFreezePlugin plugin;

  public TickCommand(TickFreezePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("tickfreeze.use")) {
      sender.sendMessage("§cYou do not have permission to use /tick");
      return true;
    }
    if (args.length != 1) {
      sender.sendMessage("§eUsage: /tick <freeze|unfreeze>");
      return true;
    }
    String a = args[0].toLowerCase();
    if ("freeze".equals(a)) {
      plugin.freeze();
      sender.sendMessage("§aServer simulation frozen (best-effort).");
      return true;
    } else if ("unfreeze".equals(a)) {
      plugin.unfreeze();
      sender.sendMessage("§aServer simulation unfrozen.");
      return true;
    } else {
      sender.sendMessage("§eUsage: /tick <freeze|unfreeze>");
      return true;
    }
  }
}
