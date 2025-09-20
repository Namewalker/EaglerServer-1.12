package com.yourname.summonnpc.commands;

import com.yourname.summonnpc.manager.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SummonNPCCommand implements CommandExecutor {
    private final NPCManager npcManager;

    public SummonNPCCommand(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("Usage: /summonnpc <playerName>");
            return false;
        }

        String targetName = args[0];
        Player player = (Player) sender;
        if (Bukkit.getOfflinePlayer(targetName).hasPlayedBefore()) {
            npcManager.spawnNPC(targetName, player.getLocation());
            player.sendMessage("Summoned NPC of " + targetName + "!");
        } else {
            player.sendMessage("That player has never joined the server.");
        }
        return true;
    }
}
