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
    loginListener.getPlugin().getLogger().info("EaglerLogin: /login invoked by " + (sender instanceof Player ? ((Player)sender).getName() : "console"));
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
      p.sendMessage(MsgUtil.color(loginListener.getPlugin().getConfig().getString("messages.not-registered","&cNot registered")));
      return true;
    }
    boolean ok = loginListener.attemptLogin(p, args[0]);
    if (ok) {
      p.sendMessage(MsgUtil.color(loginListener.getPlugin().getConfig().getString("messages.logged-in","&aLogged in")));
    } else {
      p.sendMessage(MsgUtil.color(loginListener.getPlugin().getConfig().getString("messages.wrong-password","&cWrong password")));
    }
    return true;
  }
}
