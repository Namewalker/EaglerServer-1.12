package shadowlord.windturrets;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class TurretPlacementListener implements Listener {

    private final WindTurrets plugin;

    public TurretPlacementListener(WindTurrets plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent ev) {
        if (ev.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = ev.getPlayer();
        ItemStack item = ev.getItem();
        if (item == null) return;

        // Only react to the crafted Turret Core item
        if (!Util.isTurretCore(item, plugin.getConfig())) return;

        // clicked block must be QUARTZ_BLOCK (pillar) and block below must be DISPENSER facing UP
        org.bukkit.block.Block clicked = ev.getClickedBlock();
        if (clicked == null) return;
        if (clicked.getType() != Material.QUARTZ_BLOCK) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou must click a Quartz Pillar block as the turret top."));
            return;
        }

        org.bukkit.block.Block below = clicked.getRelative(BlockFace.DOWN);
        if (below == null || below.getType() != Material.DISPENSER) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cPlace the Quartz Pillar on top of a Dispenser facing up."));
            return;
        }

        // Verify dispenser facing up using 1.12 MaterialData API (org.bukkit.material.Dispenser)
        boolean facingUp = false;
        try {
            org.bukkit.material.MaterialData md = below.getState().getData();
            if (md instanceof org.bukkit.material.Dispenser) {
                org.bukkit.material.Dispenser d = (org.bukkit.material.Dispenser) md;
                if (d.getFacing() == BlockFace.UP) facingUp = true;
            } else {
                // fallback: if we can't determine facing, be permissive if quartz sits directly above
                facingUp = true;
            }
        } catch (Throwable t) {
            // permissive fallback on unexpected errors for 1.12 compatibility
            facingUp = true;
        }

        if (!facingUp) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDispenser must be facing up."));
            return;
        }

        // All checks pass: register turret
        String id = "turret-" + UUID.randomUUID().toString().substring(0, 8);
        int radius = plugin.getConfig().getInt("default_radius", 10);
        Turret t = new Turret(id, p.getName(), clicked.getLocation(), radius, true);
        plugin.addTurret(t);

        // consume one item from hand
        Util.consumeOne(p, item);

        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aWind Turret &e" + id + " &aactivated and bound to &e" + p.getName()));
        // particle feedback
        org.bukkit.Location center = clicked.getLocation().add(0.5, 0.5, 0.5);
        for (int i = 0; i < 16; i++) {
            center.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, center, 1, 0.3, 0.3, 0.3, 0.01);
        }

        ev.setCancelled(true);
    }
}
