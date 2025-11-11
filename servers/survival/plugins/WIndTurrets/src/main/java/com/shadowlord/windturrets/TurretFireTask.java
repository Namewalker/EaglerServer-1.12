package shadowlord.windturrets;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class TurretFireTask extends BukkitRunnable {

    private final WindTurrets plugin;

    public TurretFireTask(WindTurrets plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Collection<Turret> all = plugin.getTurrets().values();
        if (all.isEmpty()) return;

        for (Turret t : all) {
            if (!t.isEnabled()) continue;
            Location loc = t.getLocation();
            if (loc == null) continue;
            // find targets
            List<Player> targets = loc.getWorld().getPlayers().stream()
                    .filter(pl -> !pl.getName().equalsIgnoreCase(t.getOwner()))
                    .filter(pl -> !pl.hasPermission("windturrets.bypass"))
                    .filter(pl -> pl.getLocation().distance(loc) <= t.getRadius())
                    .collect(Collectors.toList());
            if (targets.isEmpty()) continue;

            int max = Math.max(1, plugin.getConfig().getInt("max_targets_per_scan", 1));
            Collections.shuffle(targets);
            List<Player> chosen = targets.stream().limit(max).collect(Collectors.toList());

            for (Player target : chosen) {
                // Fire a snowball with velocity towards target (acts as wind charge)
                Location eye = loc.clone().add(0, 0.6, 0);
                Snowball s = loc.getWorld().spawn(eye, Snowball.class);
                org.bukkit.util.Vector v = target.getLocation().add(0, 1.0, 0).toVector().subtract(eye.toVector()).normalize().multiply(1.6);
                s.setVelocity(v);
                // tag as wind charge so listener can identify it
                s.setMetadata("windcharge", new FixedMetadataValue(plugin, t.getId()));
                // optional: set shooter if desired
                try { s.setShooter(null); } catch (Throwable ignored) {}

                // particles along firing
                for (int i = 0; i < 6; i++) {
                    eye.getWorld().spawnParticle(org.bukkit.Particle.SPELL_WITCH, eye.clone().add(Math.random()-0.5, Math.random()-0.5, Math.random()-0.5), 1, 0,0,0, 0.01);
                }
            }
        }
    }

    public void cancelAll() {
        this.cancel();
    }
}
