package com.thecode.miniedit.edit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class BrushTool implements Listener {
    private final Map<UUID, Brush> brushes = new HashMap<>();

    public BrushTool(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void bindBrush(Player player, Material toolItem, Material fillMaterial, int radius) {
        brushes.put(player.getUniqueId(), new Brush(toolItem, fillMaterial, radius));
        player.sendMessage("Bound sphere brush: Block=" + fillMaterial + ", Radius=" + radius);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Brush brush = brushes.get(player.getUniqueId());
        if (brush == null) return;

        if (event.getItem() == null || event.getItem().getType() != brush.toolItem) return;

        Location center = event.getClickedBlock().getLocation().add(0, 1, 0);
        int r = brush.radius;
        Material mat = brush.fillMaterial;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (x*x + y*y + z*z <= r*r) {
                        center.getWorld().getBlockAt(
                            center.clone().add(x, y, z)
                        ).setType(mat);
                    }
                }
            }
        }
        player.sendMessage("Sphere created: " + mat + " (r=" + r + ")");
    }

    private static class Brush {
        Material toolItem;
        Material fillMaterial;
        int radius;
        Brush(Material toolItem, Material fillMaterial, int radius) {
            this.toolItem = toolItem;
            this.fillMaterial = fillMaterial;
            this.radius = radius;
        }
    }
}
