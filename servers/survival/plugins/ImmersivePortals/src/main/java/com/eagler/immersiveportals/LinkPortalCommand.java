package com.eagler.immersiveportals;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LinkPortalCommand implements CommandExecutor {
    private final PortalManager portalManager;

    public LinkPortalCommand(PortalManager portalManager) {
        this.portalManager = portalManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage("Usage: /linkportal <first portal #> <second portal #>");
            return true;
        }
        try {
            int id1 = Integer.parseInt(args[0]);
            int id2 = Integer.parseInt(args[1]);
            portalManager.linkPortals(id1, id2, (Player) sender);
        } catch (NumberFormatException e) {
            sender.sendMessage("Portal IDs must be numbers.");
        }
        return true;
    }
}
