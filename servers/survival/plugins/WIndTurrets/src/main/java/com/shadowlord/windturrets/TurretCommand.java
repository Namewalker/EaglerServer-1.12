package shadowlord.windturrets;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class TurretCommand implements CommandExecutor {

    private final WindTurrets plugin;

    public TurretCommand(WindTurrets plugin) {
        this.plugin = plugin;
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(c("&6Turret commands:"));
            sender.sendMessage(c("&e/turret givecore &7- give a craft result core"));
            sender.sendMessage(c("&e/turret list &7- list turrets"));
            sender.sendMessage(c("&e/turret remove <id> &7- remove turret"));
            sender.sendMessage(c("&e/turret info <id> &7- info"));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("givecore")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(c("&cOnly players can receive items."));
                return true;
            }
            Player p = (Player) sender;
            ItemStack core = Util.createTurretCore(plugin.getConfig());
            p.getInventory().addItem(core);
            p.sendMessage(c("&aGiven a Wind Turret Core."));
            return true;
        }

        if (sub.equals("list")) {
            sender.sendMessage(c("&6Configured turrets:"));
            Map<String, Turret> turrets = plugin.getTurrets();
            if (turrets.isEmpty()) sender.sendMessage(c("&e(none)"));
            for (Turret t : turrets.values()) {
                sender.sendMessage(c("&e- " + t.getId() + " owner=" + t.getOwner() + " enabled=" + t.isEnabled() + " radius=" + t.getRadius()));
            }
            return true;
        }

        if (sub.equals("remove")) {
            if (!sender.hasPermission("windturrets.admin")) {
                sender.sendMessage(c("&cNo permission."));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(c("&cUsage: /turret remove <id>"));
                return true;
            }
            String id = args[1];
            if (plugin.getTurrets().containsKey(id)) {
                plugin.removeTurret(id);
                sender.sendMessage(c("&aRemoved turret " + id));
            } else sender.sendMessage(c("&cNo such turret."));
            return true;
        }

        if (sub.equals("info")) {
            if (args.length < 2) {
                sender.sendMessage(c("&cUsage: /turret info <id>"));
                return true;
            }
            String id = args[1];
            Turret t = plugin.getTurrets().get(id);
            if (t == null) { sender.sendMessage(c("&cNo such turret.")); return true; }
            sender.sendMessage(c("&6Turret " + id));
            sender.sendMessage(c("&eOwner: &f" + t.getOwner()));
            sender.sendMessage(c("&eLoc: &f" + (t.getLocation() != null ? Util.locString(t.getLocation()) : "null")));
            sender.sendMessage(c("&eRadius: &f" + t.getRadius()));
            sender.sendMessage(c("&eEnabled: &f" + t.isEnabled()));
            return true;
        }

        if (sub.equals("toggle")) {
            if (!sender.hasPermission("windturrets.admin")) {
                sender.sendMessage(c("&cNo permission."));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(c("&cUsage: /turret toggle <id>"));
                return true;
            }
            String id = args[1];
            Turret t = plugin.getTurrets().get(id);
            if (t == null) { sender.sendMessage(c("&cNo such turret.")); return true; }
            t.setEnabled(!t.isEnabled());
            plugin.saveTurrets();
            sender.sendMessage(c("&aTurret " + id + " enabled=" + t.isEnabled()));
            return true;
        }

        sender.sendMessage(c("&cUnknown subcommand."));
        return true;
    }
}
