package com.shadowlord.eaglerlogin.commands;

import com.shadowlord.eaglerlogin.util.BypassManager;
import com.shadowlord.eaglerlogin.util.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BypassCreateCommand implements CommandExecutor {
  private final BypassManager manager;

  public BypassCreateCommand(BypassManager manager) {
    this.manager = manager;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("eaglerlogin.bypassadmin")) {
      sender.sendMessage(MsgUtil.color("&cYou do not have permission to use this command."));
      return true;
    }
    if (args.length != 1) {
      sender.sendMessage(MsgUtil.color("&eUsage: /bypasscreate <password>"));
      return true;
    }
    boolean ok = manager.setBypassPassword(args[0]);
    sender.sendMessage(ok ? MsgUtil.color("&aBypass password set.") : MsgUtil.color("&cFailed to set bypass password."));
    return true;
  }
}
