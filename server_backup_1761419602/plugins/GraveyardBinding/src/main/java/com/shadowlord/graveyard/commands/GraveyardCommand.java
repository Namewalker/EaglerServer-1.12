package com.shadowlord.graveyard.commands;

import com.shadowlord.graveyard.GraveyardManager;
import com.shadowlord.graveyard.GraveyardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class GraveyardCommand implements CommandExecutor {
  private final GraveyardPlugin plugin;
  private final GraveyardManager manager;

  public GraveyardCommand(GraveyardPlugin plugin) {
    this.plugin = plugin;
    this.manager = plugin.getManager();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    String lc = label.toLowerCase();
    if (lc.equals("gcore")) {
      if (!(sender instanceof Player) && args.length < 1) {
        sender.sendMessage("Console must specify a player.");
        return true;
      }
      if (!sender.hasPermission("graveyard.admin")) { sender.sendMessage("No permission."); return true; }
      Player giver = (sender instanceof Player) ? (Player) sender : null;
      Player target = null;
      if (args.length >= 1) {
        target = Bukkit.getPlayer(args[0]);
        if (target == null) { sender.sendMessage("Player not found."); return true; }
      } else {
        target = giver;
      }
      ItemStack core = new ItemStack(Material.matchMaterial("MOSSY_COBBLESTONE"), 1);
      ItemMeta im = core.getItemMeta();
      im.setDisplayName("Graveyard Core");
      im.setLore(Arrays.asList("Place this to create a graveyard."));
      core.setItemMeta(im);
      target.getInventory().addItem(core);
      target.updateInventory();
      sender.sendMessage("Graveyard Core given to " + target.getName());
      return true;
    }

    if (!(sender instanceof Player)) { sender.sendMessage("Only players can use this."); return true; }
    Player p = (Player) sender;
    if (args.length == 0) { p.sendMessage("Usage: /graveyard <bind|unbind|info|upgrade|create>"); return true; }
    String sub = args[0].toLowerCase();

    if (sub.equals("create")) {
      if (!p.hasPermission("graveyard.admin")) { p.sendMessage("No permission."); return true; }
      Location loc = p.getLocation().getBlock().getLocation();
      manager.createGrave(p.getUniqueId(), loc);
      manager.bindPlayerTo(p.getUniqueId(), loc);
      p.sendMessage("Graveyard created at your location and bound to you.");
      return true;
    }

    if (sub.equals("bind")) {
      Location nearest = findNearestGrave(p);
      if (nearest == null) { p.sendMessage("No graveyards found."); return true; }
      manager.bindPlayerTo(p.getUniqueId(), nearest);
      p.sendMessage("Bound to nearest graveyard.");
      return true;
    }

    if (sub.equals("unbind")) {
      manager.unbindPlayer(p.getUniqueId());
      p.sendMessage("Unbound.");
      return true;
    }

    if (sub.equals("info")) {
      Location b = manager.getBoundLocation(p.getUniqueId());
      p.sendMessage("Bound to: " + (b == null ? "None" : (b.getBlockX() + " " + b.getBlockY() + " " + b.getBlockZ())));
      return true;
    }

    if (sub.equals("upgrade")) {
      boolean ok = manager.upgradeGrave(p.getUniqueId(), p);
      if (ok) p.sendMessage("Graveyard upgraded.");
      else p.sendMessage("Upgrade failed. Not enough Soul Tokens or no graveyard.");
      return true;
    }

    p.sendMessage("Unknown subcommand.");
    return true;
  }

  private Location findNearestGrave(Player p) {
    double best = Double.MAX_VALUE;
    Location bestLoc = null;
    for (Location loc : manager.listAllGraveCenters()) {
      double d = loc.distance(p.getLocation());
      if (d < best) { best = d; bestLoc = loc; }
    }
    return bestLoc;
  }
}
