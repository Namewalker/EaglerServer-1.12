package com.thecode.miniedit.commands;

import com.thecode.miniedit.edit.SelectionManager;
import com.thecode.miniedit.edit.ClipboardManager;
import com.thecode.miniedit.edit.ClipboardManager.BlockData;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

public class CopyCommand implements CommandExecutor {
    private final SelectionManager selectionManager;
    private final ClipboardManager clipboardManager;

    public CopyCommand(SelectionManager selectionManager, ClipboardManager clipboardManager) {
        this.selectionManager = selectionManager;
        this.clipboardManager = clipboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        Location pos1 = selectionManager.getPos1(player);
        Location pos2 = selectionManager.getPos2(player);
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

        List<BlockData> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Material mat = player.getWorld().getBlockAt(x, y, z).getType();
                    blocks.add(new BlockData(x - minX, y - minY, z - minZ, mat));
                }
            }
        }

        clipboardManager.copy(player.getUniqueId(), blocks);
        player.sendMessage("Copied " + blocks.size() + " blocks to clipboard.");
        return true;
    }
}
