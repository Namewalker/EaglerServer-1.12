package com.thecode.miniedit.commands;

import com.thecode.miniedit.edit.BrushTool;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ToolCommand implements CommandExecutor {
    private final BrushTool brushTool;

    public ToolCommand(BrushTool brushTool) {
        this.brushTool = brushTool;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length != 3 || !args[0].equalsIgnoreCase("sphere")) {
            player.sendMessage("Usage: //tool sphere <block> <radius>");
            return true;
        }

        Material fillMat = Material.matchMaterial(args[1]);
        if (fillMat == null) {
            player.sendMessage("Invalid block type.");
            return true;
        }

        int radius;
        try {
            radius = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid radius.");
            return true;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            player.sendMessage("Hold an item to bind the brush to.");
            return true;
        }

        brushTool.bindBrush(player, handItem.getType(), fillMat, radius);
        return true;
    }
}
