package com.shadowlord.undeadsiege.commands;

import com.shadowlord.undeadsiege.UndeadSiegePlugin;
import com.shadowlord.undeadsiege.SiegeManager;
import com.shadowlord.undeadsiege.model.SiegeRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SiegeCommand implements CommandExecutor {
  private final UndeadSiegePlugin plugin;
  private final SiegeManager manager;

  public SiegeCommand(UndeadSiegePlugin plugin) {
    this.plugin = plugin;
    this.manager = plugin.getSiegeManager();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    String cmd = label.toLowerCase();

    if (cmd.equals("ssr")) {
      if (!sender.hasPermission("siege.admin")) { sender.sendMessage("No permission."); return true; }
      if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
        manager.setRegion(null);
        sender.sendMessage("Siege region cleared.");
        return true;
      }
      if (args.length == 3) {
        try {
          int x = Integer.parseInt(args[0]);
          int y = Integer.parseInt(args[1]);
          int z = Integer.parseInt(args[2]);
          World w;
          if (sender instanceof Player) w = ((Player)sender).getWorld();
          else w = Bukkit.getWorlds().get(0);
          SiegeRegion r = new SiegeRegion(new Location(w, x, y, z), 75);
          manager.setRegion(r);
          sender.sendMessage("Siege region set to " + x + " " + y + " " + z + " in " + w.getName());
        } catch (NumberFormatException ex) { sender.sendMessage("Invalid numbers."); }
        return true;
      }
      sender.sendMessage("Usage: /ssr <x> <y> <z> or /ssr clear");
      return true;
    }

    if (cmd.equals("siege")) {
      if (!sender.hasPermission("siege.admin")) { sender.sendMessage("No permission."); return true; }
      if (args.length == 0) {
        sender.sendMessage("Usage: /siege <start|stop|status|listregions>");
        return true;
      }
      String sub = args[0].toLowerCase();
      if (sub.equals("start")) {
        if (manager.getRegion() == null) { sender.sendMessage("Siege region is not set."); return true; }
        manager.startSiege(true);
        sender.sendMessage("Siege started.");
        return true;
      } else if (sub.equals("stop")) {
        manager.stopSiege(true);
        sender.sendMessage("Siege stopped.");
        return true;
      } else if (sub.equals("status")) {
        if (manager.isActive()) sender.sendMessage("A siege is currently active.");
        else sender.sendMessage("No active siege.");
        SiegeRegion r = manager.getRegion();
        if (r != null) sender.sendMessage("Region center: " + r.getX() + " " + r.getY() + " " + r.getZ() + " in " + r.getWorldName());
        else sender.sendMessage("No region configured.");
        return true;
      } else if (sub.equals("listregions")) {
        SiegeRegion r = manager.getRegion();
        if (r == null) sender.sendMessage("No regions configured.");
        else sender.sendMessage("Configured region center: " + r.getX() + " " + r.getY() + " " + r.getZ() + " in " + r.getWorldName());
        return true;
      } else {
        sender.sendMessage("Unknown subcommand.");
        return true;
      }
    }

    return true;
  }
}
