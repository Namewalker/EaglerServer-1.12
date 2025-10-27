package com.shadowlord.essentialslite.commands;

import com.shadowlord.essentialslite.EssentialsLite;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

public class ToolCommands implements CommandExecutor {
  private final EssentialsLite plugin;
  public ToolCommands(EssentialsLite plugin) { this.plugin = plugin; }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) { sender.sendMessage("Only players."); return true; }
    Player p = (Player) sender;
    String cmd = label.toLowerCase();

    switch (cmd) {
      case "repair":
        if (!p.hasPermission("es.repair")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType() == Material.AIR) { p.sendMessage(ChatColor.RED + "No item in hand."); return true; }
        inHand.setDurability((short)0);
        p.sendMessage(ChatColor.GREEN + "Item repaired.");
        break;

      case "hat":
        if (!p.hasPermission("es.hat")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        ItemStack held = p.getInventory().getItemInMainHand();
        p.getInventory().setHelmet(held);
        p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        p.sendMessage(ChatColor.GREEN + "Wearing held item as hat.");
        break;

      case "near":
        if (!p.hasPermission("es.near")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        StringBuilder sb = new StringBuilder();
        for (Player other : plugin.getServer().getOnlinePlayers()) {
          if (other.equals(p)) continue;
          if (other.getWorld().equals(p.getWorld()) && other.getLocation().distance(p.getLocation()) <= 50) {
            sb.append(other.getName()).append(" ");
          }
        }
        p.sendMessage(ChatColor.GREEN + "Nearby: " + (sb.length()==0 ? "none" : sb.toString()));
        break;

      case "lightning":
        if (!p.hasPermission("es.lightning")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /lightning <player>"); return true; }
        Player t = plugin.getServer().getPlayer(args[0]);
        if (t == null) { p.sendMessage(ChatColor.RED + "Player not online."); return true; }
        t.getWorld().strikeLightningEffect(t.getLocation());
        p.sendMessage(ChatColor.GREEN + "Struck lightning at " + t.getName());
        break;

      case "nuke":
        if (!p.hasPermission("es.nuke")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        for (Player other : plugin.getServer().getOnlinePlayers()) {
          if (other.equals(p)) continue;
          Location above = other.getLocation().clone().add(0, 30, 0);
          other.getWorld().strikeLightningEffect(above);
        }
        p.sendMessage(ChatColor.GREEN + "Nuke launched.");
        break;

      case "fireball":
        if (!p.hasPermission("es.fireball")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        Fireball fb = p.launchProjectile(Fireball.class);
        fb.setVelocity(p.getLocation().getDirection().multiply(2));
        p.sendMessage(ChatColor.GREEN + "Fireball launched.");
        break;

      case "burn":
        if (!p.hasPermission("es.burn")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /burn <player>"); return true; }
        Player burnTarget = plugin.getServer().getPlayer(args[0]);
        if (burnTarget == null) { p.sendMessage(ChatColor.RED + "Player not online."); return true; }
        burnTarget.setFireTicks(100);
        p.sendMessage(ChatColor.GREEN + "Set " + burnTarget.getName() + " on fire.");
        break;

      case "ext":
        if (!p.hasPermission("es.ext")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /ext <player>"); return true; }
        Player extTarget = plugin.getServer().getPlayer(args[0]);
        if (extTarget == null) { p.sendMessage(ChatColor.RED + "Player not online."); return true; }
        extTarget.setFireTicks(0);
        p.sendMessage(ChatColor.GREEN + "Extinguished " + extTarget.getName());
        break;

      case "antioch":
        if (!p.hasPermission("es.antioch")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        Location target = p.getTargetBlock(null, 50).getLocation().add(0,1,0);
        p.getWorld().spawnEntity(target, EntityType.PRIMED_TNT);
        p.sendMessage(ChatColor.GREEN + "TNT spawned at target block.");
        break;

      case "shout":
        if (!p.hasPermission("es.shout")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length == 0) { p.sendMessage(ChatColor.YELLOW + "Usage: /shout <message>"); return true; }
        String msg = String.join(" ", args);
        for (Player pl : plugin.getServer().getOnlinePlayers()) {
          if (pl.getWorld().equals(p.getWorld()) && pl.getLocation().distance(p.getLocation()) <= 128) {
            pl.sendMessage(ChatColor.LIGHT_PURPLE + "[Shout] " + p.getName() + ": " + ChatColor.RESET + msg);
          }
        }
        p.sendMessage(ChatColor.GREEN + "Shouted.");
        break;

      case "powertool":
        if (!p.hasPermission("es.powertool")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length < 1) { p.sendMessage(ChatColor.YELLOW + "Usage: /powertool <command...>"); return true; }
        String cmdBind = String.join(" ", args);
        plugin.getStore().getConfig().set("powertools." + p.getUniqueId(), cmdBind);
        plugin.getStore().save();
        p.sendMessage(ChatColor.GREEN + "PowerTool bound: " + cmdBind);
        break;

      default:
        p.sendMessage(ChatColor.YELLOW + "Unknown tool command.");
    }
    return true;
  }
}
