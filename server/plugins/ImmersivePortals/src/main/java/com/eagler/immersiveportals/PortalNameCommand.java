package com.eagler.immersiveportals;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PortalNameCommand implements CommandExecutor {
    private final PortalManager portalManager;

    public PortalNameCommand(PortalManager portalManager) {
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
        if (args.length != 2) {
            sender.sendMessage("Usage: /portalname <portal name> <true/false>");
            return true;
        }
        String name = args[0];
        boolean show;
        if (args[1].equalsIgnoreCase("true")) {
            show = true;
        } else if (args[1].equalsIgnoreCase("false")) {
            show = false;
        } else {
            sender.sendMessage("Second argument must be true or false.");
            return true;
        }
        boolean success = portalManager.setPortalNameTagVisible(name, show, player);
        if (!success) {
            player.sendMessage("No portal found with name '" + name + "'.");
        }
        return true;
    }
}
