package shadowlord.dimensions;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DimensionCommand implements CommandExecutor {

    private final ShadowDimensions plugin;

    public DimensionCommand(ShadowDimensions plugin) {
        this.plugin = plugin;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(color("&6/dimension create <name> <type> [seed]"));
            sender.sendMessage(color("&6/dimension wand"));
            sender.sendMessage(color("&6/dimension setportal <name>"));
            sender.sendMessage(color("&6/dimension removeportal <name>"));
            sender.sendMessage(color("&6/dimension list"));
            sender.sendMessage(color("&6/dimension info <name>"));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("create")) {
            if (!sender.hasPermission("shadowdimensions.admin")) {
                sender.sendMessage(color("&cNo permission."));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(color("&cUsage: /dimension create <name> <type> [seed]"));
                return true;
            }
            String name = args[1];
            String type = args[2];
            Long seed = null;
            if (args.length >= 4) {
                try { seed = Long.parseLong(args[3]); } catch (NumberFormatException e) { sender.sendMessage(color("&cInvalid seed.")); return true; }
            }
            World w = plugin.createDimension(name, type, seed);
            sender.sendMessage(color("&aCreated dimension: &e" + name + " &a(" + (w != null ? w.getName() : "null") + ")"));
            return true;
        }

        if (sub.equals("wand")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(color("&cOnly players can use the wand."));
                return true;
            }
            Player p = (Player) sender;
            if (!p.hasPermission("shadowdimensions.wand")) {
                p.sendMessage(color("&cNo permission."));
                return true;
            }
            p.getInventory().addItem(Util.createWand(plugin.getConfig()));
            p.sendMessage(color("&aGiven Portal Selector wand."));
            return true;
        }

        if (sub.equals("setportal")) {
            if (!sender.hasPermission("shadowdimensions.admin")) {
                sender.sendMessage(color("&cNo permission."));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(color("&cOnly players can set portals."));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(color("&cUsage: /dimension setportal <dimensionName>"));
                return true;
            }
            Player p = (Player) sender;
            String dim = args[1];
            Location[] sel = plugin.getSelectionManager().getSelection(p.getUniqueId());
            if (sel == null || sel[0] == null || sel[1] == null) {
                p.sendMessage(color("&cYou must select two points with the wand first."));
                return true;
            }
            if (!sel[0].getWorld().equals(sel[1].getWorld())) {
                p.sendMessage(color("&cSelection points must be in the same world."));
                return true;
            }
            Portal portal = new Portal(args[1], sel[0], sel[1], sel[0].getWorld().getName());
            plugin.savePortal(portal);
            p.sendMessage(color("&aPortal &e" + portal.getName() + " &aset to selection and saved."));
            return true;
        }

        if (sub.equals("removeportal")) {
            if (!sender.hasPermission("shadowdimensions.admin")) {
                sender.sendMessage(color("&cNo permission."));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(color("&cUsage: /dimension removeportal <name>"));
                return true;
            }
            String name = args[1];
            List<Portal> all = plugin.getAllPortals();
            Portal found = null;
            for (Portal p : all) {
                if (p.getName().equalsIgnoreCase(name)) { found = p; break; }
            }
            if (found == null) {
                sender.sendMessage(color("&cNo portal by that name."));
                return true;
            }
            plugin.removePortal(found);
            sender.sendMessage(color("&aRemoved portal &e" + name));
            return true;
        }

        if (sub.equals("list")) {
            List<Portal> all = plugin.getAllPortals();
            sender.sendMessage(color("&6Configured portals:"));
            if (all.isEmpty()) sender.sendMessage(color("&e(none)"));
            for (Portal p : all) {
                sender.sendMessage(color("&e- " + p.getName() + " -> " + p.getTargetDimension() + " in " + p.getWorldName()));
            }
            return true;
        }

        if (sub.equals("info")) {
            if (args.length < 2) {
                sender.sendMessage(color("&cUsage: /dimension info <name>"));
                return true;
            }
            String name = args[1];
            for (Portal p : plugin.getAllPortals()) {
                if (p.getName().equalsIgnoreCase(name)) {
                    sender.sendMessage(color("&6Portal: &e" + p.getName()));
                    sender.sendMessage(color("&6World: &e" + p.getWorldName()));
                    sender.sendMessage(color("&6Target Dimension: &e" + p.getTargetDimension()));
                    sender.sendMessage(color("&6Bounds: &e" + p.boundsString()));
                    return true;
                }
            }
            sender.sendMessage(color("&cNo portal found."));
            return true;
        }

        sender.sendMessage(color("&cUnknown subcommand."));
        return true;
    }
}
