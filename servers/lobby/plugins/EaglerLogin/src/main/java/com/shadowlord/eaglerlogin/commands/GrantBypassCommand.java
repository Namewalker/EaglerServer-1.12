package com.shadowlord.eaglerlogin.commands;

import com.shadowlord.eaglerlogin.util.MsgUtil;
import com.shadowlord.eaglerlogin.EaglerLoginPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.entity.Player;

public class GrantBypassCommand implements CommandExecutor {
  private final EaglerLoginPlugin plugin;

  public GrantBypassCommand(EaglerLoginPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    // Console-only by your request
    if (sender instanceof Player) {
      sender.sendMessage(MsgUtil.color("&cThis command can only be run from the server console."));
      return true;
    }
    if (args.length != 1) {
      sender.sendMessage(MsgUtil.color("&eUsage: /grantbypass <player>"));
      return true;
    }
    String target = args[0];
    // Attempt to grant permission directly to online player; if offline, log instruction
    Player p = Bukkit.getPlayerExact(target);
    if (p != null) {
      p.addAttachment(plugin, "eaglerlogin.bypassadmin", true, 0);
      sender.sendMessage("Granted bypass permission to " + p.getName());
      plugin.getLogger().info("GrantBypass: console granted bypassadmin to " + p.getName());
      return true;
    } else {
      // For offline players we cannot directly grant ephemeral attachments; instruct admin to add to perms plugin
      plugin.getLogger().info("GrantBypass: console requested grant for offline player '" + target + "'. Use your permissions plugin to persist permission for offline players.");
      sender.sendMessage("Player not online. Use your permissions plugin to grant eaglerlogin.bypassadmin persistently.");
      return true;
    }
  }
}
