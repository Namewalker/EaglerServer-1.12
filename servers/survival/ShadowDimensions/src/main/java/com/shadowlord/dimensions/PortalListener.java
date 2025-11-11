package shadowlord.dimensions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class PortalListener implements Listener {

    private final ShadowDimensions plugin;

    public PortalListener(ShadowDimensions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent ev) {
        Location to = ev.getTo();
        if (to == null) return;
        Player p = ev.getPlayer();

        for (Portal portal : plugin.getAllPortals()) {
            if (portal.contains(to)) {
                // teleport to target world spawn (or create if missing)
                String target = portal.getTargetDimension();
                org.bukkit.World w = Bukkit.getWorld(target);
                if (w == null) {
                    // try to create simple world using default generator
                    w = plugin.createDimension(target, plugin.getConfig().getString("default_generator", "normal"), null);
                }
                Location dest = w.getSpawnLocation().clone();
                dest.setY(Math.max(1, dest.getBlockY()));
                p.teleport(dest);
                p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', "&aYou travel to &e" + target));
                return;
            }
        }
    }
}
