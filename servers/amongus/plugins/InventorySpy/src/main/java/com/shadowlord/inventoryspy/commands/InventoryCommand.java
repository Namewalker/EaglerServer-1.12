package com.shadowlord.inventoryspy.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public class InventoryCommand implements CommandExecutor {
  private final Plugin plugin;

  public InventoryCommand(Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Only players can use this command.");
      return true;
    }

    Player viewer = (Player) sender;
    if (!viewer.hasPermission("invspy.inventory")) {
      viewer.sendMessage(ChatColor.RED + "You lack permission to use this command.");
      return true;
    }

    if (args.length == 0) {
      viewer.sendMessage(ChatColor.YELLOW + "Usage: /inventory <player> [ender]");
      return true;
    }

    String targetName = args[0];
    Player target = Bukkit.getPlayerExact(targetName);
    if (target == null || !target.isOnline()) {
      viewer.sendMessage(ChatColor.RED + "Player not found or not online: " + targetName);
      return true;
    }

    boolean viewEnder = false;
    if (args.length >= 2) {
      String second = args[1].toLowerCase();
      if ("ender".equals(second) || "enderchest".equals(second) || "ec".equals(second)) viewEnder = true;
    }

    if (viewEnder) {
      // Open target's ender chest for viewer
      Inventory targetEnder = target.getEnderChest();
      // create a temporary inventory copy to avoid editing the real ender chest directly if you prefer
      // but as requirement is to modify, we open the actual ender chest so edits persist
      viewer.openInventory(targetEnder);
      viewer.sendMessage(ChatColor.GREEN + "Opened ender chest of " + target.getName() + ".");
    } else {
      // Open target's inventory for viewer (this opens their player inventory view)
      Inventory targetInv = target.getInventory();
      viewer.openInventory(targetInv);
      viewer.sendMessage(ChatColor.GREEN + "Opened inventory of " + target.getName() + ".");
    }

    // Optional: log to console for audit
    plugin.getServer().getConsoleSender().sendMessage("[InventorySpy] " + viewer.getName() + " opened " +
      (viewEnder ? "ender chest" : "inventory") + " of " + target.getName());

    return true;
  }
}
