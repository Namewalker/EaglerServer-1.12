package com.shadowlord.eaglerlogin.commands;

import com.shadowlord.eaglerlogin.EaglerLoginPlugin;
import com.shadowlord.eaglerlogin.listeners.LoginListener;
import com.shadowlord.eaglerlogin.util.MsgUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class AdminCommands implements CommandExecutor {
  private final LoginListener loginListener;
  private final EaglerLoginPlugin plugin;
  private final SecureRandom random = new SecureRandom();

  public AdminCommands(LoginListener loginListener, EaglerLoginPlugin plugin) {
    this.loginListener = loginListener;
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    String cmd = label.toLowerCase();
    if (!sender.hasPermission("eaglerlogin.admin")) {
      sender.sendMessage(MsgUtil.color("&cYou do not have permission to use this command."));
      return true;
    }
    try {
      switch (cmd) {
        case "setpassword":
          return handleSetPassword(sender, args);
        case "resetpassword":
          return handleResetPassword(sender, args);
        case "showhash":
          return handleShowHash(sender, args);
        default:
          return false;
      }
    } catch (Exception e) {
      plugin.getLogger().severe("Admin command error: " + e.getMessage());
      sender.sendMessage(MsgUtil.color("&cAn error occurred. See server console for details."));
      return true;
    }
  }

  private boolean handleSetPassword(CommandSender sender, String[] args) {
    if (args.length != 2) {
      sender.sendMessage(MsgUtil.color("&eUsage: /setpassword <player> <password>"));
      return true;
    }
    String playerName = args[0];
    String password = args[1];
    OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
    UUID uuid = op.getUniqueId();
    if (uuid == null) {
      sender.sendMessage(MsgUtil.color("&cPlayer not found."));
      return true;
    }
    boolean ok = loginListener.setPasswordForUuid(uuid, password, playerName);
    if (ok) {
      sender.sendMessage(MsgUtil.color("&aPassword set for player " + playerName));
      plugin.getLogger().info("Admin " + sender.getName() + " set password for " + playerName);
    } else {
      sender.sendMessage(MsgUtil.color("&cFailed to set password."));
    }
    return true;
  }

  private boolean handleResetPassword(CommandSender sender, String[] args) {
    if (args.length != 1) {
      sender.sendMessage(MsgUtil.color("&eUsage: /resetpassword <player>"));
      return true;
    }
    String playerName = args[0];
    OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
    UUID uuid = op.getUniqueId();
    if (uuid == null) {
      sender.sendMessage(MsgUtil.color("&cPlayer not found."));
      return true;
    }
    byte[] b = new byte[12];
    random.nextBytes(b);
    String newPass = Base64.getUrlEncoder().withoutPadding().encodeToString(b);

    boolean ok = loginListener.setPasswordForUuid(uuid, newPass, playerName);
    if (ok) {
      plugin.getLogger().info("Reset password for " + playerName + " to: " + newPass);
      sender.sendMessage(MsgUtil.color("&aPassword reset. New password printed to console."));
    } else {
      sender.sendMessage(MsgUtil.color("&cFailed to reset password."));
    }
    return true;
  }

  private boolean handleShowHash(CommandSender sender, String[] args) {
    if (args.length != 1) {
      sender.sendMessage(MsgUtil.color("&eUsage: /showhash <player>"));
      return true;
    }
    String playerName = args[0];
    OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
    UUID uuid = op.getUniqueId();
    if (uuid == null) {
      sender.sendMessage(MsgUtil.color("&cPlayer not found."));
      return true;
    }
    String salt = loginListener.getStoredSalt(uuid);
    String hash = loginListener.getStoredHash(uuid);
    if (salt == null || hash == null) {
      sender.sendMessage(MsgUtil.color("&cPlayer is not registered."));
      return true;
    }
    sender.sendMessage(MsgUtil.color("&eSalt: &6" + salt));
    sender.sendMessage(MsgUtil.color("&eHash: &6" + hash));
    plugin.getLogger().info(sender.getName() + " inspected hash for " + playerName + " (" + uuid + ")");
    return true;
  }
}
