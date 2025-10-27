package com.eagler;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class WorldEditSimple extends JavaPlugin implements Listener {
    private final Map<Player, Location[]> selections = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (label.equalsIgnoreCase("wand")) {
            player.getInventory().addItem(new ItemStack(Material.WOODEN_AXE));
            player.sendMessage("§6Wand given! Left/right click blocks to select region.");
            return true;
        }
        if (label.equalsIgnoreCase("set")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /set <block>");
                return true;
            }
            Location[] sel = selections.get(player);
            if (sel == null || sel[0] == null || sel[1] == null) {
                player.sendMessage("§cSelect two points first with the wand.");
                return true;
            }
            Material mat = Material.matchMaterial(args[0]);
            if (mat == null) {
                player.sendMessage("§cUnknown block type.");
                return true;
            }
            int minX = Math.min(sel[0].getBlockX(), sel[1].getBlockX());
            int maxX = Math.max(sel[0].getBlockX(), sel[1].getBlockX());
            int minY = Math.min(sel[0].getBlockY(), sel[1].getBlockY());
            int maxY = Math.max(sel[0].getBlockY(), sel[1].getBlockY());
            int minZ = Math.min(sel[0].getBlockZ(), sel[1].getBlockZ());
            int maxZ = Math.max(sel[0].getBlockZ(), sel[1].getBlockZ());
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Block block = player.getWorld().getBlockAt(x, y, z);
                        block.setType(mat);
                    }
                }
            }
            player.sendMessage("§6Region set to " + mat.name() + "!");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.WOODEN_AXE) return;
        if (!selections.containsKey(player)) selections.put(player, new Location[2]);
        Location[] sel = selections.get(player);
        if (event.getAction().toString().contains("LEFT_CLICK")) {
            sel[0] = event.getClickedBlock().getLocation();
            player.sendMessage("§aFirst position set: " + locString(sel[0]));
        } else if (event.getAction().toString().contains("RIGHT_CLICK")) {
            sel[1] = event.getClickedBlock().getLocation();
            player.sendMessage("§aSecond position set: " + locString(sel[1]));
        }
    }

    private String locString(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}
