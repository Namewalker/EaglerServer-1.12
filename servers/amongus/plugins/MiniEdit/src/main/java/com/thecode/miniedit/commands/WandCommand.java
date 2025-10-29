package com.thecode.miniedit.commands;

import com.thecode.miniedit.edit.SelectionManager;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WandCommand implements CommandExecutor {
    private final SelectionManager manager;

    public WandCommand(SelectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        player.getInventory().addItem(new ItemStack(Material.WOOD_AXE));
        player.sendMessage("Selection wand given. Left click to set pos1, right click to set pos2.");
        return true;
    }
}
