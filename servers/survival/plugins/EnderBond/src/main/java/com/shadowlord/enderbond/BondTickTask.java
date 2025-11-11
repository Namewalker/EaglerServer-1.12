package shadowlord.enderbond;

import org.bukkit.Bukkit;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class BondTickTask extends BukkitRunnable {
    private final EnderBond plugin;
    public BondTickTask(EnderBond plugin) { this.plugin = plugin; }

    @Override
    public void run() {
        // increment bond for players spending time near companion
        for (Map.Entry<java.util.UUID, java.util.UUID> e : plugin.getCompanionMap().entrySet()) {
            Player p = Bukkit.getPlayer(e.getKey());
            if (p == null) continue;

            // find companion by iterating worlds/entities (1.12 compatible)
            Enderman comp = null;
            for (org.bukkit.World w : Bukkit.getWorlds()) {
                for (org.bukkit.entity.Entity ent : w.getEntities()) {
                    if (ent.getUniqueId().equals(e.getValue()) && ent instanceof Enderman) {
                        comp = (Enderman) ent;
                        break;
                    }
                }
                if (comp != null) break;
            }
            if (comp == null) continue;

            double dist = comp.getLocation().distance(p.getLocation());
            if (dist <= plugin.getConfig().getDouble("bonding.nearby_distance", 6.0)) {
                int add = plugin.getConfig().getInt("bonding.time_nearby_bonus", 1);
                int current = plugin.getBondMap().getOrDefault(e.getKey(), 0);
                int nw = Math.min(plugin.getConfig().getInt("bonding.max_level", 100), current + add);
                plugin.getBondMap().put(e.getKey(), nw);
                if (nw >= plugin.getConfig().getInt("bonding.refuge_threshold", 80) && current < nw) {
                    // broadcast unlock once
                    Bukkit.broadcastMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                            "&d[EnderBond] &7" + p.getName() + " has unlocked the &5Ender Refuge&7. Use &a/enderbond refuge &7to enter or leave this realm."));
                }
            }
        }
        plugin.saveState();
    }
}
