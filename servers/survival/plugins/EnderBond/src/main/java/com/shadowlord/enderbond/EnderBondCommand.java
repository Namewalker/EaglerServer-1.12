package shadowlord.enderbond;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EnderBondCommand implements CommandExecutor {

    private final EnderBond plugin;

    public EnderBondCommand(EnderBond plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(c("&6EnderBond commands: &a/enderbond level &a/enderbond refuge &a/enderbond give &a/enderbond release"));
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("level")) {
            if (!(sender instanceof Player)) { sender.sendMessage("Only players."); return true; }
            Player p = (Player) sender;
            int val = plugin.getBondMap().getOrDefault(p.getUniqueId(), 0);
            p.sendMessage(c("&d[EnderBond] &7Your bond level: &a" + val + "/" + plugin.getConfig().getInt("bonding.max_level", 100)));
            return true;
        }
        if (sub.equals("refuge")) {
            if (!(sender instanceof Player)) { sender.sendMessage("Only players."); return true; }
            Player p = (Player) sender;
            UUID uid = p.getUniqueId();
            if (!plugin.isRefugeUnlocked(uid)) {
                p.sendMessage(c("&cYou have not unlocked the Ender Refuge yet. Build your bond to " + plugin.getConfig().getInt("bonding.refuge_threshold", 80)));
                return true;
            }
            World refuge = plugin.getRefugeWorld();
            if (refuge == null) { p.sendMessage(c("&cRefuge world not available.")); return true; }
            // toggle: if in refuge, bring back to spawn; else teleport to refuge center
            if (p.getWorld().equals(refuge)) {
                World home = p.getServer().getWorld(plugin.getConfig().getString("dimensions.home_world", "world"));
                if (home != null) p.teleport(home.getSpawnLocation());
                p.sendMessage(c("&aReturned from the Ender Refuge."));
            } else {
                p.teleport(new org.bukkit.Location(refuge, 0.5, 65, 0.5));
                p.sendMessage(c("&aYou enter the Ender Refuge."));
            }
            return true;
        }
        if (sub.equals("give")) {
            if (!(sender instanceof Player)) { sender.sendMessage("Only players."); return true; }
            Player p = (Player) sender;
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHORUS_FRUIT);
            org.bukkit.inventory.meta.ItemMeta im = item.getItemMeta();
            im.setDisplayName(c("&dEnder Treat"));
            item.setItemMeta(im);
            p.getInventory().addItem(item);
            p.sendMessage(c("&aGiven an Ender Treat."));
            return true;
        }
        if (sub.equals("release")) {
            if (!(sender instanceof Player)) { sender.sendMessage("Only players."); return true; }
            Player p = (Player) sender;
            UUID owner = p.getUniqueId();
            plugin.getCompanionMap().remove(owner);
            p.sendMessage(c("&cYour companion has been released."));
            plugin.saveState();
            return true;
        }
        sender.sendMessage(c("&cUnknown subcommand."));
        return true;
    }
}
