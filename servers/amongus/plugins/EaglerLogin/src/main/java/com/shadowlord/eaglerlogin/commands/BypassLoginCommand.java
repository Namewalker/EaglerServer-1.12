package com.shadowlord.eaglerlogin.commands;

import com.shadowlord.eaglerlogin.listeners.LoginListener;
import com.shadowlord.eaglerlogin.util.BypassManager;
import com.shadowlord.eaglerlogin.util.MsgUtil;
import com.shadowlord.eaglerlogin.EaglerLoginPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BypassLoginCommand implements CommandExecutor {
  private final BypassManager manager;
  private final LoginListener loginListener;
  private final EaglerLoginPlugin plugin;

  public BypassLoginCommand(BypassManager manager, LoginListener loginListener, EaglerLoginPlugin plugin) {
    this.manager = manager;
    this.loginListener = loginListener;
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Only players can use this command.");
      return true;
    }
    Player p = (Player) sender;
    if (!p.hasPermission("eaglerlogin.bypassadmin")) {
      p.sendMessage(MsgUtil.color("&cYou do not have permission to use this command."));
      return true;
    }
    if (args.length != 1) {
      p.sendMessage(MsgUtil.color("&eUsage: /bypasslogin <password>"));
      return true;
    }
    if (!manager.hasBypassPassword()) {
      p.sendMessage(MsgUtil.color("&cNo bypass password is configured."));
      return true;
    }
    boolean ok = manager.verifyBypassPassword(args[0]);
    if (!ok) {
      p.sendMessage(MsgUtil.color("&cIncorrect bypass password."));
      plugin.getLogger().warning("Failed bypass attempt by " + p.getName());
      return true;
    }
    // Mark the player as logged in via LoginListener API
    plugin.getLogger().info("Bypass login success for " + p.getName());
    loginListener.markLoggedIn(p.getUniqueId(), p.getName());
    p.sendMessage(MsgUtil.color("&aBypass login successful. You are now authenticated."));
    return true;
  }
}
