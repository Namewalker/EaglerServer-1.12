package com.eagler.immersiveportals;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.entity.ArmorStand;
import org.bukkit.World;
import java.util.*;

import org.bukkit.event.player.PlayerMoveEvent;

public class PortalManager implements Listener {
    private final JavaPlugin plugin;
    private int portalCounter = 1;
    private final Map<Integer, Portal> portals = new HashMap<>();
    private final Map<Integer, Integer> portalLinks = new HashMap<>();

    public PortalManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null || block.getType() != Material.FIRE) return;
        // Check for portal shape around the ignited block
        PortalShape shape = PortalShape.detect(block.getLocation());
        if (shape != null) {
            int portalId = portalCounter++;
            Portal portal = new Portal(portalId, shape.getBlocks(), block.getWorld());
            portals.put(portalId, portal);
            portal.spawnNameTag(plugin);
            player.sendMessage("Portal created with ID: " + portalId);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        for (Portal portal : portals.values()) {
            if (isInsidePortal(to, portal)) {
                Integer linkedId = portalLinks.get(portal.getId());
                if (linkedId != null) {
                    Portal linkedPortal = portals.get(linkedId);
                    if (linkedPortal != null) {
                        Location dest = linkedPortal.getCenter().add(0, 1, 0);
                        player.teleport(dest);
                        player.sendMessage("Teleported to linked portal #" + linkedId);
                        return;
                    }
                }
            }
        }
    }

    private boolean isInsidePortal(Location loc, Portal portal) {
        for (Location blockLoc : portal.getBlocks()) {
            if (loc.getBlockX() == blockLoc.getBlockX() && loc.getBlockY() == blockLoc.getBlockY() && loc.getBlockZ() == blockLoc.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    public void linkPortals(int id1, int id2, Player player) {
        if (portals.containsKey(id1) && portals.containsKey(id2)) {
            portalLinks.put(id1, id2);
            portalLinks.put(id2, id1);
            player.sendMessage("Linked portal " + id1 + " with portal " + id2);
        } else {
            player.sendMessage("Invalid portal IDs.");
        }
    }

    // ...other methods for teleportation, etc.
}
