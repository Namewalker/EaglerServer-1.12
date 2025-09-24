package com.shadowlord.eaglerlogin.commands;

import com.shadowlord.eaglerlogin.UserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {
  private final UserManager users;

  public RegisterCommand(UserManager users) {
    this.users = users;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) return false;
    Player player = (Player) sender;

    if (args.length != 1) {
      player.sendMessage("Usage: /register <password>");
      return true;
    }

    if (users.register(player.getUniqueId(), args[0])) {
      player.sendMessage("Registration successful! Use /login <password> to enter.");
    } else {
      player.sendMessage("You are already registered.");
    }
    return true;
  }
}
