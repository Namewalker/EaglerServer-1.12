
package com.eagler.immersiveportals;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
// Removed BlockIgniteEvent import
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.entity.ArmorStand;
import org.bukkit.World;
import java.util.*;

import org.bukkit.event.player.PlayerMoveEvent;

public class PortalManager implements Listener {
    private final File portalDataFile;

    // Load portals from YAML file on startup
    public void loadPortals() {
        if (!portalDataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(portalDataFile);
        List<Map<?, ?>> portalList = config.getMapList("portals");
        for (Map<?, ?> portalMap : portalList) {
            int id = (int) portalMap.get("id");
            String name = (String) portalMap.get("name");
            String worldName = (String) portalMap.get("world");
            World world = plugin.getServer().getWorld(worldName);
            List<?> blocksRaw = (List<?>) portalMap.get("blocks");
            List<Location> blocks = new ArrayList<>();
            for (Object blockObj : blocksRaw) {
                Map<?, ?> blockMap = (Map<?, ?>) blockObj;
                int x = (int) blockMap.get("x");
                int y = (int) blockMap.get("y");
                int z = (int) blockMap.get("z");
                blocks.add(new Location(world, x, y, z));
            }
            Portal portal = new Portal(id, blocks, world, name);
            portals.put(id, portal);
            portalNames.put(name, id);
            portal.spawnNameTag(plugin);
            portalCounter = Math.max(portalCounter, id + 1);
        }
    }

    // Save portals to YAML file
    public void savePortals() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> portalList = new ArrayList<>();
        for (Map.Entry<Integer, Portal> entry : portals.entrySet()) {
            Portal portal = entry.getValue();
            Map<String, Object> portalMap = new HashMap<>();
            portalMap.put("id", portal.getId());
            portalMap.put("name", getPortalNameById(portal.getId()));
            portalMap.put("world", portal.getBlocks().get(0).getWorld().getName());
            List<Map<String, Object>> blocksArr = new ArrayList<>();
            for (Location loc : portal.getBlocks()) {
                Map<String, Object> b = new HashMap<>();
                b.put("x", loc.getBlockX());
                b.put("y", loc.getBlockY());
                b.put("z", loc.getBlockZ());
                blocksArr.add(b);
            }
            portalMap.put("blocks", blocksArr);
            portalList.add(portalMap);
        }
        config.set("portals", portalList);
        try {
            config.save(portalDataFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save portals: " + e.getMessage());
        }
    }

    private String getPortalNameById(int id) {
        for (Map.Entry<String, Integer> entry : portalNames.entrySet()) {
            if (entry.getValue() == id) return entry.getKey();
        }
        return "";
    }
    private final JavaPlugin plugin;
    private int portalCounter = 1;
    private final Map<Integer, Portal> portals = new HashMap<>();
    private final Map<Integer, Integer> portalLinks = new HashMap<>();
    private final Map<String, Integer> portalNames = new HashMap<>();

    public PortalManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.portalDataFile = new File(plugin.getDataFolder(), "portals.json");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        loadPortals();
    }

        public boolean setPortalNameTagVisible(String name, boolean visible, Player player) {
            Integer id = portalNames.get(name);
            if (id != null && portals.containsKey(id)) {
                Portal portal = portals.get(id);
                if (portal != null) {
                    portal.setNameTagVisible(visible);
                    player.sendMessage("Portal name tag visibility set to " + visible + " for '" + name + "'.");
                    return true;
                }
            }
            return false;
        }

    // Removed automatic portal creation

    public void setPortal(Location loc1, Location loc2, String name, Player player) {
        int portalId = portalCounter++;
        List<Location> blocks = new ArrayList<>();
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        World world = loc1.getWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(new Location(world, x, y, z));
                }
            }
        }
    Portal portal = new Portal(portalId, blocks, world, name);
        portals.put(portalId, portal);
        portalNames.put(name, portalId);
        portal.spawnNameTag(plugin);
        savePortals();
        player.sendMessage("Portal '" + name + "' created from (" + minX + "," + minY + "," + minZ + ") to (" + maxX + "," + maxY + "," + maxZ + ")");
    }

    public boolean deletePortalByName(String name, Player player) {
        Integer id = portalNames.get(name);
        if (id != null && portals.containsKey(id)) {
            Portal portal = portals.get(id);
            if (portal != null) {
                // Remove name tag entity if exists
                if (portal.nameTag != null) {
                    portal.nameTag.remove();
                }
            }
            portals.remove(id);
            portalNames.remove(name);
            portalLinks.remove(id);
            savePortals();
            player.sendMessage("Portal '" + name + "' deleted.");
            return true;
        }
        return false;
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
                        // Move player one block forward in their facing direction
                        dest = dest.add(player.getLocation().getDirection().normalize());
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
}
