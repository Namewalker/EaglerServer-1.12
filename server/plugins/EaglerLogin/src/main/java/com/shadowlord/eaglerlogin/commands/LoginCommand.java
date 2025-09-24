package com.shadowlord.eaglerlogin.commands;

import com.shadowlord.eaglerlogin.SessionManager;
import com.shadowlord.eaglerlogin.UserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {
  private final UserManager users;
  private final SessionManager sessions;

  public LoginCommand(UserManager users, SessionManager sessions) {
    this.users = users;
    this.sessions = sessions;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) return false;
    Player player = (Player) sender;

    if (args.length != 1) {
      player.sendMessage("Usage: /login <password>");
      return true;
    }

    if (!users.isRegistered(player.getUniqueId())) {
      player.sendMessage("You must register first: /register <password>");
      return true;
    }

    if (users.verify(player.getUniqueId(), args[0])) {
      sessions.markLoggedIn(player);
      player.sendMessage("Login successful! Welcome back.");
    } else {
      player.sendMessage("Incorrect password.");
    }
    return true;
  }
}
