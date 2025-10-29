package com.thecode.miniedit.commands;

import com.thecode.miniedit.edit.SelectionManager;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class SetCommand implements CommandExecutor {
    private final SelectionManager manager;

    public SetCommand(SelectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || args.length != 1) return false;
        Player player = (Player) sender;
        Material block = Material.matchMaterial(args[0]);
        if (block == null) {
            player.sendMessage("Invalid block type.");
            return true;
        }

        Location pos1 = manager.getPos1(player);
        Location pos2 = manager.getPos2(player);
        if (pos1 == null || pos2 == null) {
            player.sendMessage("Select two points first using //wand.");
            return true;
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    player.getWorld().getBlockAt(x, y, z).setType(block);
                }
            }
        }

        player.sendMessage("Region set to " + block.name() + ".");
        return true;
    }
}
