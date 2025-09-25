package com.shadowlord.eaglerlogin.commands;

import com.shadowlord.eaglerlogin.EaglerLoginPlugin;
import com.shadowlord.eaglerlogin.listeners.LoginListener;
import com.shadowlord.eaglerlogin.util.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class RegisterCommand implements CommandExecutor {
  private final LoginListener loginListener;
  private final EaglerLoginPlugin plugin;

  public RegisterCommand(LoginListener loginListener, EaglerLoginPlugin plugin) {
    this.loginListener = loginListener;
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Console cannot use this command.");
      return true;
    }
    Player p = (Player) sender;
    if (args.length != 1) {
      p.sendMessage(MsgUtil.color(plugin.getConfig().getString("messages.register-first","&eUse /register <password>")));
      return true;
    }
    String pass = args[0];
    if (pass.length() < 4) {
      p.sendMessage(MsgUtil.color("&cPassword must be at least 4 characters."));
      return true;
    }
    if (loginListener.isRegistered(p.getUniqueId())) {
      p.sendMessage(MsgUtil.color(plugin.getConfig().getString("messages.not-registered","&cAlready registered")));
      return true;
    }
    boolean ok = loginListener.registerAccount(p, pass);
    if (ok) {
      p.sendMessage(MsgUtil.color(plugin.getConfig().getString("messages.register-success","&aRegistered")));
    } else {
      p.sendMessage(MsgUtil.color("&cRegistration failed. Contact an admin."));
    }
    return true;
  }
}
