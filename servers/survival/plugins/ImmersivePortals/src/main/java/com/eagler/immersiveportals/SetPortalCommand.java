package com.eagler.immersiveportals;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPortalCommand implements CommandExecutor {
    private final PortalManager portalManager;

    public SetPortalCommand(PortalManager portalManager) {
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
        if (args.length != 7) {
            sender.sendMessage("Usage: /setportal <x1> <y1> <z1> <x2> <y2> <z2> <name>");
            return true;
        }
        try {
            int x1 = Integer.parseInt(args[0]);
            int y1 = Integer.parseInt(args[1]);
            int z1 = Integer.parseInt(args[2]);
            int x2 = Integer.parseInt(args[3]);
            int y2 = Integer.parseInt(args[4]);
            int z2 = Integer.parseInt(args[5]);
            String name = args[6];
            Location loc1 = new Location(player.getWorld(), x1, y1, z1);
            Location loc2 = new Location(player.getWorld(), x2, y2, z2);
            portalManager.setPortal(loc1, loc2, name, player);
        } catch (NumberFormatException e) {
            sender.sendMessage("Coordinates must be numbers.");
        }
        return true;
    }
}
