package com.shadowlord.eaglerlogin.commands;

import com.shadowlord.eaglerlogin.listeners.LoginListener;
import com.shadowlord.eaglerlogin.util.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {
  private final LoginListener loginListener;

  public LoginCommand(LoginListener loginListener) {
    this.loginListener = loginListener;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Console cannot use this command.");
      return true;
    }
    Player p = (Player) sender;
    if (args.length != 1) {
      p.sendMessage(MsgUtil.color("&eUsage: &6/login <password>"));
      return true;
    }
    if (!loginListener.isRegistered(p.getUniqueId())) {
      String msg = loginListener.getPlugin().getConfig().getString("messages.not-registered","&cNot registered");
      p.sendMessage(MsgUtil.color(msg));
      return true;
    }
    boolean ok = loginListener.attemptLogin(p, args[0]);
    if (ok) {
      String msg = loginListener.getPlugin().getConfig().getString("messages.logged-in","&aLogged in");
      p.sendMessage(MsgUtil.color(msg));
    } else {
      String msg = loginListener.getPlugin().getConfig().getString("messages.wrong-password","&cWrong password");
      p.sendMessage(MsgUtil.color(msg));
    }
    return true;
  }
}
