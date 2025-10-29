package com.thecode.miniedit.edit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class SelectionManager implements Listener {
    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    public SelectionManager(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public Location getPos1(Player player) {
        return pos1.get(player.getUniqueId());
    }

    public Location getPos2(Player player) {
        return pos2.get(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.WOOD_AXE) return;

        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            pos1.put(player.getUniqueId(), event.getClickedBlock().getLocation());
            player.sendMessage("Position one set at " + event.getClickedBlock().getLocation().toVector());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            pos2.put(player.getUniqueId(), event.getClickedBlock().getLocation());
            player.sendMessage("Position two set at " + event.getClickedBlock().getLocation().toVector());
        }
    }
}
