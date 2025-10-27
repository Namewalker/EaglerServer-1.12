package com.eagler.immersiveportals;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PortalKillCommand implements CommandExecutor {
    private final PortalManager portalManager;

    public PortalKillCommand(PortalManager portalManager) {
        this.portalManager = portalManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("You must be an operator to use this command.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage("Usage: /portalkill <portal name>");
            return true;
        }
        String name = args[0];
        boolean success = portalManager.deletePortalByName(name, player);
        if (!success) {
            player.sendMessage("No portal found with name '" + name + "'.");
        }
        return true;
    }
}
